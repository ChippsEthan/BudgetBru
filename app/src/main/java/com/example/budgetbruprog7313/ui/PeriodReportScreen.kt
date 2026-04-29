package com.example.budgetbruprog7313.ui

import androidx.compose.foundation.Image
import coil.compose.rememberAsyncImagePainter
import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
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

    // Use Calendar for date selection
    val calendar = Calendar.getInstance()
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var entries by remember { mutableStateOf<List<ExpenseEntry>>(emptyList()) }
    var categoryTotals by remember { mutableStateOf<List<ExpenseEntryDao.CategorySpending>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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

    // Set default dates (current month)
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

    // Load data when dates change
    LaunchedEffect(startDate, endDate) {
        if (startDate != null && endDate != null) {
            loadData()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Expense Reports", style = MaterialTheme.typography.headlineSmall, color = BudgetBruPrimary)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Start Date Button
            Button(
                onClick = {
                    val datePicker = DatePickerDialog(
                        context,
                        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                            val selectedDate = GregorianCalendar(year, month, dayOfMonth).time
                            startDate = selectedDate
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePicker.show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard)
            ) {
                Text(
                    startDate?.let { displayDateFormat.format(it) } ?: "Start Date",
                    color = Color.White
                )
            }

            // End Date Button
            Button(
                onClick = {
                    val datePicker = DatePickerDialog(
                        context,
                        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                            val selectedDate = GregorianCalendar(year, month, dayOfMonth).time
                            endDate = selectedDate
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePicker.show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard)
            ) {
                Text(
                    endDate?.let { displayDateFormat.format(it) } ?: "End Date",
                    color = Color.White
                )
            }
        }

        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BudgetBruAccent.copy(alpha = 0.2f))
            ) {
                Text(errorMessage!!, modifier = Modifier.padding(12.dp), color = BudgetBruAccent)
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkCard,
            contentColor = BudgetBruPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Entries", color = if (selectedTab == 0) BudgetBruPrimary else Color.White) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Category Totals", color = if (selectedTab == 1) BudgetBruPrimary else Color.White) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (entries.isEmpty()) {
                    ReportEmptyStateCard(
                        title = "No expenses found",
                        message = "No expenses found for this period.\nAdd some expenses from the home screen!",
                        icon = Icons.Default.Receipt
                    )
                } else {
                    LazyColumn {
                        items(entries) { expenseEntry ->
                            ReportExpenseCard(expense = expenseEntry, dateFormat = dateFormat)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
            1 -> {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (categoryTotals.isEmpty()) {
                    ReportEmptyStateCard(
                        title = "No spending data",
                        message = "No spending in this period",
                        icon = Icons.Default.PieChart
                    )
                } else {
                    val totalSpent = categoryTotals.sumOf { it.total }

                    // Pie Chart Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Spending Distribution",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BudgetBruPrimary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            CategoryPieChart(categoryTotals = categoryTotals)

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Total Spent:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "R${String.format(Locale.getDefault(), "%.2f", totalSpent)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BudgetBruAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Breakdown by Category",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BudgetBruSecondary,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categoryTotals) { total ->
                            CategoryTotalRow(total, totalSpent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryPieChart(categoryTotals: List<ExpenseEntryDao.CategorySpending>) {
    val total = categoryTotals.sumOf { it.total }
    if (total == 0.0) return

    val pieColors = listOf(
        BudgetBruPrimary,
        BudgetBruSecondary,
        BudgetBruAccent,
        Color(0xFFF59E0B),
        Color(0xFF10B981),
        Color(0xFFEF4444),
        Color(0xFF8B5CF6),
        Color(0xFF06B6D4),
        Color(0xFFF97316),
        Color(0xFF84CC16)
    )

    var startAngle = -90f
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "pieChartProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                var currentStartAngle = startAngle
                categoryTotals.forEachIndexed { index, category ->
                    val sweepAngle = (category.total / total * 360).toFloat() * animatedProgress
                    val path = Path().apply {
                        moveTo(size.width / 2, size.height / 2)
                        arcTo(
                            rect = Rect(
                                left = 0f,
                                top = 0f,
                                right = size.width,
                                bottom = size.height
                            ),
                            startAngleDegrees = currentStartAngle,
                            sweepAngleDegrees = sweepAngle,
                            forceMoveTo = false
                        )
                        close()
                    }
                    drawPath(
                        path = path,
                        color = pieColors[index % pieColors.size]
                    )
                    currentStartAngle += sweepAngle
                }

                drawCircle(
                    color = DarkCard,
                    radius = size.width * 0.35f,
                    center = Offset(size.width / 2, size.height / 2)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryTotals.forEachIndexed { index, category ->
                    val percentage = (category.total / total * 100).toInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(pieColors[index % pieColors.size])
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "R${String.format(Locale.getDefault(), "%.2f", category.total)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BudgetBruAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "($percentage%)",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTotalRow(total: ExpenseEntryDao.CategorySpending, totalSpent: Double) {
    val percentage = if (totalSpent > 0) (total.total / totalSpent * 100).toInt() else 0
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "progressBar"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    total.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = "R${String.format(Locale.getDefault(), "%.2f", total.total)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = BudgetBruAccent
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$percentage% of total",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    percentage > 50 -> BudgetBruAccent
                    percentage > 25 -> BudgetBruPrimary
                    else -> BudgetBruSecondary
                },
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun ReportExpenseCard(expense: ExpenseEntry, dateFormat: SimpleDateFormat) {
    var showPhotoDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (expense.photoPath != null && expense.photoPath!!.isNotEmpty()) showPhotoDialog = true },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.description, fontWeight = FontWeight.Medium)
                Text(
                    text = "${dateFormat.format(expense.date)} • ${expense.startTime} - ${expense.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (expense.photoPath != null && expense.photoPath!!.isNotEmpty()) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Has Photo",
                        tint = BudgetBruPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "R${String.format(Locale.getDefault(), "%.2f", expense.amount)}",
                    color = BudgetBruAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    if (showPhotoDialog && expense.photoPath != null && expense.photoPath!!.isNotEmpty()) {
        Dialog(onDismissRequest = { showPhotoDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Receipt Photo - ${expense.description}",
                        style = MaterialTheme.typography.titleMedium,
                        color = BudgetBruPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Image(
                        painter = rememberAsyncImagePainter(expense.photoPath),
                        contentDescription = "Receipt",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showPhotoDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = BudgetBruPrimary)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun ReportEmptyStateCard(title: String, message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = BudgetBruPrimary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }
    }
}