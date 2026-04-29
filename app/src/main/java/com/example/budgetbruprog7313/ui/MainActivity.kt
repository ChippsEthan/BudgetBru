package com.example.budgetbruprog7313.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.budgetbruprog7313.data.database.AppDatabase
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.navigation.BudgetBruBottomBar
import com.example.budgetbruprog7313.ui.theme.BudgetBruPrimary
import com.example.budgetbruprog7313.ui.theme.BudgetBruTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        BudgetRepository(AppDatabase.getDatabase(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BudgetBruTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                val navController = rememberNavController()

                if (!isLoggedIn) {
                    LoginScreen(
                        onLoginSuccess = { isLoggedIn = true },
                        repository = repository
                    )
                } else {
                    Scaffold(
                        bottomBar = { BudgetBruBottomBar(navController) },
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = { /* TODO: Add Expense */ },
                                containerColor = BudgetBruPrimary
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Expense")
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Screen.Home.route) {
                                HomeScreen(repository = repository)
                            }
                            composable(Screen.Expenses.route) {
                                PeriodReportScreen()
                            }
                            composable(Screen.Goals.route) {
                                GoalsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}