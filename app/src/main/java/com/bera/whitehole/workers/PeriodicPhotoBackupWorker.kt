package com.bera.whitehole.workers

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.util.fastForEach
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.bera.whitehole.R
import com.bera.whitehole.api.BotApi
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.utils.getExtFromMimeType
import com.bera.whitehole.utils.getMimeTypeFromUri
import com.bera.whitehole.utils.sendFileApi
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

    private val channelId: Long = Preferences.getEncryptedLong(Preferences.channelId, 0L)
    private val botApi: BotApi = BotApi
    override suspend fun doWork(): Result {
        val compressionThresholdInBytes = params.inputData.getLong(
            KEY_COMPRESSION_THRESHOLD,
            0L
        )
        val imageList = DbHolder.database.photoDao().getAllNotUploaded()
        return withContext(Dispatchers.IO) {
            try {
                lateinit var tempFile: File
                imageList.fastForEach { photo ->
                    val uri = photo.pathUri.toUri()
                    try {
                        val mimeType = getMimeTypeFromUri(appContext.contentResolver, uri)
                        val ext = getExtFromMimeType(mimeType!!)
                        val bytes = appContext.contentResolver.openInputStream(uri)?.use {
                            it.readBytes()
                        }!!
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        var outputBytes: ByteArray
                        var quality = 100
                        do {
                            val outputStream = ByteArrayOutputStream()
                            val compressFormat = when (mimeType) {
                                MIME_TYPE_JPEG -> Bitmap.CompressFormat.JPEG
                                MIME_TYPE_PNG -> Bitmap.CompressFormat.PNG
                                MIME_TYPE_WEBP -> Bitmap.CompressFormat.WEBP
                                else -> Bitmap.CompressFormat.JPEG
                            }
                            outputStream.use {
                                bitmap.compress(compressFormat, quality, it)
                                outputBytes = it.toByteArray()
                                quality -= (quality * 0.1).roundToInt()
                            }
                        } while (outputBytes.size > compressionThresholdInBytes && quality > 25)
                        tempFile = File.createTempFile(
                            Random.nextLong().toString(),
                            ".$ext"
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
            makeStatusNotification(appContext.getString(R.string.backing_up_photos), appContext),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    companion object {
        const val MIME_TYPE_JPEG = "image/jpeg"
        const val MIME_TYPE_PNG = "image/png"
        const val MIME_TYPE_WEBP = "image/webp"
        const val KEY_COMPRESSION_THRESHOLD = "KEY_COMPRESSION_THRESHOLD"
        const val KEY_RESULT_ERROR = "KEY_RESULT_ERROR"
    }
}
