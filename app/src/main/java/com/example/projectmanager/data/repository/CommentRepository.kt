package com.example.projectmanager.data.repository

import com.example.projectmanager.data.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun getCommentsForTask(taskId: String): Flow<List<Comment>>
    fun getCommentsByProjectId(projectId: String): Flow<List<Comment>>
    fun getCommentsByUserId(userId: String): Flow<List<Comment>>
    suspend fun addComment(comment: Comment): String
    suspend fun updateComment(comment: Comment)
    suspend fun deleteComment(commentId: String)
}