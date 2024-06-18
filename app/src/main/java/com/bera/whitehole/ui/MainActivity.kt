package com.bera.whitehole.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.ui.main.MainPage
import com.bera.whitehole.ui.main.MainViewModel
import com.bera.whitehole.ui.main.nav.screenScopedViewModel
import com.bera.whitehole.ui.onboarding.OnboardingPage
import com.bera.whitehole.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private var hasStoragePerm: Boolean = false
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { _ ->
            hasStoragePerm = Environment.isExternalStorageManager()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasStoragePerm = Environment.isExternalStorageManager()
        val startDestination =
            if (Preferences.getLong(Preferences.channelId, 0L) == 0L) "onboard" else "main"
        setContent {
            AppTheme {
                val context = LocalContext.current
                var hasNotificationPerm by remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mutableStateOf(
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        )
                    } else mutableStateOf(true)
                }
                val permissionLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { isGranted ->
                            hasNotificationPerm = isGranted
                        }
                    )
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                val topNavController = rememberNavController()
                NavHost(navController = topNavController, startDestination = startDestination) {
                    composable("onboard") {
                        OnboardingPage(navController = topNavController)
                    }
                    composable("main") {
                        val viewModel: MainViewModel = screenScopedViewModel()
                        MainPage(viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!hasStoragePerm) {
            val intent = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.fromParts("package", packageName, null)
            )
            val pendingIntent = PendingIntent.getActivity(
                this@MainActivity, 0, intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent).build()
            activityResultLauncher.launch(intentSenderRequest)
        }
    }
}