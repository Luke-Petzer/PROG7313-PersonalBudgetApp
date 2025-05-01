package com.example.st10298850_prog7313_p2_lp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.example.st10298850_prog7313_p2_lp.data.AppDatabase
import com.example.st10298850_prog7313_p2_lp.data.Transaction
import com.example.st10298850_prog7313_p2_lp.repositories.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionHistoryViewModel(
    application: Application,
    private val repository: TransactionRepository
) : AndroidViewModel(application) {
    private val _filteredTransactions = MediatorLiveData<List<Transaction>>()
    val filteredTransactions: LiveData<List<Transaction>> = _filteredTransactions

    private val _categories = MediatorLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private var startDate: Long? = null
    private var endDate: Long? = null
    private var selectedCategory: String? = null

    init {
        loadTransactions()
        loadCategories()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            val allTransactions = repository.getAllTransactions()
            _filteredTransactions.value = allTransactions
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val allTransactions = repository.getAllTransactions()
            _categories.value = allTransactions.mapNotNull { it.category }.distinct()
        }
    }

    fun setDateFilter(start: Long, end: Long) {
        startDate = start
        endDate = end
        applyFilters()
    }

    fun clearDateFilter() {
        startDate = null
        endDate = null
        applyFilters()
    }

    fun setThisMonthFilter() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        startDate = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        endDate = calendar.timeInMillis

        applyFilters()
    }

    fun setCategoryFilter(category: String?) {
        selectedCategory = category
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            val allTransactions = repository.getAllTransactions()
            val filtered = allTransactions.filter { transaction ->
                val matchesDate = (startDate == null || transaction.date >= startDate!!) &&
                                  (endDate == null || transaction.date <= endDate!!)
                val matchesCategory = selectedCategory == null || selectedCategory == "All Categories" || transaction.category == selectedCategory
                matchesDate && matchesCategory
            }
            _filteredTransactions.value = filtered
        }
    }
}