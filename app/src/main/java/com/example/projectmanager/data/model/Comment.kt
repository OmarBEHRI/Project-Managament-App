package com.example.projectmanager.data.model

import java.util.Date

data class Comment(
    val id: String = "",
    val projectId: String = "",
    val taskId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val content: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date? = null,
    val attachmentIds: List<String> = emptyList(),
    val mentions: List<String> = emptyList(),
    val parentId: String? = null,
    val isEdited: Boolean = false
) {
    // Empty constructor for Firebase
    constructor() : this(
        id = "",
        projectId = "",
        taskId = "",
        userId = "",
        authorName = "",
        content = "",
        createdAt = Date(),
        updatedAt = null,
        attachmentIds = emptyList(),
        mentions = emptyList(),
        parentId = null,
        isEdited = false
    )
}
