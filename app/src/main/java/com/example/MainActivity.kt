package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Transaction
import com.example.ui.localization.Translator
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ViewModelProvider(this).get(FinanceViewModel::class.java)
        setContent {

            // Observe settings configurations
            val isBangla by viewModel.isBangla.collectAsStateWithLifecycle()
            val currentCurrency by viewModel.currentCurrency.collectAsStateWithLifecycle()
            val isDarkModePref by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkActive = remember(isDarkModePref, systemDark) { isDarkModePref }

            // Currency symbol mapping
            val currencySymbol = remember(currentCurrency) {
                when (currentCurrency) {
                    "USD" -> "$"
                    "INR" -> "₹"
                    else -> "৳"
                }
            }

            // Database streams
            val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
            val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
            val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()

            // Financial Calculations
            val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
            val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
            val currentBalance by viewModel.currentBalance.collectAsStateWithLifecycle()

            // Security properties
            val isPinEnabled by viewModel.isPinEnabled.collectAsStateWithLifecycle()
            val pinValue by viewModel.pinValue.collectAsStateWithLifecycle()
            val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
            val reminderEnabled by viewModel.reminderEnabled.collectAsStateWithLifecycle()

            // Main Navigation Controller States
            var activeTab by remember { mutableStateOf(0) } // 0: Home, 1: Analytics, 2: Categories, 3: Reports, 4: Settings, 5: About
            var addTransactionType by remember { mutableStateOf<String?>(null) } // "INCOME" or "EXPENSE"
            var activeEditTransaction by remember { mutableStateOf<Transaction?>(null) }
            var isUnlocked by remember { mutableStateOf(false) }

            // Toasts & messaging controller
            val operationMessage by viewModel.operationMessage.collectAsStateWithLifecycle()
            val context = this

            LaunchedEffect(operationMessage) {
                operationMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearMessage()
                }
            }

            MyApplicationTheme(darkTheme = darkActive) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Security gate rendering if PIN enabled
                    if (isPinEnabled && !isUnlocked && pinValue.isNotEmpty()) {
                        LockScreen(
                            isBangla = isBangla,
                            correctPin = pinValue,
                            isBiometricEnabled = isBiometricEnabled,
                            onUnlockSuccess = { isUnlocked = true }
                        )
                    } else {
                        // Core ledger screen content Viewport
                        if (addTransactionType != null) {
                            // Render adding transaction screen overlay
                            TransactionScreen(
                                isBangla = isBangla,
                                currencySymbol = currencySymbol,
                                transaction = null,
                                initialType = addTransactionType!!,
                                categories = allCategories,
                                onBackClick = { addTransactionType = null },
                                onSaveClick = { id, amount, type, title, category, date, time, pm, desc ->
                                    viewModel.saveTransaction(id, amount, type, title, category, date, time, pm, desc)
                                    addTransactionType = null
                                },
                                onDeleteClick = { /* No-op in add mode */ }
                            )
                        } else if (activeEditTransaction != null) {
                            // Render editing transaction screen overlay
                            TransactionScreen(
                                isBangla = isBangla,
                                currencySymbol = currencySymbol,
                                transaction = activeEditTransaction,
                                initialType = activeEditTransaction!!.type,
                                categories = allCategories,
                                onBackClick = { activeEditTransaction = null },
                                onSaveClick = { id, amount, type, title, category, date, time, pm, desc ->
                                    viewModel.saveTransaction(id, amount, type, title, category, date, time, pm, desc)
                                    activeEditTransaction = null
                                },
                                onDeleteClick = { tx ->
                                    viewModel.deleteTransaction(tx)
                                    activeEditTransaction = null
                                }
                            )
                        } else {
                            // Render standard main workspace containing Bottom Navigation
                            Scaffold(
                                bottomBar = {
                                    NavigationBar {
                                        // Tab 0: Dashboard
                                        NavigationBarItem(
                                            selected = activeTab == 0,
                                            onClick = { activeTab = 0 },
                                            icon = { Icon(imageVector = Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                                            label = { Text(text = Translator.translate("dashboard", isBangla), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                        )

                                        // Tab 1: Analytics (Graph)
                                        NavigationBarItem(
                                            selected = activeTab == 1,
                                            onClick = { activeTab = 1 },
                                            icon = { Icon(imageVector = Icons.Filled.InsertChart, contentDescription = "Analytics") },
                                            label = { Text(text = Translator.translate("analytics", isBangla), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                        )

                                        // Tab 2: Categories
                                        NavigationBarItem(
                                            selected = activeTab == 2,
                                            onClick = { activeTab = 2 },
                                            icon = { Icon(imageVector = Icons.Filled.Category, contentDescription = "Categories") },
                                            label = { Text(text = Translator.translate("category", isBangla), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                        )

                                        // Tab 3: Reports
                                        NavigationBarItem(
                                            selected = activeTab == 3,
                                            onClick = { activeTab = 3 },
                                            icon = { Icon(imageVector = Icons.Filled.Description, contentDescription = "Reports") },
                                            label = { Text(text = Translator.translate("reports", isBangla), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                        )

                                        // Tab 4: Settings
                                        NavigationBarItem(
                                            selected = activeTab == 4,
                                            onClick = { activeTab = 4 },
                                            icon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings") },
                                            label = { Text(text = Translator.translate("settings", isBangla), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                        )

                                        // Tab 5: About Author info
                                        NavigationBarItem(
                                            selected = activeTab == 5,
                                            onClick = { activeTab = 5 },
                                            icon = { Icon(imageVector = Icons.Filled.Info, contentDescription = "About") },
                                            label = { Text(text = Translator.translate("about", isBangla), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                        )
                                    }
                                }
                            ) { scaffoldPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(scaffoldPadding)
                                ) {
                                    when (activeTab) {
                                        0 -> DashboardScreen(
                                            isBangla = isBangla,
                                            currencySymbol = currencySymbol,
                                            totalIncome = totalIncome,
                                            totalExpense = totalExpense,
                                            currentBalance = currentBalance,
                                            recentTransactions = allTransactions,
                                            onAddTransactionClick = { type -> addTransactionType = type },
                                            onTransactionClick = { tx -> activeEditTransaction = tx }
                                        )
                                        1 -> AnalyticsScreen(
                                            isBangla = isBangla,
                                            currencySymbol = currencySymbol,
                                            transactions = allTransactions
                                        )
                                        2 -> CategoryScreen(
                                            isBangla = isBangla,
                                            categories = allCategories,
                                            onAddCustomCategory = { name, type, icon ->
                                                viewModel.addCustomCategory(name, type, icon)
                                            },
                                            onDeleteCategory = { cat ->
                                                viewModel.deleteCategory(cat)
                                            }
                                        )
                                        3 -> ReportScreen(
                                            isBangla = isBangla,
                                            currencySymbol = currencySymbol,
                                            transactions = allTransactions
                                        )
                                        4 -> SettingsScreen(
                                            isBangla = isBangla,
                                            onLanguageChange = { bangla -> viewModel.setLanguage(bangla) },
                                            currentCurrency = currentCurrency,
                                            onCurrencyChange = { cur -> viewModel.setCurrency(cur) },
                                            isDarkMode = isDarkModePref,
                                            onDarkModeChange = { dark -> viewModel.setDarkMode(dark) },
                                            isPinEnabled = isPinEnabled,
                                            pinValue = pinValue,
                                            onPinChange = { enabled, v -> viewModel.setPin(enabled, v) },
                                            isBiometricEnabled = isBiometricEnabled,
                                            onBiometricChange = { biometric -> viewModel.setBiometric(biometric) },
                                            reminderEnabled = reminderEnabled,
                                            onReminderChange = { r -> viewModel.setReminder(r) },
                                            onBackupClick = { viewModel.backupData() },
                                            onRestoreClick = { viewModel.restoreData() },
                                            onResetClick = { viewModel.clearAllData() }
                                        )
                                        5 -> AboutScreen(
                                            isBangla = isBangla
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

