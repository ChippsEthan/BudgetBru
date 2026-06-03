package com.example.budgetbruprog7313.data.model

data class ExpenseEntry(
    val id: String = "",
    val date: Long = 0L,
    val startTime: String = "",
    val endTime: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val categoryId: String = "",
    val photoPath: String? = null
)