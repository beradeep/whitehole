package com.bera.whitehole.workers

import android.content.Context
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
        return withContext(Dispatchers.IO) {
            val photoUriString = params.inputData.getString(KEY_PHOTO_URI)!!
            val photoUri = photoUriString.toUri()
            try {
                sendFileViaUri(photoUri, appContext.contentResolver, channelId, botApi)
                makeStatusNotification(
                    appContext.getString(R.string.photo_upload_successful),
                    appContext
                )
                Result.success()
            } catch (e: Throwable) {
                Log.d("PhotoUpload", "FAILED: ${e.localizedMessage}")
                Result.failure()
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(
            NOTIFICATION_ID,
            makeStatusNotification(appContext.getString(R.string.uploading_photo), appContext)
        )
    }

    companion object {
        const val KEY_PHOTO_URI = "photoUri"
    }
}