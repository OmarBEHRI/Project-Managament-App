package com.example.projectmanager.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.*
import com.example.projectmanager.data.repository.ChatRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.data.service.StorageService
import com.example.projectmanager.service.ChatMessageListener
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ChatUiState(
    val chat: Chat? = null,
    val messages: List<Message> = emptyList(),
    val messageText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val recipientName: String? = null,
    val projectName: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val storageService: StorageService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Variable pour stocker l'ID de l'utilisateur avec qui on veut créer une conversation
    // Variable pour stocker l'ID de l'utilisateur avec qui on veut cru00e9er une conversation
    private var pendingDirectChatUserId: String? = null
    
    fun loadChat(chatId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Vu00e9rifier si c'est une nouvelle conversation directe
            if (chatId.startsWith("new_direct_")) {
                // Extraire l'ID de l'utilisateur
                val userId = chatId.removePrefix("new_direct_")
                pendingDirectChatUserId = userId
                
                // Charger les informations de l'utilisateur pour afficher son nom
                userRepository.getUserById(userId).collect { userResult ->
                    when (userResult) {
                        is Resource.Success<*> -> {
                            val user = userResult.data as User
                            // Cru00e9er un objet Chat temporaire pour l'affichage
                            val tempChat = Chat(
                                id = "temp_" + UUID.randomUUID().toString(),
                                name = user.displayName,
                                type = ChatType.DIRECT,
                                participants = listOf(getCurrentUserId(), userId),
                                unreadCount = mapOf(getCurrentUserId() to 0, userId to 0)
                            )
                            _uiState.update {
                                it.copy(
                                    chat = tempChat,
                                    isLoading = false,
                                    error = null
                                )
                            }
                            
                            // Mu00eame pour une conversation temporaire, nous devons initialiser la liste des messages
                            // avec une liste vide pour que les nouveaux messages s'affichent correctement
                            _uiState.update {
                                it.copy(messages = emptyList())
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = userResult.message
                                )
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(isLoading = true)
                            }
                        }
                    }
                }
                // Ne pas retourner ici, continuer pour charger les messages si disponibles
            }
            
            // Chargement normal d'une conversation existante
            when (val chatResult = chatRepository.getChat(chatId)) {
                is Resource.Success<*> -> {
                    val chat = chatResult.data as Chat
                    _uiState.update {
                        it.copy(
                            chat = chat,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = chatResult.message
                        )
                    }
                }
                else -> {}
            }

            // Load messages
            chatRepository.getChatMessages(chatId).collect { result ->
                when (result) {
                    is Resource.Success<*> -> {
                        val messages = result.data as List<Message>
                        _uiState.update {
                            it.copy(
                                messages = messages,
                                isLoading = false,
                                error = null
                            )
                        }
                        
                        // Mark unread messages as read
                        val currentUserId = getCurrentUserId()
                        messages.forEach { message ->
                            if (message.senderId != currentUserId && !message.readBy.contains(currentUserId)) {
                                markMessageAsRead(message.id)
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update {
                            it.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Sauvegarder l'historique des messages lorsque le ViewModel est du00e9truit
        saveMessageHistory()
    }
    
    // Mu00e9thode pour sauvegarder l'historique des messages
    private fun saveMessageHistory() {
        val chat = uiState.value.chat ?: return
        val chatId = chat.id
        
        // Ne pas sauvegarder les conversations temporaires
        if (chatId.startsWith("temp_") || chatId.startsWith("new_direct_")) {
            return
        }
        
        // Sauvegarder les messages en mu00e9moire pour les restaurer si nu00e9cessaire
        val messages = uiState.value.messages
        if (messages.isNotEmpty()) {
            println("DEBUG: Saving ${messages.size} messages for chat $chatId")
            // On pourrait implémenter ici une sauvegarde locale si nu00e9cessaire
        }
    }

    fun updateMessageText(text: String) {
        _uiState.update { it.copy(messageText = text) }
    }

    fun sendMessage() {
        viewModelScope.launch {
            val messageText = uiState.value.messageText.trim()
            if (messageText.isBlank() || uiState.value.chat == null) return@launch

            // Get current user
            val currentUserId = getCurrentUserId()
            var senderName: String? = null
            
            // Get user's display name
            when (val userResult = userRepository.getUserById(currentUserId)) {
                is Resource.Success<*> -> {
                    val user = userResult.data as User
                    senderName = user.displayName
                }
                else -> {}
            }
            
            // Vu00e9rifier si nous avons une conversation en attente u00e0 cru00e9er
            if (pendingDirectChatUserId != null) {
                // Cru00e9er la conversation
                val newChat = Chat(
                    type = ChatType.DIRECT,
                    participants = listOf(currentUserId, pendingDirectChatUserId!!),
                    unreadCount = mapOf(currentUserId to 0, pendingDirectChatUserId!! to 0)
                )
                
                when (val createResult = chatRepository.createChat(newChat)) {
                    is Resource.Success<*> -> {
                        val chat = createResult.data as Chat
                        _uiState.update { it.copy(chat = chat) }
                        
                        // Envoyer le message dans la nouvelle conversation
                        // S'assurer que l'ID de l'expu00e9diteur est correctement du00e9fini
                        // Utiliser trim() pour u00e9viter les probu00e8mes d'espaces
                        val cleanCurrentUserId = currentUserId.trim()
                        
                        val message = Message(
                            chatId = chat.id,
                            senderId = cleanCurrentUserId,  // ID de l'utilisateur actuel (nettoyu00e9)
                            senderName = senderName,        // Nom de l'utilisateur actuel
                            content = messageText,
                            type = MessageType.TEXT,
                            status = MessageStatus.SENDING,
                            readBy = listOf(cleanCurrentUserId)  // Marquer comme lu par l'expu00e9diteur
                        )
                        
                        // Afficher des informations de du00e9bogage
                        println("DEBUG SEND VM: Creating message with senderId: '$cleanCurrentUserId'")
                        
                        _uiState.update { it.copy(messageText = "") }
                        
                        // Ajouter le message u00e0 l'interface utilisateur immu00e9diatement
                        _uiState.update { currentState ->
                            currentState.copy(
                                messages = currentState.messages + message
                            )
                        }
                        
                        // Envoyer le message u00e0 Firebase
                        val result = chatRepository.sendMessage(message)
                        println("DEBUG SEND VM: Result of sending message: $result")
                        pendingDirectChatUserId = null  // Ru00e9initialiser l'ID de l'utilisateur en attente
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = createResult.message) }
                    }
                    else -> {}
                }
            } else {
                val cleanCurrentUserId = currentUserId.trim()
                
                val message = Message(
                    chatId = uiState.value.chat!!.id,
                    senderId = cleanCurrentUserId,  // ID de l'utilisateur actuel (nettoyu00e9)
                    senderName = senderName,        // Nom de l'utilisateur actuel
                    content = messageText,
                    type = MessageType.TEXT,
                    status = MessageStatus.SENDING,
                    readBy = listOf(cleanCurrentUserId)  // Marquer comme lu par l'expu00e9diteur
                )
                
                // Afficher des informations de du00e9bogage
                println("DEBUG SEND VM: Creating message with senderId: '$cleanCurrentUserId'")

                _uiState.update { it.copy(messageText = "") }
                
                // Ajouter le message u00e0 l'interface utilisateur immu00e9diatement
                _uiState.update { currentState ->
                    currentState.copy(
                        messages = currentState.messages + message
                    )
                }

                when (val result = chatRepository.sendMessage(message)) {
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = result.message)
                        }
                    }
                    else -> {
                        println("DEBUG SEND VM: Result of sending message: $result")
                    }
                }
            }
        }
    }

    fun sendAttachment(uri: Uri) {
        viewModelScope.launch {
            uiState.value.chat?.let { chat ->
                _uiState.update { it.copy(isLoading = true) }

                // Get current user
                val currentUserId = getCurrentUserId()
                var senderName: String? = null
                
                // Get user's display name
                when (val userResult = userRepository.getUserById(currentUserId)) {
                    is Resource.Success<*> -> {
                        val user = userResult.data as User
                        senderName = user.displayName
                    }
                    else -> {}
                }

                // Upload file
                storageService.uploadFile(
                    uri = uri,
                    projectId = chat.projectId ?: "general",
                    taskId = null,
                    commentId = null
                ).collect { result ->
                    when (result) {
                        is Resource.Success<*> -> {
                            val attachment = result.data as FileAttachment
                            val message = Message(
                                chatId = chat.id,
                                senderId = currentUserId,
                                senderName = senderName,
                                content = attachment.downloadUrl,
                                type = if (attachment.mimeType.startsWith("image/")) {
                                    MessageType.IMAGE
                                } else {
                                    MessageType.FILE
                                },
                                attachments = listOf(attachment),
                                status = MessageStatus.SENDING
                            )

                            chatRepository.sendMessage(message)
                            _uiState.update { it.copy(isLoading = false) }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.message
                                )
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(isLoading = true)
                            }
                        }
                    }
                }
            }
        }
    }

    fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            uiState.value.chat?.let { chat ->
                chatRepository.markMessageAsRead(
                    messageId = messageId,
                    chatId = chat.id,
                    userId = getCurrentUserId()
                )
            }
        }
    }

    fun getCurrentUserId(): String {
        return userRepository.getCurrentUserId() ?: ""
    }
    
    // Load user information for a participant
    fun loadUserInfo(userId: String) {
        viewModelScope.launch {
            userRepository.getUserById(userId).collect { result ->
                when (result) {
                    is Resource.Success<*> -> {
                        val user = result.data as User
                        _uiState.update { it.copy(recipientName = user.displayName) }
                    }
                    is Resource.Error -> {
                        // Handle error silently
                        println("Error loading user info: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Loading state
                    }
                }
            }
        }
    }
    
    // Get the recipient name for display in the UI
    fun getRecipientName(): String? {
        return uiState.value.recipientName ?: uiState.value.chat?.name
    }
}