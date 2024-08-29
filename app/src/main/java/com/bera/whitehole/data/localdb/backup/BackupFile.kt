package com.bera.whitehole.data.localdb.backup

import androidx.annotation.Keep
import com.bera.whitehole.data.localdb.entities.Photo
import com.bera.whitehole.data.localdb.entities.RemotePhoto

@Keep
data class BackupFile(
    val photos: List<Photo> = emptyList(),
    val remotePhotos: List<RemotePhoto> = emptyList(),
)
