package com.example.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ExpenseWithCategory
import com.example.data.preferences.CurrencyOption
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ExpenseRepository
import com.example.util.ValidationUtils
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

data class ExpenseFormState(
    val isVisible: Boolean = false,
    val editingExpenseId: Long? = null,
    val amountText: String = "",
    val description: String = "",
    val selectedCategoryId: Long? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val amountError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val dateError: String? = null
)

data class ExpensesUiState(
    val expenses: List<ExpenseWithCategory> = emptyList(),
    val filteredExpenses: List<ExpenseWithCategory> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val currency: CurrencyOption = CurrencyOption.EUR,
    val searchQuery: String = "",
    val selectedCategoryFilterId: Long? = null,
    val formState: ExpenseFormState = ExpenseFormState(),
    val pendingDeleteExpense: ExpenseWithCategory? = null,
    val statusMessage: String? = null
)

class ExpensesViewModel(
    private val authRepository: AuthRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedCategoryFilterId = MutableStateFlow<Long?>(null)
    private val formState = MutableStateFlow(ExpenseFormState())
    private val pendingDelete = MutableStateFlow<ExpenseWithCategory?>(null)
    private val statusMessage = MutableStateFlow<String?>(null)
    private val currentUserId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userExpensesFlow = authRepository.loggedInUserId.flatMapLatest { uid ->
        currentUserId.value = uid
        if (uid == null) flowOf(emptyList()) else expenseRepository.observeExpenses(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userCategoriesFlow = authRepository.loggedInUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else categoryRepository.observeCategories(uid)
    }

    private val filterControlsFlow = combine(
        searchQuery,
        selectedCategoryFilterId,
        formState,
        pendingDelete,
        statusMessage
    ) { query, catFilter, form, deleteTarget, message ->
        FilterControls(query, catFilter, form, deleteTarget, message)
    }

    val uiState: StateFlow<ExpensesUiState> = combine(
        userExpensesFlow,
        userCategoriesFlow,
        preferencesRepository.currency,
        filterControlsFlow
    ) { expenses, categories, currency, controls ->
        val q = controls.searchQuery.trim()
        val filtered = expenses.filter { item ->
            val matchesCategory = controls.selectedCategoryFilterId == null ||
                item.expense.categoryId == controls.selectedCategoryFilterId
            val matchesQuery = q.isEmpty() ||
                item.expense.description.contains(q, ignoreCase = true) ||
                item.category.name.contains(q, ignoreCase = true)
            matchesCategory && matchesQuery
        }
        ExpensesUiState(
            expenses = expenses,
            filteredExpenses = filtered,
            categories = categories,
            currency = currency,
            searchQuery = controls.searchQuery,
            selectedCategoryFilterId = controls.selectedCategoryFilterId,
            formState = controls.formState,
            pendingDeleteExpense = controls.pendingDelete,
            statusMessage = controls.statusMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpensesUiState()
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onCategoryFilterSelect(categoryId: Long?) {
        selectedCategoryFilterId.value = if (selectedCategoryFilterId.value == categoryId) null else categoryId
    }

    fun openAddExpense() {
        val defaultCategoryId = uiState.value.categories.firstOrNull()?.id
        formState.value = ExpenseFormState(
            isVisible = true,
            editingExpenseId = null,
            selectedCategoryId = defaultCategoryId,
            dateMillis = System.currentTimeMillis()
        )
    }

    fun openEditExpense(item: ExpenseWithCategory) {
        formState.value = ExpenseFormState(
            isVisible = true,
            editingExpenseId = item.expense.id,
            amountText = String.format(java.util.Locale.US, "%.2f", item.expense.amount),
            description = item.expense.description,
            selectedCategoryId = item.expense.categoryId,
            dateMillis = item.expense.dateMillis
        )
    }

    fun dismissForm() {
        formState.value = ExpenseFormState(isVisible = false)
    }

    fun onFormAmountChange(value: String) {
        formState.update { it.copy(amountText = value, amountError = null) }
    }

    fun onFormDescriptionChange(value: String) {
        formState.update { it.copy(description = value, descriptionError = null) }
    }

    fun onFormCategorySelect(categoryId: Long) {
        formState.update { it.copy(selectedCategoryId = categoryId, categoryError = null) }
    }

    fun onFormDateChange(dateMillis: Long) {
        formState.update { it.copy(dateMillis = dateMillis, dateError = null) }
    }

    fun saveExpense() {
        val uid = currentUserId.value ?: return
        val current = formState.value
        val (parsedAmount, amountErr) = ValidationUtils.validateExpenseAmount(current.amountText)
        val descErr = ValidationUtils.validateExpenseDescription(current.description)
        val catErr = if (current.selectedCategoryId == null) {
            "Veuillez sélectionner une catégorie"
        } else null
        val dateErr = if (current.dateMillis <= 0L) {
            "Veuillez choisir une date valide"
        } else null

        if (amountErr != null || descErr != null || catErr != null || dateErr != null || parsedAmount == null) {
            formState.update {
                it.copy(
                    amountError = amountErr,
                    descriptionError = descErr,
                    categoryError = catErr,
                    dateError = dateErr
                )
            }
            return
        }

        val categoryId = current.selectedCategoryId!!
        viewModelScope.launch {
            if (current.editingExpenseId == null) {
                expenseRepository.addExpense(
                    userId = uid,
                    categoryId = categoryId,
                    amount = parsedAmount,
                    description = current.description,
                    dateMillis = current.dateMillis
                )
                statusMessage.value = "Dépense ajoutée"
            } else {
                val existing = uiState.value.expenses
                    .firstOrNull { it.expense.id == current.editingExpenseId }?.expense
                expenseRepository.updateExpense(
                    ExpenseEntity(
                        id = current.editingExpenseId,
                        userId = uid,
                        categoryId = categoryId,
                        amount = parsedAmount,
                        description = current.description,
                        dateMillis = current.dateMillis,
                        createdAt = existing?.createdAt ?: System.currentTimeMillis()
                    )
                )
                statusMessage.value = "Dépense mise à jour"
            }
            formState.value = ExpenseFormState(isVisible = false)
        }
    }

    fun requestDeleteExpense(item: ExpenseWithCategory) {
        pendingDelete.value = item
    }

    fun cancelDeleteExpense() {
        pendingDelete.value = null
    }

    fun confirmDeleteExpense() {
        val target = pendingDelete.value ?: return
        viewModelScope.launch {
            expenseRepository.deleteExpense(target.expense.id)
            pendingDelete.value = null
            statusMessage.value = "Dépense supprimée"
        }
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    private data class FilterControls(
        val searchQuery: String,
        val selectedCategoryFilterId: Long?,
        val formState: ExpenseFormState,
        val pendingDelete: ExpenseWithCategory?,
        val statusMessage: String?
    )

    companion object {
        fun factory(
            authRepository: AuthRepository,
            expenseRepository: ExpenseRepository,
            categoryRepository: CategoryRepository,
            preferencesRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ExpensesViewModel(
                    authRepository = authRepository,
                    expenseRepository = expenseRepository,
                    categoryRepository = categoryRepository,
                    preferencesRepository = preferencesRepository
                ) as T
            }
        }
    }
}
