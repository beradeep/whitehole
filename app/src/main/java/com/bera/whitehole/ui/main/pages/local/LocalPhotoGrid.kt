package com.bera.whitehole.ui.main.pages.local

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import com.bera.whitehole.data.models.PhotoModel
import com.bera.whitehole.ui.main.components.PhotoPageView
import com.bera.whitehole.ui.main.components.itemsPaging
import com.bera.whitehole.utils.coil.ImageLoaderModule

@Composable
fun LocalPhotoGrid(
    localPhotos: LazyPagingItems<PhotoModel.LocalPhotoModel>,
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (localPhotos.loadState.refresh == LoadState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                itemsPaging(
                    items = localPhotos
                ) { localPhoto, index ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                selectedIndex = index
                            }
                    ) {
                        AsyncImage(
                            imageLoader = ImageLoaderModule.defaultImageLoader,
                            model = localPhoto?.pathUri,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
        selectedIndex?.let {
            PhotoPageView(initialPage = it, photos = localPhotos.itemSnapshotList.items) {
                selectedIndex = null
            }
        }
    }
}