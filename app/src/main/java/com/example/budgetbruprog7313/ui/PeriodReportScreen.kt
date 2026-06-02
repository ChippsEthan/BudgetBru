package com.example.budgetbruprog7313.ui

import android.app.DatePickerDialog
import android.graphics.Paint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetbruprog7313.data.dao.ExpenseEntryDao
import com.example.budgetbruprog7313.data.model.ExpenseEntry
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.theme.BudgetBruAccent
import com.example.budgetbruprog7313.ui.theme.BudgetBruPrimary
import com.example.budgetbruprog7313.ui.theme.BudgetBruSecondary
import com.example.budgetbruprog7313.ui.theme.DarkBackground
import com.example.budgetbruprog7313.ui.theme.DarkCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

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
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Spending vs Goals") })
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
                2 -> {
                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (categoryTotals.isEmpty()) {
                        EmptyReportState("No spending data for this period", Icons.Default.PieChart)
                    } else {
                        // Fetch goals from repository
                        val goalsState by repository.getGoals().collectAsState(initial = null)
                        val minGoal = goalsState?.minMonthlyGoal
                        val maxGoal = goalsState?.maxMonthlyGoal

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                CategoryBarChart(
                                    categoryTotals = categoryTotals,
                                    minGoal = minGoal,
                                    maxGoal = maxGoal
                                )
                            }
                            // Optional: show total spent card
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
                                        Text(
                                            "R${String.format("%.2f", categoryTotals.sumOf { it.total })}",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BudgetBruAccent
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

// ==================== FIXED BAR CHART COMPONENT ====================

@Composable
fun CategoryBarChart(
    categoryTotals: List<ExpenseEntryDao.CategorySpending>,
    minGoal: Double?,
    maxGoal: Double?
) {
    if (categoryTotals.isEmpty()) return

    val colors = listOf(
        BudgetBruPrimary, BudgetBruSecondary, BudgetBruAccent,
        Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFFEF4444),
        Color(0xFF8B5CF6), Color(0xFF06B6D4), Color(0xFFF97316)
    )

    // Find max value for y-axis scaling (including goals if present)
    val maxSpent = categoryTotals.maxOfOrNull { it.total } ?: 0.0
    val yMax = listOf(maxSpent, maxGoal ?: 0.0, minGoal ?: 0.0).maxOrNull() ?: 1.0
    val yMaxRounded = (yMax * 1.1).coerceAtLeast(1.0) // add 10% headroom

    // Animate bars
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "barProgress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Spending per Category vs Goals",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BudgetBruPrimary
            )
            Spacer(Modifier.height(16.dp))

            // Chart Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                val barCount = categoryTotals.size
                val barSpacing = 8.dp.toPx()
                val availableWidth = size.width - 48.dp.toPx() // margins left+right
                val barWidth = (availableWidth - (barCount - 1) * barSpacing) / barCount
                var x = 24.dp.toPx() // left margin

                // Draw bars and labels
                categoryTotals.forEachIndexed { index, cat ->
                    val barHeight = (cat.total / yMaxRounded * (size.height - 60.dp.toPx())).toFloat()
                    val animatedHeight = barHeight * animatedProgress

                    // Bar rectangle with rounded corners (use drawRoundRect)
                    drawRoundRect(
                        color = colors[index % colors.size],
                        topLeft = Offset(x, size.height - 24.dp.toPx() - animatedHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, animatedHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Category label (abbreviated if needed)
                    val label = if (cat.name.length > 8) cat.name.take(6) + ".." else cat.name

                    // Draw text using drawIntoCanvas
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 11.dp.toPx()
                            textAlign = Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        canvas.nativeCanvas.drawText(
                            label,
                            x + barWidth / 2,
                            size.height - 8.dp.toPx(),
                            paint
                        )
                    }

                    x += barWidth + barSpacing
                }

                // Draw min goal line (green)
                if (minGoal != null && minGoal > 0) {
                    val yMin = size.height - 24.dp.toPx() - (minGoal / yMaxRounded * (size.height - 60.dp.toPx())).toFloat()
                    drawLine(
                        color = Color(0xFF4CAF50),
                        start = Offset(24.dp.toPx(), yMin),
                        end = Offset(size.width - 24.dp.toPx(), yMin),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            color = android.graphics.Color.parseColor("#4CAF50")
                            textSize = 11.dp.toPx()
                            textAlign = Paint.Align.LEFT
                            isAntiAlias = true
                        }
                        canvas.nativeCanvas.drawText(
                            "Min Goal: R${String.format("%.0f", minGoal)}",
                            24.dp.toPx(),
                            yMin - 5.dp.toPx(),
                            paint
                        )
                    }
                }

                // Draw max goal line (red)
                if (maxGoal != null && maxGoal > 0) {
                    val yMaxLine = size.height - 24.dp.toPx() - (maxGoal / yMaxRounded * (size.height - 60.dp.toPx())).toFloat()
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = Offset(24.dp.toPx(), yMaxLine),
                        end = Offset(size.width - 24.dp.toPx(), yMaxLine),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            color = android.graphics.Color.parseColor("#EF4444")
                            textSize = 11.dp.toPx()
                            textAlign = Paint.Align.LEFT
                            isAntiAlias = true
                        }
                        canvas.nativeCanvas.drawText(
                            "Max Goal: R${String.format("%.0f", maxGoal)}",
                            24.dp.toPx(),
                            yMaxLine - 5.dp.toPx(),
                            paint
                        )
                    }
                }
            }

            // Legend
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = Color(0xFF4CAF50), label = "Min Goal", isLine = true)
                LegendItem(color = Color(0xFFEF4444), label = "Max Goal", isLine = true)
                LegendItem(color = BudgetBruPrimary, label = "Spending", isLine = false)
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, isLine: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLine) {
            Canvas(modifier = Modifier.size(20.dp)) {
                drawLine(
                    color = color,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                )
            }
        } else {
            Box(
                Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
    }
}