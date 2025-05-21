package com.example.projectmanager.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class ChatType {
    DIRECT,
    GROUP,
    PROJECT
}

data class Chat(
    @DocumentId
    val id: String = "",
    val type: ChatType = ChatType.DIRECT,
    val name: String? = null,
    val participants: List<String> = emptyList(),
    @get:PropertyName("project_id")
    val projectId: String? = null,
    @get:PropertyName("last_message")
    val lastMessage: Message? = null,
    @get:PropertyName("unread_count")
    val unreadCount: Map<String, Int> = emptyMap(),
    @ServerTimestamp
    @get:PropertyName("created_at")
    val createdAt: Date = Date(),
    @ServerTimestamp
    @get:PropertyName("updated_at")
    val updatedAt: Date = Date()
)

data class Message(
    @DocumentId
    val id: String = "",
    @get:PropertyName("chat_id")
    val chatId: String = "",
    @get:PropertyName("sender_id")
    val senderId: String = "",
    @get:PropertyName("sender_name")
    val senderName: String? = null,
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val attachments: List<FileAttachment> = emptyList(),
    @get:PropertyName("replied_to")
    val repliedTo: Message? = null,
    @get:PropertyName("read_by")
    val readBy: List<String> = emptyList(),
    @ServerTimestamp
    @get:PropertyName("sent_at")
    val sentAt: Date? = null,
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageType {
    TEXT,
    IMAGE,
    FILE,
    SYSTEM      // For system messages like "X joined the project"
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    ERROR
} 