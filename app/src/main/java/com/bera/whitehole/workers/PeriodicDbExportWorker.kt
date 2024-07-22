package com.bera.whitehole.workers

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.bera.whitehole.R
import com.bera.whitehole.data.localdb.backup.BackupHelper
import com.bera.whitehole.utils.toastFromMainThread
import com.bera.whitehole.workers.WorkModule.NOTIFICATION_ID

class PeriodicDbExportWorker(
    private val appContext: Context,
    private val params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        try {
            setForeground(
                foregroundInfo = getForegroundInfo()
            )
        } catch (e: IllegalStateException) {
            appContext.toastFromMainThread(e.localizedMessage)
        }
        val uri = params.inputData.getString(KEY_URI) ?: return Result.failure()
        BackupHelper.exportDatabase(uri.toUri(), appContext)
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID,
            makeStatusNotification(appContext.getString(R.string.exporting_database), appContext),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    companion object {
        const val KEY_URI = "uri"
    }
}