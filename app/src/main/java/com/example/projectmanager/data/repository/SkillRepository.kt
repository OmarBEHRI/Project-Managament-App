package com.example.projectmanager.data.repository

import com.example.projectmanager.data.model.Skill
import com.example.projectmanager.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing skills
 */
interface SkillRepository {
    /**
     * Get all available skills
     */
    fun getAllSkills(): Flow<Resource<List<Skill>>>
    
    /**
     * Get skills by their IDs
     */
    fun getSkillsByIds(skillIds: List<String>): Flow<Resource<List<Skill>>>
    
    /**
     * Search for skills by name
     */
    fun searchSkills(query: String): Flow<Resource<List<Skill>>>
    
    /**
     * Create a new skill
     */
    suspend fun createSkill(skill: Skill): Resource<Skill>
    
    /**
     * Update an existing skill
     */
    suspend fun updateSkill(skill: Skill): Resource<Skill>
    
    /**
     * Delete a skill
     */
    suspend fun deleteSkill(skillId: String): Resource<Unit>
    
    /**
     * Get a skill by ID
     */
    fun getSkillById(skillId: String): Flow<Resource<Skill>>
}
