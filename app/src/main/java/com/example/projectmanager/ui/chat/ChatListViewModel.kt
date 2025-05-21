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

data class ChatListUiState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()
    
    // Cache pour stocker les noms d'utilisateurs
    private val userNameCache = mutableMapOf<String, String>()

    fun loadChats(projectId: String?) {
        viewModelScope.launch {
            val flow = if (projectId != null) {
                chatRepository.getProjectChats(projectId)
            } else {
                chatRepository.getChats(getCurrentUserId())
            }

            flow.collect { result ->
                when (result) {
                    is Resource.Success<*> -> {
                        val allChats = result.data as List<Chat>
                        
                        // Fusionner les conversations directes avec le mu00eame utilisateur
                        val mergedChats = mergeDirectChatsWithSameUser(allChats)
                        
                        _uiState.update {
                            it.copy(
                                chats = mergedChats,
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
                        _uiState.update {
                            it.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Fusionne les conversations directes avec le mu00eame utilisateur
     */
    private fun mergeDirectChatsWithSameUser(chats: List<Chat>): List<Chat> {
        val currentUserId = getCurrentUserId()
        val directChatsMap = mutableMapOf<String, MutableList<Chat>>()
        val result = mutableListOf<Chat>()
        
        // Su00e9parer les conversations directes et les autres types de conversations
        for (chat in chats) {
            if (chat.type == ChatType.DIRECT) {
                // Trouver l'ID de l'autre participant
                val otherUserId = chat.participants.find { it != currentUserId } ?: continue
                
                // Regrouper les conversations par utilisateur
                if (directChatsMap.containsKey(otherUserId)) {
                    directChatsMap[otherUserId]?.add(chat)
                } else {
                    directChatsMap[otherUserId] = mutableListOf(chat)
                }
            } else {
                // Ajouter les conversations non directes directement au ru00e9sultat
                result.add(chat)
            }
        }
        
        // Pour chaque utilisateur, fusionner ses conversations en une seule
        for ((otherUserId, userChats) in directChatsMap) {
            if (userChats.isEmpty()) continue
            
            // Trier les conversations par date de mise u00e0 jour (la plus ru00e9cente d'abord)
            userChats.sortByDescending { it.updatedAt }
            
            // Prendre la conversation la plus ru00e9cente comme base
            val primaryChat = userChats.first()
            
            // Ajouter cette conversation fusionnu00e9e au ru00e9sultat
            result.add(primaryChat)
        }
        
        // Trier le ru00e9sultat final par date de mise u00e0 jour
        return result.sortedByDescending { it.updatedAt }
    }

    fun getCurrentUserId(): String {
        return userRepository.getCurrentUserId()
    }
    
    // Mu00e9thode pour ru00e9cupu00e9rer le nom d'utilisateur u00e0 partir de son ID
    fun getUserName(userId: String, callback: (String) -> Unit) {
        viewModelScope.launch {
            // Vu00e9rifier si le nom est du00e9ju00e0 dans le cache
            if (userNameCache.containsKey(userId)) {
                callback(userNameCache[userId] ?: userId)
                return@launch
            }
            
            // Sinon, ru00e9cupu00e9rer le nom depuis le repository
            userRepository.getUserById(userId).collect { result ->
                when (result) {
                    is Resource.Success<*> -> {
                        val user = result.data as User
                        val displayName = user.displayName.ifBlank { user.email }
                        // Stocker dans le cache
                        userNameCache[userId] = displayName
                        callback(displayName)
                    }
                    else -> {
                        // En cas d'erreur, utiliser l'ID comme fallback
                        callback(userId)
                    }
                }
            }
        }
    }
} 