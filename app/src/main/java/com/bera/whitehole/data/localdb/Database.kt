package com.bera.whitehole.data.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bera.whitehole.data.localdb.dao.PhotoDao
import com.bera.whitehole.data.localdb.entities.Photo

@Database(
    version = 1,
    entities = [
        Photo::class
    ]
)
abstract class Database : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}