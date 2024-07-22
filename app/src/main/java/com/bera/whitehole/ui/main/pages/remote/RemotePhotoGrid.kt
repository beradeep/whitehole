package com.bera.whitehole.ui.main.pages.remote

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.bera.whitehole.R
import com.bera.whitehole.data.localdb.entities.Photo
import com.bera.whitehole.data.localdb.entities.RemotePhoto
import com.bera.whitehole.ui.components.LoadAnimation
import com.bera.whitehole.ui.components.PhotoPageView
import com.bera.whitehole.ui.components.itemsPaging
import com.bera.whitehole.utils.coil.ImageLoaderModule

@Composable
fun RemotePhotoGrid(
    remotePhotosOnDevice: LazyPagingItems<Photo>,
    remotePhotosNotOnDevice: LazyPagingItems<RemotePhoto>,
    remotePhotosOnDeviceCount: Int,
    remotePhotosNotOnDeviceCount: Int,
) {
    var onDevice by remember { mutableStateOf(true) }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(8.dp))
            FilterChip(
                selected = onDevice,
                onClick = { onDevice = true },
                label = { Text(text = stringResource(R.string.on_device)) }
            )
            Spacer(modifier = Modifier.size(8.dp))
            FilterChip(
                selected = !onDevice,
                onClick = { onDevice = false },
                label = { Text(text = stringResource(R.string.not_on_device)) }
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(
                    R.string.photos,
                    if (onDevice) remotePhotosOnDeviceCount else remotePhotosNotOnDeviceCount
                ),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
        AnimatedContent(
            targetState = onDevice,
            label = stringResource(R.string.remote_photo_grid)
        ) { onDevice ->
            if (onDevice) {
                OnDeviceGrid(remotePhotosOnDevice)
            } else {
                NotOnDeviceGrid(remotePhotosNotOnDevice)
            }
        }
    }
}

@Composable
fun OnDeviceGrid(remotePhotosOnDevice: LazyPagingItems<Photo>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (remotePhotosOnDevice.loadState.refresh == LoadState.Loading) {
            LoadAnimation(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsPaging(
                    remotePhotosOnDevice
                ) { remotePhoto, index ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                selectedIndex = index
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = remotePhoto?.pathUri,
                            contentDescription = stringResource(id = R.string.photo),
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
                                    contentDescription = stringResource(id = R.string.photo),
                                    loading = {
                                        LoadAnimation()
                                    },
                                    error = {
                                        Icon(
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            imageVector = Icons.Rounded.CloudOff,
                                            contentDescription = stringResource(
                                                id = R.string.load_error
                                            ),
                                            modifier = Modifier
                                                .size(20.dp)
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
            PhotoPageView(
                initialPage = it,
                onlyRemotePhotos = false,
                photos = remotePhotosOnDevice.itemSnapshotList.items
            ) {
                selectedIndex = null
            }
        }
    }
}

@Composable
fun NotOnDeviceGrid(
    remotePhotosNotOnDevice: LazyPagingItems<RemotePhoto>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (remotePhotosNotOnDevice.loadState.refresh == LoadState.Loading) {
            LoadAnimation(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsPaging(
                    remotePhotosNotOnDevice
                ) { remotePhoto, index ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                selectedIndex = index
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            imageLoader = ImageLoaderModule.remoteImageLoader,
                            model = ImageRequest.Builder(context)
                                .data(remotePhoto)
                                .placeholderMemoryCacheKey(remotePhoto?.remoteId)
                                .memoryCacheKey(remotePhoto?.remoteId)
                                .build(),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            contentDescription = stringResource(id = R.string.photo),
                            loading = {
                                LoadAnimation()
                            },
                            error = {
                                Icon(
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    imageVector = Icons.Rounded.CloudOff,
                                    contentDescription = stringResource(id = R.string.load_error),
                                    modifier = Modifier
                                        .padding(50.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
        selectedIndex?.let { index ->
            PhotoPageView(
                initialPage = index,
                onlyRemotePhotos = true,
                photos = remotePhotosNotOnDevice.itemSnapshotList.items.map { it.toPhoto() }
            ) {
                selectedIndex = null
            }
        }
    }
}