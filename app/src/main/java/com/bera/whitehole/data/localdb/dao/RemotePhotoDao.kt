package com.bera.whitehole.data.localdb.dao

import androidx.annotation.Keep
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bera.whitehole.data.localdb.entities.RemotePhoto
import kotlinx.coroutines.flow.Flow

@Keep
@Dao
interface RemotePhotoDao {

    @Query("SELECT * FROM remote_photos")
    suspend fun getAll(): List<RemotePhoto>

    @Query(
        "SELECT * FROM remote_photos WHERE remoteId NOT IN (SELECT remoteId FROM photos WHERE remoteId IS NOT NULL)"
    )
    suspend fun getNotOnDevice(): List<RemotePhoto>

    @Query(
        "SELECT * FROM remote_photos WHERE remoteId NOT IN (SELECT remoteId FROM photos WHERE remoteId IS NOT NULL)"
    )
    fun getNotOnDevicePaging(): PagingSource<Int, RemotePhoto>

    @Query(
        "SELECT COUNT(*) FROM remote_photos WHERE remoteId NOT IN (SELECT remoteId FROM photos WHERE remoteId IS NOT NULL)"
    )
    fun getNotOnDeviceCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg remotePhotos: RemotePhoto)

    @Delete
    suspend fun deleteAll(vararg remotePhotos: RemotePhoto)

    // NOT USED RIGHT NOW
    @Query("SELECT COUNT(*) FROM remote_photos WHERE remoteId IN (SELECT remoteId FROM photos)")
    fun getOnDeviceCountFlow(): Flow<Int>
}