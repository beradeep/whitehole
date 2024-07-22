package com.bera.whitehole.workers

import android.content.ContentValues
import android.content.Context
import android.content.pm.ServiceInfo
import android.provider.MediaStore
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.bera.whitehole.R
import com.bera.whitehole.api.BotApi
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.data.localdb.entities.Photo
import com.bera.whitehole.data.localdb.entities.RemotePhoto
import com.bera.whitehole.utils.getMimeTypeFromExt
import com.bera.whitehole.utils.toastFromMainThread
import java.io.ByteArrayInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadMissingPhotosWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        try {
            setForeground(
                foregroundInfo = getForegroundInfo()
            )
        } catch (e: IllegalStateException) {
            context.toastFromMainThread(e.localizedMessage)
        }
        try {
            withContext(Dispatchers.IO) {
                val remoteIdList = DbHolder.database.remotePhotoDao().getNotOnDevice()
                val photosToInsert = mutableListOf<Photo>()
                val remotesPhotosToRemove = mutableListOf<RemotePhoto>()
                remoteIdList
                    .forEach { remotePhoto ->
                        val byteArray = BotApi.getFile(remotePhoto.remoteId)!!
                        val inStream = ByteArrayInputStream(byteArray)
                        val contentValues = ContentValues().apply {
                            put(
                                MediaStore.MediaColumns.DISPLAY_NAME,
                                context.getString(
                                    R.string.whitehole,
                                    remotePhoto.remoteId,
                                    remotePhoto.photoType
                                )
                            )
                            put(
                                MediaStore.MediaColumns.MIME_TYPE,
                                getMimeTypeFromExt(remotePhoto.photoType)!!
                            )
                            put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                context.getString(R.string.download_whitehole)
                            )
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
                            photosToInsert.add(
                                Photo(
                                    localId = uri.lastPathSegment!!,
                                    remoteId = remotePhoto.remoteId,
                                    photoType = remotePhoto.photoType,
                                    pathUri = uri.toString()
                                )
                            )
                        }
                    }
                DbHolder.database.photoDao().insertPhotos(*photosToInsert.toTypedArray())
                DbHolder.database.remotePhotoDao().deleteAll(*remotesPhotosToRemove.toTypedArray())
            }
            return Result.success()
        } catch (e: Exception) {
            Log.d("Download All Photos", "doWork: ${e.localizedMessage}")
            context.toastFromMainThread(e.localizedMessage)
            return Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            WorkModule.NOTIFICATION_ID,
            makeStatusNotification("Downloading photos", context),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }
}