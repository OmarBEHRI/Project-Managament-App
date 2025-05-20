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

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun signIn(email: String, password: String): Resource<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user?.let {
            Resource.success(it)
        } ?: Resource.error("Sign in failed")
    } catch (e: Exception) {
        Resource.error(e.message ?: "Sign in failed")
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Resource<FirebaseUser> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.let { firebaseUser ->
            // Create user profile in Firestore
            val user = User(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName
            )
            firestore.collection("users").document(firebaseUser.uid).set(user).await()
            // Send email verification
            firebaseUser.sendEmailVerification().await()
            Resource.success(firebaseUser)
        } ?: Resource.error("Sign up failed")
    } catch (e: Exception) {
        Resource.error(e.message ?: "Sign up failed")
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override fun getUserFlow(): Flow<Resource<User?>> = flow {
        emit(Resource.loading())
        try {
            auth.currentUser?.let { firebaseUser ->
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val user = userDoc.toObject(User::class.java)
                emit(Resource.success(user))
            } ?: emit(Resource.success(null))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Failed to get user"))
        }
    }

    override suspend fun updateUserProfile(user: User): Resource<User> = try {
        firestore.collection("users").document(user.id).set(user).await()
        Resource.success(user)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to update profile")
    }

    override suspend fun sendEmailVerification(): Resource<Boolean> = try {
        auth.currentUser?.let { user ->
            user.sendEmailVerification().await()
            Resource.success(true)
        } ?: Resource.error("No user signed in")
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to send verification email")
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Boolean> = try {
        auth.sendPasswordResetEmail(email).await()
        Resource.success(true)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to send password reset email")
    }

    override suspend fun verifyPasswordResetCode(code: String): Resource<String> = try {
        val email = auth.verifyPasswordResetCode(code).await()
        Resource.success(email)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Invalid reset code")
    }

    override suspend fun confirmPasswordReset(code: String, newPassword: String): Resource<Boolean> = try {
        auth.confirmPasswordReset(code, newPassword).await()
        Resource.success(true)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to reset password")
    }

    override fun isEmailVerified(): Boolean = auth.currentUser?.isEmailVerified ?: false
}