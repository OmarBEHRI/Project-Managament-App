package com.example.projectmanager.ui.projects

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.data.model.*
import com.example.projectmanager.navigation.AppNavigator
import com.example.projectmanager.navigation.MainScreenNavigator
import com.example.projectmanager.ui.components.BottomNavBar
import com.example.projectmanager.ui.components.ProjectDatePicker
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel = hiltViewModel(),
    onProjectClick: (String) -> Unit = {},
    appNavigator: AppNavigator? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    
    // Track if we're currently searching
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProjects()
    }
    
    // Update filtered projects when search query changes
    LaunchedEffect(searchQuery) {
        viewModel.searchProjects(searchQuery)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Projects") },
                    navigationIcon = {
                        if (appNavigator != null && appNavigator !is MainScreenNavigator) {
                            IconButton(onClick = { appNavigator.navigateBack() }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearching = !isSearching }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Projects"
                            )
                        }
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Project")
                        }
                    }
                )
                
                // Search bar
                AnimatedVisibility(
                    visible = isSearching,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = {
                            keyboardController?.hide()
                        },
                        active = false,
                        onActiveChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .focusRequester(focusRequester),
                        placeholder = { Text("Search by name, description or tags") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        }
                    ) {}
                }
                
                // Project stats summary
                if (!isSearching && uiState.projects.isNotEmpty()) {
                    ProjectStatsSummary(projects = uiState.projects)
                }
            }
        },
        bottomBar = {
            // Only show bottom navigation if we're on the main Projects screen
            if (appNavigator != null && appNavigator is MainScreenNavigator) {
                BottomNavBar(
                    currentRoute = "projects",
                    appNavigator = appNavigator
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Project",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
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
                uiState.projects.isEmpty() -> {
                    EmptyProjectsMessage(
                        modifier = Modifier.align(Alignment.Center),
                        onCreateClick = { showCreateDialog = true }
                    )
                }
                uiState.filteredProjects.isEmpty() && searchQuery.isNotEmpty() -> {
                    // No search results
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No projects found matching '$searchQuery'",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { searchQuery = "" }) {
                            Text("Clear Search")
                        }
                    }
                }
                else -> {
                    ModernProjectGrid(
                        projects = if (searchQuery.isEmpty()) uiState.projects else uiState.filteredProjects,
                        onProjectClick = onProjectClick,
                        onDeleteProject = { projectId -> 
                            showDeleteConfirmation = projectId
                        }
                    )
                }
            }
        }
    }

    // Create project dialog
    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreateProject = { project ->
                viewModel.createProject(project)
                showCreateDialog = false
            }
        )
    }

    // Delete confirmation dialog
    showDeleteConfirmation?.let { projectId ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text("Delete Project") },
            text = { Text("Are you sure you want to delete this project? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(projectId)
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
}

@Composable
fun ProjectStatsSummary(projects: List<Project>) {
    val totalProjects = projects.size
    val completedProjects = projects.count { it.status == ProjectStatus.COMPLETED }
    val inProgressProjects = projects.count { it.status == ProjectStatus.IN_PROGRESS }
    val upcomingDeadlines = projects.count { 
        it.deadline != null && 
        it.deadline!!.time > System.currentTimeMillis() && 
        it.deadline!!.time < System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000 // 7 days
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                count = totalProjects,
                label = "Total",
                icon = Icons.Outlined.Folder
            )
            StatItem(
                count = inProgressProjects,
                label = "In Progress",
                icon = Icons.Outlined.PlayArrow
            )
            StatItem(
                count = completedProjects,
                label = "Completed",
                icon = Icons.Outlined.Done
            )
            StatItem(
                count = upcomingDeadlines,
                label = "Due Soon",
                icon = Icons.Outlined.Schedule
            )
        }
    }
}

@Composable
fun StatItem(count: Int, label: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ModernProjectGrid(
    projects: List<Project>,
    onProjectClick: (String) -> Unit,
    onDeleteProject: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(projects, key = { it.id }) { project ->
            ModernProjectCard(
                project = project,
                onClick = { onProjectClick(project.id) },
                onDelete = { onDeleteProject(project.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernProjectCard(
    project: Project,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val isOverdue = project.deadline?.let { it.time < System.currentTimeMillis() } ?: false
    val progressPercentage = if (project.totalTasks > 0) {
        (project.completedTasks.toFloat() / project.totalTasks.toFloat()) * 100f
    } else 0f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with priority and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority indicator
                val priorityColor = when(project.priority) {
                    Priority.LOW -> Color(0xFF4CAF50)
                    Priority.MEDIUM -> Color(0xFFFFC107)
                    Priority.HIGH -> Color(0xFFFF9800)
                    Priority.URGENT -> Color(0xFFF44336)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(priorityColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = project.priority.name,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                
                // Status chip
                val statusColor = when(project.status) {
                    ProjectStatus.NOT_STARTED -> MaterialTheme.colorScheme.outline
                    ProjectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                    ProjectStatus.ON_HOLD -> MaterialTheme.colorScheme.tertiary
                    ProjectStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                    ProjectStatus.CANCELLED -> MaterialTheme.colorScheme.error
                    ProjectStatus.ARCHIVED -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.outline
                }
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = project.status.name.replace('_', ' '),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Project name
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Project description
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${project.completedTasks}/${project.totalTasks} tasks",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = progressPercentage / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        progressPercentage >= 100f -> MaterialTheme.colorScheme.secondary
                        progressPercentage >= 75f -> MaterialTheme.colorScheme.primary
                        progressPercentage >= 25f -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer with deadline and team members
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Deadline
                project.deadline?.let { deadline ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateFormatter.format(deadline),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Team members count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${project.members.size} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyProjectsMessage(
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No projects yet",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create your first project to get started",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCreateClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Project")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreateProject: (Project) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<Date?>(null) }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Project") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
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
                    Text(deadline?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                    } ?: "Set Deadline")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreateProject(
                            Project(
                                name = name,
                                description = description,
                                priority = priority,
                                deadline = deadline,
                                status = ProjectStatus.NOT_STARTED
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
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
                deadline = it
                showDatePicker = false
            }
        )
    }
}

@Composable
fun PrioritySelector(
    selected: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Priority.values().forEach { priority ->
            FilterChip(
                selected = priority == selected,
                onClick = { onPrioritySelected(priority) },
                label = { Text(priority.name) },
                leadingIcon = {
                    val icon = when (priority) {
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
} 