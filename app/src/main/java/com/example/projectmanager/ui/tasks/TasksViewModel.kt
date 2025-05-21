package com.example.projectmanager.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskStatus
import com.example.projectmanager.data.model.TaskPriority
import com.example.projectmanager.data.repository.TaskRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class TaskFilter(
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun refresh() {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                userRepository.getCurrentUser().collect { userResource ->
                    when (userResource) {
                        is Resource.Success -> {
                            userResource.data?.let { user ->
                                println("Loading tasks for user: ${user.id}")
                                taskRepository.getTasksByUser(user.id).collect { tasks ->
                                    println("Received ${tasks.size} tasks for user ${user.id}")
                                    
                                    // Log each task for debugging
                                    tasks.forEachIndexed { index, task ->
                                        println("Task $index: id=${task.id}, title=${task.title}, assignedTo=${task.assignedTo}")
                                    }
                                    
                                    val filteredTasks = filterTasks(tasks, _uiState.value.filter)
                                    println("After filtering: ${filteredTasks.size} tasks")
                                    
                                    _uiState.update { state ->
                                        state.copy(
                                            tasks = filteredTasks,
                                            isLoading = false,
                                            error = null
                                        )
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                            println("Error loading user: ${userResource.message}")
                            _uiState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    error = userResource.message
                                )
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update { state ->
                                state.copy(isLoading = true)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Exception loading tasks: ${e.message}")
                e.printStackTrace()
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load tasks"
                    )
                }
            }
        }
    }

    fun createTask(task: Task) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                println("Creating a new task: ${task.title}")
                userRepository.getCurrentUser().collectLatest { userResource ->
                    when (userResource) {
                        is Resource.Success -> {
                            userResource.data?.let { user ->
                                println("Current user: ${user.id}, ${user.displayName}")
                                
                                // Always ensure the task is assigned to the current user
                                val assignees = if (task.assignedTo.isEmpty()) {
                                    listOf(user.id)
                                } else {
                                    // Keep existing assignees and add current user if not already included
                                    if (task.assignedTo.contains(user.id)) {
                                        task.assignedTo
                                    } else {
                                        task.assignedTo + user.id
                                    }
                                }
                                
                                println("Task will be assigned to: $assignees")
                                
                                val newTask = task.copy(
                                    createdBy = user.id,
                                    assignedTo = assignees
                                )
                                
                                println("Submitting task with assignedTo: ${newTask.assignedTo}")
                                
                                when (val result = taskRepository.createTask(newTask)) {
                                    is Resource.Success -> {
                                        println("Task created successfully: ${result.data.id}")
                                        loadTasks() // Refresh the tasks list
                                    }
                                    is Resource.Error -> {
                                        println("Error creating task: ${result.message}")
                                        _uiState.update { 
                                            it.copy(
                                                isLoading = false,
                                                error = result.message
                                            )
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                        is Resource.Error -> {
                            println("Error getting current user: ${userResource.message}")
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = userResource.message
                                )
                            }
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                println("Exception creating task: ${e.message}")
                e.printStackTrace()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to create task"
                    )
                }
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(taskId)
                loadTasks() // Refresh the task list
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to delete task")
                }
            }
        }
    }

    fun updateFilter(filter: TaskFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(filter = filter) }
            loadTasks()
        }
    }

    private fun filterTasks(tasks: List<Task>, filter: TaskFilter): List<Task> {
        return tasks.filter { task ->
            (filter.status == null || task.status == filter.status) &&
            (filter.priority == null || task.priority == filter.priority)
        }
    }

    fun markTaskAsComplete(taskId: String) {
        viewModelScope.launch {
            try {
                when (val result = taskRepository.markTaskAsComplete(taskId)) {
                    is Resource.Success -> {
                        loadTasks() // Refresh the tasks list
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(error = result.message)
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to mark task as complete")
                }
            }
        }
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus) {
        viewModelScope.launch {
            try {
                when (val result = taskRepository.updateTaskStatus(taskId, status)) {
                    is Resource.Success -> {
                        loadTasks() // Refresh the tasks list
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(error = result.message)
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to update task status")
                }
            }
        }
    }
    
    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                // Find the task in the current list
                val task = _uiState.value.tasks.find { it.id == taskId } ?: return@launch
                
                // Create an updated task with the toggled completion status
                val updatedTask = task.copy(
                    isCompleted = isCompleted,
                    status = if (isCompleted) TaskStatus.COMPLETED else task.status
                )
                
                // Update the task in the repository
                taskRepository.updateTask(updatedTask)
                
                // Update the local task list
                val updatedTasks = _uiState.value.tasks.map { 
                    if (it.id == taskId) updatedTask else it 
                }
                _uiState.update { it.copy(tasks = updatedTasks) }
                
                // Reload tasks to ensure consistency with the backend
                loadTasks()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update task: ${e.message}") }
            }
        }
    }
}