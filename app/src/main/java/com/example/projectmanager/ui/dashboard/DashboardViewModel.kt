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
    val projectsByStatus: Map<String, List<Project>> = emptyMap(),
    val projectStatusCounts: Map<String, Int> = emptyMap(),
    val taskCompletionRate: Float = 0f,
    val completedTasks: List<Task> = emptyList(),
    val totalTasks: Int = 0,
    val timeToCompletionData: List<Pair<String, Float>> = emptyList(),
    val teamProductivityData: List<Pair<String, Float>> = emptyList(),
    val teamMembers: List<com.example.projectmanager.data.model.User> = emptyList(),
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

                // Load project statistics and analytics data
                combine(
                    projectRepository.getAll(),
                    projectRepository.getRecentProjects(limit = 5),
                    taskRepository.getPendingTasks(),
                    taskRepository.getCompletedTasks(limit = 50)
                ) { allProjectsResource, recentProjects, pendingTasks, completedTasks ->
                    val allProjects = if (allProjectsResource is Resource.Success) {
                        allProjectsResource.data ?: emptyList()
                    } else {
                        emptyList()
                    }
                    
                    // Debug logging to verify data
                    println("DEBUG: Total projects loaded: ${allProjects.size}")
                    println("DEBUG: Recent projects loaded: ${recentProjects.size}")
                    println("DEBUG: Pending tasks loaded: ${pendingTasks.size}")
                    println("DEBUG: Completed tasks loaded: ${completedTasks.size}")
                    
                    val totalProjects = allProjects.size
                    val completedProjects = allProjects.count { it.isCompleted }
                    
                    // Group projects by status for filtering and analytics
                    val projectsByStatus = allProjects.groupBy { 
                        it.status.name
                    }
                    
                    // Count projects by status for pie chart
                    val projectStatusCounts = projectsByStatus.mapValues { it.value.size }
                    
                    // Calculate task completion rate
                    val totalTasks = completedTasks.size + pendingTasks.size
                    val taskCompletionRate = if (totalTasks > 0) {
                        (completedTasks.size.toFloat() / totalTasks.toFloat()) * 100f
                    } else {
                        0f
                    }
                    
                    // Calculate average time to completion for tasks by project
                    val timeToCompletionData = calculateTimeToCompletion(completedTasks)
                    
                    // Calculate team productivity (tasks completed per week)
                    val teamProductivityData = calculateTeamProductivity(completedTasks)
                    
                    // Calculate contribution metrics for each team member
                    val memberContributions = mutableMapOf<String, Int>()
                    
                    // Count completed tasks by user
                    completedTasks.forEach { task ->
                        // assignedTo is a List<String>, so we need to process each assigned user
                        task.assignedTo.forEach { userId ->
                            memberContributions[userId] = (memberContributions[userId] ?: 0) + 1
                        }
                    }
                    
                    // Get all team members involved in projects
                    val teamMemberIds = allProjects.flatMap { project -> 
                        project.members.map { it.userId }
                    }.distinct()
                    
                    // Fetch complete User objects for team members and sort by contribution
                    val teamMembers = teamMemberIds.mapNotNull { userId ->
                        try {
                            // Try to get user synchronously for simplicity
                            val userResource = userRepository.getUserById(userId).first()
                            val user = if (userResource is Resource.Success) userResource.data else null
                            
                            // Add the completed tasks count to the user object
                            user?.copy(
                                completedTasks = memberContributions[userId] ?: 0
                            )
                        } catch (e: Exception) {
                            // Create a placeholder user if we can't fetch the real one
                            com.example.projectmanager.data.model.User(
                                id = userId,
                                displayName = "Team Member",
                                email = "user@example.com",
                                completedTasks = memberContributions[userId] ?: 0
                            )
                        }
                    }.sortedByDescending { it.completedTasks } // Sort by number of completed tasks
                    
                    _uiState.update { state ->
                        state.copy(
                            totalProjects = totalProjects,
                            completedProjects = completedProjects,
                            pendingTasks = pendingTasks.size,
                            recentProjects = recentProjects,
                            upcomingTasks = pendingTasks.take(5),
                            projectsByStatus = projectsByStatus,
                            projectStatusCounts = projectStatusCounts,
                            taskCompletionRate = taskCompletionRate,
                            completedTasks = completedTasks,
                            totalTasks = totalTasks,
                            timeToCompletionData = timeToCompletionData,
                            teamProductivityData = teamProductivityData,
                            teamMembers = teamMembers,
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
    
    /**
     * Calculate average time to completion for tasks by project or category
     */
    private fun calculateTimeToCompletion(completedTasks: List<Task>): List<Pair<String, Float>> {
        // Group tasks by project
        val tasksByProject = completedTasks.groupBy { it.projectId }
        
        return tasksByProject.map { (projectId, tasks) ->
            // Get project name or use project ID if not found
            val projectName = uiState.value.recentProjects.find { it.id == projectId }?.name ?: "Project $projectId"
            
            // Calculate average completion time in days
            val avgCompletionTime = tasks.mapNotNull { task ->
                if (task.completedAt != null && task.createdAt != null) {
                    (task.completedAt.time - task.createdAt.time) / (1000 * 60 * 60 * 24f) // Convert ms to days
                } else null
            }.average().toFloat()
            
            Pair(projectName, avgCompletionTime)
        }.sortedBy { it.second }.take(5) // Take top 5 for chart clarity
    }
    
    /**
     * Calculate team productivity (tasks completed per week)
     */
    private fun calculateTeamProductivity(completedTasks: List<Task>): List<Pair<String, Float>> {
        // Get tasks completed in the last 6 weeks
        val calendar = java.util.Calendar.getInstance()
        val currentWeek = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
        
        val productivityData = mutableListOf<Pair<String, Float>>()
        
        // Calculate tasks completed per week for the last 6 weeks
        for (i in 5 downTo 0) {
            val weekNumber = currentWeek - i
            val weekLabel = "Week ${weekNumber}"
            
            calendar.set(java.util.Calendar.WEEK_OF_YEAR, weekNumber)
            val startOfWeek = calendar.timeInMillis
            calendar.add(java.util.Calendar.DAY_OF_WEEK, 7)
            val endOfWeek = calendar.timeInMillis
            
            val tasksCompletedThisWeek = completedTasks.count { task ->
                task.completedAt?.time in startOfWeek..endOfWeek
            }
            
            productivityData.add(Pair(weekLabel, tasksCompletedThisWeek.toFloat()))
        }
        
        return productivityData
    }

    fun refresh() {
        loadDashboardData()
    }
}