package com.example.projectmanager.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskStatus
import com.example.projectmanager.ui.components.TaskListItem
import com.example.projectmanager.ui.project.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    projectId: String,
    onNavigateToTaskDetails: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProjectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tasks = uiState.tasks
    val project = uiState.project
    
    // Filter states
    var selectedStatusFilter by remember { mutableStateOf<TaskStatus?>(null) }
    var showCompletedTasks by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Load project and tasks when the screen is first displayed
    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: "Tasks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCompletedTasks = !showCompletedTasks }) {
                        Icon(
                            imageVector = if (showCompletedTasks) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                            contentDescription = if (showCompletedTasks) "Hide completed tasks" else "Show completed tasks"
                        )
                    }
                    
                    IconButton(onClick = { /* Show filter dialog */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter tasks")
                    }
                }
            )
        },
        floatingActionButton = {
            if (project != null) {
                FloatingActionButton(
                    onClick = { /* Show create task dialog */ }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add task")
                }
            }
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
            } else if (tasks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No tasks found",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Create a new task to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { /* Show create task dialog */ }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Task")
                    }
                }
            } else {
                // Apply filters
                val filteredTasks = tasks.filter { task ->
                    (showCompletedTasks || !task.isCompleted) &&
                    (selectedStatusFilter == null || task.status == selectedStatusFilter) &&
                    (searchQuery.isEmpty() || task.title.contains(searchQuery, ignoreCase = true) || 
                     task.description.contains(searchQuery, ignoreCase = true))
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTasks) { task ->
                        TaskListItem(
                            task = task,
                            onClick = { onNavigateToTaskDetails(task.id) }
                        )
                    }
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
}

@Composable
fun TaskFilterDialog(
    onDismiss: () -> Unit,
    onFilterSelected: (TaskStatus?) -> Unit,
    selectedFilter: TaskStatus?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Tasks") },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedFilter == null,
                        onClick = { onFilterSelected(null) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("All")
                }
                
                TaskStatus.values().forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFilter == status,
                            onClick = { onFilterSelected(status) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(status.name.replace('_', ' '))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
