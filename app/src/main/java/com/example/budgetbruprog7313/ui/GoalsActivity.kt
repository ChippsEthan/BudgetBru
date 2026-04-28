package com.example.budgetbruprog7313.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.theme.BudgetBruTheme
import kotlinx.coroutines.launch
import com.example.budgetbruprog7313.data.database.AppDatabase
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class GoalsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetBruTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GoalsScreen()
                }
            }
        }
    }
}

@Composable
fun GoalsScreen() {
    val context = LocalContext.current
    val repository = remember {
        BudgetRepository(AppDatabase.getDatabase(context))
    }
    val scope = rememberCoroutineScope()
    var minGoal by remember { mutableStateOf("") }
    var maxGoal by remember { mutableStateOf("") }
    var currentMin by remember { mutableStateOf<Double?>(null) }
    var currentMax by remember { mutableStateOf<Double?>(null) }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    // Load existing goals
    LaunchedEffect(Unit) {
        repository.getGoals().collect { settings ->
            currentMin = settings?.minMonthlyGoal
            currentMax = settings?.maxMonthlyGoal
            if (settings != null) {
                minGoal = settings.minMonthlyGoal.toString()
                maxGoal = settings.maxMonthlyGoal.toString()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Set Monthly Spending Goals", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = minGoal,
            onValueChange = { minGoal = it },
            label = { Text("Minimum monthly goal (e.g., 500)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = maxGoal,
            onValueChange = { maxGoal = it },
            label = { Text("Maximum monthly goal (e.g., 2000)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val min = minGoal.toDoubleOrNull()
                val max = maxGoal.toDoubleOrNull()
                if (min != null && max != null && min <= max) {
                    scope.launch {
                        repository.saveGoals(min, max)
                        savedMessage = "Goals saved successfully"
                    }
                } else {
                    savedMessage = "Invalid input: Min ≤ Max required"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Goals")
        }

        savedMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Current Goals:", style = MaterialTheme.typography.titleMedium)
        currentMin?.let { Text("Minimum: $it") }
        currentMax?.let { Text("Maximum: $it") }
        if (currentMin == null) Text("No goals set yet")
    }
}