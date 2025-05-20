package com.example.projectmanager.data.remote.firebase

import com.example.projectmanager.data.model.FileAttachment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreFileSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val filesCollection = firestore.collection("files")

    suspend fun addFile(file: FileAttachment): Result<FileAttachment> {
        return try {
            val fileWithId = if (file.id.isBlank()) {
                file.copy(id = filesCollection.document().id)
            } else {
                file
            }

            filesCollection.document(fileWithId.id).set(fileWithId).await()
            Result.success(fileWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFile(file: FileAttachment): Result<FileAttachment> {
        return try {
            filesCollection.document(file.id).set(file).await()
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFile(fileId: String): Result<Unit> {
        return try {
            filesCollection.document(fileId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFileById(fileId: String): Result<FileAttachment?> {
        return try {
            val documentSnapshot = filesCollection.document(fileId).get().await()

            if (documentSnapshot.exists()) {
                Result.success(documentSnapshot.toObject<FileAttachment>())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFilesByProjectId(projectId: String): Result<List<FileAttachment>> {
        return try {
            val querySnapshot = filesCollection
                .whereEqualTo("projectId", projectId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            Result.success(parseFiles(querySnapshot))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFilesByTaskId(taskId: String): Result<List<FileAttachment>> {
        return try {
            val querySnapshot = filesCollection
                .whereEqualTo("taskId", taskId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            Result.success(parseFiles(querySnapshot))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFilesByUploaderId(uploaderId: String): Result<List<FileAttachment>> {
        return try {
            val querySnapshot = filesCollection
                .whereEqualTo("uploaderId", uploaderId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            Result.success(parseFiles(querySnapshot))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchFilesByName(query: String): Result<List<FileAttachment>> {
        return try {
            // Firestore doesn't support LIKE queries, so we use range operators
            val querySnapshot = filesCollection
                .orderBy("fileName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()

            Result.success(parseFiles(querySnapshot))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseFiles(querySnapshot: QuerySnapshot): List<FileAttachment> {
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject<FileAttachment>()
        }
    }

    suspend fun saveFileMetadata(file: FileAttachment): String {
        val fileId = filesCollection.document().id
        file.copy(id = fileId)
        filesCollection.document(fileId).set(file)
        return fileId
    }

    suspend fun deleteFileMetadata(fileId: String) {
        filesCollection.document(fileId).delete()
    }
}