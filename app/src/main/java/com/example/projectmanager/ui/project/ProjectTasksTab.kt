package com.example.projectmanager.ui.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectTasksTab(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit
) {
    var filterStatus by remember { mutableStateOf<TaskStatus?>(null) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterStatus == null,
                onClick = { filterStatus = null },
                label = { Text("All") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = null
                    )
                }
            )
            
            TaskStatus.values().forEach { status ->
                FilterChip(
                    selected = filterStatus == status,
                    onClick = { filterStatus = status },
                    label = { Text(status.name.replace('_', ' ')) },
                    leadingIcon = {
                        val icon = when(status) {
                            TaskStatus.TODO -> Icons.Outlined.CheckBoxOutlineBlank
                            TaskStatus.IN_PROGRESS -> Icons.Outlined.Pending
                            TaskStatus.REVIEW -> Icons.Outlined.RateReview
                            TaskStatus.COMPLETED -> Icons.Outlined.CheckBox
                            TaskStatus.BLOCKED -> Icons.Outlined.Block
                            TaskStatus.CANCELLED -> Icons.Outlined.Cancel
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null
                        )
                    }
                )
            }
        }
        
        // Task list
        val filteredTasks = if (filterStatus != null) {
            tasks.filter { it.status == filterStatus }
        } else {
            tasks
        }
        
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (tasks.isEmpty()) "No tasks yet" else "No tasks matching the selected filter",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredTasks.sortedWith(
                    compareBy<Task> { it.status != TaskStatus.COMPLETED }
                        .thenByDescending { it.priority.ordinal }
                        .thenBy { it.dueDate }
                )) { task ->
                    TaskListItem(
                        task = task,
                        onClick = { onTaskClick(task.id) }
                    )
                }
                
                // Add some space at the bottom for FAB
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
