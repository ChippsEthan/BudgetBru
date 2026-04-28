package com.example.budgetbruprog7313.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey(autoGenerate = false) val id: Int = 1, // only one row
    val minMonthlyGoal: Double,
    val maxMonthlyGoal: Double
)