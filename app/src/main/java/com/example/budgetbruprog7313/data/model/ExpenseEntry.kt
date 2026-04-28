package com.example.budgetbruprog7313.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "expense_entries",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Date,
    val startTime: String,
    val endTime: String,
    val description: String,
    val amount: Double,
    val categoryId: Long,
    val photoPath: String? = null
)