package com.example.projectmanager.ui.task

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.data.model.*
import com.example.projectmanager.ui.components.*
import java.util.*
import java.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Status and priority
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = true,
                onClick = { /* Show status menu */ },
                label = { Text(task.status.name) },
                leadingIcon = {
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
                }
            )

            FilterChip(
                selected = true,
                onClick = { /* Show priority menu */ },
                label = { Text(task.priority.name) },
                leadingIcon = {
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
                }
            )
        }

        // Due date
        task.dueDate?.let { dueDate ->
            Spacer(modifier = Modifier.height(8.dp))
            AssistChip(
                onClick = { /* Show date picker */ },
                label = { Text("Due ${formatDate(dueDate)}") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskDetailsSection(
    task: Task,
    onAssigneeClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = task.description,
            style = MaterialTheme.typography.bodyMedium
        )

        if (task.assignedTo.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Assignees",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                task.assignedTo.forEach { userId ->
                    AssistChip(
                        onClick = { onAssigneeClick(userId) },
                        label = { Text(userId) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Subtasks",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        subtasks.forEach { subtask ->
            ListItem(
                headlineContent = { Text(subtask.title) },
                leadingContent = {
                    Checkbox(
                        checked = subtask.isCompleted,
                        onCheckedChange = null
                    )
                },
                modifier = Modifier.clickable { onSubtaskClick(subtask.id) }
            )
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return formatter.format(date)
} 