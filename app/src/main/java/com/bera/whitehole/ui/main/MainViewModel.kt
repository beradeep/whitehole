package com.bera.whitehole.ui.main

import androidx.compose.runtime.getValue
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
}