package com.example.projectmanager.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.*
import com.example.projectmanager.data.repository.ChatRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.data.service.StorageService
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
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val storageService: StorageService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun loadChat(chatId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load chat details
            when (val chatResult = chatRepository.getChat(chatId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            chat = chatResult.data,
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
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                messages = result.data,
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

    fun updateMessageText(text: String) {
        _uiState.update { it.copy(messageText = text) }
    }

    fun sendMessage() {
        viewModelScope.launch {
            val messageText = uiState.value.messageText.trim()
            if (messageText.isBlank() || uiState.value.chat == null) return@launch

            val message = Message(
                chatId = uiState.value.chat!!.id,
                senderId = getCurrentUserId(),
                content = messageText,
                type = MessageType.TEXT,
                status = MessageStatus.SENDING
            )

            _uiState.update { it.copy(messageText = "") }

            when (val result = chatRepository.sendMessage(message)) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = result.message)
                    }
                }
                else -> {}
            }
        }
    }

    fun sendAttachment(uri: Uri) {
        viewModelScope.launch {
            uiState.value.chat?.let { chat ->
                _uiState.update { it.copy(isLoading = true) }

                // Upload file
                storageService.uploadFile(
                    uri = uri,
                    projectId = chat.projectId ?: "general",
                    taskId = null,
                    commentId = null
                ).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val attachment = result.data
                            val message = Message(
                                chatId = chat.id,
                                senderId = getCurrentUserId(),
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
        return userRepository.getCurrentUserId()
    }
}