package com.example.projectmanager.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class TimeEntry(
    @DocumentId
    val id: String = "",
    @get:PropertyName("user_id")
    val userId: String = "",
    @get:PropertyName("project_id")
    val projectId: String = "",
    @get:PropertyName("task_id")
    val taskId: String? = null,
    val description: String = "",
    @get:PropertyName("start_time")
    val startTime: Date = Date(),
    @get:PropertyName("end_time")
    val endTime: Date? = null,
    val duration: Long = 0, // Duration in minutes
    @get:PropertyName("is_billable")
    val isBillable: Boolean = true,
    val tags: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

data class TimeTrackingSummary(
    @get:PropertyName("total_duration")
    val totalDuration: Long = 0, // Total duration in minutes
    @get:PropertyName("billable_duration")
    val billableDuration: Long = 0,
    @get:PropertyName("entry_count")
    val entryCount: Int = 0,
    val tags: Map<String, Long> = emptyMap() // Tag to duration mapping
) 