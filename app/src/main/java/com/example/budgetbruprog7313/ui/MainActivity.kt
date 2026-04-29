package com.example.budgetbruprog7313.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
            println("Camera permission granted")
        } else {
            // Permission denied
            println("Camera permission denied")
        }
    }

    private fun checkAndRequestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request camera permission
        checkAndRequestCameraPermission()

        setContent {
            BudgetBruTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                val navController = rememberNavController()
                var showAddExpenseBottomSheet by remember { mutableStateOf(false) }
                val sheetState = rememberModalBottomSheetState()

                if (!isLoggedIn) {
                    LoginScreen(
                        onLoginSuccess = { isLoggedIn = true },
                        repository = repository
                    )
                } else {
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
                                onDismissRequest = {
                                    showAddExpenseBottomSheet = false
                                },
                                sheetState = sheetState
                            ) {
                                AddExpenseBottomSheet(
                                    repository = repository,
                                    onExpenseAdded = {
                                        showAddExpenseBottomSheet = false
                                    }
                                )
                            }
                        }
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
                            composable(Screen.IOU.route) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "IOU feature coming soon",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            composable(Screen.Tips.route) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Budgeting tips coming soon",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            composable(Screen.ManageCategories.route) {
                                ManageCategoriesScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}