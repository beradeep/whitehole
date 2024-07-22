package com.bera.whitehole.data.localdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bera.whitehole.data.localdb.dao.PhotoDao
import com.bera.whitehole.data.localdb.dao.RemotePhotoDao
import com.bera.whitehole.data.localdb.entities.Photo
import com.bera.whitehole.data.localdb.entities.RemotePhoto
import com.bera.whitehole.data.localdb.migration.Migration1to2_NullableRemoteId
import com.bera.whitehole.data.localdb.migration.Migration2to3_RemotePhotoTable

@Database(
    entities = [
        Photo::class, RemotePhoto::class
    ],
    version = 3
)
abstract class WhDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun remotePhotoDao(): RemotePhotoDao

    companion object {
        private const val DATABASE_NAME = "TeledriveDb"

        private fun migrations() = arrayOf(
            Migration1to2_NullableRemoteId(),
            Migration2to3_RemotePhotoTable()
        )

        fun create(applicationContext: Context): WhDatabase {
            return Room
                .databaseBuilder(
                    applicationContext,
                    WhDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(*migrations())
                .build()
        }
    }
}