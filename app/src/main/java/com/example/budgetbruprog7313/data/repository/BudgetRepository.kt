package com.example.budgetbruprog7313.data.repository

import android.util.Log
import com.example.budgetbruprog7313.data.dao.ExpenseEntryDao
import com.example.budgetbruprog7313.data.dao.SettingsDao
import com.example.budgetbruprog7313.data.database.AppDatabase
import com.example.budgetbruprog7313.data.model.Category
import com.example.budgetbruprog7313.data.model.ExpenseEntry
import com.example.budgetbruprog7313.data.model.User
import com.example.budgetbruprog7313.data.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class BudgetRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val categoryDao = db.categoryDao()
    private val entryDao = db.expenseEntryDao()
    private val settingsDao = db.settingsDao()

    private val TAG = "BudgetRepository"

    // ==================== USER METHODS ====================

    suspend fun login(username: String, password: String): User? {
        Log.d(TAG, "Login attempt for user: $username")
        return userDao.login(username, password)
    }

    suspend fun register(username: String, password: String): Result<Unit> {
        val existing = userDao.getUserByUsername(username)
        if (existing != null) return Result.failure(Exception("Username already exists"))
        userDao.insertUser(User(username = username, password = password))
        return Result.success(Unit)
    }

    // ==================== CATEGORY METHODS ====================

    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun getAllCategoriesList(): List<Category> = categoryDao.getAllCategoriesList()

    suspend fun addCategory(name: String): Long {
        Log.d(TAG, "Adding category: $name")
        return categoryDao.insertCategory(Category(name = name))
    }

    suspend fun deleteCategory(category: Category) {
        Log.d(TAG, "Deleting category: ${category.name}")
        categoryDao.deleteCategory(category)
    }

    // ==================== EXPENSE METHODS ====================

    fun getEntriesBetweenDates(start: Date, end: Date): Flow<List<ExpenseEntry>> {
        Log.d(TAG, "Getting expenses between $start and $end")
        return entryDao.getEntriesBetweenDates(start, end)
    }

    fun getCategorySpending(start: Date, end: Date): Flow<List<ExpenseEntryDao.CategorySpending>> {
        Log.d(TAG, "Getting category spending between $start and $end")
        return entryDao.getCategorySpendingBetweenDates(start, end)
    }

    suspend fun addExpenseEntry(
        date: Date, startTime: String, endTime: String,
        description: String, amount: Double, categoryId: Long, photoPath: String?
    ): Long {
        Log.d(TAG, "Adding expense: $description - R$amount")
        return entryDao.insertEntry(
            ExpenseEntry(
                date = date, startTime = startTime, endTime = endTime,
                description = description, amount = amount,
                categoryId = categoryId, photoPath = photoPath
            )
        )
    }

    suspend fun deleteExpense(expense: ExpenseEntry) {
        Log.d(TAG, "Deleting expense: ${expense.description}")
        entryDao.deleteEntry(expense)
    }

    suspend fun deleteExpenseById(id: Long) {
        Log.d(TAG, "Deleting expense by ID: $id")
        entryDao.deleteEntryById(id)
    }

    // ==================== SETTINGS METHODS ====================

    suspend fun saveGoals(min: Double, max: Double) {
        try {
            Log.d(TAG, "saveGoals called with min=$min, max=$max")
            val currentSettings = settingsDao.getSettingsSync()
            Log.d(TAG, "Current settings before save: $currentSettings")

            if (currentSettings != null) {
                val updatedSettings = currentSettings.copy(
                    minMonthlyGoal = min,
                    maxMonthlyGoal = max
                )
                settingsDao.updateSettings(updatedSettings)
                Log.d(TAG, "Updated existing settings (ID: ${currentSettings.id}): min=$min, max=$max")
            } else {
                val newSettings = Settings(
                    minMonthlyGoal = min,
                    maxMonthlyGoal = max,
                    monthlyIncome = 5000.0
                )
                val newId = settingsDao.saveSettings(newSettings)
                Log.d(TAG, "Created new settings with ID: $newId, min=$min, max=$max")
            }

            val verifySettings = settingsDao.getSettingsSync()
            Log.d(TAG, "Verification after save: Min=${verifySettings?.minMonthlyGoal}, Max=${verifySettings?.maxMonthlyGoal}")

        } catch (e: Exception) {
            Log.e(TAG, "Error saving goals: ${e.message}", e)
            throw e
        }
    }

    fun getGoals(): Flow<Settings?> {
        Log.d(TAG, "getGoals called")
        return settingsDao.getSettings()
    }

    suspend fun saveMonthlyIncome(income: Double) {
        try {
            Log.d(TAG, "saveMonthlyIncome called with income=$income")
            val currentSettings = settingsDao.getSettingsSync()

            if (currentSettings != null) {
                val updatedSettings = currentSettings.copy(monthlyIncome = income)
                settingsDao.updateSettings(updatedSettings)
                Log.d(TAG, "Updated existing settings: $updatedSettings")
            } else {
                val newSettings = Settings(monthlyIncome = income)
                settingsDao.saveSettings(newSettings)
                Log.d(TAG, "Created new settings: $newSettings")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving income: ${e.message}", e)
            throw e
        }
    }

    fun getMonthlyIncome(): Flow<Double?> {
        return settingsDao.getSettings().map { settings ->
            settings?.monthlyIncome
        }
    }

    fun getSettings(): Flow<Settings?> = settingsDao.getSettings()

    suspend fun clearSettings() {
        settingsDao.clearSettings()
        Log.d(TAG, "All settings cleared")
    }
}