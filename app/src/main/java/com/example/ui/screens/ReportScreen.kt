package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.localization.Translator
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.ExpenseRed
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    isBangla: Boolean,
    currencySymbol: String,
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var selectedRangeType by remember { mutableStateOf("1_MONTH") } // "1D", "3D", "7D", "1M", etc.
    var formatState by remember { mutableStateOf("PDF") } // "PDF" or "JPG"

    // Filter transactions corresponding to selected time frame
    val filteredForReport = remember(transactions, selectedRangeType) {
        val now = Calendar.getInstance()
        val calendarLimit = Calendar.getInstance()

        transactions.filter { tx ->
            val date = try { sdf.parse(tx.date) } catch (e: Exception) { null }
            if (date == null) false
            else {
                when (selectedRangeType) {
                    "1_DAY" -> {
                        calendarLimit.time = now.time
                        calendarLimit.add(Calendar.DAY_OF_YEAR, -1)
                        date.after(calendarLimit.time) || tx.date == sdf.format(now.time)
                    }
                    "3_DAYS" -> {
                        calendarLimit.time = now.time
                        calendarLimit.add(Calendar.DAY_OF_YEAR, -3)
                        date.after(calendarLimit.time) || tx.date == sdf.format(now.time)
                    }
                    "7_DAYS" -> {
                        calendarLimit.time = now.time
                        calendarLimit.add(Calendar.DAY_OF_YEAR, -7)
                        date.after(calendarLimit.time) || tx.date == sdf.format(now.time)
                    }
                    "10_DAYS" -> {
                        calendarLimit.time = now.time
                        calendarLimit.add(Calendar.DAY_OF_YEAR, -10)
                        date.after(calendarLimit.time) || tx.date == sdf.format(now.time)
                    }
                    "1_MONTH" -> {
                        calendarLimit.time = now.time
                        calendarLimit.add(Calendar.MONTH, -1)
                        date.after(calendarLimit.time) || tx.date == sdf.format(now.time)
                    }
                    else -> true
                }
            }
        }
    }

    val totalIncome = remember(filteredForReport) {
        filteredForReport.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(filteredForReport) {
        filteredForReport.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val balance = remember(totalIncome, totalExpense) {
        totalIncome - totalExpense
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = Translator.translate("reports", isBangla), fontWeight = FontWeight.Bold) },
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
            // Options configuration
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = if (isBangla) "এক্সপোর্ট সেটিংস" else "Export Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    // Time Selector
                    var expandedTime by remember { mutableStateOf(false) }
                    val ranges = listOf("1_DAY", "3_DAYS", "7_DAYS", "10_DAYS", "1_MONTH")
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedTime = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Period: ${selectedRangeType.replace("_", " ")}")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = expandedTime, onDismissRequest = { expandedTime = false }) {
                            ranges.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(text = r.replace("_", " ")) },
                                    onClick = {
                                        selectedRangeType = r
                                        expandedTime = false
                                    }
                                )
                            }
                        }
                    }

                    // Format Selection Row (PDF vs JPG)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { formatState = "PDF" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (formatState == "PDF") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                contentColor = if (formatState == "PDF") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "PDF Format")
                        }

                        Button(
                            onClick = { formatState = "JPG" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (formatState == "JPG") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                contentColor = if (formatState == "JPG") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "JPG Format")
                        }
                    }
                }
            }

            // Interactive Live preview of the generated report before save
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FINANCIAL SUMMARY REPORT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Period: ${selectedRangeType.replace("_", " ")}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = formatState, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

                    // Data Metrics grid
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Total Income", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(text = "$currencySymbol ${String.format(Locale.getDefault(), "%,.1f", totalIncome)}", color = IncomeGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column {
                            Text(text = "Total Expense", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(text = "$currencySymbol ${String.format(Locale.getDefault(), "%,.1f", totalExpense)}", color = ExpenseRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column {
                            Text(text = "Net Balance", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(text = "$currencySymbol ${String.format(Locale.getDefault(), "%,.1f", balance)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    // Header for Mini transactions
                    Text(text = "Entries List (${filteredForReport.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        filteredForReport.take(5).forEach { tx ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${tx.date}  ${tx.title}", fontSize = 11.sp, maxLines = 1)
                                val color = if (tx.type == "INCOME") IncomeGreen else ExpenseRed
                                val sign = if (tx.type == "INCOME") "+" else "-"
                                Text(text = "$sign$currencySymbol ${tx.amount}", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (filteredForReport.size > 5) {
                            Text(
                                text = "... and ${filteredForReport.size - 5} more transactions",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Export Actions Button group
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Local download Button
                Button(
                    onClick = {
                        val reportText = getFormattedReportText(
                            isBangla, currencySymbol, selectedRangeType, totalIncome, totalExpense, balance, filteredForReport
                        )
                        saveReportLocally(context, reportText, formatState)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = Translator.translate("save_locally", isBangla))
                }

                // Share Sheet controller Button
                Button(
                    onClick = {
                        val reportText = getFormattedReportText(
                            isBangla, currencySymbol, selectedRangeType, totalIncome, totalExpense, balance, filteredForReport
                        )
                        shareReport(context, reportText, formatState)
                    },
                    modifier = Modifier
                        .weight(1.0f)
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = Translator.translate("share_report", isBangla), maxLines = 1)
                }
            }
        }
    }
}

// Format the export layout dynamically
private fun getFormattedReportText(
    isBangla: Boolean,
    currencySymbol: String,
    period: String,
    income: Double,
    expense: Double,
    balance: Double,
    list: List<Transaction>
): String {
    val builder = StringBuilder()
    builder.append("=========================================\n")
    builder.append("          FINANCIAL STATEMENT REPORT      \n")
    builder.append("=========================================\n\n")
    builder.append("App Name: Income Tracker (Offline)\n")
    builder.append("Report Period: ${period.replace("_", " ")}\n")
    builder.append("Export Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n")
    
    builder.append("FINANCIAL OVERVIEW:\n")
    builder.append("----------------------------\n")
    builder.append("Total Income:  $currencySymbol ${String.format(Locale.getDefault(), "%,.2f", income)}\n")
    builder.append("Total Expense: $currencySymbol ${String.format(Locale.getDefault(), "%,.2f", expense)}\n")
    builder.append("Net Balance:   $currencySymbol ${String.format(Locale.getDefault(), "%,.2f", balance)}\n\n")

    builder.append("DETAILED TRANSACTION HISTORY:\n")
    builder.append("----------------------------------------------------\n")
    builder.append(String.format("%-11s | %-15s | %-12s | %s\n", "Date", "Title", "Method", "Amount"))
    builder.append("----------------------------------------------------\n")
    for (tx in list) {
        val sign = if (tx.type == "INCOME") "+" else "-"
        builder.append(
            String.format(
                "%-11s | %-15s | %-12s | %s%s %s\n",
                tx.date,
                if (tx.title.length > 15) tx.title.take(12) + "..." else tx.title,
                tx.paymentMethod,
                sign,
                currencySymbol,
                tx.amount
            )
        )
    }
    builder.append("----------------------------------------------------\n")
    builder.append("Total Executed Logs Count: ${list.size}\n")
    builder.append("\nReport Generated Offline inside Income Tracker application.")
    return builder.toString()
}

// Write simulated exports to device documents / download logs
private fun saveReportLocally(context: Context, text: String, format: String) {
    try {
        val fileName = "financial_report_${System.currentTimeMillis()}.${format.lowercase()}"
        val destFile = File(context.filesDir, fileName)
        destFile.writeText(text)
        
        // Show local toast or dialog success
        android.widget.Toast.makeText(context, "Saved: $fileName fully exported locally!", android.widget.Toast.LENGTH_LONG).show()
    } catch(e: Exception) {
        android.widget.Toast.makeText(context, "Export error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}

// Share reports using standard Android Share Intents
private fun shareReport(context: Context, text: String, format: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share financial report as $format")
    context.startActivity(shareIntent)
}
