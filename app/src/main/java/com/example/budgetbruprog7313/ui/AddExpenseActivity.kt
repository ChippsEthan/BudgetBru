package com.example.budgetbruprog7313.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budgetbruprog7313.camera.CameraHelper
import com.example.budgetbruprog7313.data.database.AppDatabase
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import kotlinx.coroutines.launch
import java.util.Date

class AddExpenseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = BudgetRepository(AppDatabase.getDatabase(this))
        setContent {
            // 🌟 Wrap with MaterialTheme
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var description by remember { mutableStateOf("") }
                    var amount by remember { mutableStateOf("") }
                    var photoPath by remember { mutableStateOf<String?>(null) }
                    val scope = rememberCoroutineScope()

                    val takePictureLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.TakePicture()
                    ) { /* photo saved */ }

                    Column(modifier = Modifier.padding(32.dp)) {
                        TextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            val (file, uri) = CameraHelper.createImageFile(this@AddExpenseActivity)
                            photoPath = file.absolutePath
                            takePictureLauncher.launch(uri)
                        }) { Text("Take Photo") }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            scope.launch {
                                val amt = amount.toDoubleOrNull() ?: 0.0
                                repository.addExpenseEntry(
                                    Date(), "14:00", "15:30", description, amt,
                                    1L,
                                    photoPath
                                )
                                finish()
                            }
                        }) { Text("Save Expense") }
                    }
                }
            }
        }
    }
}