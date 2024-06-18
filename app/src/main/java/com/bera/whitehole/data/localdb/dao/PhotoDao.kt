package com.bera.whitehole.data.localdb.dao

import androidx.annotation.Keep
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.bera.whitehole.data.localdb.entities.Photo
import kotlinx.coroutines.flow.Flow

@Keep
@Dao
interface PhotoDao {

    @Query("SELECT * FROM photos")
    suspend fun getAllPhotos(): List<Photo>

    @Query("SELECT * FROM photos")
    fun getAllPhotosFlow(): Flow<List<Photo>>

    @Query("SELECT * FROM photos")
    fun getAllPhotosPaging(): PagingSource<Int, Photo>

    @Query("SELECT remoteId FROM photos")
    suspend fun getAllPhotoIds(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM photos WHERE localId = :localId)")
    suspend fun isUploaded(localId: String): Int

    @Upsert
    fun upsertPhotos(vararg photos: Photo)

    @Delete
    suspend fun deletePhoto(photo: Photo)

    @Query("DELETE FROM photos")
    suspend fun deleteAllPhotos()
}