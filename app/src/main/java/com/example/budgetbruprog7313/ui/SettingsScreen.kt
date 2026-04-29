package com.example.budgetbruprog7313.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(repository: BudgetRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text(
            "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = BudgetBruPrimary,
            modifier = Modifier.padding(bottom = 24.dp)
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
                            checked = darkModeEnabled,
                            onCheckedChange = { darkModeEnabled = it },
                            enabled = false, // Dark mode is forced in your theme
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BudgetBruPrimary,
                                checkedTrackColor = BudgetBruPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "Version 1.0.0",
                    onClick = { /* Show about dialog */ }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Share App",
                    subtitle = "Share BudgetBru with friends",
                    onClick = { /* Share intent */ }
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                SettingsItem(
                    icon = Icons.Default.RateReview,
                    title = "Rate Us",
                    subtitle = "Rate BudgetBru on Play Store",
                    onClick = { /* Rate intent */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Danger Zone
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BudgetBruAccent.copy(alpha = 0.1f))
        ) {
            Column {
                Text(
                    "Danger Zone",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BudgetBruAccent,
                    modifier = Modifier.padding(16.dp)
                )

                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Clear All Data",
                    subtitle = "Delete all expenses and reset settings",
                    iconTint = BudgetBruAccent,
                    textColor = BudgetBruAccent,
                    onClick = {
                        // Show confirmation dialog
                    }
                )
            }
        }
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
        Icon(
            icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, color = textColor)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
            }
        }
        trailing?.invoke()
        if (onClick != null && trailing == null) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
        }
    }
}