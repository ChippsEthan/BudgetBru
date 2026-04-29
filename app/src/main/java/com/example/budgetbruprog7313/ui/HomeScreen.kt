package com.example.budgetbruprog7313.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgetbruprog7313.data.model.ExpenseEntry
import com.example.budgetbruprog7313.data.model.Transaction
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ==================== INNOVATIVE FAB COMPONENT ====================

@Composable
fun InnovativeFAB(
    onManualEntry: () -> Unit,
    onScanReceipt: () -> Unit,
    onAddIncome: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "rotation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 80.dp)
        ) {
            FabSubItem(
                label = "Scan Receipt",
                icon = Icons.Default.Camera,
                visible = isExpanded,
                delay = 100,
                onClick = {
                    isExpanded = false
                    onScanReceipt()
                }
            )
            FabSubItem(
                label = "Add Income",
                icon = Icons.Default.AttachMoney,
                visible = isExpanded,
                delay = 50,
                onClick = {
                    isExpanded = false
                    onAddIncome()
                }
            )
            FabSubItem(
                label = "Manual Entry",
                icon = Icons.Default.EditNote,
                visible = isExpanded,
                delay = 0,
                onClick = {
                    isExpanded = false
                    onManualEntry()
                }
            )
        }

        Box(contentAlignment = Alignment.Center) {
            if (!isExpanded) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(BudgetBruPrimary.copy(alpha = 0.3f))
                )
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(BudgetBruPrimary, BudgetBruSecondary)
                        )
                    )
                    .clickable { isExpanded = !isExpanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isExpanded) "Close" else "Add Expense",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

