package com.example.projectmanager.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.*
import com.example.projectmanager.data.repository.TaskRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class TaskUiState(
    val task: Task? = null,
    val subtasks: List<Task> = emptyList(),
    val assignedUsers: List<User> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val attachments: List<FileAttachment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private var currentUserId: String? = null
    private var currentUserName: String? = null

    init {
        // Load current user ID
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { resource ->
                if (resource is Resource.Success) {
                    currentUserId = resource.data?.id
                    currentUserName = resource.data?.displayName
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                taskRepository.getTaskById(taskId).collect { resource ->
                    if (resource is Resource.Success && resource.data != null) {
                        val task = resource.data
                        _uiState.update { it.copy(task = task) }
                        loadSubtasks(task.id)
                        if (task.assignedTo.isNotEmpty()) {
                            loadAssignedUsers(task.assignedTo)
                        }
                        _uiState.update { it.copy(isLoading = false) }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Task not found"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load task"
                    )
                }
            }
        }
    }

    private fun loadSubtasks(parentTaskId: String) {
        viewModelScope.launch {
            try {
                // Query tasks where parent_task_id equals the current task's ID
                // This is more accurate than using project ID for subtasks
                val subtasksFlow = flow {
                    val snapshot = firestore.collection("tasks")
                        .whereEqualTo("parent_task_id", parentTaskId)
                        .get()
                        .await()
                    
                    val subtasks = snapshot.documents.mapNotNull { doc ->
                        try {
                            Task(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                projectId = doc.getString("project_id") ?: "",
                                parentTaskId = doc.getString("parent_task_id"),
                                assignedTo = (doc.get("assigned_to") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                createdBy = doc.getString("created_by") ?: "",
                                status = doc.getString("status")?.let { TaskStatus.valueOf(it) } ?: TaskStatus.TODO,
                                priority = doc.getString("priority")?.let { TaskPriority.valueOf(it) } ?: TaskPriority.MEDIUM,
                                dueDate = doc.getDate("due_date"),
                                isCompleted = doc.getBoolean("completed") ?: false
                            )
                        } catch (e: Exception) {
                            println("Error mapping subtask document ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    emit(subtasks)
                }
                
                subtasksFlow.collect { subtasks ->
                    println("Found ${subtasks.size} subtasks for task $parentTaskId")
                    _uiState.update {
                        it.copy(
                            subtasks = subtasks,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                println("Error loading subtasks: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load subtasks"
                    )
                }
            }
        }
    }

    private fun loadAssignedUsers(userIds: List<String>) {
        viewModelScope.launch {
            try {
                val users = mutableListOf<User>()
                for (userId in userIds) {
                    userRepository.getUserById(userId).collect { resource ->
                        if (resource is Resource.Success && resource.data != null) {
                            users.add(resource.data)
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        assignedUsers = users,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load assigned users"
                    )
                }
            }
        }
    }

    fun updateTaskStatus(newStatus: TaskStatus) {
        val task = _uiState.value.task ?: return
        updateTask(task.copy(status = newStatus))
    }

    fun updateTaskPriority(newPriority: TaskPriority) {
        val task = _uiState.value.task ?: return
        updateTask(task.copy(priority = newPriority))
    }

    fun toggleTaskCompletion() {
        val task = _uiState.value.task ?: return
        val isCompleted = !task.isCompleted
        val completedAt = if (isCompleted) Date() else null
        val status = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.TODO
        
        updateTask(task.copy(
            isCompleted = isCompleted,
            completedAt = completedAt,
            status = status
        ))
    }

    fun updateTaskDueDate(newDueDate: Date?) {
        val task = _uiState.value.task ?: return
        updateTask(task.copy(dueDate = newDueDate))
    }

    fun updateTaskTitle(newTitle: String) {
        val task = _uiState.value.task ?: return
        if (newTitle.isBlank()) return
        updateTask(task.copy(title = newTitle))
    }

    fun updateTaskDescription(newDescription: String) {
        val task = _uiState.value.task ?: return
        updateTask(task.copy(description = newDescription))
    }

    fun addSubtask(title: String, description: String = "", dueDate: Date? = null) {
        val parentTask = _uiState.value.task ?: return
        if (title.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            
            try {
                // Create a document reference first to get the ID
                val documentRef = firestore.collection("tasks").document()
                
                // Create the subtask with proper fields
                val subtaskMap = mapOf(
                    "title" to title,
                    "description" to description,
                    "project_id" to parentTask.projectId,
                    "parent_task_id" to parentTask.id,  // This is crucial for subtask relationship
                    "created_by" to (currentUserId ?: ""),
                    "status" to TaskStatus.TODO.name,
                    "priority" to TaskPriority.MEDIUM.name,
                    "due_date" to dueDate,
                    "createdAt" to Date(),
                    "updatedAt" to Date(),
                    "completed" to false,
                    "assigned_to" to listOf<String>() // Empty list initially
                )
                
                // Log the subtask creation
                println("Creating subtask with parent task ID: ${parentTask.id}")
                println("Subtask details: title=$title, project=${parentTask.projectId}")
                
                // Save to Firestore
                documentRef.set(subtaskMap).await()
                
                // Refresh the subtasks list
                loadSubtasks(parentTask.id)
                
                _uiState.update { 
                    it.copy(
                        isUpdating = false,
                        updateSuccess = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                println("Error creating subtask: ${e.message}")
                e.printStackTrace()
                _uiState.update { 
                    it.copy(
                        isUpdating = false,
                        error = e.message ?: "Failed to add subtask"
                    )
                }
            }
        }
    }

    fun addComment(content: String) {
        val task = _uiState.value.task ?: return
        if (content.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            
            try {
                val comment = Comment(
                    id = UUID.randomUUID().toString(),
                    taskId = task.id,
                    userId = currentUserId ?: "",
                    authorName = currentUserName ?: "Unknown User",
                    content = content,
                    createdAt = Date()
                )
                
                val result = taskRepository.addComment(task.id, comment)
                
                if (result is Resource.Success) {
                    val updatedComments = _uiState.value.comments + comment
                    _uiState.update { 
                        it.copy(
                            comments = updatedComments,
                            isUpdating = false,
                            updateSuccess = true,
                            error = null
                        )
                    }
                } else if (result is Resource.Error) {
                    _uiState.update { 
                        it.copy(
                            isUpdating = false,
                            error = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isUpdating = false,
                        error = e.message ?: "Failed to add comment"
                    )
                }
            }
        }
    }

    private fun updateTask(updatedTask: Task) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            
            try {
                val result = taskRepository.updateTask(updatedTask)
                
                if (result is Resource.Success) {
                    _uiState.update { 
                        it.copy(
                            task = updatedTask,
                            isUpdating = false,
                            updateSuccess = true,
                            error = null
                        )
                    }
                } else if (result is Resource.Error) {
                    _uiState.update { 
                        it.copy(
                            isUpdating = false,
                            error = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isUpdating = false,
                        error = e.message ?: "Failed to update task"
                    )
                }
            }
        }
    }

    fun resetUpdateState() {
        _uiState.update { it.copy(updateSuccess = false, error = null) }
    }
    
    fun toggleSubtaskCompletion(subtaskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            
            try {
                // Get the current subtask
                val subtask = _uiState.value.subtasks.find { it.id == subtaskId } ?: return@launch
                
                // Toggle completion status
                val isCompleted = !subtask.isCompleted
                val completedAt = if (isCompleted) Date() else null
                val status = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.TODO
                
                // Update in Firestore
                val updates = mapOf(
                    "completed" to isCompleted,
                    "completed_at" to completedAt,
                    "status" to status.name
                )
                
                println("Toggling subtask $subtaskId completion to $isCompleted")
                firestore.collection("tasks").document(subtaskId).update(updates).await()
                
                // Refresh subtasks
                val parentTaskId = _uiState.value.task?.id ?: return@launch
                loadSubtasks(parentTaskId)
                
                _uiState.update { 
                    it.copy(
                        isUpdating = false,
                        updateSuccess = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                println("Error toggling subtask completion: ${e.message}")
                e.printStackTrace()
                _uiState.update { 
                    it.copy(
                        isUpdating = false,
                        error = e.message ?: "Failed to update subtask"
                    )
                }
            }
        }
    }
}