package com.example.projectmanager.data.local.dao

import androidx.room.*
import com.example.projectmanager.data.local.entity.CommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Update
    suspend fun updateComment(comment: CommentEntity)

    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: String)

    @Query("DELETE FROM comments WHERE taskId = :taskId")
    suspend fun deleteCommentsByTaskId(taskId: String)

    @Query("SELECT * FROM comments WHERE id = :commentId")
    fun getCommentById(commentId: String): Flow<CommentEntity?>

    @Query("SELECT * FROM comments WHERE taskId = :taskId ORDER BY createdAt ASC")
    fun getCommentsByTaskId(taskId: String): Flow<List<CommentEntity>>

    @Transaction
    @Query("""
        SELECT c.* FROM comments c 
        INNER JOIN tasks t ON c.taskId = t.id 
        WHERE t.projectId = :projectId 
        ORDER BY c.createdAt DESC
    """)
    fun getCommentsByProjectId(projectId: String): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE userId = :userId ORDER BY createdAt DESC")
    fun getCommentsByUserId(userId: String): Flow<List<CommentEntity>>
}