package com.example.budgetbruprog7313.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.budgetbruprog7313.camera.CameraHelper
import com.example.budgetbruprog7313.data.model.Category
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import com.example.budgetbruprog7313.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseBottomSheet(
    repository: BudgetRepository,
    onExpenseAdded: () -> Unit
) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Photo was saved successfully
            println("Photo saved successfully to: $photoPath")
        } else {
            // Photo capture failed
            photoPath = null
            errorMessage = "Failed to capture photo"
        }
    }

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
        Text("Add Expense", style = MaterialTheme.typography.headlineSmall, color = BudgetBruPrimary)
        Spacer(modifier = Modifier.height(16.dp))

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it; errorMessage = null },
            label = { Text("Description *") },
            placeholder = { Text("e.g., Lunch, Uber, Coffee") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            enabled = !isSaving,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BudgetBruPrimary,
                focusedLabelColor = BudgetBruPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Amount
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it; errorMessage = null },
            label = { Text("Amount (R) *") },
            placeholder = { Text("e.g., 99.99") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSaving,
            leadingIcon = { Text("R", fontWeight = FontWeight.Bold) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BudgetBruPrimary,
                focusedLabelColor = BudgetBruPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Start Time
        OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            label = { Text("Start Time (HH:MM) *") },
            placeholder = { Text("e.g., 14:30") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = BudgetBruPrimary) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // End Time
        OutlinedTextField(
            value = endTime,
            onValueChange = { endTime = it },
            label = { Text("End Time (HH:MM) *") },
            placeholder = { Text("e.g., 15:30") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = BudgetBruPrimary) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Selection
        Text("Category *", style = MaterialTheme.typography.labelMedium, color = BudgetBruPrimary)

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
                        enabled = !isSaving,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BudgetBruPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Photo Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Optional Photo", style = MaterialTheme.typography.labelMedium, color = BudgetBruSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Updated Camera Button with error handling
                    Button(
                        onClick = {
                            try {
                                val result = CameraHelper.createImageFile(context)
                                if (result != null) {
                                    val (file, uri) = result
                                    photoPath = file.absolutePath
                                    cameraLauncher.launch(uri)
                                    errorMessage = null
                                } else {
                                    errorMessage = "Failed to create image file"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Camera error: ${e.message}"
                                e.printStackTrace()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BudgetBruSecondary),
                        enabled = !isSaving
                    ) {
                        Icon(Icons.Default.Camera, contentDescription = "Camera")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (photoPath != null) "Change Photo" else "Take Photo")
                    }

                    if (photoPath != null) {
                        Text("Photo saved", color = BudgetBruPrimary, fontSize = 12.sp)
                    }
                }

                if (photoPath != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showPhotoDialog = true }
                            .background(DarkBackground)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(photoPath),
                            contentDescription = "Receipt Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(BudgetBruPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ZoomIn,
                                contentDescription = "Zoom",
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage != null) {
            Text(
                errorMessage!!,
                color = BudgetBruAccent,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
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
                    when {
                        description.isBlank() -> errorMessage = "Please enter a description"
                        amt == null || amt <= 0 -> errorMessage = "Please enter a valid amount"
                        startTime.isBlank() -> errorMessage = "Please enter start time"
                        endTime.isBlank() -> errorMessage = "Please enter end time"
                        selectedCategoryId == null -> errorMessage = "Please select a category"
                        else -> {
                            errorMessage = null
                            isSaving = true
                            scope.launch {
                                try {
                                    val now = Date()
                                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    repository.addExpenseEntry(
                                        date = now,
                                        startTime = startTime,
                                        endTime = endTime,
                                        description = description,
                                        amount = amt,
                                        categoryId = selectedCategoryId!!,
                                        photoPath = photoPath
                                    )
                                    onExpenseAdded()
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.message}"
                                    isSaving = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isSaving && !isLoading && categories.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = BudgetBruPrimary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Save Expense")
                }
            }
        }
    }

    // Photo View Dialog
    if (showPhotoDialog && photoPath != null) {
        Dialog(
            onDismissRequest = { showPhotoDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Receipt Photo", style = MaterialTheme.typography.titleMedium, color = BudgetBruPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Image(
                        painter = rememberAsyncImagePainter(photoPath),
                        contentDescription = "Receipt",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showPhotoDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = BudgetBruPrimary)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}