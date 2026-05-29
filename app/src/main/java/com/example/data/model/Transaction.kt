package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val title: String,
    val category: String,
    val date: String, // "YYYY-MM-DD"
    val time: String, // "HH:MM"
    val paymentMethod: String, // "Cash", "Card", "Bank", etc.
    val description: String = ""
)
