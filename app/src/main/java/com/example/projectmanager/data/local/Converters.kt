package com.example.projectmanager.data.local

import androidx.room.TypeConverter
import com.example.projectmanager.data.model.Comment
import com.example.projectmanager.data.model.Priority
import com.example.projectmanager.data.model.ProjectStatus
import com.example.projectmanager.data.model.TaskStatus
import com.example.projectmanager.data.model.UserRole
import com.example.projectmanager.data.model.NotificationType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromCommentList(value: List<Comment>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCommentList(value: String): List<Comment> {
        val listType = object : TypeToken<List<Comment>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromProjectStatus(value: ProjectStatus): String {
        return value.name
    }

    @TypeConverter
    fun toProjectStatus(value: String): ProjectStatus {
        return ProjectStatus.valueOf(value)
    }

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String {
        return value.name
    }

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus {
        return TaskStatus.valueOf(value)
    }

    @TypeConverter
    fun fromPriority(value: Priority): String {
        return value.name
    }

    @TypeConverter
    fun toPriority(value: String): Priority {
        return Priority.valueOf(value)
    }

    @TypeConverter
    fun fromUserRole(value: UserRole): String {
        return value.name
    }

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return UserRole.valueOf(value)
    }

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String {
        return value.name
    }

    @TypeConverter
    fun toNotificationType(value: String): NotificationType {
        return NotificationType.valueOf(value)
    }

    @TypeConverter
    fun fromDate(value: Date?): Long? {
        return value?.time
    }

    @TypeConverter
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }
}