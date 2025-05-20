package com.example.projectmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projectmanager.data.local.Converters
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskStatus
import com.example.projectmanager.data.model.TaskPriority
import com.example.projectmanager.data.model.Comment
import com.example.projectmanager.data.model.TaskDependency

@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val projectId: String,
    val assignedTo: List<String> = emptyList(),
    val createdBy: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val dueDate: Long?,
    val createdAt: Long?,
    val updatedAt: Long?,
    val tags: List<String> = emptyList(),
    val isCompleted: Boolean,
    val completedAt: Long?,
    val isOverdue: Boolean,
    val dependencies: List<String> = emptyList(),
    val estimatedHours: Float?,
    val actualHours: Float?,
    val comments: List<Comment> = emptyList()
) {
    companion object {
        fun fromDomain(task: Task): TaskEntity {
            return TaskEntity(
                id = task.id,
                title = task.title,
                description = task.description,
                projectId = task.projectId,
                assignedTo = task.assignedTo,
                createdBy = task.createdBy,
                status = task.status,
                priority = task.priority,
                dueDate = task.dueDate?.time,
                createdAt = task.createdAt?.time,
                updatedAt = task.updatedAt?.time,
                tags = task.tags,
                isCompleted = task.isCompleted,
                completedAt = task.completedAt?.time,
                isOverdue = task.isOverdue,
                dependencies = task.dependencies.map { it.dependentTaskId },
                estimatedHours = task.estimatedHours,
                actualHours = task.actualHours,
                comments = task.comments
            )
        }
    }

    fun toDomain(): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            projectId = projectId,
            assignedTo = assignedTo,
            createdBy = createdBy,
            status = status,
            priority = priority,
            dueDate = dueDate?.let { java.util.Date(it) },
            createdAt = createdAt?.let { java.util.Date(it) },
            updatedAt = updatedAt?.let { java.util.Date(it) },
            tags = tags,
            isCompleted = isCompleted,
            completedAt = completedAt?.let { java.util.Date(it) },
            isOverdue = isOverdue,
            dependencies = dependencies.map { TaskDependency(dependentTaskId = it) },
            estimatedHours = estimatedHours,
            actualHours = actualHours,
            comments = comments
        )
    }
}