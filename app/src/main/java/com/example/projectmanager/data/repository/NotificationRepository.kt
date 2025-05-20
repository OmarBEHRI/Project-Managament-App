package com.example.projectmanager.data.repository

import com.example.projectmanager.data.model.Notification
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface NotificationRepository {
    fun getNotificationsForUser(userId: String): Flow<List<Notification>>
    fun getUnreadNotifications(userId: String): Flow<List<Notification>>
    fun getUnreadNotificationCount(userId: String): Flow<Int>
    suspend fun addNotification(notification: Notification): String
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead(userId: String)
    suspend fun deleteNotification(notificationId: String)
    suspend fun deleteOldNotifications(userId: String, before: Date)
} 