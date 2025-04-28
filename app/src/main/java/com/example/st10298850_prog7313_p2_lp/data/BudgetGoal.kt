package com.example.st10298850_prog7313_p2_lp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget_goals",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"]
        )
    ]
)
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true) val budgetId: Long = 0,
    val userId: Long,
    val name: String,
    val goalAmount: Double
)