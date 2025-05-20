package com.example.projectmanager.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.Project
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.repository.ProjectRepository
import com.example.projectmanager.data.repository.TaskRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val userName: String = "",
    val totalProjects: Int = 0,
    val completedProjects: Int = 0,
    val pendingTasks: Int = 0,
    val recentProjects: List<Project> = emptyList(),
    val upcomingTasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load user data
                userRepository.getCurrentUser().collectLatest { userResource ->
                    when (userResource) {
                        is Resource.Success -> {
                            userResource.data?.let { user ->
                                _uiState.update { it.copy(userName = user.displayName ?: "User") }
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(error = userResource.message) }
                        }
                        else -> {}
                    }
                }

                // Load project statistics
                combine(
                    projectRepository.getAll(),
                    projectRepository.getRecentProjects(limit = 5),
                    taskRepository.getPendingTasks()
                ) { allProjectsResource, recentProjects, pendingTasks ->
                    val allProjects = if (allProjectsResource is Resource.Success) {
                        allProjectsResource.data
                    } else {
                        emptyList()
                    }
                    val totalProjects = allProjects.size
                    val completedProjects = allProjects.count { it.isCompleted }
                    
                    _uiState.update { state ->
                        state.copy(
                            totalProjects = totalProjects,
                            completedProjects = completedProjects,
                            pendingTasks = pendingTasks.size,
                            recentProjects = recentProjects,
                            upcomingTasks = pendingTasks.take(5),
                            isLoading = false
                        )
                    }
                }.collect()

            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load dashboard data"
                    )
                }
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }
}