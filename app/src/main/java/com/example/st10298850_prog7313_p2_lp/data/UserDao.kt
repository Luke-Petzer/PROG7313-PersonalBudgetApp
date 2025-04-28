package com.example.st10298850_prog7313_p2_lp.data

import androidx.room.*

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun getUserByEmailAndPassword(email: String, password: String): User?

    @Query("UPDATE users SET loginStreak = :streak, lastLoginDate = :date WHERE userId = :userId")
    suspend fun updateLoginStreak(userId: Long, streak: Int, date: Long)
}