package com.example.projectmanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projectmanager.data.local.Converters
import com.example.projectmanager.data.model.Comment
import java.util.Date

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("taskId"),
        Index("userId")
    ]
)
@TypeConverters(Converters::class)
data class CommentEntity(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val userId: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long?,
    val attachmentIds: List<String> = emptyList()
) {
    fun toDomain() = Comment(
        id = id,
        projectId = "", // Default value, not in entity
        taskId = taskId,
        userId = userId,
        authorName = "", // Default value, not in entity
        content = content,
        createdAt = Date(createdAt),
        updatedAt = updatedAt?.let { Date(it) },
        attachmentIds = attachmentIds,
        mentions = emptyList(), // Default value, not in entity
        parentId = null, // Default value, not in entity
        isEdited = false // Default value, not in entity
    )

    companion object {
        fun fromDomain(comment: Comment) = CommentEntity(
            id = comment.id,
            taskId = comment.taskId,
            userId = comment.userId,
            content = comment.content,
            createdAt = comment.createdAt.time,
            updatedAt = comment.updatedAt?.time,
            attachmentIds = comment.attachmentIds
        )
    }
}