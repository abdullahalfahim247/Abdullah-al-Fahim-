package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.localization.Translator
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.ExpenseRed
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    isBangla: Boolean,
    currencySymbol: String,
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Active filters
    var timeFilter by remember { mutableStateOf("7_DAYS") } // "1_DAY", "3_DAYS", "7_DAYS", "10_DAYS", "1_MONTH", "CUSTOM"
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var startDateStr by remember { mutableStateOf("") }
    var endDateStr by remember { mutableStateOf("") }

    // Date range filter calculations
    val filteredTransactions = remember(transactions, timeFilter, startDateStr, endDateStr) {
        val now = Calendar.getInstance()
        val limitCalendar = Calendar.getInstance()

        val parsedStart = try { sdf.parse(startDateStr) } catch(e: Exception) { null }
        val parsedEnd = try { sdf.parse(endDateStr) } catch(e: Exception) { null }

        transactions.filter { tx ->
            val txDate = try { sdf.parse(tx.date) } catch(e: Exception) { null }
            if (txDate == null) false
            else {
                when (timeFilter) {
                    "1_DAY" -> {
                        limitCalendar.time = now.time
                        limitCalendar.add(Calendar.DAY_OF_YEAR, -1)
                        txDate.after(limitCalendar.time) || tx.date == sdf.format(now.time)
                    }
                    "3_DAYS" -> {
                        limitCalendar.time = now.time
                        limitCalendar.add(Calendar.DAY_OF_YEAR, -3)
                        txDate.after(limitCalendar.time) || tx.date == sdf.format(now.time)
                    }
                    "7_DAYS" -> {
                        limitCalendar.time = now.time
                        limitCalendar.add(Calendar.DAY_OF_YEAR, -7)
                        txDate.after(limitCalendar.time) || tx.date == sdf.format(now.time)
                    }
                    "10_DAYS" -> {
                        limitCalendar.time = now.time
                        limitCalendar.add(Calendar.DAY_OF_YEAR, -10)
                        txDate.after(limitCalendar.time) || tx.date == sdf.format(now.time)
                    }
                    "1_MONTH" -> {
                        limitCalendar.time = now.time
                        limitCalendar.add(Calendar.MONTH, -1)
                        txDate.after(limitCalendar.time) || tx.date == sdf.format(now.time)
                    }
                    "CUSTOM" -> {
                        if (parsedStart != null && parsedEnd != null) {
                            (txDate.after(parsedStart) || tx.date == startDateStr) &&
                                    (txDate.before(parsedEnd) || tx.date == endDateStr)
                        } else true
                    }
                    else -> true
                }
            }
        }
    }

    // Mathematical values derived from Filtered List
    val totalInc = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExp = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }

    // Pie chart values (category spending)
    val categorySpending = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .mapValues { group -> group.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    val chartColors = listOf(
        Color(0xFF3B82F6), // Blue 500
        Color(0xFFF59E0B), // Amber 500
        Color(0xFF10B981), // Emerald 500
        Color(0xFFEC4899), // Pink 500
        Color(0xFF8B5CF6), // Purple 500
        Color(0xFFEF4444), // Red 500
        Color(0xFF06B6D4), // Cyan 500
        Color(0xFF14B8A6), // Teal 500
        Color(0xFFF97316), // Orange 500
        Color(0xFF64748B)  // Slate 500
    )

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = Translator.translate("analytics", isBangla), fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time filter controls row
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = Translator.translate("time_filter", isBangla),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val filters = listOf(
                            "1D" to "1_DAY",
                            "3D" to "3_DAYS",
                            "7D" to "7_DAYS",
                            "10D" to "10_DAYS",
                            "1M" to "1_MONTH",
                            "Custom" to "CUSTOM"
                        )
                        filters.forEach { (label, key) ->
                            val isSelected = timeFilter == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    )
                                    .clickable {
                                        timeFilter = key
                                        if (key == "CUSTOM") {
                                            showCustomDatePicker = true
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            // Custom Range selectors details
            if (timeFilter == "CUSTOM" && showCustomDatePicker) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val now = Calendar.getInstance()
                                    DatePickerDialog(context, { _, y, m, d ->
                                        val cal = Calendar.getInstance().apply { set(y, m, d) }
                                        startDateStr = sdf.format(cal.time)
                                    }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = if (startDateStr.isEmpty()) "Start Date" else startDateStr)
                            }

                            Button(
                                onClick = {
                                    val now = Calendar.getInstance()
                                    DatePickerDialog(context, { _, y, m, d ->
                                        val cal = Calendar.getInstance().apply { set(y, m, d) }
                                        endDateStr = sdf.format(cal.time)
                                    }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = if (endDateStr.isEmpty()) "End Date" else endDateStr)
                            }
                        }
                    }
                }
            }

            // Summary Analytics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = Translator.translate("total_income", isBangla), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(text = "$currencySymbol ${String.format(Locale.getDefault(), "%,.1f", totalInc)}", color = IncomeGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = Translator.translate("total_expense", isBangla), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(text = "$currencySymbol ${String.format(Locale.getDefault(), "%,.1f", totalExp)}", color = ExpenseRed, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            // Bar Chart Comparison Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "আয় বনাম ব্যয় তুলনা" else "Income vs Expense Comparison",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    IncomeExpenseBarChart(income = totalInc, expense = totalExp, currencySymbol = currencySymbol)
                }
            }

            // Pie Chart Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Translator.translate("category_wise", isBangla),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (categorySpending.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = if (isBangla) "ব্যয়ের কোনো ক্যাটেগরি ডেটা নেই" else "No expense category data to display.", color = MaterialTheme.colorScheme.secondary)
                        }
                    } else {
                        CategoryPieChart(
                            spendingList = categorySpending,
                            colorsList = chartColors,
                            currencySymbol = currencySymbol
                        )
                    }
                }
            }

            // Line Graph Section (spending trends)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "দৈনিক ব্যয়ের ধারা" else "Daily Spending Trend",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    val dailySpending = remember(filteredTransactions) {
                        filteredTransactions.filter { it.type == "EXPENSE" }
                            .groupBy { it.date }
                            .mapValues { it.value.sumOf { p -> p.amount } }
                            .toList()
                            .sortedBy { it.first }
                    }

                    if (dailySpending.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = if (isBangla) "লাইন গ্রাফের জন্য কোনো ডেটা নেই" else "No spending trend data to display.", color = MaterialTheme.colorScheme.secondary)
                        }
                    } else {
                        LineGraphTrend(
                            dailyDataPoints = dailySpending,
                            currencySymbol = currencySymbol
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Custom Native Bar Chart inside Canvas
@Composable
fun IncomeExpenseBarChart(
    income: Double,
    expense: Double,
    currencySymbol: String
) {
    val maxBarVal = maxOf(income, expense, 1.0).toFloat()
    val incomePercent = (income / maxBarVal).toFloat()
    val expensePercent = (expense / maxBarVal).toFloat()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 12.dp)
    ) {
        val totalWidth = size.width
        val totalHeight = size.height

        val barWidth = 60.dp.toPx()
        val spacing = 80.dp.toPx()

        val startIncomeX = (totalWidth / 2) - barWidth - (spacing / 2)
        val startExpenseX = (totalWidth / 2) + (spacing / 2)

        // Draw Income Column
        val incomeBarHeight = totalHeight * incomePercent
        drawRoundRect(
            color = IncomeGreen,
            topLeft = Offset(startIncomeX, totalHeight - incomeBarHeight),
            size = Size(barWidth, incomeBarHeight),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
        )

        // Draw Expense Column
        val expenseBarHeight = totalHeight * expensePercent
        drawRoundRect(
            color = ExpenseRed,
            topLeft = Offset(startExpenseX, totalHeight - expenseBarHeight),
            size = Size(barWidth, expenseBarHeight),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(12.dp).background(IncomeGreen, shape = CircleShape))
            Text(text = "Income", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "$currencySymbol ${String.format(Locale.getDefault(), "%,.0f", income)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(12.dp).background(ExpenseRed, shape = CircleShape))
            Text(text = "Expense", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "$currencySymbol ${String.format(Locale.getDefault(), "%,.0f", expense)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Custom Native Pie Chart inside Canvas
@Composable
fun CategoryPieChart(
    spendingList: List<Pair<String, Double>>,
    colorsList: List<Color>,
    currencySymbol: String
) {
    val totalExpense = spendingList.sumOf { it.second }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Draw Radial segments
        Canvas(
            modifier = Modifier
                .size(140.dp)
                .weight(1f)
        ) {
            var currentStartAngle = -90f
            spendingList.forEachIndexed { index, pair ->
                val sweep = ((pair.second / totalExpense) * 360f).toFloat()
                drawArc(
                    color = colorsList.getOrElse(index) { Color.Gray },
                    startAngle = currentStartAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    size = Size(size.width, size.height),
                    style = Stroke(width = 28.dp.toPx(), cap = StrokeCap.Round)
                )
                currentStartAngle += sweep
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Legends
        Column(
            modifier = Modifier.weight(1.2f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            spendingList.take(6).forEachIndexed { index, pair ->
                val percentVal = (pair.second / totalExpense) * 100.0
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(colorsList.getOrElse(index) { Color.Gray }, shape = CircleShape)
                    )
                    Text(
                        text = "${pair.first}: ${String.format(Locale.getDefault(), "%.1f", percentVal)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Custom Native Line Graph Trend inside Canvas
@Composable
fun LineGraphTrend(
    dailyDataPoints: List<Pair<String, Double>>,
    currencySymbol: String
) {
    val maxVal = maxOf(dailyDataPoints.maxByOrNull { it.second }?.second ?: 1.0, 1.0).toFloat()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 12.dp)
    ) {
        val totalWidth = size.width
        val totalHeight = size.height

        val stepX = totalWidth / (dailyDataPoints.size - 1).coerceAtLeast(1)
        val path = Path()

        dailyDataPoints.forEachIndexed { index, pair ->
            val x = index * stepX
            val y = totalHeight - ((pair.second.toFloat() / maxVal) * totalHeight * 0.85f)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Draw a neat point
            drawCircle(
                color = ExpenseRed,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }

        // Draw Line Stroke
        drawPath(
            path = path,
            color = ExpenseRed,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // Horizontal Labels
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val firstDate = dailyDataPoints.firstOrNull()?.first ?: ""
        val lastDate = dailyDataPoints.lastOrNull()?.first ?: ""
        Text(text = firstDate, fontSize = 9.sp, color = Color.Gray)
        Text(text = "Chronology", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(text = lastDate, fontSize = 9.sp, color = Color.Gray)
    }
}
