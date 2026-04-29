package com.example.budgetbruprog7313.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import com.example.budgetbruprog7313.data.database.AppDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen() {
    val context = LocalContext.current
    val repository = remember {
        BudgetRepository(AppDatabase.getDatabase(context))
    }
    val scope = rememberCoroutineScope()

    // State variables
    var monthlyIncome by remember { mutableStateOf("") }
    var currentIncome by remember { mutableStateOf(5000.0) }
    var minGoal by remember { mutableStateOf("") }
    var maxGoal by remember { mutableStateOf("") }
    var currentMin by remember { mutableStateOf<Double?>(null) }
    var currentMax by remember { mutableStateOf<Double?>(null) }
    var currentMonthTotal by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showGoalsDialog by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Helper to format currency
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA")).apply {
        currency = java.util.Currency.getInstance("ZAR")
    }

    fun getCurrentMonthRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val start = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val end = calendar.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        return Pair(start, end)
    }

    // Load all data
    LaunchedEffect(Unit) {
        try {
            val (start, end) = getCurrentMonthRange()
            repository.getEntriesBetweenDates(start, end).collect { entries ->
                currentMonthTotal = entries.sumOf { it.amount }
                isLoading = false
            }

            repository.getGoals().collect { settings ->
                currentMin = settings?.minMonthlyGoal
                currentMax = settings?.maxMonthlyGoal
                if (settings?.minMonthlyGoal != null) {
                    minGoal = settings.minMonthlyGoal.toString()
                }
                if (settings?.maxMonthlyGoal != null) {
                    maxGoal = settings.maxMonthlyGoal.toString()
                }
            }

            repository.getMonthlyIncome().collect { income ->
                currentIncome = income ?: 5000.0
            }
        } catch (e: Exception) {
            isLoading = false
        }
    }

    // Calculate metrics
    val remainingBudget = currentIncome - currentMonthTotal
    val savingsRate = if (currentIncome > 0) (remainingBudget / currentIncome) * 100 else 0.0
    val progressToMaxGoal = if (currentMax != null && currentMax!! > 0) {
        (currentMonthTotal / currentMax!!).toFloat().coerceIn(0f, 1f)
    } else 0f
    val isOverBudget = currentMax != null && currentMonthTotal > currentMax!!
    val isUnderMinGoal = currentMin != null && currentMonthTotal < currentMin!!

    // Calculate days in current month
    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showIncomeDialog = true },
                containerColor = BudgetBruPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.AttachMoney, contentDescription = "Set Income")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(BudgetBruPrimary, BudgetBruSecondary)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "Financial Goals",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Track your spending and savings",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Save Message
            saveMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("✅"))
                            BudgetBruPrimary.copy(alpha = 0.2f)
                        else
                            BudgetBruAccent.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        message,
                        modifier = Modifier.padding(12.dp),
                        color = if (message.contains("✅")) BudgetBruPrimary else BudgetBruAccent
                    )
                }
            }

            // Income Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = BudgetBruPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Monthly Income", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        IconButton(
                            onClick = { showIncomeDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BudgetBruPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        currencyFormat.format(currentIncome),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = BudgetBruPrimary
                    )
                }
            }

            // Spending Goals Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrackChanges, contentDescription = "Goals", tint = BudgetBruSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Spending Goals", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        IconButton(
                            onClick = { showGoalsDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = BudgetBruSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentMin == null && currentMax == null) {
                        Text(
                            "No goals set yet. Tap the settings icon to set your monthly spending goals.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Minimum", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    currencyFormat.format(currentMin ?: 0.0),
                                    fontWeight = FontWeight.Bold,
                                    color = BudgetBruSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Maximum", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    currencyFormat.format(currentMax ?: 0.0),
                                    fontWeight = FontWeight.Bold,
                                    color = BudgetBruSecondary
                                )
                            }
                        }
                    }
                }
            }

            // ===== BUDGET SUMMARY CARD =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BudgetBruPrimary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.PieChart,
                            contentDescription = "Summary",
                            tint = BudgetBruPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Budget Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = BudgetBruPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Daily Budget",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                "R${String.format("%.2f", (currentMax ?: 0.0) / daysInMonth)}",
                                fontWeight = FontWeight.Bold,
                                color = BudgetBruPrimary,
                                fontSize = 14.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Weekly Budget",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                "R${String.format("%.2f", (currentMax ?: 0.0) / 4)}",
                                fontWeight = FontWeight.Bold,
                                color = BudgetBruSecondary,
                                fontSize = 14.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Avg Daily Spent",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                "R${String.format("%.2f", if (currentDay > 0) currentMonthTotal / currentDay else 0.0)}",
                                fontWeight = FontWeight.Bold,
                                color = if ((if (currentDay > 0) currentMonthTotal / currentDay else 0.0) > ((currentMax ?: 0.0) / daysInMonth))
                                    BudgetBruAccent
                                else
                                    BudgetBruPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Daily comparison indicator
                    Spacer(modifier = Modifier.height(8.dp))
                    val avgDailySpent = if (currentDay > 0) currentMonthTotal / currentDay else 0.0
                    val dailyBudget = (currentMax ?: 0.0) / daysInMonth
                    if (dailyBudget > 0) {
                        val percentage = (avgDailySpent / dailyBudget * 100).toInt().coerceIn(0, 200)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Daily spend vs budget:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            LinearProgressIndicator(
                                progress = (percentage / 100f).coerceIn(0f, 1f),
                                modifier = Modifier.weight(1f).height(4.dp),
                                color = if (avgDailySpent <= dailyBudget) BudgetBruPrimary else BudgetBruAccent
                            )
                            Text(
                                "$percentage%",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (avgDailySpent <= dailyBudget) BudgetBruPrimary else BudgetBruAccent
                            )
                        }
                    }
                }
            }

            // Progress Section
            if (currentMax != null && currentMax!! > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOverBudget)
                            BudgetBruAccent.copy(alpha = 0.15f)
                        else DarkCard
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("📊 Monthly Progress", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Spent: ${currencyFormat.format(currentMonthTotal)}")
                                Text("Goal: ${currencyFormat.format(currentMax)}")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = progressToMaxGoal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = when {
                                    isOverBudget -> BudgetBruAccent
                                    progressToMaxGoal > 0.8f -> Color(0xFFFFA726)
                                    else -> BudgetBruPrimary
                                },
                                trackColor = DarkCard
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatBox(
                                title = "Remaining",
                                value = currencyFormat.format(remainingBudget),
                                color = if (remainingBudget > 0) BudgetBruPrimary else BudgetBruAccent,
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                title = "Savings Rate",
                                value = "${savingsRate.toInt()}%",
                                color = BudgetBruSecondary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status messages
                        when {
                            isOverBudget -> {
                                val overspent = currentMonthTotal - currentMax!!
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = BudgetBruAccent.copy(alpha = 0.2f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = BudgetBruAccent)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "⚠️ Overspent by ${currencyFormat.format(overspent)}",
                                            color = BudgetBruAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            isUnderMinGoal -> {
                                val under = currentMin!! - currentMonthTotal
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = BudgetBruPrimary.copy(alpha = 0.2f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = BudgetBruPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "✅ Under budget by ${currencyFormat.format(under)}",
                                            color = BudgetBruPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            currentMonthTotal > 0 && remainingBudget < (currentIncome * 0.1) -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFA726).copy(alpha = 0.2f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFFFFA726))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "⚠️ Only 10% of income remaining",
                                            color = Color(0xFFFFA726),
                                            fontWeight = FontWeight.Bold
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

    // Income Dialog
    if (showIncomeDialog) {
        AlertDialog(
            onDismissRequest = { showIncomeDialog = false },
            title = { Text("Set Monthly Income") },
            text = {
                OutlinedTextField(
                    value = monthlyIncome,
                    onValueChange = { monthlyIncome = it },
                    label = { Text("Income Amount (R)") },
                    placeholder = { Text("e.g., 5000") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Money") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val income = monthlyIncome.toDoubleOrNull()
                        if (income != null && income > 0) {
                            scope.launch {
                                isSaving = true
                                try {
                                    repository.saveMonthlyIncome(income)
                                    currentIncome = income
                                    saveMessage = "✅ Income updated successfully!"
                                    monthlyIncome = ""
                                    showIncomeDialog = false
                                } catch (e: Exception) {
                                    saveMessage = "❌ Error: ${e.message}"
                                } finally {
                                    isSaving = false
                                }
                            }
                        } else {
                            saveMessage = "❌ Please enter a valid income amount"
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Save", color = BudgetBruPrimary)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showIncomeDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkCard
        )
    }

    // Goals Dialog
    if (showGoalsDialog) {
        var tempMin by remember { mutableStateOf(currentMin?.toString() ?: "") }
        var tempMax by remember { mutableStateOf(currentMax?.toString() ?: "") }

        AlertDialog(
            onDismissRequest = { showGoalsDialog = false },
            title = { Text("Set Spending Goals") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempMin,
                        onValueChange = { tempMin = it },
                        label = { Text("Minimum Goal (R)") },
                        placeholder = { Text("e.g., 500") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempMax,
                        onValueChange = { tempMax = it },
                        label = { Text("Maximum Goal (R)") },
                        placeholder = { Text("e.g., 2000") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val min = tempMin.toDoubleOrNull()
                        val max = tempMax.toDoubleOrNull()
                        if (min != null && max != null && min <= max && min > 0 && max > 0) {
                            scope.launch {
                                isSaving = true
                                try {
                                    repository.saveGoals(min, max)
                                    currentMin = min
                                    currentMax = max
                                    saveMessage = "✅ Goals saved successfully!"
                                    showGoalsDialog = false
                                } catch (e: Exception) {
                                    saveMessage = "❌ Error: ${e.message}"
                                } finally {
                                    isSaving = false
                                }
                            }
                        } else {
                            saveMessage = "❌ Invalid: Min ≤ Max and both > 0"
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Save", color = BudgetBruPrimary)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalsDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkCard
        )
    }
}

@Composable
fun StatBox(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 16.sp)
        }
    }
}