package com.example.budgetbruprog7313.ui

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.budgetbruprog7313.data.model.ExpenseEntry
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.theme.BudgetBruTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.budgetbruprog7313.data.database.AppDatabase
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class PeriodReportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetBruTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PeriodReportScreen()
                }
            }
        }
    }
}

@Composable
fun PeriodReportScreen() {
    val context = LocalContext.current
    val repository = remember {
        BudgetRepository(AppDatabase.getDatabase(context))
    }
    val scope = rememberCoroutineScope()

    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var entries by remember { mutableStateOf<List<ExpenseEntry>>(emptyList()) }
    var categoryTotals by remember { mutableStateOf<List<com.example.budgetbruprog7313.data.dao.ExpenseEntryDao.CategorySpending>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = entries, 1 = totals

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun loadData() {
        if (startDate != null && endDate != null) {
            scope.launch {
                // Convert Date to String format that matches stored date format
                // Assuming ExpenseEntry.date is a Date object – Dao uses BETWEEN on Date if stored as Long?
                // In current code, ExpenseEntry.date is Date type (kotlinx.datetime? Actually java.util.Date)
                // The existing query in ExpenseEntryDao.kt: @Query("SELECT * FROM expense_entries WHERE date BETWEEN :startDate AND :endDate")
                // It expects Date parameters. So we pass startDate and endDate directly.
                // For safety, we use startDate!! and endDate!! which are Date.
                repository.getEntriesBetweenDates(startDate!!, endDate!!).collect { entryList ->
                    entries = entryList
                }
                repository.getCategorySpending(startDate!!, endDate!!).collect { totals ->
                    categoryTotals = totals
                }
            }
        }
    }

    // Reload when dates change
    LaunchedEffect(startDate, endDate) {
        if (startDate != null && endDate != null) loadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Expense Reports", style = MaterialTheme.typography.headlineSmall)

        // Date range pickers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    showDatePickerDialog(context) { date ->
                        startDate = date
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(startDate?.let { dateFormat.format(it) } ?: "Start Date")
            }
            Button(
                onClick = {
                    showDatePickerDialog(context) { date ->
                        endDate = date
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(endDate?.let { dateFormat.format(it) } ?: "End Date")
            }
        }

        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Entries") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Category Totals") }
            )
        }

        // Content based on selected tab
        when (selectedTab) {
            0 -> {
                if (entries.isEmpty()) {
                    Text("No entries in this period", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn {
                        items(entries) { entry ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(entry.description, style = MaterialTheme.typography.titleMedium)
                                    Text("Amount: ${entry.amount}")
                                    Text("Date: ${dateFormat.format(entry.date)}")
                                    if (!entry.photoPath.isNullOrEmpty()) {
                                        Text("📷 Has photo", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                if (categoryTotals.isEmpty()) {
                    Text("No spending in this period", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn {
                        items(categoryTotals) { total ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(total.name, style = MaterialTheme.typography.bodyLarge)
                                    Text("R${total.total}", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to show DatePickerDialog from Compose
private fun showDatePickerDialog(context: android.content.Context, onDateSelected: (Date) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = GregorianCalendar(year, month, dayOfMonth).time
            onDateSelected(selectedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}