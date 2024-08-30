package com.bera.whitehole.ui.main.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(val displayTitle: String, val route: String, val icon: ImageVector? = null) {
    data object RemotePhotos :
        Screens(displayTitle = "Cloud", route = "cloud", icon = Icons.Default.Cloud)

    data object LocalPhotos :
        Screens(displayTitle = "Device", route = "device", icon = Icons.Default.Smartphone)

    data object Settings :
        Screens(displayTitle = "Settings", route = "settings", icon = Icons.Default.Settings)

    data object About : Screens(displayTitle = "About", route = "about", icon = Icons.Default.Info)

    companion object {
        val drawerScreens by lazy { listOf(LocalPhotos, RemotePhotos, Settings, About) }
    }
}