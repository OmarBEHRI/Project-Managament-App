package com.example.projectmanager.data.service

import com.example.projectmanager.data.model.Skill
import com.example.projectmanager.data.model.Task
import com.example.projectmanager.data.model.User
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for interacting with the Gemini AI API
 */
@Singleton
class GeminiAiService @Inject constructor() {
    
    private val apiKey = "AIzaSyDmwx11QTmZ_bJGa9ru3jVQ7X2xyxFkX6g"
    
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 2048
            },
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
            )
        )
    }
    
    /**
     * Generate tasks from a project description
     * @param projectName The name of the project
     * @param projectDescription The description of the project
     * @return A list of generated tasks or null if not enough information is provided
     */
    suspend fun generateTasksFromDescription(
        projectName: String,
        projectDescription: String
    ): List<Task>? = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are a project management expert. Based on the following project description, 
                generate a list of tasks that would be required to complete this project.
                
                Project Name: $projectName
                Project Description: $projectDescription
                
                If there is not enough information in the description to generate meaningful tasks, 
                respond with exactly "INSUFFICIENT_INFO". Otherwise, generate a JSON array of tasks.
                
                Each task should have the following properties:
                - title: A short, descriptive title for the task
                - description: A more detailed description of what needs to be done
                - estimated_hours: An estimate of how many hours this task might take (integer)
                - priority: One of "LOW", "MEDIUM", or "HIGH"
                
                Format your response as a valid JSON array of objects. Do not include any explanations or text outside the JSON.
                Example:
                [
                  {
                    "title": "Task 1",
                    "description": "Description of task 1",
                    "estimated_hours": 4,
                    "priority": "MEDIUM"
                  },
                  {
                    "title": "Task 2",
                    "description": "Description of task 2",
                    "estimated_hours": 2,
                    "priority": "HIGH"
                  }
                ]
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt).text?.trim() ?: ""
            
            if (response == "INSUFFICIENT_INFO") {
                Timber.d("Not enough information provided for task generation")
                return@withContext null
            }
            
            // Parse the JSON response
            return@withContext parseTasksFromJson(response)
        } catch (e: Exception) {
            Timber.e(e, "Error generating tasks from description")
            return@withContext null
        }
    }
    
    /**
     * Assign tasks to team members based on their skills
     * @param tasks The tasks to assign
     * @param teamMembers The team members available for assignment
     * @return A map of task IDs to user IDs for the assignments, or null if not enough information is available
     */
    suspend fun assignTasksToTeamMembers(
        tasks: List<Task>,
        teamMembers: List<User>
    ): Map<String, String>? = withContext(Dispatchers.IO) {
        try {
            if (tasks.isEmpty() || teamMembers.isEmpty()) {
                Timber.d("No tasks or team members provided for assignment")
                return@withContext null
            }
            
            // Build a representation of team members and their skills
            val teamMembersJson = teamMembers.map { user ->
                val userSkills = user.skills.joinToString(", ")
                """
                {
                    "id": "${user.id}",
                    "name": "${user.displayName ?: "Unknown"}",
                    "skills": "$userSkills"
                }
                """.trimIndent()
            }.joinToString(",\n")
            
            // Build a representation of tasks
            val tasksJson = tasks.map { task ->
                """
                {
                    "id": "${task.id}",
                    "title": "${task.title}",
                    "description": "${task.description ?: ""}",
                    "priority": "${task.priority}"
                }
                """.trimIndent()
            }.joinToString(",\n")
            
            val prompt = """
                You are a project management expert. Based on the following team members and their skills, 
                assign the most appropriate team member to each task.
                
                Team Members:
                [
                $teamMembersJson
                ]
                
                Tasks:
                [
                $tasksJson
                ]
                
                If there is not enough information to make meaningful assignments, respond with exactly "INSUFFICIENT_INFO".
                Otherwise, generate a JSON array of assignments.
                
                Each assignment should have the following properties:
                - task_id: The ID of the task
                - user_id: The ID of the user assigned to the task
                - reasoning: A brief explanation of why this user is a good fit for the task
                
                Format your response as a valid JSON array of objects. Do not include any explanations or text outside the JSON.
                Example:
                [
                  {
                    "task_id": "task123",
                    "user_id": "user456",
                    "reasoning": "This user has experience with the required skills for this task"
                  }
                ]
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt).text?.trim() ?: ""
            
            if (response == "INSUFFICIENT_INFO") {
                Timber.d("Not enough information provided for task assignment")
                return@withContext null
            }
            
            // Parse the JSON response
            return@withContext parseAssignmentsFromJson(response)
        } catch (e: Exception) {
            Timber.e(e, "Error assigning tasks to team members")
            return@withContext null
        }
    }
    
    /**
     * Parse tasks from JSON response
     */
    private fun parseTasksFromJson(json: String): List<Task>? {
        return try {
            // Use a JSON library to parse the response
            // For simplicity, we'll use a regex-based approach here
            // In a real application, you would use a proper JSON parser
            
            val taskRegex = """"title":\s*"([^"]+)",\s*"description":\s*"([^"]+)",\s*"estimated_hours":\s*(\d+),\s*"priority":\s*"([^"]+)"""".toRegex()
            
            val tasks = mutableListOf<Task>()
            
            taskRegex.findAll(json).forEach { matchResult ->
                val (title, description, hoursStr, priorityStr) = matchResult.destructured
                val hours = hoursStr.toFloatOrNull()
                val priority = when (priorityStr.uppercase()) {
                    "LOW" -> com.example.projectmanager.data.model.TaskPriority.LOW
                    "HIGH" -> com.example.projectmanager.data.model.TaskPriority.HIGH
                    else -> com.example.projectmanager.data.model.TaskPriority.MEDIUM
                }
                
                tasks.add(
                    Task(
                        title = title,
                        description = description,
                        estimatedHours = hours,
                        priority = priority
                    )
                )
            }
            
            if (tasks.isEmpty()) {
                Timber.e("Failed to parse tasks from JSON: $json")
                null
            } else {
                tasks
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing tasks from JSON")
            null
        }
    }
    
    /**
     * Parse assignments from JSON response
     */
    private fun parseAssignmentsFromJson(json: String): Map<String, String>? {
        return try {
            // Use a JSON library to parse the response
            // For simplicity, we'll use a regex-based approach here
            // In a real application, you would use a proper JSON parser
            
            val assignmentRegex = """"task_id":\s*"([^"]+)",\s*"user_id":\s*"([^"]+)"""".toRegex()
            
            val assignments = mutableMapOf<String, String>()
            
            assignmentRegex.findAll(json).forEach { matchResult ->
                val (taskId, userId) = matchResult.destructured
                assignments[taskId] = userId
            }
            
            if (assignments.isEmpty()) {
                Timber.e("Failed to parse assignments from JSON: $json")
                null
            } else {
                assignments
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing assignments from JSON")
            null
        }
    }
}
