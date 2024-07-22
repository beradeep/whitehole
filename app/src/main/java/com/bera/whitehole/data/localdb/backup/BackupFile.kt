package com.bera.whitehole.data.localdb.backup

import com.bera.whitehole.data.localdb.entities.Photo
import com.bera.whitehole.data.localdb.entities.RemotePhoto

data class BackupFile(
    val photos: List<Photo> = emptyList(),
    val remotePhotos: List<RemotePhoto> = emptyList(),
)
