package com.example.projectmanager.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.Chat
import com.example.projectmanager.data.model.ChatType
import com.example.projectmanager.data.model.User
import com.example.projectmanager.data.repository.ChatRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewChatUiState(
    val users: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val selectedUserIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NewChatViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewChatUiState())
    val uiState: StateFlow<NewChatUiState> = _uiState.asStateFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val currentUserId = getCurrentUserId()
            
            userRepository.searchUsers("").collect { result ->
                when (result) {
                    is Resource.Success<*> -> {
                        // Filter out current user
                        val filteredUsers = (result.data as List<User>).filter { it.id != currentUserId }
                        _uiState.update { 
                            it.copy(
                                users = filteredUsers,
                                filteredUsers = filteredUsers,
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
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    searchQuery = query,
                    filteredUsers = if (query.isBlank()) {
                        it.users
                    } else {
                        it.users.filter { user ->
                            user.displayName.contains(query, ignoreCase = true) || 
                            user.email.contains(query, ignoreCase = true)
                        }
                    }
                )
            }
        }
    }
    
    fun toggleUserSelection(userId: String) {
        viewModelScope.launch {
            val currentSelection = _uiState.value.selectedUserIds
            val newSelection = if (currentSelection.contains(userId)) {
                currentSelection - userId
            } else {
                currentSelection + userId
            }
            
            _uiState.update { it.copy(selectedUserIds = newSelection) }
        }
    }
    
    fun createDirectChat(userId: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentUserId = getCurrentUserId()
            
            // Créer directement un nouveau chat sans vérifier s'il existe déjà
            // Cela évite les problèmes liés à l'attente de la requête Firebase
            val newChat = Chat(
                type = ChatType.DIRECT,
                participants = listOf(currentUserId, userId),
                unreadCount = mapOf(currentUserId to 0, userId to 0)
            )
            
            when (val createResult = chatRepository.createChat(newChat)) {
                is Resource.Success<*> -> {
                    val chat = createResult.data as Chat
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(chat.id)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = createResult.message) }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
    
    fun createGroupChat(groupName: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            if (_uiState.value.selectedUserIds.isEmpty()) return@launch
            
            val currentUserId = getCurrentUserId()
            val participants = _uiState.value.selectedUserIds.toList() + currentUserId
            
            val newChat = Chat(
                type = ChatType.GROUP,
                name = groupName,
                participants = participants,
                unreadCount = participants.associateWith { 0 }
            )
            
            when (val result = chatRepository.createChat(newChat)) {
                is Resource.Success<*> -> {
                    // Clear selection
                    _uiState.update { it.copy(selectedUserIds = emptySet()) }
                    val chat = result.data as Chat
                    onSuccess(chat.id)
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
