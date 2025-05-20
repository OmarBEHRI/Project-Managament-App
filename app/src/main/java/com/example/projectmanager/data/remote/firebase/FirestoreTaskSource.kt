package com.example.projectmanager.data.remote.firebase

import com.example.projectmanager.data.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreTaskSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val tasksCollection = firestore.collection("tasks")

    suspend fun createTask(task: Task): String {
        val taskRef = if (task.id.isEmpty()) {
            tasksCollection.document()
        } else {
            tasksCollection.document(task.id)
        }

        val newTask = if (task.id.isEmpty()) {
            task.copy(id = taskRef.id)
        } else {
            task
        }

        taskRef.set(newTask).await()
        return newTask.id
    }

    suspend fun getTask(taskId: String): Task {
        val snapshot = tasksCollection.document(taskId).get().await()
        return snapshot.toObject(Task::class.java)
            ?: throw IllegalStateException("Task not found")
    }

    fun getTasksForProject(projectId: String): Flow<List<Task>> = callbackFlow {
        val listenerRegistration = tasksCollection
            .whereEqualTo("projectId", projectId)
            .orderBy("deadline")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents?.mapNotNull {
                    it.toObject(Task::class.java)
                } ?: emptyList()

                trySend(tasks)
            }

        awaitClose { listenerRegistration.remove() }
    }

    fun getTasksAssignedToUser(userId: String): Flow<List<Task>> = callbackFlow {
        val listenerRegistration = tasksCollection
            .whereEqualTo("assigneeId", userId)
            .orderBy("deadline")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents?.mapNotNull {
                    it.toObject(Task::class.java)
                } ?: emptyList()

                trySend(tasks)
            }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun updateTask(task: Task) {
        tasksCollection.document(task.id).set(task).await()
    }

    suspend fun deleteTask(taskId: String) {
        tasksCollection.document(taskId).delete().await()
    }
}