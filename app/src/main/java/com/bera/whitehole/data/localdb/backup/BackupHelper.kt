package com.bera.whitehole.data.localdb.backup

import android.content.Context
import android.net.Uri
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.utils.toastFromMainThread
import com.fasterxml.jackson.databind.ObjectMapper

object BackupHelper {
    const val JSON_MIME = "application/json"
    private val mapper by lazy {
        ObjectMapper()
    }

    suspend fun exportPhotos(uri: Uri, context: Context) {
        try {
            val uploaded = DbHolder.database.photoDao().getAllPhotos()
            val backupFile = PhotoBackupFile(uploaded = uploaded)
            context.contentResolver.openOutputStream(uri)?.use {
                val favoritesJson = mapper.writeValueAsBytes(backupFile)
                it.write(favoritesJson)
            }
            context.toastFromMainThread("Backup Success")
        } catch (e: Exception) {
            context.toastFromMainThread(e.localizedMessage)
        }
    }

    suspend fun importPhotos(uri: Uri, context: Context) {
        try {
            context.contentResolver.openInputStream(uri)?.use {
                val backupFile = mapper.readValue(it.readBytes(), PhotoBackupFile::class.java)
                DbHolder.database.photoDao().upsertPhotos(*backupFile.uploaded.toTypedArray())
            }
            context.toastFromMainThread("Import Success")
        } catch (e: Exception) {
            context.toastFromMainThread(e.localizedMessage)
        }
    }
}