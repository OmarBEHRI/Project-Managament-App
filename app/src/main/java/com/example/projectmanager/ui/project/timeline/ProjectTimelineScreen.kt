package com.example.projectmanager.ui.project.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.ui.components.GanttChart
import com.example.projectmanager.ui.components.GanttChartState
import com.example.projectmanager.ui.components.GanttTask
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskStatus
import java.util.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectTimelineScreen(
    projectId: String,
    viewModel: ProjectTimelineViewModel = hiltViewModel(),
    onNavigateToTask: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timeline") },
                actions = {
                    // Filter button
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    // Sort button
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    // View options menu
                    IconButton(onClick = { /* Show view options */ }) {
                        Icon(Icons.Default.ViewWeek, contentDescription = "View Options")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Timeline controls
            TimelineControls(
                onZoomIn = { viewModel.zoomIn() },
                onZoomOut = { viewModel.zoomOut() },
                onToday = { viewModel.scrollToToday() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.error!!)
                    }
                }
                else -> {
                    // Task list and Gantt chart
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Task list (left side)
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.3f)
                                .fillMaxHeight()
                        ) {
                            items(uiState.tasks) { task ->
                                TaskListItem(
                                    task = task,
                                    onClick = { onNavigateToTask(task.id) }
                                )
                            }
                        }

                        // Gantt chart (right side)
                        GanttChart(
                            tasks = uiState.tasks,
                            modifier = Modifier
                                .weight(0.7f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }

        // Filter dialog
        if (showFilterDialog) {
            FilterDialog(
                currentFilter = uiState.filter,
                onFilterChange = { viewModel.updateFilter(it) },
                onDismiss = { showFilterDialog = false }
            )
        }

        // Sort dialog
        if (showSortDialog) {
            SortDialog(
                currentSort = uiState.sort,
                onSortChange = { viewModel.updateSort(it) },
                onDismiss = { showSortDialog = false }
            )
        }
    }
}

@Composable
fun TimelineControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Zoom controls
        IconButton(onClick = onZoomOut) {
            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
        }
        IconButton(onClick = onZoomIn) {
            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
        }
        
        Divider(
            modifier = Modifier
                .height(24.dp)
                .width(1.dp)
        )

        // Today button
        FilledTonalButton(onClick = onToday) {
            Icon(Icons.Default.Today, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Today")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListItem(
    task: Task,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(task.title) },
        supportingContent = {
            task.dueDate?.let { dueDate ->
                Text("Due: ${formatDate(dueDate)}")
            }
        },
        leadingContent = {
            Icon(
                imageVector = when (task.status) {
                    TaskStatus.TODO -> Icons.Default.RadioButtonUnchecked
                    TaskStatus.IN_PROGRESS -> Icons.Default.PlayCircleOutline
                    TaskStatus.REVIEW -> Icons.Default.PauseCircleOutline
                    TaskStatus.COMPLETED -> Icons.Default.CheckCircle
                    TaskStatus.BLOCKED -> Icons.Default.Block
                    TaskStatus.CANCELLED -> Icons.Default.Cancel
                },
                contentDescription = null,
                tint = when (task.status) {
                    TaskStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                    TaskStatus.BLOCKED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterDialog(
    currentFilter: TimelineFilter,
    onFilterChange: (TimelineFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Tasks") },
        text = {
            Column {
                // Status filter
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskStatus.values().forEach { status ->
                        FilterChip(
                            selected = status in currentFilter.statuses,
                            onClick = {
                                onFilterChange(
                                    if (status in currentFilter.statuses) {
                                        currentFilter.copy(statuses = currentFilter.statuses - status)
                                    } else {
                                        currentFilter.copy(statuses = currentFilter.statuses + status)
                                    }
                                )
                            },
                            label = { Text(status.name) }
                        )
                    }
                }

                // Assignee filter
                // Add more filter options as needed
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun SortDialog(
    currentSort: TimelineSort,
    onSortChange: (TimelineSort) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Tasks") },
        text = {
            Column {
                TimelineSortOption.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSortChange(
                                    if (currentSort.option == option) {
                                        currentSort.copy(ascending = !currentSort.ascending)
                                    } else {
                                        TimelineSort(option = option)
                                    }
                                )
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSort.option == option,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option.displayName)
                        if (currentSort.option == option) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (currentSort.ascending) {
                                    Icons.Default.ArrowUpward
                                } else {
                                    Icons.Default.ArrowDownward
                                },
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

private fun calculateTaskProgress(task: Task): Float {
    if (task.isCompleted) return 1f
    if (task.checklists.isEmpty()) return 0f
    val totalItems = task.checklists.sumOf { it.items.size }
    if (totalItems == 0) return 0f
    val completedItems = task.checklists.sumOf { checklist ->
        checklist.items.count { it.isCompleted }
    }
    return completedItems.toFloat() / totalItems
}

private fun formatDate(date: Date): String {
    // Implement date formatting logic
    return date.toString()
}