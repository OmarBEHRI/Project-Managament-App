package com.example.projectmanager.data.repository

import com.example.projectmanager.data.local.dao.TaskDao
import com.example.projectmanager.data.local.entity.TaskEntity
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.Comment
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

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTaskById(taskId: String): Flow<Resource<Task>>
    fun getTasksByProject(projectId: String): Flow<List<Task>>
    fun getTasksByUser(userId: String): Flow<List<Task>>
    fun getPendingTasks(): Flow<List<Task>>
    fun getOverdueTasks(): Flow<List<Task>>
    fun getCompletedTasks(limit: Int = 50): Flow<List<Task>>
    suspend fun createTask(task: Task): Resource<Task>
    suspend fun updateTask(task: Task): Resource<Task>
    suspend fun deleteTask(taskId: String): Resource<Unit>
    suspend fun toggleTaskCompletion(taskId: String, completed: Boolean): Resource<Boolean>
    suspend fun addComment(taskId: String, comment: Comment): Resource<Unit>
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Resource<Unit>
    suspend fun assignTask(taskId: String, userId: String?): Resource<Unit>
    suspend fun markTaskAsComplete(taskId: String): Resource<Unit>
    suspend fun syncTasks()
    suspend fun syncTask(taskId: String)
}

