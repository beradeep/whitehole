package com.bera.whitehole.workers

import android.content.Context
import android.net.Uri
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.Duration

object WorkModule {
    private lateinit var manager: WorkManager
    fun create(applicationContext: Context) {
        manager = WorkManager.getInstance(applicationContext)
    }

    fun backupPeriodic() {
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicUploadWorkRequest =
            PeriodicWorkRequestBuilder<PeriodicPhotoBackupWorker>(Duration.ofDays(7))
                .setInputData(
                    workDataOf(PeriodicPhotoBackupWorker.KEY_COMPRESSION_THRESHOLD to 1024 * 50L)
                )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    backoffPolicy = BackoffPolicy.EXPONENTIAL,
                    duration = Duration.ofMinutes(20)
                )
                .build()

        manager.enqueueUniquePeriodicWork(
            PERIODIC_PHOTO_BACKUP_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicUploadWorkRequest
        )
    }

    fun restoreAll() {
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val instantUploadRequest =
            OneTimeWorkRequestBuilder<DownloadAllPhotosWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .build()

        manager.enqueueUniqueWork(
            RESTORE_ALL_PHOTOS_WORK,
            ExistingWorkPolicy.KEEP,
            instantUploadRequest
        )
    }

    fun instantUpload(uri: Uri) {
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val instantUploadRequest =
            OneTimeWorkRequestBuilder<InstantPhotoUploadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(
                    workDataOf(InstantPhotoUploadWorker.KEY_PHOTO_URI to uri.toString())
                )
                .setConstraints(constraints)
                .build()

        manager.enqueueUniqueWork(
            "$UPLOADING_ID:${uri.lastPathSegment}",
            ExistingWorkPolicy.KEEP,
            instantUploadRequest
        )
    }

    fun observeInstantWorkerById(id: String) = flow<List<WorkInfo>> {
        manager.getWorkInfosForUniqueWorkFlow("upload-id:$id")
            .flowOn(Dispatchers.IO)
    }

    fun cancelPeriodicBackupWorker() {
        manager.cancelUniqueWork(PERIODIC_PHOTO_BACKUP_WORK)
    }

    private const val PERIODIC_PHOTO_BACKUP_WORK = "PeriodicPhotoBackupWork"
    private const val RESTORE_ALL_PHOTOS_WORK = "RestoreAllPhotosWork"
    private const val UPLOADING_ID = "UploadingId"
    val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence = "Verbose WorkManager Notifications"
    const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION = "Shows notifications whenever work starts"
    val NOTIFICATION_TITLE: CharSequence = "Whitehole"
    const val CHANNEL_ID = "VERBOSE_WHITE_HOLE_APP_NOTIFICATION"
    const val NOTIFICATION_ID = 1
}