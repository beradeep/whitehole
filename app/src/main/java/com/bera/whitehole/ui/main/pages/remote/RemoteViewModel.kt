package com.bera.whitehole.ui.main.pages.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.data.models.PhotoModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RemoteViewModel: ViewModel() {
    val remotePhotosFlow: Flow<PagingData<PhotoModel.RemotePhotoModel>> by lazy {
        Pager(
            config = PagingConfig(pageSize = 32),
            pagingSourceFactory = { DbHolder.database.photoDao().getAllPhotosPaging() }
        ).flow.map {  pagingData ->
            pagingData.map {
                it.toRemotePhotoModel()
            }
        }.cachedIn(viewModelScope)
    }
}