package com.example.budgetbruprog7313.ui

import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgetbruprog7313.camera.CameraHelper
import com.example.budgetbruprog7313.data.model.ExpenseEntry
import com.example.budgetbruprog7313.data.model.Transaction
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ==================== INNOVATIVE FAB COMPONENT ====================

@Composable
fun InnovativeFAB(
    onAddExpense: () -> Unit,
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
                label = "Add Expense",
                icon = Icons.Default.EditNote,
                visible = isExpanded,
                delay = 0,
                onClick = {
                    isExpanded = false
                    onAddExpense()
                }
            )
        }

        Box(contentAlignment = Alignment.Center) {
            if (!isExpanded) {
                Box(
                    modifier = Modifier.size(56.dp).scale(pulseScale).clip(CircleShape).background(BudgetBruPrimary.copy(alpha = 0.3f))
                )
            }

            Box(
                modifier = Modifier.size(64.dp).shadow(12.dp, CircleShape).clip(CircleShape).background(
                    Brush.linearGradient(listOf(BudgetBruPrimary, BudgetBruSecondary))
                ).clickable { isExpanded = !isExpanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isExpanded) "Close" else "Add Expense",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp).rotate(rotation)
                )
            }
        }
    }
}

@Composable
fun FabSubItem(label: String, icon: ImageVector, visible: Boolean, delay: Int, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200, delay)) + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut(tween(100)) + slideOutVertically(targetOffsetY = { it / 2 })
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)), elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.padding(end = 12.dp)) {
                Text(label, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
            }
            Box(modifier = Modifier.size(48.dp).shadow(8.dp, CircleShape).clip(CircleShape).background(Color(0xFF2D2D44)).clickable { onClick() }, contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = BudgetBruPrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ==================== TIME PICKER COMPONENT ====================

@Composable
fun TimePickerField(
    label: String,
    time: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    OutlinedTextField(
        value = time,
        onValueChange = { /* Read-only, use time picker */ },
        label = { Text(label) },
        placeholder = { Text("HH:MM") },
        readOnly = true,
        trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = "Select Time") },
        modifier = modifier.clickable {
            val calendar = Calendar.getInstance()
            val currentHour = if (time.isNotBlank()) time.split(":")[0].toInt() else calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = if (time.isNotBlank() && time.split(":").size > 1) time.split(":")[1].toInt() else calendar.get(Calendar.MINUTE)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                    onTimeSelected(formattedTime)
                },
                currentHour,
                currentMinute,
                true
            ).show()
        },
        shape = RoundedCornerShape(12.dp)
    )
}

