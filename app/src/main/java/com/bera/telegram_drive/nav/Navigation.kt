package com.bera.telegram_drive.nav

import androidx.compose.runtime.Composable
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigation(start: String) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.HomeScreen.fullRoute) {

    }
}