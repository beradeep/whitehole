package com.bera.whitehole.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.bera.whitehole.api.BotApi
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.data.localdb.entities.Photo
import com.github.kotlintelegrambot.network.fold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
    var fileName: String? = null
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
    }
    return fileName
}

fun getMimeTypeFromUri(contentResolver: ContentResolver, uri: Uri): String? {
    val mimeType = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        contentResolver.getType(uri)
    } else {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension?.lowercase())
    }
    return mimeType
}

fun getExtFromMimeType(mimeType: String): String? {
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
}

fun scaleIntoContainer(
    direction: ScaleTransitionDirection = ScaleTransitionDirection.INWARDS,
    initialScale: Float = if (direction == ScaleTransitionDirection.OUTWARDS) 0.9f else 1.1f
): EnterTransition {
    return scaleIn(
        animationSpec = tween(220, delayMillis = 90),
        initialScale = initialScale
    ) + fadeIn(animationSpec = tween(220, delayMillis = 90))
}

fun scaleOutOfContainer(
    direction: ScaleTransitionDirection = ScaleTransitionDirection.OUTWARDS,
    targetScale: Float = if (direction == ScaleTransitionDirection.INWARDS) 0.9f else 1.1f
): ExitTransition {
    return scaleOut(
        animationSpec = tween(
            durationMillis = 220,
            delayMillis = 90
        ), targetScale = targetScale
    ) + fadeOut(tween(delayMillis = 90))
}

enum class ScaleTransitionDirection {
    INWARDS, OUTWARDS
}

fun getMimeTypeFromExt(extension: String): String? {
    val mimeTypeMap = MimeTypeMap.getSingleton()
    return mimeTypeMap.getMimeTypeFromExtension(extension)
}

suspend fun sendFileViaUri(
    uri: Uri,
    contentResolver: ContentResolver,
    channelId: Long,
    botApi: BotApi
) {
    val mimeType: String? = getMimeTypeFromUri(contentResolver, uri)
    val fileExtension = getExtFromMimeType(mimeType!!)
    val inputStream = contentResolver.openInputStream(uri)
    inputStream?.use { ipStream ->
        val tempFile = File.createTempFile(Random.nextLong().toString(), fileExtension)
        val outputStream = FileOutputStream(tempFile)
        ipStream.copyTo(outputStream)
        sendFileApi(
            botApi,
            channelId,
            uri,
            tempFile,
            fileExtension!!
        )
        outputStream.close()
        Log.d(ContentValues.TAG, tempFile.name)
        tempFile.deleteOnExit()
    }
}

suspend fun sendFileApi(
    botApi: BotApi,
    channelId: Long,
    pathUri: Uri,
    file: File,
    extension: String,
) {
    botApi.sendFile(file, channelId).fold(
        { response ->
            Log.d("tag", "sendFile: success1")
            response?.result?.document?.let { resFile ->
                val photo =
                    Photo(
                        pathUri.lastPathSegment ?: "",
                        resFile.fileId,
                        extension,
                        pathUri.toString()
                    )
                DbHolder.database.photoDao().upsertPhotos(photo)
                Log.d("tag", "sendFile: success")
                Log.d("tag", "sendFile: $photo")
            } ?: {
                Log.d("tag", "sendFile: failed")
            }
        }
    )
    Log.d("tag", "sendFile: success3")
}

suspend fun Context.toastFromMainThread(msg: String?, length: Int = Toast.LENGTH_LONG) =
    withContext(Dispatchers.Main) {
        Toast.makeText(this@toastFromMainThread, msg ?: "Error", length).show()
    }
