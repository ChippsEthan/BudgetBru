package com.example.budgetbruprog7313.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen("home", "Home", Default.Home)
    data object Expenses : Screen("expenses", "Reports", Default.List)
    data object Goals : Screen("goals", "Goals", Icons.Default.Flag)
    data object ManageCategories : Screen("manage_categories", "Categories", Default.Category)
    data object More : Screen("more", "More", Icons.Default.MoreVert)
    data object AllExpenses : Screen("all_expenses", "All Expenses", Default.List)

    // Screens accessible from More menu
    data object IOU : Screen("iou", "IOU Tracker", Default.People)
    data object Tips : Screen("tips", "Budgeting Tips", Default.Info)
    data object Settings : Screen("settings", "Settings", Default.Settings)
}