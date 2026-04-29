package com.example.budgetbruprog7313.data.model

import java.util.Date

sealed class Transaction {
    abstract val id: Long
    abstract val amount: Double
    abstract val description: String
    abstract val date: Date

    data class Expense(
        override val id: Long,
        override val amount: Double,
        override val description: String,
        override val date: Date,
        val categoryId: Long,
        val categoryName: String,
        val startTime: String,
        val endTime: String,
        val photoPath: String?
    ) : Transaction()

    data class Income(
        override val id: Long,
        override val amount: Double,
        override val description: String,
        override val date: Date,
        val source: String
    ) : Transaction()
}