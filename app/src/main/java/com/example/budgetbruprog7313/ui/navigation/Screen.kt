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
    data object Home     : Screen("home",     "Home",     Default.Home)
    data object Expenses : Screen("expenses", "Expenses", Default.List)

    data object Goals : Screen("goals", "Goals", Icons.Default.Flag)
    data object IOU      : Screen("iou",      "IOU",      Default.Person)
    data object Tips     : Screen("tips",     "Tips",     Default.Info)
}