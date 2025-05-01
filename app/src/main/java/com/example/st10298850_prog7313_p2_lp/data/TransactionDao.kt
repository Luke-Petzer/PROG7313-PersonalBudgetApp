package com.example.st10298850_prog7313_p2_lp.data

import androidx.room.*
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY startDate DESC")
    suspend fun getTransactionsForUser(userId: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND startDate BETWEEN :startDate AND :endDate")
    suspend fun getTransactionsForUserInDateRange(userId: Long, startDate: Long, endDate: Long): List<Transaction>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<Transaction>

    @Query("""
        SELECT category AS categoryName, SUM(amount) AS totalAmount, COUNT(*) AS transactionCount
        FROM transactions
        WHERE userId = :userId
        GROUP BY category
    """)
    suspend fun getCategoryTotalsForUser(userId: Long): List<CategoryTotal>

    @Query("""
        SELECT category AS categoryName, SUM(amount) AS totalAmount, COUNT(*) AS transactionCount
        FROM transactions
        WHERE userId = :userId AND startDate BETWEEN :startDate AND :endDate
        GROUP BY category
    """)
    suspend fun getCategoryTotalsForUserInDateRange(userId: Long, startDate: Long, endDate: Long): List<CategoryTotal>
}