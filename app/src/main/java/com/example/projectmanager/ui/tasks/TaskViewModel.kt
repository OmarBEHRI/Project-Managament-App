package com.example.projectmanager.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.*
import com.example.projectmanager.data.repository.TaskRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val userRepository: UserRepository
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
                taskRepository.getTasksByProject(parentTaskId).collect { subtasks ->
                    _uiState.update {
                        it.copy(
                            subtasks = subtasks,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
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
                val subtask = Task(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    projectId = parentTask.projectId,
                    parentTaskId = parentTask.id,
                    createdBy = currentUserId ?: "",
                    status = TaskStatus.TODO,
                    priority = TaskPriority.MEDIUM,
                    dueDate = dueDate,
                    createdAt = Date()
                )
                
                val result = taskRepository.createTask(subtask)
                
                if (result is Resource.Success) {
                    loadSubtasks(parentTask.id)
                    _uiState.update { 
                        it.copy(
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
}