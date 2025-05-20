package com.example.projectmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projectmanager.data.model.TaskPriority
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit,
    showDatePicker: (Date?, (Date) -> Unit) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var dueDate by remember { mutableStateOf(task.dueDate) }
    var priority by remember { mutableStateOf(task.priority) }
    var status by remember { mutableStateOf(task.status) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Due date selector
                OutlinedButton(
                    onClick = { 
                        showDatePicker(dueDate) { selectedDate ->
                            dueDate = selectedDate
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select due date"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dueDate?.let { 
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                        } ?: "Set Due Date"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Priority selector
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.values().forEach { priorityOption ->
                        FilterChip(
                            selected = priority == priorityOption,
                            onClick = { priority = priorityOption },
                            label = { Text(priorityOption.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Status selector
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskStatus.values().forEach { statusOption ->
                        if (statusOption != TaskStatus.CANCELLED) { // Optional filtering
                            FilterChip(
                                selected = status == statusOption,
                                onClick = { status = statusOption },
                                label = { Text(statusOption.name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        task.copy(
                            title = title,
                            description = description,
                            dueDate = dueDate,
                            priority = priority,
                            status = status
                        )
                    )
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 