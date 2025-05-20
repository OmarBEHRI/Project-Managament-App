package com.example.projectmanager.data.remote.firebase

import com.example.projectmanager.data.model.Comment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCommentSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val commentsCollection = firestore.collection("comments")

    suspend fun addComment(comment: Comment): Result<Comment> {
        return try {
            val commentWithId = if (comment.id.isBlank()) {
                comment.copy(id = commentsCollection.document().id)
            } else {
                comment
            }

            commentsCollection.document(commentWithId.id).set(commentWithId).await()
            Result.success(commentWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateComment(comment: Comment): Result<Comment> {
        return try {
            commentsCollection.document(comment.id).set(comment).await()
            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(commentId: String): Result<Unit> {
        return try {
            commentsCollection.document(commentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCommentsByTaskId(taskId: String): Result<List<Comment>> {
        return try {
            val querySnapshot = commentsCollection
                .whereEqualTo("taskId", taskId)
                .orderBy("createdAt")
                .get()
                .await()

            Result.success(parseComments(querySnapshot))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCommentById(commentId: String): Result<Comment?> {
        return try {
            val documentSnapshot = commentsCollection.document(commentId).get().await()

            if (documentSnapshot.exists()) {
                Result.success(documentSnapshot.toObject<Comment>())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCommentsByUserId(userId: String): Result<List<Comment>> {
        return try {
            val querySnapshot = commentsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            Result.success(parseComments(querySnapshot))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseComments(querySnapshot: QuerySnapshot): List<Comment> {
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject<Comment>()
        }
    }
}