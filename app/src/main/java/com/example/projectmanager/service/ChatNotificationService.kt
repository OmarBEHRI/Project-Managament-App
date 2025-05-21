package com.example.projectmanager.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.projectmanager.R
import com.example.projectmanager.data.model.Chat
import com.example.projectmanager.data.model.Message
import com.example.projectmanager.data.repository.UserPreferencesRepository
import com.example.projectmanager.ui.main.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val CHANNEL_ID = "chat_notifications"
        private const val CHANNEL_NAME = "Chat Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for new chat messages"
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showMessageNotification(message: Message, chat: Chat) {
        coroutineScope.launch {
            // Check if chat is muted
            val mutedChats = userPreferencesRepository.getMutedChats()
            if (mutedChats.contains(chat.id)) {
                return@launch
            }
            
            // Create intent for when notification is tapped
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("destination", "chat")
                putExtra("chatId", chat.id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Build notification
            val chatName = chat.name ?: message.senderName ?: "Chat"
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(chatName)
                .setContentText(message.content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            
            // Show notification
            notificationManager.notify(chat.id.hashCode(), builder.build())
        }
    }
}
