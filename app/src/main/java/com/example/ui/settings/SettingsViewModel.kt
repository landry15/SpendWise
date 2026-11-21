package com.example.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CategoryEntity
import com.example.data.preferences.CurrencyOption
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ExpenseRepository
import com.example.util.CsvExporter
import com.example.util.ValidationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CategoryFormState(
    val isVisible: Boolean = false,
    val editingCategory: CategoryEntity? = null,
    val name: String = "",
    val iconKey: String = "shopping",
    val colorHex: Long = 0xFF0B5D4E,
    val nameError: String? = null
)

data class ExportBannerState(
    val fileName: String,
    val filePath: String,
    val rowCount: Int,
    val uri: Uri
)

data class SettingsUiState(
    val currency: CurrencyOption = CurrencyOption.EUR,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val categories: List<CategoryEntity> = emptyList(),
    val expenseCount: Int = 0,
    val categoryForm: CategoryFormState = CategoryFormState(),
    val lastExport: ExportBannerState? = null,
    val isResetDialogVisible: Boolean = false,
    val statusMessage: String? = null
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val currentUserId = MutableStateFlow<Long?>(null)
    private val categoryForm = MutableStateFlow(CategoryFormState())
    private val lastExport = MutableStateFlow<ExportBannerState?>(null)
    private val resetDialogVisible = MutableStateFlow(false)
    private val statusMessage = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val categoriesFlow = authRepository.loggedInUserId.flatMapLatest { uid ->
        currentUserId.value = uid
        if (uid == null) flowOf(emptyList()) else categoryRepository.observeCategories(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val expensesFlow = authRepository.loggedInUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else expenseRepository.observeExpenses(uid)
    }

    private val dialogStateFlow = combine(
        categoryForm,
        lastExport,
        resetDialogVisible,
        statusMessage
    ) { form, exportBanner, resetOpen, msg ->
        SettingsDialogs(form, exportBanner, resetOpen, msg)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.currency,
        preferencesRepository.themeMode,
        categoriesFlow,
        expensesFlow,
        dialogStateFlow
    ) { currency, themeMode, categories, expenses, dialogs ->
        SettingsUiState(
            currency = currency,
            themeMode = themeMode,
            categories = categories,
            expenseCount = expenses.size,
            categoryForm = dialogs.categoryForm,
            lastExport = dialogs.lastExport,
            isResetDialogVisible = dialogs.resetDialogVisible,
            statusMessage = dialogs.statusMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setCurrency(option: CurrencyOption) {
        viewModelScope.launch {
            preferencesRepository.setCurrency(option)
            statusMessage.value = "Devise définie sur ${option.label}"
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun openAddCategory() {
        categoryForm.value = CategoryFormState(isVisible = true)
    }

    fun openEditCategory(category: CategoryEntity) {
        categoryForm.value = CategoryFormState(
            isVisible = true,
            editingCategory = category,
            name = category.name,
            iconKey = category.iconKey,
            colorHex = category.colorHex
        )
    }

    fun dismissCategoryForm() {
        categoryForm.value = CategoryFormState(isVisible = false)
    }

    fun onCategoryNameChange(value: String) {
        categoryForm.update { it.copy(name = value, nameError = null) }
    }

    fun onCategoryIconSelect(iconKey: String) {
        categoryForm.update { it.copy(iconKey = iconKey) }
    }

    fun onCategoryColorSelect(colorHex: Long) {
        categoryForm.update { it.copy(colorHex = colorHex) }
    }

    fun saveCategory() {
        val uid = currentUserId.value ?: return
        val form = categoryForm.value
        val nameErr = ValidationUtils.validateCategoryName(form.name)
        if (nameErr != null) {
            categoryForm.update { it.copy(nameError = nameErr) }
            return
        }

        val duplicate = uiState.value.categories.any {
            it.name.equals(form.name.trim(), ignoreCase = true) && it.id != form.editingCategory?.id
        }
        if (duplicate) {
            categoryForm.update { it.copy(nameError = "Une catégorie avec ce nom existe déjà") }
            return
        }

        viewModelScope.launch {
            val editing = form.editingCategory
            if (editing == null) {
                categoryRepository.addCategory(
                    userId = uid,
                    name = form.name,
                    iconKey = form.iconKey,
                    colorHex = form.colorHex
                )
                statusMessage.value = "Catégorie ajoutée"
            } else {
                categoryRepository.updateCategory(
                    editing.copy(
                        name = form.name.trim(),
                        iconKey = form.iconKey,
                        colorHex = form.colorHex
                    )
                )
                statusMessage.value = "Catégorie mise à jour"
            }
            categoryForm.value = CategoryFormState(isVisible = false)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            val deleted = categoryRepository.deleteCustomCategory(uid, category)
            statusMessage.value = if (deleted) {
                "Catégorie supprimée (dépenses déplacées vers Autre)"
            } else {
                "Les catégories par défaut ne peuvent pas être supprimées"
            }
        }
    }

    fun exportCsv(context: Context, onReadyToShare: (ExportBannerState) -> Unit = {}) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            val expenses = expenseRepository.getExpensesSnapshot(uid)
            if (expenses.isEmpty()) {
                statusMessage.value = "Aucune dépense à exporter"
                return@launch
            }
            val result = withContext(Dispatchers.IO) {
                CsvExporter.exportToLocalCsv(context, expenses, uiState.value.currency)
            }
            val banner = ExportBannerState(
                fileName = result.file.name,
                filePath = result.file.absolutePath,
                rowCount = result.rowCount,
                uri = result.uri
            )
            lastExport.value = banner
            statusMessage.value = "Export CSV créé : ${result.file.name}"
            onReadyToShare(banner)
        }
    }

    fun openResetDialog() {
        resetDialogVisible.value = true
    }

    fun dismissResetDialog() {
        resetDialogVisible.value = false
    }

    fun confirmResetAllData() {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            expenseRepository.resetUserData(uid)
            resetDialogVisible.value = false
            lastExport.value = null
            statusMessage.value = "Toutes vos dépenses et catégories personnalisées ont été réinitialisées"
        }
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    private data class SettingsDialogs(
        val categoryForm: CategoryFormState,
        val lastExport: ExportBannerState?,
        val resetDialogVisible: Boolean,
        val statusMessage: String?
    )

    companion object {
        fun factory(
            authRepository: AuthRepository,
            categoryRepository: CategoryRepository,
            expenseRepository: ExpenseRepository,
            preferencesRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    authRepository = authRepository,
                    categoryRepository = categoryRepository,
                    expenseRepository = expenseRepository,
                    preferencesRepository = preferencesRepository
                ) as T
            }
        }
    }
}
