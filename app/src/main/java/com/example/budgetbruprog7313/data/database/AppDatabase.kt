package com.example.budgetbruprog7313.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgetbruprog7313.data.dao.*
import com.example.budgetbruprog7313.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Category::class, ExpenseEntry::class, Settings::class, IncomeEntry::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseEntryDao(): ExpenseEntryDao
    abstract fun settingsDao(): SettingsDao
    abstract fun incomeDao(): IncomeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgetbru_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val userDao = database.userDao()
                    val categoryDao = database.categoryDao()
                    val settingsDao = database.settingsDao()

                    val existingUser = userDao.getUserByUsername("test")
                    if (existingUser == null) {
                        val defaultUser = User(username = "test", password = "1234")
                        userDao.insertUser(defaultUser)
                    }

                    val existingCategories = categoryDao.getAllCategoriesList()
                    if (existingCategories.isEmpty()) {
                        val defaultCategories = listOf(
                            Category(name = "Food"),
                            Category(name = "Transport"),
                            Category(name = "Groceries"),
                            Category(name = "Fun"),
                            Category(name = "Study"),
                            Category(name = "Bills"),
                            Category(name = "Health"),
                            Category(name = "Other")
                        )
                        defaultCategories.forEach { category ->
                            categoryDao.insertCategory(category)
                        }
                    }

                    val existingSettings = settingsDao.getSettingsSync()
                    if (existingSettings == null) {
                        val defaultSettings = Settings(
                            monthlyIncome = 5000.0,
                            minMonthlyGoal = null,
                            maxMonthlyGoal = null
                        )
                        settingsDao.saveSettings(defaultSettings)
                    }
                }
            }
        }
    }
}