package com.bera.whitehole.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.ui.components.ConnectivityStatusPopup
import com.bera.whitehole.ui.main.nav.AppNavHost
import com.bera.whitehole.ui.main.nav.NavDrawer
import com.bera.whitehole.ui.main.nav.Screens
import com.bera.whitehole.ui.main.nav.screenScopedViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    viewModel: MainViewModel = screenScopedViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    LaunchedEffect(viewModel) {
        val initialPage = if (viewModel.currentDestination in Screens.screens) {
            val lastSelectedTab = Preferences.getString(Preferences.startTabKey, "")
            Screens.screens.firstOrNull { it.route == lastSelectedTab }
        } else {
            viewModel.currentDestination
        }
        initialPage?.route?.runCatching { navController.navigate(this) }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (Screens.drawerScreens.any { it.route == destination.route }) {
                Preferences.edit { putString(Preferences.startTabKey, destination.route) }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        NavDrawer(
            drawerState = drawerState,
            navController = navController,
            viewModel = viewModel,
            pages = listOf(Screens.LocalPhotos, Screens.RemotePhotos, Screens.Settings)
        ) {
            Scaffold(
                topBar = {
                    Column {
                        TopAppBar(
                            title = {
                                Text(
                                    viewModel.currentDestination.displayTitle
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Menu,
                                        null
                                    )
                                }
                            }
                        )
                        ConnectivityStatusPopup()
                    }
                }
            ) {
                AppNavHost(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize(),
                    navController = navController
                )
            }
        }
    }
}
