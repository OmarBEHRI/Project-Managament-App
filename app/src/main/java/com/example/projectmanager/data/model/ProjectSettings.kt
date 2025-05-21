package com.example.projectmanager.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Settings for a project, including various feature toggles and preferences
 */
data class ProjectSettings(
    @get:PropertyName("enable_subtasks")
    @set:PropertyName("enable_subtasks")
    var enableSubtasks: Boolean = true,
    
    @get:PropertyName("enable_task_dependencies")
    @set:PropertyName("enable_task_dependencies")
    var enableTaskDependencies: Boolean = false,
    
    @get:PropertyName("enable_time_tracking")
    @set:PropertyName("enable_time_tracking")
    var enableTimeTracking: Boolean = false,
    
    @get:PropertyName("enable_budget_tracking")
    @set:PropertyName("enable_budget_tracking")
    var enableBudgetTracking: Boolean = false,
    
    @get:PropertyName("default_view")
    @set:PropertyName("default_view")
    var defaultView: String = "board",
    
    @get:PropertyName("notification_settings")
    @set:PropertyName("notification_settings")
    var notificationSettings: NotificationSettings = NotificationSettings()
)

/**
 * Notification preferences for a project
 */
data class NotificationSettings(
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val dueDateReminders: Boolean = true,
    val assignmentNotifications: Boolean = true,
    val commentNotifications: Boolean = true
)
