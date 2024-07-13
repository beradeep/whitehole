package com.bera.whitehole.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//@Preview
@Composable
fun NoInternetScreen(modifier: Modifier = Modifier, isConnected: Boolean) {
    var showSpinner by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                tint = MaterialTheme.colorScheme.primary,
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = "No Internet Connection",
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.size(20.dp))
            Text(
                text = "Oops, looks like there's no Internet connection. " +
                        "Please check your connection and try again.", textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(20.dp))
            OutlinedButton(onClick = {
                scope.launch {
                    showSpinner = true
                    delay(1000)
                    showSpinner = false
                }
            }) {
                Text(text = "Retry")
                AnimatedVisibility(visible = showSpinner || isConnected) {
                    Row {
                        Spacer(modifier = Modifier.width(10.dp))
                        CircularProgressIndicator(
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}