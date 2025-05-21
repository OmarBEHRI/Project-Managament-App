package com.example.projectmanager.ui.task

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.data.model.*
import com.example.projectmanager.ui.components.*
import java.util.*
import java.text.SimpleDateFormat
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    taskId: String,
    projectId: String,
    viewModel: TaskDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToSubtask: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(taskId, projectId) {
        viewModel.loadTask(taskId, projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.task?.title ?: "Task Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Task")
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error!!)
                }
            }
            uiState.task != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Task header
                    TaskHeader(
                        task = uiState.task!!,
                        onStatusChange = { viewModel.updateStatus(it) },
                        onPriorityChange = { viewModel.updatePriority(it) }
                    )

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    // Task details
                    TaskDetailsSection(
                        task = uiState.task!!,
                        onAssigneeClick = { /* Navigate to user profile */ }
                    )

                    if (uiState.task!!.subtasks.isNotEmpty()) {
                        Divider(modifier = Modifier.padding(vertical = 16.dp))

                        // Subtasks
                        SubtasksSection(
                            subtasks = uiState.task!!.subtasks,
                            onSubtaskClick = onNavigateToSubtask
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    // Checklists
                    ChecklistsSection(
                        checklists = uiState.task!!.checklists,
                        onChecklistItemToggle = { checklistId, itemId, isCompleted ->
                            viewModel.toggleChecklistItem(checklistId, itemId, isCompleted)
                        },
                        onAddChecklistItem = { checklistId, text ->
                            viewModel.addChecklistItem(checklistId, text)
                        },
                        onDeleteChecklistItem = { checklistId, itemId ->
                            viewModel.deleteChecklistItem(checklistId, itemId)
                        }
                    )

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    // Comments and attachments
                    CommentsAndAttachments(
                        comments = uiState.task!!.comments,
                        attachments = uiState.task!!.attachments,
                        onAddComment = { text ->
                            viewModel.addComment(text)
                        },
                        onAddAttachment = { uri ->
                            viewModel.addAttachment(uri)
                        },
                        onDownloadAttachment = { attachment ->
                            viewModel.downloadAttachment(attachment)
                        },
                        onDeleteComment = { comment ->
                            viewModel.deleteComment(comment)
                        },
                        onDeleteAttachment = { attachment ->
                            viewModel.deleteAttachment(attachment)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }
            }
        }

        // Edit dialog
        if (showEditDialog) {
            // Date picker state for the dialog
            var showDatePickerDialog by remember { mutableStateOf(false) }
            var dateToSelect by remember { mutableStateOf<Date?>(null) }
            var onDateSelectedCallback by remember { mutableStateOf<((Date) -> Unit)?>(null) }

            EditTaskDialog(
                task = uiState.task!!,
                onDismiss = { showEditDialog = false },
                onSave = { updatedTask ->
                    viewModel.updateTask(updatedTask)
                    showEditDialog = false
                },
                showDatePicker = { currentDate, onDateSelected ->
                    // Save callback for when date picker is shown
                    dateToSelect = currentDate
                    onDateSelectedCallback = onDateSelected
                    showDatePickerDialog = true
                }
            )

            // Show date picker if requested
            if (showDatePickerDialog && onDateSelectedCallback != null) {
                ProjectDatePicker(
                    onDismissRequest = { showDatePickerDialog = false },
                    onDateSelected = {
                        onDateSelectedCallback?.invoke(it)
                        showDatePickerDialog = false
                    }
                )
            }
        }

        // Delete confirmation dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Task") },
                text = { Text("Are you sure you want to delete this task?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTask()
                            showDeleteConfirmation = false
                            onNavigateBack()
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TaskHeader(
    task: Task,
    onStatusChange: (TaskStatus) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Task completion status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Modern checkbox design
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = if (task.isCompleted) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (task.isCompleted) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = if (task.isCompleted) "Completed" else "In Progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (task.isCompleted) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status and priority chips in a horizontal scrollable row
            SingleRowChips(
                task = task,
                onStatusChange = onStatusChange,
                onPriorityChange = onPriorityChange
            )
            
            // Due date with calendar icon
            task.dueDate?.let { dueDate ->
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = if (task.isOverdue) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Due ${formatDate(dueDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (task.isOverdue) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SingleRowChips(
    task: Task,
    onStatusChange: (TaskStatus) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status chip
        SuggestionChip(
            onClick = { /* Show status menu */ },
            label = { Text(task.status.name.replace('_', ' ')) },
            icon = {
                Icon(
                    imageVector = when (task.status) {
                        TaskStatus.TODO -> Icons.Default.RadioButtonUnchecked
                        TaskStatus.IN_PROGRESS -> Icons.Default.PlayCircleOutline
                        TaskStatus.REVIEW -> Icons.Default.PauseCircleOutline
                        TaskStatus.COMPLETED -> Icons.Default.CheckCircle
                        TaskStatus.BLOCKED -> Icons.Default.Block
                        TaskStatus.CANCELLED -> Icons.Default.Cancel
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )

        // Priority chip
        SuggestionChip(
            onClick = { /* Show priority menu */ },
            label = { Text(task.priority.name) },
            icon = {
                Icon(
                    imageVector = when (task.priority) {
                        TaskPriority.LOW -> Icons.Default.ArrowDownward
                        TaskPriority.MEDIUM -> Icons.Default.Remove
                        TaskPriority.HIGH -> Icons.Default.ArrowUpward
                        TaskPriority.URGENT -> Icons.Default.PriorityHigh
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = when (task.priority) {
                    TaskPriority.LOW -> MaterialTheme.colorScheme.tertiaryContainer
                    TaskPriority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                    TaskPriority.HIGH -> MaterialTheme.colorScheme.errorContainer
                    TaskPriority.URGENT -> MaterialTheme.colorScheme.errorContainer
                },
                labelColor = when (task.priority) {
                    TaskPriority.LOW -> MaterialTheme.colorScheme.onTertiaryContainer
                    TaskPriority.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                    TaskPriority.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                    TaskPriority.URGENT -> MaterialTheme.colorScheme.onErrorContainer
                },
                iconContentColor = when (task.priority) {
                    TaskPriority.LOW -> MaterialTheme.colorScheme.onTertiaryContainer
                    TaskPriority.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                    TaskPriority.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                    TaskPriority.URGENT -> MaterialTheme.colorScheme.onErrorContainer
                }
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskDetailsSection(
    task: Task,
    onAssigneeClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Description section with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Description text in a surface with rounded corners
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = task.description.ifEmpty { "No description provided" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Assignees section
            if (task.assignedTo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Assignees header with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Assignees",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Assignee chips in a flow layout
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    task.assignedTo.forEach { userId ->
                        SuggestionChip(
                            onClick = { onAssigneeClick(userId) },
                            label = { Text(userId) },
                            icon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubtasksSection(
    subtasks: List<Task>,
    onSubtaskClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Subtasks header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SubdirectoryArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Subtasks",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${subtasks.count { it.isCompleted }}/${subtasks.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Divider for visual separation
            Divider(modifier = Modifier.padding(bottom = 8.dp))
            
            // Subtask list with modern styling
            subtasks.forEach { subtask ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSubtaskClick(subtask.id) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (subtask.isCompleted) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Modern checkbox design
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = if (subtask.isCompleted) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.surface,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (subtask.isCompleted) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (subtask.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = subtask.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (subtask.isCompleted) 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) 
                                else 
                                    MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (subtask.isCompleted) 
                                    TextDecoration.LineThrough 
                                else 
                                    TextDecoration.None
                            )
                            
                            if (subtask.dueDate != null) {
                                Text(
                                    text = "Due ${formatDate(subtask.dueDate!!)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (subtask.isOverdue) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return formatter.format(date)
} 