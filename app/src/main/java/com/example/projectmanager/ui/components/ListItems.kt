package com.example.projectmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.projectmanager.data.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListItem(
    project: Project,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
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
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                PriorityChip(priority = project.priority)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = project.status)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${project.completedTasks}/${project.totalTasks}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicTaskListItem(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                PriorityChip(priority = task.priority)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = task.status)
                if (task.dueDate != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (task.isOverdue) Icons.Default.Warning else Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (task.isOverdue) MaterialTheme.colorScheme.error
                                  else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDate(task.dueDate),
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
fun StatusChip(
    status: ProjectStatus
) {
    AssistChip(
        onClick = { },
        label = { Text(status.name) },
        leadingIcon = {
            Icon(
                imageVector = when (status) {
                    ProjectStatus.NOT_STARTED -> Icons.Default.Edit
                    ProjectStatus.IN_PROGRESS -> Icons.Default.PlayArrow
                    ProjectStatus.ON_HOLD -> Icons.Default.Pause
                    ProjectStatus.COMPLETED -> Icons.Default.Done
                    ProjectStatus.CANCELLED -> Icons.Default.Close
                    ProjectStatus.NOT_STARTED -> Icons.Default.HourglassEmpty
                    ProjectStatus.ARCHIVED -> Icons.Default.Archive
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

@Composable
fun StatusChip(
    status: TaskStatus
) {
    AssistChip(
        onClick = { },
        label = { Text(status.name) },
        leadingIcon = {
            Icon(
                imageVector = when (status) {
                    TaskStatus.TODO -> Icons.Default.Assignment
                    TaskStatus.IN_PROGRESS -> Icons.Default.PlayArrow
                    TaskStatus.REVIEW -> Icons.Default.RateReview
                    TaskStatus.COMPLETED -> Icons.Default.Done
                    TaskStatus.BLOCKED -> Icons.Default.Block
                    TaskStatus.CANCELLED -> Icons.Default.Close
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

@Composable
fun PriorityChip(
    priority: Priority
) {
    AssistChip(
        onClick = { },
        label = { Text(priority.name) },
        leadingIcon = {
            Icon(
                imageVector = when (priority) {
                    Priority.HIGH -> Icons.Default.PriorityHigh
                    Priority.MEDIUM -> Icons.Default.Sort
                    Priority.LOW -> Icons.Default.LowPriority
                    Priority.URGENT -> Icons.Default.Warning
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            leadingIconContentColor = when (priority) {
                Priority.HIGH -> MaterialTheme.colorScheme.error
                Priority.MEDIUM -> MaterialTheme.colorScheme.tertiary
                Priority.LOW -> MaterialTheme.colorScheme.primary
                Priority.URGENT -> MaterialTheme.colorScheme.error
            }
        )
    )
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return formatter.format(date)
} 