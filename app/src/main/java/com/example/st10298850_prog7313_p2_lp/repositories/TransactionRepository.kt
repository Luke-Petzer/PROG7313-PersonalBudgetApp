package com.example.st10298850_prog7313_p2_lp.repositories

import com.example.st10298850_prog7313_p2_lp.data.CategoryTotal
import com.example.st10298850_prog7313_p2_lp.data.Transaction
import com.example.st10298850_prog7313_p2_lp.data.TransactionDao


class TransactionRepository(private val transactionDao: TransactionDao) {

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun getAllTransactions(): List<Transaction> {
        return transactionDao.getAllTransactions()
    }

    // Add other methods as needed, such as getTransactions(), updateTransaction(), etc.

    suspend fun getTransactionsForUser(userId: Long): List<Transaction> {
        return transactionDao.getTransactionsForUser(userId)
    }

    suspend fun getCategoryTotalsForUser(userId: Long): List<CategoryTotal> {
        return transactionDao.getCategoryTotalsForUser(userId)
    }

    suspend fun getCategoryTotalsForUserInDateRange(userId: Long, startDate: Long, endDate: Long): List<CategoryTotal> {
        return transactionDao.getCategoryTotalsForUserInDateRange(userId, startDate, endDate)
    }
}