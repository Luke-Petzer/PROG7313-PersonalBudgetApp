package com.example.st10298850_prog7313_p2_lp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.st10298850_prog7313_p2_lp.data.Account
import com.example.st10298850_prog7313_p2_lp.data.Category
import com.example.st10298850_prog7313_p2_lp.data.Transaction
import com.example.st10298850_prog7313_p2_lp.repositories.AccountRepository
import com.example.st10298850_prog7313_p2_lp.repositories.CategoryRepository
import com.example.st10298850_prog7313_p2_lp.repositories.TransactionRepository
import kotlinx.coroutines.launch
import android.util.Log

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    fun loadCategoriesForUser(userId: Long) {
        viewModelScope.launch {
            val fetchedCategories = categoryRepository.getCategoriesForUser(userId)
            Log.d("AddTransactionViewModel", "Fetched ${fetchedCategories.size} categories")
            _categories.postValue(fetchedCategories)
        }
    }

    fun insertDefaultCategories(userId: Long) {
        viewModelScope.launch {
            categoryRepository.insertDefaultCategories(userId)
            loadCategoriesForUser(userId)
        }
    }

    fun loadAccountsForUser(userId: Long) {
        viewModelScope.launch {
            _accounts.value = accountRepository.getAccountsForUser(userId)
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.insertTransaction(transaction)
        }
    }

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository,
        private val accountRepository: AccountRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddTransactionViewModel(transactionRepository, categoryRepository, accountRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}