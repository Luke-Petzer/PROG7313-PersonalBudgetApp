package com.example.st10298850_prog7313_p2_lp.repositories

import com.example.st10298850_prog7313_p2_lp.data.Account
import com.example.st10298850_prog7313_p2_lp.data.AccountDao

class AccountRepository(private val accountDao: AccountDao) {
    suspend fun getAccountsForUser(userId: Long): List<Account> {
        return accountDao.getAccountsForUser(userId)
    }
}