package com.example.projectmanager.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.Chat
import com.example.projectmanager.data.model.User
import com.example.projectmanager.data.repository.ChatRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.data.repository.UserPreferencesRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatSettingsUiState(
    val chat: Chat? = null,
    val participants: List<User> = emptyList(),
    val isAdmin: Boolean = false,
    val isMuted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatSettingsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatSettingsUiState())
    val uiState: StateFlow<ChatSettingsUiState> = _uiState.asStateFlow()
    
    private var chatId: String = ""
    
    fun loadChatDetails(chatId: String) {
        this.chatId = chatId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Load chat details
            when (val result = chatRepository.getChat(chatId)) {
                is Resource.Success<*> -> {
                    val chat = result.data as Chat
                    _uiState.update { it.copy(chat = chat) }
                    
                    // Load participants
                    loadParticipants(chat.participants)
                    
                    // Check if current user is admin
                    val currentUserId = getCurrentUserId()
                    val isAdmin = chat.participants.firstOrNull() == currentUserId
                    
                    // Check if chat is muted
                    val mutedChats = userPreferencesRepository.getMutedChats()
                    val isMuted = mutedChats.contains(chatId)
                    
                    _uiState.update { 
                        it.copy(
                            isAdmin = isAdmin,
                            isMuted = isMuted,
                            isLoading = false,
                            error = null
                        )
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
                else -> {}
            }
        }
    }
    
    private suspend fun loadParticipants(participantIds: List<String>) {
        val participants = mutableListOf<User>()
        
        for (userId in participantIds) {
            when (val result = userRepository.getUserById(userId)) {
                is Resource.Success<*> -> {
                    // Cast nécessaire car le type est effacé à l'exécution
                    val user = result.data as User
                    participants.add(user)
                }
                else -> {}
            }
        }
        
        _uiState.update { it.copy(participants = participants) }
    }
    
    fun toggleMuteNotifications() {
        viewModelScope.launch {
            val currentState = _uiState.value.isMuted
            val newState = !currentState
            
            if (newState) {
                userPreferencesRepository.muteChat(chatId)
            } else {
                userPreferencesRepository.unmuteChat(chatId)
            }
            
            _uiState.update { it.copy(isMuted = newState) }
        }
    }
    
    fun clearChatHistory() {
        viewModelScope.launch {
            // Implementation would depend on your backend
            // This is a simplified version
            _uiState.update { it.copy(isLoading = true) }
            
            // Placeholder for clearing chat history
            // In a real implementation, you would call a repository method
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    fun leaveGroup(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val currentUserId = getCurrentUserId()
            val chat = _uiState.value.chat ?: return@launch
            
            // Remove current user from participants
            val updatedParticipants = chat.participants.filter { it != currentUserId }
            
            // Update chat with new participants list
            val updatedChat = chat.copy(participants = updatedParticipants)
            
            when (val result = chatRepository.createChat(updatedChat)) {
                is Resource.Success<*> -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onComplete()
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }
    
    fun deleteChat(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = chatRepository.deleteChat(chatId)) {
                is Resource.Success<*> -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onComplete()
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }
    
    fun removeParticipant(userId: String) {
        viewModelScope.launch {
            val chat = _uiState.value.chat ?: return@launch
            
            // Remove user from participants
            val updatedParticipants = chat.participants.filter { it != userId }
            
            // Update chat with new participants list
            val updatedChat = chat.copy(participants = updatedParticipants)
            
            when (val result = chatRepository.createChat(updatedChat)) {
                is Resource.Success<*> -> {
                    // Update local state
                    val updatedParticipantsList = _uiState.value.participants.filter { it.id != userId }
                    _uiState.update { 
                        it.copy(
                            chat = updatedChat,
                            participants = updatedParticipantsList
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }
    
    fun renameChat(newName: String) {
        viewModelScope.launch {
            val chat = _uiState.value.chat ?: return@launch
            
            // Update chat with new name
            val updatedChat = chat.copy(name = newName)
            
            when (val result = chatRepository.createChat(updatedChat)) {
                is Resource.Success<*> -> {
                    _uiState.update { it.copy(chat = updatedChat) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }
    
    fun getCurrentUserId(): String {
        return userRepository.getCurrentUserId()
    }
}
