package com.bera.whitehole.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowCircleUp
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bera.whitehole.ui.main.pages.local.UploadState

@Composable
fun FloatingBottomBar(
    modifier: Modifier = Modifier,
    uploadState: UploadState,
    onClickUpload: () -> Unit,
    contentColor: Color,
) {
    Box(
        modifier = Modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (uploadState) {
                UploadState.CHECKING -> TextIcon(
                    imageVector = Icons.Rounded.CloudSync,
                    title = "Checking Backup Status",
                    color = contentColor,
                    isEnabled = false
                )

                UploadState.UPLOADING -> TextIcon(
                    imageVector = Icons.Rounded.ArrowCircleUp,
                    title = "Uploading",
                    color = contentColor,
                    isEnabled = false
                )

                UploadState.UPLOADED -> TextIcon(
                    imageVector = Icons.Rounded.CloudDone,
                    title = "Backed Up",
                    color = contentColor,
                    isEnabled = false
                )

                UploadState.NOT_UPLOADED -> TextIcon(
                    imageVector = Icons.Rounded.CloudUpload,
                    title = "Backup to Cloud",
                    color = contentColor,
                    isEnabled = true,
                    onItemClick = onClickUpload
                )

                UploadState.FAILED -> TextIcon(
                    imageVector = Icons.Rounded.CloudOff,
                    title = "Upload failed",
                    color = contentColor,
                    isEnabled = true,
                    onItemClick = onClickUpload
                )

                UploadState.ENQUEUED -> TextIcon(
                    imageVector = Icons.Rounded.Timer,
                    title = "Upload enqueued",
                    color = contentColor,
                    isEnabled = true,
                    onItemClick = onClickUpload
                )

                UploadState.BLOCKED -> TextIcon(
                    imageVector = Icons.Rounded.CloudOff,
                    title = "Upload blocked",
                    color = contentColor,
                    isEnabled = false
                )
            }
        }
    }
}