package com.example.projectmanager.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.navigation.AppNavigator
import com.example.projectmanager.ui.components.DashboardContent
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    appNavigator: AppNavigator
) {
    // Get the current route to determine which tab should be selected
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        appNavigator.navigateToProjects()
                    },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Projects") },
                    label = { Text("Projects") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        appNavigator.navigateToTasks()
                    },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Tasks") },
                    label = { Text("Tasks") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        appNavigator.navigateToChats()
                    },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("Chat") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        1 -> appNavigator.navigateToCreateProject()
                        2 -> appNavigator.navigateToCreateTask()
                        3 -> appNavigator.navigateToNewChat()
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> DashboardContent(
                modifier = Modifier.padding(padding),
                viewModel = viewModel,
                onProjectClick = { projectId ->
                    appNavigator.navigateToProject(projectId)
                },
                onTaskClick = { taskId ->
                    appNavigator.navigateToTask(taskId)
                },
                onNavigateToAnalytics = {
                    appNavigator.navigateToAnalyticsDashboard()
                }
            )
            // Projects and Tasks screens are handled by navigation
        }
    }
} 