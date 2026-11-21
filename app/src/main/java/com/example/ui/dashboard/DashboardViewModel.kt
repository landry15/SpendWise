package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseWithCategory
import com.example.data.preferences.CurrencyOption
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ExpenseRepository
import com.example.util.FormatUtils
import com.example.util.ValidationUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DashboardPeriod(val label: String) {
    DAY("Jour"),
    WEEK("Semaine"),
    MONTH("Mois")
}

data class CategoryBreakdownItem(
    val category: CategoryEntity,
    val totalAmount: Double,
    val percentage: Float
)

data class DashboardUiState(
    val userName: String = "",
    val monthlyBudget: Double = 1500.0,
    val monthlySpent: Double = 0.0,
    val remainingBudget: Double = 1500.0,
    val budgetRatio: Float = 0f,
    val selectedPeriod: DashboardPeriod = DashboardPeriod.MONTH,
    val selectedCategoryId: Long? = null,
    val periodTotal: Double = 0.0,
    val categories: List<CategoryEntity> = emptyList(),
    val breakdown: List<CategoryBreakdownItem> = emptyList(),
    val recentExpenses: List<ExpenseWithCategory> = emptyList(),
    val currency: CurrencyOption = CurrencyOption.EUR,
    val isBudgetDialogVisible: Boolean = false,
    val budgetInput: String = "",
    val budgetError: String? = null
)

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val selectedPeriod = MutableStateFlow(DashboardPeriod.MONTH)
    private val selectedCategoryId = MutableStateFlow<Long?>(null)
    private val budgetDialogVisible = MutableStateFlow(false)
    private val budgetInput = MutableStateFlow("")
    private val budgetError = MutableStateFlow<String?>(null)
    private val currentUserId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val expensesFlow = authRepository.loggedInUserId.flatMapLatest { uid ->
        currentUserId.value = uid
        if (uid == null) flowOf(emptyList()) else expenseRepository.observeExpenses(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val categoriesFlow = authRepository.loggedInUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else categoryRepository.observeCategories(uid)
    }

    private val controlsFlow = combine(
        selectedPeriod,
        selectedCategoryId,
        budgetDialogVisible,
        budgetInput,
        budgetError
    ) { period, catId, dialogOpen, bInput, bErr ->
        DashboardControls(period, catId, dialogOpen, bInput, bErr)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        authRepository.currentUser,
        expensesFlow,
        categoriesFlow,
        preferencesRepository.currency,
        controlsFlow
    ) { user, expenses, categories, currency, controls ->
        val now = System.currentTimeMillis()
        val monthStart = FormatUtils.startOfMonthMillis(now)
        val endOfToday = FormatUtils.endOfDayMillis(now)
        val periodStart = when (controls.period) {
            DashboardPeriod.DAY -> FormatUtils.startOfDayMillis(now)
            DashboardPeriod.WEEK -> FormatUtils.startOfWeekMillis(now)
            DashboardPeriod.MONTH -> monthStart
        }

        val monthlySpent = expenses
            .filter { it.expense.dateMillis in monthStart..endOfToday }
            .sumOf { it.expense.amount }

        val budget = user?.monthlyBudget ?: 1500.0
        val remaining = budget - monthlySpent
        val ratio = if (budget > 0.0) (monthlySpent / budget).toFloat().coerceAtLeast(0f) else 0f

        val periodExpensesAllCats = expenses.filter {
            it.expense.dateMillis in periodStart..endOfToday
        }

        val periodExpensesFiltered = periodExpensesAllCats.filter {
            controls.selectedCategoryId == null || it.expense.categoryId == controls.selectedCategoryId
        }

        val periodTotal = periodExpensesFiltered.sumOf { it.expense.amount }

        val breakdownSourceTotal = periodExpensesAllCats.sumOf { it.expense.amount }
        val breakdown = if (breakdownSourceTotal <= 0.0) {
            emptyList()
        } else {
            periodExpensesAllCats
                .groupBy { it.category }
                .map { (cat, list) ->
                    val sum = list.sumOf { it.expense.amount }
                    CategoryBreakdownItem(
                        category = cat,
                        totalAmount = sum,
                        percentage = (sum / breakdownSourceTotal).toFloat()
                    )
                }
                .sortedByDescending { it.totalAmount }
        }

        DashboardUiState(
            userName = user?.name.orEmpty(),
            monthlyBudget = budget,
            monthlySpent = monthlySpent,
            remainingBudget = remaining,
            budgetRatio = ratio,
            selectedPeriod = controls.period,
            selectedCategoryId = controls.selectedCategoryId,
            periodTotal = periodTotal,
            categories = categories,
            breakdown = breakdown,
            recentExpenses = periodExpensesFiltered.take(5),
            currency = currency,
            isBudgetDialogVisible = controls.isBudgetDialogVisible,
            budgetInput = controls.budgetInput,
            budgetError = controls.budgetError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun onPeriodSelect(period: DashboardPeriod) {
        selectedPeriod.value = period
    }

    fun onCategorySelect(categoryId: Long?) {
        selectedCategoryId.value = if (selectedCategoryId.value == categoryId) null else categoryId
    }

    fun openBudgetDialog() {
        val currentBudget = uiState.value.monthlyBudget
        budgetInput.value = String.format(java.util.Locale.US, "%.0f", currentBudget)
        budgetError.value = null
        budgetDialogVisible.value = true
    }

    fun dismissBudgetDialog() {
        budgetDialogVisible.value = false
    }

    fun onBudgetInputChange(value: String) {
        budgetInput.value = value
        budgetError.value = null
    }

    fun saveMonthlyBudget() {
        val uid = currentUserId.value ?: return
        val (parsed, err) = ValidationUtils.validateBudget(budgetInput.value)
        if (err != null || parsed == null) {
            budgetError.value = err
            return
        }
        viewModelScope.launch {
            authRepository.updateMonthlyBudget(uid, parsed)
            budgetDialogVisible.value = false
        }
    }

    private data class DashboardControls(
        val period: DashboardPeriod,
        val selectedCategoryId: Long?,
        val isBudgetDialogVisible: Boolean,
        val budgetInput: String,
        val budgetError: String?
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
                return DashboardViewModel(
                    authRepository = authRepository,
                    expenseRepository = expenseRepository,
                    categoryRepository = categoryRepository,
                    preferencesRepository = preferencesRepository
                ) as T
            }
        }
    }
}
