package com.example.st10298850_prog7313_p2_lp.data

import androidx.room.*

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
    
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun getUser(username: String, password: String): User?

    @Query("UPDATE users SET loginStreak = :streak, lastLoginDate = :date WHERE userId = :userId")
    suspend fun updateLoginStreak(userId: Long, streak: Int, date: Long)

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: Long): User?
}