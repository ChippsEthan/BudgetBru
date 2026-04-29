package com.example.budgetbruprog7313.data.dao

import androidx.room.*
import com.example.budgetbruprog7313.data.model.IncomeEntry
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface IncomeDao {
    @Insert
    suspend fun insertIncome(income: IncomeEntry): Long

    @Delete
    suspend fun deleteIncome(income: IncomeEntry)

    @Query("DELETE FROM income_entries WHERE id = :id")
    suspend fun deleteIncomeById(id: Long)

    @Query("SELECT * FROM income_entries ORDER BY date DESC")
    fun getAllIncomes(): Flow<List<IncomeEntry>>

    @Query("SELECT * FROM income_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getIncomesBetweenDates(startDate: Date, endDate: Date): Flow<List<IncomeEntry>>
}