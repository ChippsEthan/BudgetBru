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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    repository: BudgetRepository
) {
    val viewModel: GoalsViewModel = viewModel(factory = GoalsViewModelFactory(repository))

    val currentIncome by viewModel.currentIncome.collectAsState()
    val currentMin by viewModel.currentMin.collectAsState()
    val currentMax by viewModel.currentMax.collectAsState()
    val currentMonthTotal by viewModel.currentMonthTotal.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showSuccessMessage by viewModel.showSuccessMessage.collectAsState()
    val successMessageText by viewModel.successMessageText.collectAsState()

    // Dialog state variables
    var showSetGoalsDialog by remember { mutableStateOf(false) }
    var showSetIncomeDialog by remember { mutableStateOf(false) }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA")).apply {
        currency = java.util.Currency.getInstance("ZAR")
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
                onClick = { showSetGoalsDialog = true },
                containerColor = BudgetBruPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Goals", tint = Color.White)
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
            // Success Message
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(BudgetBruPrimary, BudgetBruSecondary, Color(0xFF6B21A5))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "🎯 Financial Goals",
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

            // Current Goals Card
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
                        Text("Current Goals", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BudgetBruPrimary)
                        TextButton(
                            onClick = { showSetGoalsDialog = true }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", color = BudgetBruPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentMin == null && currentMax == null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BudgetBruSecondary.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "No goals set. Tap the edit button to set your monthly spending goals.",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
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

            // Monthly Income Card
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
                        Text("Monthly Income", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BudgetBruSecondary)
                        TextButton(
                            onClick = { showSetIncomeDialog = true }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", color = BudgetBruSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        currencyFormat.format(currentIncome),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = BudgetBruSecondary
                    )
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
                            DailyStatBox(
                                title = "Daily Budget",
                                value = currencyFormat.format((currentMax ?: 0.0) / daysInMonth),
                                color = BudgetBruPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            DailyStatBox(
                                title = "Avg Daily Spent",
                                value = currencyFormat.format(currentMonthTotal / maxOf(1, currentDay)),
                                color = BudgetBruAccent,
                                modifier = Modifier.weight(1f)
                            )
                            DailyStatBox(
                                title = "Days Left",
                                value = "$daysLeft",
                                color = BudgetBruSecondary,
                                modifier = Modifier.weight(1f)
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
                                StatusBox(
                                    message = "⚠️ Overspent by ${currencyFormat.format(overspent)}",
                                    color = BudgetBruAccent,
                                    icon = Icons.Default.Warning
                                )
                            }
                            isUnderMinGoal -> {
                                val under = currentMin!! - currentMonthTotal
                                StatusBox(
                                    message = "✅ Under budget by ${currencyFormat.format(under)}",
                                    color = BudgetBruPrimary,
                                    icon = Icons.Default.CheckCircle
                                )
                            }
                            remainingBudget < currentIncome * 0.1 && remainingBudget > 0 -> {
                                StatusBox(
                                    message = "⚠️ Only 10% of income remaining. Spend wisely!",
                                    color = Color(0xFFFFA726),
                                    icon = Icons.Default.Info
                                )
                            }
                            savingsRate > 20 -> {
                                StatusBox(
                                    message = "🎉 Amazing! You're saving ${savingsRate.toInt()}% of your income!",
                                    color = Color(0xFF4CAF50),
                                    icon = Icons.Default.Star
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Set Goals Dialog
    if (showSetGoalsDialog) {
        var tempMin by remember { mutableStateOf(currentMin?.toString() ?: "") }
        var tempMax by remember { mutableStateOf(currentMax?.toString() ?: "") }

        AlertDialog(
            onDismissRequest = { showSetGoalsDialog = false },
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
                            viewModel.saveGoals(min, max)
                            showSetGoalsDialog = false
                        }
                    }
                ) {
                    Text("Save", color = BudgetBruSecondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetGoalsDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkCard
        )
    }

    // Set Income Dialog
    if (showSetIncomeDialog) {
        var tempIncome by remember { mutableStateOf(currentIncome.toString()) }

        AlertDialog(
            onDismissRequest = { showSetIncomeDialog = false },
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
                            viewModel.saveIncome(income)
                            showSetIncomeDialog = false
                        }
                    }
                ) {
                    Text("Save", color = BudgetBruPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetIncomeDialog = false }) {
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
fun DailyStatBox(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(4.dp))
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
fun StatusBox(message: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
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