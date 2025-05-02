package com.example.st10298850_prog7313_p2_lp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.st10298850_prog7313_p2_lp.data.AppDatabase
import com.example.st10298850_prog7313_p2_lp.data.BudgetGoal
import com.example.st10298850_prog7313_p2_lp.data.BudgetGoalRepository
import com.example.st10298850_prog7313_p2_lp.data.CategoryTotal
import com.example.st10298850_prog7313_p2_lp.repositories.TransactionRepository
import com.example.st10298850_prog7313_p2_lp.utils.UserSessionManager
import kotlinx.coroutines.launch

class HomeViewModel(
    application: Application,
    private val budgetGoalRepository: BudgetGoalRepository
) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    private val userId: Long

    private val _categoryTotals = MutableLiveData<List<CategoryTotal>>()
    val categoryTotals: LiveData<List<CategoryTotal>> = _categoryTotals

    private val _budgetGoals = MutableLiveData<List<BudgetGoal>>()
    val budgetGoals: LiveData<List<BudgetGoal>> = _budgetGoals

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepository(database.transactionDao())
        userId = UserSessionManager.getUserId(application)
        loadCategoryTotals()
        loadBudgetGoals()
    }

    fun loadCategoryTotals() {
        viewModelScope.launch {
            _categoryTotals.value = repository.getCategoryTotalsForUser(userId)
        }
    }

    fun loadCategoryTotalsForDateRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            _categoryTotals.value = repository.getCategoryTotalsForUserInDateRange(userId, startDate, endDate)
        }
    }

    fun saveBudgetGoals(vararg goals: BudgetGoal) {
        viewModelScope.launch {
            goals.forEach { goal ->
                budgetGoalRepository.insertOrUpdateBudgetGoal(goal)
            }
            loadBudgetGoals()
        }
    }

    fun loadBudgetGoals() {
        viewModelScope.launch {
            _budgetGoals.value = budgetGoalRepository.getBudgetGoalsForUser(userId)
        }
    }
}