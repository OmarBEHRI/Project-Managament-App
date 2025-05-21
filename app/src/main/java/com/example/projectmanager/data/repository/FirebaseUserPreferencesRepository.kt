package com.example.projectmanager.data.repository

import com.example.projectmanager.data.model.UserPreferences
import com.example.projectmanager.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUserPreferencesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) : UserPreferencesRepository {

    private val usersCollection = firestore.collection("users")
    
    override suspend fun getUserPreferences(): Resource<UserPreferences> {
        return try {
            val userId = userRepository.getCurrentUserId()
            val userDoc = usersCollection.document(userId).get().await()
            
            val preferences = userDoc.get("preferences") as? Map<String, Any>
            if (preferences != null) {
                val userPrefs = UserPreferences(
                    theme = preferences["theme"] as? String ?: "system",
                    emailNotifications = preferences["emailNotifications"] as? Boolean ?: true,
                    pushNotifications = preferences["pushNotifications"] as? Boolean ?: true,
                    defaultProjectView = preferences["default_project_view"] as? String ?: "list",
                    language = preferences["language"] as? String ?: "en"
                )
                Resource.Success(userPrefs)
            } else {
                // Return default preferences if none exist
                Resource.Success(UserPreferences())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get user preferences")
        }
    }

    override suspend fun updateUserPreferences(preferences: UserPreferences): Resource<UserPreferences> {
        return try {
            val userId = userRepository.getCurrentUserId()
            usersCollection.document(userId)
                .update("preferences", preferences)
                .await()
            
            Resource.Success(preferences)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update user preferences")
        }
    }

    override suspend fun getMutedChats(): Set<String> {
        return try {
            val userId = userRepository.getCurrentUserId()
            val userDoc = usersCollection.document(userId).get().await()
            
            val mutedChats = userDoc.get("muted_chats") as? List<String>
            mutedChats?.toSet() ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    override suspend fun muteChat(chatId: String): Resource<Unit> {
        return try {
            val userId = userRepository.getCurrentUserId()
            val currentMutedChats = getMutedChats().toMutableSet()
            currentMutedChats.add(chatId)
            
            usersCollection.document(userId)
                .update("muted_chats", currentMutedChats.toList())
                .await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mute chat")
        }
    }

    override suspend fun unmuteChat(chatId: String): Resource<Unit> {
        return try {
            val userId = userRepository.getCurrentUserId()
            val currentMutedChats = getMutedChats().toMutableSet()
            currentMutedChats.remove(chatId)
            
            usersCollection.document(userId)
                .update("muted_chats", currentMutedChats.toList())
                .await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unmute chat")
        }
    }
}
