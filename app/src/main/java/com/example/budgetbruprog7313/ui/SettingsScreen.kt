package com.example.budgetbruprog7313.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: BudgetRepository,
    onLogout: () -> Unit          // wired from MainAppContent
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
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

        // ── PREFERENCES ───────────────────────────────────────────────────────
        SectionLabel("PREFERENCES")
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
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Always use dark theme",
                    trailing = {
                        Switch(
                            checked = true,
                            onCheckedChange = {},
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

        // ── DATA MANAGEMENT ───────────────────────────────────────────────────
        SectionLabel("DATA MANAGEMENT")
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
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                SettingsItem(
                    icon = Icons.Default.Refresh,
                    title = "Reset Settings",
                    subtitle = "Reset income and goals to defaults",
                    onClick = { showResetDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── ACCOUNT ───────────────────────────────────────────────────────────
        SectionLabel("ACCOUNT")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            SettingsItem(
                icon = Icons.Default.Logout,
                title = "Sign Out",
                subtitle = "Sign out of your Firebase account",
                iconTint = BudgetBruAccent,
                textColor = BudgetBruAccent,
                onClick = { showLogoutDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── SUPPORT ───────────────────────────────────────────────────────────
        SectionLabel("SUPPORT")
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
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Share App",
                    subtitle = "Share BudgetBru with friends",
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out BudgetBru - the smart budgeting app for students! #BudgetBru"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share BudgetBru via"))
                    }
                )
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                SettingsItem(
                    icon = Icons.Default.RateReview,
                    title = "Rate Us",
                    subtitle = "Rate BudgetBru on Play Store",
                    onClick = { showRateDialog = true }
                )
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
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

        // Footer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("BudgetBru", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BudgetBruPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Smart Budgeting for Students", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "© 2024 BudgetBru. All rights reserved.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out?", fontWeight = FontWeight.Bold, color = BudgetBruAccent) },
            text = { Text("You will need to sign in again to access your data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = BudgetBruAccent)
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            },
            containerColor = DarkCard
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data?", fontWeight = FontWeight.Bold, color = BudgetBruAccent) },
            text = { Text("This permanently deletes all your expenses, categories, and goals. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.clearSettings()
                            showClearDataDialog = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = BudgetBruAccent)
                ) { Text("Delete Everything") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") }
            },
            containerColor = DarkCard
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings?", fontWeight = FontWeight.Bold, color = BudgetBruPrimary) },
            text = { Text("This will reset your income to R5000 and clear your goals.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.saveGoals(0.0, 0.0)
                            repository.saveMonthlyIncome(5000.0)
                            notificationsEnabled = true
                            showResetDialog = false
                        }
                    }
                ) { Text("Reset", color = BudgetBruPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            },
            containerColor = DarkCard
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About BudgetBru", fontWeight = FontWeight.Bold, color = BudgetBruPrimary) },
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
                    Text("BudgetBru", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BudgetBruPrimary)
                    Text("Version 1.0.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "A smart budgeting app for students to track expenses, set goals, and manage money effectively.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "• Expense Tracking with Photos\n• Category Management\n• Monthly Goals & Budgeting\n• Spending Reports\n• IOU Tracker\n• Budgeting Tips",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
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

    if (showRateDialog) {
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("Enjoying BudgetBru?", fontWeight = FontWeight.Bold, color = BudgetBruPrimary) },
            text = { Text("Please take a moment to rate us on the Play Store!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRateDialog = false
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://play.google.com/store/apps/details?id=com.example.budgetbruprog7313")
                        }
                        context.startActivity(intent)
                    }
                ) { Text("Rate Now", color = BudgetBruPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) { Text("Maybe Later") }
            },
            containerColor = DarkCard
        )
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = BudgetBruPrimary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
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
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, color = textColor, fontSize = 16.sp)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
            }
        }
        trailing?.invoke()
        if (onClick != null && trailing == null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}