package com.example.projectmanager.ui.task

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.navigation.AppNavigator
import com.example.projectmanager.ui.tasks.TaskDetailsScreen
import com.example.projectmanager.ui.task.TaskViewModel

@Composable
fun TaskScreen(
    taskId: String,
    appNavigator: AppNavigator
) {
    // Use our TaskDetailsScreen implementation with proper navigation
    TaskDetailsScreen(
        taskId = taskId,
        onNavigateBack = { appNavigator.navigateBack() },
        onNavigateToProject = { projectId -> 
            // Navigate to the project details screen
            appNavigator.navigateToProject(projectId)
        },
        onTaskClick = { subtaskId ->
            // Navigate to the subtask details
            appNavigator.navigateToTask(subtaskId)
        }
    )
}