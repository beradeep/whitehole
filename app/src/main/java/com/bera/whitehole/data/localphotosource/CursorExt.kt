package com.bera.whitehole.data.localphotosource

import android.content.ContentUris
import android.database.Cursor
import android.provider.MediaStore
import com.bera.whitehole.data.models.PhotoModel
import com.bera.whitehole.utils.getExtFromMimeType

@Throws(Exception::class)
fun Cursor.getPhotoFromCursor(): PhotoModel.LocalPhotoModel {
    val id: Long = getLong(getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID))
    val mimeType: String = getString(getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE))
    val contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    val uri = ContentUris.withAppendedId(contentUri, id)
    return PhotoModel.LocalPhotoModel(
        id = id.toString(),
        type = getExtFromMimeType(mimeType) ?: "jpg",
        uri = uri.toString()
    )
}