package com.example.budgetbruprog7313.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.budgetbruprog7313.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen() {
    var selectedTip by remember { mutableStateOf<Tip?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Budgeting", "Saving", "Cutting Costs", "Student Perks", "Emergency")

    val allTips = listOf(
        Tip(
            id = 1,
            title = "50/30/20 Rule",
            description = "Allocate 50% to needs, 30% to wants, 20% to savings",
            fullContent = """
                The 50/30/20 rule is a simple budgeting framework:
                
                • 50% for NEEDS: Rent, utilities, groceries, transport, minimum loan payments
                • 30% for WANTS: Dining out, entertainment, shopping, subscriptions
                • 20% for SAVINGS: Emergency fund, investments, extra debt payments
                
                This rule helps maintain balance between living comfortably and saving for the future.
                As a student, adjust the percentages to fit your situation (e.g., 60/20/20).
            """.trimIndent(),
            icon = Icons.Default.PieChart,
            color = BudgetBruPrimary,
            category = "Budgeting"
        ),
        Tip(
            id = 2,
            title = "Track Every Expense",
            description = "Small purchases add up. Track everything to identify spending patterns",
            fullContent = """
                Why track every expense?
                
                • Small daily purchases (coffee, snacks) can cost R500+ per month
                • Tracking reveals spending leaks you didn't notice
                • Apps like BudgetBru make it easy to categorize spending
                • Awareness is the first step to better habits
                
                Try tracking for 30 days - you'll be surprised where your money goes!
                Most students discover they spend 30% more than they thought on food delivery.
            """.trimIndent(),
            icon = Icons.Default.Receipt,
            color = BudgetBruSecondary,
            category = "Budgeting"
        ),
        Tip(
            id = 3,
            title = "Use Cash for Spending",
            description = "Withdraw cash for entertainment to limit overspending",
            fullContent = """
                The cash envelope method:
                
                1. Set a weekly budget for variable expenses (eating out, entertainment)
                2. Withdraw that amount in cash
                3. When cash runs out, stop spending until next week
                
                Why it works:
                • Spending physical cash feels more real than tapping a card
                • You can't overspend what you don't have
                • Visual reminder of remaining budget
                
                Try this for one month - most students reduce discretionary spending by 25%!
            """.trimIndent(),
            icon = Icons.Default.AttachMoney,
            color = Color(0xFF4CAF50),
            category = "Saving"
        ),
        Tip(
            id = 4,
            title = "Automate Savings",
            description = "Set up automatic transfers to savings on payday",
            fullContent = """
                Pay yourself first strategy:
                
                • Set up an automatic transfer the day after income arrives
                • Start small - even R200 per month grows over time
                • Use a separate savings account to avoid temptation
                
                Student saving goals:
                • Emergency fund: 3-6 months of expenses (R3,000 - R6,000)
                • Short-term: New laptop, textbooks, travel
                • Long-term: Post-grad studies, car deposit
                
                R200/month invested for 3 years = R7,200 + interest!
            """.trimIndent(),
            icon = Icons.Default.AutoAwesome,
            color = Color(0xFFFF9800),
            category = "Saving"
        ),
        Tip(
            id = 5,
            title = "Review Subscriptions",
            description = "Cancel unused subscriptions and memberships",
            fullContent = """
                Hidden subscription costs for students:
                
                Common subscriptions:
                • Streaming services (Netflix, Spotify, Disney+): R500+/month
                • Gym memberships: R300-600/month
                • Apps and cloud storage: R100-200/month
                • Meal kits and boxes: R400+/month
                
                Action steps:
                1. List all active subscriptions
                2. Calculate total monthly cost
                3. Cancel what you haven't used in 30 days
                4. Share accounts with roommates when possible
                
                Most students save R500-1000/month by canceling unused subscriptions!
            """.trimIndent(),
            icon = Icons.Default.Cancel,
            color = BudgetBruAccent,
            category = "Cutting Costs"
        ),
        Tip(
            id = 6,
            title = "Meal Prep",
            description = "Plan meals to reduce food delivery costs",
            fullContent = """
                Student meal prep strategy:
                
                Cost comparison:
                • Takeout lunch: R80-120/day = R400-600/week
                • Meal prepped lunch: R30-40/day = R150-200/week
                • Monthly savings: R1,000+
                
                Beginner meal prep tips:
                1. Cook 2-3 large batches on Sunday
                2. Use versatile ingredients (rice, beans, chicken, vegetables)
                3. Invest in quality containers
                4. Freeze portions for busy weeks
                
                Try: Rice + beans + roasted vegetables + protein of choice
                This costs under R30 per meal and is nutritionally balanced!
            """.trimIndent(),
            icon = Icons.Default.Restaurant,
            color = Color(0xFF8BC34A),
            category = "Cutting Costs"
        ),
        Tip(
            id = 7,
            title = "Student Discounts",
            description = "Always ask for student discounts everywhere",
            fullContent = """
                Student discounts you might miss:
                
                Software & Tech:
                • Microsoft Office: Free with student email
                • Adobe Creative Cloud: 60%+ off
                • Spotify Premium: 50% off
                • GitHub Student Pack: $200+ of free tools
                
                Local discounts:
                • Museums and galleries: 50% off with student ID
                • Movie theaters: R10-20 off tickets
                • Public transport: Student fares
                • Gym memberships: Student rates
                
                Always carry your student ID and ASK! You'd be surprised what's available.
            """.trimIndent(),
            icon = Icons.Default.School,
            color = Color(0xFF9C27B0),
            category = "Student Perks"
        ),
        Tip(
            id = 8,
            title = "Emergency Fund",
            description = "Save 3-6 months of expenses for emergencies",
            fullContent = """
                Why students need emergency funds:
                
                Unexpected expenses happen:
                • Laptop repair/replacement: R2,000-5,000
                • Medical emergency: variable
                • Car trouble: R1,000-3,000
                • Last-minute travel: R1,000+
                
                Building your fund:
                1. Start with R1,000 goal
                2. Aim for 1 month of expenses
                3. Work toward 3 months
                4. Keep in high-yield savings account
                
                Even R500 saved can prevent going into debt for small emergencies!
            """.trimIndent(),
            icon = Icons.Default.Security,
            color = Color(0xFF2196F3),
            category = "Emergency"
        )
    )

    // Filter tips
    val filteredTips = allTips.filter { tip ->
        (selectedCategory == "All" || tip.category == selectedCategory) &&
                (searchQuery.isEmpty() || tip.title.contains(searchQuery, ignoreCase = true) ||
                        tip.description.contains(searchQuery, ignoreCase = true))
    }

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
                        colors = listOf(BudgetBruPrimary, BudgetBruSecondary, Color(0xFF6B21A5))
                    ),
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "💡 Budgeting Tips",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "${filteredTips.size} expert tips to save money",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search tips...", color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BudgetBruPrimary) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BudgetBruPrimary,
                focusedLabelColor = BudgetBruPrimary,
                cursorColor = BudgetBruPrimary
            )
        )

        // Category Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            category,
                            fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BudgetBruPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tips List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredTips) { tip ->
                ModernTipCard(
                    tip = tip,
                    onClick = { selectedTip = tip }
                )
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Tip Detail Dialog
    if (selectedTip != null) {
        ModernTipDetailDialog(
            tip = selectedTip!!,
            onDismiss = { selectedTip = null }
        )
    }
}

@Composable
fun ModernTipCard(tip: Tip, onClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = tip.color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        tip.icon,
                        contentDescription = tip.title,
                        tint = tip.color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tip.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = tip.color
                )
                Text(
                    tip.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Category badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = tip.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        tip.category,
                        fontSize = 10.sp,
                        color = tip.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = tip.color,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ModernTipDetailDialog(tip: Tip, onDismiss: () -> Unit) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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
                        color = tip.color.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                tip.icon,
                                contentDescription = tip.title,
                                tint = tip.color,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            tip.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = tip.color
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = tip.color.copy(alpha = 0.15f)
                        ) {
                            Text(
                                tip.category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = tip.color,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Divider(color = tip.color.copy(alpha = 0.2f))

                // Content
                Text(
                    tip.fullContent,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tip.color)
                ) {
                    Text("Got it! 💪", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

data class Tip(
    val id: Int,
    val title: String,
    val description: String,
    val fullContent: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val category: String
)