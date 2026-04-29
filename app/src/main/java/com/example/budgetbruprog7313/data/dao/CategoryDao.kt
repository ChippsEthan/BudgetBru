package com.example.budgetbruprog7313.data.dao

import androidx.room.*
import com.example.budgetbruprog7313.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    // Get all categories as Flow (for real-time updates)
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    // Get all categories as List (for one-time fetch)
    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategoriesList(): List<Category>

    // Insert a new category
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    // Delete a category
    @Delete
    suspend fun deleteCategory(category: Category)

    // Get a single category by ID
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?
}