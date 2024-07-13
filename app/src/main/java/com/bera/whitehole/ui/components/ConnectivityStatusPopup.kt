package com.bera.whitehole.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bera.whitehole.connectivity.ConnectivityObserver
import com.bera.whitehole.connectivity.ConnectivityStatus
import kotlinx.coroutines.delay

@Composable
fun ConnectivityStatusPopup() {
    val connectivityObserver = remember { ConnectivityObserver }
    val connection by connectivityObserver.observe()
        .collectAsState(initial = ConnectivityStatus.Unavailable)
    val isConnected = connection === ConnectivityStatus.Available

    var visibility by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = visibility,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        ConnectivityStatusBox(isConnected = isConnected)
    }

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            delay(500)
            visibility = true
        } else {
            delay(2000)
            visibility = false
        }
    }
}

@Composable
fun ConnectivityStatusBox(isConnected: Boolean) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isConnected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.error,
        label = "connectivity status background color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isConnected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onError,
        label = "connectivity status background color"
    )
    val message = if (isConnected) "Back Online!" else "No Internet Connection!"
    val iconVector = if (isConnected) {
        Icons.Rounded.CloudDone
    } else {
        Icons.Rounded.CloudOff
    }

    Box(
        modifier = Modifier
            .background(backgroundColor)
            .fillMaxWidth()
            .height(60.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = iconVector, "Connectivity Icon", tint = contentColor)
            Spacer(modifier = Modifier.size(8.dp))
            Text(message, color = contentColor, fontSize = 15.sp)
        }
    }
}