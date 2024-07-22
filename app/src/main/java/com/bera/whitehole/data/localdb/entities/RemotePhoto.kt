package com.bera.whitehole.data.localdb.entities

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "remote_photos")
data class RemotePhoto(
    @PrimaryKey val remoteId: String,
    @ColumnInfo val photoType: String,
) : Parcelable {

    fun toPhoto(): Photo = Photo("", remoteId, photoType, "")

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(
            @JsonProperty("remoteId") remoteId: String,
            @JsonProperty("photoType") photoType: String,
        ): RemotePhoto = RemotePhoto(remoteId, photoType)
    }
}