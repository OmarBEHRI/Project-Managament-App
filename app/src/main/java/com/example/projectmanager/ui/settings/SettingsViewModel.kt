package com.example.projectmanager.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.local.preferences.AppPreferences
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.navigation.AUTH_GRAPH_ROUTE
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppTheme(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark"),
    DYNAMIC("Material You")
}

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val isDarkMode: Boolean = false,
    val emailNotificationsEnabled: Boolean = true,
    val pushNotificationsEnabled: Boolean = true,
    val defaultProjectView: String = "list",
    val language: String = "en",
    val appVersion: String = "1.0.0",
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    var isSignedOut by mutableStateOf(false)
        private set

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.value = SettingsUiState(
            theme = AppTheme.valueOf(appPreferences.theme.uppercase()),
            isDarkMode = appPreferences.isDarkMode,
            emailNotificationsEnabled = appPreferences.emailNotifications,
            pushNotificationsEnabled = appPreferences.pushNotifications,
            defaultProjectView = appPreferences.defaultProjectView,
            language = appPreferences.language,
            appVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        )
    }

    fun updateTheme(theme: AppTheme) {
        appPreferences.theme = theme.name.lowercase()
        _uiState.value = _uiState.value.copy(theme = theme)
    }

    fun updateDarkMode(enabled: Boolean) {
        appPreferences.isDarkMode = enabled
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
    }

    fun updateEmailNotifications(enabled: Boolean) {
        appPreferences.emailNotifications = enabled
        _uiState.value = _uiState.value.copy(emailNotificationsEnabled = enabled)
    }

    fun updatePushNotifications(enabled: Boolean) {
        appPreferences.pushNotifications = enabled
        _uiState.value = _uiState.value.copy(pushNotificationsEnabled = enabled)
    }

    fun updateDefaultProjectView(view: String) {
        appPreferences.defaultProjectView = view
        _uiState.value = _uiState.value.copy(defaultProjectView = view)
    }

    fun updateLanguage(language: String) {
        appPreferences.language = language
        _uiState.value = _uiState.value.copy(language = language)
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                userRepository.signOut()
                isSignedOut = true
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to sign out")
            }
        }
    }

    fun clearAppData() {
        viewModelScope.launch {
            try {
                appPreferences.clear()
                // Add any other data clearing operations here
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to clear app data")
            }
        }
    }

    fun openPrivacyPolicy() {
        openUrl("https://example.com/privacy-policy")
    }

    fun openTermsOfService() {
        openUrl("https://example.com/terms-of-service")
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to open URL")
        }
    }
}