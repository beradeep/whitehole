package com.bera.whitehole.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkInfo
import com.bera.whitehole.R
import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.ui.components.ConnectivityStatusPopup
import com.bera.whitehole.ui.main.nav.AppNavHost
import com.bera.whitehole.ui.main.nav.NavDrawer
import com.bera.whitehole.ui.main.nav.Screens
import com.bera.whitehole.ui.main.nav.screenScopedViewModel
import com.bera.whitehole.workers.WorkModule
import com.bera.whitehole.workers.WorkModule.SYNC_MEDIA_STORE_WORK
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(viewModel: MainViewModel = screenScopedViewModel()) {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        val initialPage = if (viewModel.currentDestination in Screens.drawerScreens) {
            val lastSelectedTab = Preferences.getString(Preferences.startTabKey, "")
            Screens.drawerScreens.firstOrNull { it.route == lastSelectedTab }
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

    LaunchedEffect(viewModel) {
        WorkModule.SyncMediaStore.enqueueInstant()
        scope.launch {
            WorkModule.observeWorkerByName(SYNC_MEDIA_STORE_WORK)
                .collectLatest {
                    it.firstOrNull()?.let { workInfo ->
                        when (workInfo.state) {
                            WorkInfo.State.RUNNING -> viewModel.updateSyncState(SyncState.SYNCING)
                            WorkInfo.State.SUCCEEDED -> {
                                viewModel.updateSyncState(SyncState.IDLE)
                                WorkModule.SyncMediaStore.enqueuePeriodic()
                            }

                            else -> viewModel.updateSyncState(SyncState.IDLE)
                        }
                    }
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
            pages = Screens.drawerScreens
        ) {
            Scaffold(
                topBar = {
                    Column {
                        TopAppBar(
                            title = {
                                Text(
                                    text = viewModel.currentDestination.displayTitle,
                                    color = MaterialTheme.colorScheme.secondary
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
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = stringResource(R.string.menu_icon),
                                        tint = MaterialTheme.colorScheme.secondary
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
        AnimatedVisibility(visible = syncState == SyncState.SYNCING) {
            Dialog({}) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primaryContainer, strokeCap = StrokeCap.Square)
                    Spacer(Modifier.size(16.dp))
                    Text(
                        text = stringResource(R.string.syncing_your_photos),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}
