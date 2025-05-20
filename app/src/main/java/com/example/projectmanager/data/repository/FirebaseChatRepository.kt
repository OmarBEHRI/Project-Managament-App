package com.example.projectmanager.data.repository

import com.example.projectmanager.data.model.Chat
import com.example.projectmanager.data.model.Message
import com.example.projectmanager.data.model.MessageStatus
import com.example.projectmanager.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    private val chatsCollection = firestore.collection("chats")
    private val messagesCollection = firestore.collection("messages")

    override fun getChats(userId: String): Flow<Resource<List<Chat>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = chatsCollection
            .whereArrayContains("participants", userId)
            .orderBy("updated_at", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load chats"))
                    return@addSnapshotListener
                }

                val chats = snapshot?.documents?.mapNotNull { it.toObject<Chat>() } ?: emptyList()
                trySend(Resource.Success(chats))
            }

        awaitClose { listener.remove() }
    }

    override fun getProjectChats(projectId: String): Flow<Resource<List<Chat>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = chatsCollection
            .whereEqualTo("project_id", projectId)
            .orderBy("updated_at", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load project chats"))
                    return@addSnapshotListener
                }

                val chats = snapshot?.documents?.mapNotNull { it.toObject<Chat>() } ?: emptyList()
                trySend(Resource.Success(chats))
            }

        awaitClose { listener.remove() }
    }

    override fun getChatMessages(chatId: String): Flow<Resource<List<Message>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = messagesCollection
            .whereEqualTo("chat_id", chatId)
            .orderBy("sent_at", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load messages"))
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { it.toObject<Message>() } ?: emptyList()
                trySend(Resource.Success(messages))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getChat(chatId: String): Resource<Chat> {
        return try {
            val document = chatsCollection.document(chatId).get().await()
            val chat = document.toObject<Chat>()
            if (chat != null) {
                Resource.Success(chat)
            } else {
                Resource.Error("Chat not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get chat")
        }
    }

    override suspend fun createChat(chat: Chat): Resource<Chat> {
        return try {
            val chatRef = if (chat.id.isBlank()) {
                chatsCollection.document()
            } else {
                chatsCollection.document(chat.id)
            }

            val chatWithId = chat.copy(id = chatRef.id)
            chatRef.set(chatWithId).await()
            Resource.Success(chatWithId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create chat")
        }
    }

    override suspend fun sendMessage(message: Message): Resource<Message> {
        return try {
            val messageRef = if (message.id.isBlank()) {
                messagesCollection.document()
            } else {
                messagesCollection.document(message.id)
            }

            val messageWithId = message.copy(
                id = messageRef.id,
                status = if (message.status == MessageStatus.SENDING) {
                    MessageStatus.SENT
                } else {
                    message.status
                }
            )

            // Update message
            messageRef.set(messageWithId).await()

            // Update chat's last message and timestamp
            chatsCollection.document(message.chatId).update(
                mapOf(
                    "last_message" to messageWithId,
                    "updated_at" to Date()
                )
            ).await()

            Resource.Success(messageWithId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send message")
        }
    }

    override suspend fun markMessageAsRead(
        messageId: String,
        chatId: String,
        userId: String
    ): Resource<Unit> {
        return try {
            val messageRef = messagesCollection.document(messageId)
            val message = messageRef.get().await().toObject<Message>()

            message?.let {
                if (!it.readBy.contains(userId)) {
                    // Add user to readBy list
                    messageRef.update(
                        "read_by", it.readBy + userId,
                        "status", MessageStatus.READ
                    ).await()

                    // Update chat's unread count for the user
                    val chatRef = chatsCollection.document(chatId)
                    val chat = chatRef.get().await().toObject<Chat>()
                    chat?.let { c ->
                        val newUnreadCount = c.unreadCount.toMutableMap()
                        newUnreadCount[userId] = (newUnreadCount[userId] ?: 1) - 1
                        chatRef.update("unread_count", newUnreadCount).await()
                    }
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark message as read")
        }
    }

    override suspend fun deleteMessage(messageId: String, chatId: String): Resource<Unit> {
        return try {
            messagesCollection.document(messageId).delete().await()

            // Update chat's last message if needed
            val lastMessage = messagesCollection
                .whereEqualTo("chat_id", chatId)
                .orderBy("sent_at", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.toObject<Message>()

            chatsCollection.document(chatId).update(
                mapOf(
                    "last_message" to lastMessage,
                    "updated_at" to Date()
                )
            ).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete message")
        }
    }

    override suspend fun deleteChat(chatId: String): Resource<Unit> {
        return try {
            // Delete all messages in the chat
            val messages = messagesCollection
                .whereEqualTo("chat_id", chatId)
                .get()
                .await()

            messages.documents.forEach { doc ->
                messagesCollection.document(doc.id).delete().await()
            }

            // Delete the chat
            chatsCollection.document(chatId).delete().await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete chat")
        }
    }
} 