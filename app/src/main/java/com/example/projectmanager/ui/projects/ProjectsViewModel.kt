package com.example.projectmanager.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.Project
import com.example.projectmanager.data.model.ProjectMember
import com.example.projectmanager.data.model.ProjectRole
import com.example.projectmanager.data.repository.ProjectRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectsUiState(
    val projects: List<Project> = emptyList(),
    val filteredProjects: List<Project> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectsUiState())
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()
    
    // Store the original list of projects
    private var allProjects: List<Project> = emptyList()

    init {
        loadProjects()
    }

    fun refresh() {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                userRepository.getCurrentUser().collect { userResource ->
                    when (userResource) {
                        is Resource.Success -> {
                            val user = userResource.data
                            if (user != null) {
                                projectRepository.getProjectsByUser(user.id).collect { projects ->
                                    when (projects) {
                                        is Resource.Success -> {
                                            _uiState.update { state ->
                                                allProjects = projects.data
                                                state.copy(
                                                    projects = projects.data,
                                                    filteredProjects = filterProjects(projects.data, state.searchQuery),
                                                    isLoading = false,
                                                    error = null
                                                )
                                            }
                                        }

                                        is Resource.Error -> {
                                            _uiState.update { state ->
                                                state.copy(
                                                    isLoading = false,
                                                    error = projects.message
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
                            } else {
                                _uiState.update { state ->
                                    state.copy(
                                        isLoading = false,
                                        error = "User not logged in"
                                    )
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
                        error = e.message ?: "Failed to load projects"
                    )
                }
            }
        }
    }

    fun createProject(project: Project) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                userRepository.getCurrentUser().collectLatest { userResource ->
                    when (userResource) {
                        is Resource.Success -> {
                            userResource.data?.let { user ->
                                val newProject = project.copy(
                                    ownerId = user.id,
                                    members = listOf(
                                        ProjectMember(
                                            userId = user.id,
                                            role = ProjectRole.OWNER
                                        )
                                    )
                                )
                                
                                when (val result = projectRepository.createProject(newProject)) {
                                    is Resource.Success -> {
                                        loadProjects() // Refresh the projects list
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
                        error = e.message ?: "Failed to create project"
                    )
                }
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            try {
                when (val result = projectRepository.deleteProject(projectId)) {
                    is Resource.Success -> {
                        loadProjects() // Refresh the projects list
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
                    it.copy(error = e.message ?: "Failed to delete project")
                }
            }
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            try {
                when (val result = projectRepository.updateProject(project)) {
                    is Resource.Success -> {
                        loadProjects() // Refresh the projects list
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
                    it.copy(error = e.message ?: "Failed to update project")
                }
            }
        }
    }
    
    fun searchProjects(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredProjects = filterProjects(allProjects, query)
            )
        }
    }
    
    private fun filterProjects(projects: List<Project>, query: String): List<Project> {
        if (query.isBlank()) return projects
        
        val lowercaseQuery = query.lowercase()
        return projects.filter { project ->
            project.name.lowercase().contains(lowercaseQuery) ||
            project.description.lowercase().contains(lowercaseQuery) ||
            project.tags.any { it.lowercase().contains(lowercaseQuery) }
        }
    }

    fun addMemberToProject(projectId: String, userId: String) {
        viewModelScope.launch {
            try {
                projectRepository.addMemberToProject(projectId, userId)
                loadProjects()
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "Failed to add member")
                }
            }
        }
    }

    fun removeMemberFromProject(projectId: String, userId: String) {
        viewModelScope.launch {
            try {
                projectRepository.removeMemberFromProject(projectId, userId)
                loadProjects()
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "Failed to remove member")
                }
            }
        }
    }
} 