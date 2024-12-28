package com.takaapoo.weatherer.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.takaapoo.weatherer.data.AddLocation
import com.takaapoo.weatherer.data.HomePane
import com.takaapoo.weatherer.data.Radar
import com.takaapoo.weatherer.data.Settings
import com.takaapoo.weatherer.ui.screens.add_location.AddLocationScreen
import com.takaapoo.weatherer.ui.screens.home.HomeItemDetailPane
import com.takaapoo.weatherer.ui.screens.radar.RadarScreen
import com.takaapoo.weatherer.ui.screens.settings.SettingsScreen
import com.takaapoo.weatherer.ui.utility.KeepScreenOn
import com.takaapoo.weatherer.ui.viewModels.AddLocationViewModel
import com.takaapoo.weatherer.ui.viewModels.DetailViewModel
import com.takaapoo.weatherer.ui.viewModels.HomeViewModel
import com.takaapoo.weatherer.ui.viewModels.PreferenceViewModel

//@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WeatherNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    addLocationViewModel: AddLocationViewModel,
    preferenceViewModel: PreferenceViewModel /*= hiltViewModel()*/,
    modifier: Modifier = Modifier,
    singlePane: Boolean,
    permanentDrawer: Boolean,
    onMenuButtonClick: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val appSettings by preferenceViewModel.appSettings.collectAsStateWithLifecycle()
    val mapState by addLocationViewModel.mapState.collectAsStateWithLifecycle()
    KeepScreenOn(enabled = appSettings.screenOn)
//    SharedTransitionLayout {

    NavHost(
        navController = navController,
        startDestination = HomePane,
        modifier = modifier
    ) {
        composable<HomePane> {
//            val homeViewModel = hiltViewModel<HomeViewModel>()
            val detailViewModel = hiltViewModel<DetailViewModel>()
            val chartStateList by detailViewModel.hourlyChartState.collectAsStateWithLifecycle()
            val dailyChartStateList by detailViewModel.dailyChartState.collectAsStateWithLifecycle()
            val hourlyChartsData by detailViewModel.locationsHourlyChartData.collectAsStateWithLifecycle()
            val dailyChartsData by detailViewModel.locationsDailyChartData.collectAsStateWithLifecycle()
            val locationsCount by homeViewModel.locationsCount.collectAsStateWithLifecycle()

            HomeItemDetailPane(
                modifier = Modifier.fillMaxSize(),
                homeViewModel = homeViewModel,
                detailViewModel = detailViewModel,
                chartStateList = chartStateList,
                dailyChartStateList = dailyChartStateList,
                hourlyChartsData = hourlyChartsData,
                dailyChartsData = dailyChartsData,
                singlePane = singlePane,
                locationsCount = locationsCount,
                permanentDrawer = permanentDrawer,
                appSettings = appSettings,
                weatherDataLoadStatus = mapState.weatherDataLoadStatus,
                onMenuButtonClick = onMenuButtonClick,
                onNavigateToAddLocationScreen = {
                    navController.navigate(route = AddLocation)
                },
                onReloadALocationWeatherData = { location ->
                    addLocationViewModel.loadALocationWeatherData(location)
                }
            )
        }
//        composable<Home> { backStackEntry ->
//            val pageNumberReturningFromDetail: Int? = backStackEntry.savedStateHandle[LAST_PAGE_NUMBER]
//            HomeScreen(
//                appSettings = appSettings,
//                initialFirstItemIndex = pageNumberReturningFromDetail,
//                onMenuButtonClick = onMenuButtonClick,
//                onNavigateToDetailScreen = { roomWidth, locationsCount, initialPageNumber ->
//                    navController.navigate(route = Detail(roomWidth, locationsCount, initialPageNumber))
//                },
//                onNavigateToAddLocationScreen = {
//                    navController.navigate(route = AddLocation)
//                }
//            )
//        }
        composable<AddLocation> {
            AddLocationScreen(
                mapState = mapState,
                onUpdateLocationPermissionGranted = addLocationViewModel::updateLocationPermissionGranted,
                onUpdateDeviceLocationEnabled = addLocationViewModel::updateDeviceLocationEnabled,
                onUpdateTriggerMapRelocation = addLocationViewModel::updateTriggerMapRelocation,
                onShowIndicatingCircle = addLocationViewModel::showIndicatingCircle,
                onResetAddResult = addLocationViewModel::resetAddResult,
                onUpdateShowLocationState = addLocationViewModel::updateShowLocationState,
                onGoToMyLocation = addLocationViewModel::goToMyLocation,
                onUpdateSearchQuery = addLocationViewModel::updateSearchQuery,
                onGoToLocation = addLocationViewModel::goToLocation,
                onUpdateSelectedLocationLatLng = addLocationViewModel::updateSelectedLocationLatLng,
                onUpdateAddLocationDialogVisibility = addLocationViewModel::updateAddLocationDialogVisibility,
                onAddLocationToDB = addLocationViewModel::addLocationToDB,
                onUpdateSelectedLocationName = addLocationViewModel::updateSelectedLocationName,
                onNavigateUp = navController::navigateUp
            )
        }
//        composable<Detail> { backStackEntry ->
//            val args = backStackEntry.toRoute<Detail>()
//            DetailScreen(
//                modifier = Modifier.fillMaxSize(),
//                appSettings = appSettings,
//                locationsCount = args.locationsCount,
//                initialPageNumber = args.initialPageNumber,
////                    sharedTransitionScope = this@SharedTransitionLayout,
////                    animatedContentScope = this@composable,
//                onNavigateUp = { pageNumber ->
//                    navController.previousBackStackEntry?.savedStateHandle?.set(LAST_PAGE_NUMBER, pageNumber)
//                    navController.navigateUp()
//                },
//                onNavigateToAQDetail = { pageNumber ->
//                    navController.navigate(route = AQDetail(pageNumber = pageNumber))
//                }
//            )
//        }
        /*composable<AQDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<AQDetail>()
            val detailViewModel: DetailViewModel = hiltViewModel()
            val pageNumber = args.pageNumber
            val detailState by detailViewModel.detailState.map { it[pageNumber] }
                .collectAsStateWithLifecycle(initialValue = DetailState())
            val location = detailViewModel.allLocations.getOrElse(
                index = pageNumber,
                defaultValue = { Location() }
            )
            AirQualityDetailScreen(
                detailState = detailState,
                location = location,
                appSettings = appSettings,
//                    sharedTransitionScope = this@SharedTransitionLayout,
//                    animatedContentScope = this@composable
                onNavigateUp = navController::navigateUp
            )
        }*/
        composable<Radar> {
            RadarScreen()
        }
        composable<Settings> {
            val settingsState by preferenceViewModel.settingsState.collectAsStateWithLifecycle()
            SettingsScreen(
                appSettings = appSettings,
                settingsState = settingsState,
                onUpdateSilence = preferenceViewModel::updateSilent,
                onUpdateScreenOn = preferenceViewModel::updateScreenOn,
                onUpdateThemeDialogVisibility = preferenceViewModel::updateThemeDialogVisibility,
                onUpdateTheme = preferenceViewModel::updateTheme,
                onUpdateTemperatureUnit = preferenceViewModel::updateTemperatureUnit,
                onUpdateLengthUnit = preferenceViewModel::updateLengthUnit,
                onUpdatePressureUnit = preferenceViewModel::updatePressureUnit,
                onUpdateSpeedUnit = preferenceViewModel::updateSpeedUnit,
                onUpdateClockGaugeVisibility = preferenceViewModel::updateClockGaugeVisibility,
                onNavigateUp = navController::navigateUp,
            )
        }
    }
//    }
}