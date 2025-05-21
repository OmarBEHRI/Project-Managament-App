package com.example.projectmanager.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onTaskClick: (String) -> Unit = {},
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
                title = { 
                    Text(
                        "Task Details", 
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (task != null) {
                        IconButton(
                            onClick = { viewModel.toggleTaskCompletion() }
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) 
                                    Icons.Default.CheckCircle 
                                else 
                                    Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (task.isCompleted) "Mark as incomplete" else "Mark as complete",
                                tint = if (task.isCompleted) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                // Task details content with enhanced styling
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Task title with modern styling
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TITLE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            IconButton(
                                onClick = { showEditTitleDialog = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit title",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Status and priority with enhanced styling
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Status card with visual indicator
                        ElevatedCard(
                            onClick = { showStatusDialog = true },
                            modifier = Modifier.weight(1f),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = when(task.status) {
                                    TaskStatus.TODO -> MaterialTheme.colorScheme.secondaryContainer
                                    TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
                                    TaskStatus.REVIEW -> MaterialTheme.colorScheme.tertiaryContainer
                                    TaskStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
                                    TaskStatus.BLOCKED -> MaterialTheme.colorScheme.errorContainer
                                    TaskStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
                                }.copy(alpha = 0.7f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = when(task.status) {
                                        TaskStatus.TODO -> Icons.Default.Assignment
                                        TaskStatus.IN_PROGRESS -> Icons.Default.Timelapse
                                        TaskStatus.REVIEW -> Icons.Default.RateReview
                                        TaskStatus.COMPLETED -> Icons.Default.CheckCircle
                                        TaskStatus.BLOCKED -> Icons.Default.Block
                                        TaskStatus.CANCELLED -> Icons.Default.Cancel
                                    },
                                    contentDescription = null,
                                    tint = when(task.status) {
                                        TaskStatus.TODO -> MaterialTheme.colorScheme.onSecondaryContainer
                                        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onPrimaryContainer
                                        TaskStatus.REVIEW -> MaterialTheme.colorScheme.onTertiaryContainer
                                        TaskStatus.COMPLETED -> MaterialTheme.colorScheme.onSurfaceVariant
                                        TaskStatus.BLOCKED -> MaterialTheme.colorScheme.onErrorContainer
                                        TaskStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Status",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.status.name.replace('_', ' '),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when(task.status) {
                                        TaskStatus.TODO -> MaterialTheme.colorScheme.onSecondaryContainer
                                        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onPrimaryContainer
                                        TaskStatus.REVIEW -> MaterialTheme.colorScheme.onTertiaryContainer
                                        TaskStatus.COMPLETED -> MaterialTheme.colorScheme.onSurfaceVariant
                                        TaskStatus.BLOCKED -> MaterialTheme.colorScheme.onErrorContainer
                                        TaskStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                        
                        // Priority card with visual indicator
                        ElevatedCard(
                            onClick = { showPriorityDialog = true },
                            modifier = Modifier.weight(1f),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = when(task.priority) {
                                    TaskPriority.LOW -> MaterialTheme.colorScheme.surfaceVariant
                                    TaskPriority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                                    TaskPriority.HIGH -> MaterialTheme.colorScheme.primaryContainer
                                    TaskPriority.URGENT -> MaterialTheme.colorScheme.errorContainer
                                }.copy(alpha = 0.7f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = when(task.priority) {
                                        TaskPriority.LOW -> Icons.Default.ArrowDownward
                                        TaskPriority.MEDIUM -> Icons.Default.Remove
                                        TaskPriority.HIGH -> Icons.Default.ArrowUpward
                                        TaskPriority.URGENT -> Icons.Default.PriorityHigh
                                    },
                                    contentDescription = null,
                                    tint = when(task.priority) {
                                        TaskPriority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
                                        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                                        TaskPriority.HIGH -> MaterialTheme.colorScheme.onPrimaryContainer
                                        TaskPriority.URGENT -> MaterialTheme.colorScheme.onErrorContainer
                                    },
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Priority",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.priority.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when(task.priority) {
                                        TaskPriority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
                                        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                                        TaskPriority.HIGH -> MaterialTheme.colorScheme.onPrimaryContainer
                                        TaskPriority.URGENT -> MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Due date with enhanced styling
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ElevatedCard(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (task.isOverdue) 
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                            else 
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = if (task.isOverdue) 
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                        else 
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (task.isOverdue) 
                                        Icons.Default.Warning
                                    else 
                                        Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = if (task.isOverdue) 
                                        MaterialTheme.colorScheme.error
                                    else 
                                        MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DUE DATE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (task.isOverdue) 
                                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                    else 
                                        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.dueDate?.let { 
                                        SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(it) 
                                    } ?: "No due date set",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (task.isOverdue) 
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else 
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                
                                // Add time remaining if due date exists
                                task.dueDate?.let { dueDate ->
                                    if (!task.isCompleted) {
                                        val currentTime = System.currentTimeMillis()
                                        val dueTime = dueDate.time
                                        val timeRemaining = dueTime - currentTime
                                        
                                        if (timeRemaining > 0) {
                                            val daysRemaining = timeRemaining / (1000 * 60 * 60 * 24)
                                            val hoursRemaining = (timeRemaining % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "$daysRemaining days, $hoursRemaining hours remaining",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                            )
                                        } else {
                                            val daysOverdue = -timeRemaining / (1000 * 60 * 60 * 24)
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Overdue by $daysOverdue days",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }
                            }
                            IconButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = if (task.isOverdue) 
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                        else 
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit due date",
                                    tint = if (task.isOverdue) 
                                        MaterialTheme.colorScheme.error
                                    else 
                                        MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Project Information section
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ElevatedCard(
                        onClick = { task.projectId.takeIf { it.isNotEmpty() }?.let { onNavigateToProject(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PROJECT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = viewModel.projectName.value.ifEmpty { "Loading project..." },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            IconButton(
                                onClick = { task.projectId.takeIf { it.isNotEmpty() }?.let { onNavigateToProject(it) } },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Go to project",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    
                    // Description with enhanced styling
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ElevatedCard(
                        onClick = { showEditDescriptionDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
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
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "DESCRIPTION",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Task Details",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                IconButton(
                                    onClick = { showEditDescriptionDialog = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit description",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = task.description.ifBlank { "No description provided" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (task.description.isBlank()) 
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
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
                    
                    // Subtasks with enhanced styling
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
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
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FormatListBulleted,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "SUBTASKS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${subtasks.size} Subtasks",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                FilledTonalIconButton(
                                    onClick = { showAddSubtaskDialog = true },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add subtask",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (subtasks.isEmpty()) {
                                // Empty state with illustration
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlaylistAdd,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "No subtasks yet",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Break down this task into smaller steps by adding subtasks",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        FilledTonalButton(
                                            onClick = { showAddSubtaskDialog = true },
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Add Subtask")
                                        }
                                    }
                                }
                            } else {
                                // List of subtasks with enhanced styling
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        subtasks.forEachIndexed { index, subtask ->
                                            SubtaskItem(
                                                subtask = subtask,
                                                onToggleComplete = { viewModel.toggleSubtaskCompletion(subtask.id) },
                                                onClick = { onTaskClick(subtask.id) }
                                            )
                                            if (index < subtasks.size - 1) {
                                                Divider(
                                                    modifier = Modifier.padding(horizontal = 16.dp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                                )
                                            }
                                        }
                                    }
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
