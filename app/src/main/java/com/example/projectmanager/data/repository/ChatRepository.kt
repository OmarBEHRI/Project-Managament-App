package com.example.projectmanager.data.repository

import com.example.projectmanager.data.model.Chat
import com.example.projectmanager.data.model.Message
import com.example.projectmanager.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(userId: String): Flow<Resource<List<Chat>>>
    fun getProjectChats(projectId: String): Flow<Resource<List<Chat>>>
    fun getChatMessages(chatId: String): Flow<Resource<List<Message>>>
    suspend fun getAllChatMessagesHistory(chatId: String): Resource<List<Message>>
    suspend fun getChat(chatId: String): Resource<Chat>
    suspend fun createChat(chat: Chat): Resource<Chat>
    suspend fun sendMessage(message: Message): Resource<Message>
    suspend fun markMessageAsRead(messageId: String, chatId: String, userId: String): Resource<Unit>
    suspend fun deleteMessage(messageId: String, chatId: String): Resource<Unit>
    suspend fun deleteChat(chatId: String): Resource<Unit>
    
    // Find an existing direct chat between two users
    suspend fun findDirectChat(user1Id: String, user2Id: String): Resource<Chat?>
} 