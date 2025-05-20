package com.example.projectmanager.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ProjectTemplate(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    @get:PropertyName("created_by")
    val createdBy: String = "",
    val category: String = "",
    val isPublic: Boolean = false,
    val tasks: List<TaskTemplate> = emptyList(),
    val milestones: List<MilestoneTemplate> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null,
    val tags: List<String> = emptyList(),
    @get:PropertyName("estimated_duration")
    val estimatedDuration: Int = 0, // in days
    val defaultMembers: List<String> = emptyList() // List of ProjectRole enum names
)

data class TaskTemplate(
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    @get:PropertyName("estimated_hours")
    val estimatedHours: Float = 0f,
    val dependencies: List<String> = emptyList(), // References other task titles in the template
    val assigneeRole: String = "", // References a role from ProjectRole
    val order: Int = 0
)

data class MilestoneTemplate(
    val title: String = "",
    val description: String = "",
    @get:PropertyName("relative_deadline")
    val relativeDeadline: Int = 0, // Days from project start
    val requiredTasks: List<String> = emptyList() // References task titles that must be completed
)

 