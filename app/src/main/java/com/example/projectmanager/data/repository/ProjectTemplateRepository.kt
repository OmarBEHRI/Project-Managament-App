package com.example.projectmanager.data.repository

import com.example.projectmanager.data.model.ProjectTemplate
import com.example.projectmanager.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProjectTemplateRepository {
    suspend fun getTemplates(): Flow<Resource<List<ProjectTemplate>>>
    suspend fun getTemplate(id: String): Resource<ProjectTemplate>
    suspend fun createTemplate(template: ProjectTemplate): Resource<ProjectTemplate>
    suspend fun updateTemplate(template: ProjectTemplate): Resource<ProjectTemplate>
    suspend fun deleteTemplate(id: String): Resource<Unit>
    suspend fun createTemplateFromProject(projectId: String): Resource<ProjectTemplate>
} 