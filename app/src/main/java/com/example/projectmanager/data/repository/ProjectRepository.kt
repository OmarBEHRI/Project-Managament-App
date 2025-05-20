package com.example.projectmanager.data.repository

import com.example.projectmanager.data.model.Comment
import com.example.projectmanager.data.model.FileAttachment
import com.example.projectmanager.data.model.Project
import com.example.projectmanager.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getProjectsByUser(userId: String): Flow<Resource<List<Project>>>
    fun getProjectById(projectId: String): Flow<Project?>
    suspend fun createProject(project: Project): Resource<Project>
    suspend fun updateProject(project: Project): Resource<Project>
    suspend fun deleteProject(projectId: String): Resource<Unit>
    fun getRecentProjects(limit: Int): Flow<List<Project>>
    suspend fun addMemberToProject(projectId: String, userId: String): Resource<Unit>
    suspend fun removeMemberFromProject(projectId: String, userId: String): Resource<Unit>
    suspend fun get(id: String): Resource<Project>
    suspend fun create(item: Project): Resource<Project>
    suspend fun update(item: Project): Resource<Project>
    suspend fun delete(id: String): Resource<Boolean>
    fun getAll(): Flow<Resource<List<Project>>>
    fun getStream(id: String): Flow<Resource<Project>>
    suspend fun syncProjects()
    suspend fun syncProject(projectId: String)

    // Comments related methods
    fun getProjectComments(projectId: String): Flow<List<Comment>>
    suspend fun addComment(comment: Comment): Resource<Comment>

    // Attachments related methods
    fun getProjectAttachments(projectId: String): Flow<List<FileAttachment>>
    suspend fun uploadAttachment(
        projectId: String,
        fileName: String,
        fileSize: Long,
        mimeType: String,
        fileUri: String,
        uploadedBy: String
    ): Resource<FileAttachment>
}