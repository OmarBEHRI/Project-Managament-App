package com.example.projectmanager.data.model

import java.util.*

/**
 * Represents a member of a project with their role and join date
 */
data class ProjectMember(
    val userId: String = "",
    val role: ProjectRole = ProjectRole.MEMBER,
    val joinedAt: Date = Date()
) {
    // Empty constructor for Firebase
    constructor() : this("", ProjectRole.MEMBER, Date())
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as ProjectMember
        
        return userId == other.userId
    }
    
    override fun hashCode(): Int {
        return userId.hashCode()
    }
}
