package com.example.projectmanager.ui.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.projectmanager.data.model.*
import com.example.projectmanager.ui.components.ProjectDatePicker
import java.text.SimpleDateFormat
import java.util.*

// Top bar for project details screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsTopBar(
    project: Project?,
    onBackClick: () -> Unit,
    isLoading: Boolean
) {
    TopAppBar(
        title = { 
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Loading project...")
                    Spacer(modifier = Modifier.width(16.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                Text(text = project?.name ?: "Project Details")
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (project != null) {
                IconButton(onClick = { /* Share project */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share Project")
                }
                IconButton(onClick = { /* More options */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

// Project header with summary info
@Composable
fun ProjectHeader(
    project: Project,
    taskStats: TaskStats = TaskStats()
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val isOverdue = project.deadline?.let { it.time < System.currentTimeMillis() } ?: false
    
    // Use the taskStats for progress calculation (excluding canceled tasks)
    val progressPercentage = taskStats.completedPercentage
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Project description
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Project stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Priority
                Column {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PriorityChip(priority = project.priority)
                }
                
                // Status
                Column {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusChip(status = project.status)
                }
                
                // Deadline
                Column {
                    Text(
                        text = "Deadline",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    project.deadline?.let { deadline ->
                        Text(
                            text = dateFormatter.format(deadline),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    } ?: Text(
                        text = "No deadline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${taskStats.completed}/${taskStats.active} active tasks (${progressPercentage.toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Outlined green progress bar with rounded corners
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF4CAF50), // Material Green
                            shape = RoundedCornerShape(6.dp)
                        )
                        .background(Color(0xFFEEEEEE)) // Light gray background
                        .padding(1.dp) // Padding to ensure progress doesn't overlap border
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressPercentage / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF4CAF50)) // Material Green
                    )
                }
                
                // Add a small note about canceled tasks if there are any
                if (taskStats.total > taskStats.active) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${taskStats.total - taskStats.active} canceled tasks excluded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

// Priority chip component
@Composable
fun PriorityChip(priority: Priority) {
    val priorityColor = when(priority) {
        Priority.LOW -> Color(0xFF4CAF50)
        Priority.MEDIUM -> Color(0xFFFFC107)
        Priority.HIGH -> Color(0xFFFF9800)
        Priority.URGENT -> Color(0xFFF44336)
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = priorityColor.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(priorityColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = priority.name,
                style = MaterialTheme.typography.labelSmall,
                color = priorityColor
            )
        }
    }
}

// Status chip component
@Composable
fun StatusChip(status: ProjectStatus) {
    val statusColor = when(status) {
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
            text = status.name.replace('_', ' '),
            style = MaterialTheme.typography.labelSmall,
            color = statusColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
