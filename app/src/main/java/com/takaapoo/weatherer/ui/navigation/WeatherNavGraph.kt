package com.takaapoo.weatherer.ui.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextMeasurer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.domain.model.DonatePaneContent
import com.takaapoo.weatherer.domain.model.DonateState
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.domain.model.LocationsState
import com.takaapoo.weatherer.ui.screens.add_location.AddLocationScreen
import com.takaapoo.weatherer.ui.screens.detail.DetailScreen
import com.takaapoo.weatherer.ui.screens.detail.aq_detail.AirQualityDetailScreen
import com.takaapoo.weatherer.ui.screens.donate.DonateScreen
import com.takaapoo.weatherer.ui.screens.home.HomeScreen
import com.takaapoo.weatherer.ui.screens.home.HomeStartScreen
import com.takaapoo.weatherer.ui.screens.radar.RadarScreen
import com.takaapoo.weatherer.ui.screens.settings.GeneralSettingsScreen
import com.takaapoo.weatherer.ui.screens.settings.HomeCardWeatherQuantityScreen
import com.takaapoo.weatherer.ui.screens.settings.HourlyDiagramCurvesScreen
import com.takaapoo.weatherer.ui.screens.settings.MainSettingsScreen
import com.takaapoo.weatherer.ui.screens.settings.NotificationSettingsScreen
import com.takaapoo.weatherer.ui.screens.settings.UnitSettingsScreen
import com.takaapoo.weatherer.ui.viewModels.AddLocationViewModel
import com.takaapoo.weatherer.ui.viewModels.AppViewModel
import com.takaapoo.weatherer.ui.viewModels.DetailViewModel
import com.takaapoo.weatherer.ui.viewModels.DonateViewModel
import com.takaapoo.weatherer.ui.viewModels.HomeViewModel
import com.takaapoo.weatherer.ui.viewModels.PreferenceViewModel
import com.takaapoo.weatherer.utility.KeepScreenOn
import com.takaapoo.weatherer.utility.collectAsStateWithPermission
import com.takaapoo.weatherer.utility.toDp
import com.takaapoo.weatherer.utility.toPx
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow

