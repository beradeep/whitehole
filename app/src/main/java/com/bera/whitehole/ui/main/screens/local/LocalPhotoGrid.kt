package com.bera.whitehole.ui.main.screens.local

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.compose.SubcomposeAsyncImage
import com.bera.whitehole.R
import com.bera.whitehole.data.localdb.entities.Photo
import com.bera.whitehole.ui.components.LoadAnimation
import com.bera.whitehole.ui.components.PhotoPageView
import com.bera.whitehole.ui.components.itemsPaging

@Composable
fun LocalPhotoGrid(localPhotos: LazyPagingItems<Photo>, totalCount: Int) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            Modifier
                .height(40.dp)
                .padding(horizontal = 8.dp),
            Arrangement.spacedBy(8.dp),
            Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                text = "$totalCount photos",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic
            )
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (localPhotos.loadState.refresh == LoadState.Loading) {
                LoadAnimation(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsPaging(
                        items = localPhotos,
                        { it.localId }
                    ) { localPhoto, index ->
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
                                model = localPhoto?.pathUri,
                                contentDescription = stringResource(id = R.string.photo),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                error = {
                                    Icon(
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        imageVector = Icons.Rounded.BrokenImage,
                                        contentDescription = stringResource(
                                            id = R.string.load_error
                                        ),
                                        modifier = Modifier
                                            .size(20.dp)
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
                    photos = localPhotos.itemSnapshotList.items
                ) {
                    selectedIndex = null
                }
            }
        }
    }
}