package com.example.budgetbruprog7313.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

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

    // Get current month's expenses
    private val currentMonthRange = getCurrentMonthRange()
    private val currentMonthExpenses: Flow<List<com.example.budgetbruprog7313.data.model.ExpenseEntry>> =
        repository.getEntriesBetweenDates(currentMonthRange.first, currentMonthRange.second)

    // Total spent this month
    val totalSpent: StateFlow<Double> = currentMonthExpenses
        .map { expenses -> expenses.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Recent 5 expenses (from all expenses, not just current month)
    val recentExpenses: StateFlow<List<com.example.budgetbruprog7313.data.model.ExpenseEntry>> =
        repository.getEntriesBetweenDates(Date(0), Date()) // Get all expenses
            .map { expenses -> expenses.sortedByDescending { it.date }.take(5) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Available balance (mock: assume R5000 monthly income minus spent)
    val availableBalance: StateFlow<Double> = totalSpent.map { total ->
        5000.0 - total
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000.0)

    fun addQuickExpense(amount: Double, description: String, categoryId: Long) {
        viewModelScope.launch {
            val now = Date()
            repository.addExpenseEntry(
                date = now,
                startTime = "00:00",
                endTime = "23:59",
                description = description,
                amount = amount,
                categoryId = categoryId,
                photoPath = null
            )
        }
    }
}