const val FAB_EXPLODE_KEY = "fab_explode_key"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WeatherNavGraph(
//    navController: NavHostController,
    navigator: Navigator,
    navigationState: NavigationState,
    appViewModel: AppViewModel,
    homeViewModel: HomeViewModel,
    addLocationViewModel: AddLocationViewModel,
    preferenceViewModel: PreferenceViewModel /*= hiltViewModel()*/,
    donateViewModel: DonateViewModel,
    donateState: DonateState,
    appSettings: AppSettings,
    locationListState: ImmutableList<LocationsState>,
//    appUiState: AppUiState,
    modifier: Modifier = Modifier,
    singlePane: Boolean,
    permanentDrawer: Boolean,
    drawerOpen: Boolean,
    onMenuButtonClick: () -> Unit = {}
) {
    val mapState by addLocationViewModel.mapState.collectAsStateWithLifecycle()
    KeepScreenOn(enabled = appSettings.screenOn)
    val detailViewModel = hiltViewModel<DetailViewModel>()
    val dataReadyForDisplay by detailViewModel.dataReadyForDisplay.collectAsStateWithLifecycle()

    val detailState by detailViewModel.detailState.collectAsStateWithPermission(dataReadyForDisplay)
    val homePaneState by homeViewModel.homePaneState.collectAsStateWithLifecycle()
    val homeState by homeViewModel.homeState.collectAsStateWithLifecycle()

//    SharedTransitionLayout {
    val entryProvider = entryProvider {
        entry<Start> {
            HomeStartScreen(
                onNavigateToAddLocationScreen = {
                    navigator.navigate(route = AddLocation)
                }
            )
        }
        entry<Home>(
            metadata = ListDetailSceneStrategy.listPane()
        )
        {
            val density = LocalDensity.current
            val mainActivity = LocalActivity.current as MainActivity

            // temporary variable //
            val locations by homeViewModel.allLocations.collectAsStateWithLifecycle()

            var homeScreenWidth by rememberSaveable { mutableFloatStateOf(0f) }
            var homeScreenHeight by rememberSaveable { mutableFloatStateOf(0f) }

            val cardHeightPx = dimensionResource(R.dimen.home_card_max_height).toPx(density)
            val zoomMax = mainActivity.windowHeight / cardHeightPx
//            val width by remember { mutableStateOf(0.dp) }

            val transition = updateTransition(
                targetState = homeState.zoom,
                label = "Transition"
            )

            val transitionDuration = 1300
            val zoomEasing = CubicBezierEasing(0.7f, 0f, 0.3f, 1f)
            val translationEasing = if (homeState.zoom) CubicBezierEasing(0f, 0f, 0.3f, 1f)
            else CubicBezierEasing(1f, 0f, 0.7f, 1f)

            val translationTransitionSpec =
                tween<Float>(durationMillis = transitionDuration, easing = translationEasing)
            val zoom by transition.animateFloat(
                transitionSpec = { tween(durationMillis = transitionDuration, easing = zoomEasing) },
                label = "Zoom",
                targetValueByState = { state ->
                    if (state) zoomMax else 1f
                }
            )
            val itemTranslationX by transition.animateFloat(
                transitionSpec = { translationTransitionSpec },
                label = "TranslationX"
            ) { state ->
                if (state) (homeScreenWidth - homeState.cardWidth)/2 - homeState.selectedCardX
                else 0f
            }
            val itemTranslationY = transition.animateFloat(
                transitionSpec = { translationTransitionSpec },
                label = "TranslationY",
                targetValueByState = { state ->
                    if (state) (mainActivity.windowHeight - cardHeightPx)/2 - (homeState.selectedCardY /*+ listBoxTopPadding*/)
                    else -homeState.verticalOffsetDifference
                }
            )
            val windowRotation by transition.animateFloat(
                transitionSpec = { translationTransitionSpec },
                label = "windowRotation"
            ) { state -> if (state) 90f else 0f }

            LaunchedEffect(homeScreenWidth, dataReadyForDisplay) {
//                detailViewModel.reInitialize()
                if (dataReadyForDisplay) {
                    detailViewModel.updateInitialXAxisBounds(
                        detailScreenWidth = homeScreenWidth.toDp(density)
                    )
//                delay(1000)
                    detailViewModel.initializeXAxisBounds()
                }
            }

            HomeScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        homeScreenWidth = it.width.toFloat()
                        homeScreenHeight = it.height.toFloat()
                    },
                locations = locations,
                homeState = homeState,
                locationListState = locationListState,
                appSettings = appSettings,
                weatherDataLoadStatus = mapState.weatherDataLoadStatus,
                isZooming = transition.isRunning,
                homeScreenWidth = homeScreenWidth,
                homeScreenHeight = homeScreenHeight,
//                        listBoxTopPadding = listBoxTopPadding,
                zoom = zoom,
                zoomMax = zoomMax,
                itemTranslationX = itemTranslationX,
                itemTranslationY = itemTranslationY.value,
                windowRotation = windowRotation,
                initialFirstItemIndex = homePaneState.pageNumberReturningFromDetail,
                singlePane = singlePane,
                permanentDrawer = permanentDrawer,
//                            sharedTransitionScope = sharedTransitionScope,
//                            animatedVisibilityScope = animatedVisibilityScope,

                onUpdateClockGaugeRotation = remember {
                    {
                        homeViewModel.updateClockGaugeRotation(rotation = it)
                    }
                },
                onRefreshWeatherData = remember {
                    {
                        homeViewModel.refreshWeatherData()
                    }
                },
                onUpdateNavigatedToDetailScreen = remember {
                    {
                        homeViewModel.updateNavigatedToDetailScreen(navigated = it)
                    }
                },
                onUpdateVerticalOffsetDifference = remember {
                    {
                        homeViewModel.updateVerticalOffsetDifference(diff = it)
                    }
                },
                onResetEditNameResult = remember {
                    {
                        homeViewModel.resetEditNameResult()
                    }
                },
                onResetZoom = remember {
                    {
                        homeViewModel.resetZoom()
                    }
                },
                onBalanceClockGaugeValues = remember {
                    {
                        homeViewModel.balanceClockGaugeValues()
                    }
                },
                onResetDayGauge = remember {
                    {
                        homeViewModel.resetDayGauge()
                    }
                },
                onUpdateVisibleDayIndex = remember {
                    {
                        homeViewModel.updateVisibleDayIndex(newIndex = it)
                    }
                },
                onUpdateDayGaugeIndex = remember {
                    {
                        homeViewModel.updateDayGaugeIndex(newVisibleDay = it)
                    }
                },
                /*onHandleClickSound = remember {
                    { currentRotation, previousRotation ->
                        homeViewModel.handleClickSound(
                            currentRotation,
                            previousRotation
                        )
                    }
                },*/
                onUpdateDialogVisibility = remember {
                    { dialogType, visible ->
                        homeViewModel.updateDialogVisibility(dialogType, visible)
                    }
                },
                onUpdateToBeDeletedLocationId = remember {
                    {
                        homeViewModel.updateToBeDeletedLocationId()
                    }
                },
                onUpdateHomePaneContent = remember {
                    { content ->
                        homeViewModel.updateHomePaneContent(content)
                    }
                },
                onModifyInitialPage = remember {
                    {
                        homeViewModel.modifyInitialPage(deletedPage = it)
                    }
                },
                onUpdateLocation = remember {
                    { id, newName ->
                        homeViewModel.updateLocation(id, newName)
                    }
                },
                onUpdateEditDialogLocationName = remember {
                    {
                        homeViewModel.updateEditDialogLocationName(locationName = it)
                    }
                },
                onUpdateSelectedItemIndexOffset = remember {
                    { selectedItemIndex, selectedItemOffset ->
                        homeViewModel.updateSelectedItemIndexOffset(
                            selectedItemIndex,
                            selectedItemOffset
                        )
                    }
                },
                onNavigateToItem = remember {
                    { locationId, cardX, cardY, cardWidth ->
                        homeViewModel.navigateToItem(
                            locationId,
                            cardX,
                            cardY,
                            cardWidth
                        )
                    }
                },
                onUpdateSelectedLocationId = remember {
                    {
                        homeViewModel.updateSelectedLocationId(locationId = it)
                    }
                },
                onUpdateDialogData = remember {
                    { dialogType, id, locationName ->
                        homeViewModel.updateDialogData(dialogType, id, locationName)
                    }
                },
                onDeleteLocation = remember {
                    {
                        homeViewModel.deleteLocation(it)
                    }
                },
                onUpdateFilterText = remember {
                    {
                        homeViewModel.updateFilterText(it)
                    }
                },

                onSwapLocations = { id1, id2 ->
                    homeViewModel.swapLocationCustomId(id1, id2)
                    detailViewModel.swapLocations(id1, id2)
                    if (!singlePane){
                        val currentPageLocationId = homeViewModel.locationsState.value
                            .getOrNull(homePaneState.initialPage)?.locationId

                        when (currentPageLocationId) {
                            id1 -> homeViewModel.updateInitialPage(
                                homeViewModel.locationsState.value.indexOfFirst {
                                    it.locationId == id2
                                }
                            )

                            id2 -> homeViewModel.updateInitialPage(
                                homeViewModel.locationsState.value.indexOfFirst {
                                    it.locationId == id1
                                }
                            )
                        }
                    }
                },
//                        onEvaluateTopPaddingHeight = {
//                            listBoxTopPadding = it
//                        },
                onMenuButtonClick = onMenuButtonClick,
                onNavigateToDetailScreen = { locationsCount: Int, initialPageNumber: Int ->
                    homeViewModel.updateLocationsCount(count = locationsCount)
                    homeViewModel.updateInitialPage(page = initialPageNumber)
                    detailViewModel.setSliderThumbPositions()
                    navigator.navigate(Detail)
                },
                onNavigateToAddLocationScreen = {
                    navigator.navigate(route = AddLocation)
                },
                onNavigateUp = navigator::goBack,
                onPrepareDeleteLocation = remember {
                    { locationId ->
                        detailViewModel.prepareDeleteLocation(locationId)
                    }
                },
                onReloadALocationWeatherData = { location ->
                    addLocationViewModel.loadALocationWeatherData(location)
                },
                onUpdateClockGaugeLock = remember { homeViewModel::updateClockGaugeLock },
                onUpdateItemDragged = remember { homeViewModel::updateItemDragged },
                onDownloadAndSaveLandscapes = remember {
                    { context, picId, picUrl, picName ->
                        homeViewModel.downloadAndSaveLandscapes(context, picId, picUrl, picName)
                    }
                },
                onUpdateInitialLandscape = remember { detailViewModel::updateInitialLandScape },
                onReInitialize = remember { detailViewModel::reInitialize },
                onAdjustSettings = remember { detailViewModel::adjustSettings }
            )
        }
        entry<Detail>(
            metadata = ListDetailSceneStrategy.detailPane() +
                    NavDisplay.transitionSpec {
                        EnterTransition.None togetherWith ExitTransition.KeepUntilTransitionsFinished
                    } +
                    NavDisplay.popTransitionSpec {
                        EnterTransition.None togetherWith ExitTransition.None
                    }
        )
        {
            val chartStateList = detailViewModel.hourlyChartState.map {
                it.collectAsStateWithPermission(dataReadyForDisplay).value
            }
            val dailyChartStateList = detailViewModel.dailyChartState.map {
                it.collectAsStateWithPermission(dataReadyForDisplay).value
            }
            val hourlyChartsData = detailViewModel.locationsHourlyChartData.map {
                it.collectAsStateWithPermission(dataReadyForDisplay).value
            }
//            Log.i("data1", "names = ${detailViewModel.locationsHourlyChartData.map{it.value.firstOrNull()?.locationName}}")
            val dailyChartsData = detailViewModel.locationsDailyChartData.map {
                it.collectAsStateWithPermission(dataReadyForDisplay).value
            }
            val historicalChartStateList = detailViewModel.historicalHourlyChartState.map {
                it.collectAsStateWithPermission(dataReadyForDisplay).value
            }
            val historicalHourlyChartData = detailViewModel.historicalHourlyChartData.map {
                it.collectAsStateWithPermission(dataReadyForDisplay).value
            }
            val historicalDailyChartStateList = detailViewModel.historicalDailyChartState.map {
                it.collectAsStateWithPermission(dataReadyForDisplay).value
            }
            val historicalDailyChartData = detailViewModel.historicalDailyChartData.map {
                it.collectAsStateWithPermission(dataReadyForDisplay).value
            }
            val pagerState = rememberPagerState(
                initialPage = homePaneState.initialPage,
                pageCount = { homePaneState.listItemCount }
            )
            LaunchedEffect(dataReadyForDisplay, homePaneState.initialPage) {
                if (dataReadyForDisplay) {
//                    Log.i("data1", "size = ${hourlyChartsData.size} , ${hourlyChartsData.lastOrNull()?.size}")
                    pagerState.requestScrollToPage(homePaneState.initialPage)
                }
            }

//            var currentPage by rememberSaveable {
//                mutableIntStateOf(0)
//            }

            DetailScreen(
                modifier = Modifier
                    .fillMaxSize(),
//                    .alpha(if (homePaneState.paneContent == HomePaneContent.WEATHERDETAIL) 1f else 0f),
                detailState = detailState,
                chartStateList = chartStateList,
                dailyChartStateList = dailyChartStateList,
                hourlyChartsData = hourlyChartsData,
                dailyChartsData = dailyChartsData,
                historicalHourlyChartData = historicalHourlyChartData,
                historicalDailyChartData = historicalDailyChartData,
                historicalChartStateList = historicalChartStateList,
                historicalDailyChartStateList = historicalDailyChartStateList,
                detailViewModel = detailViewModel,
                appSettings = appSettings,
                pagerState = pagerState,
                singlePane = singlePane,
//                dataReady = dataReadyForDisplay,
                onUpdateHomeSelectedItemIndex = { index: Int ->
                    homeViewModel.updateSelectedItemIndexOffset(index = index)
                },
                onNavigateUp = remember {
                    { pageNumber ->
                        homeViewModel.updatePageNumberReturningFromDetail(page = pageNumber)
                        navigator.goBack()
                    }
                },
                onNavigateToAQDetail = remember {
                    { pageNumber, scrollValue ->
//                        currentPage = pageNumber
                        navigator.navigate(AQDetail(pageNumber))
                    }
                },
                onNavigateToDonateScreen = {
                    navigator.navigate(route = Donate)
                },
            )

        }
        entry<AQDetail>(
            metadata = ListDetailSceneStrategy.detailPane()
        ) {
            val location = detailViewModel.allLocations.getOrElse(
                index = it.pageNumber,
                defaultValue = { Location() }
            )
            AirQualityDetailScreen(
                detailState = detailState.getOrElse(
                    index = it.pageNumber,
                    defaultValue = { DetailState() }
                ),
                location = location,
                appSettings = appSettings,
                drawerOpen = drawerOpen,
                onNavigateUp = navigator::goBack
            )
        }
        /*entry<HomePane> {
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
                    navigator.navigate(route = AddLocation)
                },
                onReloadALocationWeatherData = { location ->
                    addLocationViewModel.loadALocationWeatherData(location)
                },
                onUpdateNotificationLocationId = preferenceViewModel::updateNotificationLocationId,
                onUpdateNotificationVisibility = preferenceViewModel::updateNotificationVisibility,
                onUpdateWidgetLocationId = preferenceViewModel::updateWidgetLocationId,
                onNavigateToDonateScreen = {
                    navigator.navigate(route = Donate)
                }
            )
        }*/
        entry<AddLocation> {
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
                onNavigateUp = navigator::goToStartRoute,
                onUpdateDonateDialogVisibility = addLocationViewModel::updateDonateDialogVisibility,
                onUpdateLocationCountDialogVisibility = addLocationViewModel::updateLocationCountDialogVisibility,
                onUpdateAddLocationHelpVisibility = preferenceViewModel::updateAddLocationHelpVisibility,
                onNavigateToDonateScreen = {
                    navigator.navigate(Donate)
                },
                onShowPrecipitation = addLocationViewModel::showPrecipitation,
                onReInitialize = remember { detailViewModel::reInitialize }
            )
        }
        entry<Radar> {
//            val locationListState by homeViewModel.locationsState.collectAsStateWithLifecycle()
            RadarScreen()
        }
        entry<Settings>(
            metadata = ListDetailSceneStrategy.listPane()
        ) {
//            val settingsState by preferenceViewModel.settingsState.collectAsStateWithLifecycle()
//            val locationListState by homeViewModel.locationsState.collectAsStateWithLifecycle()
//            val allLocations by homeViewModel.allLocations.collectAsStateWithLifecycle()

//                val parentEntry = remember(key1 = it) { navController.getBackStackEntry<HomePane>() }
//            val detailViewModel = hiltViewModel<DetailViewModel>(
//                    viewModelStoreOwner = parentEntry
//            )
//            val chartStateList by detailViewModel.hourlyChartState.getOrElse(0) {
//                MutableStateFlow(HourlyChartState())
//            }.collectAsStateWithLifecycle()
//            val hourlyChartData by detailViewModel.locationsHourlyChartData.getOrElse(0) {
//                MutableStateFlow(emptyList())
//            }.collectAsStateWithLifecycle()
//
//            val notificationHourlyChartData by appViewModel.notificationHourlyChartData
//                .collectAsStateWithLifecycle()
//            val notificationDailyChartData by appViewModel.notificationDailyChartData
//                .collectAsStateWithLifecycle()

            MainSettingsScreen(
                modifier = modifier,
                appSettings = appSettings,
                onUpdateNotificationVisibility = preferenceViewModel::updateNotificationVisibility,
                onNavigateUp = navigator::goToStartRoute,
                onUpdatePaneContent = {_, _ -> },
                onNavigateToScreen = navigator::navigate
            )

            /*SettingsScreen(
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

                onNavigateUp = navigator::goBack,
            )*/
        }
        entry<SettingsGeneral>(
            metadata = ListDetailSceneStrategy.detailPane()
        )
        {
            val settingsState by preferenceViewModel.settingsState.collectAsStateWithLifecycle()
            GeneralSettingsScreen(
                modifier = modifier,
                appSettings = appSettings,
                singlePane = singlePane,
                settingsState = settingsState,
                onUpdateSilence = preferenceViewModel::updateSilent,
                onUpdateVibration = preferenceViewModel::updateVibration,
                onUpdateScreenOn = preferenceViewModel::updateScreenOn,
                onUpdateThemeDialogVisibility = preferenceViewModel::updateThemeDialogVisibility,
                onUpdateDynamicTheme = preferenceViewModel::updateDynamicColor,
                onUpdateClockGaugeVisibility = preferenceViewModel::updateClockGaugeVisibility,
                onUpdatePaneContent = {_, _ -> },
                onNavigateToScreen = navigator::navigate,
                onUpdateTheme = preferenceViewModel::updateTheme,
                onNavigateUp = navigator::goBack,
            )
        }
        entry<SettingsCard>(
            metadata = ListDetailSceneStrategy.detailPane()
        ) {
//            val locationListState by homeViewModel.locationsState.collectAsStateWithLifecycle()
            HomeCardWeatherQuantityScreen(
                modifier = modifier,
                itemState = locationListState[0],
                appSettings = appSettings,
                onUpdateHomeCardQuantityUnitsVisibility = preferenceViewModel::updateHomeCardQuantityUnitsVisibility,
                cardWeatherQuantities = appSettings.cardWeatherQuantities,
                onUpdateCardWeatherQuantities = preferenceViewModel::updateCardWeatherQuantities,
                onNavigateUp = navigator::goBack,
            )
        }
        entry<SettingsUnit>(
            metadata = ListDetailSceneStrategy.detailPane()
        ) {
            UnitSettingsScreen(
                modifier = modifier,
                appSettings = appSettings,
                singlePane = singlePane,
                onUpdateLengthUnit = preferenceViewModel::updateLengthUnit,
                onUpdateTemperatureUnit = preferenceViewModel::updateTemperatureUnit,
                onUpdatePressureUnit = preferenceViewModel::updatePressureUnit,
                onUpdateSpeedUnit = preferenceViewModel::updateSpeedUnit,
                onNavigateUp = navigator::goBack
            )
        }
        entry<SettingsHourlyDiagram>(
            metadata = ListDetailSceneStrategy.detailPane()
        ) {
            val hourlyChartData by detailViewModel.locationsHourlyChartData.getOrElse(0) {
                MutableStateFlow(emptyList())
            }.collectAsStateWithLifecycle()
            val firstHourlyChartState by detailViewModel.hourlyChartState.getOrElse(0) {
                MutableStateFlow( HourlyChartState())
            }.collectAsStateWithLifecycle()
            val settingsState by preferenceViewModel.settingsState.collectAsStateWithLifecycle()

            HourlyDiagramCurvesScreen(
                modifier = modifier,
                appSettings = appSettings,
                singlePane = singlePane,
                hourlyChartData = hourlyChartData.toImmutableList(),
                hourlyChartState = firstHourlyChartState,
                settingsState = settingsState,
                initialXAxisRange = detailViewModel.initialXAxisStart .. detailViewModel.initialXAxisEnd,
//                onUpdateInitialXAxisBounds = detailViewModel::updateInitialXAxisBounds,
                onAddYAxis = {
                        start: Float, end: Float, diagramHeight: Float,
                        textMeasurer: TextMeasurer, axisIndex: Int?,
                        curveAnimatorInitialValue: Float
                    ->
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
                onUpdateThemeDialogVisibility = preferenceViewModel::updateDiagramThemeDialogVisibility,
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

                onNavigateUp = navigator::goBack,
                onUpdateTimeAxis = {
                    preferenceViewModel.updateTimeAxis(it)
                    detailViewModel.setSliderThumbPositions(it)
                },
                onUpdateChartTheme = detailViewModel::updateHourlyChartTheme,
                onUpdateChartGrid = detailViewModel::updateHourlyChartGrid
            )
        }
        entry<SettingsNotification>(
            metadata = ListDetailSceneStrategy.detailPane()
        )
        {
            val notificationHourlyChartData by appViewModel.notificationHourlyChartData
                .collectAsStateWithLifecycle()
            val notificationDailyChartData by appViewModel.notificationDailyChartData
                .collectAsStateWithLifecycle()
            val settingsState by preferenceViewModel.settingsState.collectAsStateWithLifecycle()
            val allLocations by homeViewModel.allLocations.collectAsStateWithLifecycle()

            NotificationSettingsScreen(
                modifier = modifier,
                appSettings = appSettings,
                singlePane = singlePane,
                settingsState = settingsState,
                allLocations = allLocations,

                notificationHourlyChartData = notificationHourlyChartData,
                notificationDailyChartData = notificationDailyChartData,
                onUpdateNotificationVisibility = preferenceViewModel::updateNotificationVisibility,
                onUpdateNotificationCurrentQuantities = preferenceViewModel::updateNotificationCurrentWeatherQuantities,
                onUpdateNotificationHourlyDiagramIconsVisibility =
                    preferenceViewModel::updateNotificationHourlyDiagramIconsVisibility,
                onUpdateNotificationHourlyQuantities =
                    preferenceViewModel::updateNotificationHourlyQuantities,
                onUpdateNotificationCurveShadowVisibility =
                    preferenceViewModel::updateNotificationHourlyCurveShadowVisibility,
                onSaveCurvesColor = preferenceViewModel::updateNotificationCurvesColor,
                onUpdateWholeCurvesColor = preferenceViewModel::updateNotificationCurvesColor,
                onUpdateNotificationTimeAxisDialogVisibility =
                    preferenceViewModel::updateNotificationTimeAxisDialogVisibility,
                onUpdateNotificationDailyWeatherIconsVisible =
                    preferenceViewModel::updateNotificationDailyWeatherIconsVisible,
                onUpdateNotificationDailyQuantity = preferenceViewModel::updateNotificationDailyQuantity,
                onUpdateGridDialogVisibility =
                    preferenceViewModel::updateNotificationDiagramGridDialogVisibility,
                onUpdateLocationDialogVisibility =
                    preferenceViewModel::updateNotificationLocationDialogVisibility,
                onUpdateNotificationWeatherIconVisibility =
                    preferenceViewModel::updateNotificationWeatherIconVisible,
                onUpdateNotificationDateTimeDialogVisibility =
                    preferenceViewModel::updateNotificationDateTimeDialogVisibility,

                onNavigateUp = navigator::goBack,
                onUpdateNotificationTimeAxis = preferenceViewModel::updateNotificationTimeAxis,
                onUpdateDailyNotificationChartGrid = preferenceViewModel::updateNotificationDailyChartGrid,
                onUpdateNotificationDateTime = preferenceViewModel::updateNotificationDateTime,
                onUpdateNotificationLocationDialogVisibility =
                    preferenceViewModel::updateNotificationLocationDialogVisibility,
                onUpdateNotificationLocationId = preferenceViewModel::updateNotificationLocationId
            )
        }
        entry<Donate> {
//                val parentEntry = remember(key1 = it) { navController.getBackStackEntry<Donate>() }
//                val donateViewModel = hiltViewModel<DonateViewModel>(
//                    viewModelStoreOwner = parentEntry
//                )
//            val donateState by donateViewModel.donateState.collectAsStateWithLifecycle()
            val activity = LocalActivity.current as MainActivity
            DonateScreen(
                modifier = Modifier.fillMaxSize(),
                singlePane = singlePane,
                donateState = donateState,
                billingConnectionState = donateViewModel.billingClient.connectionState,
                drawerOpen = drawerOpen,

                onNavigateUp = {
                    navigator.goToStartRoute()
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

    val springStiffness = Spring.StiffnessLow
    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        sceneStrategy = rememberListDetailSceneStrategy(singlePane),
        onBack = { navigator.goBack() },
        transitionSpec = {
            ContentTransform(
                targetContentEnter = slideInHorizontally(
                    animationSpec = SpringSpec(stiffness = springStiffness),
                    initialOffsetX = { (1f * it).toInt() }
                ) + fadeIn(animationSpec = SpringSpec(stiffness = springStiffness), initialAlpha = 0.2f),
                initialContentExit = scaleOut(
                    targetScale = 0.5f, animationSpec = SpringSpec(stiffness = springStiffness)
                ) + fadeOut(animationSpec = SpringSpec(stiffness = springStiffness), targetAlpha = 0.2f)
            )
        },
        popTransitionSpec = {
            ContentTransform(
                targetContentEnter = scaleIn(
                    initialScale = 0.5f,
                    animationSpec = SpringSpec(stiffness = springStiffness)
                ) + fadeIn(animationSpec = SpringSpec(stiffness = springStiffness), initialAlpha = 0.2f),
                initialContentExit = slideOutHorizontally(
                    animationSpec = SpringSpec(stiffness = springStiffness),
                    targetOffsetX = { (1f * it).toInt() }
                ) + fadeOut(animationSpec = SpringSpec(stiffness = springStiffness), targetAlpha = 0.2f)
            )
        },
        predictivePopTransitionSpec = {
            ContentTransform(
                targetContentEnter = scaleIn(
                    initialScale = 0.5f,
                    animationSpec = SpringSpec(stiffness = springStiffness)
                ) + fadeIn(animationSpec = SpringSpec(stiffness = springStiffness), initialAlpha = 0.2f),
                initialContentExit = slideOutHorizontally(
                    animationSpec = SpringSpec(stiffness = springStiffness),
                    targetOffsetX = { (1f * it).toInt() }
                ) + fadeOut(animationSpec = SpringSpec(stiffness = springStiffness), targetAlpha = 0.2f)
            )
        }
    )
}