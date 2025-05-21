package com.example.projectmanager.navigation

/**
 * Navigation routes for the app
 */
object Routes {
    // Auth routes
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
    
    // Main navigation graph
    const val HOME_GRAPH_ROUTE = "home_graph"
    
    // Project routes
    const val PROJECTS = "projects"
    const val PROJECT_DETAILS = "project/{projectId}"
    const val CREATE_PROJECT = "create_project"
    const val EDIT_PROJECT = "edit_project/{projectId}"
    
    // Task routes
    const val TASKS = "tasks"
    const val TASK_DETAILS = "task/{taskId}"
    const val CREATE_TASK = "create_task?projectId={projectId}"
    const val EDIT_TASK = "edit_task/{taskId}"
    
    // Other routes
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
}
