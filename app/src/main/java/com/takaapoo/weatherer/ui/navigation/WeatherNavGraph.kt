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
import androidx.compose.runtime.State
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.domain.model.DonatePaneContent
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.domain.model.LocationsState
import com.takaapoo.weatherer.ui.screens.ContactUsScreen
import com.takaapoo.weatherer.ui.screens.SupportScreen
import com.takaapoo.weatherer.ui.screens.add_location.AddLocationScreen
import com.takaapoo.weatherer.ui.screens.detail.DetailScreen
import com.takaapoo.weatherer.ui.screens.detail.aq_detail.AirQualityDetailScreen
import com.takaapoo.weatherer.ui.screens.donate.DonateScreen
import com.takaapoo.weatherer.ui.screens.home.HomeScreen
import com.takaapoo.weatherer.ui.screens.home.HomeStartScreen
import com.takaapoo.weatherer.ui.screens.login.Login
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
import com.takaapoo.weatherer.ui.viewModels.LoginViewModel
import com.takaapoo.weatherer.ui.viewModels.PreferenceViewModel
import com.takaapoo.weatherer.ui.viewModels.RadarViewModel
import com.takaapoo.weatherer.utility.KeepScreenOn
import com.takaapoo.weatherer.utility.collectAsStateWithPermission
import com.takaapoo.weatherer.utility.toDp
import com.takaapoo.weatherer.utility.toPx
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
//    donateViewModel: DonateViewModel,
//    donateState: DonateState,
    appSettings: AppSettings,
    locationListState: State<List<LocationsState>>,
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
    val homeState = homeViewModel.homeState.collectAsStateWithLifecycle()
    val mainActivity = LocalActivity.current as MainActivity

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
            val locationsBriefStates by homeViewModel.locationsBriefState.collectAsStateWithLifecycle()

            var homeScreenWidth by rememberSaveable { mutableFloatStateOf(0f) }
            var homeScreenHeight by rememberSaveable { mutableFloatStateOf(0f) }

            val cardHeightPx = dimensionResource(R.dimen.home_card_max_height).toPx(density)
            val zoomMax = remember(mainActivity.windowHeight) {
                mainActivity.windowHeight / cardHeightPx
            }
