package com.example.projectmanager.data.repository

import com.example.projectmanager.data.local.dao.CommentDao
import com.example.projectmanager.data.local.entity.CommentEntity
import com.example.projectmanager.data.model.Comment
import com.example.projectmanager.data.remote.firebase.FirestoreCommentSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val commentDao: CommentDao,
    private val firestoreCommentSource: FirestoreCommentSource
) : CommentRepository {
    override fun getCommentsForTask(taskId: String): Flow<List<Comment>> {
        return commentDao.getCommentsByTaskId(taskId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCommentsByProjectId(projectId: String): Flow<List<Comment>> {
        return commentDao.getCommentsByProjectId(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCommentsByUserId(userId: String): Flow<List<Comment>> {
        return commentDao.getCommentsByUserId(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addComment(comment: Comment): String {
        val result = firestoreCommentSource.addComment(comment)
        if (result.isSuccess) {
            val commentWithId = result.getOrThrow()
            commentDao.insertComment(CommentEntity.fromDomain(commentWithId))
            return commentWithId.id
        } else {
            throw result.exceptionOrNull() ?: Exception("Failed to add comment")
        }
    }

    override suspend fun updateComment(comment: Comment) {
        firestoreCommentSource.updateComment(comment)
        commentDao.updateComment(CommentEntity.fromDomain(comment))
    }

    override suspend fun deleteComment(commentId: String) {
        firestoreCommentSource.deleteComment(commentId)
        commentDao.deleteCommentById(commentId)
    }
}