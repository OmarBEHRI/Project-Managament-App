package com.example.projectmanager.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projectmanager.data.model.Project
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.ui.components.ProjectCard
import com.example.projectmanager.ui.components.TaskItem
import com.example.projectmanager.ui.theme.TrelloColors

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel,
    onProjectClick: (String) -> Unit,
    onTaskClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            WelcomeHeader(userName = uiState.userName)
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            StatisticsRow(
                totalProjects = uiState.totalProjects,
                completedProjects = uiState.completedProjects,
                pendingTasks = uiState.pendingTasks
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        item {
            SectionHeader(title = "Recent Projects")
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        items(uiState.recentProjects) { project ->
            ProjectCard(
                project = project,
                onClick = { onProjectClick(project.id) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Upcoming Tasks")
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        items(uiState.upcomingTasks) { task ->
            TaskItem(
                task = task,
                onClick = { onTaskClick(task.id) },
                onToggleCompletion = { viewModel.toggleTaskCompletion(task.id, !task.isCompleted) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Add some padding at the bottom
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun WelcomeHeader(userName: String) {
    Column {
        Text(
            text = "Welcome back,",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun StatisticsRow(
    totalProjects: Int,
    completedProjects: Int,
    pendingTasks: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Total Projects",
            value = totalProjects.toString(),
            icon = Icons.Default.Folder,
            color = TrelloColors.TrelloBlue,
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Completed",
            value = completedProjects.toString(),
            icon = Icons.Default.CheckCircle,
            color = TrelloColors.TrelloGreen,
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Pending Tasks",
            value = pendingTasks.toString(),
            icon = Icons.Default.Assignment,
            color = TrelloColors.TrelloPurple,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
