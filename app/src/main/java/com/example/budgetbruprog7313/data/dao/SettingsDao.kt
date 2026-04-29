package com.example.budgetbruprog7313.data.dao

import androidx.room.*
import com.example.budgetbruprog7313.data.model.Settings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings LIMIT 1")
    fun getSettings(): Flow<Settings?>

    @Query("SELECT * FROM settings LIMIT 1")
    suspend fun getSettingsSync(): Settings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: Settings): Long

    @Update
    suspend fun updateSettings(settings: Settings)

    @Query("DELETE FROM settings")
    suspend fun clearSettings()
}