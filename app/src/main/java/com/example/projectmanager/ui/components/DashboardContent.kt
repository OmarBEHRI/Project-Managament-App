package com.example.projectmanager.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.ui.home.HomeViewModel
import com.example.projectmanager.ui.home.ProjectStats

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onProjectClick: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onNavigateToAnalytics: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Use the enhanced DashboardScreen with analytics
    val dashboardViewModel = hiltViewModel<com.example.projectmanager.ui.dashboard.DashboardViewModel>()

    // Get the context for toast messages
    val context = LocalContext.current
    
    // Create a simple AppNavigator implementation for the DashboardScreen
    val appNavigator = object : com.example.projectmanager.navigation.AppNavigator {
        override fun navigateBack() {}
        override fun navigateToSignIn() {}
        override fun navigateToSignUp() {}
        override fun navigateToForgotPassword() {}
        override fun navigateToHome() {}
        override fun navigateToProjects() {}
        override fun navigateToProject(projectId: String) { onProjectClick(projectId) }
        override fun navigateToTasks() {}
        override fun navigateToTask(taskId: String) { onTaskClick(taskId) }
        override fun navigateToProfile() {}
        override fun navigateToSettings() {}
        override fun navigateToCreateProject() {}
        override fun navigateToCreateTask(projectId: String?) {}
        override fun navigateToEditProject(projectId: String) {}
        override fun navigateToEditTask(taskId: String) {}
        override fun navigateToAnalyticsDashboard() {
            // Call the navigation callback to go to the Analytics Dashboard
            onNavigateToAnalytics()
        }
        override fun isUserSignedIn(): Boolean = true
        
        // Chat navigation methods
        override fun navigateToChats() {}
        override fun navigateToChat(chatId: String) {}
        override fun navigateToNewChat(projectId: String?) {}
        override fun navigateToChatSettings(chatId: String) {}
    }

    // Use our enhanced DashboardScreen
    com.example.projectmanager.ui.dashboard.DashboardScreen(
        viewModel = dashboardViewModel,
        appNavigator = appNavigator
    )
}

@Composable
fun ProjectStatsSection(stats: ProjectStats) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Project Statistics",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Changed from "Total Projects" to "Projects" to avoid duplication
                StatItem(
                    icon = Icons.Default.Folder,
                    label = "Projects",
                    value = stats.totalProjects.toString()
                )
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Completed",
                    value = stats.completedProjects.toString()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Default.Assignment,
                    label = "Total Tasks",
                    value = stats.totalTasks.toString()
                )
                StatItem(
                    icon = Icons.Default.Done,
                    label = "Completed",
                    value = stats.completedTasks.toString()
                )
                StatItem(
                    icon = Icons.Default.Warning,
                    label = "Overdue",
                    value = stats.overdueTasksCount.toString(),
                    valueColor = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyStateMessage(
    icon: ImageVector,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}