package com.example.budgetbruprog7313.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,  // Change from 1 to 0 to let Room auto-generate
    val minMonthlyGoal: Double? = null,
    val maxMonthlyGoal: Double? = null,
    val monthlyIncome: Double = 5000.0
)