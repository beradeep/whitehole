package com.bera.whitehole.data.localdb.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.bera.whitehole.R
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.utils.toastFromMainThread
import com.fasterxml.jackson.databind.ObjectMapper

object BackupHelper {
    const val JSON_MIME = "application/json"
    private val mapper by lazy {
        ObjectMapper()
    }

    suspend fun exportDatabase(uri: Uri, context: Context) {
        try {
            val photos = DbHolder.database.photoDao().getAll()
            val remotePhotos = DbHolder.database.remotePhotoDao().getAll()
            val backupFile = BackupFile(photos, remotePhotos)
            context.contentResolver.openOutputStream(uri)?.use {
                val favoritesJson = mapper.writeValueAsBytes(backupFile)
                it.write(favoritesJson)
            }
            context.toastFromMainThread(context.getString(R.string.export_successful))
        } catch (e: Exception) {
            context.toastFromMainThread(e.localizedMessage)
            Log.d("Export All Photos", "doWork: ${e.localizedMessage}")
        }
    }

    suspend fun importDatabase(uri: Uri, context: Context) {
        try {
            context.contentResolver.openInputStream(uri)?.use {
                val backupFile = mapper.readValue(it.readBytes(), BackupFile::class.java)
                DbHolder.database.photoDao().updatePhotos(*backupFile.photos.toTypedArray())
                DbHolder.database.remotePhotoDao().insertAll(
                    *backupFile.remotePhotos.toTypedArray()
                )
            }
            context.toastFromMainThread(context.getString(R.string.import_successful))
        } catch (e: Exception) {
            Log.d("Import All Photos", "doWork: ${e.localizedMessage}")
            context.toastFromMainThread(e.localizedMessage)
        }
    }
}