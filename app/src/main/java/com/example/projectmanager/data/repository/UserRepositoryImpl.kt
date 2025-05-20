package com.example.projectmanager.data.repository

import com.example.projectmanager.data.local.dao.UserDao
import com.example.projectmanager.data.local.entity.UserEntity
import com.example.projectmanager.data.model.User
import com.example.projectmanager.data.model.UserPreferences
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

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuthSource: FirebaseAuthSource,
    private val firestoreUserSource: FirestoreUserSource,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    val currentUser: User?
        get() {
            val firebaseUser = firebaseAuthSource.currentUser ?: return null
            return User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString()
            )
        }

    override fun getCurrentUser(): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val userDoc = usersCollection.document(firebaseUser.uid).get().await()
                val user = userDoc.toObject(User::class.java)
                if (user != null) {
                    emit(Resource.Success(user))
                } else {
                    emit(Resource.Error("User data not found"))
                }
            } else {
                emit(Resource.Error("No user signed in"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get current user"))
        }
    }

    override fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { authInstance ->
            trySend(authInstance.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): Resource<User> {
        return try {
            val firebaseUser = firebaseAuthSource.signIn(email, password)
            val userDoc = usersCollection.document(firebaseUser.uid).get().await()
            userDoc.toObject(User::class.java)?.let {
                Resource.Success(it)
            } ?: Resource.Error("User data not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign in failed")
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String): Resource<User> {
        return try {
            val firebaseUser = firebaseAuthSource.signUp(email, password)
            val user = User(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName,
                photoUrl = null,
                isEmailVerified = false,
                lastActive = Timestamp.now()
            )
            firestoreUserSource.createUser(user)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign up failed")
        }
    }

    override suspend fun signOut() {
        firebaseAuthSource.signOut()
        // Clear any cached user data
        try {
            // Clear local user data if necessary
            // For example: Clear in-memory cache or session data
        } catch (e: Exception) {
            // Log error but continue with sign out
            Timber.e(e, "Error clearing local data during sign out")
        }
    }

    fun getProjectMembers(projectId: String, projectMemberIds: List<String>): Flow<List<User>> = flow {
        try {
            val users = projectMemberIds.mapNotNull { memberId ->
                firestore.collection("users")
                    .document(memberId)
                    .get()
                    .await()
                    .toObject(User::class.java)
            }
            emit(users)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun updateUserProfile(user: User) {
        firestoreUserSource.updateUser(user)
        userDao.insertUser(UserEntity.fromDomain(user))
    }

    suspend fun updateUserSkills(userId: String, skills: List<String>) {
        val userDoc = firestore.collection("users").document(userId).get().await()
        val user = userDoc.toObject(User::class.java) ?: throw Exception("User not found")
        val updatedUser = user.copy(skills = skills)
        firestoreUserSource.updateUser(updatedUser)
        userDao.insertUser(UserEntity.fromDomain(updatedUser))
    }

    override suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send reset password email")
        }
    }

    override suspend fun updateProfile(updates: Map<String, Any>): Resource<User> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("No user signed in")
            val userDoc = usersCollection.document(currentUser.uid)
            userDoc.update(updates).await()
            val updatedUser = userDoc.get().await().toObject(User::class.java)
                ?: return Resource.Error("Failed to get updated user data")
            Resource.Success(updatedUser)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }

    override suspend fun deleteAccount(): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("No user signed in")
            usersCollection.document(currentUser.uid).delete().await()
            currentUser.delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete account")
        }
    }

    override suspend fun verifyEmail(): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("No user signed in")
            currentUser.sendEmailVerification().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send verification email")
        }
    }

    override suspend fun updateUserPreferences(updates: Map<String, Any>): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("No user signed in")
            val userDoc = usersCollection.document(currentUser.uid)
            userDoc.update("preferences", updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update preferences")
        }
    }

    override suspend fun updateFcmToken(userId: String, token: String): Resource<Unit> {
        return try {
            val userDoc = usersCollection.document(userId)
            userDoc.update("fcmToken", token).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update FCM token")
        }
    }

    override suspend fun updateFcmToken(token: String): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("No user signed in")
            val userDoc = usersCollection.document(currentUser.uid)
            userDoc.update("fcmToken", token).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update FCM token")
        }
    }

    override suspend fun syncUsers() {
        try {
            // Sync all users from Firestore to local database
            val querySnapshot = usersCollection.get().await()
            val users = querySnapshot.documents.mapNotNull {
                it.toObject(User::class.java)
            }

            // Save to local database
            users.forEach { user ->
                userDao.insertUser(UserEntity.fromDomain(user))
            }

            Timber.d("Synced ${users.size} users")
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync users")
            throw e
        }
    }

    override suspend fun syncUser(userId: String) {
        try {
            val userDoc = usersCollection.document(userId).get().await()
            val user = userDoc.toObject(User::class.java)

            if (user != null) {
                userDao.insertUser(UserEntity.fromDomain(user))
                Timber.d("Synced user: ${user.id}")
            } else {
                Timber.w("User not found: $userId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync user: $userId")
            throw e
        }
    }

    override fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("No user signed in")
    }

    // User search and retrieval methods
    override fun getUserById(userId: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val userDoc = usersCollection.document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
            if (user != null) {
                // Cache user in local database
                userDao.insertUser(UserEntity.fromDomain(user))
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("User not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user by ID: $userId")
            emit(Resource.Error(e.message ?: "Failed to get user"))
            
            // Try to get from local cache as fallback
            try {
                userDao.getUserById(userId).collect { userEntity ->
                    userEntity?.let { entity ->
                        // Create User domain model from UserEntity
                        val user = User(
                            id = entity.id,
                            email = entity.email,
                            displayName = entity.displayName,
                            photoUrl = entity.photoUrl,
                            isEmailVerified = entity.isEmailVerified,
                            role = entity.role,
                            createdAt = entity.createdAt?.let { java.util.Date(it) },
                            lastLoginAt = entity.lastLoginAt?.let { java.util.Date(it) },
                            fcmToken = entity.fcmToken,
                            preferences = com.example.projectmanager.data.model.UserPreferences(
                                theme = entity.theme,
                                emailNotifications = entity.emailNotifications,
                                pushNotifications = entity.pushNotifications,
                                defaultProjectView = entity.defaultProjectView,
                                language = entity.language
                            )
                        )
                        emit(Resource.Success(user))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error getting user from local cache")
            }
        }
    }
    
    override fun searchUsers(query: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading)
        try {
            // Search by displayName or email containing the query string
            // This is a simple implementation - in a real app, you might want to use
            // a more sophisticated search mechanism like Firestore's array-contains
            // or a dedicated search service
            val querySnapshot = usersCollection
                .orderBy("displayName")
                .get()
                .await()
                
            val allUsers = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)
            }
            
            // Filter users whose displayName or email contains the query (case insensitive)
            val lowerQuery = query.lowercase()
            val filteredUsers = allUsers.filter { user ->
                user.displayName.lowercase().contains(lowerQuery) ||
                user.email.lowercase().contains(lowerQuery)
            }
            
            emit(Resource.Success(filteredUsers))
        } catch (e: Exception) {
            Timber.e(e, "Error searching users with query: $query")
            emit(Resource.Error(e.message ?: "Failed to search users"))
        }
    }
}