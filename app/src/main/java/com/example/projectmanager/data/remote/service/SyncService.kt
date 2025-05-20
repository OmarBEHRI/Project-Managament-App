package com.example.projectmanager.data.remote.service

import android.content.Context
import androidx.work.*
import com.example.projectmanager.data.repository.ProjectRepository
import com.example.projectmanager.data.repository.TaskRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.data.remote.service.SyncWorker
import com.example.projectmanager.util.Constants
import com.example.projectmanager.util.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) {
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime

    init {
        // Setup periodic sync
        setupPeriodicSync()
    }

    fun scheduleSyncNow() {
        if (!networkUtils.isNetworkAvailable()) {
            _syncState.value = SyncState.FAILED
            Timber.d("Sync skipped: No network connection")
            return
        }

        _syncState.value = SyncState.IN_PROGRESS

        // Unique work ensures we don't stack multiple sync operations
        val syncWorkRequest = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )

        // Observe the work status
        workManager.getWorkInfoByIdLiveData(syncWorkRequest.id)
            .observeForever { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        _syncState.value = SyncState.COMPLETE
                        _lastSyncTime.value = System.currentTimeMillis()
                        Timber.d("Sync completed successfully")
                    }
                    WorkInfo.State.FAILED -> {
                        _syncState.value = SyncState.FAILED
                        Timber.d("Sync failed")
                    }
                    WorkInfo.State.CANCELLED -> {
                        _syncState.value = SyncState.IDLE
                        Timber.d("Sync cancelled")
                    }
                    else -> {
                        // Keep the current state
                    }
                }
            }
    }

    fun scheduleSyncFor(entityType: String, entityId: String) {
        val syncWorkRequest = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                workDataOf(
                    Constants.SYNC_ENTITY_TYPE to entityType,
                    Constants.SYNC_ENTITY_ID to entityId
                )
            )
            .build()

        workManager.enqueue(syncWorkRequest)
    }

    fun cancelSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        _syncState.value = SyncState.IDLE
    }

    suspend fun performDataSync() {
        try {
            _syncState.value = SyncState.IN_PROGRESS

            CoroutineScope(Dispatchers.IO).launch {
                // Sync users
                try {
                    userRepository.syncUsers()
                } catch (e: Exception) {
                    Timber.e(e, "Error syncing users")
                }

                // Sync projects
                try {
                    projectRepository.syncProjects()
                } catch (e: Exception) {
                    Timber.e(e, "Error syncing projects")
                }

                // Sync tasks
                try {
                    taskRepository.syncTasks()
                } catch (e: Exception) {
                    Timber.e(e, "Error syncing tasks")
                }

                _syncState.value = SyncState.COMPLETE
                _lastSyncTime.value = System.currentTimeMillis()
            }
        } catch (e: Exception) {
            Timber.e(e, "Sync failed")
            _syncState.value = SyncState.FAILED
        }
    }

    private fun setupPeriodicSync() {
        val periodicSyncRequest = PeriodicWorkRequest.Builder(
            SyncWorker::class.java, SYNC_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }

    enum class SyncState {
        IDLE,
        IN_PROGRESS,
        COMPLETE,
        FAILED
    }

    companion object {
        private const val SYNC_WORK_NAME = "one_time_sync_work"
        private const val PERIODIC_SYNC_WORK_NAME = "periodic_sync_work"
        private const val SYNC_INTERVAL_HOURS = 4L
    }
}