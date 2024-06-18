package com.bera.whitehole.ui.main.pages.local

import android.net.Uri
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bera.whitehole.data.localphotosource.LocalPhotoSource
import com.bera.whitehole.data.models.PhotoModel
import com.bera.whitehole.workers.WorkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LocalViewModel: ViewModel() {
    val localPhotosFlow: Flow<PagingData<PhotoModel.LocalPhotoModel>> by lazy {
        Pager(
            config = PagingConfig(
                pageSize = 32
            ),
            pagingSourceFactory = { LocalPhotoSource }
        ).flow.cachedIn(viewModelScope)
    }
    fun uploadMultiplePhotos(
        uris: List<Uri>,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            uris.fastForEach { uri ->
                WorkModule.instantUpload(uri)
            }
        }
    }
}