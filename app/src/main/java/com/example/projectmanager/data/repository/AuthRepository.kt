package com.example.projectmanager.data.repository

import com.example.projectmanager.data.model.User
import com.example.projectmanager.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    val currentUser: FirebaseUser?
    fun getCurrentUserId(): String?
    suspend fun signIn(email: String, password: String): Resource<FirebaseUser>
    suspend fun signUp(email: String, password: String, displayName: String): Resource<FirebaseUser>
    suspend fun signOut()
    fun getUserFlow(): Flow<Resource<User?>>
    suspend fun updateUserProfile(user: User): Resource<User>
    suspend fun sendEmailVerification(): Resource<Boolean>
    suspend fun sendPasswordResetEmail(email: String): Resource<Boolean>
    suspend fun verifyPasswordResetCode(code: String): Resource<String>
    suspend fun confirmPasswordReset(code: String, newPassword: String): Resource<Boolean>
    fun isEmailVerified(): Boolean
}

