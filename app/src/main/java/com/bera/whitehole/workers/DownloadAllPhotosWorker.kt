package com.bera.whitehole.workers

import android.content.ContentValues
import android.content.Context
import android.content.pm.ServiceInfo
import android.provider.MediaStore
import androidx.compose.ui.util.fastForEach
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.bera.whitehole.api.BotApi
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.utils.getMimeTypeFromExt
import com.bera.whitehole.utils.toastFromMainThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

class DownloadAllPhotosWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                setForeground(
                    foregroundInfo = getForegroundInfo()
                )
            } catch (e: IllegalStateException) {
                context.toastFromMainThread(e.localizedMessage)
            }
            try {
                DbHolder.database.photoDao().getAllPhotos()
                    .fastForEach { photo ->
                        val byteArray = BotApi.getFile(photo.remoteId)
                        val inStream = ByteArrayInputStream(byteArray)
                        val contentValues = ContentValues().apply {
                            put(
                                MediaStore.MediaColumns.DISPLAY_NAME,
                                "whitehole_${photo.remoteId}.${photo.photoType}"
                            )
                            put(
                                MediaStore.MediaColumns.MIME_TYPE,
                                getMimeTypeFromExt(photo.photoType)
                            )
                            put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/Whitehole")
                        }

                        val resolver = context.contentResolver

                        val uri = resolver.insert(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            contentValues
                        )
                        if (uri != null) {
                            inStream.use {
                                resolver.openOutputStream(uri).use { outStream ->
                                    inStream.copyTo(outStream!!)
                                }
                            }
                            val newPhoto = photo.copy(
                                localId = uri.lastPathSegment ?: ""
                            )
                            DbHolder.database.photoDao().upsertPhotos(newPhoto)
                        }
                    }
                return@withContext Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                context.toastFromMainThread(e.localizedMessage)
                return@withContext Result.failure()
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            WorkModule.NOTIFICATION_ID,
            makeStatusNotification("Saving all photos to device..", context),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }
}