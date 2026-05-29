package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val iconName: String, // Name of standard Material Icon (e.g. "Fastfood", "Wallet")
    val isCustom: Boolean = false
)
