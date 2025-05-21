package com.example.projectmanager.ui.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.projectmanager.data.model.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProjectMetricsDialog(
    project: Project,
    onDismiss: () -> Unit,
    onSaveMetrics: (Double, String, Double, Float, Float) -> Unit
) {
    var budgetAmount by remember { mutableStateOf(project.budgetAmount.toString()) }
    var budgetCurrency by remember { mutableStateOf(project.budgetCurrency) }
    var actualCost by remember { mutableStateOf(project.actualCost.toString()) }
    var estimatedHours by remember { mutableStateOf(project.estimatedHours.toString()) }
    var actualHours by remember { mutableStateOf(project.actualHours.toString()) }
    
    // Validation state
    val isBudgetValid = budgetAmount.toDoubleOrNull() != null
    val isActualCostValid = actualCost.toDoubleOrNull() != null
    val isEstimatedHoursValid = estimatedHours.toFloatOrNull() != null
    val isActualHoursValid = actualHours.toFloatOrNull() != null
    
    val isFormValid = isBudgetValid && isActualCostValid && isEstimatedHoursValid && isActualHoursValid
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Edit Project Metrics",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Budget section
                Text(
                    text = "Budget",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Currency
                    OutlinedTextField(
                        value = budgetCurrency,
                        onValueChange = { budgetCurrency = it },
                        label = { Text("Currency") },
                        singleLine = true,
                        modifier = Modifier.width(100.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Budget amount
                    OutlinedTextField(
                        value = budgetAmount,
                        onValueChange = { budgetAmount = it },
                        label = { Text("Budget Amount") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = budgetAmount.isNotEmpty() && !isBudgetValid
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Actual cost
                OutlinedTextField(
                    value = actualCost,
                    onValueChange = { actualCost = it },
                    label = { Text("Actual Cost") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = actualCost.isNotEmpty() && !isActualCostValid
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Time section
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Estimated hours
                    OutlinedTextField(
                        value = estimatedHours,
                        onValueChange = { estimatedHours = it },
                        label = { Text("Estimated Hours") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = estimatedHours.isNotEmpty() && !isEstimatedHoursValid,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Actual hours
                    OutlinedTextField(
                        value = actualHours,
                        onValueChange = { actualHours = it },
                        label = { Text("Actual Hours") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = actualHours.isNotEmpty() && !isActualHoursValid
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (isFormValid) {
                                onSaveMetrics(
                                    budgetAmount.toDoubleOrNull() ?: 0.0,
                                    budgetCurrency,
                                    actualCost.toDoubleOrNull() ?: 0.0,
                                    estimatedHours.toFloatOrNull() ?: 0f,
                                    actualHours.toFloatOrNull() ?: 0f
                                )
                                onDismiss()
                            }
                        },
                        enabled = isFormValid
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
