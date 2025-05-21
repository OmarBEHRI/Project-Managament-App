package com.example.projectmanager.service

import com.example.projectmanager.data.model.Chat
import com.example.projectmanager.data.model.Message
import com.example.projectmanager.data.repository.ChatRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatMessageListener @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val chatNotificationService: ChatNotificationService
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var activeChat: String? = null
    
    fun startListening() {
        val currentUserId = userRepository.getCurrentUserId()
        
        coroutineScope.launch {
            chatRepository.getChats(currentUserId).collectLatest { result ->
                if (result is Resource.Success) {
                    for (chat in result.data) {
                        listenForChatMessages(chat)
                    }
                }
            }
        }
    }
    
    private fun listenForChatMessages(chat: Chat) {
        val currentUserId = userRepository.getCurrentUserId()
        
        coroutineScope.launch {
            chatRepository.getChatMessages(chat.id).collectLatest { result ->
                if (result is Resource.Success) {
                    val messages = result.data
                    
                    // Find new messages that haven't been read by current user
                    val newMessages = messages.filter { message ->
                        message.senderId != currentUserId && 
                        !message.readBy.contains(currentUserId)
                    }
                    
                    // Process new messages
                    for (message in newMessages) {
                        // If not in active chat, show notification
                        if (activeChat != chat.id) {
                            chatNotificationService.showMessageNotification(message, chat)
                        }
                        
                        // Mark as read if in active chat
                        if (activeChat == chat.id) {
                            chatRepository.markMessageAsRead(
                                messageId = message.id,
                                chatId = chat.id,
                                userId = currentUserId
                            )
                        }
                    }
                }
            }
        }
    }
    
    fun setActiveChat(chatId: String?) {
        activeChat = chatId
    }
}