@Composable
fun FabSubItem(
    label: String,
    icon: ImageVector,
    visible: Boolean,
    delay: Int,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200, delay)) + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut(tween(100)) + slideOutVertically(targetOffsetY = { it / 2 })
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF2D2D44))
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = BudgetBruPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ==================== MAIN HOME SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: BudgetRepository,
    onViewAllClick: () -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
    val totalSpent by viewModel.totalSpent.collectAsState()
    val availableBalance by viewModel.availableBalance.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val recentActivity by viewModel.recentActivity.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var selectedQuickAmount by remember { mutableStateOf(50.0) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntry?>(null) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showScanReceiptSnackbar by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) }
    val currentDate = remember { dateFormatter.format(Date()) }
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    val animatedBalance by animateFloatAsState(
        targetValue = availableBalance.toFloat(),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "balance"
    )

    // Show snackbar for scan receipt
    LaunchedEffect(showScanReceiptSnackbar) {
        if (showScanReceiptSnackbar) {
            snackbarHostState.showSnackbar("📷 Receipt scanning coming soon!")
            showScanReceiptSnackbar = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "$greeting, Bru!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "BudgetBru",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BudgetBruPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    scrolledContainerColor = DarkBackground
                )
            )
        },
        floatingActionButton = {
            InnovativeFAB(
                onManualEntry = { showAddExpenseDialog = true },
                onScanReceipt = { showScanReceiptSnackbar = true },
                onAddIncome = { showAddIncomeDialog = true }
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Date Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = BudgetBruPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            currentDate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = BudgetBruPrimary.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                SimpleDateFormat("dd", Locale.getDefault()).format(Date()),
                                fontWeight = FontWeight.Bold,
                                color = BudgetBruPrimary
                            )
                        }
                    }
                }
            }

            // Balance Card with Gradient
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    BudgetBruPrimary,
                                    BudgetBruSecondary,
                                    Color(0xFF6B21A5)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Available Balance",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "R ${String.format("%,.2f", animatedBalance)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 48.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            BalanceMiniStat(
                                title = "Total Spent",
                                value = "R ${String.format("%,.2f", totalSpent)}",
                                color = Color(0xFFFF6B6B)
                            )
                            BalanceMiniStat(
                                title = "Remaining",
                                value = "R ${String.format("%,.2f", availableBalance)}",
                                color = Color(0xFF4ECDC4)
                            )
                        }
                    }
                }
            }

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatCard(
                    title = "Monthly Spent",
                    value = "R ${String.format("%,.2f", totalSpent)}",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = BudgetBruAccent,
                    percentage = if (availableBalance + totalSpent > 0)
                        (totalSpent / (availableBalance + totalSpent) * 100).toInt() else 0,
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Monthly Budget",
                    value = "R ${String.format("%,.2f", availableBalance + totalSpent)}",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = BudgetBruSecondary,
                    percentage = 100,
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Add Section
            Text(
                "Quick Add Expense",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Amount selector chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(listOf(20.0, 50.0, 100.0, 200.0, 500.0)) { amount ->
                    FilterChip(
                        selected = selectedQuickAmount == amount,
                        onClick = { selectedQuickAmount = amount },
                        label = { Text("R${amount.toInt()}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BudgetBruPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Category chips
            if (categories.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                ) {
                    items(categories.take(8)) { category ->
                        QuickAddChip(
                            category = category.name,
                            onClick = {
                                viewModel.addQuickExpense(selectedQuickAmount, "Quick ${category.name}", category.id)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "R${selectedQuickAmount.toInt()} added for ${category.name}"
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Recent Activity Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onViewAllClick) {
                    Text("View All", color = BudgetBruPrimary)
                }
            }

            // Recent Activity List (shows both expenses AND income)
            if (isLoading) {
                repeat(3) {
                    ShimmerCard()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else if (recentActivity.isEmpty()) {
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
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = BudgetBruPrimary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No activity yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap the + button to add expenses or income",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                recentActivity.forEachIndexed { index, transaction ->
                    AnimatedExpenseItem(index) {
                        TransactionCard(
                            transaction = transaction,
                            onDelete = {
                                when (transaction) {
                                    is Transaction.Expense -> {
                                        scope.launch {
                                            repository.deleteExpenseById(transaction.id)
                                            viewModel.refresh()
                                            snackbarHostState.showSnackbar("Expense deleted")
                                        }
                                    }
                                    is Transaction.Income -> {
                                        viewModel.deleteIncome(transaction.id)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Income deleted")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Delete Expense Confirmation Dialog
    if (showDeleteConfirmation && expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                expenseToDelete = null
            },
            title = {
                Text(
                    "Delete Expense",
                    fontWeight = FontWeight.Bold,
                    color = BudgetBruAccent
                )
            },
            text = {
                Text("Are you sure you want to delete \"${expenseToDelete?.description}\" for R${String.format("%.2f", expenseToDelete?.amount ?: 0.0)}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        expenseToDelete?.let { expense ->
                            scope.launch {
                                try {
                                    repository.deleteExpense(expense)
                                    showDeleteConfirmation = false
                                    expenseToDelete = null
                                    viewModel.refresh()
                                    snackbarHostState.showSnackbar("Expense deleted successfully")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error deleting expense: ${e.message}")
                                }
                            }
                        }
                    }
                ) {
                    Text("Delete", color = BudgetBruAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    expenseToDelete = null
                }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkCard,
            titleContentColor = BudgetBruAccent,
            textContentColor = Color.White
        )
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        var customAmount by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("Add Expense", fontWeight = FontWeight.Bold, color = BudgetBruPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = { customAmount = it },
                        label = { Text("Amount (R)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Text("R") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text("Category", fontSize = 13.sp, color = BudgetBruSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategoryId == category.id,
                                onClick = { selectedCategoryId = category.id },
                                label = { Text(category.name) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amt = customAmount.toDoubleOrNull()
                        if (description.isNotBlank() && amt != null && selectedCategoryId != null && amt > 0) {
                            scope.launch {
                                viewModel.addQuickExpense(amt, description, selectedCategoryId!!)
                                showAddExpenseDialog = false
                                snackbarHostState.showSnackbar("Expense added!")
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please fill all fields")
                            }
                        }
                    }
                ) {
                    Text("Save", color = BudgetBruPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkCard
        )
    }

    // Add Income Dialog
    if (showAddIncomeDialog) {
        var incomeAmount by remember { mutableStateOf("") }
        var incomeDescription by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddIncomeDialog = false },
            title = { Text("Add Income", fontWeight = FontWeight.Bold, color = BudgetBruSecondary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Add money to your budget",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = incomeAmount,
                        onValueChange = { incomeAmount = it },
                        label = { Text("Income Amount (R)") },
                        placeholder = { Text("e.g., 5000") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Text("R") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = incomeDescription,
                        onValueChange = { incomeDescription = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("e.g., Salary, Freelance, Gift") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = incomeAmount.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            scope.launch {
                                try {
                                    val description = if (incomeDescription.isNotBlank()) incomeDescription else "Income Added"
                                    viewModel.addIncome(amount, description)
                                    showAddIncomeDialog = false
                                    snackbarHostState.showSnackbar("✅ R${String.format("%.2f", amount)} added!")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                }
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please enter a valid amount")
                            }
                        }
                    }
                ) {
                    Text("Add Income", color = BudgetBruSecondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddIncomeDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkCard
        )
    }
}

// ==================== UI COMPONENTS ====================

@Composable
fun BalanceMiniStat(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    percentage: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.bodySmall)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = color,
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun QuickAddChip(category: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(90.dp)
            .height(70.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                when (category.lowercase()) {
                    "food" -> Icons.Default.Restaurant
                    "transport" -> Icons.Default.DirectionsCar
                    "groceries" -> Icons.Default.ShoppingCart
                    "fun" -> Icons.Default.MusicNote
                    "study" -> Icons.Default.School
                    "bills" -> Icons.Default.Receipt
                    else -> Icons.Default.Category
                },
                contentDescription = null,
                tint = BudgetBruPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AnimatedExpenseItem(index: Int, content: @Composable () -> Unit) {
    val delayMillis = index * 100L
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { it / 2 })
    ) {
        content()
    }
}

@Composable
fun ShimmerCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Gray.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {}
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Gray.copy(alpha = 0.2f),
                    modifier = Modifier.width(150.dp).height(16.dp)
                ) {}
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Gray.copy(alpha = 0.2f),
                    modifier = Modifier.width(100.dp).height(12.dp)
                ) {}
            }
        }
    }
}

// ==================== TRANSACTION CARD ====================

@Composable
fun TransactionCard(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    var showDeleteButton by remember { mutableStateOf(false) }

    val isExpense = transaction is Transaction.Expense
    val icon = if (isExpense) Icons.Default.Receipt else Icons.Default.AttachMoney
    val iconColor = if (isExpense) BudgetBruAccent else BudgetBruSecondary
    val amountColor = if (isExpense) BudgetBruAccent else BudgetBruSecondary
    val amountPrefix = if (isExpense) "-R" else "+R"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDeleteButton = !showDeleteButton },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.description,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()).format(transaction.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isExpense && (transaction as Transaction.Expense).categoryName.isNotBlank()) {
                        Text(
                            text = (transaction as Transaction.Expense).categoryName,
                            style = MaterialTheme.typography.labelSmall,
                            color = BudgetBruSecondary
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showDeleteButton,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally()
            ) {
                Row {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = BudgetBruAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Text(
                text = "$amountPrefix${String.format("%,.2f", transaction.amount)}",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}