package com.example.projectmanager.ui.components

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.projectmanager.ui.home.HomeViewModel
import com.example.projectmanager.ui.home.ProjectStats

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onProjectClick: (String) -> Unit,
    onTaskClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome message
        item {
            Text(
                text = "Welcome back, ${uiState.user?.displayName ?: "User"}!",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        // Project Stats
        item {
            ProjectStatsSection(stats = uiState.projectStats)
        }

        // Recent Projects
        item {
            Text(
                text = "Recent Projects",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (uiState.recentProjects.isEmpty()) {
            item {
                EmptyStateMessage(
                    icon = Icons.Default.Folder,
                    message = "No recent projects"
                )
            }
        } else {
            items(uiState.recentProjects) { project ->
                ProjectListItem(
                    project = project,
                    onClick = { onProjectClick(project.id) }
                )
            }
        }

        // Pending Tasks
        item {
            Text(
                text = "Pending Tasks",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (uiState.pendingTasks.isEmpty()) {
            item {
                EmptyStateMessage(
                    icon = Icons.Default.Assignment,
                    message = "No pending tasks"
                )
            }
        } else {
            items(uiState.pendingTasks) { task ->
                TaskListItem(
                    task = task,
                    onClick = { onTaskClick(task.id) }
                )
            }
        }

        // Error message
        uiState.error?.let { error ->
            item {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
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
                StatItem(
                    icon = Icons.Default.Folder,
                    label = "Total Projects",
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