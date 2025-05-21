package com.example.projectmanager.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmanager.data.model.Skill
import com.example.projectmanager.data.model.User
import com.example.projectmanager.data.repository.SkillRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val skills: List<Skill> = emptyList(),
    val availableSkills: List<Skill> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSearchingSkills: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val skillRepository: SkillRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                userRepository.getCurrentUser().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    user = resource.data,
                                    isLoading = false,
                                    error = null
                                )
                            }
                            
                            // Load user skills
                            loadUserSkills()
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = resource.message
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
                        error = e.message ?: "Failed to load profile"
                    )
                }
            }
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Convert User object to Map<String, Any>
                val updates = mapOf(
                    "displayName" to (user.displayName ?: ""),
                    "photoUrl" to (user.photoUrl ?: ""),
                    "phoneNumber" to (user.phoneNumber ?: ""),
                    "bio" to (user.bio ?: ""),
                    "position" to (user.position ?: ""),
                    "department" to (user.department ?: ""),
                    "skills" to (user.skills ?: emptyList<String>()),
                    "skillIds" to (user.skillIds ?: emptyList<String>())
                )
                
                when (val result = userRepository.updateProfile(updates)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                user = result.data,
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
                        error = e.message ?: "Failed to update profile"
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                userRepository.signOut()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to sign out")
                }
            }
        }
    }
    
    /**
     * Load user skills based on their skill IDs
     */
    private fun loadUserSkills() {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            
            // Combine both skills and skillIds for backward compatibility
            val skillNames = user.skills
            val skillIds = user.skillIds
            
            if (skillIds.isNotEmpty()) {
                // Log the skill IDs for debugging
                Timber.d("Loading skills for IDs: $skillIds")
                
                skillRepository.getSkillsByIds(skillIds).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val loadedSkills = resource.data
                            Timber.d("Loaded ${loadedSkills.size} skills: ${loadedSkills.map { it.name }}")
                            _uiState.update { it.copy(skills = loadedSkills) }
                        }
                        is Resource.Error -> {
                            Timber.e("Error loading skills: ${resource.message}")
                            _uiState.update { it.copy(error = resource.message) }
                        }
                        else -> {}
                    }
                }
            } else if (skillNames.isNotEmpty()) {
                // For backward compatibility, create skill objects from skill names
                Timber.d("Creating skills from names: $skillNames")
                val skills = skillNames.map { name ->
                    Skill(name = name)
                }
                _uiState.update { it.copy(skills = skills) }
            } else {
                Timber.d("No skills or skill IDs found for user")
                _uiState.update { it.copy(skills = emptyList()) }
            }
        }
    }
    
    /**
     * Search for skills based on a query
     */
    fun searchSkills(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingSkills = true) }
            
            if (query.isBlank()) {
                // If query is blank, load all skills
                skillRepository.getAllSkills().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update { 
                                it.copy(
                                    availableSkills = resource.data,
                                    isSearchingSkills = false
                                ) 
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update { 
                                it.copy(
                                    error = resource.message,
                                    isSearchingSkills = false
                                ) 
                            }
                        }
                        else -> {}
                    }
                }
            } else {
                // Search for skills based on query
                skillRepository.searchSkills(query).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update { 
                                it.copy(
                                    availableSkills = resource.data,
                                    isSearchingSkills = false
                                ) 
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update { 
                                it.copy(
                                    error = resource.message,
                                    isSearchingSkills = false
                                ) 
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
    
    /**
     * Add a skill to the user's profile
     */
    fun addSkill(skillName: String) {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            
            Timber.d("Adding skill: $skillName for user: ${user.id}")
            
            // Create a new skill if it doesn't exist
            val skill = Skill(
                name = skillName,
                createdBy = user.id
            )
            
            when (val result = skillRepository.createSkill(skill)) {
                is Resource.Success -> {
                    // Add the skill ID to the user's profile
                    val createdSkill = result.data
                    Timber.d("Skill created/found: ${createdSkill.name} with ID: ${createdSkill.id}")
                    
                    val updatedSkillIds = user.skillIds + createdSkill.id
                    val updatedSkills = user.skills + createdSkill.name
                    
                    Timber.d("Updating user with skillIds: $updatedSkillIds")
                    
                    // Update the user's profile
                    val updates = mapOf(
                        "skillIds" to updatedSkillIds,
                        "skills" to updatedSkills
                    )
                    
                    when (val updateResult = userRepository.updateProfile(updates)) {
                        is Resource.Success -> {
                            val updatedUser = updateResult.data
                            Timber.d("User updated with skillIds: ${updatedUser?.skillIds}")
                            
                            // Update the UI state with both the updated user and the new skill
                            _uiState.update { 
                                it.copy(
                                    user = updatedUser,
                                    skills = it.skills + createdSkill
                                ) 
                            }
                            
                            // Reload skills to ensure they're properly loaded
                            loadUserSkills()
                        }
                        is Resource.Error -> {
                            Timber.e("Error updating user: ${updateResult.message}")
                            _uiState.update { it.copy(error = updateResult.message) }
                        }
                        else -> {}
                    }
                }
                is Resource.Error -> {
                    Timber.e("Error creating skill: ${result.message}")
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }
    
    /**
     * Add an existing skill to the user's profile
     */
    fun addExistingSkill(skill: Skill) {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            
            Timber.d("Adding existing skill: ${skill.name} with ID: ${skill.id} for user: ${user.id}")
            
            // Check if the user already has this skill
            if (user.skillIds.contains(skill.id)) {
                Timber.d("User already has this skill")
                _uiState.update { it.copy(error = "You already have this skill") }
                return@launch
            }
            
            // Add the skill ID to the user's profile
            val updatedSkillIds = user.skillIds + skill.id
            val updatedSkills = user.skills + skill.name
            
            Timber.d("Updating user with skillIds: $updatedSkillIds")
            
            // Update the user's profile
            val updates = mapOf(
                "skillIds" to updatedSkillIds,
                "skills" to updatedSkills
            )
            
            when (val result = userRepository.updateProfile(updates)) {
                is Resource.Success -> {
                    val updatedUser = result.data
                    Timber.d("User updated with skillIds: ${updatedUser?.skillIds}")
                    
                    // Update the UI state with both the updated user and the new skill
                    _uiState.update { 
                        it.copy(
                            user = updatedUser,
                            skills = it.skills + skill
                        ) 
                    }
                    
                    // Reload skills to ensure they're properly loaded
                    loadUserSkills()
                }
                is Resource.Error -> {
                    Timber.e("Error updating user: ${result.message}")
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }
    
    /**
     * Remove a skill from the user's profile
     */
    fun removeSkill(skill: Skill) {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            
            // Remove the skill ID from the user's profile
            val updatedSkillIds = user.skillIds.filter { it != skill.id }
            val updatedSkills = user.skills.filter { it != skill.name }
            
            // Update the user's profile
            val updates = mapOf(
                "skillIds" to updatedSkillIds,
                "skills" to updatedSkills
            )
            
            when (val result = userRepository.updateProfile(updates)) {
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            user = result.data,
                            skills = it.skills.filter { s -> s.id != skill.id }
                        ) 
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }
} 