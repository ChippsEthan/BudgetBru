package com.example.budgetbruprog7313.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.budgetbruprog7313.data.dao.CategoryDao
import com.example.budgetbruprog7313.data.dao.ExpenseEntryDao
import com.example.budgetbruprog7313.data.dao.SettingsDao
import com.example.budgetbruprog7313.data.dao.UserDao
import com.example.budgetbruprog7313.data.model.Category
import com.example.budgetbruprog7313.data.model.ExpenseEntry
import com.example.budgetbruprog7313.data.model.Settings
import com.example.budgetbruprog7313.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Category::class, ExpenseEntry::class, Settings::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseEntryDao(): ExpenseEntryDao
    abstract fun settingsDao(): SettingsDao

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
                    .addCallback(DatabaseCallback())   // ← This enables default data
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

    // This runs only once when the database is created for the first time
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val userDao = database.userDao()
                    val categoryDao = database.categoryDao()

                    // Create default user: test / 1234
                    val defaultUser = User(
                        username = "test",
                        password = "1234"
                    )
                    userDao.insertUser(defaultUser)

                    // Create default categories (student-friendly)
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

                    println("✅ BudgetBru: Default user 'test/1234' and categories created!")
                }
            }
        }
    }
}