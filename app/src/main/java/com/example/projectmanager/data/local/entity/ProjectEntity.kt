package com.example.projectmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projectmanager.data.local.Converters
import com.example.projectmanager.data.model.Project
import com.example.projectmanager.data.model.ProjectStatus
import com.example.projectmanager.data.model.Priority
import com.example.projectmanager.data.model.ProjectMember

@Entity(tableName = "projects")
@TypeConverters(Converters::class)
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val ownerId: String,
    val members: List<String> = emptyList(),
    val status: ProjectStatus,
    val priority: Priority,
    val deadline: Long?,
    val createdAt: Long?,
    val updatedAt: Long?,
    val tags: List<String> = emptyList(),
    val totalTasks: Int,
    val completedTasks: Int,
    val isCompleted: Boolean
) {
    fun toDomain(): Project = Project(
        id = id,
        name = name,
        description = description,
        ownerId = ownerId,
        members = members.map { ProjectMember(userId = it) },
        status = status,
        priority = priority,
        deadline = deadline?.let { java.util.Date(it) },
        createdAt = createdAt?.let { java.util.Date(it) },
        updatedAt = updatedAt?.let { java.util.Date(it) },
        tags = tags,
        totalTasks = totalTasks,
        completedTasks = completedTasks,
        isCompleted = isCompleted
    )

    companion object {
        fun fromDomain(project: Project) = ProjectEntity(
            id = project.id,
            name = project.name,
            description = project.description,
            ownerId = project.ownerId,
            members = project.members.map { it.userId },
            status = project.status,
            priority = project.priority,
            deadline = project.deadline?.time,
            createdAt = project.createdAt?.time,
            updatedAt = project.updatedAt?.time,
            tags = project.tags,
            totalTasks = project.totalTasks,
            completedTasks = project.completedTasks,
            isCompleted = project.isCompleted
        )
    }
}