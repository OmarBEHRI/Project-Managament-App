package com.example.projectmanager.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp
import java.util.Date

data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val bio: String = "",
    val position: String = "",
    val department: String = "",
    val skills: List<String> = emptyList(),
    val projectIds: List<String> = emptyList(),
    @get:PropertyName("last_active")
    @set:PropertyName("last_active")
    var lastActive: Timestamp = Timestamp.now(),
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val lastLoginAt: Date? = null,
    val status: UserStatus = UserStatus.ACTIVE,
    @get:PropertyName("is_email_verified")
    @set:PropertyName("is_email_verified")
    var isEmailVerified: Boolean = false,
    val role: UserRole = UserRole.MEMBER,
    val fcmToken: String? = null,
    val preferences: UserPreferences = UserPreferences(),
    @get:PropertyName("total_tasks")
    val totalTasks: Int = 0,
    @get:PropertyName("completed_tasks")
    val completedTasks: Int = 0,
    @get:PropertyName("active_projects")
    val activeProjects: Int = 0
)

enum class UserStatus {
    ACTIVE, AWAY, BUSY, OFFLINE
}

enum class UserRole {
    ADMIN,
    MANAGER,
    MEMBER
}

data class UserPreferences(
    val theme: String = "system",
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    @get:PropertyName("default_project_view")
    val defaultProjectView: String = "list",
    val language: String = "en"
)
