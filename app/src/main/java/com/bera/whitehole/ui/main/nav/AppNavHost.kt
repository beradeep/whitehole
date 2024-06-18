package com.bera.whitehole.ui.main.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.bera.whitehole.ui.main.pages.local.LocalPhotoGrid
import com.bera.whitehole.ui.main.pages.local.LocalViewModel
import com.bera.whitehole.ui.main.pages.remote.RemotePhotoGrid
import com.bera.whitehole.ui.main.pages.remote.RemoteViewModel
import com.bera.whitehole.ui.main.pages.settings.SettingsScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screens.LocalPhotos.route
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = Screens.LocalPhotos.route,
        ) {
            val viewModel: LocalViewModel = screenScopedViewModel()
            val localPhotos = viewModel.localPhotosFlow.collectAsLazyPagingItems()
            LocalPhotoGrid(localPhotos = localPhotos)
        }
        composable(
            route = Screens.RemotePhotos.route,
        ) {
            val viewModel: RemoteViewModel = screenScopedViewModel()
            val remotePhotos = viewModel.remotePhotosFlow.collectAsLazyPagingItems()
            RemotePhotoGrid(remotePhotos = remotePhotos)
        }
        composable(route = Screens.Settings.route) {
            SettingsScreen()
        }
    }
}

/**
 * Provides a [ViewModel] instance scoped the screen's life.
 * When the user navigates away from the screen all screen scoped
 * viewModels are destroyed.
 */
@Composable
inline fun <reified T : ViewModel> screenScopedViewModel(
    factory: ViewModelProvider.Factory? = null
): T {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    requireNotNull(viewModelStoreOwner) { "No ViewModelStoreOwner provided" }
    val viewModelProvider = factory?.let {
        ViewModelProvider(viewModelStoreOwner, it)
    } ?: ViewModelProvider(viewModelStoreOwner)
    return viewModelProvider[T::class.java]
}