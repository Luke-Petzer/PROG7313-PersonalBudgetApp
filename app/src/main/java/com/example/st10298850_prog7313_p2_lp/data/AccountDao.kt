package com.example.st10298850_prog7313_p2_lp.data

import androidx.room.*

@Dao
interface AccountDao {
    @Insert
    suspend fun insertAccount(account: Account): Long

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    suspend fun getAccountsForUser(userId: Long): List<Account>

    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accountId AND type = 'Expense'")
    suspend fun getTotalSpentForAccount(accountId: Long): Double?
}