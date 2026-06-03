package com.example.budgetbruprog7313.data.model

// Remove java.util.Date import - we'll use Long timestamps

sealed class Transaction {
    abstract val id: String  // Changed from Long to String (Firebase uses String IDs)
    abstract val amount: Double
    abstract val description: String
    abstract val date: Long   // Changed from Date to Long (timestamp)

    data class Expense(
        override val id: String,        // String ID from Firebase
        override val amount: Double,
        override val description: String,
        override val date: Long,         // Long timestamp
        val categoryId: String,          // String ID (Firebase)
        val categoryName: String,
        val startTime: String,
        val endTime: String,
        val photoPath: String?
    ) : Transaction()

    data class Income(
        override val id: String,         // String ID from Firebase
        override val amount: Double,
        override val description: String,
        override val date: Long,          // Long timestamp
        val source: String
    ) : Transaction()
}