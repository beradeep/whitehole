package com.bera.whitehole.data.models

import com.bera.whitehole.data.localdb.entities.Photo

sealed class PhotoModel(
    val localId: String?,
    val remoteId: String?,
    val photoType: String,
    val pathUri: String
) {
    data class LocalPhotoModel(val id: String, val type: String, val uri: String) :
        PhotoModel(id, null, type, uri)

    data class RemotePhotoModel(val id: String, val type: String, val uri: String) :
        PhotoModel(null, id, type, uri)

    fun toPhotoEntity(): Photo =
        Photo(localId ?: "", remoteId ?: "", photoType, pathUri)
}