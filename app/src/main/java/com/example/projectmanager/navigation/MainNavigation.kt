package com.example.projectmanager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.projectmanager.ui.home.HomeScreen
import com.example.projectmanager.ui.project.ProjectScreen
import com.example.projectmanager.ui.projects.ProjectsScreen
import com.example.projectmanager.ui.task.TaskScreen
import com.example.projectmanager.ui.tasks.TasksScreen
import com.example.projectmanager.ui.profile.ProfileScreen
import com.example.projectmanager.ui.settings.SettingsScreen
import com.example.projectmanager.ui.dashboard.AnalyticsDashboardScreen
import com.example.projectmanager.navigation.AppNavigatorImpl.Companion as Routes

@Composable
fun MainNavigation(
    navController: NavHostController,
    startDestination: String = Routes.HOME_GRAPH_ROUTE,
    appNavigator: AppNavigator
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Graph
        authNavigation(
            navController = navController,
            onAuthSuccess = {
                navController.navigate(Routes.HOME_GRAPH_ROUTE) {
                    popUpTo(AUTH_GRAPH_ROUTE) { inclusive = true }
                }
            }
        )

        // Home Graph
        composable(Routes.HOME_GRAPH_ROUTE) {
            HomeScreen(appNavigator = appNavigator)
        }

        // Projects
        composable(Routes.PROJECTS_ROUTE) {
            ProjectsScreen(
                onProjectClick = { projectId ->
                    navController.navigate("${Routes.PROJECT_DETAIL_ROUTE}/$projectId")
                },
                appNavigator = appNavigator
            )
        }

        composable(
            route = "${Routes.PROJECT_DETAIL_ROUTE}/{projectId}",
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ProjectScreen(
                projectId = backStackEntry.arguments?.getString("projectId") ?: "",
                appNavigator = appNavigator
            )
        }

        // Tasks
        composable(Routes.TASKS_ROUTE) {
            TasksScreen(
                onTaskClick = { taskId ->
                    navController.navigate("${Routes.TASK_DETAIL_ROUTE}/$taskId")
                },
                appNavigator = appNavigator
            )
        }

        composable(
            route = "${Routes.TASK_DETAIL_ROUTE}/{taskId}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            TaskScreen(
                taskId = backStackEntry.arguments?.getString("taskId") ?: "",
                appNavigator = appNavigator
            )
        }

        // Profile
        composable(Routes.PROFILE_ROUTE) {
            ProfileScreen()
        }

        // Settings
        composable(Routes.SETTINGS_ROUTE) {
            SettingsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onSignOut = {
                    // Force navigation to auth screen after sign out
                    navController.navigate(AUTH_GRAPH_ROUTE) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Create Project
        composable(Routes.CREATE_PROJECT_ROUTE) {
            // TODO: Implement CreateProjectScreen
        }

        // Create Task
        composable(
            route = "${Routes.CREATE_TASK_ROUTE}?projectId={projectId}",
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            // TODO: Implement CreateTaskScreen
            val projectId = backStackEntry.arguments?.getString("projectId")
        }

        // Edit Project
        composable(
            route = "${Routes.EDIT_PROJECT_ROUTE}/{projectId}",
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // TODO: Implement EditProjectScreen
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
        }

        // Edit Task
        composable(
            route = "${Routes.EDIT_TASK_ROUTE}/{taskId}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // TODO: Implement EditTaskScreen
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
        }

        // Analytics Dashboard
        composable(Routes.ANALYTICS_DASHBOARD_ROUTE) {
            AnalyticsDashboardScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
} 