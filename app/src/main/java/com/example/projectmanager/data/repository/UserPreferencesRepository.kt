package com.example.projectmanager.data.repository

import com.example.projectmanager.data.model.UserPreferences
import com.example.projectmanager.util.Resource

interface UserPreferencesRepository {
    suspend fun getUserPreferences(): Resource<UserPreferences>
    suspend fun updateUserPreferences(preferences: UserPreferences): Resource<UserPreferences>
    suspend fun getMutedChats(): Set<String>
    suspend fun muteChat(chatId: String): Resource<Unit>
    suspend fun unmuteChat(chatId: String): Resource<Unit>
}
