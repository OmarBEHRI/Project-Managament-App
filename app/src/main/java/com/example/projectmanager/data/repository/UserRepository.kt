package com.example.projectmanager.data.repository

import com.example.projectmanager.data.local.dao.UserDao
import com.example.projectmanager.data.local.entity.UserEntity
import com.example.projectmanager.data.model.Project
import com.example.projectmanager.data.model.User
import com.example.projectmanager.data.remote.firebase.FirebaseAuthSource
import com.example.projectmanager.data.remote.firebase.FirestoreUserSource
import com.example.projectmanager.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    fun getCurrentUser(): Flow<Resource<User>>
    fun observeAuthState(): Flow<FirebaseUser?>
    suspend fun signIn(email: String, password: String): Resource<User>
    suspend fun signUp(email: String, password: String, displayName: String): Resource<User>
    suspend fun signOut()
    suspend fun resetPassword(email: String): Resource<Unit>
    suspend fun updateProfile(updates: Map<String, Any>): Resource<User>
    suspend fun deleteAccount(): Resource<Unit>
    suspend fun verifyEmail(): Resource<Unit>
    suspend fun updateUserPreferences(updates: Map<String, Any>): Resource<Unit>
    suspend fun updateFcmToken(userId: String, token: String): Resource<Unit>
    suspend fun updateFcmToken(token: String): Resource<Unit>
    suspend fun syncUsers()
    suspend fun syncUser(userId: String)
    fun getCurrentUserId(): String
    
    // User search and retrieval
    fun getUserById(userId: String): Flow<Resource<User>>
    fun searchUsers(query: String): Flow<Resource<List<User>>>
}
