package com.bera.whitehole.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bera.whitehole.api.BotApi
import com.bera.whitehole.data.localdb.Preferences
import com.github.kotlintelegrambot.entities.ChatId
import kotlinx.coroutines.launch

@Composable
fun UidComponent(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onNavigate: () -> Unit,
    botApi: BotApi = BotApi
) {
    var inputIdState by remember { mutableStateOf("") }
    var isValidInput by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = Unit) {
        botApi.create()
        botApi.startPolling()
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = remember { DialogProperties(usePlatformDefaultWidth = false) }
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            TextField(
                value = inputIdState,
                onValueChange = { inputIdState = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                isError = !isValidInput,
                supportingText = {
                    AnimatedVisibility(visible = !isValidInput, enter = slideInVertically()) {
                        Text(
                            text = "Invalid UID or token.",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                colors = TextFieldDefaults.colors().copy(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorSupportingTextColor = MaterialTheme.colorScheme.onError
                ),
                label = { Text(text = "Unique ID (/start in gc)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .width(300.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                onClick = {
                    scope.launch {
                        val id = inputIdState.toLongOrNull()
                        if (id != null) {
                            if (botApi.getChat(ChatId.fromId(id))) {
                                Preferences.edit {
                                    putLong(Preferences.channelId, id)
                                }
                                onNavigate()
                            } else {
                                isValidInput = false
                            }
                        } else {
                            isValidInput = false
                        }
                    }
                }
            ) {
                Text(text = "Proceed")
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}