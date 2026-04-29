package com.example.budgetbruprog7313.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetbruprog7313.data.model.Category
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseBottomSheet(
    repository: BudgetRepository,
    onExpenseAdded: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Load categories
    LaunchedEffect(Unit) {
        repository.allCategories.collect { categoryList ->
            categories = categoryList
            isLoading = false
            if (selectedCategoryId == null && categoryList.isNotEmpty()) {
                selectedCategoryId = categoryList.first().id
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Add Expense", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            placeholder = { Text("e.g., Lunch, Uber, Coffee") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (R)") },
            placeholder = { Text("e.g., 99.99") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSaving,
            isError = errorMessage != null
        )

        if (errorMessage != null) {
            Text(
                errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Category", style = MaterialTheme.typography.labelMedium)

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        } else if (categories.isEmpty()) {
            Text(
                "No categories found. Please add categories in the Categories screen.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id },
                        label = { Text(category.name) },
                        enabled = !isSaving
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onExpenseAdded() },
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (description.isBlank()) {
                        errorMessage = "Please enter a description"
                    } else if (amt == null || amt <= 0) {
                        errorMessage = "Please enter a valid amount"
                    } else if (selectedCategoryId == null) {
                        errorMessage = "Please select a category"
                    } else {
                        errorMessage = null
                        isSaving = true
                        scope.launch {
                            try {
                                val now = Date()
                                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                repository.addExpenseEntry(
                                    date = now,
                                    startTime = timeFormat.format(now),
                                    endTime = timeFormat.format(now),
                                    description = description,
                                    amount = amt,
                                    categoryId = selectedCategoryId!!,
                                    photoPath = null
                                )
                                onExpenseAdded()
                            } catch (e: Exception) {
                                errorMessage = "Error: ${e.message}"
                                isSaving = false
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isSaving && !isLoading && categories.isNotEmpty()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)


                } else {
                    Text("Save Expense")
                }
            }
        }
    }
}