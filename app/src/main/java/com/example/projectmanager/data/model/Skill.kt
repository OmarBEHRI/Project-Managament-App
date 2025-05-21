package com.example.projectmanager.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a skill that can be associated with users.
 * Skills are created by users and can be reused by other users.
 */
data class Skill(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val createdBy: String = "", // User ID of the creator
    @ServerTimestamp
    val createdAt: Date? = null,
    val usageCount: Int = 0 // Number of users who have this skill
)
