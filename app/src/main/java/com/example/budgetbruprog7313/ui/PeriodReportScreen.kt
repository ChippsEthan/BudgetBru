package com.example.budgetbruprog7313.ui

import android.app.DatePickerDialog
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetbruprog7313.data.dao.ExpenseEntryDao
import com.example.budgetbruprog7313.data.model.ExpenseEntry
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodReportScreen() {
    val context = LocalContext.current
    val repository = remember {
        BudgetRepository(com.example.budgetbruprog7313.data.database.AppDatabase.getDatabase(context))
    }
    val scope = rememberCoroutineScope()

    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var entries by remember { mutableStateOf<List<ExpenseEntry>>(emptyList()) }
    var categoryTotals by remember { mutableStateOf<List<ExpenseEntryDao.CategorySpending>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val displayDateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val calendar = Calendar.getInstance()

    fun loadData() {
        if (startDate != null && endDate != null) {
            scope.launch {
                isLoading = true
                errorMessage = null
                try {
                    val entryList = repository.getEntriesBetweenDates(startDate!!, endDate!!).first()
                    entries = entryList
                    val totalsList = repository.getCategorySpending(startDate!!, endDate!!).first()
                    categoryTotals = totalsList
                    isLoading = false
                } catch (e: Exception) {
                    errorMessage = "Error loading data: ${e.message}"
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val cal = Calendar.getInstance()
        val start = cal.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val end = cal.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        startDate = start
        endDate = end
    }

    LaunchedEffect(startDate, endDate) {
        if (startDate != null && endDate != null) loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Reports", fontWeight = FontWeight.Bold, color = BudgetBruPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
        ) {
            // Date picker row
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    startDate = GregorianCalendar(year, month, dayOfMonth).time
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BudgetBruSecondary)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Start Date", Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(startDate?.let { displayDateFormat.format(it) } ?: "Start Date", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    endDate = GregorianCalendar(year, month, dayOfMonth).time
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BudgetBruSecondary)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "End Date", Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(endDate?.let { displayDateFormat.format(it) } ?: "End Date", fontSize = 12.sp)
                    }
                }
            }

            if (errorMessage != null) {
                Card(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = BudgetBruAccent.copy(alpha = 0.2f))) {
                    Text(errorMessage!!, Modifier.padding(12.dp), color = BudgetBruAccent)
                }
            }

            TabRow(selectedTabIndex = selectedTab, containerColor = DarkCard, contentColor = BudgetBruPrimary) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Entries") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Category Totals") })
            }

            when (selectedTab) {
                0 -> {
                    if (isLoading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    else if (entries.isEmpty()) EmptyReportState("No expenses found", Icons.Default.Receipt)
                    else LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(entries) { entry ->
                            ReportExpenseCard(expense = entry, dateFormat = dateFormat)
                        }
                    }
                }
                1 -> {
                    if (isLoading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    else if (categoryTotals.isEmpty()) EmptyReportState("No spending data", Icons.Default.PieChart)
                    else {
                        val totalSpent = categoryTotals.sumOf { it.total }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = BudgetBruPrimary.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total Spent:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Text("R${String.format("%.2f", totalSpent)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BudgetBruAccent)
                                    }
                                }
                            }
                            // Pie Chart
                            item {
                                CategoryPieChart(categoryTotals = categoryTotals, totalSpent = totalSpent)
                            }
                            items(categoryTotals) { total ->
                                CategoryTotalRow(total = total, totalSpent = totalSpent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryPieChart(categoryTotals: List<ExpenseEntryDao.CategorySpending>, totalSpent: Double) {
    if (categoryTotals.isEmpty() || totalSpent == 0.0) return
    val colors = listOf(
        BudgetBruPrimary, BudgetBruSecondary, BudgetBruAccent,
        Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFFEF4444),
        Color(0xFF8B5CF6), Color(0xFF06B6D4), Color(0xFFF97316)
    )
    var startAngle = -90f
    val animatedProgress by animateFloatAsState(targetValue = 1f, animationSpec = tween(800), label = "pie")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Spending Distribution", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BudgetBruPrimary)
            Spacer(Modifier.height(12.dp))
            Canvas(modifier = Modifier.size(220.dp)) {
                var currentStart = startAngle
                categoryTotals.forEachIndexed { index, cat ->
                    val sweep = (cat.total / totalSpent * 360).toFloat() * animatedProgress
                    val path = Path().apply {
                        moveTo(size.width / 2, size.height / 2)
                        arcTo(Rect(0f, 0f, size.width, size.height), currentStart, sweep, false)
                        close()
                    }
                    drawPath(path, colors[index % colors.size])
                    currentStart += sweep
                }
                drawCircle(color = DarkCard, radius = size.width * 0.35f, center = Offset(size.width / 2, size.height / 2))
            }
            Spacer(Modifier.height(16.dp))
            // Legend
            Column(modifier = Modifier.fillMaxWidth()) {
                categoryTotals.forEachIndexed { idx, cat ->
                    val percent = ((cat.total / totalSpent) * 100).toInt()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(colors[idx % colors.size]))
                            Spacer(Modifier.width(8.dp))
                            Text(cat.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Text("R${String.format("%.2f", cat.total)} ($percent%)", fontSize = 13.sp, color = BudgetBruAccent)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportExpenseCard(expense: ExpenseEntry, dateFormat: SimpleDateFormat) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(expense.description, fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text("R${String.format("%.2f", expense.amount)}", color = BudgetBruAccent, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text("${dateFormat.format(expense.date)} • ${expense.startTime} - ${expense.endTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!expense.photoPath.isNullOrEmpty()) {
                Icon(Icons.Default.Image, contentDescription = "Photo", tint = BudgetBruPrimary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun CategoryTotalRow(total: ExpenseEntryDao.CategorySpending, totalSpent: Double) {
    val percent = if (totalSpent > 0) (total.total / totalSpent * 100).toInt() else 0
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(total.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text("R${String.format("%.2f", total.total)}", fontWeight = FontWeight.Bold, color = BudgetBruAccent)
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(progress = percent / 100f, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = BudgetBruPrimary, trackColor = Color.Gray.copy(alpha = 0.2f))
            Text("$percent% of total", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun EmptyReportState(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = message, Modifier.size(64.dp), tint = BudgetBruPrimary.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))
            Text(message, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}