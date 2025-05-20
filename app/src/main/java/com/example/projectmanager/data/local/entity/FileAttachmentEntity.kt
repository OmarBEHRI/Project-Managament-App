package com.example.projectmanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projectmanager.data.local.Converters
import java.util.Date

@Entity(
    tableName = "file_attachments",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CommentEntity::class,
            parentColumns = ["id"],
            childColumns = ["commentId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["uploadedById"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("taskId"),
        Index("projectId"),
        Index("commentId"),
        Index("uploadedById")
    ]
)
@TypeConverters(Converters::class)
data class FileAttachmentEntity(
    @PrimaryKey
    val id: String,
    val taskId: String?,
    val projectId: String?,
    val commentId: String?,
    val fileName: String,
    val fileType: String,
    val filePath: String,
    val fileSize: Long,
    val uploadedById: String,
    val uploadedAt: Date,
    val description: String?
)