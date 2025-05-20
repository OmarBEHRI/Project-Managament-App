package com.example.projectmanager.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.User
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _authState = MutableStateFlow<Resource<User>>(Resource.loading())
    val authState: StateFlow<Resource<User>> = _authState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            userRepository.observeAuthState().collect { firebaseUser ->
                if (firebaseUser != null) {
                    userRepository.getCurrentUser().collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                _uiState.update {
                                    it.copy(
                                        isAuthenticated = true,
                                        user = resource.data,
                                        isLoading = false,
                                        error = null
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isAuthenticated = false,
                                        user = null,
                                        isLoading = false,
                                        error = resource.message
                                    )
                                }
                            }
                            is Resource.Loading -> {
                                _uiState.update {
                                    it.copy(isLoading = true, error = null)
                                }
                            }
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isAuthenticated = false,
                            user = null,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            _authState.value = Resource.loading()
            try {
                when (val result = userRepository.signIn(email, password)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isAuthenticated = true,
                                user = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                        _authState.value = result
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        _authState.value = result
                    }
                    is Resource.Loading -> {
                        _uiState.update {
                            it.copy(isLoading = true, error = null)
                        }
                        _authState.value = Resource.loading()
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Sign in failed"
                    )
                }
                _authState.value = Resource.Error(e.message ?: "Sign in failed")
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            _authState.value = Resource.loading()
            try {
                when (val result = userRepository.signUp(email, password, displayName)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isAuthenticated = true,
                                user = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                        _authState.value = result
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        _authState.value = result
                    }
                    is Resource.Loading -> {
                        _uiState.update {
                            it.copy(isLoading = true, error = null)
                        }
                        _authState.value = Resource.loading()
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Sign up failed"
                    )
                }
                _authState.value = Resource.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                when (val result = userRepository.resetPassword(email)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
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
                            it.copy(isLoading = true, error = null)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Password reset failed"
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            userRepository.signOut()
            _uiState.update {
                it.copy(
                    isAuthenticated = false,
                    user = null,
                    isLoading = false,
                    error = null
                )
            }
        }
    }
}