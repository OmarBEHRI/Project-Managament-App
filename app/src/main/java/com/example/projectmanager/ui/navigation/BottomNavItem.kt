package com.example.projectmanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : BottomNavItem(
        route = "dashboard",
        title = "Dashboard",
        icon = Icons.Default.Dashboard
    )
    
    object Projects : BottomNavItem(
        route = "projects",
        title = "Projects",
        icon = Icons.Default.Work
    )
    
    object Tasks : BottomNavItem(
        route = "tasks",
        title = "Tasks",
        icon = Icons.Default.Assignment
    )
    
    object Chat : BottomNavItem(
        route = "chats",
        title = "Chat",
        icon = Icons.Default.Chat
    )
    
    object Profile : BottomNavItem(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person
    )
} 