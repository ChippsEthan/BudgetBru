package com.example.budgetbruprog7313.data.dao

import androidx.room.*
import com.example.budgetbruprog7313.data.model.Settings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: Settings)

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<Settings?>
}