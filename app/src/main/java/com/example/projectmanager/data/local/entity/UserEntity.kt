package com.example.projectmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projectmanager.data.local.Converters
import com.example.projectmanager.data.model.User
import com.example.projectmanager.data.model.UserPreferences
import com.example.projectmanager.data.model.UserRole

@Entity(tableName = "users")
@TypeConverters(Converters::class)
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
    val role: UserRole,
    val createdAt: Long?,
    val lastLoginAt: Long?,
    val fcmToken: String?,
    val theme: String,
    val emailNotifications: Boolean,
    val pushNotifications: Boolean,
    val defaultProjectView: String,
    val language: String
) {
    fun toDomain(): User = User(
        id = id,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        isEmailVerified = isEmailVerified,
        role = role,
        createdAt = createdAt?.let { java.util.Date(it) },
        lastLoginAt = lastLoginAt?.let { java.util.Date(it) },
        fcmToken = fcmToken,
        preferences = UserPreferences(
            theme = theme,
            emailNotifications = emailNotifications,
            pushNotifications = pushNotifications,
            defaultProjectView = defaultProjectView,
            language = language
        )
    )

    companion object {
        fun fromDomain(user: User) = UserEntity(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            photoUrl = user.photoUrl,
            isEmailVerified = user.isEmailVerified,
            role = user.role,
            createdAt = user.createdAt?.time,
            lastLoginAt = user.lastLoginAt?.time,
            fcmToken = user.fcmToken,
            theme = user.preferences.theme,
            emailNotifications = user.preferences.emailNotifications,
            pushNotifications = user.preferences.pushNotifications,
            defaultProjectView = user.preferences.defaultProjectView,
            language = user.preferences.language
        )
    }
}