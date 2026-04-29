package com.example.budgetbruprog7313.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.budgetbruprog7313.data.database.AppDatabase
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.navigation.InnovativeBottomBar
import com.example.budgetbruprog7313.ui.theme.BudgetBruPrimary
import com.example.budgetbruprog7313.ui.theme.BudgetBruTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        BudgetRepository(AppDatabase.getDatabase(this))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BudgetBruTheme {
                var isLoggedIn by rememberSaveable { mutableStateOf(false) }

                if (!isLoggedIn) {
                    LoginScreen(
                        onLoginSuccess = { isLoggedIn = true },
                        repository = repository
                    )
                } else {
                    MainAppContent(repository = repository)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppContent(repository: BudgetRepository) {
    val navController = rememberNavController()
    var showAddExpenseBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        bottomBar = { InnovativeBottomBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExpenseBottomSheet = true },
                containerColor = BudgetBruPrimary,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->

        if (showAddExpenseBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddExpenseBottomSheet = false },
                sheetState = sheetState
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
            modifier = Modifier.padding(innerPadding)
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
                GoalsScreen()
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
                SettingsScreen(repository = repository)
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