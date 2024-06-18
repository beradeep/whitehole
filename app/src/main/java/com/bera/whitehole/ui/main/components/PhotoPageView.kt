package com.bera.whitehole.ui.main.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bera.whitehole.data.models.PhotoModel

@Composable
fun PhotoPageView(
    initialPage: Int,
    photos: List<PhotoModel>,
    onDismissRequest: () -> Unit
) {
    val showUiState = remember { mutableStateOf(true) }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        initialPageOffsetFraction = 0f
    ) {
        photos.size
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = remember { DialogProperties(usePlatformDefaultWidth = false) }
    ) {
        HorizontalPager(modifier = Modifier.fillMaxSize(), state = pagerState) {
            PhotoView(
                photo = photos[it],
                showUiState = { showUiState })
        }
    }
}