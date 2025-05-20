package com.example.projectmanager.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class FileAttachment(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val size: Long = 0,
    @get:PropertyName("mime_type")
    val mimeType: String = "",
    @get:PropertyName("storage_path")
    val storagePath: String = "",
    @get:PropertyName("download_url")
    val downloadUrl: String = "",
    @get:PropertyName("uploaded_by")
    val uploadedBy: String = "",
    @get:PropertyName("uploaded_by_id")
    val uploadedById: String = "",
    @ServerTimestamp
    @get:PropertyName("uploaded_at")
    val uploadedAt: Date? = null,
    val thumbnail: String? = null,
    @get:PropertyName("project_id")
    val projectId: String? = null,
    @get:PropertyName("task_id")
    val taskId: String? = null,
    @get:PropertyName("comment_id")
    val commentId: String? = null
)

enum class AttachmentType(val mimeTypes: List<String>) {
    IMAGE(listOf("image/jpeg", "image/png", "image/gif", "image/webp")),
    DOCUMENT(listOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
    SPREADSHEET(listOf("application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
    PRESENTATION(listOf("application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation")),
    VIDEO(listOf("video/mp4", "video/quicktime", "video/webm")),
    AUDIO(listOf("audio/mpeg", "audio/wav", "audio/ogg")),
    ARCHIVE(listOf("application/zip", "application/x-rar-compressed")),
    OTHER(listOf());

    companion object {
        fun fromMimeType(mimeType: String): AttachmentType {
            return values().find { type ->
                type.mimeTypes.contains(mimeType.lowercase())
            } ?: OTHER
        }
    }
}