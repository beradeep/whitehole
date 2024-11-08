package com.bera.whitehole.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.ui.main.MainPage
import com.bera.whitehole.ui.main.MainViewModel
import com.bera.whitehole.ui.main.nav.screenScopedViewModel
import com.bera.whitehole.ui.onboarding.OnboardingPage
import com.bera.whitehole.ui.permission.PermissionDialogScreen
import com.bera.whitehole.ui.permission.PermissionViewModel
import com.bera.whitehole.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val isSdkAbove33 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    private var hasPhotosPerm by mutableStateOf(false)
    private var startDestination by mutableStateOf(ScreenFlow.Onboarding.route)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val topNavController = rememberNavController()
                NavHost(navController = topNavController, startDestination = startDestination) {
                    composable(ScreenFlow.Onboarding.route) {
                        OnboardingPage(onProceed = {
                            val navigateToRoute = if (hasPhotosPerm) {
                                ScreenFlow.Main.route
                            } else {
                                ScreenFlow.Permission.route
                            }
                            topNavController.navigate(navigateToRoute) {
                                popUpTo(ScreenFlow.Onboarding.route) { inclusive = true }
                            }
                        })
                    }
                    composable(ScreenFlow.Main.route) {
                        val viewModel: MainViewModel = screenScopedViewModel()
                        MainPage(viewModel)
                    }
                    dialog(ScreenFlow.Permission.route) {
                        val viewModel: PermissionViewModel = screenScopedViewModel()
                        val dialogQueue = viewModel.visiblePermissionDialogQueue

                        val permissionsToRequest = remember {
                            if (isSdkAbove33) {
                                arrayOf(
                                    Manifest.permission.READ_MEDIA_IMAGES,
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            } else {
                                arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                )
                            }
                        }

                        PermissionDialogScreen(
                            permissionsToRequest = permissionsToRequest,
                            onPermissionLauncherResult = { perms: Map<String, Boolean> ->
                                permissionsToRequest.forEach { permission ->
                                    viewModel.onPermissionResult(
                                        permission,
                                        isGranted = perms[permission] == true
                                    )
                                }
                                if (isSdkAbove33) {
                                    perms[Manifest.permission.READ_MEDIA_IMAGES]?.let { isGranted ->
                                        hasPhotosPerm = isGranted
                                    }
                                } else {
                                    perms[Manifest.permission.READ_EXTERNAL_STORAGE]?.let { isGranted ->
                                        hasPhotosPerm = isGranted
                                    }
                                }
                            },
                            dialogQueue = dialogQueue,
                            isPermanentyDeclined = { permission ->
                                !shouldShowRequestPermissionRationale(
                                    permission
                                )
                            },
                            onGoToAppSettingsClick = { openAppSettings() },
                            onDismissDialog = { viewModel.dismissDialog() },
                            onOkClick = { viewModel.dismissDialog() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hasPhotosPerm = if (isSdkAbove33) {
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        startDestination = when {
            Preferences.getEncryptedLong(Preferences.channelId, 0) == 0L -> ScreenFlow.Onboarding.route
            !hasPhotosPerm -> ScreenFlow.Permission.route
            else -> ScreenFlow.Main.route
        }
    }
}

sealed class ScreenFlow(val route: String) {
    data object Onboarding : ScreenFlow("onboard")
    data object Main : ScreenFlow("main")
    data object Permission : ScreenFlow("permission")
}

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}