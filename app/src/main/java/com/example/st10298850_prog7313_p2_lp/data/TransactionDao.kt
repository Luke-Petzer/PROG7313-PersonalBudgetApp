package com.example.st10298850_prog7313_p2_lp.data

import androidx.room.*

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    suspend fun getTransactionsForUser(userId: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTransactionsForUserInDateRange(userId: Long, startDate: Long, endDate: Long): List<Transaction>
}