package com.example.projectmanager.ui.task

import androidx.compose.runtime.Composable
import com.example.projectmanager.ui.tasks.TaskDetailsScreen

@Composable
fun TaskScreen(
    taskId: String,
    projectId: String = "",
    onNavigateBack: () -> Unit = {}
) {
    // Use our new TaskDetailsScreen implementation
    TaskDetailsScreen(
        taskId = taskId,
        onNavigateBack = onNavigateBack,
        onNavigateToProject = { projectId -> 
            // This would navigate to the project screen, but we'll just use the back navigation for now
            onNavigateBack()
        }
    )
}