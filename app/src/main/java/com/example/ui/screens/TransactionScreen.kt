package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.ui.components.IconMapper
import com.example.ui.localization.Translator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    isBangla: Boolean,
    currencySymbol: String,
    transaction: Transaction?, // null means ADD mode, non-null means EDIT mode
    initialType: String, // "INCOME" or "EXPENSE"
    categories: List<Category>,
    onBackClick: () -> Unit,
    onSaveClick: (id: Int, amount: Double, type: String, title: String, category: String, date: String, time: String, paymentMethod: String, description: String) -> Unit,
    onDeleteClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Form states initialized with existing values if editing
    var amountStr by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var title by remember { mutableStateOf(transaction?.title ?: "") }
    var selectedType by remember { mutableStateOf(transaction?.type ?: initialType) }
    
    // Sort categories corresponding to Income / Expense
    val typeCategories = remember(categories, selectedType) {
        categories.filter { it.type == selectedType }
    }
    
    var selectedCategory by remember { 
        mutableStateOf(transaction?.category ?: typeCategories.firstOrNull()?.name ?: "Others") 
    }
    
    // Auto-update category if type changes and current category does not match newly selected type categories list
    LaunchedEffect(selectedType, typeCategories) {
        if (transaction == null || transaction.type != selectedType) {
            typeCategories.firstOrNull()?.name?.let {
                selectedCategory = it
            }
        }
    }

    // Date & Time formatting states
    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
    var dateStr by remember { mutableStateOf(transaction?.date ?: sdfDate.format(calendar.time)) }
    var timeStr by remember { mutableStateOf(transaction?.time ?: sdfTime.format(calendar.time)) }

    var paymentMethod by remember { mutableStateOf(transaction?.paymentMethod ?: "Cash") }
    var description by remember { mutableStateOf(transaction?.description ?: "") }

    var showError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Date Picker launcher
    val datePickerDialog = remember {
        val parts = dateStr.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
        val month = parts.getOrNull(1)?.toIntOrNull()?.minus(1) ?: calendar.get(Calendar.MONTH)
        val day = parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
        
        DatePickerDialog(context, { _, y, m, d ->
            val newCal = Calendar.getInstance()
            newCal.set(y, m, d)
            dateStr = sdfDate.format(newCal.time)
        }, year, month, day)
    }

    // Time Picker launcher
    val timePickerDialog = remember {
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: calendar.get(Calendar.MINUTE)
        
        TimePickerDialog(context, { _, h, m ->
            val formattedHour = String.format(Locale.getDefault(), "%02d", h)
            val formattedMinute = String.format(Locale.getDefault(), "%02d", m)
            timeStr = "$formattedHour:$formattedMinute"
        }, hour, minute, true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (transaction == null) {
                            Translator.translate("add_transaction", isBangla)
                        } else {
                            Translator.translate("edit_transaction", isBangla)
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (transaction != null) {
                        IconButton(onClick = { onDeleteClick(transaction) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
            // Type Toggle (INCOME / EXPENSE)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // INCOME segment selector
                Button(
                    onClick = { selectedType = "INCOME" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == "INCOME") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        },
                        contentColor = if (selectedType == "INCOME") {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        }
                    )
                ) {
                    Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = Translator.translate("income", isBangla))
                }

                // EXPENSE segment selector
                Button(
                    onClick = { selectedType = "EXPENSE" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == "EXPENSE") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        },
                        contentColor = if (selectedType == "EXPENSE") {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        }
                    )
                ) {
                    Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = Translator.translate("expense", isBangla))
                }
            }

            // Amount input card with Currency decorator
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text(text = Translator.translate("amount", isBangla)) },
                leadingIcon = {
                    Text(
                        text = currencySymbol,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Title/Note Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(text = Translator.translate("title", isBangla)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.EditNote, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Picker Layout
            var categoryDropdownExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = Translator.translate("category", isBangla)) },
                    leadingIcon = {
                        Icon(imageVector = IconMapper.getIconByName(selectedCategory), contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { categoryDropdownExpanded = !categoryDropdownExpanded }) {
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    typeCategories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = IconMapper.getIconByName(category.iconName),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = category.name)
                                }
                            },
                            onClick = {
                                selectedCategory = category.name
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Date & Time pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date picker trigger field
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = Translator.translate("date", isBangla)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Date",
                            modifier = Modifier.clickable { datePickerDialog.show() }
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { datePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp)
                )

                // Time picker trigger field
                OutlinedTextField(
                    value = timeStr,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = Translator.translate("time", isBangla)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = "Time",
                            modifier = Modifier.clickable { timePickerDialog.show() }
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { timePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Payment Method Selector
            var pmExpanded by remember { mutableStateOf(false) }
            val paymentMethods = listOf("Cash", "Card", "Bank Transfer", "bKash", "Nagad", "Rocket")
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = Translator.translate("payment_method", isBangla)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Payments, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { pmExpanded = !pmExpanded }) {
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pmExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = pmExpanded,
                    onDismissRequest = { pmExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    paymentMethods.forEach { pm ->
                        DropdownMenuItem(
                            text = { Text(text = pm) },
                            onClick = {
                                paymentMethod = pm
                                pmExpanded = false
                            }
                        )
                    }
                }
            }

            // Optional Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(text = Translator.translate("description", isBangla)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Description, contentDescription = null)
                },
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Dynamic validations error tag
            if (showError) {
                Text(
                    text = if (isBangla) "দয়া করে সঠিক পরিমাণ এবং শিরোনাম প্রদান করুন" else "Please enter a valid amount and title to save",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save transaction button
            Button(
                onClick = {
                    val amountValue = amountStr.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0 && title.isNotBlank()) {
                        showError = false
                        onSaveClick(
                            transaction?.id ?: 0,
                            amountValue,
                            selectedType,
                            title,
                            selectedCategory,
                            dateStr,
                            timeStr,
                            paymentMethod,
                            description
                        )
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Translator.translate("save", isBangla),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
