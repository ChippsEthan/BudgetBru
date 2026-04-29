package com.example.budgetbruprog7313.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BudgetBruDarkColorScheme = darkColorScheme(
    primary = BudgetBruPrimary,
    secondary = BudgetBruSecondary,
    tertiary = BudgetBruAccent,

    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCard,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFCCCCDD)
)

@Composable
fun BudgetBruTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = BudgetBruDarkColorScheme
    val view = LocalView.current

    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = DarkBackground.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}