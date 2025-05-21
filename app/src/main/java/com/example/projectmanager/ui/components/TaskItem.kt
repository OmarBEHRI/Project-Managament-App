package com.example.projectmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskPriority
import com.example.projectmanager.data.model.TaskStatus
import com.example.projectmanager.ui.theme.TrelloColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * A modern task item component inspired by Trello's card design.
 * This component displays a task with its title, description, status, priority, and due date.
 */
@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onToggleCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    
    TrelloCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        backgroundColor = if (task.isCompleted) 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        else 
            MaterialTheme.colorScheme.surface,
        elevation = if (task.isCompleted) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(getStatusColor(task.status))
                    .padding(4.dp)
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title with optional strikethrough for completed tasks
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Description (if available)
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Task metadata row
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority chip
                    PriorityIndicator(priority = task.priority)
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Due date (if available)
                    task.dueDate?.let { dueDate ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (task.isOverdue) 
                                    MaterialTheme.colorScheme.error
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dateFormat.format(dueDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (task.isOverdue) 
                                    MaterialTheme.colorScheme.error
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Subtasks indicator (if any)
                    if (task.subtasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${task.subtasks.count { it.isCompleted }}/${task.subtasks.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Assignees count (if any)
                    if (task.assignedTo.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.assignedTo.size.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Completion toggle
            IconButton(
                onClick = onToggleCompletion,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) 
                        Icons.Default.CheckCircle 
                    else 
                        Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (task.isCompleted) "Mark as incomplete" else "Mark as complete",
                    tint = if (task.isCompleted) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun PriorityIndicator(priority: TaskPriority) {
    val (color, text) = when (priority) {
        TaskPriority.LOW -> Pair(TrelloColors.LowPriority, "Low")
        TaskPriority.MEDIUM -> Pair(TrelloColors.MediumPriority, "Medium")
        TaskPriority.HIGH -> Pair(TrelloColors.HighPriority, "High")
        TaskPriority.URGENT -> Pair(TrelloColors.TrelloRed, "Urgent")
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.height(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun getStatusColor(status: TaskStatus): Color {
    return when (status) {
        TaskStatus.TODO -> TrelloColors.TodoColor
        TaskStatus.IN_PROGRESS -> TrelloColors.InProgressColor
        TaskStatus.REVIEW -> TrelloColors.ReviewColor
        TaskStatus.COMPLETED -> TrelloColors.CompletedColor
        TaskStatus.BLOCKED -> TrelloColors.BlockedColor
        TaskStatus.CANCELLED -> Color.Gray
    }
}
