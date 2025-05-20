package com.example.projectmanager.data.model

import com.google.firebase.Timestamp

/**
 * Represents a milestone in a project
 */
data class Milestone(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val deadline: Timestamp? = null,
    val status: MilestoneStatus = MilestoneStatus.PENDING,
    val completedAt: Timestamp? = null
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", null, MilestoneStatus.PENDING, null)
}

/**
 * Enum representing the status of a milestone
 */
enum class MilestoneStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    DELAYED
}
