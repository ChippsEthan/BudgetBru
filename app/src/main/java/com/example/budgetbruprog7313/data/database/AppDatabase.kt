package com.example.budgetbruprog7313.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.budgetbruprog7313.data.dao.CategoryDao
import com.example.budgetbruprog7313.data.dao.ExpenseEntryDao
import com.example.budgetbruprog7313.data.dao.UserDao
import com.example.budgetbruprog7313.data.model.Category
import com.example.budgetbruprog7313.data.model.ExpenseEntry
import com.example.budgetbruprog7313.data.model.User

@Database(
    entities = [User::class, Category::class, ExpenseEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseEntryDao(): ExpenseEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgetbru_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}