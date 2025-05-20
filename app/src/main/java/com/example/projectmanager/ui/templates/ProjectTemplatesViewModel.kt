package com.example.projectmanager.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.ProjectTemplate
import com.example.projectmanager.data.repository.ProjectTemplateRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectTemplatesUiState(
    val templates: List<ProjectTemplate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProjectTemplatesViewModel @Inject constructor(
    private val templateRepository: ProjectTemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectTemplatesUiState())
    val uiState: StateFlow<ProjectTemplatesUiState> = _uiState.asStateFlow()

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            templateRepository.getTemplates().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                templates = result.data,
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
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            when (val result = templateRepository.deleteTemplate(templateId)) {
                is Resource.Success -> {
                    loadTemplates()
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

    fun createTemplateFromProject(projectId: String) {
        viewModelScope.launch {
            when (val result = templateRepository.createTemplateFromProject(projectId)) {
                is Resource.Success -> {
                    loadTemplates()
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