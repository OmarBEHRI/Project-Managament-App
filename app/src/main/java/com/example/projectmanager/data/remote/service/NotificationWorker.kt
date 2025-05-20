package com.example.projectmanager.data.remote.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.projectmanager.data.model.NotificationType
import com.example.projectmanager.util.Constants.KEY_NOTIFICATION_CONTENT
import com.example.projectmanager.util.Constants.KEY_NOTIFICATION_ID
import com.example.projectmanager.util.Constants.KEY_NOTIFICATION_TITLE
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.Date

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationService: NotificationService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val notificationId = inputData.getString(KEY_NOTIFICATION_ID) ?: return Result.failure()
            val title = inputData.getString(KEY_NOTIFICATION_TITLE) ?: return Result.failure()
            val content = inputData.getString(KEY_NOTIFICATION_CONTENT) ?: return Result.failure()

            notificationService.showNotification(
                notificationId.hashCode(),
                title,
                content
            )

            return Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Failed to show notification")
            return Result.failure()
        }
    }
}