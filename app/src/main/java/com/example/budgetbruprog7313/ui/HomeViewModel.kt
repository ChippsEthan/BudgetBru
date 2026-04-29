package com.example.budgetbruprog7313.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetbruprog7313.data.model.Category
import com.example.budgetbruprog7313.data.model.ExpenseEntry
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

    // Get current month's start and end dates
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

    // Real data from database - current month expenses
    private val currentMonthExpenses: Flow<List<ExpenseEntry>> =
        repository.getEntriesBetweenDates(currentMonthRange.first, currentMonthRange.second)

    // Total spent this month
    val totalSpent: StateFlow<Double> = currentMonthExpenses
        .map { expenses -> expenses.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Recent 5 expenses
    val recentExpenses: StateFlow<List<ExpenseEntry>> =
        repository.getEntriesBetweenDates(Date(0), Date())
            .map { expenses -> expenses.sortedByDescending { it.date }.take(5) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real monthly income from database
    private val monthlyIncome: StateFlow<Double> = repository.getMonthlyIncome()
        .map { it ?: 5000.0 } // Default to 5000 if not set
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000.0)

    // Available balance (real income minus spent)
    val availableBalance: StateFlow<Double> = combine(totalSpent, monthlyIncome) { spent, income ->
        income - spent
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000.0)

    // Get all categories for quick add
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
            // Force refresh by collecting again
            totalSpent.collect { /* no-op */ }
            recentExpenses.collect { /* no-op */ }
            availableBalance.collect { /* no-op */ }
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
        }
    }
}