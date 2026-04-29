package com.example.budgetbruprog7313.data.dao

import androidx.room.*
import com.example.budgetbruprog7313.data.model.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long  // Returns the row ID

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
}