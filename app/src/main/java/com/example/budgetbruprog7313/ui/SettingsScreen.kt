package com.example.budgetbruprog7313.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(repository: BudgetRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
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
                    "⚙️ Settings",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Customize your app experience",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preferences Section
        Text(
            "PREFERENCES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = BudgetBruPrimary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Receive alerts about your spending",
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BudgetBruPrimary,
                                checkedTrackColor = BudgetBruPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Always use dark theme",
                    trailing = {
                        Switch(
                            checked = true,
                            onCheckedChange = { },
                            enabled = false,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BudgetBruPrimary,
                                checkedTrackColor = BudgetBruPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Data Management Section
        Text(
            "DATA MANAGEMENT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = BudgetBruPrimary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Clear All Data",
                    subtitle = "Delete all expenses, categories, and goals",
                    iconTint = BudgetBruAccent,
                    textColor = BudgetBruAccent,
                    onClick = { showClearDataDialog = true }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                SettingsItem(
                    icon = Icons.Default.Refresh,
                    title = "Reset Settings",
                    subtitle = "Reset all settings to default",
                    onClick = { showResetDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Support Section
        Text(
            "SUPPORT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = BudgetBruPrimary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "Version 1.0.0",
                    onClick = { showAboutDialog = true }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Share App",
                    subtitle = "Share BudgetBru with friends",
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Check out BudgetBru - the best budgeting app for students! Track expenses, set goals, and save money smartly. #BudgetBru")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share BudgetBru via"))
                    }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                SettingsItem(
                    icon = Icons.Default.RateReview,
                    title = "Rate Us",
                    subtitle = "Rate BudgetBru on Play Store",
                    onClick = { showRateDialog = true }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "Contact Support",
                    subtitle = "support@budgetbru.com",
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@budgetbru.com")
                            putExtra(Intent.EXTRA_SUBJECT, "BudgetBru App Support")
                            putExtra(Intent.EXTRA_TEXT, "Hello BudgetBru Team,\n\n")
                        }
                        context.startActivity(emailIntent)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Info Footer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "BudgetBru",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BudgetBruPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Smart Budgeting for Students",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "© 2024 BudgetBru. All rights reserved.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text(
                    "Clear All Data?",
                    fontWeight = FontWeight.Bold,
                    color = BudgetBruAccent
                )
            },
            text = {
                Text("This will permanently delete all your expenses, categories, and goals. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            // Clear all data from repository
                            // This would need to be implemented in the repository
                            showClearDataDialog = false
                            // Show a snackbar or toast
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = BudgetBruAccent)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkCard
        )
    }

    // Reset Settings Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    "Reset Settings?",
                    fontWeight = FontWeight.Bold,
                    color = BudgetBruPrimary
                )
            },
            text = {
                Text("This will reset all your preferences to default values.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            // Reset settings
                            repository.saveGoals(0.0, 0.0)
                            repository.saveMonthlyIncome(5000.0)
                            notificationsEnabled = true
                            showResetDialog = false
                        }
                    }
                ) {
                    Text("Reset", color = BudgetBruPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkCard
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    "About BudgetBru",
                    fontWeight = FontWeight.Bold,
                    color = BudgetBruPrimary
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = BudgetBruPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "BudgetBru",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BudgetBruPrimary
                    )
                    Text(
                        "Version 1.0.0",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "A smart budgeting app designed for students to track expenses, set financial goals, and manage money effectively.",
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Features:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "• Expense Tracking with Photos\n• Category Management\n• Monthly Goals & Budgeting\n• Spending Reports\n• IOU Tracker\n• Budgeting Tips",
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = BudgetBruPrimary)
                }
            },
            containerColor = DarkCard
        )
    }

    // Rate Dialog
    if (showRateDialog) {
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = {
                Text(
                    "Enjoying BudgetBru?",
                    fontWeight = FontWeight.Bold,
                    color = BudgetBruPrimary
                )
            },
            text = {
                Text("If you like using BudgetBru, please take a moment to rate us on the Play Store. Your support helps us improve!")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRateDialog = false
                        // Open Play Store link when available
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://play.google.com/store/apps/details?id=com.example.budgetbruprog7313")
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Rate Now", color = BudgetBruPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) {
                    Text("Maybe Later")
                }
            },
            containerColor = DarkCard
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = BudgetBruPrimary,
    textColor: Color = Color.White,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = iconTint.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.Medium,
                color = textColor,
                fontSize = 16.sp
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        // Trailing content (Switch, etc.)
        trailing?.invoke()

        // Chevron for clickable items without custom trailing
        if (onClick != null && trailing == null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}