package com.example.budgetbruprog7313.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.budgetbruprog7313.ui.theme.*
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
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

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Load IOUs from SharedPreferences
    LaunchedEffect(Unit) {
        val gson = Gson()
        val json = sharedPrefs.getString("ious", null)
        if (json != null) {
            val type = object : TypeToken<List<IOUEntry>>() {}.type
            ious = gson.fromJson(json, type)
        } else {
            // Sample data for first run
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(BudgetBruPrimary, BudgetBruSecondary)
                    ),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "🤝 IOU Tracker",
                    fontSize = 28.sp,
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

        // Stats Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatSummaryCard(
                title = "You Lent",
                amount = totalLent,
                color = BudgetBruPrimary,
                icon = Icons.Default.ArrowUpward,
                modifier = Modifier.weight(1f)
            )
            StatSummaryCard(
                title = "You Borrowed",
                amount = totalBorrowed,
                color = BudgetBruAccent,
                icon = Icons.Default.ArrowDownward,
                modifier = Modifier.weight(1f)
            )
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
            edgePadding = 16.dp
        ) {
            listOf("all", "lent", "borrowed").forEachIndexed { index, type ->
                FilterChip(
                    selected = filter == type,
                    onClick = { filter = type },
                    label = {
                        Text(
                            when (type) {
                                "all" -> "All"
                                "lent" -> "You Lent"
                                "borrowed" -> "You Borrowed"
                                else -> ""
                            }
                        )
                    },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BudgetBruPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Add Button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BudgetBruPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New IOU")
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
                        modifier = Modifier.size(64.dp),
                        tint = BudgetBruPrimary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No IOUs yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tap + to add money you lent or borrowed",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredIOUs) { iou ->
                    IOUCard(
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
        AddIOUDialog(
            onAdd = { newIOU ->
                ious = listOf(newIOU) + ious
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // IOU Detail Dialog
    if (selectedIOU != null) {
        IOUDetailDialog(
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

private fun saveIOUs(sharedPrefs: SharedPreferences, ious: List<IOUEntry>) {
    val gson = Gson()
    val json = gson.toJson(ious)
    sharedPrefs.edit().putString("ious", json).apply()
}

@Composable
fun StatSummaryCard(title: String, amount: Double, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            Text(
                "R${String.format("%.2f", amount)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun IOUCard(iou: IOUEntry, onSettle: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit, dateFormat: SimpleDateFormat) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (iou.isSettled) DarkCard.copy(alpha = 0.6f) else DarkCard
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = (if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent).copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (iou.type == "lent") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (iou.type == "lent") "Lent to ${iou.personName}" else "Borrowed from ${iou.personName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent
                    )
                    if (iou.isSettled) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BudgetBruPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "Settled",
                                fontSize = 10.sp,
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
                    maxLines = 1
                )
                Text(
                    dateFormat.format(iou.date),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Text(
                "R${String.format("%.2f", iou.amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent
            )
        }
    }
}

@Composable
fun AddIOUDialog(onAdd: (IOUEntry) -> Unit, onDismiss: () -> Unit) {
    var personName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("lent") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add IOU",
                fontWeight = FontWeight.Bold,
                color = BudgetBruPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "lent",
                        onClick = { type = "lent" },
                        label = { Text("I Lent Money") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BudgetBruPrimary
                        )
                    )
                    FilterChip(
                        selected = type == "borrowed",
                        onClick = { type = "borrowed" },
                        label = { Text("I Borrowed Money") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BudgetBruAccent
                        )
                    )
                }

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text(if (type == "lent") "Who borrowed from you?" else "Who did you borrow from?") },
                    placeholder = { Text("Friend's name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (R)") },
                    placeholder = { Text("e.g., 150.00") },
                    singleLine = true,
                    leadingIcon = { Text("R", fontWeight = FontWeight.Bold) }
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    placeholder = { Text("e.g., Lunch, Movie tickets, Rent") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
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
                }
            ) {
                Text("Add", color = BudgetBruPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = DarkCard
    )
}

@Composable
fun IOUDetailDialog(iou: IOUEntry, onDismiss: () -> Unit, onSettle: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (iou.type == "lent") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent
                )
                Text(
                    if (iou.type == "lent") "Money Lent" else "Money Borrowed",
                    fontWeight = FontWeight.Bold,
                    color = if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("Person", iou.personName, if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent)
                InfoRow("Amount", "R${String.format("%.2f", iou.amount)}", if (iou.type == "lent") BudgetBruPrimary else BudgetBruAccent)
                InfoRow("Date", dateFormat.format(iou.date), Color.White.copy(alpha = 0.7f))
                InfoRow("Reason", iou.reason, Color.White.copy(alpha = 0.7f))

                if (iou.isSettled) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = BudgetBruPrimary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BudgetBruPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("This IOU has been settled", color = BudgetBruPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!iou.isSettled) {
                    TextButton(
                        onClick = onSettle,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Settled")
                    }
                }
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(contentColor = BudgetBruAccent)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        containerColor = DarkCard
    )
}

@Composable
fun InfoRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

private fun getSampleIOUs(): List<IOUEntry> {
    val calendar = Calendar.getInstance()
    return listOf(
        IOUEntry(
            id = 1,
            personName = "John",
            amount = 150.0,
            type = "lent",
            reason = "Lunch",
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -2) }.time,
            isSettled = false
        ),
        IOUEntry(
            id = 2,
            personName = "Sarah",
            amount = 50.0,
            type = "borrowed",
            reason = "Movie ticket",
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
    val type: String, // "lent" or "borrowed"
    val reason: String,
    val date: Date,
    val isSettled: Boolean
)