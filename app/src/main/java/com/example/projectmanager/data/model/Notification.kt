package com.example.projectmanager.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Notification(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    @get:PropertyName("user_id")
    val userId: String = "",
    @get:PropertyName("project_id")
    val projectId: String? = null,
    @get:PropertyName("task_id")
    val taskId: String? = null,
    val read: Boolean = false,
    @ServerTimestamp
    val timestamp: Date? = null
) {
    val isRead: Boolean
        get() = read
}

