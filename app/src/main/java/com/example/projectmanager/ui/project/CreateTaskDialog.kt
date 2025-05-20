package com.example.projectmanager.ui.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.projectmanager.data.model.*
import com.example.projectmanager.ui.components.ProjectDatePicker
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    project: Project,
    availableMembers: List<User>,
    onDismiss: () -> Unit,
    onCreateTask: (Task) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedMemberIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var estimatedHours by remember { mutableStateOf("") }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog title
                Text(
                    text = "Create New Task",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Task title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("Enter task title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Task description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Enter task description") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Priority selection
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.values().forEach { priorityOption ->
                        FilterChip(
                            selected = priority == priorityOption,
                            onClick = { priority = priorityOption },
                            label = { Text(priorityOption.name) },
                            leadingIcon = {
                                val icon = when (priorityOption) {
                                    Priority.LOW -> Icons.Default.ArrowDownward
                                    Priority.MEDIUM -> Icons.Default.Remove
                                    Priority.HIGH -> Icons.Default.ArrowUpward
                                    Priority.URGENT -> Icons.Default.PriorityHigh
                                }
                                Icon(icon, contentDescription = null)
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Due date selection
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dueDate?.let { 
                            java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it) 
                        } ?: "Set Due Date"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Estimated hours
                OutlinedTextField(
                    value = estimatedHours,
                    onValueChange = { 
                        // Only allow numeric input with decimal point
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            estimatedHours = it
                        }
                    },
                    label = { Text("Estimated Hours") },
                    placeholder = { Text("e.g., 4.5") },
                    singleLine = true,
                    leadingIcon = { 
                        Icon(Icons.Outlined.Timer, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Member assignment
                Text(
                    text = "Assign To",
                    style = MaterialTheme.typography.labelLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (availableMembers.isEmpty()) {
                    Text(
                        text = "No members available to assign",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableMembers.forEach { user ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedMemberIds.contains(user.id),
                                    onCheckedChange = { isChecked ->
                                        selectedMemberIds = if (isChecked) {
                                            selectedMemberIds + user.id
                                        } else {
                                            selectedMemberIds - user.id
                                        }
                                    }
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Dialog buttons
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
                            val newTask = Task(
                                title = title,
                                description = description,
                                projectId = project.id,
                                assignedTo = selectedMemberIds,
                                createdBy = "", // This will be set by the repository
                                status = TaskStatus.TODO,
                                priority = priority,
                                dueDate = dueDate,
                                estimatedHours = estimatedHours.toFloatOrNull()
                            )
                            onCreateTask(newTask)
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Create Task")
                    }
                }
            }
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        ProjectDatePicker(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = {
                dueDate = it
                showDatePicker = false
            }
        )
    }
}
