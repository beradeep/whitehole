package com.bera.whitehole.workers

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bera.whitehole.data.localdb.Preferences
import java.time.Duration
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

object WorkModule {
    private lateinit var manager: WorkManager
    fun create(applicationContext: Context) {
        manager = WorkManager.getInstance(applicationContext)
    }

    object PeriodicBackup {

        private val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                NetworkType.valueOf(
                    Preferences.getString(
                        Preferences.autoBackupNetworkTypeKey,
                        NetworkType.CONNECTED.name
                    )
                )
            )
            .build()
        private val repeatIntervalDays = Preferences.getString(
            Preferences.autoBackupIntervalKey,
            Preferences.defaultAutoBackupInterval.toString()
        ).toLong()

        private val periodicUploadWorkRequest =
            PeriodicWorkRequestBuilder<PeriodicPhotoBackupWorker>(
                Duration.ofDays(repeatIntervalDays)
            )
                .setInputData(
                    workDataOf(PeriodicPhotoBackupWorker.KEY_COMPRESSION_THRESHOLD to 1024 * 50L)
                )
                .setConstraints(constraints)
                .setInitialDelay(Duration.ofDays(1))
                .setBackoffCriteria(
                    backoffPolicy = BackoffPolicy.EXPONENTIAL,
                    duration = Duration.ofMinutes(10)
                )
                .build()

        fun enqueue(forceUpdate: Boolean = false) {
            manager.enqueueUniquePeriodicWork(
                PERIODIC_PHOTO_BACKUP_WORK,
                if (forceUpdate) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
                periodicUploadWorkRequest
            )
        }

        fun cancel() {
            manager.cancelUniqueWork(PERIODIC_PHOTO_BACKUP_WORK)
        }
    }

    object SyncMediaStore {

        private val periodicSyncMediaStoreRequest =
            PeriodicWorkRequestBuilder<PeriodicPhotoBackupWorker>(Duration.ofHours(24))
                .setInputData(
                    workDataOf(PeriodicPhotoBackupWorker.KEY_COMPRESSION_THRESHOLD to 1024 * 50L)
                )
                .setInitialDelay(Duration.ofDays(1))
                .setBackoffCriteria(
                    backoffPolicy = BackoffPolicy.EXPONENTIAL,
                    duration = Duration.ofMinutes(10)
                )
                .build()

        private val instantSyncMediaStoreRequest =
            OneTimeWorkRequestBuilder<SyncDbMediaStoreWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

        fun enqueuePeriodic() {
            manager.enqueueUniquePeriodicWork(
                SYNC_MEDIA_STORE_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncMediaStoreRequest
            )
        }

        fun enqueueInstant() {
            manager.enqueueUniqueWork(
                SYNC_MEDIA_STORE_WORK,
                ExistingWorkPolicy.REPLACE,
                instantSyncMediaStoreRequest
            )
        }

        fun cancel() {
            manager.cancelUniqueWork(SYNC_MEDIA_STORE_WORK)
        }
    }

    class InstantUpload(private val uri: Uri) {

        private val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        private val instantUploadRequest =
            OneTimeWorkRequestBuilder<InstantPhotoUploadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(
                    workDataOf(InstantPhotoUploadWorker.KEY_PHOTO_URI to uri.toString())
                )
                .setConstraints(constraints)
                .build()

        fun enqueue() {
            manager.enqueueUniqueWork(
                "$UPLOADING_ID:${uri.lastPathSegment}",
                ExistingWorkPolicy.KEEP,
                instantUploadRequest
            )
        }

        fun cancel() {
            manager.cancelUniqueWork("$UPLOADING_ID:${uri.lastPathSegment}")
        }
    }

    object RestoreMissingFromDevice {

        private val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        private val instantUploadRequest =
            OneTimeWorkRequestBuilder<DownloadMissingPhotosWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .build()

        fun enqueue() {
            manager.enqueueUniqueWork(
                RESTORE_ALL_PHOTOS_WORK,
                ExistingWorkPolicy.KEEP,
                instantUploadRequest
            )
        }

        fun cancel() {
            manager.cancelUniqueWork(RESTORE_ALL_PHOTOS_WORK)
        }
    }

    object PeriodicDbExport {

        private val repeatIntervalDays = Preferences.getString(
            Preferences.autoExportDatabaseIntervalKey,
            Preferences.defaultAutoBackupInterval.toString()
        ).toLong()

        private val uri = Preferences.getString(
            Preferences.autoExportDatabseLocation,
            MediaStore.Downloads.EXTERNAL_CONTENT_URI.toString()
        )

        private val periodicDbExportRequest =
            PeriodicWorkRequestBuilder<PeriodicDbExportWorker>(Duration.ofDays(repeatIntervalDays))
                .setInputData(
                    workDataOf(PeriodicDbExportWorker.KEY_URI to uri)
                )
                .setBackoffCriteria(
                    backoffPolicy = BackoffPolicy.EXPONENTIAL,
                    duration = Duration.ofMinutes(10)
                )
                .build()

        fun enqueue(forceUpdate: Boolean = false) {
            manager.enqueueUniquePeriodicWork(
                PERIODIC_DB_EXPORT_WORK,
                if (forceUpdate) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
                periodicDbExportRequest
            )
        }

        fun cancel() {
            manager.cancelUniqueWork(PERIODIC_DB_EXPORT_WORK)
        }
    }

    fun observeWorkerByName(name: String) = manager.getWorkInfosForUniqueWorkFlow(name)
        .flowOn(Dispatchers.IO)

    fun observeWorkerById(id: String) = manager.getWorkInfoByIdFlow(UUID.fromString(id))
        .flowOn(Dispatchers.IO)

    fun cancelPeriodicBackupWorker() {
        manager.cancelUniqueWork(PERIODIC_PHOTO_BACKUP_WORK)
    }

    const val PERIODIC_PHOTO_BACKUP_WORK = "PeriodicPhotoBackupWork"
    const val SYNC_MEDIA_STORE_WORK = "SyncMediaStoreWork"
    const val RESTORE_ALL_PHOTOS_WORK = "RestoreAllPhotosWork"
    const val PERIODIC_DB_EXPORT_WORK = "PeriodicDbExportWork"
    const val UPLOADING_ID = "UploadingId"
    val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence = "Verbose WorkManager Notifications"
    const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION = "Shows notifications whenever work starts"
    val NOTIFICATION_TITLE: CharSequence = "Whitehole"
    const val CHANNEL_ID = "VERBOSE_WHITE_HOLE_APP_NOTIFICATION"
    const val NOTIFICATION_ID = 1
}