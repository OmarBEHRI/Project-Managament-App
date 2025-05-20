package com.example.projectmanager.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.User
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                userRepository.getCurrentUser().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    user = resource.data,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = resource.message
                                )
                            }
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load profile"
                    )
                }
            }
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Convert User object to Map<String, Any>
                val updates = mapOf(
                    "displayName" to (user.displayName ?: ""),
                    "photoUrl" to (user.photoUrl ?: ""),
                    "phoneNumber" to (user.phoneNumber ?: ""),
                    "bio" to (user.bio ?: ""),
                    "position" to (user.position ?: ""),
                    "department" to (user.department ?: ""),
                    "skills" to (user.skills ?: emptyList<String>())
                )
                
                when (val result = userRepository.updateProfile(updates)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                user = result.data,
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
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update profile"
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                userRepository.signOut()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to sign out")
                }
            }
        }
    }
} 