package com.takaapoo.weatherer.ui.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextMeasurer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.data.AddLocation
import com.takaapoo.weatherer.data.Donate
import com.takaapoo.weatherer.data.HomePane
import com.takaapoo.weatherer.data.Radar
import com.takaapoo.weatherer.data.Settings
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DonatePaneContent
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.ui.screens.add_location.AddLocationScreen
import com.takaapoo.weatherer.ui.screens.donate.DonateScreen
import com.takaapoo.weatherer.ui.screens.home.HomeItemDetailPane
import com.takaapoo.weatherer.ui.screens.radar.RadarScreen
import com.takaapoo.weatherer.ui.screens.settings.SettingsScreen
import com.takaapoo.weatherer.ui.viewModels.AddLocationViewModel
import com.takaapoo.weatherer.ui.viewModels.AppViewModel
import com.takaapoo.weatherer.ui.viewModels.DetailViewModel
import com.takaapoo.weatherer.ui.viewModels.DonateViewModel
import com.takaapoo.weatherer.ui.viewModels.HomeViewModel
import com.takaapoo.weatherer.ui.viewModels.PreferenceViewModel
import com.takaapoo.weatherer.utility.KeepScreenOn
import com.takaapoo.weatherer.utility.isDestinationInBackStack
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow

const val FAB_EXPLODE_KEY = "fab_explode_key"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WeatherNavGraph(
    navController: NavHostController,
    appViewModel: AppViewModel,
    homeViewModel: HomeViewModel,
    addLocationViewModel: AddLocationViewModel,
    preferenceViewModel: PreferenceViewModel /*= hiltViewModel()*/,
    donateViewModel: DonateViewModel,
    appSettings: AppSettings,
//    appUiState: AppUiState,
    modifier: Modifier = Modifier,
    singlePane: Boolean,
    permanentDrawer: Boolean,
    drawerOpen: Boolean,
    onMenuButtonClick: () -> Unit = {}
) {
//    val lifecycleOwner = LocalLifecycleOwner.current
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
                val chartStateList = detailViewModel.hourlyChartState.map {
                    it.collectAsStateWithLifecycle().value
                }
                val dailyChartStateList = detailViewModel.dailyChartState.map {
                    it.collectAsStateWithLifecycle().value
                }
                val hourlyChartsData = detailViewModel.locationsHourlyChartData.map {
                    it.collectAsStateWithLifecycle().value
                }
                val dailyChartsData = detailViewModel.locationsDailyChartData.map {
                    it.collectAsStateWithLifecycle().value
                }
                val historicalChartStateList = detailViewModel.historicalHourlyChartState.map {
                    it.collectAsStateWithLifecycle().value
                }
                val historicalHourlyChartData = detailViewModel.historicalHourlyChartData.map {
                    it.collectAsStateWithLifecycle().value
                }
                val historicalDailyChartStateList = detailViewModel.historicalDailyChartState.map {
                    it.collectAsStateWithLifecycle().value
                }
                val historicalDailyChartData = detailViewModel.historicalDailyChartData.map {
                    it.collectAsStateWithLifecycle().value
                }

                HomeItemDetailPane(
                    modifier = Modifier.fillMaxSize(),
                    homeViewModel = homeViewModel,
                    detailViewModel = detailViewModel,
                    chartStateList = chartStateList,
                    dailyChartStateList = dailyChartStateList,
                    hourlyChartsData = hourlyChartsData,
                    dailyChartsData = dailyChartsData,
                    historicalHourlyChartData = historicalHourlyChartData,
                    historicalDailyChartData = historicalDailyChartData,
                    historicalChartStateList = historicalChartStateList,
                    historicalDailyChartStateList = historicalDailyChartStateList,
//                    sharedTransitionScope = this@SharedTransitionLayout,
//                    animatedVisibilityScope = this,
                    singlePane = singlePane,
                    permanentDrawer = permanentDrawer,
                    drawerOpen = drawerOpen,
                    appSettings = appSettings,
                    weatherDataLoadStatus = mapState.weatherDataLoadStatus,
                    onMenuButtonClick = onMenuButtonClick,
                    onNavigateToAddLocationScreen = {
                        navController.navigate(route = AddLocation)
                    },
                    onReloadALocationWeatherData = { location ->
                        addLocationViewModel.loadALocationWeatherData(location)
                    },
                    onUpdateNotificationLocationId = preferenceViewModel::updateNotificationLocationId,
                    onUpdateNotificationVisibility = preferenceViewModel::updateNotificationVisibility,
                    onUpdateWidgetLocationId = preferenceViewModel::updateWidgetLocationId,
                    onNavigateToDonateScreen = {

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
                val locationsCount by addLocationViewModel.locationsCount.collectAsStateWithLifecycle()
                AddLocationScreen(
//                    modifier = Modifier.sharedBounds(
//                        sharedContentState = rememberSharedContentState(
//                            key = FAB_EXPLODE_KEY
//                        ),
//                        animatedVisibilityScope = this
//                    ),
                    mapState = mapState,
                    locationsCount = locationsCount,
                    appSettings = appSettings,
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
                    onNavigateUp = navController::navigateUp,
                    onUpdateDonateDialogVisibility = addLocationViewModel::updateDonateDialogVisibility,
                    onUpdateLocationCountDialogVisibility = addLocationViewModel::updateLocationCountDialogVisibility,
                    onUpdateAddLocationHelpVisibility = preferenceViewModel::updateAddLocationHelpVisibility,
                    onNavigateToDonateScreen = {
                        val screenAlreadyVisited = isDestinationInBackStack(navController, Donate)
                        if (screenAlreadyVisited)
                            navController.popBackStack(route = Donate, inclusive = false)
                        else
                            navController.navigate(route = Donate)
                    },
                    onShowPrecipitation =  addLocationViewModel::showPrecipitation
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
                val locationListState by homeViewModel.locationsState.collectAsStateWithLifecycle()
                val allLocations by homeViewModel.allLocations.collectAsStateWithLifecycle()

//                val parentEntry = remember(key1 = it) { navController.getBackStackEntry<HomePane>() }
                val detailViewModel = hiltViewModel<DetailViewModel>(
//                    viewModelStoreOwner = parentEntry
                )
                val chartStateList by detailViewModel.hourlyChartState.getOrElse(0) {
                    MutableStateFlow(HourlyChartState())
                }.collectAsStateWithLifecycle()
                val hourlyChartData by detailViewModel.locationsHourlyChartData.getOrElse(0) {
                    MutableStateFlow(emptyList())
                }.collectAsStateWithLifecycle()

                val notificationHourlyChartData by appViewModel.notificationHourlyChartData
                    .collectAsStateWithLifecycle()
                val notificationDailyChartData by appViewModel.notificationDailyChartData
                    .collectAsStateWithLifecycle()

                SettingsScreen(
                    modifier = Modifier.fillMaxSize(),
                    appSettings = appSettings,
                    settingsState = settingsState,
                    singlePane = singlePane,
                    itemState = locationListState[0],
                    hourlyChartData = hourlyChartData.toImmutableList(),
                    hourlyChartState = chartStateList,
                    notificationHourlyChartData = notificationHourlyChartData,
                    notificationDailyChartData = notificationDailyChartData,
                    allLocations = allLocations,
                    drawerOpen = drawerOpen,

                    onUpdateSilence = preferenceViewModel::updateSilent,
                    onUpdateVibration = preferenceViewModel::updateVibration,
                    onUpdateScreenOn = preferenceViewModel::updateScreenOn,
                    onUpdateThemeDialogVisibility = preferenceViewModel::updateThemeDialogVisibility,
                    onUpdateTheme = preferenceViewModel::updateTheme,
                    onUpdateChartTheme = detailViewModel::updateHourlyChartTheme,
                    onUpdateChartGrid = detailViewModel::updateHourlyChartGrid,
                    onUpdateDynamicTheme = preferenceViewModel::updateDynamicColor,
                    onUpdateTemperatureUnit = preferenceViewModel::updateTemperatureUnit,
                    onUpdateLengthUnit = preferenceViewModel::updateLengthUnit,
                    onUpdatePressureUnit = preferenceViewModel::updatePressureUnit,
                    onUpdateSpeedUnit = preferenceViewModel::updateSpeedUnit,
                    onUpdateTimeAxis = preferenceViewModel::updateTimeAxis,
                    onUpdateClockGaugeVisibility = preferenceViewModel::updateClockGaugeVisibility,
                    onUpdateNotificationVisibility = preferenceViewModel::updateNotificationVisibility,
                    onUpdatePaneContent = preferenceViewModel::updatePaneContent,
                    onUpdateDiagramThemeDialogVisibility = preferenceViewModel::updateDiagramThemeDialogVisibility,
                    onUpdateCardWeatherQuantities = preferenceViewModel::updateCardWeatherQuantities,
                    onUpdateHomeCardQuantityUnitsVisibility = preferenceViewModel::updateHomeCardQuantityUnitsVisibility,
                    onUpdateInitialXAxisBounds = detailViewModel::updateInitialXAxisBounds,
                    onAddYAxis = {
                            start: Float, end: Float, diagramHeight: Float,
                            textMeasurer: TextMeasurer, axisIndex: Int?,
                            curveAnimatorInitialValue: Float ->
                        detailViewModel.addYAxis(
                            start, end, 0, diagramHeight, textMeasurer, axisIndex,
                            curveAnimatorInitialValue, isHistoric = false
                        )
                    },
                    onMoveHourlyDiagramAxis = {
                        detailViewModel.moveHourlyDiagramAxis(it, 0)
                    },
                    onScaleHourlyDiagramAxis = { center, scaleX, scaleY ->
                        detailViewModel.scaleHourlyDiagramAxis(
                            center = center,
                            scaleX = scaleX,
                            scaleY = scaleY,
                            pageIndex = 0
                        )
                    },

                    onCalculateHorizontalBarSeparation = detailViewModel::calculateHorizontalBarSeparation,
                    onCalculateVerticalBarSeparation = detailViewModel::calculateVerticalBarSeparation,
                    onUpdateHourlyChartDotsOnCurveVisibility = detailViewModel::updateHourlyChartDotsOnCurveVisibility,
                    onUpdateHourlyChartCurveShadowVisibility = detailViewModel::updateHourlyChartCurveShadowVisibility,
                    onUpdateHourlyChartWeatherIconVisibility = detailViewModel::updateHourlyChartWeatherIconVisibility,
                    onUpdateHourlyChartSunRiseSetIconsVisibility = detailViewModel::updateHourlyChartSunRiseSetIconsVisibility,
                    onUpdateTimeAxisDialogVisibility = preferenceViewModel::updateTimeAxisDialogVisibility,
                    onUpdateGridDialogVisibility = preferenceViewModel::updateDiagramGridDialogVisibility,
                    onUpdateCurvesColor = detailViewModel::updateCurvesColor,
                    onUpdateWholeCurvesColor = detailViewModel::updateCurvesColor,
                    onSaveCurvesColor = detailViewModel::saveCurvesColor,
                    onUpdateCurvesSmoothness = detailViewModel::updateCurveSmoothness,
                    onSaveCurvesSmoothness = detailViewModel::saveCurveSmoothness,
                    onAddRemoveQuantity = { quantity, addRemove ->
                        if (addRemove)
                            detailViewModel.addChartQuantity(quantity, 0, isHistoric = false)
                        else
                            detailViewModel.removeChartQuantity(quantity, 0, isHistoric = false)
                    },
                    onUpdateNotificationCurrentQuantities =
                        preferenceViewModel::updateNotificationCurrentWeatherQuantities,

                    onUpdateNotificationHourlyDiagramIconsVisibility =
                        preferenceViewModel::updateNotificationHourlyDiagramIconsVisibility,
                    onUpdateNotificationHourlyQuantities =
                        preferenceViewModel::updateNotificationHourlyQuantities,
                    onUpdateNotificationCurveShadowVisibility =
                        preferenceViewModel::updateNotificationHourlyCurveShadowVisibility,
                    onSaveNotificationCurvesColor = preferenceViewModel::updateNotificationCurvesColor,
                    onUpdateWholeNotificationCurvesColor = preferenceViewModel::updateNotificationCurvesColor,
                    onUpdateNotificationTimeAxisDialogVisibility =
                        preferenceViewModel::updateNotificationTimeAxisDialogVisibility,
                    onUpdateNotificationTimeAxis = preferenceViewModel::updateNotificationTimeAxis,
                    onUpdateNotificationDailyWeatherIconsVisible =
                        preferenceViewModel::updateNotificationDailyWeatherIconsVisible,
                    onUpdateNotificationDailyQuantity = preferenceViewModel::updateNotificationDailyQuantity,
                    onUpdateDailyNotificationGridDialogVisibility =
                        preferenceViewModel::updateNotificationDiagramGridDialogVisibility,
                    onUpdateDailyNotificationChartGrid = preferenceViewModel::updateNotificationDailyChartGrid,
                    onUpdateLocationDialogVisibility =
                        preferenceViewModel::updateNotificationLocationDialogVisibility,
                    onUpdateNotificationWeatherIconVisibility =
                        preferenceViewModel::updateNotificationWeatherIconVisible,
                    onUpdateNotificationDateTimeDialogVisibility =
                        preferenceViewModel::updateNotificationDateTimeDialogVisibility,
                    onUpdateNotificationDateTime = preferenceViewModel::updateNotificationDateTime,
                    onUpdateNotificationLocationDialogVisibility =
                        preferenceViewModel::updateNotificationLocationDialogVisibility,
                    onUpdateNotificationLocationId = preferenceViewModel::updateNotificationLocationId,

                    onNavigateUp = navController::navigateUp,
                )
            }
            composable<Donate> {
//                val parentEntry = remember(key1 = it) { navController.getBackStackEntry<Donate>() }
//                val donateViewModel = hiltViewModel<DonateViewModel>(
//                    viewModelStoreOwner = parentEntry
//                )
                val donateState by donateViewModel.donateState.collectAsStateWithLifecycle()
                val activity = LocalActivity.current as MainActivity
                DonateScreen(
                    modifier = Modifier.fillMaxSize(),
                    singlePane = singlePane,
                    donateState = donateState,
                    billingConnectionState = donateViewModel.billingClient.connectionState,
                    drawerOpen = drawerOpen,

                    onNavigateUp = {
                        navController.navigateUp()
                        donateViewModel.updatePaneContent(DonatePaneContent.MOTIVATION)
                    },
                    onNavigateToPay = {
                        donateViewModel.updatePaneContent(DonatePaneContent.PAY)
                    },
                    onConnectRetry = {
                        donateViewModel.startBillingConnection()
                    },
                    onPurchaseProduct = { productDetails ->
                        donateViewModel.launchBillingFlow(
                            activity = activity,
                            productDetails = productDetails
                        )
                    }
                )
            }
        }
//    }
}