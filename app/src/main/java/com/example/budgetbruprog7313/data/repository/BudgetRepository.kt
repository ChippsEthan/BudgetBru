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

    suspend fun login(username: String, password: String): User? =
        userDao.login(username, password)

    suspend fun register(username: String, password: String): Result<Unit> {
        val existing = userDao.getUserByUsername(username)
        if (existing != null) return Result.failure(Exception("Username already exists"))
        userDao.insertUser(User(username = username, password = password))
        return Result.success(Unit)
    }

    // ==================== CATEGORY METHODS ====================

    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun getAllCategoriesList(): List<Category> = categoryDao.getAllCategoriesList()

    suspend fun addCategory(name: String) =
        categoryDao.insertCategory(Category(name = name))

    suspend fun deleteCategory(category: Category) =
        categoryDao.deleteCategory(category)

    // ==================== EXPENSE METHODS ====================

    fun getEntriesBetweenDates(start: Date, end: Date): Flow<List<ExpenseEntry>> =
        entryDao.getEntriesBetweenDates(start, end)

    fun getCategorySpending(start: Date, end: Date): Flow<List<ExpenseEntryDao.CategorySpending>> =
        entryDao.getCategorySpendingBetweenDates(start, end)

    suspend fun addExpenseEntry(
        date: Date, startTime: String, endTime: String,
        description: String, amount: Double, categoryId: Long, photoPath: String?
    ) {
        entryDao.insertEntry(
            ExpenseEntry(
                date = date, startTime = startTime, endTime = endTime,
                description = description, amount = amount,
                categoryId = categoryId, photoPath = photoPath
            )
        )
    }

    // ==================== SETTINGS METHODS ====================

    // Goals (Min/Max)
    suspend fun saveGoals(min: Double, max: Double) {
        try {
            Log.d(TAG, "saveGoals called with min=$min, max=$max")
            val currentSettings = settingsDao.getSettingsSync()
            Log.d(TAG, "Current settings: $currentSettings")

            if (currentSettings != null) {
                val updatedSettings = currentSettings.copy(
                    minMonthlyGoal = min,
                    maxMonthlyGoal = max
                )
                settingsDao.updateSettings(updatedSettings)
                Log.d(TAG, "Updated existing settings: $updatedSettings")
            } else {
                val newSettings = Settings(
                    minMonthlyGoal = min,
                    maxMonthlyGoal = max,
                    monthlyIncome = 5000.0
                )
                val id = settingsDao.saveSettings(newSettings)
                Log.d(TAG, "Created new settings with ID: $id, settings: $newSettings")
            }

            // Verify save
            val verify = settingsDao.getSettingsSync()
            Log.d(TAG, "Verification after save: $verify")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving goals: ${e.message}", e)
            throw e
        }
    }

    fun getGoals(): Flow<Settings?> {
        Log.d(TAG, "getGoals called")
        return settingsDao.getSettings()
    }

    // Monthly Income
    suspend fun saveMonthlyIncome(income: Double) {
        try {
            Log.d(TAG, "saveMonthlyIncome called with income=$income")
            val currentSettings = settingsDao.getSettingsSync()
            Log.d(TAG, "Current settings: $currentSettings")

            if (currentSettings != null) {
                val updatedSettings = currentSettings.copy(monthlyIncome = income)
                settingsDao.updateSettings(updatedSettings)
                Log.d(TAG, "Updated existing settings: $updatedSettings")
            } else {
                val newSettings = Settings(monthlyIncome = income)
                val id = settingsDao.saveSettings(newSettings)
                Log.d(TAG, "Created new settings with ID: $id, settings: $newSettings")
            }

            // Verify save
            val verify = settingsDao.getSettingsSync()
            Log.d(TAG, "Verification after save: $verify")
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