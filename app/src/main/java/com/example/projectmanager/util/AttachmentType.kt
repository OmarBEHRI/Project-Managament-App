package com.example.projectmanager.util

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