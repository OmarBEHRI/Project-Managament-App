package com.example.projectmanager.data.remote.firebase

import com.example.projectmanager.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun createUser(user: User): Result<User> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<User> {
        return try {
            usersCollection.document(user
                .id).set(user, SetOptions.merge()).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()

            if (documentSnapshot.exists()) {
                Result.success(documentSnapshot.toObject<User>())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByEmail(email: String): Result<User?> {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            val users = parseUsers(querySnapshot)
            Result.success(users.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val querySnapshot = usersCollection.get().await()
            Result.success(parseUsers(querySnapshot))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsersByName(query: String): Result<List<User>> {
        return try {
            // Firestore doesn't support direct LIKE queries, so we use range operators
            val querySnapshot = usersCollection
                .orderBy("displayName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()

            Result.success(parseUsers(querySnapshot))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "isOnline" to isOnline,
                "lastActive" to if (!isOnline) System.currentTimeMillis() else 0
            )

            usersCollection.document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsersByProjectId(projectId: String): Result<List<User>> {
        return try {
            // This assumes there's a separate collection for project memberships
            val projectMembersRef = firestore.collection("project_members")
                .whereEqualTo("projectId", projectId)
                .get()
                .await()

            val userIds = projectMembersRef.documents.mapNotNull { it.getString("userId") }

            // Firestore doesn't support direct 'IN' queries with more than 10 values
            // So we'll batch the fetches if needed
            val batchSize = 10
            val usersList = mutableListOf<User>()

            for (i in userIds.indices step batchSize) {
                val endIndex = minOf(i + batchSize, userIds.size)
                val batch = userIds.subList(i, endIndex)

                val batchUsers = usersCollection
                    .whereIn("userId", batch)
                    .get()
                    .await()

                usersList.addAll(parseUsers(batchUsers))
            }

            Result.success(usersList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseUsers(querySnapshot: QuerySnapshot): List<User> {
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject<User>()
        }
    }
}