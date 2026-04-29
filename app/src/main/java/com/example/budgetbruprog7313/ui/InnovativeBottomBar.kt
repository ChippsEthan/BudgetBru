// KEEP YOUR ORIGINAL PACKAGE LINE HERE IF IT IS DIFFERENT
package com.example.budgetbruprog7313.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

// --- LOCAL THEME COLORS ---
val BudgetBruPrimary = Color(0xFF6366F1)
val BudgetBruSecondary = Color(0xFFA855F7)
val DarkDockBg = Color(0xFF0F172A).copy(alpha = 0.96f)

// --- SCREEN DEFINITIONS (Defined here to prevent "Unresolved Screen" errors) ---
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Expenses : Screen("expenses", "Reports", Icons.Default.List)
    data object Goals : Screen("goals", "Goals", Icons.Default.Flag)
    data object ManageCategories : Screen("manage_categories", "Categories", Icons.Default.Category)
    data object More : Screen("more", "More", Icons.Default.MoreVert)
}

@Composable
fun InnovativeBottomBar(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.Expenses,
        Screen.Goals,
        Screen.ManageCategories,
        Screen.More
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .shadow(20.dp, RoundedCornerShape(32.dp), ambientColor = BudgetBruPrimary)
                .clip(RoundedCornerShape(32.dp))
                .background(DarkDockBg)
        ) {
            // --- BOXWITHCONSTRAINTS SCOPE ACTIVATED ---
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                // Using 'this.' ensures the compiler sees we are using the scope
                val totalWidth = this.maxWidth
                val itemWidth = totalWidth / items.size
                val pillWidth = itemWidth * 0.70f

                val indicatorOffset by animateDpAsState(
                    targetValue = (itemWidth * selectedIndex) + (itemWidth - pillWidth) / 2,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "Indicator"
                )

                // The Animated Background Pill
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .align(Alignment.CenterStart)
                        .width(pillWidth)
                        .height(48.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(listOf(BudgetBruPrimary, BudgetBruSecondary))
                        )
                )

                // The Navigation Items
                Row(modifier = Modifier.fillMaxSize()) {
                    items.forEachIndexed { index, screen ->
                        val isSelected = index == selectedIndex
                        val iconScale by animateFloatAsState(if (isSelected) 1.2f else 1f)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (!isSelected) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    tint = if (isSelected) Color.White else Color.Gray,
                                    modifier = Modifier.size(24.dp).scale(iconScale)
                                )
                                AnimatedVisibility(visible = isSelected) {
                                    Text(
                                        text = screen.title,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}