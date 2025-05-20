package com.example.projectmanager.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.MilestoneTemplate
import com.example.projectmanager.data.model.ProjectTemplate
import com.example.projectmanager.data.model.TaskTemplate
import com.example.projectmanager.data.repository.ProjectTemplateRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditTemplateUiState(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val estimatedDuration: Int = 0,
    val tasks: List<TaskTemplate> = emptyList(),
    val milestones: List<MilestoneTemplate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val isValid: Boolean = false
)

@HiltViewModel
class EditTemplateViewModel @Inject constructor(
    private val templateRepository: ProjectTemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditTemplateUiState())
    val uiState: StateFlow<EditTemplateUiState> = _uiState.asStateFlow()

    fun loadTemplate(templateId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = templateRepository.getTemplate(templateId)) {
                is Resource.Success -> {
                    result.data?.let { template ->
                        _uiState.update {
                            it.copy(
                                id = template.id,
                                name = template.name,
                                description = template.description,
                                category = template.category,
                                estimatedDuration = template.estimatedDuration,
                                tasks = template.tasks,
                                milestones = template.milestones,
                                isLoading = false,
                                error = null
                            )
                        }
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
            validateForm()
        }
    }

    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = if (name.isBlank()) "Name is required" else null
            )
        }
        validateForm()
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
        validateForm()
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
        validateForm()
    }

    fun updateEstimatedDuration(duration: Int) {
        _uiState.update { it.copy(estimatedDuration = duration) }
        validateForm()
    }

    fun addTask(task: TaskTemplate) {
        _uiState.update {
            it.copy(tasks = it.tasks + task)
        }
        validateForm()
    }

    fun removeTask(task: TaskTemplate) {
        _uiState.update {
            it.copy(tasks = it.tasks - task)
        }
        validateForm()
    }

    fun addMilestone(milestone: MilestoneTemplate) {
        _uiState.update {
            it.copy(milestones = it.milestones + milestone)
        }
        validateForm()
    }

    fun removeMilestone(milestone: MilestoneTemplate) {
        _uiState.update {
            it.copy(milestones = it.milestones - milestone)
        }
        validateForm()
    }

    fun saveTemplate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val template = ProjectTemplate(
                id = uiState.value.id,
                name = uiState.value.name,
                description = uiState.value.description,
                category = uiState.value.category,
                estimatedDuration = uiState.value.estimatedDuration,
                tasks = uiState.value.tasks,
                milestones = uiState.value.milestones
            )

            when (val result = if (template.id.isBlank()) {
                templateRepository.createTemplate(template)
            } else {
                templateRepository.updateTemplate(template)
            }) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
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

    private fun validateForm() {
        _uiState.update {
            it.copy(
                isValid = it.name.isNotBlank() &&
                        it.nameError == null &&
                        it.tasks.isNotEmpty()
            )
        }
    }
} 