//            val width by remember { mutableStateOf(0.dp) }

            val transition = updateTransition(
                targetState = homeState.value.zoom,
                label = "Transition"
            )

            val transitionDuration = 1300
            val zoomEasing = CubicBezierEasing(0.7f, 0f, 0.3f, 1f)
            val translationEasing = if (homeState.value.zoom) CubicBezierEasing(0f, 0f, 0.3f, 1f)
            else CubicBezierEasing(1f, 0f, 0.7f, 1f)

            val translationTransitionSpec =
                tween<Float>(durationMillis = transitionDuration, easing = translationEasing)
            val zoom = transition.animateFloat(
                transitionSpec = { tween(durationMillis = transitionDuration, easing = zoomEasing) },
                label = "Zoom",
                targetValueByState = { state ->
                    if (state) zoomMax else 1f
                }
            )
            val itemTranslationX = transition.animateFloat(
                transitionSpec = { translationTransitionSpec },
                label = "TranslationX"
            ) { state ->
                if (state) (homeScreenWidth - homeState.value.cardWidth)/2 - homeState.value.selectedCardX
                else 0f
            }
            val itemTranslationY = transition.animateFloat(
                transitionSpec = { translationTransitionSpec },
                label = "TranslationY",
                targetValueByState = { state ->
                    if (state) (mainActivity.windowHeight - cardHeightPx)/2 - (homeState.value.selectedCardY)
                    else -homeState.value.verticalOffsetDifference
                }
            )
            val windowRotation = transition.animateFloat(
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
                locationsBriefStates = locationsBriefStates,
                homeRealState = homeState,
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
                itemTranslationY = itemTranslationY,
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
//                onUpdateHomePaneContent = remember {
//                    { content ->
//                        homeViewModel.updateHomePaneContent(content)
//                    }
//                },
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
                    { locationId, itemIndex ->
                        homeViewModel.navigateToItem(locationId, itemIndex)
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
                    if (!singlePane) {
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
                    detailViewModel.initializeSliderThumbPositions()
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
//                onUpdateItemDragged = remember { homeViewModel::updateItemDragged },
                onDownloadAndSaveLandscapes = remember {
                    { context, picId, picUrl, picName ->
                        homeViewModel.downloadAndSaveLandscapes(context, picId, picUrl, picName)
                    }
                },
                onUpdateInitialLandscape = remember { detailViewModel::updateInitialLandScape },
//                onReInitialize = remember { detailViewModel::reInitialize },
                onAdjustSettings = remember { detailViewModel::adjustSettings },
                onUpdateHomeCardX = remember {
                    { cardX: Float, itemIndex: Int ->
                        homeViewModel.updateHomeCardX(cardX, itemIndex)
                    }
                },
                onUpdateHomeCardY = remember {
                    { cardY: Float, itemIndex: Int ->
                        homeViewModel.updateHomeCardY(cardY, itemIndex)
                    }
                },
                onUpdateHomeCardWidth = remember {
                    { cardWidth: Float, itemIndex: Int ->
                        homeViewModel.updateHomeCardWidth(cardWidth, itemIndex)
                    }
                }
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
                it.collectAsStateWithPermission(dataReadyForDisplay)
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
            val trialPeriodExpired by appViewModel.user.trialPeriodExpired.collectAsStateWithLifecycle()
            val hasDonated by appViewModel.billing.hasDonated.collectAsStateWithLifecycle()

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
                trialPeriodExpired = trialPeriodExpired,
                hasDonated = hasDonated,
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
                        navigator.navigate(route = AQDetail(pageNumber))
                    }
                },
                onNavigateToDonateScreen = {
                    navigator.navigate(route = Donate)
                },
                onNavigateToLoginScreen = {
                    navigator.navigate(route = Login)
                },
                onConnectRetry = {
                    appViewModel.billing.startBillingConnection()
                }
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
                onGoToMyLocation = {
                    addLocationViewModel.goToMyLocation2(mainActivity)
                },
                onUpdateSearchQuery = addLocationViewModel::updateSearchQuery,
                onGoToLocation = addLocationViewModel::goToLocation,
                onUpdateSelectedLocationLatLng = addLocationViewModel::updateSelectedLocationLatLng,
                onUpdateAddLocationDialogVisibility = addLocationViewModel::updateAddLocationDialogVisibility,
                onAddLocationToDB = addLocationViewModel::addLocationToDB,
                onUpdateSelectedLocationName = addLocationViewModel::updateSelectedLocationName,
                onNavigateUp = navigator::goBack,
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
            val radarViewModel = hiltViewModel<RadarViewModel>()
            val radarState = radarViewModel.radarState.collectAsStateWithLifecycle()
            val internetConnected = radarViewModel.internetConnected.collectAsStateWithLifecycle()
            val locationsBriefStates by homeViewModel.locationsBriefState.collectAsStateWithLifecycle()
            var radarScreenWidth by rememberSaveable { mutableFloatStateOf(0f) }

            RadarScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        radarScreenWidth = it.width.toFloat()
                    },
                radarRealState = radarState,
                locationState = locationsBriefStates,
                appSettings = appSettings,
                internetConnected = internetConnected,
                radarScreenWidth = radarScreenWidth,

                onUpdateSelectedQuantity = radarViewModel::updateRadarQuantity,
                onUpdateQuantityWeatherModel = radarViewModel::updateQuantityWeatherModels,
                onUpdateQuantityDialogVisible = radarViewModel::updateQuantityDialogVisible,
                onUpdateLoadingIndicatorVisible = radarViewModel::updateLoadingIndicatorVisible,
                onUpdateQuantityValue = radarViewModel::updateQuantityValue,
                onUpdateElevation = radarViewModel::updateElevation,
                onUpdateSettingsOpen = radarViewModel::updateSettingsOpen,
                onUpdateCrossHairVisibility = radarViewModel::updateCrossHairVisible,
                onUpdateElevationVisibility = radarViewModel::updateElevationVisible,
                onUpdateClockGaugeVisibility = radarViewModel::updateClockGaugeVisible,
                onUpdateRadarClock = radarViewModel::updateClock,
                onReloadRadarData = radarViewModel::reloadRadarData,

                onUpdateVisibleDayIndex = radarViewModel::updateVisibleDayIndex,
                onUpdateDayGaugeIndex = radarViewModel::updateDayGaugeIndex,
                onUpdateClockGaugeRotation = radarViewModel::updateClockGaugeRotation,
                onBalanceClockGaugeValues = radarViewModel::balanceClockGaugeValues,
                onResetDayGauge = radarViewModel::resetDayGauge,
                onInitializeClockGauge = remember { radarViewModel::initializeClockGauge },
                onUpdateClockGaugeDragStopped = radarViewModel::updateClockGaugeDragStopped,
                onUpdateNoDataWarningVisible = remember { radarViewModel::updateNoDataWarningVisible },
                onCalculateDataIndex = remember { radarViewModel::calculateDataIndex },

                onNavigateUp = navigator::goBack
            )
        }
        entry<Settings>(
            metadata = ListDetailSceneStrategy.listPane()
        ) {
            MainSettingsScreen(
                modifier = Modifier.fillMaxSize(),
                appSettings = appSettings,
                onUpdateNotificationVisibility = preferenceViewModel::updateNotificationVisibility,
                onNavigateUp = navigator::goToStartRoute,
                onUpdatePaneContent = {_, _ -> },
                onNavigateToScreen = navigator::navigate
            )
        }
        entry<SettingsGeneral>(
            metadata = ListDetailSceneStrategy.detailPane()
        )
        {
            val settingsState by preferenceViewModel.settingsState.collectAsStateWithLifecycle()
            GeneralSettingsScreen(
                modifier = Modifier.fillMaxSize(),
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
                modifier = Modifier.fillMaxSize(),
                itemState = locationListState.value[0],
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
                modifier = Modifier.fillMaxSize(),
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
                modifier = Modifier.fillMaxSize(),
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
                    detailViewModel.initializeSliderThumbPositions(it)
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
                modifier = Modifier.fillMaxSize(),
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
            val donateViewModel = hiltViewModel<DonateViewModel>()
            val donateState by donateViewModel.donateState.collectAsStateWithLifecycle()
            val billingState by appViewModel.billing.billingState.collectAsStateWithLifecycle()
            val activity = LocalActivity.current as MainActivity
            DonateScreen(
                modifier = Modifier.fillMaxSize(),
                singlePane = singlePane,
                donateState = donateState,
                billingState = billingState,

                billingConnectionState = donateViewModel.billing.billingClient.connectionState,
                drawerOpen = drawerOpen,

                onNavigateUp = {
                    navigator.goBack()
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
                },
                onUpdatePurchaseSuccessDialogVisibility =
                    appViewModel.billing::updatePurchaseSuccessDialogVisibility,
            )
        }
        entry<Login>(
//            metadata = ListDetailSceneStrategy.detailPane()
        ){
            val loginViewModel = hiltViewModel<LoginViewModel>()
            val loginState by loginViewModel.loginState.collectAsStateWithLifecycle()
            Login(
                modifier = Modifier.fillMaxSize(),
                loginState = loginState,
                user = loginViewModel.user,
                theme = appSettings.theme,

                onNavigateUp = navigator::goBack,
                onUpdateLoginState = loginViewModel::updateLoginState,
            )
        }
        entry<ContactUs>{
            ContactUsScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateUp = navigator::goBack,
            )
        }
        entry<Support>{
            SupportScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateUp = navigator::goBack,
            )
        }
    }

    val springStiffness = Spring.StiffnessLow

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        modifier = modifier,
        sceneStrategies = listOf( rememberListDetailSceneStrategy(singlePane) ),
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