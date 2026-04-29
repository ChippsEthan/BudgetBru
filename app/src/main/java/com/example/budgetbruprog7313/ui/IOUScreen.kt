package com.example.budgetbruprog7313.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.budgetbruprog7313.ui.theme.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IOUScreen() {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("iou_prefs", Context.MODE_PRIVATE)

    var ious by remember { mutableStateOf(listOf<IOUEntry>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedIOU by remember { mutableStateOf<IOUEntry?>(null) }
    var filter by remember { mutableStateOf("all") } // all, lent, borrowed
    var showStats by remember { mutableStateOf(true) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Load IOUs from SharedPreferences
    LaunchedEffect(Unit) {
        val gson = Gson()
        val json = sharedPrefs.getString("ious", null)
        if (json != null) {
            val type = object : TypeToken<List<IOUEntry>>() {}.type
            ious = gson.fromJson(json, type)
        } else {
            ious = getSampleIOUs()
            saveIOUs(sharedPrefs, ious)
        }
    }

    // Save IOUs whenever they change
    LaunchedEffect(ious) {
        saveIOUs(sharedPrefs, ious)
    }

    // Calculate totals
    val totalLent = ious.filter { it.type == "lent" && !it.isSettled }.sumOf { it.amount }
    val totalBorrowed = ious.filter { it.type == "borrowed" && !it.isSettled }.sumOf { it.amount }
    val settledCount = ious.count { it.isSettled }
    val activeCount = ious.count { !it.isSettled }

    // Animated values
    val animatedLent by animateFloatAsState(
        targetValue = totalLent.toFloat(),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "lent"
    )
    val animatedBorrowed by animateFloatAsState(
        targetValue = totalBorrowed.toFloat(),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "borrowed"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Animated Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(BudgetBruPrimary, BudgetBruSecondary, Color(0xFF6B21A5))
                    ),
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "🤝 IOU Tracker",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Track money owed to and from friends",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Stats Cards Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernStatCard(
                title = "You Lent",
                amount = animatedLent,
                color = BudgetBruPrimary,
                icon = Icons.Default.ArrowUpward,
                modifier = Modifier.weight(1f)
            )
            ModernStatCard(
                title = "You Borrowed",
                amount = animatedBorrowed,
                color = BudgetBruAccent,
                icon = Icons.Default.ArrowDownward,
                modifier = Modifier.weight(1f)
            )
        }

        // Summary Row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryChip(
                    label = "Active",
                    count = activeCount,
                    color = BudgetBruPrimary
                )
                SummaryChip(
                    label = "Settled",
                    count = settledCount,
                    color = BudgetBruSecondary
                )
                SummaryChip(
                    label = "Total",
                    count = ious.size,
                    color = BudgetBruAccent
                )
            }
        }

        // Filter Chips
        ScrollableTabRow(
            selectedTabIndex = when (filter) {
                "all" -> 0
                "lent" -> 1
                "borrowed" -> 2
                else -> 0
            },
            containerColor = DarkBackground,
            edgePadding = 16.dp,
            indicator = {}
        ) {
            listOf("all", "lent", "borrowed").forEachIndexed { index, type ->
                FilterChip(
                    selected = filter == type,
                    onClick = { filter = type },
                    label = {
                        Text(
                            when (type) {
                                "all" -> "All IOUs"
                                "lent" -> "You Lent"
                                "borrowed" -> "You Borrowed"
                                else -> ""
                            },
                            fontWeight = if (filter == type) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BudgetBruPrimary,
                        selectedLabelColor = Color.White,
                        disabledContainerColor = DarkCard,
                        disabledLabelColor = Color.White.copy(alpha = 0.5f)
                    )
                )
            }
        }

        // Add Button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BudgetBruPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New IOU", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        // IOU List
        val filteredIOUs = when (filter) {
            "lent" -> ious.filter { it.type == "lent" }
            "borrowed" -> ious.filter { it.type == "borrowed" }
            else -> ious
        }

        if (filteredIOUs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = BudgetBruPrimary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No IOUs yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tap the + button to add money you lent or borrowed",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredIOUs) { iou ->
                    ModernIOUCard(
                        iou = iou,
                        onSettle = {
                            ious = ious.map {
                                if (it.id == iou.id) it.copy(isSettled = true)
                                else it
                            }
                        },
                        onDelete = {
                            ious = ious.filter { it.id != iou.id }
                        },
                        onClick = { selectedIOU = iou },
                        dateFormat = dateFormat
                    )
                }
            }
        }
    }

    // Add IOU Dialog
    if (showAddDialog) {
        ModernAddIOUDialog(
            onAdd = { newIOU ->
                ious = listOf(newIOU) + ious
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // IOU Detail Dialog
    if (selectedIOU != null) {
        ModernIOUDetailDialog(
            iou = selectedIOU!!,
            onDismiss = { selectedIOU = null },
            onSettle = {
                ious = ious.map {
                    if (it.id == selectedIOU!!.id) it.copy(isSettled = true)
                    else it
                }
                selectedIOU = null
            },
            onDelete = {
                ious = ious.filter { it.id != selectedIOU!!.id }
                selectedIOU = null
            }
        )
    }
}

@Composable
fun ModernStatCard(title: String, amount: Float, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
            Text(
                "R${String.format("%,.2f", amount)}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun SummaryChip(label: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
        Text(
            "$count",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ModernIOUCard(iou: IOUEntry, onSettle: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit, dateFormat: SimpleDateFormat) {
    var showActions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showActions = !showActions },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (iou.isSettled) DarkCard.copy(alpha = 0.5f) else DarkCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = (if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent).copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            iou.personName.take(1).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            iou.personName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent
                        )
                        if (iou.isSettled) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BudgetBruPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "SETTLED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BudgetBruPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Text(
                        iou.reason,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        dateFormat.format(iou.date),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }

                // Amount
                Text(
                    "R${String.format("%,.2f", iou.amount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent
                )
            }

            // Action Buttons (appear on click)
            AnimatedVisibility(
                visible = showActions && !iou.isSettled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onSettle,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Settled")
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BudgetBruAccent
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun ModernAddIOUDialog(onAdd: (IOUEntry) -> Unit, onDismiss: () -> Unit) {
    var personName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("lent") }
    var reason by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf(0) }

    val presets = listOf(50.0, 100.0, 200.0, 500.0)

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Add New IOU",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = BudgetBruPrimary
                )

                // Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = type == "lent",
                        onClick = { type = "lent" },
                        label = { Text("💸 I Lent") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BudgetBruPrimary
                        )
                    )
                    FilterChip(
                        selected = type == "borrowed",
                        onClick = { type = "borrowed" },
                        label = { Text("💰 I Borrowed") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BudgetBruAccent
                        )
                    )
                }

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("Friend's Name") },
                    placeholder = { Text("e.g., John") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Amount with presets
                Column {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (R)") },
                        placeholder = { Text("e.g., 150.00") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Text("R", fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { amount = preset.toString() },
                                color = if (amount.toDoubleOrNull() == preset)
                                    BudgetBruPrimary.copy(alpha = 0.3f)
                                else DarkCard
                            ) {
                                Text(
                                    "R$preset",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    color = if (amount.toDoubleOrNull() == preset) BudgetBruPrimary else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    placeholder = { Text("e.g., Lunch, Movie, Rent") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (personName.isNotBlank() && amt != null && amt > 0 && reason.isNotBlank()) {
                                val newIOU = IOUEntry(
                                    id = System.currentTimeMillis(),
                                    personName = personName.trim(),
                                    amount = amt,
                                    type = type,
                                    reason = reason,
                                    date = Date(),
                                    isSettled = false
                                )
                                onAdd(newIOU)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == "lent") BudgetBruPrimary else BudgetBruAccent)
                    ) {
                        Text("Add IOU")
                    }
                }
            }
        }
    }
}

