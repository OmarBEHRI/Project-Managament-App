package com.example.projectmanager.data.model

/**
 * Enum representing the different roles a user can have in a project
 */
enum class ProjectRole {
    OWNER,      // Project creator with full access
    MANAGER,    // Can manage the project, tasks, and members
    ADMIN,      // Can manage tasks and members
    MEMBER      // Regular team member
}
