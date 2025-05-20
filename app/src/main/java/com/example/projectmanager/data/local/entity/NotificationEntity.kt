package com.example.projectmanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projectmanager.data.local.Converters
import com.example.projectmanager.data.model.Notification
import com.example.projectmanager.data.model.NotificationType
import java.util.Date

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId")
    ]
)
@TypeConverters(Converters::class)
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val type: NotificationType,
    val userId: String,
    val projectId: String?,
    val taskId: String?,
    val isRead: Boolean = false,
    val createdAt: Date = Date(),
    val timestamp: Date? = null
) {
    fun toDomain() = Notification(
        id = id,
        title = title,
        content = content,
        type = type,
        userId = userId,
        projectId = projectId,
        taskId = taskId,
        read = isRead,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(notification: Notification) = NotificationEntity(
            id = notification.id,
            title = notification.title,
            content = notification.content,
            type = notification.type,
            userId = notification.userId,
            projectId = notification.projectId,
            taskId = notification.taskId,
            isRead = notification.read,
            timestamp = notification.timestamp
        )
    }
}