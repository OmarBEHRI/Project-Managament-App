package com.example.projectmanager.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.data.model.TaskStatus

@Composable
fun TaskScreen(
    taskId: String,
    projectId: String = "",
    onNavigateBack: () -> Unit = {}
) {
    var taskTitle by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(taskId) {
        // Simulate loading task data
        taskTitle = "Task #$taskId"
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = taskTitle.ifEmpty { "Task Details" }) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Task ID: $taskId")
                    if (projectId.isNotEmpty()) {
                        Text(text = "Project ID: $projectId")
                    }
                    Text(text = "Status: ${TaskStatus.TODO}")
                }
            }
        }
    }
} 