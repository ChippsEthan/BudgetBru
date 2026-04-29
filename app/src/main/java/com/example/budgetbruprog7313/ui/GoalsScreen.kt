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
            }

            repository.getGoals().collect { settings ->
                currentMin = settings?.minMonthlyGoal
                currentMax = settings?.maxMonthlyGoal
                // Update input fields if goals exist
                if (settings?.minMonthlyGoal != null) {
                    minGoal = settings.minMonthlyGoal.toString()
                }
                if (settings?.maxMonthlyGoal != null) {
                    maxGoal = settings.maxMonthlyGoal.toString()
                }
                isLoading = false
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
                Snackbar(
                    modifier = Modifier.fillMaxWidth(),
                    action = {
                        TextButton(onClick = { saveMessage = null }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(message)
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

            // Progress Section (same as before - kept for brevity)
            if (currentMax != null && currentMax!! > 0) {
                // ... (keep existing progress section)
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
            }
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
            }
        )
    }
}