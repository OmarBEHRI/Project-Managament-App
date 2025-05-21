package com.example.projectmanager.data.repository

import com.example.projectmanager.data.local.dao.TaskDao
import com.example.projectmanager.data.local.entity.TaskEntity
import com.example.projectmanager.data.model.Comment
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskStatus
import com.example.projectmanager.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val firestore: FirebaseFirestore
) : TaskRepository {

    private val tasksCollection = firestore.collection("tasks")

    override fun getAllTasks(): Flow<List<Task>> = flow {
        try {
            val snapshot = tasksCollection
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .get()
                .await()
            val tasks = snapshot.toObjects(Task::class.java)
            emit(tasks)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getTaskById(taskId: String): Flow<Resource<Task>> = flow {
        emit(Resource.loading())
        try {
            val document = tasksCollection.document(taskId).get().await()
            val task = document.toObject(Task::class.java)
            if (task != null) {
                emit(Resource.success(task))
            } else {
                emit(Resource.error("Task not found"))
            }
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Failed to get task"))
        }
    }

    override fun getTasksByProject(projectId: String): Flow<List<Task>> = flow {
        try {
            val snapshot = tasksCollection
                .whereEqualTo("project_id", projectId)
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .get()
                .await()
            val tasks = snapshot.toObjects(Task::class.java)
            emit(tasks)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getTasksByUser(userId: String): Flow<List<Task>> = flow {
        try {
            val snapshot = tasksCollection
                .whereEqualTo("assigned_to", userId)
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .get()
                .await()
            val tasks = snapshot.toObjects(Task::class.java)
            emit(tasks)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getPendingTasks(): Flow<List<Task>> = flow {
        try {
            val snapshot = tasksCollection
                .whereEqualTo("isCompleted", false)
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .get()
                .await()
            val tasks = snapshot.toObjects(Task::class.java)
            emit(tasks)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getOverdueTasks(): Flow<List<Task>> = flow {
        val currentTime = Date().time
        try {
            val tasks = tasksCollection
                .whereEqualTo("isCompleted", false)
                .whereLessThan("dueDate", Date(currentTime))
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Task::class.java) }

            taskDao.insertTasks(tasks.map { TaskEntity.fromDomain(it) })

            taskDao.getOverdueTasks(currentTime).collect { entities ->
                emit(entities.map { it.toDomain() })
            }
        } catch (e: Exception) {
            taskDao.getOverdueTasks(currentTime).collect { entities ->
                emit(entities.map { it.toDomain() })
            }
        }
    }

    override fun getCompletedTasks(limit: Int): Flow<List<Task>> = flow {
        try {
            val snapshot = tasksCollection
                .whereEqualTo("isCompleted", true)
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            val tasks = snapshot.toObjects(Task::class.java)
            emit(tasks)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun createTask(task: Task): Resource<Task> = try {
        val documentRef = tasksCollection.document()
        val newTask = task.copy(id = documentRef.id)
        documentRef.set(newTask).await()
        Resource.success(newTask)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to create task")
    }

    override suspend fun updateTask(task: Task): Resource<Task> = try {
        tasksCollection.document(task.id).set(task).await()
        Resource.success(task)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to update task")
    }

    override suspend fun deleteTask(taskId: String): Resource<Unit> = try {
        tasksCollection.document(taskId).delete().await()
        Resource.success(Unit)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to delete task")
    }

    override suspend fun toggleTaskCompletion(taskId: String, completed: Boolean): Resource<Boolean> = try {
        val updates = mapOf(
            "isCompleted" to completed,
            "completedAt" to if (completed) Date() else null
        )
        tasksCollection.document(taskId).update(updates).await()
        Resource.success(true)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to update task completion")
    }

    override suspend fun addComment(taskId: String, comment: Comment): Resource<Unit> = try {
        val task = tasksCollection.document(taskId).get().await()
            .toObject(Task::class.java) ?: throw Exception("Task not found")

        val updatedComments = task.comments + comment
        tasksCollection.document(taskId)
            .update("comments", updatedComments)
            .await()

        Resource.success(Unit)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to add comment")
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Resource<Unit> = try {
        tasksCollection.document(taskId)
            .update("status", status)
            .await()
        Resource.success(Unit)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to update task status")
    }

    override suspend fun assignTask(taskId: String, userId: String?): Resource<Unit> = try {
        tasksCollection.document(taskId)
            .update("assigned_to", userId)
            .await()
        Resource.success(Unit)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to assign task")
    }

    override suspend fun markTaskAsComplete(taskId: String): Resource<Unit> = try {
        val updates = mapOf(
            "isCompleted" to true,
            "completedAt" to Date(),
            "status" to TaskStatus.COMPLETED
        )
        tasksCollection.document(taskId)
            .update(updates)
            .await()
        Resource.success(Unit)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to mark task as complete")
    }

    override suspend fun syncTasks() {
        // Implementation needed
    }

    override suspend fun syncTask(taskId: String) {
        // Implementation needed
    }
}