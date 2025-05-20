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
import com.example.projectmanager.ui.components.ProjectDatePicker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectScreen(
    projectId: String,
    viewModel: ProjectViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToTask: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateTaskDialog by remember { mutableStateOf(false) }
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
            ProjectDetailsTopBar(
                project = uiState.project,
                onBackClick = onNavigateBack,
                isLoading = uiState.isLoading
            )
        },
        floatingActionButton = {
            // Only show FAB if user is a manager and we're on the Tasks tab
            if (currentUserIsManager && selectedTabIndex == 1 && !uiState.isLoading && uiState.project != null) {
                FloatingActionButton(
                    onClick = { showCreateTaskDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Task",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
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
                        ProjectHeader(project = project)
                        
                        // Tab navigation
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            tabTitles.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { Text(title) },
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
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                        
                        // Tab content
                        when (selectedTabIndex) {
                            0 -> ProjectOverviewTab(project = project)
                            1 -> ProjectTasksTabComponent(
                                tasks = uiState.tasks,
                                onTaskClick = onNavigateToTask
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
            onDismiss = { showCreateTaskDialog = false },
            onCreateTask = { task ->
                viewModel.createTask(task)
                showCreateTaskDialog = false
            },
            availableMembers = uiState.members
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