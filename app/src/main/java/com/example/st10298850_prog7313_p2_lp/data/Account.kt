package com.example.st10298850_prog7313_p2_lp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"]
        )
    ]
)
data class Account(
    @PrimaryKey(autoGenerate = true) val accountId: Long = 0,
    val userId: Long,
    val name: String,
    val type: String,
    val goalAmount: Double
)