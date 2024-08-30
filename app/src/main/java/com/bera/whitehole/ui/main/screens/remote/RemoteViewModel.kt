package com.bera.whitehole.ui.main.screens.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.data.localdb.entities.Photo
import com.bera.whitehole.data.localdb.entities.RemotePhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RemoteViewModel : ViewModel() {
    val remotePhotosOnDeviceFlow: Flow<PagingData<Photo>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, jumpThreshold = JUMP_THRESHOLD),
            pagingSourceFactory = { DbHolder.database.photoDao().getAllUploadedPaging() }
        ).flow.cachedIn(viewModelScope)

    val remotePhotosNotOnDeviceFlow: Flow<PagingData<RemotePhoto>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, jumpThreshold = JUMP_THRESHOLD),
            pagingSourceFactory = { DbHolder.database.remotePhotoDao().getNotOnDevicePaging() }
        ).flow.cachedIn(viewModelScope)

    val remotePhotosOnDeviceCount: StateFlow<Int> =
        DbHolder.database.photoDao().getAllUploadedCountFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val remotePhotosNotOnDeviceCount: StateFlow<Int> =
        DbHolder.database.remotePhotoDao().getNotOnDeviceCountFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    companion object {
        const val PAGE_SIZE = 32
        const val JUMP_THRESHOLD = 3 * 32
    }
}