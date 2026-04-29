package com.example.budgetbruprog7313.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 6,
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

        // Define migrations for each version change
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns or tables for version 2
                database.execSQL("ALTER TABLE settings ADD COLUMN monthlyIncome REAL DEFAULT 5000.0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migrations for version 3 if needed
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migrations for version 4 if needed
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migrations for version 5 if needed
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migrations for version 6 if needed
                // Add any schema changes from version 5 to 6
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgetbru_db"
                )
                    // IMPORTANT: Remove fallbackToDestructiveMigration() for production!
                    // Add migrations instead
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6
                    )
                    // Only use this in development, remove for production
                    // .fallbackToDestructiveMigration()
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

                    // Only seed data if tables are empty
                    val existingUser = userDao.getUserByUsername("test")
                    if (existingUser == null) {
                        // Create default user: test / 1234
                        val defaultUser = User(username = "test", password = "1234")
                        userDao.insertUser(defaultUser)
                        println("✅ BudgetBru: Default user 'test/1234' created!")
                    }

                    // Create default categories only if none exist
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
                        println("✅ BudgetBru: Default categories created!")
                    }

                    // Create default settings only if none exist
                    val existingSettings = settingsDao.getSettingsSync()
                    if (existingSettings == null) {
                        val defaultSettings = Settings(
                            monthlyIncome = 5000.0,
                            minMonthlyGoal = null,
                            maxMonthlyGoal = null
                        )
                        settingsDao.saveSettings(defaultSettings)
                        println("✅ BudgetBru: Default settings created!")
                    }
                }
            }
        }
    }
}