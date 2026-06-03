package com.example.budgetbruprog7313.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class GoalsViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentIncome = MutableStateFlow(5000.0)
    val currentIncome: StateFlow<Double> = _currentIncome.asStateFlow()

    private val _currentMin = MutableStateFlow<Double?>(null)
    val currentMin: StateFlow<Double?> = _currentMin.asStateFlow()

    private val _currentMax = MutableStateFlow<Double?>(null)
    val currentMax: StateFlow<Double?> = _currentMax.asStateFlow()

    private val _currentMonthTotal = MutableStateFlow(0.0)
    val currentMonthTotal: StateFlow<Double> = _currentMonthTotal.asStateFlow()

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    private val _showSuccessMessage = MutableStateFlow(false)
    val showSuccessMessage: StateFlow<Boolean> = _showSuccessMessage.asStateFlow()

    private val _successMessageText = MutableStateFlow("")
    val successMessageText: StateFlow<String> = _successMessageText.asStateFlow()

    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val startMillis = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endMillis = calendar.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        return Pair(startMillis, endMillis)
    }

    init {
        viewModelScope.launch {
            val (start, end) = getCurrentMonthRange()
            // Long millis passed directly
            repository.getEntriesBetweenDates(start, end).collect { entries ->
                _currentMonthTotal.value = entries.sumOf { it.amount }
            }
        }

        viewModelScope.launch {
            repository.getGoals().collect { settings ->
                _currentMin.value = settings?.minMonthlyGoal
                _currentMax.value = settings?.maxMonthlyGoal
                _isLoading.value = false
            }
        }

        viewModelScope.launch {
            repository.getMonthlyIncome().collect { income ->
                _currentIncome.value = income ?: 5000.0
            }
        }
    }

    fun saveGoals(min: Double, max: Double) {
        viewModelScope.launch {
            try {
                repository.saveGoals(min, max)
                _currentMin.value = min
                _currentMax.value = max
                _successMessageText.value = "Goals saved!"
                _showSuccessMessage.value = true
                delay(2000)
                _showSuccessMessage.value = false
            } catch (e: Exception) {
                _saveMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun saveIncome(income: Double) {
        viewModelScope.launch {
            try {
                repository.saveMonthlyIncome(income)
                _currentIncome.value = income
                _successMessageText.value = "Income updated!"
                _showSuccessMessage.value = true
                delay(2000)
                _showSuccessMessage.value = false
            } catch (e: Exception) {
                _saveMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun clearSuccessMessage() {
        _showSuccessMessage.value = false
        _saveMessage.value = null
    }
}