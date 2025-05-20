package com.example.projectmanager.data.remote.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.example.projectmanager.R
import com.example.projectmanager.data.model.Notification
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.MainActivity
import com.example.projectmanager.data.model.User
import com.example.projectmanager.util.Constants
import com.example.projectmanager.util.Resource
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class NotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var userRepository: UserRepository

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: Flow<List<Notification>> = _notifications

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get the Resource<User> from the Flow
                val userResource = userRepository.getCurrentUser().first()

                // Check if it's a Success and extract the User
                when (userResource) {
                    is Resource.Success -> {
                        val user = userResource.data
                        val token = FirebaseMessaging.getInstance().token.await()
                        user?.let {
                            userRepository.updateFcmToken(it.id, token)
                        }
                    }
                    is Resource.Error -> {
                        Timber.e(userResource.exception, "Failed to get current user")
                    }
                    is Resource.Loading -> {
                        // Optionally handle loading state
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to register for FCM notifications")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Don't directly call getCurrentUserId()
                // Instead, get the user first and extract ID if successful
                val userResource = userRepository.getCurrentUser().first()
                when (userResource) {
                    is Resource.Success -> {
                        val user = userResource.data
                        user?.let {
                            userRepository.updateFcmToken(it.id, token)
                        }
                    }
                    else -> {
                        // Handle other cases or use updateFcmToken(token) as fallback
                        userRepository.updateFcmToken(token)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update FCM token")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notification = message.notification
        val data = message.data

        if (notification != null) {
            val notificationId = data[Constants.EXTRA_NOTIFICATION_ID]?.toIntOrNull()
                ?: System.currentTimeMillis().toInt()
            showNotification(notificationId, notification.title, notification.body)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = Constants.NOTIFICATION_CHANNEL_NAME
            val descriptionText = Constants.NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(notificationId: Int, title: String?, content: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            Timber.e(e, "Failed to show notification")
        }
    }

    fun scheduleReminder(notificationId: String, title: String, content: String, delayInMinutes: Long) {
        val data = workDataOf(
            Constants.KEY_NOTIFICATION_ID to notificationId,
            Constants.KEY_NOTIFICATION_TITLE to title,
            Constants.KEY_NOTIFICATION_CONTENT to content
        )

        val reminderRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(data)
            .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
            .build()

        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(reminderRequest)
    }

    fun cancelScheduledReminder(notificationId: String) {
        val workManager = WorkManager.getInstance(this)
        workManager.cancelAllWorkByTag(notificationId)
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    fun markNotificationAsRead(notificationId: String) {
        val currentList = _notifications.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            // Create a copy with updated isRead status
            val notification = currentList[index]
            val updatedNotification = notification.copy(read = true)
            currentList[index] = updatedNotification
            _notifications.value = currentList
        }
    }

    companion object {
        const val CHANNEL_ID = "project_manager_channel"
        const val KEY_NOTIFICATION_ID = "notification_id"
        const val KEY_NOTIFICATION_TITLE = "notification_title"
        const val KEY_NOTIFICATION_CONTENT = "notification_content"
    }
}