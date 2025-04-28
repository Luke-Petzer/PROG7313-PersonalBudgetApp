package com.example.st10298850_prog7313_p2_lp.data

import androidx.room.*

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getCategoriesForUser(userId: Long): List<Category>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'Expense'")
    suspend fun getTotalSpentForUser(userId: Long): Double?
}