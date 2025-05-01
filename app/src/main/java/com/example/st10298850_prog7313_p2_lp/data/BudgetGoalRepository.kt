package com.example.st10298850_prog7313_p2_lp.viewmodels

import com.example.st10298850_prog7313_p2_lp.data.BudgetGoal
import com.example.st10298850_prog7313_p2_lp.data.BudgetGoalDao


class BudgetGoalRepository(private val budgetGoalDao: BudgetGoalDao) {
    suspend fun insertOrUpdateBudgetGoal(goal: BudgetGoal) {
        budgetGoalDao.insertOrUpdate(goal)
    }

    suspend fun getBudgetGoalsForUser(userId: Long): List<BudgetGoal> {
        return budgetGoalDao.getBudgetGoalsForUser(userId)
    }
}