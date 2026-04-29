package com.example.budgetbruprog7313.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    repository: BudgetRepository   // Passed from MainActivity
) {
    // Create ViewModel with factory
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))

    val totalSpent by viewModel.totalSpent.collectAsState()
    val availableBalance by viewModel.availableBalance.collectAsState()
    val recentExpenses by viewModel.recentExpenses.collectAsState()

    val scrollState = rememberScrollState()
    val dateFormatter = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) }
    val currentDate = remember { dateFormatter.format(Date()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text("Hey Bru! 👋", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = BudgetBruPrimary)
        Text(currentDate, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        // Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BudgetBruGradient),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Available Balance", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "R ${String.format("%.2f", availableBalance)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 42.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Total Spent", "R ${String.format("%.2f", totalSpent)}", BudgetBruAccent, true, Modifier.weight(1f))
            StatCard("Income", "R 5,800.00", BudgetBruSecondary, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text("Quick Add", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(listOf("Food", "Transport", "Groceries", "Fun", "Study", "Bills")) { cat ->
                QuickAddChip(
                    category = cat,
                    onClick = {
                        // Simple quick add example with placeholder category ID
                        viewModel.addQuickExpense(50.0, "Quick $cat", 1L)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text("Recent Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

        if (recentExpenses.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Text(
                    "No expenses yet.\nStart adding some!",
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            recentExpenses.forEach { expense ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "${expense.description} - R${expense.amount}"
                    )
                }
            }
        }
    }
}

// Reusable Components
@Composable
fun StatCard(
    title: String,
    amount: String,
    color: Color,
    isExpense: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(118.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isExpense) BudgetBruAccent else color
            )
        }
    }
}

@Composable
fun QuickAddChip(
    category: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.size(82.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}