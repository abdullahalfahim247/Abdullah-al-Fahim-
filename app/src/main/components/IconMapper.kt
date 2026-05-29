package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {
    fun getIconByName(name: String): ImageVector {
        return when (name) {
            "Fastfood" -> Icons.Filled.Fastfood
            "ShoppingBag" -> Icons.Filled.ShoppingBag
            "DirectionsCar" -> Icons.Filled.DirectionsCar
            "PhoneAndroid" -> Icons.Filled.PhoneAndroid
            "Wifi" -> Icons.Filled.Wifi
            "ReceiptLong" -> Icons.Filled.ReceiptLong
            "School" -> Icons.Filled.School
            "SportsEsports" -> Icons.Filled.SportsEsports
            "LocalPharmacy" -> Icons.Filled.LocalPharmacy
            "Category" -> Icons.Filled.Category
            "Payments" -> Icons.Filled.Payments
            "Storefront" -> Icons.Filled.Storefront
            "Computer" -> Icons.Filled.Computer
            "CardGiftcard" -> Icons.Filled.CardGiftcard
            "AccountBalance" -> Icons.Filled.AccountBalance
            "Work" -> Icons.Filled.Work
            "Receipt" -> Icons.Filled.Receipt
            "MedicalServices" -> Icons.Filled.MedicalServices
            "Settings" -> Icons.Filled.Settings
            "Person" -> Icons.Filled.Person
            "Info" -> Icons.Filled.Info
            "Notifications" -> Icons.Filled.Notifications
            "Share" -> Icons.Filled.Share
            "Download" -> Icons.Filled.Download
            "Security" -> Icons.Filled.Security
            "Language" -> Icons.Filled.Language
            "AttachMoney" -> Icons.Filled.AttachMoney
            "TrendingUp" -> Icons.Filled.TrendingUp
            "TrendingDown" -> Icons.Filled.TrendingDown
            "Menu" -> Icons.Filled.Menu
            "ArrowBack" -> Icons.Filled.ArrowBack
            "Search" -> Icons.Filled.Search
            "Edit" -> Icons.Filled.Edit
            "Delete" -> Icons.Filled.Delete
            else -> Icons.Filled.Category
        }
    }
    
    val allAvailableIcons = listOf(
        "Fastfood" to Icons.Filled.Fastfood,
        "ShoppingBag" to Icons.Filled.ShoppingBag,
        "DirectionsCar" to Icons.Filled.DirectionsCar,
        "PhoneAndroid" to Icons.Filled.PhoneAndroid,
        "Wifi" to Icons.Filled.Wifi,
        "ReceiptLong" to Icons.Filled.ReceiptLong,
        "School" to Icons.Filled.School,
        "SportsEsports" to Icons.Filled.SportsEsports,
        "LocalPharmacy" to Icons.Filled.LocalPharmacy,
        "Payments" to Icons.Filled.Payments,
        "Storefront" to Icons.Filled.Storefront,
        "Computer" to Icons.Filled.Computer,
        "CardGiftcard" to Icons.Filled.CardGiftcard,
        "AccountBalance" to Icons.Filled.AccountBalance,
        "Category" to Icons.Filled.Category
    )
}
