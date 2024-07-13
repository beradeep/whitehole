package com.bera.whitehole.ui.main.pages.remote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.bera.whitehole.data.models.PhotoModel
import com.bera.whitehole.ui.components.LoadAnimation
import com.bera.whitehole.ui.components.PhotoPageView
import com.bera.whitehole.ui.components.itemsPaging
import com.bera.whitehole.utils.coil.ImageLoaderModule

@Composable
fun RemotePhotoGrid(
    remotePhotos: LazyPagingItems<PhotoModel.RemotePhotoModel>
) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (remotePhotos.loadState.refresh == LoadState.Loading) {
            LoadAnimation(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize().padding(2.dp),
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                itemsPaging(
                    remotePhotos
                ) { remotePhoto, index ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                selectedIndex = index
                            }
                    ) {
                        SubcomposeAsyncImage(
                            imageLoader = ImageLoaderModule.defaultImageLoader,
                            model = remotePhoto?.pathUri,
                            contentDescription = "photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = {
                                SubcomposeAsyncImage(
                                    imageLoader = ImageLoaderModule.remoteImageLoader,
                                    model = ImageRequest.Builder(context)
                                        .data(remotePhoto)
                                        .placeholderMemoryCacheKey(remotePhoto?.remoteId)
                                        .memoryCacheKey(remotePhoto?.remoteId)
                                        .build(),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    contentDescription = "photo",
                                    loading = {
                                        LoadAnimation()
                                    },
                                    error = {
                                        Icon(
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            imageVector = Icons.Rounded.CloudOff,
                                            contentDescription = "load error",
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(20.dp)
                                                .padding(16.dp)
                                        )
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
        selectedIndex?.let {
            PhotoPageView(initialPage = it, photos = remotePhotos.itemSnapshotList.items) {
                selectedIndex = null
            }
        }
    }
}