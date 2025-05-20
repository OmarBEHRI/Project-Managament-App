package com.example.projectmanager.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskStatus
import com.example.projectmanager.data.model.Priority
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
    val priority: Priority? = null
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
                                taskRepository.getTasksByUser(user.id).collect { tasks ->
                                    val filteredTasks = filterTasks(tasks, _uiState.value.filter)
                                    
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
                userRepository.getCurrentUser().collectLatest { userResource ->
                    when (userResource) {
                        is Resource.Success -> {
                            userResource.data?.let { user ->
                                val newTask = task.copy(
                                    createdBy = user.id,
                                    assignedTo = listOf(user.id)
                                )
                                
                                when (val result = taskRepository.createTask(newTask)) {
                                    is Resource.Success -> {
                                        loadTasks() // Refresh the tasks list
                                    }
                                    is Resource.Error -> {
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
                when (val result = taskRepository.deleteTask(taskId)) {
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
}