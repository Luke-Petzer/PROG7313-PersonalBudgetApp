package com.example.st10298850_prog7313_p2_lp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"]
        )
    ]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val categoryId: Long = 0,
    val userId: Long,
    var name: String,
    var budgetedAmount: Double
)