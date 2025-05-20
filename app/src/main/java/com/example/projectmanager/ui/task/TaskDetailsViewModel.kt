package com.example.projectmanager.ui.task

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.*
import com.example.projectmanager.data.repository.ProjectRepository
import com.example.projectmanager.data.repository.TaskRepository
import com.example.projectmanager.data.service.StorageService
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class TaskDetailsUiState(
    val task: Task? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val storageService: StorageService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailsUiState())
    val uiState: StateFlow<TaskDetailsUiState> = _uiState.asStateFlow()

    fun loadTask(taskId: String, projectId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                taskRepository.getTaskById(taskId).collect { result ->
                    when (result) {
                        is Resource.Success<Task> -> {
                            _uiState.update {
                                it.copy(
                                    task = result.data,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.message
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
                        error = e.message ?: "Failed to load task"
                    )
                }
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = taskRepository.updateTask(task)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            task = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
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

    fun updateStatus(status: TaskStatus) {
        uiState.value.task?.let { task ->
            updateTask(task.copy(status = status))
        }
    }

    fun updatePriority(priority: Priority) {
        uiState.value.task?.let { task ->
            updateTask(task.copy(priority = priority))
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            uiState.value.task?.let { task ->
                _uiState.update { it.copy(isLoading = true) }

                try {
                    when (val result = taskRepository.deleteTask(task.id)) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    task = null,
                                    isLoading = false,
                                    error = null
                                )
                            }
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
                            error = e.message ?: "Failed to delete task"
                        )
                    }
                }
            }
        }
    }

    fun addComment(text: String) {
        viewModelScope.launch {
            uiState.value.task?.let { task ->
                val currentUserId = getCurrentUserId() // Get current user ID
                val comment = Comment(
                    id = UUID.randomUUID().toString(),
                    projectId = task.projectId, // Added projectId from task
                    taskId = task.id, // Added taskId from task
                    userId = currentUserId,
                    authorName = "Current User", // Placeholder, fetch actual name if possible
                    content = text,
                    createdAt = Date() // Pass Date object directly
                )

                val updatedTask = task.copy(
                    comments = task.comments + comment
                )

                updateTask(updatedTask)
            }
        }
    }

    fun deleteComment(comment: Comment) {
        viewModelScope.launch {
            uiState.value.task?.let { task ->
                val updatedTask = task.copy(
                    comments = task.comments - comment
                )

                updateTask(updatedTask)
            }
        }
    }

    fun addAttachment(uri: Uri) {
        viewModelScope.launch {
            uiState.value.task?.let { task ->
                _uiState.update { it.copy(isLoading = true) }

                storageService.uploadFile(
                    uri = uri,
                    projectId = task.projectId,
                    taskId = task.id
                ).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val updatedTask = task.copy(
                                attachments = task.attachments + result.data
                            )
                            updateTask(updatedTask)
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.message
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
            }
        }
    }

    fun downloadAttachment(attachment: FileAttachment) {
        viewModelScope.launch {
            storageService.downloadFile(attachment).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        // Handle successful download (e.g., open file)
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = result.message)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun deleteAttachment(attachment: FileAttachment) {
        viewModelScope.launch {
            uiState.value.task?.let { task ->
                _uiState.update { it.copy(isLoading = true) }

                // Delete from storage
                storageService.deleteFile(attachment).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            // Update task
                            val updatedTask = task.copy(
                                attachments = task.attachments - attachment
                            )
                            updateTask(updatedTask)
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
        }
    }

    fun addChecklist(title: String) {
        viewModelScope.launch {
            uiState.value.task?.let { task ->
                val checklist = Checklist(
                    id = UUID.randomUUID().toString(),
                    title = title
                )

                val updatedTask = task.copy(
                    checklists = task.checklists + checklist
                )

                updateTask(updatedTask)
            }
        }
    }

    fun addChecklistItem(checklistId: String, text: String) {
        viewModelScope.launch {
            uiState.value.task?.let { task ->
                val updatedChecklists = task.checklists.map { checklist ->
                    if (checklist.id == checklistId) {
                        checklist.copy(
                            items = checklist.items + ChecklistItem(
                                id = UUID.randomUUID().toString(),
                                text = text
                            )
                        )
                    } else {
                        checklist
                    }
                }

                val updatedTask = task.copy(checklists = updatedChecklists)
                updateTask(updatedTask)
            }
        }
    }

    fun toggleChecklistItem(checklistId: String, itemId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            uiState.value.task?.let { task ->
                val updatedChecklists = task.checklists.map { checklist ->
                    if (checklist.id == checklistId) {
                        checklist.copy(
                            items = checklist.items.map { item ->
                                if (item.id == itemId) {
                                    item.copy(
                                        isCompleted = isCompleted,
                                        completedBy = if (isCompleted) getCurrentUserId() else null,
                                        completedAt = if (isCompleted) Date() else null
                                    )
                                } else {
                                    item
                                }
                            }
                        )
                    } else {
                        checklist
                    }
                }

                val updatedTask = task.copy(checklists = updatedChecklists)
                updateTask(updatedTask)
            }
        }
    }

    fun deleteChecklist(checklistId: String) {
        viewModelScope.launch {
            uiState.value.task?.let { task ->
                val updatedTask = task.copy(
                    checklists = task.checklists.filter { it.id != checklistId }
                )
                updateTask(updatedTask)
            }
        }
    }

    fun deleteChecklistItem(checklistId: String, itemId: String) {
        viewModelScope.launch {
            uiState.value.task?.let { task ->
                val updatedChecklists = task.checklists.map { checklist ->
                    if (checklist.id == checklistId) {
                        checklist.copy(
                            items = checklist.items.filter { it.id != itemId }
                        )
                    } else {
                        checklist
                    }
                }

                val updatedTask = task.copy(checklists = updatedChecklists)
                updateTask(updatedTask)
            }
        }
    }

    private fun getCurrentUserId(): String {
        // Implement getting current user ID from UserRepository
        return ""
    }
} 