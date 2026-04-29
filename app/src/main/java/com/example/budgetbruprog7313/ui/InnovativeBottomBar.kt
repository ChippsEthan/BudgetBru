package com.example.budgetbruprog7313.ui.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.budgetbruprog7313.ui.Screen
import com.example.budgetbruprog7313.ui.theme.BudgetBruPrimary
import com.example.budgetbruprog7313.ui.theme.BudgetBruSecondary
import com.example.budgetbruprog7313.ui.theme.DarkCard

@Composable
fun InnovativeBottomBar(navController: NavController) {
    val items = listOf(
        NavItem(Screen.Home, "Home", Icons.Default.Home),
        NavItem(Screen.Expenses, "Reports", Icons.Default.TrendingUp),
        NavItem(Screen.Goals, "Goals", Icons.Default.Flag),
        NavItem(Screen.ManageCategories, "Categories", Icons.Default.Category),
        NavItem(Screen.IOU, "IOU", Icons.Default.People)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(
                elevation = 15.dp,
                shape = RoundedCornerShape(30.dp),
                spotColor = BudgetBruPrimary.copy(alpha = 0.2f),
                ambientColor = BudgetBruSecondary.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(30.dp),
        color = DarkCard,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.screen.route == currentRoute
                ModernNavItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ModernNavItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Use a fixed width container instead of weight
    Box(
        modifier = Modifier
            .width(70.dp)
            .height(64.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated icon background for selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    BudgetBruPrimary.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                radius = 25f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        item.icon,
                        contentDescription = item.title,
                        tint = BudgetBruPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Icon(
                    item.icon,
                    contentDescription = item.title,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Label
            Text(
                text = item.title,
                fontSize = if (isSelected) 11.sp else 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) BudgetBruPrimary else Color.White.copy(alpha = 0.5f),
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

data class NavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)