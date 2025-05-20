package com.example.projectmanager.data.remote.firebase

import com.example.projectmanager.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreNotificationSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val notificationsCollection = firestore.collection("notifications")

    suspend fun addNotification(notification: Notification): String {
        val documentRef = if (notification.id.isNotEmpty()) {
            notificationsCollection.document(notification.id)
        } else {
            notificationsCollection.document()
        }
        val notificationWithId = notification.copy(id = documentRef.id)
        documentRef.set(notificationWithId).await()
        return documentRef.id
    }

    suspend fun updateNotification(notification: Notification) {
        notificationsCollection.document(notification.id).set(notification).await()
    }

    suspend fun deleteNotification(notificationId: String) {
        notificationsCollection.document(notificationId).delete().await()
    }

    suspend fun markAsRead(notificationId: String) {
        notificationsCollection.document(notificationId)
            .update("isRead", true)
            .await()
    }

    suspend fun markAllAsRead(userId: String) {
        val batch = firestore.batch()
        notificationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .await()
            .documents
            .forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
        batch.commit().await()
    }

    suspend fun deleteOldNotifications(userId: String, before: Date) {
        val batch = firestore.batch()
        notificationsCollection
            .whereEqualTo("userId", userId)
            .whereLessThan("createdAt", before)
            .get()
            .await()
            .documents
            .forEach { doc ->
                batch.delete(doc.reference)
            }
        batch.commit().await()
    }
} 