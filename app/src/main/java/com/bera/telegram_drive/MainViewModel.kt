package com.bera.telegram_drive

import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    fun onPermissionResult(permission: String, isGranted: Boolean) {
        if (!isGranted) {

        }
    }
}