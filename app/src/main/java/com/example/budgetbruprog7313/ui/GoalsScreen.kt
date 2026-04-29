package com.example.budgetbruprog7313.ui

import androidx.compose.animation.*
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
import kotlinx.coroutines.delay
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
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessageText by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA")).apply {
        currency = java.util.Currency.getInstance("ZAR")
    }

    // Auto-hide success message
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            delay(2000)
            showSuccessMessage = false
        }
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
    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysLeft = daysInMonth - currentDay
    val dailyBudgetRemaining = if (daysLeft > 0 && remainingBudget > 0) remainingBudget / daysLeft else 0.0

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showIncomeDialog = true },
                containerColor = BudgetBruPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.AttachMoney, contentDescription = "Set Income", tint = Color.White)
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
            // Animated Success Message
            AnimatedVisibility(
                visible = showSuccessMessage,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BudgetBruPrimary.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = BudgetBruPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(successMessageText, color = BudgetBruPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    BudgetBruPrimary,
                                    BudgetBruSecondary,
                                    Color(0xFF6B21A5)
                                )
                            )
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
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = BudgetBruPrimary.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Wallet",
                                        tint = BudgetBruPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Monthly Income", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("After tax", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                            }
                        }
                        IconButton(
                            onClick = { showIncomeDialog = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BudgetBruPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        currencyFormat.format(currentIncome),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = BudgetBruPrimary
                    )
                }
            }

            // Goals Card
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
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = BudgetBruSecondary.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.TrackChanges,
                                        contentDescription = "Goals",
                                        tint = BudgetBruSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Spending Goals", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Monthly limits", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                            }
                        }
                        IconButton(
                            onClick = { showGoalsDialog = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Edit", tint = BudgetBruSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentMin == null && currentMax == null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BudgetBruSecondary.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = BudgetBruSecondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "No goals set yet. Tap the edit icon to set your monthly spending goals.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GoalCard(
                                title = "Minimum Goal",
                                amount = currentMin ?: 0.0,
                                color = BudgetBruSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            GoalCard(
                                title = "Maximum Goal",
                                amount = currentMax ?: 0.0,
                                color = BudgetBruPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Daily Budget Card
            if (currentMax != null && currentMax!! > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BudgetBruPrimary.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = BudgetBruPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Daily Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DailyStatCard(
                                title = "Daily Budget",
                                value = currencyFormat.format((currentMax ?: 0.0) / daysInMonth),
                                icon = Icons.Default.Today,
                                color = BudgetBruPrimary
                            )
                            DailyStatCard(
                                title = "Avg Daily Spent",
                                value = currencyFormat.format(currentMonthTotal / maxOf(1, currentDay)),
                                icon = Icons.Default.TrendingUp,
                                color = BudgetBruAccent
                            )
                            DailyStatCard(
                                title = "Days Left",
                                value = "$daysLeft",
                                icon = Icons.Default.DateRange,
                                color = BudgetBruSecondary
                            )
                        }

                        if (dailyBudgetRemaining > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Remaining per day:", fontSize = 13.sp)
                                    Text(
                                        currencyFormat.format(dailyBudgetRemaining),
                                        fontWeight = FontWeight.Bold,
                                        color = BudgetBruPrimary,
                                        fontSize = 16.sp
                                    )
                                }
                            }
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
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "${(progressToMaxGoal * 100).toInt()}% of your monthly goal",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

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

                        when {
                            isOverBudget -> {
                                val overspent = currentMonthTotal - currentMax!!
                                StatusCard(
                                    message = "⚠️ Overspent by ${currencyFormat.format(overspent)}",
                                    color = BudgetBruAccent,
                                    icon = Icons.Default.Warning
                                )
                            }
                            isUnderMinGoal -> {
                                val under = currentMin!! - currentMonthTotal
                                StatusCard(
                                    message = "✅ Under budget by ${currencyFormat.format(under)}",
                                    color = BudgetBruPrimary,
                                    icon = Icons.Default.CheckCircle
                                )
                            }
                            remainingBudget < currentIncome * 0.1 && remainingBudget > 0 -> {
                                StatusCard(
                                    message = "⚠️ Only 10% of income remaining. Spend wisely!",
                                    color = Color(0xFFFFA726),
                                    icon = Icons.Default.Info
                                )
                            }
                            savingsRate > 20 -> {
                                StatusCard(
                                    message = "🎉 Amazing! You're saving ${savingsRate.toInt()}% of your income!",
                                    color = Color(0xFF4CAF50),
                                    icon = Icons.Default.Star
                                )
                            }
                        }
                    }
                }
            }

            // Reset Button
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = BudgetBruAccent
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Reset")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Settings")
            }
        }
    }

    // Income Dialog
    if (showIncomeDialog) {
        var tempIncome by remember { mutableStateOf(currentIncome.toString()) }

        AlertDialog(
            onDismissRequest = { showIncomeDialog = false },
            title = {
                Text(
                    "Set Monthly Income",
                    fontWeight = FontWeight.Bold,
                    color = BudgetBruPrimary
                )
            },
            text = {
                Column {
                    Text("Enter your monthly income after tax:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempIncome,
                        onValueChange = { tempIncome = it },
                        label = { Text("Income Amount (R)") },
                        placeholder = { Text("e.g., 5000") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Money") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val income = tempIncome.toDoubleOrNull()
                        if (income != null && income > 0) {
                            scope.launch {
                                repository.saveMonthlyIncome(income)
                                currentIncome = income
                                successMessageText = "Income updated to ${currencyFormat.format(income)}!"
                                showSuccessMessage = true
                                showIncomeDialog = false
                            }
                        }
                    }
                ) {
                    Text("Save", color = BudgetBruPrimary)
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
            title = {
                Text(
                    "Set Spending Goals",
                    fontWeight = FontWeight.Bold,
                    color = BudgetBruSecondary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Set your monthly spending limits:", fontSize = 13.sp)
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
                                repository.saveGoals(min, max)
                                currentMin = min
                                currentMax = max
                                successMessageText = "Goals saved: Min ${currencyFormat.format(min)}, Max ${currencyFormat.format(max)}"
                                showSuccessMessage = true
                                showGoalsDialog = false
                            }
                        }
                    }
                ) {
                    Text("Save", color = BudgetBruSecondary)
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

    // Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    "Reset All Settings?",
                    fontWeight = FontWeight.Bold,
                    color = BudgetBruAccent
                )
            },
            text = {
                Text("This will clear your income and spending goals. Your expenses will not be affected.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.saveMonthlyIncome(5000.0)
                            repository.saveGoals(0.0, 0.0)
                            currentIncome = 5000.0
                            currentMin = null
                            currentMax = null
                            successMessageText = "All settings have been reset!"
                            showSuccessMessage = true
                            showResetDialog = false
                        }
                    }
                ) {
                    Text("Reset", color = BudgetBruAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkCard
        )
    }
}

@Composable
fun GoalCard(title: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 12.sp, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "R${String.format("%.2f", amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun DailyStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
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

@Composable
fun StatusCard(message: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = message, tint = color)
            Spacer(modifier = Modifier.width(8.dp))
            Text(message, color = color, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }
    }
}