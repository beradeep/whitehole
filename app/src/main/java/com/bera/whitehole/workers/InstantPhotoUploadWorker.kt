package com.bera.whitehole.workers

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.util.Log
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.bera.whitehole.api.BotApi
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.utils.sendFileViaUri
import com.bera.whitehole.utils.toastFromMainThread
import com.bera.whitehole.workers.WorkModule.NOTIFICATION_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstantPhotoUploadWorker(
    private val appContext: Context,
    private val params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val channelId = Preferences.getLong(Preferences.channelId, 0L)
    private val botApi = BotApi
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val photoUriString = params.inputData.getString(KEY_PHOTO_URI)!!
            val photoUri = photoUriString.toUri()
            val isUploaded = DbHolder.database.photoDao().isUploaded(photoUri.lastPathSegment ?: "")
            if (isUploaded == 0) {
                try {
                    sendFileViaUri(photoUri, appContext.contentResolver, channelId, botApi)
                    Result.success()
                } catch (e: Throwable) {
                    Log.d("PhotoUpload", "FAILED: ${e.localizedMessage}")
                    Result.failure()
                }
            } else {
                makeStatusNotification("Photo upload successful!", appContext)
                Result.success()
            }
        }
    }

    companion object {
        const val KEY_PHOTO_URI = "photoUri"
    }
}