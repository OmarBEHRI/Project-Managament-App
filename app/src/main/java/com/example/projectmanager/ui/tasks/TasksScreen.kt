package com.example.projectmanager.ui.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskStatus
import com.example.projectmanager.data.model.TaskPriority
import com.example.projectmanager.ui.components.ProjectDatePicker
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel = hiltViewModel(),
    onTaskClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf<String?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    // Add Task icon removed as requested
                }
            )
        },
        // Floating action button removed as requested
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                uiState.tasks.isEmpty() -> {
                    EmptyTasksMessage(
                        modifier = Modifier.align(Alignment.Center),
                        onCreateClick = { showCreateDialog = true }
                    )
                }
                else -> {
                    TaskList(
                        tasks = uiState.tasks,
                        onTaskClick = onTaskClick,
                        onDeleteTask = { taskId -> 
                            showDeleteConfirmation = taskId
                        }
                    )
                }
            }
        }
    }

    // Create task dialog
    if (showCreateDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateDialog = false },
            onCreateTask = { task ->
                viewModel.createTask(task)
                showCreateDialog = false
            }
        )
    }

    // Delete confirmation dialog
    showDeleteConfirmation?.let { taskId ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(taskId)
                        showDeleteConfirmation = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Filter sheet
    if (showFilterSheet) {
        TaskFilterSheet(
            currentFilter = uiState.filter,
            onFilterChange = { filter ->
                viewModel.updateFilter(filter)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskList(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = tasks,
            key = { it.id }
        ) { task ->
            val dismissState = rememberSwipeToDismissBoxState(
                positionalThreshold = { _ -> 0.5f },
                confirmValueChange = { dismissValue ->
                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                        onDeleteTask(task.id)
                        true
                    } else {
                        false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                enableDismissFromEndToStart = true,
                backgroundContent = {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                },
                content = {
                    // Using a simple Card instead of TaskListItem since we need to implement it
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTaskClick(task.id) }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Priority indicator
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = when (task.priority) {
                                                TaskPriority.LOW -> MaterialTheme.colorScheme.tertiary
                                                TaskPriority.MEDIUM -> MaterialTheme.colorScheme.secondary
                                                TaskPriority.HIGH -> MaterialTheme.colorScheme.primary
                                                TaskPriority.URGENT -> MaterialTheme.colorScheme.error
                                            },
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            if (task.description.isNotBlank()) {
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status chip
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(task.status.name) }
                                )
                                
                                // Due date
                                task.dueDate?.let { date ->
                                    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                                    Text(
                                        text = formatter.format(date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (task.isOverdue) MaterialTheme.colorScheme.error 
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyTasksMessage(
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Assignment,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No tasks assigned to you",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tasks assigned to you will appear here",
            style = MaterialTheme.typography.bodyMedium
        )
        // Create Task button removed as requested
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onCreateTask: (Task) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
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
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Priority")
                    PrioritySelector(
                        selected = priority,
                        onPrioritySelected = { priority = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dueDate?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                    } ?: "Set Due Date")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreateTask(
                            Task(
                                title = title,
                                description = description,
                                priority = priority,
                                dueDate = dueDate,
                                status = TaskStatus.TODO
                            )
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskFilterSheet(
    currentFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var filter by remember { mutableStateOf(currentFilter) }

    @OptIn(ExperimentalMaterial3Api::class)
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Filter Tasks",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Status",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskStatus.values().forEach { status ->
                    FilterChip(
                        selected = filter.status == status,
                        onClick = {
                            filter = filter.copy(status = if (filter.status == status) null else status)
                        },
                        label = { Text(status.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Priority",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.values().forEach { priority ->
                    FilterChip(
                        selected = filter.priority == priority,
                        onClick = {
                            filter = filter.copy(priority = if (filter.priority == priority) null else priority)
                        },
                        label = { Text(priority.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        filter = TaskFilter()
                        onFilterChange(filter)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
                Button(
                    onClick = { onFilterChange(filter) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PrioritySelector(
    selected: TaskPriority,
    onPrioritySelected: (TaskPriority) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskPriority.values().forEach { priority ->
            FilterChip(
                selected = priority == selected,
                onClick = { onPrioritySelected(priority) },
                label = { Text(priority.name) },
                leadingIcon = {
                    val icon = when (priority) {
                        TaskPriority.LOW -> Icons.Default.ArrowDownward
                        TaskPriority.MEDIUM -> Icons.Default.Remove
                        TaskPriority.HIGH -> Icons.Default.ArrowUpward
                        TaskPriority.URGENT -> Icons.Default.PriorityHigh
                    }
                    Icon(icon, contentDescription = null)
                }
            )
        }
    }
} 