package com.example.projectmanager.util

import com.example.projectmanager.data.model.*
import com.google.firebase.Timestamp
import java.util.*
import kotlin.random.Random

/**
 * Utility class to generate sample data for UI testing
 */
object SampleDataGenerator {
    
    private val random = Random(System.currentTimeMillis())
    
    /**
     * Generate a list of sample projects with varying completion levels, priorities, and deadlines
     */
    fun generateSampleProjects(count: Int = 10): List<Project> {
        val projects = mutableListOf<Project>()
        val currentUserId = "current_user"
        
        for (i in 1..count) {
            val totalTasks = random.nextInt(5, 20)
            val completedTasks = random.nextInt(0, totalTasks + 1)
            val progressPercentage = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks.toFloat()) * 100f else 0f
            
            // Determine status based on progress
            val status = when {
                progressPercentage >= 100f -> ProjectStatus.COMPLETED
                progressPercentage > 0f -> ProjectStatus.IN_PROGRESS
                random.nextBoolean() -> ProjectStatus.NOT_STARTED
                else -> ProjectStatus.ON_HOLD
            }
            
            // Random deadline between past and future
            val deadline = when {
                random.nextInt(10) < 3 -> null // 30% chance of no deadline
                random.nextInt(10) < 5 -> Date(System.currentTimeMillis() - random.nextLong(1, 30) * 24 * 60 * 60 * 1000) // Past deadline
                else -> Date(System.currentTimeMillis() + random.nextLong(1, 60) * 24 * 60 * 60 * 1000) // Future deadline
            }
            
            // Random priority
            val priority = when (random.nextInt(4)) {
                0 -> Priority.LOW
                1 -> Priority.MEDIUM
                2 -> Priority.HIGH
                else -> Priority.URGENT
            }
            
            // Generate team members (1-10)
            val memberCount = random.nextInt(1, 11)
            val members = mutableListOf<ProjectMember>()
            
            // Always add current user as owner
            members.add(
                ProjectMember(
                    userId = currentUserId,
                    role = ProjectRole.OWNER,
                    joinedAt = Date(System.currentTimeMillis() - random.nextLong(1, 365) * 24 * 60 * 60 * 1000)
                )
            )
            
            // Add other members
            for (j in 1 until memberCount) {
                val role = when {
                    j == 1 && random.nextBoolean() -> ProjectRole.MANAGER
                    j == 2 && random.nextBoolean() -> ProjectRole.ADMIN
                    else -> ProjectRole.MEMBER
                }
                
                members.add(
                    ProjectMember(
                        userId = "user_${j}",
                        role = role,
                        joinedAt = Date(System.currentTimeMillis() - random.nextLong(1, 365) * 24 * 60 * 60 * 1000)
                    )
                )
            }
            
            // Generate milestones
            val milestoneCount = random.nextInt(0, 5)
            val milestones = mutableListOf<Milestone>()
            
            for (j in 1..milestoneCount) {
                val milestoneStatus = when {
                    j <= completedTasks / 4 -> MilestoneStatus.COMPLETED
                    j <= completedTasks / 2 -> MilestoneStatus.IN_PROGRESS
                    random.nextBoolean() -> MilestoneStatus.PENDING
                    else -> MilestoneStatus.DELAYED
                }
                
                val milestoneDeadline = when (milestoneStatus) {
                    MilestoneStatus.COMPLETED -> Timestamp(Date(System.currentTimeMillis() - random.nextLong(1, 30) * 24 * 60 * 60 * 1000))
                    MilestoneStatus.DELAYED -> Timestamp(Date(System.currentTimeMillis() - random.nextLong(1, 15) * 24 * 60 * 60 * 1000))
                    else -> Timestamp(Date(System.currentTimeMillis() + random.nextLong(1, 60) * 24 * 60 * 60 * 1000))
                }
                
                milestones.add(
                    Milestone(
                        id = "milestone_${i}_${j}",
                        title = "Milestone ${j} for Project ${i}",
                        description = "This is a sample milestone for demonstration purposes",
                        deadline = milestoneDeadline,
                        status = milestoneStatus,
                        completedAt = if (milestoneStatus == MilestoneStatus.COMPLETED) 
                            Timestamp(Date(System.currentTimeMillis() - random.nextLong(1, 10) * 24 * 60 * 60 * 1000)) 
                            else null
                    )
                )
            }
            
            // Generate attachments
            val attachmentCount = random.nextInt(0, 5)
            val attachments = mutableListOf<FileAttachment>()
            
            for (j in 1..attachmentCount) {
                attachments.add(
                    FileAttachment(
                        id = "attachment_${i}_${j}",
                        name = "Sample File ${j}.${getRandomFileExtension()}",
                        downloadUrl = "https://example.com/files/sample_${i}_${j}",
                        size = random.nextLong(1024, 10 * 1024 * 1024),
                        mimeType = getRandomMimeType(),
                        uploadedBy = members[random.nextInt(members.size)].userId,
                        uploadedAt = Date(System.currentTimeMillis() - random.nextLong(1, 30) * 24 * 60 * 60 * 1000)
                    )
                )
            }
            
            // Create project
            projects.add(
                Project(
                    id = "project_$i",
                    name = "Sample Project $i",
                    description = "This is a sample project for demonstration purposes. It includes various tasks, team members, and milestones to showcase the UI components.",
                    ownerId = currentUserId,
                    members = members,
                    status = status,
                    priority = priority,
                    deadline = deadline,
                    createdAt = Date(System.currentTimeMillis() - random.nextLong(30, 365) * 24 * 60 * 60 * 1000),
                    updatedAt = Date(System.currentTimeMillis() - random.nextLong(1, 30) * 24 * 60 * 60 * 1000),
                    tags = getRandomTags(),
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    isCompleted = status == ProjectStatus.COMPLETED,
                    visibility = ProjectVisibility.PRIVATE,
                    milestones = milestones,
                    budgetAmount = random.nextDouble(1000.0, 50000.0),
                    budgetCurrency = "USD",
                    actualCost = random.nextDouble(0.0, 60000.0),
                    estimatedHours = random.nextFloat() * 100,
                    actualHours = random.nextFloat() * 120,
                    attachments = attachments
                )
            )
        }
        
