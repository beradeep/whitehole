package com.bera.whitehole.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bera.whitehole.R
import com.bera.whitehole.api.BotApi
import com.bera.whitehole.connectivity.ConnectivityObserver
import com.bera.whitehole.connectivity.ConnectivityStatus
import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.ui.components.NoInternetScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Preview
@Composable
fun OnboardingPage(
    modifier: Modifier = Modifier,
    botApi: BotApi = BotApi,
    navController: NavController = rememberNavController()
) {

    val connectivityObserver = remember { ConnectivityObserver }
    val connection by connectivityObserver.observe()
        .collectAsState(initial = ConnectivityStatus.Unavailable)
    val isConnected = connection === ConnectivityStatus.Available

    var visibility by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = visibility, label = "onboarding_visibility",
    ) {
        if (it) {
            NoInternetScreen(isConnected = isConnected)
        } else {
            Onboarding(modifier = Modifier.fillMaxSize(), navController = navController)
        }
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
fun Onboarding(
    modifier: Modifier = Modifier, botApi: BotApi = BotApi,
    navController: NavController = rememberNavController()
) {
    val scope = rememberCoroutineScope()
    var inputToken by remember { mutableStateOf("") }
    var isValidToken by remember { mutableStateOf(true) }
    var showStepsDisclaimer by remember { mutableStateOf(true) }
    var showUidComponent by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Image(
                modifier = Modifier.clip(CircleShape),
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentScale = ContentScale.FillBounds,
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(80.dp))
            TextField(
                value = inputToken,
                onValueChange = { inputToken = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                isError = !isValidToken,
                supportingText = {
                    Column {
                        AnimatedContent(targetState = isValidToken, label = "SupportText") {
                            if (it) {
                                Text(
                                    text = "Recommended to only copy-paste to avoid error.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Text(
                                    text = "Token cannot be empty.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                },
                colors = TextFieldDefaults.colors().copy(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                label = { Text(text = "Bot Token") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodySmall,
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
                        if (inputToken.isNotBlank()) {
                            Preferences.edit {
                                putString(Preferences.botToken, inputToken)
                            }
                            showUidComponent = true
                        } else {
                            isValidToken = false
                        }
                    }
                }) {
                Text(text = "Proceed")
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        AnimatedVisibility(visible = showStepsDisclaimer) {
            AlertDialog(
                onDismissRequest = {},
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "app_icon"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showStepsDisclaimer = false }) {
                        Text(
                            text = "GOT IT",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                title = { Text(text = "Getting Started") },
                text = {
                    Column {
                        Text(text = "1. Visit BotFather on Telegram.")
                        Text(text = "2. Create a new bot.")
                        Text(text = "3. Get the bot token from BotFather.")
                        Text(text = "4. Paste the token here.")
                        Text(text = "5. Click on Proceed.")
                    }
                }
            )
        }
        if (showUidComponent) {
            UidComponent(
                onDismissRequest = {},
                onNavigate = {
                    navController.navigate("main") {
                        popUpTo("onboarding") {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}