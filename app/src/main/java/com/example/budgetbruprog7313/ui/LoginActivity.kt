package com.example.budgetbruprog7313.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budgetbruprog7313.data.database.AppDatabase
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import kotlinx.coroutines.launch


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = BudgetRepository(AppDatabase.getDatabase(this))
        setContent {
            // 🌟 Wrap everything with MaterialTheme
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var username by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }
                    var error by remember { mutableStateOf<String?>(null) }
                    val scope = rememberCoroutineScope()

                    Column(modifier = Modifier.padding(32.dp)) {
                        TextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") }

                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            scope.launch {
                                val user = repository.login(username, password)
                                if (user != null) {
                                    startActivity(Intent(this@LoginActivity, AddExpenseActivity::class.java))
                                } else {
                                    error = "Invalid login"
                                }
                            }
                        }) { Text("Login") }
                        error?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}