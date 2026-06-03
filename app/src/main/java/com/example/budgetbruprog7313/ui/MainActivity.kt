package com.example.budgetbruprog7313.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.navigation.InnovativeBottomBar
import com.example.budgetbruprog7313.ui.theme.BudgetBruTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy { BudgetRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BudgetBruTheme {
                var isLoggedIn by rememberSaveable { mutableStateOf(repository.isLoggedIn()) }

                if (!isLoggedIn) {
                    LoginScreen(
                        onLoginSuccess = { isLoggedIn = true },
                        repository = repository
                    )
                } else {
                    MainAppContent(
                        repository = repository,
                        onLogout = { isLoggedIn = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    repository: BudgetRepository,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    var showAddExpenseBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Track current route for FAB visibility
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = Color(0xFF07070F),
        bottomBar = {
            InnovativeBottomBar(navController = navController)
        }
        // FAB COMPLETELY REMOVED FROM HERE - no floatingActionButton section!
    ) { innerPadding ->

        // Add Expense Bottom Sheet
        if (showAddExpenseBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddExpenseBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF0F0F1E),
                tonalElevation = 0.dp,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }
            ) {
                AddExpenseBottomSheet(
                    repository = repository,
                    onExpenseAdded = { showAddExpenseBottomSheet = false }
                )
            }
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    repository = repository,
                    onViewAllClick = {
                        navController.navigate(Screen.AllExpenses.route)
                    }
                )
            }
            composable(Screen.Expenses.route) {
                PeriodReportScreen()
            }
            composable(Screen.Goals.route) {
                GoalsScreen(repository = repository)
            }
            composable(Screen.ManageCategories.route) {
                ManageCategoriesScreen()
            }
            composable(Screen.More.route) {
                MoreScreen(navController = navController)
            }
            composable(Screen.IOU.route) {
                IOUScreen()
            }
            composable(Screen.Tips.route) {
                TipsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    repository = repository,
                    onLogout = {
                        repository.logout()
                        onLogout()
                    }
                )
            }
            composable(Screen.AllExpenses.route) {
                AllExpensesScreen(
                    navController = navController,
                    repository = repository
                )
            }
        }
    }
}