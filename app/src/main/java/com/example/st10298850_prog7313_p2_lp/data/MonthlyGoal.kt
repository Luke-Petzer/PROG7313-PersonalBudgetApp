package com.example.st10298850_prog7313_p2_lp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_goals")
data class MonthlyGoal(
    @PrimaryKey val userId: Long,
    val minGoal: Double,
    val maxGoal: Double,
    val month: Int,
    val year: Int
)