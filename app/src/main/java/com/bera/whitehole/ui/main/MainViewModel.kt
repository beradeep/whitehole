package com.bera.whitehole.ui.main

import android.Manifest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bera.whitehole.ui.main.nav.Screens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    var currentDestination by mutableStateOf<Screens>(Screens.LocalPhotos)
        private set

    fun updateDestination(destination: Screens) {
        currentDestination = destination
    }

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirst()
    }

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    fun updateSyncState(newState: SyncState) {
        _syncState.value = newState
    }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        if (!isGranted &&
            !visiblePermissionDialogQueue.contains(permission) &&
            permission != Manifest.permission.POST_NOTIFICATIONS
        ) {
            visiblePermissionDialogQueue.add(permission)
        }
    }
}