// ==================== MAIN HOME SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repository: BudgetRepository, onViewAllClick: () -> Unit = {}) {
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

    val animatedBalance by animateFloatAsState(targetValue = availableBalance.toFloat(), animationSpec = tween(1000, easing = FastOutSlowInEasing), label = "balance")

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
                        Text("$greeting, Bru!", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("BudgetBru", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BudgetBruPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground, scrolledContainerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            InnovativeFAB(
                onAddExpense = { showAddExpenseDialog = true },
                onScanReceipt = { showScanReceiptSnackbar = true },
                onAddIncome = { showAddIncomeDialog = true }
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().background(DarkBackground).verticalScroll(scrollState).padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Date Card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, "Date", tint = BudgetBruPrimary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(currentDate, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(shape = CircleShape, color = BudgetBruPrimary.copy(alpha = 0.2f), modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(SimpleDateFormat("dd", Locale.getDefault()).format(Date()), fontWeight = FontWeight.Bold, color = BudgetBruPrimary)
                        }
                    }
                }
            }

            // Balance Card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), elevation = CardDefaults.cardElevation(8.dp)) {
                Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BudgetBruPrimary, BudgetBruSecondary, Color(0xFF6B21A5)))).padding(24.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Available Balance", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f), letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("R ${String.format("%,.2f", animatedBalance)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            BalanceMiniStat("Total Spent", "R ${String.format("%,.2f", totalSpent)}", Color(0xFFFF6B6B))
                            BalanceMiniStat("Remaining", "R ${String.format("%,.2f", availableBalance)}", Color(0xFF4ECDC4))
                        }
                    }
                }
            }

            // Stats Row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickStatCard("Monthly Spent", "R ${String.format("%,.2f", totalSpent)}", Icons.AutoMirrored.Filled.TrendingDown, BudgetBruAccent, if (availableBalance + totalSpent > 0) (totalSpent / (availableBalance + totalSpent) * 100).toInt() else 0, Modifier.weight(1f))
                QuickStatCard("Monthly Budget", "R ${String.format("%,.2f", availableBalance + totalSpent)}", Icons.Default.AccountBalanceWallet, BudgetBruSecondary, 100, Modifier.weight(1f))
            }

            // Quick Add Section
            Text("Quick Add Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))

            // Amount chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                items(listOf(20.0, 50.0, 100.0, 200.0, 500.0)) { amount ->
                    FilterChip(selected = selectedQuickAmount == amount, onClick = { selectedQuickAmount = amount }, label = { Text("R${amount.toInt()}") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BudgetBruPrimary, selectedLabelColor = Color.White))
                }
            }

            // Category chips
            if (categories.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
                    items(categories.take(8)) { category ->
                        QuickAddChip(category.name, onClick = {
                            viewModel.addQuickExpense(selectedQuickAmount, "Quick ${category.name}", category.id)
                            scope.launch { snackbarHostState.showSnackbar("R${selectedQuickAmount.toInt()} added for ${category.name}") }
                        })
                    }
                }
            }

            // Recent Activity Header
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onViewAllClick) { Text("View All", color = BudgetBruPrimary) }
            }

            // Recent Activity List
            if (isLoading) {
                repeat(3) { ShimmerCard(); Spacer(Modifier.height(8.dp)) }
            } else if (recentActivity.isEmpty()) {
                Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = DarkCard), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, null, Modifier.size(64.dp), tint = BudgetBruPrimary.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("No activity yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Tap the + button to add expenses or income", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                recentActivity.forEachIndexed { index, transaction ->
                    AnimatedExpenseItem(index) {
                        TransactionCard(transaction, onDelete = {
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
                                    scope.launch { snackbarHostState.showSnackbar("Income deleted") }
                                }
                            }
                        })
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    // Delete Expense Dialog
    if (showDeleteConfirmation && expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false; expenseToDelete = null },
            title = { Text("Delete Expense", fontWeight = FontWeight.Bold, color = BudgetBruAccent) },
            text = { Text("Delete \"${expenseToDelete?.description}\" for R${String.format("%.2f", expenseToDelete?.amount ?: 0.0)}?") },
            confirmButton = {
                TextButton(onClick = {
                    expenseToDelete?.let { expense ->
                        scope.launch {
                            repository.deleteExpense(expense)
                            showDeleteConfirmation = false
                            expenseToDelete = null
                            viewModel.refresh()
                            snackbarHostState.showSnackbar("Expense deleted")
                        }
                    }
                }) { Text("Delete", color = BudgetBruAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false; expenseToDelete = null }) { Text("Cancel") }
            },
            containerColor = DarkCard
        )
    }

    // Add Expense Dialog with Time Picker and Camera
    if (showAddExpenseDialog) {
        var customAmount by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
        var startTime by remember { mutableStateOf("") }
        var endTime by remember { mutableStateOf("") }
        var photoPath by remember { mutableStateOf<String?>(null) }
        var showPhotoPreview by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (!success) {
                photoPath = null
            }
        }

        // Request camera permission
        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                scope.launch { snackbarHostState.showSnackbar("Camera permission is required to take photos") }
            }
        }

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("Add Expense", fontWeight = FontWeight.Bold, color = BudgetBruPrimary) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description *") },
                        placeholder = { Text("e.g., Lunch, Uber, Coffee") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = { customAmount = it },
                        label = { Text("Amount (R) *") },
                        placeholder = { Text("e.g., 99.99") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Text("R") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Time pickers
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimePickerField(
                            label = "Start Time",
                            time = startTime,
                            onTimeSelected = { startTime = it },
                            modifier = Modifier.weight(1f)
                        )
                        TimePickerField(
                            label = "End Time",
                            time = endTime,
                            onTimeSelected = { endTime = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Category *", fontSize = 13.sp, color = BudgetBruSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategoryId == category.id,
                                onClick = { selectedCategoryId = category.id },
                                label = { Text(category.name) }
                            )
                        }
                    }

                    // Photo Section
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Optional Photo", fontSize = 13.sp, color = BudgetBruSecondary)
                            Spacer(Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    val permission = android.Manifest.permission.CAMERA
                                    if (ContextCompat.checkSelfPermission(
                                            context, permission
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        val result = CameraHelper.createImageFile(context)
                                        if (result != null) {
                                            val (file, uri) = result
                                            photoPath = file.absolutePath
                                            cameraLauncher.launch(uri)
                                        } else {
                                            scope.launch { snackbarHostState.showSnackbar("Failed to create image file") }
                                        }
                                    } else {
                                        cameraPermissionLauncher.launch(permission)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BudgetBruSecondary)
                            ) {
                                Icon(Icons.Default.Camera, contentDescription = "Camera")
                                Spacer(Modifier.width(4.dp))
                                Text(if (photoPath != null) "Change Photo" else "Take Photo")
                            }

                            if (photoPath != null) {
                                Spacer(Modifier.height(8.dp))
                                Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).clickable { showPhotoPreview = true }.background(DarkBackground)) {
                                    val bitmap = File(photoPath!!).takeIf { it.exists() }?.let { BitmapFactory.decodeFile(it.absolutePath)?.asImageBitmap() }
                                    if (bitmap != null) {
                                        Image(bitmap = bitmap, contentDescription = "Receipt Photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amt = customAmount.toDoubleOrNull()
                        val finalStartTime = if (startTime.isBlank()) currentTime else startTime
                        val finalEndTime = if (endTime.isBlank()) currentTime else endTime

                        if (description.isNotBlank() && amt != null && selectedCategoryId != null && amt > 0) {
                            scope.launch {
                                val now = Date()
                                repository.addExpenseEntry(
                                    date = now,
                                    startTime = finalStartTime,
                                    endTime = finalEndTime,
                                    description = description,
                                    amount = amt,
                                    categoryId = selectedCategoryId!!,
                                    photoPath = photoPath
                                )
                                showAddExpenseDialog = false
                                viewModel.refresh()
                                snackbarHostState.showSnackbar("✅ Expense added!")
                            }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Please fill all required fields (*)") }
                        }
                    }
                ) { Text("Save", color = BudgetBruPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) { Text("Cancel") }
            },
            containerColor = DarkCard
        )

        // Photo Preview Dialog
        if (showPhotoPreview && photoPath != null) {
            Dialog(onDismissRequest = { showPhotoPreview = false }) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Receipt Photo", style = MaterialTheme.typography.titleMedium, color = BudgetBruPrimary)
                        Spacer(Modifier.height(12.dp))
                        val bitmap = File(photoPath!!).takeIf { it.exists() }?.let { BitmapFactory.decodeFile(it.absolutePath)?.asImageBitmap() }
                        if (bitmap != null) {
                            Image(bitmap = bitmap, contentDescription = "Receipt", modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { showPhotoPreview = false }) { Text("Close") }
                    }
                }
            }
        }
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
                    Text("Add money to your budget", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(incomeAmount, onValueChange = { incomeAmount = it }, label = { Text("Income Amount (R)") }, placeholder = { Text("e.g., 5000") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Text("R") }, shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(incomeDescription, onValueChange = { incomeDescription = it }, label = { Text("Description (Optional)") }, placeholder = { Text("e.g., Salary, Freelance, Gift") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = incomeAmount.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            scope.launch {
                                val desc = if (incomeDescription.isNotBlank()) incomeDescription else "Income Added"
                                viewModel.addIncome(amount, desc)
                                showAddIncomeDialog = false
                                snackbarHostState.showSnackbar("✅ R${String.format("%.2f", amount)} added!")
                            }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Please enter a valid amount") }
                        }
                    }
                ) { Text("Add Income", color = BudgetBruSecondary) }
            },
            dismissButton = {
                TextButton(onClick = { showAddIncomeDialog = false }) { Text("Cancel") }
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
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun QuickStatCard(title: String, value: String, icon: ImageVector, color: Color, percentage: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(110.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.bodySmall)
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
            LinearProgressIndicator(progress = { percentage / 100f }, modifier = Modifier.fillMaxWidth().height(4.dp), color = color, trackColor = Color.Gray.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun QuickAddChip(category: String, onClick: () -> Unit) {
    Card(modifier = Modifier.width(90.dp).height(70.dp).clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(
                when (category.lowercase()) {
                    "food" -> Icons.Default.Restaurant
                    "transport" -> Icons.Default.DirectionsCar
                    "groceries" -> Icons.Default.ShoppingCart
                    "fun" -> Icons.Default.MusicNote
                    "study" -> Icons.Default.School
                    "bills" -> Icons.Default.Receipt
                    else -> Icons.Default.Category
                }, null, tint = BudgetBruPrimary, modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(category, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun AnimatedExpenseItem(index: Int, content: @Composable () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 100L)
        isVisible = true
    }
    AnimatedVisibility(visible = isVisible, enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { it / 2 })) {
        content()
    }
}

@Composable
fun ShimmerCard() {
    Card(modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.size(48.dp)) {}
            Spacer(Modifier.width(12.dp))
            Column {
                Surface(modifier = Modifier.width(150.dp).height(16.dp), color = Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {}
                Spacer(Modifier.height(8.dp))
                Surface(modifier = Modifier.width(80.dp).height(12.dp), color = Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {}
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction, onDelete: () -> Unit) {
    var showDeleteButton by remember { mutableStateOf(false) }
    val isExpense = transaction is Transaction.Expense
    val icon = if (isExpense) Icons.Default.Receipt else Icons.Default.AttachMoney
    val iconColor = if (isExpense) BudgetBruAccent else Color(0xFF4FC3F7)
    val amountColor = if (isExpense) BudgetBruAccent else Color(0xFF4FC3F7)
    val amountPrefix = if (isExpense) "-R" else "+R"

    Card(modifier = Modifier.fillMaxWidth().clickable { showDeleteButton = !showDeleteButton }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = iconColor.copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(transaction.description, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White)
                    Text(SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()).format(transaction.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (isExpense && (transaction as Transaction.Expense).categoryName.isNotBlank()) {
                        Text((transaction as Transaction.Expense).categoryName, style = MaterialTheme.typography.labelSmall, color = BudgetBruSecondary)
                    }
                }
            }
            if (showDeleteButton) {
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = BudgetBruAccent) }
            } else {
                Text("$amountPrefix${String.format("%,.2f", transaction.amount)}", color = amountColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}