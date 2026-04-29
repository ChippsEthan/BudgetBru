package com.example.budgetbruprog7313.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetbruprog7313.ui.theme.*

@Composable
fun TipsScreen() {
    val tips = listOf(
        BudgetTip(
            title = "50/30/20 Rule",
            description = "Allocate 50% of income to needs, 30% to wants, and 20% to savings.",
            icon = Icons.Default.PieChart,
            color = BudgetBruPrimary
        ),
        BudgetTip(
            title = "Track Every Expense",
            description = "Small purchases add up. Track everything to identify spending patterns.",
            icon = Icons.Default.Receipt,
            color = BudgetBruSecondary
        ),
        BudgetTip(
            title = "Use Cash for Discretionary",
            description = "Withdraw cash for entertainment and dining out to limit overspending.",
            icon = Icons.Default.AttachMoney,
            color = Color(0xFF4CAF50)
        ),
        BudgetTip(
            title = "Automate Savings",
            description = "Set up automatic transfers to savings on payday.",
            icon = Icons.Default.AutoAwesome,
            color = Color(0xFFFF9800)
        ),
        BudgetTip(
            title = "Review Subscriptions",
            description = "Cancel unused subscriptions and memberships.",
            icon = Icons.Default.Cancel,
            color = BudgetBruAccent
        ),
        BudgetTip(
            title = "Meal Prep",
            description = "Plan meals to reduce food delivery and dining out costs.",
            icon = Icons.Default.Restaurant,
            color = Color(0xFF8BC34A)
        ),
        BudgetTip(
            title = "Emergency Fund",
            description = "Save 3-6 months of expenses for emergencies.",
            icon = Icons.Default.Security,
            color = Color(0xFF2196F3)
        ),
        BudgetTip(
            title = "Use Student Discounts",
            description = "Always ask for student discounts on software, transport, and entertainment.",
            icon = Icons.Default.School,
            color = Color(0xFF9C27B0)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text(
            "Budgeting Tips",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = BudgetBruPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tips) { tip ->
                TipCard(tip)
            }
        }
    }
}

data class BudgetTip(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color
)

@Composable
fun TipCard(tip: BudgetTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(tip.color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    tip.icon,
                    contentDescription = null,
                    tint = tip.color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tip.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = tip.color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    tip.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}