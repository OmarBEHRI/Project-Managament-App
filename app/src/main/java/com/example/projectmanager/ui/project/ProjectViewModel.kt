package com.example.projectmanager.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.*
import com.example.projectmanager.data.repository.ProjectRepository
import com.example.projectmanager.data.repository.TaskRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class TaskStats(
    val total: Int = 0,
    val completed: Int = 0,
    val active: Int = 0,  // All tasks excluding canceled ones
    val completedPercentage: Float = 0f
)

data class ProjectUiState(
    val project: Project? = null,
    val tasks: List<Task> = emptyList(),
    val members: List<User> = emptyList(),
    val userSuggestions: List<User> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val attachments: List<FileAttachment> = emptyList(),
    val taskStats: TaskStats = TaskStats(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
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

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                projectRepository.getProjectById(projectId).collect { project ->
                    if (project != null) {
                        _uiState.update { it.copy(project = project) }
                        loadProjectTasks(project.id)
                        loadProjectMembers(project)
                        loadProjectComments(project.id)
                        loadProjectAttachments(project.id)
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Project not found"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load project"
                    )
                }
            }
        }
    }

    private fun loadProjectTasks(projectId: String) {
        viewModelScope.launch {
            try {
                println("ProjectViewModel: Loading tasks for project $projectId")
                taskRepository.getTasksByProject(projectId).collect { tasks ->
                    println("ProjectViewModel: Received ${tasks.size} tasks for project $projectId")
                    tasks.forEach { task ->
                        println("ProjectViewModel: Task ${task.id}: ${task.title}, assigned to: ${task.assignedTo}")
                    }
                    
                    // Calculate task statistics excluding canceled tasks
                    val taskStats = calculateTaskStats(tasks)
                    
                    _uiState.update {
                        it.copy(
                            tasks = tasks,
                            taskStats = taskStats,
                            isLoading = false,
                            error = null
                        )
                    }
                    
                    println("ProjectViewModel: Updated UI state with ${tasks.size} tasks")
                }
            } catch (e: Exception) {
                println("ProjectViewModel: Error loading tasks: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load project tasks"
                    )
                }
            }
        }
    }

    private fun loadProjectMembers(project: Project) {
        viewModelScope.launch {
            try {
                // Get users for each project member
                val memberIds = project.members.map { it.userId }
                val members = mutableListOf<User>()
                
                for (memberId in memberIds) {
                    userRepository.getUserById(memberId).collect { resource ->
                        if (resource is Resource.Success<User> && resource.data != null) {
                            members.add(resource.data)
                        }
                    }
                }
                
                _uiState.update {
                    it.copy(
                        members = members,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load project members"
                    )
                }
            }
        }
    }
    
    private fun loadProjectComments(projectId: String) {
        viewModelScope.launch {
            try {
                projectRepository.getProjectComments(projectId).collect { comments ->
                    _uiState.update {
                        it.copy(comments = comments)
                    }
                }
            } catch (e: Exception) {
                // Just log the error, don't update UI state as this is a secondary feature
                e.printStackTrace()
            }
        }
    }
    
    private fun loadProjectAttachments(projectId: String) {
        viewModelScope.launch {
            try {
                projectRepository.getProjectAttachments(projectId).collect { attachments ->
                    _uiState.update {
                        it.copy(attachments = attachments)
                    }
                }
            } catch (e: Exception) {
                // Just log the error, don't update UI state as this is a secondary feature
                e.printStackTrace()
            }
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = projectRepository.updateProject(project)
                when (result) {
                    is Resource.Success -> {
                        loadProject(project.id)
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
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update project"
                    )
                }
            }
        }
    }
    
    fun getCurrentUserId(): String {
        return currentUserId ?: ""
    }
    
    fun createTask(task: Task) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = taskRepository.createTask(task)
                when (result) {
                    is Resource.Success -> {
                        // Refresh tasks list
                        loadProject(task.projectId)
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
    
    // Search for users to add to the project
    fun searchUsers(query: String) {
        viewModelScope.launch {
            if (query.length < 2) {
                _uiState.update { it.copy(userSuggestions = emptyList(), searchQuery = query) }
                return@launch
            }
            
            _uiState.update { it.copy(searchQuery = query) }
            
            try {
                userRepository.searchUsers(query).collect { resource ->
                    when (resource) {
                        is Resource.Success<List<User>> -> {
                            val users = resource.data ?: emptyList()
                            // Filter out users who are already members
                            val currentMemberIds = _uiState.value.project?.members?.map { it.userId } ?: emptyList()
                            val filteredUsers = users.filter { user -> !currentMemberIds.contains(user.id) }
                            
                            _uiState.update { it.copy(userSuggestions = filteredUsers) }
                        }
                        else -> {
                            _uiState.update { it.copy(userSuggestions = emptyList()) }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userSuggestions = emptyList()) }
            }
        }
    }
    
    // Add a member to the project
    fun addMemberToProject(user: User, role: ProjectRole = ProjectRole.MEMBER) {
        viewModelScope.launch {
            val project = _uiState.value.project ?: return@launch
            
            // Create a new ProjectMember
            val newMember = ProjectMember(
                userId = user.id,
                role = role,
                joinedAt = Date()
            )
            
            // Check if user is already a member
            if (project.members.any { it.userId == user.id }) {
                _uiState.update { it.copy(error = "User is already a member of this project") }
                return@launch
            }
            
            // Add the member to the project
            val updatedProject = project.copy(
                members = project.members + newMember
            )
            
            updateProject(updatedProject)
            
            // Clear suggestions after adding
            _uiState.update { it.copy(userSuggestions = emptyList(), searchQuery = "") }
        }
    }
    
    // Remove a member from the project
    fun removeMemberFromProject(userId: String) {
        viewModelScope.launch {
            val project = _uiState.value.project ?: return@launch
            
            // Check if current user has permission to remove members
            if (!isCurrentUserManager()) {
                _uiState.update { it.copy(error = "You don't have permission to remove members") }
                return@launch
            }
            
            // Check if trying to remove the owner
            val memberToRemove = project.members.find { it.userId == userId }
            if (memberToRemove?.role == ProjectRole.OWNER) {
                _uiState.update { it.copy(error = "Cannot remove the project owner") }
                return@launch
            }
            
            // Remove the member
            val updatedProject = project.copy(
                members = project.members.filter { it.userId != userId }
            )
            
            updateProject(updatedProject)
        }
    }
    
    // Add a comment to the project
    fun addComment(content: String) {
        viewModelScope.launch {
            val project = _uiState.value.project ?: return@launch
            val userId = currentUserId ?: return@launch
            val userName = currentUserName ?: "Unknown User"
            
            val comment = Comment(
                id = UUID.randomUUID().toString(),
                projectId = project.id,
                userId = userId,
                authorName = userName,
                content = content,
                createdAt = Date()
            )
            
            try {
                val result = projectRepository.addComment(comment)
                when (result) {
                    is Resource.Success<Comment> -> {
                        // Refresh comments
                        loadProjectComments(project.id)
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to add comment") }
            }
        }
    }
    
    // Upload a file attachment to the project
    fun uploadAttachment(fileName: String, fileSize: Long, mimeType: String, fileUri: String) {
        viewModelScope.launch {
            val project = _uiState.value.project ?: return@launch
            val userId = currentUserId ?: return@launch
            
            try {
                val result = projectRepository.uploadAttachment(
                    projectId = project.id,
                    fileName = fileName,
                    fileSize = fileSize,
                    mimeType = mimeType,
                    fileUri = fileUri,
                    uploadedBy = userId
                )
                
                when (result) {
                    is Resource.Success<FileAttachment> -> {
                        // Refresh attachments
                        loadProjectAttachments(project.id)
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to upload attachment") }
            }
        }
    }
    
    // Check if current user is a manager of the project
    fun isCurrentUserManager(): Boolean {
        val project = _uiState.value.project ?: return false
        val userId = currentUserId ?: return false
        
        return project.ownerId == userId || 
               project.members.any { 
                   it.userId == userId && 
                   (it.role == ProjectRole.MANAGER || it.role == ProjectRole.OWNER || it.role == ProjectRole.ADMIN)
               }
    }
    
    // Clear search query and suggestions
    fun clearSearch() {
        _uiState.update { it.copy(userSuggestions = emptyList(), searchQuery = "") }
    }
    
    /**
     * Calculate task statistics excluding canceled tasks
     */
    private fun calculateTaskStats(tasks: List<Task>): TaskStats {
        // Filter out canceled tasks
        val activeTasks = tasks.filter { it.status != TaskStatus.CANCELLED }
        
        // Count tasks as completed if either isCompleted flag is true OR status is COMPLETED
        val completedTasks = activeTasks.filter { it.isCompleted || it.status == TaskStatus.COMPLETED }
        
        val total = tasks.size
        val active = activeTasks.size
        val completed = completedTasks.size
        
        // Calculate completion percentage based on active tasks (excluding canceled)
        val completedPercentage = if (active > 0) {
            (completed.toFloat() / active.toFloat()) * 100f
        } else {
            0f
        }
        
        return TaskStats(
            total = total,
            active = active,
            completed = completed,
            completedPercentage = completedPercentage
        )
    }
}