package com.example.projectmanager.navigation

import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNavigatorImpl @Inject constructor(
    private val auth: FirebaseAuth
) : MainScreenNavigator {
    private var navController: NavController? = null

    fun setNavController(navController: NavController) {
        this.navController = navController
    }

    override fun navigateBack() {
        navController?.navigateUp()
    }

    override fun navigateToSignIn() {
        navController?.navigate(AUTH_GRAPH_ROUTE) {
            popUpTo(0)
        }
    }

    override fun navigateToSignUp() {
        navController?.navigate(SIGN_UP_ROUTE)
    }

    override fun navigateToForgotPassword() {
        navController?.navigate(FORGOT_PASSWORD_ROUTE)
    }

    override fun navigateToHome() {
        navController?.navigate(HOME_GRAPH_ROUTE) {
            popUpTo(0)
        }
    }

    override fun navigateToProjects() {
        navController?.navigate(PROJECTS_ROUTE)
    }

    override fun navigateToProject(projectId: String) {
        navController?.navigate("$PROJECT_DETAIL_ROUTE/$projectId")
    }

    override fun navigateToTasks() {
        navController?.navigate(TASKS_ROUTE)
    }

    override fun navigateToTask(taskId: String) {
        navController?.navigate("$TASK_DETAIL_ROUTE/$taskId")
    }

    override fun navigateToProfile() {
        navController?.navigate(PROFILE_ROUTE)
    }

    override fun navigateToSettings() {
        navController?.navigate(SETTINGS_ROUTE)
    }

    override fun navigateToCreateProject() {
        navController?.navigate(CREATE_PROJECT_ROUTE)
    }

    override fun navigateToCreateTask(projectId: String?) {
        navController?.navigate(
            if (projectId != null) "$CREATE_TASK_ROUTE?projectId=$projectId"
            else CREATE_TASK_ROUTE
        )
    }

    override fun navigateToEditProject(projectId: String) {
        navController?.navigate("$EDIT_PROJECT_ROUTE/$projectId")
    }

    override fun navigateToEditTask(taskId: String) {
        navController?.navigate("$EDIT_TASK_ROUTE/$taskId")
    }

    override fun navigateToAnalyticsDashboard() {
        navController?.navigate(ANALYTICS_DASHBOARD_ROUTE)
    }

    override fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    companion object {
        const val HOME_GRAPH_ROUTE = "home"
        const val PROJECTS_ROUTE = "projects"
        const val PROJECT_DETAIL_ROUTE = "project"
        const val TASKS_ROUTE = "tasks"
        const val TASK_DETAIL_ROUTE = "task"
        const val PROFILE_ROUTE = "profile"
        const val SETTINGS_ROUTE = "settings"
        const val CREATE_PROJECT_ROUTE = "create_project"
        const val CREATE_TASK_ROUTE = "create_task"
        const val EDIT_PROJECT_ROUTE = "edit_project"
        const val EDIT_TASK_ROUTE = "edit_task"
        const val ANALYTICS_DASHBOARD_ROUTE = "analytics_dashboard"
    }
} 