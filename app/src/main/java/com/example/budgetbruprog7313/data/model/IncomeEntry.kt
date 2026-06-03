package com.example.budgetbruprog7313.data.model

data class IncomeEntry(
    val id: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val date: Long = 0L,
    val source: String = ""
)