package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.Translator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isBangla: Boolean,
    onLanguageChange: (Boolean) -> Unit,
    currentCurrency: String,
    onCurrencyChange: (String) -> Unit,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    isPinEnabled: Boolean,
    pinValue: String,
    onPinChange: (Boolean, String) -> Unit,
    isBiometricEnabled: Boolean,
    onBiometricChange: (Boolean) -> Unit,
    reminderEnabled: Boolean,
    onReminderChange: (Boolean) -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var pinDialogShow by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = Translator.translate("settings", isBangla), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General preferences card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isBangla) "সাধারণ পছন্দসমূহ" else "GENERAL PREFERENCES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    // Language Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = Translator.translate("language", isBangla), fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = isBangla,
                            onCheckedChange = onLanguageChange
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    // Dark Mode Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = Translator.translate("dark_mode", isBangla), fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onDarkModeChange
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    // Currency Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.AttachMoney, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = Translator.translate("currency", isBangla), fontWeight = FontWeight.SemiBold)
                        }
                        
                        var currencyExpanded by remember { mutableStateOf(false) }
                        Box {
                            Button(
                                onClick = { currencyExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = currentCurrency, color = MaterialTheme.colorScheme.onBackground)
                            }
                            
                            DropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                                listOf("BDT", "USD", "INR").forEach { cur ->
                                    DropdownMenuItem(
                                        text = { Text(text = cur) },
                                        onClick = {
                                            onCurrencyChange(cur)
                                            currencyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Security controls Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isBangla) "নিরাপত্তা" else "SECURITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    // PIN lock config toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Pin, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = Translator.translate("pin_lock", isBangla), fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    pinDialogShow = true
                                } else {
                                    onPinChange(false, "")
                                }
                            }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    // Biometric lock config toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = Translator.translate("biometric_lock", isBangla), fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = onBiometricChange
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    // Notification entries reminder bell toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = if (isBangla) "দৈনিক মনে করিয়ে দেওয়ার নোটিফিকেশন" else "Daily Reminder Notification", fontWeight = FontWeight.SemiBold)
                        }
                        Switch(checked = reminderEnabled, onCheckedChange = onReminderChange)
                    }
                }
            }

            // Backup, Restore & Reset card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isBangla) "তথ্য ব্যবস্থাপনা" else "DATA MANAGEMENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    // Execute Backup local
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = if (isBangla) "ব্যাকআপ তথ্য সেভ করুন" else "Create Local Backup", fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = onBackupClick) {
                            Icon(imageVector = Icons.Filled.Save, contentDescription = "Backup Now", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    // Execute Restore local
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = if (isBangla) "ব্যাকআপ তথ্য রিস্টোর করুন" else "Restore From Backup", fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = onRestoreClick) {
                            Icon(imageVector = Icons.Filled.RestorePage, contentDescription = "Restore Now", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    // Permanent Database data wipe/reset operation button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = Translator.translate("reset_data", isBangla), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                        }
                        Button(
                            onClick = onResetClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = if (isBangla) "রিসেট দিন" else "Reset Now", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Setup 4-digit PIN dialog
        if (pinDialogShow) {
            var inputPin by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { pinDialogShow = false },
                title = { Text(text = "Configure 4-Digit Security PIN", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Please write a 4-digit numeric credentials PIN to secure reports and dashboard entry.")
                        OutlinedTextField(
                            value = inputPin,
                            onValueChange = { if (it.length <= 4) inputPin = it },
                            label = { Text(text = "Enter PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputPin.length == 4 && inputPin.all { it.isDigit() }) {
                                onPinChange(true, inputPin)
                                pinDialogShow = false
                            }
                        }
                    ) {
                        Text(text = "Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pinDialogShow = false }) {
                        Text(text = Translator.translate("cancel", isBangla))
                    }
                }
            )
        }
    }
}
