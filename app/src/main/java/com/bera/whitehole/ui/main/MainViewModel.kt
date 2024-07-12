package com.bera.whitehole.ui.main

import android.Manifest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bera.whitehole.ui.main.nav.Screens

class MainViewModel : ViewModel() {
    var currentDestination by mutableStateOf<Screens>(Screens.Document)
        private set

    fun updateDestination(destination: Screens) {
        currentDestination = destination
    }

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if (!isGranted &&
            !visiblePermissionDialogQueue.contains(permission) &&
            permission != Manifest.permission.POST_NOTIFICATIONS
        ) {
            visiblePermissionDialogQueue.add(permission)
        }
    }
}