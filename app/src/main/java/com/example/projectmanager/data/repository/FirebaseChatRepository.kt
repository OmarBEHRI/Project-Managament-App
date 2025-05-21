package com.example.projectmanager.data.repository

import com.example.projectmanager.data.model.Chat
import com.example.projectmanager.data.model.ChatType
import com.example.projectmanager.data.model.Message
import com.example.projectmanager.data.model.MessageStatus
import com.example.projectmanager.data.model.MessageType
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

    private val chatsCollection = firestore.collection("chat_chats")
    private val messagesCollection = firestore.collection("chat_messages")

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

        // Utiliser un limit plus élevé pour s'assurer de récupérer tous les messages
        // La limite par défaut de Firestore est de 1000, ce qui devrait être suffisant pour la plupart des conversations
        val listener = messagesCollection
            .whereEqualTo("chat_id", chatId)
            .orderBy("sent_at", Query.Direction.ASCENDING)
            .limit(1000)  // Limite explicite pour s'assurer d'avoir tous les messages
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load messages"))
                    return@addSnapshotListener
                }

                // Récupérer les documents bruts pour déboguer
                val rawMessages = snapshot?.documents?.map { doc ->
                    mapOf(
                        "id" to doc.id,
                        "sender_id" to doc.getString("sender_id"),
                        "content" to doc.getString("content").orEmpty().take(20)
                    )
                }
                println("DEBUG RAW: Raw message data from Firestore: $rawMessages")

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    // Récupérer manuellement les champs pour s'assurer qu'ils sont correctement mappés
                    val id = doc.id
                    val senderId = doc.getString("sender_id") ?: ""
                    val chatId = doc.getString("chat_id") ?: ""
                    val content = doc.getString("content") ?: ""
                    val senderName = doc.getString("sender_name")
                    val typeStr = doc.getString("type") ?: "TEXT"
                    val type = try { MessageType.valueOf(typeStr) } catch (e: Exception) { MessageType.TEXT }
                    val sentAt = doc.getTimestamp("sent_at")?.toDate() ?: Date()
                    val statusStr = doc.getString("status") ?: "SENT"
                    val status = try { MessageStatus.valueOf(statusStr) } catch (e: Exception) { MessageStatus.SENT }
                    val readBy = (doc.get("read_by") as? List<*>)?.filterIsInstance<String>() ?: listOf()
                    
                    // Créer le message avec les valeurs extraites manuellement
                    val message = Message(
                        id = id,
                        chatId = chatId,
                        senderId = senderId.trim(), // Nettoyer l'ID pour éviter les problèmes d'espaces
                        senderName = senderName,
                        content = content,
                        type = type,
                        sentAt = sentAt,
                        status = status,
                        readBy = readBy
                    )
                    
                    // Déboguer chaque message
                    println("DEBUG LOAD: Message ID: $id, senderId: '$senderId', content: '${content.take(20)}...'")
                    
                    message
                } ?: emptyList()
                
                println("DEBUG: Loaded ${messages.size} messages for chat $chatId")
                trySend(Resource.Success(messages))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getAllChatMessagesHistory(chatId: String): Resource<List<Message>> {
        return try {
            val snapshot = messagesCollection
                .whereEqualTo("chat_id", chatId)
                .orderBy("sent_at", Query.Direction.ASCENDING)
                .get()
                .await()
                
            val messages = snapshot.documents.mapNotNull { it.toObject<Message>() }
            Resource.Success(messages)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load message history")
        }
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
                messagesCollection.document() // Firestore génère un ID si absent
            } else {
                messagesCollection.document(message.id)
            }

            // Nettoyage et validation rigoureux du senderId
            val finalSenderId = (message.senderId?.trim())?.takeIf { it.isNotBlank() } ?: "unknown_sender_in_repo"
            
            println("DEBUG SEND REPO: Attempting to send. Original senderId: '${message.senderId}', Final senderId for Firestore: '$finalSenderId', Message ID for Firestore: '${messageRef.id}'")

            // Créer une map explicite des champs à sauvegarder pour s'assurer que les noms de champs sont corrects
            val messageData = mapOf(
                "id" to messageRef.id,
                "chat_id" to message.chatId,
                "sender_id" to finalSenderId,  // Utiliser le senderId nettoyé et validé
                "sender_name" to message.senderName,
                "content" to message.content,
                "type" to message.type.toString(),
                "status" to MessageStatus.SENT.toString(),
                "sent_at" to (message.sentAt ?: Date()),
                "read_by" to (message.readBy ?: listOf(finalSenderId))
            )

            // Sauvegarder les données explicites
            messageRef.set(messageData).await()

            // Créer un objet Message pour le retour et la mise à jour du chat
            val messageToStore = Message(
                id = messageRef.id,
                chatId = message.chatId,
                senderId = finalSenderId,
                senderName = message.senderName,
                content = message.content,
                type = message.type,
                sentAt = message.sentAt ?: Date(),
                status = MessageStatus.SENT,
                readBy = message.readBy ?: listOf(finalSenderId)
            )

            // Créer un MessageSummary pour le dernier message du chat
            val messageSummary = com.example.projectmanager.data.model.MessageSummary(
                id = messageRef.id,
                chatId = message.chatId,
                senderId = finalSenderId,
                senderName = message.senderName,
                content = message.content,
                type = message.type,
                sentAt = message.sentAt ?: Date(),
                status = MessageStatus.SENT
            )
            
            // Mettre à jour le dernier message du chat
            chatsCollection.document(messageToStore.chatId).update(
                mapOf(
                    "last_message" to messageSummary,
                    "updated_at" to Date()
                )
            ).await()

            println("DEBUG SEND REPO: Message successfully stored with senderId: '$finalSenderId'")
            Resource.Success(messageToStore)
        } catch (e: Exception) {
            println("ERROR SEND REPO: Failed to send message: ${e.message}")
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
            val lastMessageDoc = messagesCollection
                .whereEqualTo("chat_id", chatId)
                .orderBy("sent_at", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
                
            // Create a MessageSummary from the last message document
            val lastMessageSummary = if (lastMessageDoc != null) {
                val id = lastMessageDoc.id
                val senderId = lastMessageDoc.getString("sender_id") ?: ""
                val chatId = lastMessageDoc.getString("chat_id") ?: ""
                val content = lastMessageDoc.getString("content") ?: ""
                val senderName = lastMessageDoc.getString("sender_name")
                val typeStr = lastMessageDoc.getString("type") ?: "TEXT"
                val type = try { MessageType.valueOf(typeStr) } catch (e: Exception) { MessageType.TEXT }
                val sentAt = lastMessageDoc.getTimestamp("sent_at")?.toDate() ?: Date()
                val statusStr = lastMessageDoc.getString("status") ?: "SENT"
                val status = try { MessageStatus.valueOf(statusStr) } catch (e: Exception) { MessageStatus.SENT }
                
                com.example.projectmanager.data.model.MessageSummary(
                    id = id,
                    chatId = chatId,
                    senderId = senderId,
                    senderName = senderName,
                    content = content,
                    type = type,
                    sentAt = sentAt,
                    status = status
                )
            } else null

            chatsCollection.document(chatId).update(
                mapOf(
                    "last_message" to lastMessageSummary,
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
    
    override suspend fun findDirectChat(user1Id: String, user2Id: String): Resource<Chat?> {
        return try {
            // We need to check both possible arrangements of participants
            // since we don't know the order in which they were stored
            val participants1 = listOf(user1Id, user2Id)
            val participants2 = listOf(user2Id, user1Id)
            
            // Query for chats that have exactly these two participants and are direct chats
            val query = chatsCollection
                .whereEqualTo("type", ChatType.DIRECT)
                .whereArrayContainsAny("participants", participants1)
                .get()
                .await()
                
            // Filter results to ensure we have exactly the two participants we want
            val matchingChats = query.documents.mapNotNull { doc ->
                val chat = doc.toObject<Chat>()
                // Check if the chat has exactly our two participants (no more, no less)
                if (chat != null && 
                    chat.participants.size == 2 && 
                    chat.participants.containsAll(participants1)) {
                    chat
                } else {
                    null
                }
            }
            
            // Return the most recently updated chat if multiple exist
            val mostRecentChat = matchingChats.maxByOrNull { it.updatedAt }
            
            Resource.Success(mostRecentChat)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to find direct chat")
        }
    }
} 