package com.example.budgetbruprog7313.data.repository

import android.util.Log
import com.example.budgetbruprog7313.data.model.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BudgetRepository {

    private val auth = Firebase.auth
    private val db   = Firebase.firestore
    private val TAG  = "BudgetRepository"

    // ── Current user helpers ──────────────────────────────────────────────────
    private val uid get() = auth.currentUser?.uid
        ?: throw IllegalStateException("No logged-in user")

    private fun userDoc()       = db.collection("users").document(uid)
    private fun expensesCol()   = userDoc().collection("expenses")
    private fun incomesCol()    = userDoc().collection("incomes")
    private fun categoriesCol() = userDoc().collection("categories")
    private fun settingsDoc()   = userDoc().collection("settings").document("main")

    // ==================== AUTH ====================

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun logout() = auth.signOut()

    suspend fun login(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Login success: $email")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Login failed: ${e.message}")
            false
        }
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val newUid = result.user?.uid
                ?: return Result.failure(Exception("Registration failed: no UID returned"))
            seedDefaultData(newUid)
            Log.d(TAG, "Registration success: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun seedDefaultData(uid: String) {
        try {
            val userRef = db.collection("users").document(uid)

            // Default settings
            userRef.collection("settings").document("main").set(
                mapOf(
                    "monthlyIncome"   to 5000.0,
                    "minMonthlyGoal"  to null,
                    "maxMonthlyGoal"  to null
                )
            ).await()

            // Default categories via batch write
            val batch = db.batch()
            listOf("Food", "Transport", "Groceries", "Fun", "Study", "Bills", "Health", "Other")
                .forEach { name ->
                    val ref = userRef.collection("categories").document()
                    batch.set(ref, mapOf("name" to name))
                }
            batch.commit().await()

            Log.d(TAG, "Seed data written for uid: $uid")
        } catch (e: Exception) {
            Log.e(TAG, "Seed failed: ${e.message}")
        }
    }

    // ==================== CATEGORIES ====================

    val allCategories: Flow<List<Category>> = callbackFlow {
        val listener = categoriesCol()
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Categories listener error: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    Category(
                        id   = doc.id,
                        name = doc.getString("name") ?: ""
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAllCategoriesList(): List<Category> {
        return try {
            categoriesCol().orderBy("name").get().await().documents.map { doc ->
                Category(id = doc.id, name = doc.getString("name") ?: "")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAllCategoriesList failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun addCategory(name: String): String {
        return try {
            val ref = categoriesCol().add(mapOf("name" to name)).await()
            Log.d(TAG, "Category added: $name")
            ref.id
        } catch (e: Exception) {
            Log.e(TAG, "addCategory failed: ${e.message}")
            ""
        }
    }

    suspend fun deleteCategory(category: Category) {
        try {
            categoriesCol().document(category.id).delete().await()
            Log.d(TAG, "Category deleted: ${category.name}")
        } catch (e: Exception) {
            Log.e(TAG, "deleteCategory failed: ${e.message}")
        }
    }

    // ==================== EXPENSES ====================

    fun getEntriesBetweenDates(startMillis: Long, endMillis: Long): Flow<List<ExpenseEntry>> =
        callbackFlow {
            val listener = expensesCol()
                .whereGreaterThanOrEqualTo("date", startMillis)
                .whereLessThanOrEqualTo("date", endMillis)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Expenses listener error: ${error.message}")
                        close(error)
                        return@addSnapshotListener
                    }
                    val list = snapshot?.documents?.map { doc ->
                        ExpenseEntry(
                            id          = doc.id,
                            date        = doc.getLong("date") ?: 0L,
                            startTime   = doc.getString("startTime") ?: "",
                            endTime     = doc.getString("endTime") ?: "",
                            description = doc.getString("description") ?: "",
                            amount      = doc.getDouble("amount") ?: 0.0,
                            categoryId  = doc.getString("categoryId") ?: "",
                            photoPath   = doc.getString("photoPath")
                        )
                    } ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }

    suspend fun addExpenseEntry(
        dateMillis: Long,
        startTime: String,
        endTime: String,
        description: String,
        amount: Double,
        categoryId: String,
        photoPath: String?
    ): String {
        return try {
            val data = mapOf(
                "date"        to dateMillis,
                "startTime"   to startTime,
                "endTime"     to endTime,
                "description" to description,
                "amount"      to amount,
                "categoryId"  to categoryId,
                "photoPath"   to photoPath
            )
            val ref = expensesCol().add(data).await()
            Log.d(TAG, "Expense added: $description - R$amount")
            ref.id
        } catch (e: Exception) {
            Log.e(TAG, "addExpenseEntry failed: ${e.message}")
            ""
        }
    }

    suspend fun deleteExpense(expense: ExpenseEntry) {
        try {
            expensesCol().document(expense.id).delete().await()
            Log.d(TAG, "Expense deleted: ${expense.description}")
        } catch (e: Exception) {
            Log.e(TAG, "deleteExpense failed: ${e.message}")
        }
    }

    suspend fun deleteExpenseById(id: String) {
        try {
            expensesCol().document(id).delete().await()
            Log.d(TAG, "Expense deleted by id: $id")
        } catch (e: Exception) {
            Log.e(TAG, "deleteExpenseById failed: ${e.message}")
        }
    }

    suspend fun getCategorySpendingBetweenDates(
        startMillis: Long,
        endMillis: Long
    ): Map<String, Double> {
        return try {
            val expenses = expensesCol()
                .whereGreaterThanOrEqualTo("date", startMillis)
                .whereLessThanOrEqualTo("date", endMillis)
                .get().await()

            val categories = getAllCategoriesList().associateBy { it.id }
            val totals = mutableMapOf<String, Double>()

            expenses.documents.forEach { doc ->
                val catId  = doc.getString("categoryId") ?: return@forEach
                val amount = doc.getDouble("amount") ?: 0.0
                val name   = categories[catId]?.name ?: "Unknown"
                totals[name] = (totals[name] ?: 0.0) + amount
            }
            totals
        } catch (e: Exception) {
            Log.e(TAG, "getCategorySpending failed: ${e.message}")
            emptyMap()
        }
    }

    // ==================== INCOME ====================

    fun getAllIncomes(): Flow<List<IncomeEntry>> = callbackFlow {
        val listener = incomesCol()
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Incomes listener error: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    IncomeEntry(
                        id          = doc.id,
                        amount      = doc.getDouble("amount") ?: 0.0,
                        description = doc.getString("description") ?: "",
                        date        = doc.getLong("date") ?: 0L,
                        source      = doc.getString("source") ?: ""
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addIncomeEntry(income: IncomeEntry): String {
        return try {
            val data = mapOf(
                "amount"      to income.amount,
                "description" to income.description,
                "date"        to income.date,
                "source"      to income.source
            )
            val ref = incomesCol().add(data).await()
            Log.d(TAG, "Income added: ${income.description} - R${income.amount}")
            ref.id
        } catch (e: Exception) {
            Log.e(TAG, "addIncomeEntry failed: ${e.message}")
            ""
        }
    }

    suspend fun deleteIncomeEntry(income: IncomeEntry) {
        try {
            incomesCol().document(income.id).delete().await()
            Log.d(TAG, "Income deleted: ${income.description}")
        } catch (e: Exception) {
            Log.e(TAG, "deleteIncomeEntry failed: ${e.message}")
        }
    }

    suspend fun deleteIncomeById(id: String) {
        try {
            incomesCol().document(id).delete().await()
            Log.d(TAG, "Income deleted by id: $id")
        } catch (e: Exception) {
            Log.e(TAG, "deleteIncomeById failed: ${e.message}")
        }
    }

    // ==================== SETTINGS ====================

    fun getSettings(): Flow<Settings?> = callbackFlow {
        val listener = settingsDoc()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Settings listener error: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                val settings = snapshot?.let {
                    Settings(
                        minMonthlyGoal = it.getDouble("minMonthlyGoal"),
                        maxMonthlyGoal = it.getDouble("maxMonthlyGoal"),
                        monthlyIncome  = it.getDouble("monthlyIncome") ?: 5000.0
                    )
                }
                trySend(settings)
            }
        awaitClose { listener.remove() }
    }

    fun getGoals(): Flow<Settings?> = getSettings()

    fun getMonthlyIncome(): Flow<Double?> = getSettings().map { it?.monthlyIncome }

    suspend fun saveGoals(min: Double, max: Double) {
        try {
            settingsDoc().update(
                mapOf(
                    "minMonthlyGoal" to min,
                    "maxMonthlyGoal" to max
                )
            ).await()
            Log.d(TAG, "Goals saved: min=$min, max=$max")
        } catch (e: Exception) {
            Log.e(TAG, "saveGoals failed: ${e.message}")
            throw e
        }
    }

    suspend fun saveMonthlyIncome(income: Double) {
        try {
            settingsDoc().set(
                mapOf("monthlyIncome" to income),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            Log.d(TAG, "Monthly income saved: $income")
        } catch (e: Exception) {
            Log.e(TAG, "saveMonthlyIncome failed: ${e.message}")
            throw e
        }
    }

    suspend fun clearSettings() {
        try {
            settingsDoc().delete().await()
            Log.d(TAG, "Settings cleared")
        } catch (e: Exception) {
            Log.e(TAG, "clearSettings failed: ${e.message}")
        }
    }
}