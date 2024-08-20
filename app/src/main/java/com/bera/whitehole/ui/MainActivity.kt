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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bera.whitehole.R
import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.ui.main.MainPage
import com.bera.whitehole.ui.main.MainViewModel
import com.bera.whitehole.ui.main.nav.screenScopedViewModel
import com.bera.whitehole.ui.onboarding.OnboardingPage
import com.bera.whitehole.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val isSdkAbove33 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    private val permissionsToRequest = if (isSdkAbove33) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    private var hasPhotosPerm by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        val startDestination =
            if (Preferences.getEncryptedLong(
                    Preferences.channelId,
                    0L
                ) == 0L
            ) {
                ScreenFlow.Onboarding.route
            } else {
                ScreenFlow.Main.route
            }
        setContent {
            AppTheme {
                val context = LocalContext.current
                val topNavController = rememberNavController()
                NavHost(navController = topNavController, startDestination = startDestination) {
                    composable(ScreenFlow.Onboarding.route) {
                        OnboardingPage(navController = topNavController)
                    }
                    composable(ScreenFlow.Main.route) {
                        val viewModel: MainViewModel = screenScopedViewModel()
                        val dialogQueue = viewModel.visiblePermissionDialogQueue

                        val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestMultiplePermissions(),
                            onResult = { perms ->
                                permissionsToRequest.forEach { permission ->
                                    viewModel.onPermissionResult(
                                        permission = permission,
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
                            }
                        )
                        AnimatedContent(
                            targetState = !hasPhotosPerm,
                            label = "PermissionsPage"
                        ) { showPerms ->
                            if (showPerms) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.permissions_required),
                                        style = MaterialTheme.typography.headlineLarge
                                    )
                                    Spacer(modifier = Modifier.size(40.dp))
                                    Text(
                                        text = stringResource(R.string.this_app_requires_permission_to_photos_and_notifications) +
                                            stringResource(R.string.to_work_as_intended_please_grant_the_permissions_to_continue)
                                    )
                                    Spacer(modifier = Modifier.size(40.dp))
                                    Button(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        onClick = {
                                            multiplePermissionResultLauncher.launch(
                                                permissionsToRequest
                                            )
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.grant_permissions),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }

                                dialogQueue
                                    .reversed()
                                    .forEach { permission ->
                                        PermissionDialog(
                                            permissionTextProvider = when (permission) {
                                                Manifest.permission.READ_MEDIA_IMAGES -> {
                                                    PhotosPermissionTextProvider()
                                                }

                                                Manifest.permission.READ_EXTERNAL_STORAGE -> {
                                                    PhotosPermissionTextProvider()
                                                }

                                                else -> return@forEach
                                            },
                                            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                                                permission
                                            ),
                                            onDismiss = viewModel::dismissDialog,
                                            onOkClick = {
                                                viewModel.dismissDialog()
                                                multiplePermissionResultLauncher.launch(
                                                    arrayOf(permission)
                                                )
                                            },
                                            onGoToAppSettingsClick = ::openAppSettings
                                        )
                                    }
                            } else {
                                MainPage(viewModel)
                            }
                        }
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
    }
}

sealed class ScreenFlow(val route: String) {
    data object Onboarding : ScreenFlow("onboard")
    data object Main : ScreenFlow("main")
}

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}