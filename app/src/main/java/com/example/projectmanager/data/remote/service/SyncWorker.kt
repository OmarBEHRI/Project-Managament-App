package com.example.projectmanager.data.remote.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.projectmanager.data.remote.service.SyncService
import com.example.projectmanager.data.repository.ProjectRepository
import com.example.projectmanager.data.repository.TaskRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncService: SyncService,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        try {
            val entityType = inputData.getString(Constants.SYNC_ENTITY_TYPE)
            val entityId = inputData.getString(Constants.SYNC_ENTITY_ID)

            // If specific entity specified, sync just that
            if (!entityType.isNullOrBlank() && !entityId.isNullOrBlank()) {
                when (entityType) {
                    Constants.ENTITY_TYPE_USER -> userRepository.syncUser(entityId)
                    Constants.ENTITY_TYPE_PROJECT -> projectRepository.syncProject(entityId)
                    Constants.ENTITY_TYPE_TASK -> taskRepository.syncTask(entityId)
                }
                return Result.success()
            }

            // Otherwise do a full sync
            syncService.performDataSync()
            return Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Sync worker failed")
            return Result.failure()
        }
    }
}