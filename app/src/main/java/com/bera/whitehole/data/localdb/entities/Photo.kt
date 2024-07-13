package com.bera.whitehole.data.localdb.entities

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bera.whitehole.data.models.PhotoModel
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Keep
@Entity(tableName = "photos")
@Parcelize
data class Photo(
    @ColumnInfo val localId: String,
    @PrimaryKey val remoteId: String,
    @ColumnInfo val photoType: String,
    @ColumnInfo val pathUri: String,
): Parcelable {
    fun toRemotePhotoModel(): PhotoModel.RemotePhotoModel {
        return PhotoModel.RemotePhotoModel(
            id = this.remoteId,
            type = this.photoType,
            uri = this.pathUri
        )
    }

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(
            @JsonProperty("localId") localId: String,
            @JsonProperty("remoteId") remoteId: String,
            @JsonProperty("photoType") photoType: String,
            @JsonProperty("pathUri") pathUri: String
        ): Photo = Photo(localId, remoteId, photoType, pathUri)
    }
}
