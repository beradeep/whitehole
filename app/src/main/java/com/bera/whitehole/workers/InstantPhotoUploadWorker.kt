package com.bera.whitehole.workers

import android.content.Context
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.bera.whitehole.R
import com.bera.whitehole.api.BotApi
import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.utils.sendFileViaUri
import com.bera.whitehole.workers.WorkModule.NOTIFICATION_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstantPhotoUploadWorker(
    private val appContext: Context,
    private val params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    private val channelId = Preferences.getEncryptedLong(Preferences.channelId, 0L)
    private val botApi = BotApi
    override suspend fun doWork(): Result {
        try {
            setForeground(getForegroundInfo())
        } catch (e: IllegalStateException) {
            Log.d("PhotoUpload", "FAILED: ${e.localizedMessage}")
            return Result.failure()
        }
        return withContext(Dispatchers.IO) {
            try {
                val photoUriString = params.inputData.getString(KEY_PHOTO_URI)!!
                val photoUri = photoUriString.toUri()
                sendFileViaUri(photoUri, appContext.contentResolver, channelId, botApi)
                setForeground(
                    ForegroundInfo(
                        NOTIFICATION_ID,
                        makeStatusNotification(
                            appContext.getString(R.string.photo_upload_successful),
                            appContext
                        )
                    )
                )
                Result.success()
            } catch (e: Throwable) {
                Log.d("PhotoUpload", "FAILED: ${e.localizedMessage}")
                Result.failure()
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID,
            makeStatusNotification(appContext.getString(R.string.uploading_photo), appContext),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    companion object {
        const val KEY_PHOTO_URI = "photoUri"
    }
}