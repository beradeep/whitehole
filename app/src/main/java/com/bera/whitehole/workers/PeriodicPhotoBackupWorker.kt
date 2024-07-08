package com.bera.whitehole.workers

import android.content.ContentUris
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.util.fastForEach
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.bera.whitehole.api.BotApi
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.utils.getExtFromMimeType
import com.bera.whitehole.utils.getMimeTypeFromUri
import com.bera.whitehole.utils.sendFileApi
import com.bera.whitehole.utils.toastFromMainThread
import com.bera.whitehole.workers.WorkModule.NOTIFICATION_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlin.math.roundToInt
import kotlin.random.Random

class PeriodicPhotoBackupWorker(
    private val appContext: Context,
    private val params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    private val channelId: Long = Preferences.getLong(Preferences.channelId, 0L)
    private val botApi: BotApi = BotApi
    override suspend fun doWork(): Result {
        try {
            setForeground(
                foregroundInfo = getForegroundInfo()
            )
        } catch (e: IllegalStateException) {
            appContext.toastFromMainThread(e.localizedMessage)
        }
        val compressionThresholdInBytes = params.inputData.getLong(
            KEY_COMPRESSION_THRESHOLD,
            0L
        )
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )
        val imageList = mutableListOf<Uri>()
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        return withContext(Dispatchers.IO) {
            val cursor = appContext.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )
            cursor?.use {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    imageList.add(contentUri)
                }
            }
            lateinit var tempFile: File
            try {
                imageList.fastForEach { uri ->
                    val uriString = uri.toString()
                    val localId = uri.lastPathSegment
                    val isUploaded = localId?.let {
                        DbHolder.database.photoDao().isUploaded(it)
                    }
                    if (isUploaded == 0) {
                        try {
                            val mimeType = getMimeTypeFromUri(appContext.contentResolver, uri)
                            val ext = getExtFromMimeType(mimeType!!)
                            val bytes = appContext.contentResolver.openInputStream(uri)?.use {
                                it.readBytes()
                            } ?: return@withContext Result.failure()
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            var outputBytes: ByteArray
                            var quality = 100
                            do {
                                val outputStream = ByteArrayOutputStream()
                                val compressFormat = when (mimeType) {
                                    "image/jpeg" -> Bitmap.CompressFormat.JPEG
                                    "image/png" -> Bitmap.CompressFormat.PNG
                                    "image/webp" -> Bitmap.CompressFormat.WEBP_LOSSY
                                    else -> Bitmap.CompressFormat.JPEG
                                }
                                outputStream.use {
                                    bitmap.compress(compressFormat, quality, it)
                                    outputBytes = it.toByteArray()
                                    quality -= (quality * 0.1).roundToInt()
                                }
                            } while (outputBytes.size > compressionThresholdInBytes && quality > 25)
                            tempFile = File.createTempFile(
                                "${Random.nextLong()}",
                                ext
                            )
                            tempFile.writeBytes(outputBytes)
                            sendFileApi(botApi, channelId, uri, tempFile, ext!!)
                        } catch (e: IOException) {
                            return@withContext Result.failure(
                                workDataOf(KEY_RESULT_ERROR to "${e.message}")
                            )
                        } finally {
                            tempFile.deleteOnExit()
                        }
                    }
                }
                Result.success()
            } catch (e: Exception) {
                Result.failure(
                    workDataOf(KEY_RESULT_ERROR to "${e.message}")
                )
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID,
            makeStatusNotification("Backing up photos..", appContext),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    companion object {
        const val KEY_EXTENSION = "KEY_EXTENSION"
        const val KEY_CONTENT_URI = "KEY_CONTENT_URI"
        const val KEY_FILE_NAME = "KEY_FILE_NAME"
        const val KEY_COMPRESSION_THRESHOLD = "KEY_COMPRESSION_THRESHOLD"
        const val KEY_RESULT_ERROR = "KEY_RESULT_ERROR"
    }
}