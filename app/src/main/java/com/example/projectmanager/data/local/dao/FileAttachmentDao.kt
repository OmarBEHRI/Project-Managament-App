package com.example.projectmanager.data.local.dao


import androidx.room.*
import com.example.projectmanager.data.local.entity.FileAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileAttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFileAttachment(fileAttachment: FileAttachmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFileAttachments(fileAttachments: List<FileAttachmentEntity>)

    @Update
    suspend fun updateFileAttachment(fileAttachment: FileAttachmentEntity)

    @Delete
    suspend fun deleteFileAttachment(fileAttachment: FileAttachmentEntity)

    @Query("DELETE FROM file_attachments WHERE id = :fileId")
    suspend fun deleteFileAttachmentById(fileId: String)

    @Query("SELECT * FROM file_attachments WHERE id = :fileId")
    fun getFileAttachmentById(fileId: String): Flow<FileAttachmentEntity?>

    @Query("SELECT * FROM file_attachments WHERE taskId = :taskId")
    fun getFileAttachmentsByTaskId(taskId: String): Flow<List<FileAttachmentEntity>>

    @Query("SELECT * FROM file_attachments WHERE projectId = :projectId")
    fun getFileAttachmentsByProjectId(projectId: String): Flow<List<FileAttachmentEntity>>

    @Query("SELECT * FROM file_attachments WHERE commentId = :commentId")
    fun getFileAttachmentsByCommentId(commentId: String): Flow<List<FileAttachmentEntity>>

    @Query("SELECT * FROM file_attachments WHERE uploadedById = :userId")
    fun getFileAttachmentsByUserId(userId: String): Flow<List<FileAttachmentEntity>>

    @Query("SELECT * FROM file_attachments WHERE fileName LIKE '%' || :query || '%'")
    fun searchFileAttachmentsByName(query: String): Flow<List<FileAttachmentEntity>>

    @Query("SELECT * FROM file_attachments WHERE fileType IN (:fileTypes)")
    fun getFileAttachmentsByType(fileTypes: List<String>): Flow<List<FileAttachmentEntity>>

    @Query("DELETE FROM file_attachments WHERE taskId = :taskId")
    suspend fun deleteFileAttachmentsByTaskId(taskId: String)

    @Query("DELETE FROM file_attachments WHERE projectId = :projectId")
    suspend fun deleteFileAttachmentsByProjectId(projectId: String)

    @Query("DELETE FROM file_attachments WHERE commentId = :commentId")
    suspend fun deleteFileAttachmentsByCommentId(commentId: String)
}