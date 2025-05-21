package com.example.projectmanager.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.projectmanager.navigation.AppNavigator

/**
 * A shared bottom navigation bar component that can be used across the main screens.
 * 
 * @param currentRoute The current route to highlight the correct tab
 * @param appNavigator The app navigator for handling navigation between screens
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    appNavigator: AppNavigator
) {
    // Map routes to tab indices
    val selectedTab = when(currentRoute) {
        "dashboard" -> 0
        "projects" -> 1
        "tasks" -> 2
        "chats" -> 3
        else -> 0
    }
    
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { 
                if (selectedTab != 0) {
                    appNavigator.navigateToHome()
                }
            },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard") }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = {
                if (selectedTab != 1) {
                    appNavigator.navigateToProjects()
                }
            },
            icon = { Icon(Icons.Default.Folder, contentDescription = "Projects") },
            label = { Text("Projects") }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = {
                if (selectedTab != 2) {
                    appNavigator.navigateToTasks()
                }
            },
            icon = { Icon(Icons.Default.Assignment, contentDescription = "Tasks") },
            label = { Text("Tasks") }
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = {
                if (selectedTab != 3) {
                    appNavigator.navigateToChats()
                }
            },
            icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
            label = { Text("Chat") }
        )
    }
}
