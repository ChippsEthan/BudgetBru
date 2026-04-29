package com.example.budgetbruprog7313.data.dao

import androidx.room.*
import com.example.budgetbruprog7313.data.model.ExpenseEntry
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ExpenseEntryDao {
    @Insert
    suspend fun insertEntry(entry: ExpenseEntry): Long  // Returns the row ID

    @Delete
    suspend fun deleteEntry(entry: ExpenseEntry)

    @Query("DELETE FROM expense_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("""
        SELECT * FROM expense_entries 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY date ASC
    """)
    fun getEntriesBetweenDates(startDate: Date, endDate: Date): Flow<List<ExpenseEntry>>

    @Query("""
        SELECT c.name, SUM(e.amount) as total 
        FROM expense_entries e 
        INNER JOIN categories c ON e.categoryId = c.id
        WHERE e.date BETWEEN :startDate AND :endDate
        GROUP BY e.categoryId
    """)
    fun getCategorySpendingBetweenDates(startDate: Date, endDate: Date): Flow<List<CategorySpending>>

    data class CategorySpending(
        val name: String,
        val total: Double
    )
}