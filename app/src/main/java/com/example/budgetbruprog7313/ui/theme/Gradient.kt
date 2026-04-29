package com.example.budgetbruprog7313.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val BudgetBruGradient = Brush.linearGradient(
    colors = listOf(
        PurpleGradientStart,
        BudgetBruPrimary,
        BudgetBruSecondary
    )
)

val CardGradient = Brush.linearGradient(
    colors = listOf(
        DarkCard,
        Color(0xFF1E2A5E)
    )
)