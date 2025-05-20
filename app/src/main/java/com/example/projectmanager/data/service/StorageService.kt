package com.example.projectmanager.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.projectmanager.data.model.FileAttachment
import com.example.projectmanager.util.Resource
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) {
    private val storageRef = storage.reference

    fun uploadFile(
        uri: Uri,
        projectId: String,
        taskId: String? = null,
        commentId: String? = null
    ): Flow<Resource<FileAttachment>> = flow {
        try {
            emit(Resource.Loading)

            // Get file metadata
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: ""
            val fileName = getFileName(uri)
            val fileSize = getFileSize(uri)

            // Generate storage path
            val storagePath = buildStoragePath(projectId, taskId, commentId, fileName)
            val fileRef = storageRef.child(storagePath)

            // Upload file
            val uploadTask = fileRef.putFile(uri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()

            // Generate thumbnail for images
            val thumbnail = if (mimeType.startsWith("image/")) {
                generateThumbnail(uri)?.let { bitmap ->
                    val thumbnailRef = storageRef.child("$storagePath.thumb.jpg")
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    thumbnailRef.putBytes(baos.toByteArray()).await()
                    thumbnailRef.downloadUrl.await().toString()
                }
            } else null

            // Create file attachment
            val attachment = FileAttachment(
                id = UUID.randomUUID().toString(),
                name = fileName,
                type = mimeType.split("/").firstOrNull() ?: "unknown",
                size = fileSize,
                mimeType = mimeType,
                storagePath = storagePath,
                downloadUrl = downloadUrl,
                thumbnail = thumbnail
            )

            emit(Resource.Success(attachment))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to upload file"))
        }
    }.flowOn(Dispatchers.IO)

    fun downloadFile(attachment: FileAttachment): Flow<Resource<File>> = flow {
        try {
            emit(Resource.Loading)

            val localFile = File(context.cacheDir, attachment.name)
            storageRef.child(attachment.storagePath).getFile(localFile).await()

            emit(Resource.Success(localFile))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to download file"))
        }
    }.flowOn(Dispatchers.IO)

    fun deleteFile(attachment: FileAttachment): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading)

            // Delete main file
            storageRef.child(attachment.storagePath).delete().await()

            // Delete thumbnail if exists
            attachment.thumbnail?.let { thumbnailUrl ->
                val thumbnailPath = getThumbnailPath(attachment.storagePath)
                storageRef.child(thumbnailPath).delete().await()
            }

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete file"))
        }
    }.flowOn(Dispatchers.IO)

    private fun getFileName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex("_display_name")
            it.moveToFirst()
            it.getString(nameIndex)
        } ?: uri.lastPathSegment ?: "unknown_file"
    }

    private fun getFileSize(uri: Uri): Long {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val sizeIndex = it.getColumnIndex("_size")
            it.moveToFirst()
            it.getLong(sizeIndex)
        } ?: 0L
    }

    private fun buildStoragePath(
        projectId: String,
        taskId: String?,
        commentId: String?,
        fileName: String
    ): String {
        val pathBuilder = StringBuilder("projects/$projectId")
        taskId?.let { pathBuilder.append("/tasks/$it") }
        commentId?.let { pathBuilder.append("/comments/$it") }
        pathBuilder.append("/files/$fileName")
        return pathBuilder.toString()
    }

    private fun generateThumbnail(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate sample size
            val maxSize = 200
            var sampleSize = 1
            if (options.outHeight > maxSize || options.outWidth > maxSize) {
                val heightRatio = (options.outHeight.toFloat() / maxSize).toInt()
                val widthRatio = (options.outWidth.toFloat() / maxSize).toInt()
                sampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
            }

            // Decode with sample size
            val inputStream2 = context.contentResolver.openInputStream(uri)
            options.apply {
                inJustDecodeBounds = false
                inSampleSize = sampleSize
            }
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
            inputStream2?.close()
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun getThumbnailPath(originalPath: String): String {
        return "$originalPath.thumb.jpg"
    }
} 