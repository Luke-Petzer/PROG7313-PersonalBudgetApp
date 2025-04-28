package com.example.st10298850_prog7313_p2_lp.data

import androidx.room.*

@Dao
interface BudgetGoalDao {
    @Insert
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): Long

    @Query("SELECT * FROM budget_goals WHERE userId = :userId")
    suspend fun getBudgetGoalsForUser(userId: Long): List<BudgetGoal>
}