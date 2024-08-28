package com.takaapoo.weatherer.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.takaapoo.weatherer.data.Screens
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.ui.screens.add_location.AddLocationScreen
import com.takaapoo.weatherer.ui.screens.detail.DetailScreen
import com.takaapoo.weatherer.ui.screens.detail.LAST_PAGE_NUMBER
import com.takaapoo.weatherer.ui.screens.detail.aq_detail.AirQualityDetailScreen
import com.takaapoo.weatherer.ui.screens.home.HomeScreen
import com.takaapoo.weatherer.ui.screens.radar.RadarScreen
import com.takaapoo.weatherer.ui.viewModels.AddLocationViewModel
import com.takaapoo.weatherer.ui.viewModels.DetailViewModel
import com.takaapoo.weatherer.ui.viewModels.PreferenceViewModel
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WeatherNavGraph(
    navController: NavHostController,
    addLocationViewModel: AddLocationViewModel,
    preferenceViewModel: PreferenceViewModel,
    modifier: Modifier = Modifier,
    onMenuButtonClick: () -> Unit
) {
    val appSettings by preferenceViewModel.appSettings.collectAsStateWithLifecycle()
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Screens.HOME.name,
            modifier = modifier
        ) {
            composable(route = Screens.HOME.name) {
                val pageNumberReturningFromDetail: Int = it.savedStateHandle[LAST_PAGE_NUMBER] ?: 0
                HomeScreen(
                    navController = navController,
                    appSettings = appSettings,
                    initialFirstItemIndex = pageNumberReturningFromDetail,
                    onMenuButtonClick = onMenuButtonClick
                )
            }
            composable(route = Screens.ADDLOCATION.name) {
                AddLocationScreen(
                    addViewModel = addLocationViewModel,
                    navController = navController
                )
            }
            composable(
                route = "${Screens.DETAIL.name}/{roomWidth}/{locationsCount}/{initialPageNumber}",
                arguments = listOf(
                    navArgument(name = "roomWidth") { type = NavType.FloatType },
                    navArgument(name = "locationsCount") { type = NavType.IntType },
                    navArgument(name = "initialPageNumber") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                DetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    appSettings = appSettings,
                    roomWidth = backStackEntry.arguments?.getFloat("roomWidth"),
                    locationsCount = backStackEntry.arguments?.getInt("locationsCount") ?: 0,
                    initialPageNumber = backStackEntry.arguments?.getInt("initialPageNumber") ?: 0,
                    navController = navController,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
                )
            }
            composable(
                route = "${Screens.AQDETAIL.name}/{pageNumber}/{locationsCount}",
                arguments = listOf(
                    navArgument(name = "pageNumber") { type = NavType.IntType },
                    navArgument(name = "locationsCount") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val detailViewModel: DetailViewModel = hiltViewModel()
                val pageNumber = backStackEntry.arguments?.getInt("pageNumber") ?: 0
                val detailState by detailViewModel.detailState.map { it[pageNumber] }
                    .collectAsStateWithLifecycle(initialValue = DetailState())
                val location = detailViewModel.allLocations.getOrElse(
                    index = pageNumber,
                    defaultValue = { Location() }
                )
                AirQualityDetailScreen(
                    navController = navController,
                    detailState = detailState,
                    location = location,
                    appSettings = appSettings,
//                    sharedTransitionScope = this@SharedTransitionLayout,
//                    animatedContentScope = this@composable
                )
            }
            composable(route = Screens.RADAR.name) {
                RadarScreen(
                    navController = navController
                )
            }
        }
    }
}