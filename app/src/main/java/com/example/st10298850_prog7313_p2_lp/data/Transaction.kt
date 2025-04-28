package com.example.st10298850_prog7313_p2_lp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"]
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["accountId"],
            childColumns = ["accountId"]
        )
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val transactionId: Long = 0,
    val userId: Long,
    val type: String,  // "Expense" or "Income"
    val amount: Double,
    val payee: String,
    val accountId: Long,
    val date: Long,
    val description: String,
    val receiptPath: String?,
    val repeat: Boolean
)