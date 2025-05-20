package com.example.projectmanager.data.remote.firebase

import com.example.projectmanager.data.model.Project
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreProjectSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val projectsCollection = firestore.collection("projects")

    suspend fun createProject(project: Project): String {
        val projectRef = if (project.id.isEmpty()) {
            projectsCollection.document()
        } else {
            projectsCollection.document(project.id)
        }

        val newProject = if (project.id.isEmpty()) {
            project.copy(id = projectRef.id)
        } else {
            project
        }

        projectRef.set(newProject).await()
        return newProject.id
    }

    suspend fun getProject(projectId: String): Project {
        val snapshot = projectsCollection.document(projectId).get().await()
        return snapshot.toObject(Project::class.java)
            ?: throw IllegalStateException("Project not found")
    }

    fun getProjectsForUser(userId: String): Flow<List<Project>> = callbackFlow {
        val listenerRegistration = projectsCollection
            .whereArrayContains("memberIds", userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val projects = snapshot?.documents?.mapNotNull {
                    it.toObject(Project::class.java)
                } ?: emptyList()

                trySend(projects)
            }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun updateProject(project: Project) {
        projectsCollection.document(project.id).set(project).await()
    }

    suspend fun deleteProject(projectId: String) {
        projectsCollection.document(projectId).delete().await()
    }
}