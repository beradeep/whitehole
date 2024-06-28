package com.bera.whitehole.ui.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.work.WorkInfo
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.data.models.PhotoModel
import com.bera.whitehole.ui.main.pages.local.UploadState
import com.bera.whitehole.utils.coil.ImageLoaderModule
import com.bera.whitehole.workers.WorkModule
import com.posthog.PostHog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun PhotoView(
    photo: PhotoModel,
    showUiState: () -> MutableState<Boolean>,
) {
    val context = LocalContext.current
    var showUi by showUiState()
    val scope = rememberCoroutineScope()
    val isLocal = rememberSaveable { photo.localId != null }
    var photoUploadState by rememberSaveable { mutableStateOf(UploadState.NOT_UPLOADED) }
    LaunchedEffect(key1 = Unit) {
        if (isLocal) {
            scope.launch(Dispatchers.IO) {
                photoUploadState = UploadState.CHECKING
                val isUploaded = DbHolder.database.photoDao().isUploaded(photo.localId!!)
                photoUploadState = if (isUploaded == 0) {
                    UploadState.NOT_UPLOADED
                } else {
                    UploadState.UPLOADED
                }
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {
                    showUi = !showUi
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val zoomState = rememberZoomState()

        val alpha by animateFloatAsState(
            targetValue = if (showUi) 0.5f else 0f,
            label = "backgroundAlpha",
            animationSpec = tween(500)
        )
        AsyncImage(
            imageLoader = ImageLoaderModule.defaultImageLoader,
            model = photo.pathUri,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp)
                .alpha(alpha)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zoomArea(zoomState),
            contentAlignment = Alignment.Center
        ) {
            if (isLocal) {
                AsyncImage(
                    imageLoader = ImageLoaderModule.defaultImageLoader,
                    model = photo.pathUri,
                    contentDescription = "photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .zoomImage(zoomState),
                    contentScale = ContentScale.Fit,
                )
            } else {
                SubcomposeAsyncImage(
                    imageLoader = ImageLoaderModule.defaultImageLoader,
                    model = photo.pathUri,
                    contentDescription = "photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .zoomImage(zoomState),
                    error = {
                        SubcomposeAsyncImage(
                            imageLoader = ImageLoaderModule.remoteImageLoader,
                            model = ImageRequest.Builder(context)
                                .data(photo)
                                .placeholderMemoryCacheKey(photo.remoteId)
                                .memoryCacheKey(photo.remoteId)
                                .build(),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .zoomImage(zoomState),
                            contentDescription = "photo",
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .aspectRatio(1f)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    LoadAnimation()
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .aspectRatio(1f)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        imageVector = Icons.Rounded.CloudOff,
                                        contentDescription = "error",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(16.dp)
                                    )
                                }
                            }
                        )
                    }
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = showUi && isLocal
                ) {
                    FloatingBottomBar(
                        modifier = Modifier
                            .padding(bottom = 30.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        uploadState = photoUploadState,
                        onClickUpload = {
                            PostHog.capture("upload-single")
                            WorkModule.instantUpload(photo.pathUri.toUri())
                            scope.launch(Dispatchers.IO) {
                                WorkModule.observeInstantWorkerById(photo.localId!!)
                                    .collectLatest {
                                        it.first().let { workInfo ->
                                            photoUploadState = when (workInfo.state) {
                                                WorkInfo.State.ENQUEUED -> UploadState.UPLOADING
                                                WorkInfo.State.RUNNING -> UploadState.UPLOADING
                                                WorkInfo.State.SUCCEEDED -> UploadState.UPLOADED
                                                WorkInfo.State.FAILED -> UploadState.FAILED
                                                WorkInfo.State.BLOCKED -> UploadState.FAILED
                                                WorkInfo.State.CANCELLED -> UploadState.FAILED
                                            }
                                        }
                                    }
                            }
                        }
                    )
                }
            }
        }
    }
}