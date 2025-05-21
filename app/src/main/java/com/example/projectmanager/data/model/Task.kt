package com.example.projectmanager.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Task(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    @get:PropertyName("rich_description")
    val richDescription: RichTextContent? = null,
    @get:PropertyName("project_id")
    val projectId: String = "",
    @get:PropertyName("parent_task_id")
    val parentTaskId: String? = null,
    val subtasks: List<Task> = emptyList(),
    @get:PropertyName("assigned_to")
    val assignedTo: List<String> = emptyList(),
    @get:PropertyName("created_by")
    val createdBy: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    @get:PropertyName("start_date")
    val startDate: Date? = null,
    @get:PropertyName("due_date")
    val dueDate: Date? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null,
    val tags: List<String> = emptyList(),
    @get:PropertyName("completed")
    val isCompleted: Boolean = false,
    @get:PropertyName("completed_at")
    val completedAt: Date? = null,
    @set:PropertyName("overdue")
    @get:PropertyName("overdue")
    var isOverdue: Boolean = false,
    val dependencies: List<TaskDependency> = emptyList(),
    @get:PropertyName("estimated_hours")
    val estimatedHours: Float? = null,
    @get:PropertyName("actual_hours")
    val actualHours: Float? = null,
    val comments: List<Comment> = emptyList(),
    val attachments: List<FileAttachment> = emptyList(),
    val checklists: List<Checklist> = emptyList(),
    @get:PropertyName("milestone_id")
    val milestoneId: String? = null,
    val order: Int = 0,
    val watchers: List<String> = emptyList(),
    @get:PropertyName("last_activity")
    val lastActivity: TaskActivity? = null
)

data class TaskDependency(
    @PropertyName("dependent_task_id")
    var dependentTaskId: String = "",
    val type: DependencyType = DependencyType.FINISH_TO_START
)

enum class DependencyType {
    FINISH_TO_START,    // Task can't start until dependent task is finished
    START_TO_START,     // Task can't start until dependent task starts
    FINISH_TO_FINISH,   // Task can't finish until dependent task finishes
    START_TO_FINISH     // Task can't finish until dependent task starts
}

data class RichTextContent(
    val content: String = "", // HTML or Markdown content
    val format: TextFormat = TextFormat.HTML,
    val mentions: List<UserMention> = emptyList()
)

enum class TextFormat {
    HTML,
    MARKDOWN
}

data class UserMention(
    @PropertyName("user_id")
    val userId: String = "",
    val offset: Int = 0,
    val length: Int = 0
)

data class Checklist(
    val id: String = "",
    val title: String = "",
    val items: List<ChecklistItem> = emptyList()
)

data class ChecklistItem(
    val id: String = "",
    val text: String = "",
    val isCompleted: Boolean = false,
    @PropertyName("completed_by")
    val completedBy: String? = null,
    @PropertyName("completed_at")
    val completedAt: Date? = null,
    @PropertyName("assigned_to")
    val assignedTo: String? = null
)

data class TaskActivity(
    val type: TaskActivityType = TaskActivityType.UPDATE,
    @PropertyName("user_id")
    val userId: String = "",
    val timestamp: Date = Date(),
    val changes: Map<String, Any>? = null
)

enum class TaskActivityType {    CREATE,    UPDATE,    COMMENT,    ATTACHMENT,    STATUS_CHANGE,    ASSIGNMENT,    CHECKLIST_UPDATE}
