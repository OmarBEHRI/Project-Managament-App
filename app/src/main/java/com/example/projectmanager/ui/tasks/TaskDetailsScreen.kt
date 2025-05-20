package com.example.projectmanager.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.data.model.*
import com.example.projectmanager.ui.components.ProjectDatePicker
import com.example.projectmanager.ui.components.TaskStatusChip
import com.example.projectmanager.ui.components.PriorityChip
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProject: (String) -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val task = uiState.task
    val subtasks = uiState.subtasks
    val assignedUsers = uiState.assignedUsers
    
    // Load task when the screen is first displayed
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }
    
    // Show success message when task is updated
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            // Reset update state after showing success message
            viewModel.resetUpdateState()
        }
    }
    
    // Dialog states
    var showStatusDialog by remember { mutableStateOf(false) }
    var showPriorityDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddSubtaskDialog by remember { mutableStateOf(false) }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var showEditDescriptionDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (task != null) {
                        IconButton(
                            onClick = { viewModel.toggleTaskCompletion() }
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (task.isCompleted) "Mark as incomplete" else "Mark as complete",
                                tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (task == null) {
                Text(
                    text = uiState.error ?: "Task not found",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                // Task details content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Task title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(onClick = { showEditTitleDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit title"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Status and priority
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            onClick = { showStatusDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Status",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TaskStatusChip(status = task.status)
                            }
                        }
                        
                        Card(
                            onClick = { showPriorityDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Priority",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                PriorityChip(priority = task.priority)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Due date
                    Card(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Due Date",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.dueDate?.let { 
                                        SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(it) 
                                    } ?: "No due date set",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (task.isOverdue) MaterialTheme.colorScheme.error 
                                           else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit due date",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Description
                    Card(
                        onClick = { showEditDescriptionDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Description",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit description",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = task.description.ifBlank { "No description provided" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (task.description.isBlank()) 
                                       MaterialTheme.colorScheme.onSurfaceVariant 
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Assigned users
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Assigned To",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (assignedUsers.isEmpty()) {
                                Text(
                                    text = "No users assigned",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                assignedUsers.forEach { user ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = MaterialTheme.colorScheme.primary
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
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Subtasks
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Subtasks",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { showAddSubtaskDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add subtask"
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (subtasks.isEmpty()) {
                                Text(
                                    text = "No subtasks",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                subtasks.forEach { subtask ->
                                    SubtaskItem(
                                        subtask = subtask,
                                        onToggleComplete = { /* Handle in future implementation */ },
                                        onClick = { /* Navigate to subtask details */ }
                                    )
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            
            // Error message
            if (uiState.error != null && !uiState.isLoading) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(text = uiState.error!!)
                }
            }
        }
    }
    
    // Status selection dialog
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Update Status") },
            text = {
                Column {
                    TaskStatus.values().forEach { status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    viewModel.updateTaskStatus(status)
                                    showStatusDialog = false
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.task?.status == status,
                                onClick = {
                                    viewModel.updateTaskStatus(status)
                                    showStatusDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TaskStatusChip(status = status)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Priority selection dialog
    if (showPriorityDialog) {
        AlertDialog(
            onDismissRequest = { showPriorityDialog = false },
            title = { Text("Update Priority") },
            text = {
                Column {
                    TaskPriority.values().forEach { priority ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    viewModel.updateTaskPriority(priority)
                                    showPriorityDialog = false
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.task?.priority == priority,
                                onClick = {
                                    viewModel.updateTaskPriority(priority)
                                    showPriorityDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PriorityChip(priority = priority)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPriorityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Date picker
    if (showDatePicker) {
        ProjectDatePicker(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = {
                viewModel.updateTaskDueDate(it)
                showDatePicker = false
            }
        )
    }
    
    // Add subtask dialog
    if (showAddSubtaskDialog) {
        AddSubtaskDialog(
            onDismiss = { showAddSubtaskDialog = false },
            onAddSubtask = { title, description, dueDate ->
                viewModel.addSubtask(title, description, dueDate)
                showAddSubtaskDialog = false
            }
        )
    }
    
    // Edit title dialog
    if (showEditTitleDialog && task != null) {
        EditTextFieldDialog(
            title = "Edit Task Title",
            initialValue = task.title,
            onDismiss = { showEditTitleDialog = false },
            onConfirm = { newTitle ->
                viewModel.updateTaskTitle(newTitle)
                showEditTitleDialog = false
            }
        )
    }
    
    // Edit description dialog
    if (showEditDescriptionDialog && task != null) {
        EditTextFieldDialog(
            title = "Edit Description",
            initialValue = task.description,
            singleLine = false,
            onDismiss = { showEditDescriptionDialog = false },
            onConfirm = { newDescription ->
                viewModel.updateTaskDescription(newDescription)
                showEditDescriptionDialog = false
            }
        )
    }
}

@Composable
fun SubtaskItem(
    subtask: Task,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = subtask.isCompleted,
            onCheckedChange = { onToggleComplete() }
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subtask.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (subtask.dueDate != null) {
                Text(
                    text = "Due: ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(subtask.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (subtask.isOverdue) MaterialTheme.colorScheme.error 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        TaskStatusChip(status = subtask.status)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubtaskDialog(
    onDismiss: () -> Unit,
    onAddSubtask: (title: String, description: String, dueDate: Date?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add Subtask",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dueDate?.let { 
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it) 
                            } ?: "Set due date (optional)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                        onClick = { onAddSubtask(title, description, dueDate) },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
    
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTextFieldDialog(
    title: String,
    initialValue: String,
    singleLine: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine,
                minLines = if (singleLine) 1 else 3,
                maxLines = if (singleLine) 1 else 5
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(value) },
                enabled = value.isNotBlank()
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