@Composable
fun ModernIOUDetailDialog(iou: IOUEntry, onDismiss: () -> Unit, onSettle: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = (if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent).copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                iou.personName.take(1).uppercase(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent
                            )
                        }
                    }
                    Column {
                        Text(
                            if (iou.type == "lent") "Lent to" else "Borrowed from",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            iou.personName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.1f))

                // Details Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailItem(
                        label = "Amount",
                        value = "R${String.format("%,.2f", iou.amount)}",
                        color = if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent,
                        modifier = Modifier.weight(1f)
                    )
                    DetailItem(
                        label = "Status",
                        value = if (iou.isSettled) "Settled" else "Pending",
                        color = if (iou.isSettled) BudgetBruPrimary else Color(0xFFFFA726),
                        modifier = Modifier.weight(1f)
                    )
                }

                DetailItem(
                    label = "Reason",
                    value = iou.reason,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )

                DetailItem(
                    label = "Date",
                    value = dateFormat.format(iou.date),
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!iou.isSettled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onSettle,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BudgetBruPrimary)
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Settled")
                        }
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = BudgetBruAccent
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete")
                        }
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

private fun saveIOUs(sharedPrefs: SharedPreferences, ious: List<IOUEntry>) {
    val gson = Gson()
    val json = gson.toJson(ious)
    sharedPrefs.edit().putString("ious", json).apply()
}

private fun getSampleIOUs(): List<IOUEntry> {
    val calendar = Calendar.getInstance()
    return listOf(
        IOUEntry(
            id = 1,
            personName = "Alex",
            amount = 150.0,
            type = "lent",
            reason = "Lunch & Coffee",
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -2) }.time,
            isSettled = false
        ),
        IOUEntry(
            id = 2,
            personName = "Sarah",
            amount = 50.0,
            type = "borrowed",
            reason = "Movie tickets",
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -5) }.time,
            isSettled = false
        ),
        IOUEntry(
            id = 3,
            personName = "Mike",
            amount = 200.0,
            type = "lent",
            reason = "Textbook",
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -10) }.time,
            isSettled = true
        )
    )
}

data class IOUEntry(
    val id: Long,
    val personName: String,
    val amount: Double,
    val type: String,
    val reason: String,
    val date: Date,
    val isSettled: Boolean
)