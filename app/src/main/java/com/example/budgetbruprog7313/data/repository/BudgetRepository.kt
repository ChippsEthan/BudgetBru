package com.example.budgetbruprog7313.data.repository

import com.example.budgetbruprog7313.data.dao.ExpenseEntryDao
import com.example.budgetbruprog7313.data.database.AppDatabase
import com.example.budgetbruprog7313.data.model.Category
import com.example.budgetbruprog7313.data.model.ExpenseEntry
import com.example.budgetbruprog7313.data.model.User
import kotlinx.coroutines.flow.Flow
import java.util.Date

class BudgetRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val categoryDao = db.categoryDao()
    private val entryDao = db.expenseEntryDao()

    // Login
    suspend fun login(username: String, password: String): User? =
        userDao.login(username, password)

    suspend fun register(username: String, password: String): Result<Unit> {
        val existing = userDao.getUserByUsername(username)
        if (existing != null) return Result.failure(Exception("Username already exists"))
        userDao.insertUser(User(username = username, password = password))
        return Result.success(Unit)
    }

    // Categories
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun addCategory(name: String) =
        categoryDao.insertCategory(Category(name = name))

    suspend fun deleteCategory(category: Category) =
        categoryDao.deleteCategory(category)

    // Expenses
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
}