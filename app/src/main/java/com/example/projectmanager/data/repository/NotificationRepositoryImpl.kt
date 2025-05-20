package com.example.projectmanager.data.repository

import com.example.projectmanager.data.local.dao.NotificationDao
import com.example.projectmanager.data.local.entity.NotificationEntity
import com.example.projectmanager.data.model.Notification
import com.example.projectmanager.data.remote.firebase.FirestoreNotificationSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val firestoreNotificationSource: FirestoreNotificationSource
) : NotificationRepository {
    override fun getNotificationsForUser(userId: String): Flow<List<Notification>> {
        return notificationDao.getNotificationsByUserId(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUnreadNotifications(userId: String): Flow<List<Notification>> {
        return notificationDao.getUnreadNotifications(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUnreadNotificationCount(userId: String): Flow<Int> {
        return notificationDao.getUnreadNotificationCount(userId)
    }

    override suspend fun addNotification(notification: Notification): String {
        val notificationId = firestoreNotificationSource.addNotification(notification)
        notificationDao.insertNotification(NotificationEntity.fromDomain(notification.copy(id = notificationId)))
        return notificationId
    }

    override suspend fun markAsRead(notificationId: String) {
        notificationDao.markNotificationAsRead(notificationId)
        firestoreNotificationSource.markAsRead(notificationId)
    }

    override suspend fun markAllAsRead(userId: String) {
        notificationDao.markAllNotificationsAsRead(userId)
        firestoreNotificationSource.markAllAsRead(userId)
    }

    override suspend fun deleteNotification(notificationId: String) {
        notificationDao.deleteNotificationById(notificationId)
        firestoreNotificationSource.deleteNotification(notificationId)
    }

    override suspend fun deleteOldNotifications(userId: String, before: Date) {
        notificationDao.deleteOldNotifications(userId, before)
        firestoreNotificationSource.deleteOldNotifications(userId, before)
    }
} 