package com.example.projectmanager.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projectmanager.ui.navigation.BottomNavItem
import com.example.projectmanager.ui.theme.ProjectmanagerTheme
import com.example.projectmanager.ui.dashboard.DashboardScreen
import com.example.projectmanager.ui.projects.ProjectsScreen
import com.example.projectmanager.ui.tasks.TasksScreen
import com.example.projectmanager.ui.chat.ChatScreen
import com.example.projectmanager.ui.chat.ChatListScreen
import com.example.projectmanager.ui.profile.ProfileScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectmanagerTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    BottomNavItem.Dashboard,
                    BottomNavItem.Projects,
                    BottomNavItem.Tasks,
                    BottomNavItem.Chat,
                    BottomNavItem.Profile
                )
                
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen()
            }
            composable(BottomNavItem.Projects.route) {
                ProjectsScreen()
            }
            composable(BottomNavItem.Tasks.route) {
                TasksScreen()
            }
            composable(BottomNavItem.Chat.route) {
                ChatListScreen(
                    onNavigateToChat = { chatId ->
                        navController.navigate("chat/$chatId")
                    }
                )
            }
            composable(
                route = "chat/{chatId}",
                arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                ChatScreen(
                    chatId = backStackEntry.arguments?.getString("chatId") ?: "",
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen()
            }
        }
    }
}