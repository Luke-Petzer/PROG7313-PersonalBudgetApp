package com.example.st10298850_prog7313_p2_lp.data

import androidx.room.*

@Dao
interface MonthlyGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGoal(goal: MonthlyGoal)

    @Query("SELECT * FROM monthly_goals WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getGoalForMonth(userId: Long, month: Int, year: Int): MonthlyGoal?
}