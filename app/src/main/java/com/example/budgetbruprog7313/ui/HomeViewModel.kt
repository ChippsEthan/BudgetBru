package com.example.budgetbruprog7313.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetbruprog7313.data.model.Category
import com.example.budgetbruprog7313.data.model.ExpenseEntry
import com.example.budgetbruprog7313.data.model.IncomeEntry
import com.example.budgetbruprog7313.data.model.Transaction
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Force refresh trigger
    private val _refreshTrigger = MutableStateFlow(0)

    // Get all expenses from database
    private val allExpenses: Flow<List<ExpenseEntry>> = _refreshTrigger.flatMapLatest {
        repository.getEntriesBetweenDates(Date(0), Date())
    }.catch { emit(emptyList()) }

    // Get all incomes from database
    private val allIncomes: Flow<List<IncomeEntry>> = _refreshTrigger.flatMapLatest {
        repository.getAllIncomes()
    }.catch { emit(emptyList()) }

    // Combined recent activity (expenses + income)
    val recentActivity: StateFlow<List<Transaction>> = combine(
        allExpenses,
        allIncomes
    ) { expenses, incomes ->
        val expenseTransactions = expenses.map { expense ->
            Transaction.Expense(
                id = expense.id,
                amount = expense.amount,
                description = expense.description,
                date = expense.date,
                categoryId = expense.categoryId,
                categoryName = "",
                startTime = expense.startTime,
                endTime = expense.endTime,
                photoPath = expense.photoPath
            )
        }
        val incomeTransactions = incomes.map { income ->
            Transaction.Income(
                id = income.id,
                amount = income.amount,
                description = income.description,
                date = income.date,
                source = income.source
            )
        }
        (expenseTransactions + incomeTransactions).sortedByDescending { it.date }.take(15)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun getCurrentMonthRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val startDate = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endDate = calendar.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        return Pair(startDate, endDate)
    }

    private val currentMonthRange = getCurrentMonthRange()
    private val currentMonthExpenses: Flow<List<ExpenseEntry>> = _refreshTrigger.flatMapLatest {
        repository.getEntriesBetweenDates(currentMonthRange.first, currentMonthRange.second)
    }

    val totalSpent: StateFlow<Double> = currentMonthExpenses
        .map { expenses -> expenses.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val recentExpenses: StateFlow<List<ExpenseEntry>> = allExpenses
        .map { expenses -> expenses.sortedByDescending { it.date }.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val monthlyIncome: StateFlow<Double> = repository.getMonthlyIncome()
        .map { it ?: 5000.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000.0)

    val availableBalance: StateFlow<Double> = combine(totalSpent, monthlyIncome) { spent, income ->
        income - spent
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000.0)

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _isLoading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _refreshTrigger.value++
            delay(100)
            _isLoading.value = false
        }
    }

    fun addQuickExpense(amount: Double, description: String, categoryId: Long) {
        viewModelScope.launch {
            val now = Date()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            repository.addExpenseEntry(
                date = now,
                startTime = timeFormat.format(now),
                endTime = timeFormat.format(now),
                description = description,
                amount = amount,
                categoryId = categoryId,
                photoPath = null
            )
            refresh()
        }
    }

    fun addIncome(amount: Double, description: String) {
        viewModelScope.launch {
            val now = Date()
            val incomeEntry = IncomeEntry(
                amount = amount,
                description = description.ifBlank { "Income Added" },
                date = now,
                source = "Manual"
            )
            repository.addIncomeEntry(incomeEntry)

            // Also update the monthly income setting
            val currentIncome = repository.getMonthlyIncome().first()
            val newIncome = (currentIncome ?: 5000.0) + amount
            repository.saveMonthlyIncome(newIncome)

            refresh()
        }
    }

    fun deleteIncome(incomeId: Long) {
        viewModelScope.launch {
            repository.deleteIncomeById(incomeId)
            refresh()
        }
    }

    fun deleteExpense(expense: ExpenseEntry) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            refresh()
        }
    }
}