        return projects
    }
    
    /**
     * Generate a list of sample tasks for a project
     */
    fun generateSampleTasks(projectId: String, count: Int = 15): List<Task> {
        val tasks = mutableListOf<Task>()
        val currentUserId = "current_user"
        
        for (i in 1..count) {
            val status = when (random.nextInt(6)) {
                0 -> TaskStatus.TODO
                1 -> TaskStatus.IN_PROGRESS
                2 -> TaskStatus.REVIEW
                3 -> TaskStatus.COMPLETED
                4 -> TaskStatus.BLOCKED
                else -> TaskStatus.CANCELLED
            }
            
            val isCompleted = status == TaskStatus.COMPLETED
            
            // Random due date
            val dueDate = when {
                random.nextInt(10) < 2 -> null // 20% chance of no deadline
                random.nextInt(10) < 4 -> Date(System.currentTimeMillis() - random.nextLong(1, 30) * 24 * 60 * 60 * 1000) // Past deadline
                else -> Date(System.currentTimeMillis() + random.nextLong(1, 60) * 24 * 60 * 60 * 1000) // Future deadline
            }
            
            // Random priority
            val priority = when (random.nextInt(4)) {
                0 -> Priority.LOW
                1 -> Priority.MEDIUM
                2 -> Priority.HIGH
                else -> Priority.URGENT
            }
            
            // Random assignees (0-3)
            val assigneeCount = random.nextInt(0, 4)
            val assignees = mutableListOf<String>()
            
            for (j in 1..assigneeCount) {
                assignees.add("user_${j}")
            }
            
            // Create task
            tasks.add(
                Task(
                    id = "task_${projectId}_$i",
                    title = "Sample Task $i",
                    description = "This is a sample task for demonstration purposes. It includes various details like status, priority, and due date.",
                    projectId = projectId,
                    assignedTo = assignees,
                    createdBy = currentUserId,
                    status = status,
                    priority = priority,
                    dueDate = dueDate,
                    createdAt = Date(System.currentTimeMillis() - random.nextLong(1, 60) * 24 * 60 * 60 * 1000),
                    updatedAt = Date(System.currentTimeMillis() - random.nextLong(1, 30) * 24 * 60 * 60 * 1000),
                    isCompleted = isCompleted,
                    completedAt = if (isCompleted) Date(System.currentTimeMillis() - random.nextLong(1, 15) * 24 * 60 * 60 * 1000) else null,
                    isOverdue = dueDate?.let { it.time < System.currentTimeMillis() && !isCompleted } ?: false,
                    estimatedHours = random.nextFloat() * 40,
                    actualHours = if (isCompleted) random.nextFloat() * 50 else null
                )
            )
        }
        
        return tasks
    }
    
    /**
     * Generate a list of sample users
     */
    fun generateSampleUsers(count: Int = 10): List<User> {
        val users = mutableListOf<User>()
        
        // Always add current user
        users.add(
            User(
                id = "current_user",
                email = "current.user@example.com",
                displayName = "Current User",
                photoUrl = null,
                createdAt = Date(System.currentTimeMillis() - 365 * 24 * 60 * 60 * 1000L)
            )
        )
        
        // Add other users
        for (i in 1..count) {
            val firstName = getRandomFirstName()
            val lastName = getRandomLastName()
            
            users.add(
                User(
                    id = "user_$i",
                    email = "${firstName.lowercase()}.${lastName.lowercase()}@example.com",
                    displayName = "$firstName $lastName",
                    photoUrl = null,
                    createdAt = Date(System.currentTimeMillis() - random.nextLong(1, 730) * 24 * 60 * 60 * 1000)
                )
            )
        }
        
        return users
    }
    
    // Helper methods
    
    private fun getRandomTags(): List<String> {
        val allTags = listOf("development", "design", "marketing", "research", "testing", "deployment", "planning", "maintenance", "documentation", "training")
        val tagCount = random.nextInt(0, 5)
        return allTags.shuffled().take(tagCount)
    }
    
    private fun getRandomFileExtension(): String {
        val extensions = listOf("pdf", "docx", "xlsx", "pptx", "txt", "png", "jpg", "zip")
        return extensions[random.nextInt(extensions.size)]
    }
    
    private fun getRandomMimeType(): String {
        val mimeTypes = listOf(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "image/png",
            "image/jpeg",
            "application/zip"
        )
        return mimeTypes[random.nextInt(mimeTypes.size)]
    }
    
    private fun getRandomFirstName(): String {
        val firstNames = listOf("John", "Jane", "Michael", "Emily", "David", "Sarah", "Robert", "Jennifer", "William", "Elizabeth")
        return firstNames[random.nextInt(firstNames.size)]
    }
    
    private fun getRandomLastName(): String {
        val lastNames = listOf("Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Garcia", "Rodriguez", "Wilson")
        return lastNames[random.nextInt(lastNames.size)]
    }
}
