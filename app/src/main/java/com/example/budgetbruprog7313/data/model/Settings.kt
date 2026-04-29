package com.example.budgetbruprog7313.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,  // Let Room auto-generate ID
    val minMonthlyGoal: Double? = null,
    val maxMonthlyGoal: Double? = null,
    val monthlyIncome: Double = 5000.0
)