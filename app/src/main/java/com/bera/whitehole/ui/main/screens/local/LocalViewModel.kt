package com.bera.whitehole.ui.main.screens.local

import android.net.Uri
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.data.localdb.entities.Photo
import com.bera.whitehole.workers.WorkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocalViewModel : ViewModel() {
    val localPhotosFlow: Flow<PagingData<Photo>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, jumpThreshold = JUMP_THRESHOLD),
            pagingSourceFactory = { DbHolder.database.photoDao().getAllPaging() }
        ).flow.cachedIn(viewModelScope)


    val localPhotosCount: StateFlow<Int> =
        DbHolder.database.photoDao().getAllCountFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)


    fun uploadMultiplePhotos(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            uris.fastForEach { uri ->
                WorkModule.InstantUpload(uri).enqueue()
            }
        }
    }

    companion object {
        const val PAGE_SIZE = 32
        const val JUMP_THRESHOLD = 3 * 32
    }
}