package com.example.projectmanager.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var theme: String
        get() = prefs.getString(KEY_THEME, "system") ?: "system"
        set(value) = prefs.edit { putString(KEY_THEME, value) }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit { putBoolean(KEY_DARK_MODE, value) }

    var emailNotifications: Boolean
        get() = prefs.getBoolean(KEY_EMAIL_NOTIFICATIONS, true)
        set(value) = prefs.edit { putBoolean(KEY_EMAIL_NOTIFICATIONS, value) }

    var pushNotifications: Boolean
        get() = prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true)
        set(value) = prefs.edit { putBoolean(KEY_PUSH_NOTIFICATIONS, value) }

    var defaultProjectView: String
        get() = prefs.getString(KEY_DEFAULT_PROJECT_VIEW, "list") ?: "list"
        set(value) = prefs.edit { putString(KEY_DEFAULT_PROJECT_VIEW, value) }

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit { putString(KEY_LANGUAGE, value) }

    var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(value) = prefs.edit { putLong(KEY_LAST_SYNC, value) }

    fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "project_manager_prefs"
        private const val KEY_THEME = "theme"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_EMAIL_NOTIFICATIONS = "email_notifications"
        private const val KEY_PUSH_NOTIFICATIONS = "push_notifications"
        private const val KEY_DEFAULT_PROJECT_VIEW = "default_project_view"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_LAST_SYNC = "last_sync"
    }
} 