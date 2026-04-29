package com.example.budgetbruprog7313.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetbruprog7313.data.model.Category
import com.example.budgetbruprog7313.data.model.ExpenseEntry
import com.example.budgetbruprog7313.data.model.Transaction
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Income transactions (in‑memory)
    private val _incomeTransactions = MutableStateFlow<List<Transaction.Income>>(emptyList())
    val incomeTransactions: StateFlow<List<Transaction.Income>> = _incomeTransactions.asStateFlow()

    // Expenses from DB
    private val _expenses = MutableStateFlow<List<ExpenseEntry>>(emptyList())
    val expenses: StateFlow<List<ExpenseEntry>> = _expenses.asStateFlow()

    // Categories map
    private val categoriesMap = mutableMapOf<Long, String>()

    // Combined recent activity (expenses + income)
    val recentActivity: StateFlow<List<Transaction>> = combine(
        _expenses,
        _incomeTransactions
    ) { expenses, incomes ->
        val expenseTransactions = expenses.map { expense ->
            Transaction.Expense(
                id = expense.id,
                amount = expense.amount,
                description = expense.description,
                date = expense.date,
                categoryId = expense.categoryId,
                categoryName = categoriesMap[expense.categoryId] ?: "",
                startTime = expense.startTime,
                endTime = expense.endTime,
                photoPath = expense.photoPath
            )
        }
        (expenseTransactions + incomes).sortedByDescending { it.date }.take(10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun getCurrentMonthRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val start = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val end = calendar.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        return Pair(start, end)
    }

    private val currentMonthRange = getCurrentMonthRange()
    private val currentMonthExpenses = repository.getEntriesBetweenDates(currentMonthRange.first, currentMonthRange.second)

    val totalSpent: StateFlow<Double> = currentMonthExpenses
        .map { it.sumOf { exp -> exp.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val monthlyIncome: StateFlow<Double> = repository.getMonthlyIncome()
        .map { it ?: 5000.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000.0)

    val availableBalance: StateFlow<Double> = combine(totalSpent, monthlyIncome) { spent, income ->
        income - spent
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000.0)

    val categories: StateFlow<List<Category>> = repository.allCategories
        .onEach { list ->
            list.forEach { categoriesMap[it.id] = it.name }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Load expenses
            repository.getEntriesBetweenDates(Date(0), Date()).collect { expensesList ->
                _expenses.value = expensesList.sortedByDescending { it.date }.take(10)
            }
            _isLoading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getEntriesBetweenDates(Date(0), Date()).collect { expensesList ->
                _expenses.value = expensesList.sortedByDescending { it.date }.take(10)
            }
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
            val income = Transaction.Income(
                id = System.currentTimeMillis(),
                amount = amount,
                description = description.ifBlank { "Income Added" },
                date = now,
                source = "Manual"
            )
            val current = _incomeTransactions.value.toMutableList()
            current.add(0, income)
            _incomeTransactions.value = current

            // Update real income in settings
            val currentIncome = repository.getMonthlyIncome().first()
            val newIncome = (currentIncome ?: 5000.0) + amount
            repository.saveMonthlyIncome(newIncome)

            refresh()
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            repository.deleteExpenseById(expenseId)
            refresh()
        }
    }

    fun deleteIncome(incomeId: Long) {
        viewModelScope.launch {
            val current = _incomeTransactions.value.toMutableList()
            current.removeAll { it.id == incomeId }
            _incomeTransactions.value = current
        }
    }
}