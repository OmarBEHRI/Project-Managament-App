package com.example.projectmanager.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    private val storageRef = storage.reference

    suspend fun uploadFile(file: File, path: String): String {
        val fileRef = storageRef.child(path)
        val uploadTask = fileRef.putFile(Uri.fromFile(file)).await()
        return fileRef.downloadUrl.await().toString()
    }

    suspend fun deleteFile(path: String) {
        storageRef.child(path).delete().await()
    }

    suspend fun getDownloadUrl(path: String): String {
        return storageRef.child(path).downloadUrl.await().toString()
    }
}