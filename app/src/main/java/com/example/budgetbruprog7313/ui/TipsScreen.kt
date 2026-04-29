package com.example.budgetbruprog7313.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.budgetbruprog7313.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen() {
    var selectedTip by remember { mutableStateOf<Tip?>(null) }

    val tips = listOf(
        Tip(
            id = 1,
            title = "50/30/20 Rule",
            description = "Allocate 50% of your income to needs, 30% to wants, and 20% to savings.",
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
            description = "Small purchases add up. Track everything to identify spending patterns.",
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
            category = "Tracking"
        ),
        Tip(
            id = 3,
            title = "Use Cash for Spending",  // Changed from "discretionary"
            description = "Withdraw cash for entertainment to limit overspending.",
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
            category = "Spending Control"
        ),
        Tip(
            id = 4,
            title = "Automate Savings",
            description = "Set up automatic transfers to savings on payday.",
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
            description = "Cancel unused subscriptions and memberships.",
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
            description = "Plan meals to reduce food delivery and dining out costs.",
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
            category = "Food & Groceries"
        ),
        Tip(
            id = 7,
            title = "Emergency Fund",
            description = "Save 3-6 months of expenses for emergencies.",
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
        ),
        Tip(
            id = 8,
            title = "Student Discounts",
            description = "Always ask for student discounts on software, transport, and entertainment.",
            fullContent = """
                Student discounts you might miss:
                
                Software & Tech:
                • Microsoft Office: Free with student email
                • Adobe Creative Cloud: 60%+ off
                • Spotify + Hulu: R50/month instead of R120
                • Amazon Prime Student: 50% off + free delivery
                • GitHub Student Pack: $200+ of free tools
                
                Local discounts:
                • Museums and galleries: 50% off with student ID
                • Movie theaters: R10-20 off tickets
                • Public transport: Student fares
                • Gym memberships: Student rates
                • Phone plans: Student data bundles
                
                Always carry your student ID and ASK! You'd be surprised what's available.
            """.trimIndent(),
            icon = Icons.Default.School,
            color = Color(0xFF9C27B0),
            category = "Student Perks"
        ),
        Tip(
            id = 9,
            title = "Use Student Banking",
            description = "Student accounts offer zero fees and other benefits.",
            fullContent = """
                Student banking benefits in South Africa:
                
                Features to look for:
                • Zero monthly fees
                • Free transactions
                • Small overdraft facility (R500-1,000)
                • Cashback on purchases
                • Budgeting tools
                
                Banks to compare:
                • Capitec: Low fees, good app
                • FNB Student: eBucks rewards
                • Standard Bank: Student-specific benefits
                • ABSA: Zero fees on many transactions
                • Nedbank: Savings bonuses
                
                Avoid: Credit cards with high interest, payday loans, store cards
            """.trimIndent(),
            icon = Icons.Default.AccountBalance,
            color = Color(0xFF673AB7),
            category = "Banking"
        ),
        Tip(
            id = 10,
            title = "Buy Secondhand",
            description = "Textbooks, furniture, and electronics are much cheaper used.",
            fullContent = """
                Where to find student deals:
                
                Textbooks:
                • Campus notice boards
                • Facebook Marketplace
                • Textbook exchange groups
                • Previous year editions (usually 90% same content)
                • Digital versions can be cheaper
                
                Furniture & Appliances:
                • Students graduating sell everything cheap
                • Look for end-of-semester sales (June, November)
                • Check BBB (Bid or Buy) and Gumtree
                • Freecycle groups for free items
                
                Electronics:
                • Certified refurbished from reputable sellers
                • Last year's model (saves 30-50%)
                • Student discount on Apple Education Store
                
                One tip: Never buy new textbooks - you can save 50-70%!
            """.trimIndent(),
            icon = Icons.Default.ShoppingCart,
            color = Color(0xFFE91E63),
            category = "Smart Shopping"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(BudgetBruPrimary, BudgetBruSecondary)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "💰 Budgeting Tips",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "${tips.size} expert tips to save money",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tips List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tips) { tip ->
                    TipCard(
                        tip = tip,
                        onClick = { selectedTip = tip }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Tip Detail Dialog
    if (selectedTip != null) {
        TipDetailDialog(
            tip = selectedTip!!,
            onDismiss = { selectedTip = null }
        )
    }
}

@Composable
fun TipCard(tip: Tip, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        ),
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
            Spacer(modifier = Modifier.width(16.dp))
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        tip.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = tip.color
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    tip.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = tip.color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TipDetailDialog(tip: Tip, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = DarkCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape,
                        color = tip.color.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                tip.icon,
                                contentDescription = tip.title,
                                tint = tip.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            tip.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = tip.color
                        )
                        Text(
                            tip.category,
                            fontSize = 12.sp,
                            color = tip.color.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = tip.color.copy(alpha = 0.3f))

                Spacer(modifier = Modifier.height(16.dp))

                // Content
                Text(
                    tip.fullContent,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = tip.color)
                ) {
                    Text("Close", color = Color.White)
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