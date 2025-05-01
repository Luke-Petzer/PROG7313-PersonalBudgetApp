package com.example.st10298850_prog7313_p2_lp.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.st10298850_prog7313_p2_lp.data.BudgetGoalRepository

class HomeViewModelFactory(
    private val application: Application,
    private val budgetGoalRepository: BudgetGoalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application, budgetGoalRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}