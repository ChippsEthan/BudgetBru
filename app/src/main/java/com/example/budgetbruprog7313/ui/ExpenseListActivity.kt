package com.example.budgetbruprog7313.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budgetbruprog7313.data.database.AppDatabase
import com.example.budgetbruprog7313.data.model.ExpenseEntry
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import java.util.Date


class ExpenseListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = BudgetRepository(AppDatabase.getDatabase(this))
        setContent {
            // 🌟 Wrap with MaterialTheme
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var entries by remember { mutableStateOf(listOf<ExpenseEntry>()) }
                    LaunchedEffect(Unit) {
                        repository.getEntriesBetweenDates(Date(0), Date()).collect {
                            entries = it
                        }
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Expenses", style = MaterialTheme.typography.headlineMedium)
                        entries.forEach {
                            Text("${it.description} – R${it.amount}")
                        }
                    }
                }
            }
        }
    }
}