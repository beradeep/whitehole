package com.bera.whitehole.data.localdb.backup

import com.bera.whitehole.data.localdb.entities.Photo

data class PhotoBackupFile(
    val uploaded: List<Photo> = emptyList()
)
