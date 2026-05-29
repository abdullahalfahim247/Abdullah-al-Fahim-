package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val sharedPrefs = application.getSharedPreferences("income_tracker_prefs", Context.MODE_PRIVATE)

    // Data streams
    val allTransactions: StateFlow<List<Transaction>>
    val allCategories: StateFlow<List<Category>>

    // Settings States
    val isBangla = MutableStateFlow(sharedPrefs.getBoolean("pref_bangla", false))
    val currentCurrency = MutableStateFlow(sharedPrefs.getString("pref_currency", "BDT") ?: "BDT") // "BDT", "USD", "INR"
    val isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("pref_dark_mode", false))
    val isPinEnabled = MutableStateFlow(sharedPrefs.getBoolean("pref_pin_enabled", false))
    val pinValue = MutableStateFlow(sharedPrefs.getString("pref_pin_value", "") ?: "")
    val isBiometricEnabled = MutableStateFlow(sharedPrefs.getBoolean("pref_biometric_enabled", false))
    val reminderEnabled = MutableStateFlow(sharedPrefs.getBoolean("pref_reminder_enabled", false))

    // UI Search & Dynamic Filters
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow("ALL") // "ALL", "INCOME", "EXPENSE"
    val filterCategory = MutableStateFlow("ALL")
    val selectedDateRange = MutableStateFlow<Pair<String, String>?>(null) // Pair(startDate, endDate) representing "YYYY-MM-DD"

    // Combined/Filtered Transactions
    val filteredTransactions: StateFlow<List<Transaction>>

    // Financial calculations
    val totalIncome: StateFlow<Double>
    val totalExpense: StateFlow<Double>
    val currentBalance: StateFlow<Double>

    // Alert details for UI feedback
    val operationMessage = MutableStateFlow<String?>(null)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database.categoryDao(), database.transactionDao())

        allTransactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allCategories = repository.allCategories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Reactive computation of financials
        totalIncome = allTransactions.map { list ->
            list.filter { it.type == "INCOME" }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        totalExpense = allTransactions.map { list ->
            list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        currentBalance = combine(totalIncome, totalExpense) { inc, exp ->
            inc - exp
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        // Setup reactive search and filter combined logic
        filteredTransactions = combine(
            allTransactions,
            searchQuery,
            filterType,
            filterCategory,
            selectedDateRange
        ) { txList, query, type, cat, range ->
            var list = txList

            // Search query filter (title or description)
            if (query.isNotEmpty()) {
                list = list.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
                }
            }

            // Type filter
            if (type != "ALL") {
                list = list.filter { it.type == type }
            }

            // Category filter
            if (cat != "ALL") {
                list = list.filter { it.category.equals(cat, ignoreCase = true) }
            }

            // Date Range filter
            if (range != null) {
                val start = range.first
                val end = range.second
                list = list.filter { it.date in start..end }
            }

            list
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Currency Symbol mapper
    fun getCurrencySymbol(): String {
        return when (currentCurrency.value) {
            "USD" -> "$"
            "INR" -> "₹"
            else -> "৳"
        }
    }

    // Operations
    fun saveTransaction(id: Int, amount: Double, type: String, title: String, category: String, date: String, time: String, paymentMethod: String, description: String) {
        viewModelScope.launch {
            val tx = Transaction(
                id = if (id == 0) 0 else id,
                amount = amount,
                type = type,
                title = title,
                category = category,
                date = date,
                time = time,
                paymentMethod = paymentMethod,
                description = description
            )
            if (id == 0) {
                repository.insertTransaction(tx)
                operationMessage.value = "Transaction added successfully"
            } else {
                repository.updateTransaction(tx)
                operationMessage.value = "Transaction updated successfully"
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            operationMessage.value = "Transaction deleted successfully"
        }
    }

    fun addCustomCategory(name: String, type: String, iconName: String = "Category") {
        viewModelScope.launch {
            val uppercaseType = type.uppercase()
            val cat = Category(
                name = name,
                type = uppercaseType,
                iconName = iconName,
                isCustom = true
            )
            repository.insertCategory(cat)
            operationMessage.value = "Category $name added successfully"
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            operationMessage.value = "Category deleted successfully"
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllTransactions()
            operationMessage.value = "All transaction data has been permanently reset"
        }
    }

    // Toggle Preferences
    fun setLanguage(bangla: Boolean) {
        isBangla.value = bangla
        sharedPrefs.edit().putBoolean("pref_bangla", bangla).apply()
    }

    fun setCurrency(currency: String) {
        currentCurrency.value = currency
        sharedPrefs.edit().putString("pref_currency", currency).apply()
    }

    fun setDarkMode(dark: Boolean) {
        isDarkMode.value = dark
        sharedPrefs.edit().putBoolean("pref_dark_mode", dark).apply()
    }

    fun setPin(enabled: Boolean, pin: String) {
        isPinEnabled.value = enabled
        pinValue.value = pin
        sharedPrefs.edit()
            .putBoolean("pref_pin_enabled", enabled)
            .putString("pref_pin_value", pin)
            .apply()
    }

    fun setBiometric(enabled: Boolean) {
        isBiometricEnabled.value = enabled
        sharedPrefs.edit().putBoolean("pref_biometric_enabled", enabled).apply()
    }

    fun setReminder(enabled: Boolean) {
        reminderEnabled.value = enabled
        sharedPrefs.edit().putBoolean("pref_reminder_enabled", enabled).apply()
    }

    fun clearMessage() {
        operationMessage.value = null
    }

    // LOCAL JSON BACKUP & RESTORE
    fun backupData() {
        viewModelScope.launch {
            try {
                val transactionsList = allTransactions.value
                val backupFile = File(getApplication<Application>().filesDir, "income_tracker_backup.txt")
                
                // Construct a custom simple CSV/Text representation to avoid complex dependencies
                // Format: TX|id|amount|type|title|category|date|time|paymentMethod|description
                val builder = StringBuilder()
                for (tx in transactionsList) {
                    builder.append("TX|")
                        .append(tx.id).append("|")
                        .append(tx.amount).append("|")
                        .append(tx.type).append("|")
                        .append(tx.title.replace("|", " ")).append("|")
                        .append(tx.category.replace("|", " ")).append("|")
                        .append(tx.date).append("|")
                        .append(tx.time).append("|")
                        .append(tx.paymentMethod.replace("|", " ")).append("|")
                        .append(tx.description.replace("|", " ").replace("\n", " "))
                        .append("\n")
                }

                backupFile.writeText(builder.toString())
                operationMessage.value = "Backup successful: Saved to Internal Storage"
            } catch (e: Exception) {
                Log.e("FinanceViewModel", "Backup error", e)
                operationMessage.value = "Backup failed: ${e.message}"
            }
        }
    }

    fun restoreData() {
        viewModelScope.launch {
            try {
                val backupFile = File(getApplication<Application>().filesDir, "income_tracker_backup.txt")
                if (!backupFile.exists()) {
                    operationMessage.value = "Restore failed: No backup file found. Make a backup first!"
                    return@launch
                }

                val lines = backupFile.readLines()
                var importedCount = 0
                for (line in lines) {
                    if (line.startsWith("TX|")) {
                        val parts = line.split("|")
                        if (parts.size >= 10) {
                            val amount = parts[2].toDoubleOrNull() ?: 0.0
                            val type = parts[3]
                            val title = parts[4]
                            val category = parts[5]
                            val date = parts[6]
                            val time = parts[7]
                            val paymentMethod = parts[8]
                            val description = parts[9]
                            
                            val tx = Transaction(
                                amount = amount,
                                type = type,
                                title = title,
                                category = category,
                                date = date,
                                time = time,
                                paymentMethod = paymentMethod,
                                description = description
                            )
                            repository.insertTransaction(tx)
                            importedCount++
                        }
                    }
                }
                operationMessage.value = "Successfully restored $importedCount transactions"
            } catch (e: Exception) {
                Log.e("FinanceViewModel", "Restore error", e)
                operationMessage.value = "Restore failed: ${e.message}"
            }
        }
    }
}
