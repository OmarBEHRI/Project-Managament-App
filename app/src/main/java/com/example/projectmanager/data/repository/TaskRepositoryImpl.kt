package com.example.projectmanager.data.repository

import com.example.projectmanager.data.local.dao.TaskDao
import com.example.projectmanager.data.local.entity.TaskEntity
import com.example.projectmanager.data.model.Comment
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskPriority
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
            
            // Instead of using toObject which is causing issues with PropertyName annotations,
            // manually map the document to a Task object
            if (document.exists()) {
                try {
                    val task = Task(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        description = document.getString("description") ?: "",
                        richDescription = null, // Will implement if needed
                        projectId = document.getString("project_id") ?: "",
                        parentTaskId = document.getString("parent_task_id"),
                        subtasks = emptyList(), // Will be populated separately
                        assignedTo = (document.get("assigned_to") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        createdBy = document.getString("created_by") ?: "",
                        status = document.getString("status")?.let { TaskStatus.valueOf(it) } ?: TaskStatus.TODO,
                        priority = document.getString("priority")?.let { TaskPriority.valueOf(it) } ?: TaskPriority.MEDIUM,
                        startDate = document.getDate("start_date"),
                        dueDate = document.getDate("due_date"),
                        createdAt = document.getDate("createdAt"),
                        updatedAt = document.getDate("updatedAt"),
                        tags = (document.get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        isCompleted = document.getBoolean("completed") ?: false,
                        completedAt = document.getDate("completed_at"),
                        isOverdue = document.getBoolean("overdue") ?: false,
                        estimatedHours = document.getDouble("estimated_hours")?.toFloat(),
                        actualHours = document.getDouble("actual_hours")?.toFloat(),
                        milestoneId = document.getString("milestone_id"),
                        order = document.getLong("order")?.toInt() ?: 0,
                        watchers = (document.get("watchers") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )
                    
                    // Log the task details for debugging
                    println("Retrieved task ${task.id} with title: ${task.title}")
                    println("Due date: ${task.dueDate}, Status: ${task.status}, Priority: ${task.priority}")
                    
                    emit(Resource.success(task))
                } catch (e: Exception) {
                    println("Error mapping task document ${document.id}: ${e.message}")
                    e.printStackTrace()
                    emit(Resource.error("Error parsing task data: ${e.message}"))
                }
            } else {
                emit(Resource.error("Task not found"))
            }
        } catch (e: Exception) {
            println("Error fetching task: ${e.message}")
            e.printStackTrace()
            emit(Resource.error(e.message ?: "Failed to get task"))
        }
    }

    override fun getTasksByProject(projectId: String): Flow<List<Task>> = flow {
        try {
            println("Fetching tasks for project ID: $projectId")
            
            // Get tasks from Firestore
            val snapshot = tasksCollection
                .whereEqualTo("project_id", projectId)
                .get()
                .await()
            
            // Convert to Task objects
            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    // Manually map Firestore document to Task object
                    Task(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        richDescription = null, // Will be implemented later if needed
                        projectId = doc.getString("project_id") ?: "",
                        parentTaskId = doc.getString("parent_task_id"),
                        subtasks = emptyList(), // Will be populated separately
                        assignedTo = (doc.get("assigned_to") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        createdBy = doc.getString("created_by") ?: "",
                        status = doc.getString("status")?.let { TaskStatus.valueOf(it) } ?: TaskStatus.TODO,
                        priority = doc.getString("priority")?.let { TaskPriority.valueOf(it) } ?: TaskPriority.MEDIUM,
                        startDate = doc.getDate("start_date"),
                        dueDate = doc.getDate("due_date"),
                        createdAt = doc.getDate("createdAt"),
                        updatedAt = doc.getDate("updatedAt"),
                        tags = (doc.get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        isCompleted = doc.getBoolean("completed") ?: false,
                        completedAt = doc.getDate("completed_at"),
                        isOverdue = doc.getBoolean("overdue") ?: false,
                        estimatedHours = doc.getDouble("estimated_hours")?.toFloat(),
                        actualHours = doc.getDouble("actual_hours")?.toFloat(),
                        milestoneId = doc.getString("milestone_id")
                    )
                } catch (e: Exception) {
                    println("Error mapping document ${doc.id}: ${e.message}")
                    null
                }
            }
            
            println("Found ${tasks.size} tasks for project $projectId")
            tasks.forEach { task ->
                println("Task: ${task.id}, ${task.title}, assigned to: ${task.assignedTo}")
            }
            
            // Sort tasks by due date
            val sortedTasks = tasks.sortedBy { it.dueDate }
            emit(sortedTasks)
        } catch (e: Exception) {
            println("Error fetching tasks for project $projectId: ${e.message}")
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override fun getTasksByUser(userId: String): Flow<List<Task>> = flow {
        try {
            // Debug log to help troubleshoot
            println("Fetching tasks for user ID: $userId")
            
            // Use the field name as it appears in Firestore
            // The PropertyName annotation in Task.kt indicates the field is 'assigned_to' in Firestore
            val snapshot = tasksCollection
                .whereArrayContains("assigned_to", userId) // Using the correct field name in Firestore
                .get()
                .await()
            
            // Convert to Task objects manually to avoid DocumentId issues
            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    // Manually map Firestore document to Task object
                    Task(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        projectId = doc.getString("project_id") ?: "",
                        assignedTo = (doc.get("assigned_to") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        createdBy = doc.getString("created_by") ?: "",
                        status = doc.getString("status")?.let { TaskStatus.valueOf(it) } ?: TaskStatus.TODO,
                        priority = doc.getString("priority")?.let { TaskPriority.valueOf(it) } ?: TaskPriority.MEDIUM,
                        dueDate = doc.getDate("due_date"),
                        isCompleted = doc.getBoolean("completed") ?: false,
                        estimatedHours = doc.getDouble("estimated_hours")?.toFloat()
                    )
                } catch (e: Exception) {
                    println("Error mapping document ${doc.id}: ${e.message}")
                    null
                }
            }
            
            println("Found ${tasks.size} tasks for user $userId")
            tasks.forEach { task ->
                println("Task: ${task.id}, ${task.title}, assigned to: ${task.assignedTo}")
            }
            
            // Sort in memory instead of in the query
            val sortedTasks = tasks.sortedBy { it.dueDate }
            emit(sortedTasks)
        } catch (e: Exception) {
            println("Error fetching tasks for user $userId: ${e.message}")
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override fun getPendingTasks(): Flow<List<Task>> = flow {
        try {
            println("Fetching all pending tasks")
            
            val snapshot = tasksCollection
                .whereEqualTo("completed", false)
                .get()
                .await()
            
            // Convert to Task objects manually to avoid DocumentId issues
            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    // Manually map Firestore document to Task object
                    Task(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        projectId = doc.getString("project_id") ?: "",
                        assignedTo = (doc.get("assigned_to") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        createdBy = doc.getString("created_by") ?: "",
                        status = doc.getString("status")?.let { TaskStatus.valueOf(it) } ?: TaskStatus.TODO,
                        priority = doc.getString("priority")?.let { TaskPriority.valueOf(it) } ?: TaskPriority.MEDIUM,
                        dueDate = doc.getDate("due_date"),
                        isCompleted = doc.getBoolean("completed") ?: false,
                        estimatedHours = doc.getDouble("estimated_hours")?.toFloat()
                    )
                } catch (e: Exception) {
                    println("Error mapping document ${doc.id}: ${e.message}")
                    null
                }
            }
            
            println("Received ${tasks.size} pending tasks")
            tasks.forEach { task ->
                println("Pending task: ${task.id}, ${task.title}, assigned to: ${task.assignedTo}")
            }
            
            val sortedTasks = tasks.sortedBy { it.dueDate }
            emit(sortedTasks)
        } catch (e: Exception) {
            println("Error fetching pending tasks: ${e.message}")
            e.printStackTrace()
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
        
        // Convert the task to a map with the correct field names for Firestore
        val taskMap = mapOf(
            "id" to newTask.id,
            "title" to newTask.title,
            "description" to newTask.description,
            "project_id" to newTask.projectId,
            "assigned_to" to newTask.assignedTo,
            "created_by" to newTask.createdBy,
            "status" to newTask.status,
            "priority" to newTask.priority,
            "due_date" to newTask.dueDate,
            "createdAt" to newTask.createdAt,
            "updatedAt" to newTask.updatedAt,
            "completed" to newTask.isCompleted,  // Using 'completed' instead of 'isCompleted'
            "estimated_hours" to newTask.estimatedHours,
            "tags" to newTask.tags
        )
        
        // Log the task being created
        println("Creating task with ID: ${newTask.id}, assigned to: ${newTask.assignedTo}")
        println("Task details: title=${newTask.title}, project=${newTask.projectId}, status=${newTask.status}")
        
        documentRef.set(taskMap).await()
        Resource.success(newTask)
    } catch (e: Exception) {
        println("Error creating task: ${e.message}")
        e.printStackTrace()
        Resource.error(e.message ?: "Failed to create task")
    }

    override suspend fun updateTask(task: Task): Resource<Task> = try {
        // Convert the task to a map with the correct field names for Firestore
        val taskMap = mapOf(
            "id" to task.id,
            "title" to task.title,
            "description" to task.description,
            "project_id" to task.projectId,
            "assigned_to" to task.assignedTo,
            "created_by" to task.createdBy,
            "status" to task.status,
            "priority" to task.priority,
            "due_date" to task.dueDate,
            "createdAt" to task.createdAt,
            "updatedAt" to task.updatedAt,
            "completed" to task.isCompleted,  // Using 'completed' instead of 'isCompleted'
            "estimated_hours" to task.estimatedHours,
            "tags" to task.tags
        )
        
        println("Updating task with ID: ${task.id}, assigned to: ${task.assignedTo}")
        println("Task details: title=${task.title}, project=${task.projectId}, status=${task.status}")
        
        tasksCollection.document(task.id).set(taskMap).await()
        Resource.success(task)
    } catch (e: Exception) {
        println("Error updating task: ${e.message}")
        e.printStackTrace()
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
            "completed" to completed,  // Using the correct field name in Firestore
            "completed_at" to if (completed) Date() else null
        )
        println("Toggling task $taskId completion to $completed")
        tasksCollection.document(taskId).update(updates).await()
        Resource.success(true)
    } catch (e: Exception) {
        println("Error toggling task completion: ${e.message}")
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
            "completed" to true,  // Using 'completed' instead of 'isCompleted'
            "completed_at" to Date(),  // Using 'completed_at' instead of 'completedAt'
            "status" to TaskStatus.COMPLETED
        )
        println("Marking task $taskId as complete")
        tasksCollection.document(taskId)
            .update(updates)
            .await()
        Resource.success(Unit)
    } catch (e: Exception) {
        println("Error marking task as complete: ${e.message}")
        Resource.error(e.message ?: "Failed to mark task as complete")
    }

    override suspend fun syncTasks() {
        // Implementation needed
    }

    override suspend fun syncTask(taskId: String) {
        // Implementation needed
    }
}