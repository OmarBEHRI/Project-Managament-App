package com.example.projectmanager.ui.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import com.example.projectmanager.ui.project.AiTaskGenerationDialog
import com.example.projectmanager.ui.project.AiTaskAssignmentDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.data.model.*
import com.example.projectmanager.navigation.AppNavigator
import com.example.projectmanager.ui.components.ProjectDatePicker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectScreen(
    projectId: String,
    viewModel: ProjectViewModel = hiltViewModel(),
    appNavigator: AppNavigator
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var showAiTaskGenerationDialog by remember { mutableStateOf(false) }
    var showAiTaskAssignmentDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Overview", "Tasks", "Members", "Comments", "Attachments")
    
    // Check if current user is the project manager
    val currentUserIsManager = remember(uiState.project) {
        viewModel.isCurrentUserManager()
    }

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.project?.name ?: "Project Details",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { appNavigator.navigateBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Only show FAB if user is a manager and we're on the Tasks tab
            if (currentUserIsManager && selectedTabIndex == 1 && !uiState.isLoading && uiState.project != null) {
                var showMenu by remember { mutableStateOf(false) }
                
                Box {
                    FloatingActionButton(
                        onClick = { showMenu = true },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Task Options",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.width(200.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add Task Manually") },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                            onClick = {
                                showCreateTaskDialog = true
                                showMenu = false
                            }
                        )
                        
                        // Only show AI task generation if there are no tasks yet
                        if (uiState.tasks.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Generate Tasks with AI") },
                                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                                onClick = {
                                    showAiTaskGenerationDialog = true
                                    showMenu = false
                                }
                            )
                        }
                        
                        // Only show AI task assignment if there are tasks and team members
                        if (uiState.tasks.isNotEmpty() && uiState.members.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Assign Tasks with AI") },
                                leadingIcon = { Icon(Icons.Default.AssignmentInd, contentDescription = null) },
                                onClick = {
                                    showAiTaskAssignmentDialog = true
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                uiState.project != null -> {
                    val project = uiState.project!!
                    
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Project header with summary info
                        ProjectHeader(
                            project = project,
                            taskStats = uiState.taskStats
                        )
                        
                        // Tab navigation
                        ScrollableTabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            edgePadding = 0.dp
                        ) {
                            tabTitles.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { 
                                        Text(
                                            text = title,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        ) 
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = when (index) {
                                                0 -> Icons.Outlined.Dashboard
                                                1 -> Icons.Outlined.Assignment
                                                2 -> Icons.Outlined.People
                                                3 -> Icons.Outlined.Comment
                                                4 -> Icons.Outlined.Attachment
                                                else -> Icons.Outlined.Info
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    modifier = Modifier.height(56.dp)
                                )
                            }
                        }
                        
                        // Tab content
                        when (selectedTabIndex) {
                            0 -> ProjectOverviewTab(project = project)
                            1 -> ProjectTasksTabComponent(
                                tasks = uiState.tasks,
                                onTaskClick = { taskId -> appNavigator.navigateToTask(taskId) }
                            )
                            2 -> ProjectMembersTab(
                                project = project,
                                members = uiState.members,
                                userSuggestions = uiState.userSuggestions,
                                searchQuery = uiState.searchQuery,
                                isCurrentUserManager = currentUserIsManager,
                                onSearchQueryChange = { query -> viewModel.searchUsers(query) },
                                onAddMember = { user, role -> viewModel.addMemberToProject(user, role) },
                                onRemoveMember = { userId -> viewModel.removeMemberFromProject(userId) },
                                onClearSearch = { viewModel.clearSearch() }
                            )
                            3 -> ProjectCommentsTab(
                                project = project,
                                comments = uiState.comments,
                                onAddComment = { content -> viewModel.addComment(content) }
                            )
                            4 -> ProjectAttachmentsTab(
                                project = project,
                                attachments = uiState.attachments,
                                onUploadAttachment = { name, size, mimeType, uri ->
                                    viewModel.uploadAttachment(name, size, mimeType, uri)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Create task dialog
    if (showCreateTaskDialog && uiState.project != null) {
        CreateTaskDialog(
            project = uiState.project!!,
            availableMembers = uiState.members,
            onDismiss = { showCreateTaskDialog = false },
            onCreateTask = { task ->
                viewModel.createTask(task)
                showCreateTaskDialog = false
            }
        )
    }
    
    // AI Task Generation Dialog
    if (showAiTaskGenerationDialog) {
        AiTaskGenerationDialog(
            isGenerating = uiState.isGeneratingTasks,
            generatedTasks = uiState.generatedTasks,
            error = uiState.taskGenerationError,
            onGenerateTasks = { viewModel.generateTasksWithAi() },
            onSaveTasks = { 
                viewModel.saveGeneratedTasks()
                showAiTaskGenerationDialog = false
            },
            onDismiss = { showAiTaskGenerationDialog = false }
        )
    }
    
    // AI Task Assignment Dialog
    if (showAiTaskAssignmentDialog) {
        AiTaskAssignmentDialog(
            isAssigning = uiState.isAssigningTasks,
            taskAssignments = uiState.taskAssignments,
            tasks = uiState.tasks,
            members = uiState.members,
            error = uiState.taskAssignmentError,
            onAssignTasks = { viewModel.assignTasksWithAi() },
            onSaveAssignments = { 
                viewModel.saveTaskAssignments()
                showAiTaskAssignmentDialog = false
            },
            onDismiss = { showAiTaskAssignmentDialog = false }
        )
    }
    
    // Show error message if there is one
    if (uiState.error != null) {
        LaunchedEffect(uiState.error) {
            // Clear error after showing it
            // You could add a Snackbar here to show the error
        }
    }
}