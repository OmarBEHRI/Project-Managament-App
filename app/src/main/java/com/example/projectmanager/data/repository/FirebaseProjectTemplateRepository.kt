package com.example.projectmanager.data.repository

import com.example.projectmanager.data.model.ProjectTemplate
import com.example.projectmanager.data.model.Project
import com.example.projectmanager.data.model.TaskTemplate
import com.example.projectmanager.data.model.MilestoneTemplate
import com.example.projectmanager.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseProjectTemplateRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val projectRepository: ProjectRepository
) : ProjectTemplateRepository {

    private val templatesCollection = firestore.collection("templates")

    override suspend fun getTemplates(): Flow<Resource<List<ProjectTemplate>>> = flow {
        try {
            emit(Resource.Loading)
            val snapshot = templatesCollection.get().await()
            val templates = snapshot.documents.mapNotNull { it.toObject<ProjectTemplate>() }
            emit(Resource.Success(templates))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load templates"))
        }
    }

    override suspend fun getTemplate(id: String): Resource<ProjectTemplate> {
        return try {
            val document = templatesCollection.document(id).get().await()
            document.toObject<ProjectTemplate>()?.let {
                Resource.Success(it)
            } ?: Resource.Error("Template not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load template")
        }
    }

    override suspend fun createTemplate(template: ProjectTemplate): Resource<ProjectTemplate> {
        return try {
            val documentRef = templatesCollection.document()
            val templateWithId = template.copy(id = documentRef.id)
            documentRef.set(templateWithId).await()
            Resource.Success(templateWithId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create template")
        }
    }

    override suspend fun updateTemplate(template: ProjectTemplate): Resource<ProjectTemplate> {
        return try {
            templatesCollection.document(template.id).set(template).await()
            Resource.Success(template)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update template")
        }
    }

    override suspend fun deleteTemplate(id: String): Resource<Unit> {
        return try {
            templatesCollection.document(id).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete template")
        }
    }

    override suspend fun createTemplateFromProject(projectId: String): Resource<ProjectTemplate> {
        return try {
            val projectResult = projectRepository.get(projectId)
            when (projectResult) {
                is Resource.Success -> {
                    // Fetch tasks from the project's subcollection
                    val tasksSnapshot = firestore.collection("projects")
                        .document(projectId)
                        .collection("tasks")
                        .get()
                        .await()
                    val tasks = tasksSnapshot.documents.mapNotNull {
                        it.toObject(TaskTemplate::class.java)
                    }

                    val template = ProjectTemplate(
                        name = projectResult.data.name,
                        description = projectResult.data.description,
                        tasks = tasks
                    )
                    createTemplate(template)
                }
                is Resource.Error -> Resource.Error(projectResult.message)
                is Resource.Loading -> Resource.Error("Unexpected loading state")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create template from project")
        }
    }
} 