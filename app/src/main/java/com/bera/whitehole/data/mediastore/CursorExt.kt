package com.bera.whitehole.data.mediastore

import android.content.ContentUris
import android.database.Cursor
import android.provider.MediaStore
import com.bera.whitehole.data.localdb.entities.Photo
import com.bera.whitehole.utils.getExtFromMimeType

@Throws(Exception::class)
fun Cursor.getPhotoFromCursor(): Photo {
    val id: Long = getLong(getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID))
    val mimeType: String =
        getString(getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE))
    val contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    val uri = ContentUris.withAppendedId(contentUri, id)
    return Photo(
        localId = id.toString(),
        remoteId = null,
        photoType = getExtFromMimeType(mimeType) ?: "jpg",
        pathUri = uri.toString()
    )
}