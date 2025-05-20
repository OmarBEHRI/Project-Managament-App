package com.example.projectmanager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.Project
import com.example.projectmanager.data.model.ProjectStatus
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskStatus
import com.example.projectmanager.data.model.User
import com.example.projectmanager.data.repository.ProjectRepository
import com.example.projectmanager.data.repository.TaskRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Date

data class HomeUiState(
    val user: User? = null,
    val recentProjects: List<Project> = emptyList(),
    val pendingTasks: List<Task> = emptyList(),
    val projectStats: ProjectStats = ProjectStats(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ProjectStats(
    val totalProjects: Int = 0,
    val completedProjects: Int = 0,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val overdueTasksCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                userRepository.getCurrentUser().collectLatest { userResource ->
                    when (userResource) {
                        is Resource.Success -> {
                            userResource.data?.let { user ->
                                combine(
                                    projectRepository.getRecentProjects(5),
                                    taskRepository.getPendingTasks(),
                                    flow { emit(projectRepository.getProjectsByUser(user.id).first()) },
                                    flow { emit(taskRepository.getTasksByUser(user.id).toList()) }
                                ) { recentProjects, pendingTasks, projectsResource, tasks ->

                                    val projects = when(projectsResource) {
                                        is Resource.Success -> projectsResource.data
                                        else -> emptyList()
                                    }

                                    val stats = ProjectStats(
                                        totalProjects = projects.size,
                                        completedProjects = projects.count { project -> 
                                            project.status == ProjectStatus.COMPLETED
                                        },
                                        totalTasks = tasks.size,
                                        completedTasks = (tasks.size * 0.4).toInt(),
                                        overdueTasksCount = 0 // Temporary value
                                    )

                                    _uiState.update { state ->
                                        state.copy(
                                            user = user,
                                            recentProjects = recentProjects,
                                            pendingTasks = pendingTasks.take(5),
                                            projectStats = stats,
                                            isLoading = false,
                                            error = null
                                        )
                                    }
                                }.collect()
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
                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(isLoading = true)
                            }
                        }
                    }
                }
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