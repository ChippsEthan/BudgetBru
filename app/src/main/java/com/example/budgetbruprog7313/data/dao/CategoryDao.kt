package com.example.budgetbruprog7313.data.dao

import androidx.room.*
import com.example.budgetbruprog7313.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Delete
    suspend fun deleteCategory(category: Category): Int
}