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

// MessageSummary is used for the lastMessage field in Chat to avoid DocumentId conflicts
data class MessageSummary(
    var id: String = "",
    @get:PropertyName("chat_id")
    @set:PropertyName("chat_id")
    var chatId: String = "",
    @get:PropertyName("sender_id")
    @set:PropertyName("sender_id")
    var senderId: String = "",
    @get:PropertyName("sender_name")
    @set:PropertyName("sender_name")
    var senderName: String? = null,
    var content: String = "",
    var type: MessageType = MessageType.TEXT,
    @ServerTimestamp
    @get:PropertyName("sent_at")
    @set:PropertyName("sent_at")
    var sentAt: Date? = null,
    var status: MessageStatus = MessageStatus.SENT
)

data class Chat(
    @DocumentId
    val id: String = "",
    val type: ChatType = ChatType.DIRECT,
    val name: String? = null,
    val participants: List<String> = emptyList(),
    @get:PropertyName("project_id")
    @set:PropertyName("project_id")
    var projectId: String? = null,
    @get:PropertyName("last_message")
    @set:PropertyName("last_message")
    var lastMessage: MessageSummary? = null,
    @get:PropertyName("unread_count")
    @set:PropertyName("unread_count")
    var unreadCount: Map<String, Int> = emptyMap(),
    @ServerTimestamp
    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Date = Date(),
    @ServerTimestamp
    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    var updatedAt: Date = Date()
)

data class Message(
    // Only use @DocumentId when directly querying Message documents
    // For nested Message objects (like lastMessage in Chat), this should be a regular field
    var id: String = "",
    @get:PropertyName("chat_id")
    @set:PropertyName("chat_id")
    var chatId: String = "",
    @get:PropertyName("sender_id")
    @set:PropertyName("sender_id")
    var senderId: String = "",
    @get:PropertyName("sender_name")
    @set:PropertyName("sender_name")
    var senderName: String? = null,
    var content: String = "",
    var type: MessageType = MessageType.TEXT,
    var attachments: List<FileAttachment> = emptyList(),
    @get:PropertyName("replied_to")
    @set:PropertyName("replied_to")
    var repliedTo: Message? = null,
    @get:PropertyName("read_by")
    @set:PropertyName("read_by")
    var readBy: List<String> = emptyList(),
    @ServerTimestamp
    @get:PropertyName("sent_at")
    @set:PropertyName("sent_at")
    var sentAt: Date? = null,
    var status: MessageStatus = MessageStatus.SENT
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