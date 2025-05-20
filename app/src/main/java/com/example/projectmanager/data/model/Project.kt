package com.example.projectmanager.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Project(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    @get:PropertyName("owner_id")
    val ownerId: String = "",
    val members: List<ProjectMember> = emptyList(),
    val status: ProjectStatus = ProjectStatus.NOT_STARTED,
    val priority: Priority = Priority.MEDIUM,
    val deadline: Date? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null,
    val tags: List<String> = emptyList(),
    @get:PropertyName("total_tasks")
    @set:PropertyName("total_tasks")
    var totalTasks: Int = 0,
    @get:PropertyName("completed_tasks")
    @set:PropertyName("completed_tasks")
    var completedTasks: Int = 0,
    val isCompleted: Boolean = false,
    @get:PropertyName("template_id")
    val templateId: String? = null,
    val visibility: ProjectVisibility = ProjectVisibility.PRIVATE,
    val milestones: List<Milestone> = emptyList(),
    @get:PropertyName("budget_amount")
    val budgetAmount: Double = 0.0,
    @get:PropertyName("budget_currency")
    val budgetCurrency: String = "USD",
    @get:PropertyName("actual_cost")
    val actualCost: Double = 0.0,
    @get:PropertyName("estimated_hours")
    val estimatedHours: Float = 0f,
    @get:PropertyName("actual_hours")
    val actualHours: Float = 0f,
    val attachments: List<FileAttachment> = emptyList(),
    @get:PropertyName("parent_project_id")
    val parentProjectId: String? = null,
    @get:PropertyName("sub_projects")
    val subProjects: List<String> = emptyList(),
    @get:PropertyName("is_archived")
    val isArchived: Boolean = false,
    @get:PropertyName("archived_at")
    val archivedAt: Date? = null,
    val settings: ProjectSettings = ProjectSettings()
)

enum class ProjectStatus {
    NOT_STARTED,
    IN_PROGRESS,
    ON_HOLD,
    COMPLETED,
    CANCELLED,
    ARCHIVED
}

enum class ProjectVisibility {
    PRIVATE,    // Only members can view
    TEAM,       // All team members can view
    PUBLIC      // Anyone in the organization can view
}

enum class ProjectPermission {
    VIEW_PROJECT,
    EDIT_PROJECT,
    DELETE_PROJECT,
    MANAGE_MEMBERS,
    CREATE_TASKS,
    EDIT_TASKS,
    DELETE_TASKS,
    MANAGE_BUDGET,
    VIEW_BUDGET,
    MANAGE_SETTINGS
}

data class ProjectSettings(
    @get:PropertyName("default_view")
    val defaultView: ProjectView = ProjectView.LIST,
    @get:PropertyName("enable_time_tracking")
    val enableTimeTracking: Boolean = true,
    @get:PropertyName("enable_budget_tracking")
    val enableBudgetTracking: Boolean = true,
    @get:PropertyName("enable_task_dependencies")
    val enableTaskDependencies: Boolean = true,
    @get:PropertyName("enable_subtasks")
    val enableSubtasks: Boolean = true,
    @get:PropertyName("notification_settings")
    val notificationSettings: NotificationSettings = NotificationSettings()
)

enum class ProjectView {
    LIST,
    BOARD,
    TIMELINE,
    GANTT,
    CALENDAR
}

data class NotificationSettings(
    @get:PropertyName("notify_on_task_assignment")
    val notifyOnTaskAssignment: Boolean = true,
    @get:PropertyName("notify_on_task_completion")
    val notifyOnTaskCompletion: Boolean = true,
    @get:PropertyName("notify_on_milestone")
    val notifyOnMilestone: Boolean = true,
    @get:PropertyName("notify_on_comment")
    val notifyOnComment: Boolean = true,
    @get:PropertyName("notify_on_mention")
    val notifyOnMention: Boolean = true
)

enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
