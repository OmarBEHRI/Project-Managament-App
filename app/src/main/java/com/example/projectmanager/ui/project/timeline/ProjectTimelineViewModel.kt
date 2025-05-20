package com.example.projectmanager.ui.project.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.Project
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.TaskStatus
import com.example.projectmanager.data.repository.ProjectRepository
import com.example.projectmanager.data.repository.TaskRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ProjectTimelineUiState(
    val tasks: List<Task> = emptyList(),
    val timelineStartDate: Date = Date(),
    val timelineEndDate: Date = Date(),
    val daysToShow: Int = 30,
    val zoomLevel: Float = 1f,
    val filter: TimelineFilter = TimelineFilter(),
    val sort: TimelineSort = TimelineSort(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class TimelineFilter(
    val statuses: Set<TaskStatus> = TaskStatus.values().toSet(),
    val assignedToMe: Boolean = false,
    val dueDateRange: ClosedRange<Date>? = null
)

data class TimelineSort(
    val option: TimelineSortOption = TimelineSortOption.START_DATE,
    val ascending: Boolean = true
)

enum class TimelineSortOption(val displayName: String) {
    START_DATE("Start Date"),
    DUE_DATE("Due Date"),
    PRIORITY("Priority"),
    STATUS("Status"),
    TITLE("Title")
}

@HiltViewModel
class ProjectTimelineViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectTimelineUiState())
    val uiState: StateFlow<ProjectTimelineUiState> = _uiState.asStateFlow()

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            _uiState.update { currentState -> currentState.copy(isLoading = true) }

            try {
                projectRepository.getProjectById(projectId).collect { project ->
                    if (project != null) {
                        loadProjectTasks(project.id)
                    } else {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = "Project not found"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
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
                taskRepository.getTasksByProject(projectId).collect { tasks ->
                    val timelineStartDate = tasks
                        .mapNotNull { it.startDate }
                        .minOrNull() ?: Date()

                    val timelineEndDate = tasks
                        .mapNotNull { it.dueDate }
                        .maxOrNull() ?: Date()

                    _uiState.update { currentState ->
                        currentState.copy(
                            tasks = applyFilterAndSort(tasks),
                            timelineStartDate = timelineStartDate,
                            timelineEndDate = timelineEndDate,
                            daysToShow = calculateDaysToShow(timelineStartDate, timelineEndDate),
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load project tasks"
                    )
                }
            }
        }
    }

    fun updateFilter(filter: TimelineFilter) {
        _uiState.update { currentState ->
            currentState.copy(
                filter = filter,
                tasks = applyFilterAndSort(currentState.tasks)
            )
        }
    }

    fun updateSort(sort: TimelineSort) {
        _uiState.update { currentState ->
            currentState.copy(
                sort = sort,
                tasks = applyFilterAndSort(currentState.tasks)
            )
        }
    }

    fun zoomIn() {
        _uiState.update { currentState ->
            currentState.copy(
                zoomLevel = (currentState.zoomLevel * 1.2f).coerceAtMost(2f),
                daysToShow = (currentState.daysToShow / 1.2f).toInt().coerceAtLeast(7)
            )
        }
    }

    fun zoomOut() {
        _uiState.update { currentState ->
            currentState.copy(
                zoomLevel = (currentState.zoomLevel / 1.2f).coerceAtLeast(0.5f),
                daysToShow = (currentState.daysToShow * 1.2f).toInt().coerceAtMost(90)
            )
        }
    }

    fun scrollToToday() {
        val today = Date()
        _uiState.update { currentState ->
            currentState.copy(
                timelineStartDate = today,
                timelineEndDate = Calendar.getInstance().apply {
                    time = today
                    add(Calendar.DAY_OF_MONTH, currentState.daysToShow)
                }.time
            )
        }
    }

    private fun applyFilterAndSort(tasks: List<Task>): List<Task> {
        val filtered = tasks.filter { task ->
            val statusMatch = task.status in uiState.value.filter.statuses
            val assigneeMatch = if (uiState.value.filter.assignedToMe) {
                task.assignedTo.contains(getCurrentUserId())
            } else {
                true
            }
            val dateMatch = uiState.value.filter.dueDateRange?.let { range ->
                task.dueDate?.let { it in range } ?: false
            } ?: true

            statusMatch && assigneeMatch && dateMatch
        }

        return when (uiState.value.sort.option) {
            TimelineSortOption.START_DATE -> filtered.sortedBy { it.startDate }
            TimelineSortOption.DUE_DATE -> filtered.sortedBy { it.dueDate }
            TimelineSortOption.PRIORITY -> filtered.sortedBy { it.priority }
            TimelineSortOption.STATUS -> filtered.sortedBy { it.status }
            TimelineSortOption.TITLE -> filtered.sortedBy { it.title }
        }.let {
            if (uiState.value.sort.ascending) it else it.reversed()
        }
    }

    private fun calculateDaysToShow(start: Date, end: Date): Int {
        return ((end.time - start.time) / (1000 * 60 * 60 * 24)).toInt().coerceIn(7, 90)
    }

    private fun getCurrentUserId(): String {
        // Implement getting current user ID from UserRepository
        return ""
    }
} 