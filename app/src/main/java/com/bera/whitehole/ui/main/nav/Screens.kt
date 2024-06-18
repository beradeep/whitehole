package com.bera.whitehole.ui.main.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(val displayTitle: String, val route: String, val icon: ImageVector? = null) {
    object Document : Screens(displayTitle = "Documents", route = "docs", icon = Icons.Default.Book)
    object RemotePhotos :
        Screens(displayTitle = "Cloud", route = "cloud", icon = Icons.Default.Cloud)

    object LocalPhotos :
        Screens(displayTitle = "Device", route = "device", icon = Icons.Default.Smartphone)

    object Settings :
        Screens(displayTitle = "Settings", route = "settings", icon = Icons.Default.Settings)

    object PhotoDetail : Screens(displayTitle = "Photo", route = "pic-detail")

    companion object {
        val drawerScreens by lazy { listOf(Document, RemotePhotos, LocalPhotos) }
        val screens by lazy { listOf(*drawerScreens.toTypedArray(), PhotoDetail) }
    }
}