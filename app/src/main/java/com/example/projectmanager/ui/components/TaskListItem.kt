package com.example.projectmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskStatus
import com.example.projectmanager.data.model.TaskPriority
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListItem(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TaskStatusChip(status = task.status)
                PriorityChip(priority = task.priority)
                
                Spacer(modifier = Modifier.weight(1f))
                
                task.dueDate?.let { dueDate ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Due date",
                            modifier = Modifier.size(16.dp),
                            tint = if (task.isOverdue) MaterialTheme.colorScheme.error
                                  else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(dueDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (task.isOverdue) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskStatusChip(
    status: TaskStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = when (status) {
        TaskStatus.TODO -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        TaskStatus.REVIEW -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        TaskStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        TaskStatus.BLOCKED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        TaskStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = status.name.replace('_', ' '),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun PriorityChip(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = when (priority) {
        TaskPriority.LOW -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        TaskPriority.HIGH -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        TaskPriority.URGENT -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }

    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (priority) {
                    TaskPriority.LOW -> Icons.Default.ArrowDownward
                    TaskPriority.MEDIUM -> Icons.Default.Remove
                    TaskPriority.HIGH -> Icons.Default.ArrowUpward
                    TaskPriority.URGENT -> Icons.Default.PriorityHigh
                },
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = priority.name,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
} 