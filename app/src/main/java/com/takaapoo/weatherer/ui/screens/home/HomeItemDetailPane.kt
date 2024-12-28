package com.takaapoo.weatherer.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.domain.model.HomePaneContent
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.ui.screens.detail.DetailPagerScreen
import com.takaapoo.weatherer.ui.screens.detail.DetailScreen
import com.takaapoo.weatherer.ui.screens.detail.aq_detail.AirQualityDetailScreen
import com.takaapoo.weatherer.ui.theme.Gray30
import com.takaapoo.weatherer.ui.utility.toDp
import com.takaapoo.weatherer.ui.utility.toPx
import com.takaapoo.weatherer.ui.viewModels.DetailViewModel
import com.takaapoo.weatherer.ui.viewModels.HomeViewModel
import kotlinx.collections.immutable.toImmutableList


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HomeItemDetailPane(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    detailViewModel: DetailViewModel,
    chartStateList: List<HourlyChartState>,
    dailyChartStateList: List<DailyChartState>,
    hourlyChartsData: List<List<HourlyChartDto>>,
    dailyChartsData: List<List<LocalDailyWeather>>,

    singlePane: Boolean,
    locationsCount: Int,
    permanentDrawer: Boolean,
    appSettings: AppSettings,
    weatherDataLoadStatus: Map<Int, MyResult<Unit>>,
    onMenuButtonClick: () -> Unit,
    onNavigateToAddLocationScreen: () -> Unit,
    onReloadALocationWeatherData: (location: Location) -> Unit
) {
    val density = LocalDensity.current
    val mainActivity = LocalContext.current as MainActivity
    val homePaneState by homeViewModel.homePaneState.collectAsStateWithLifecycle()
    val homeState by homeViewModel.homeState.collectAsStateWithLifecycle()
    val locationListState by homeViewModel.locationsState.collectAsStateWithLifecycle()

    var homeScreenWidth by rememberSaveable { mutableFloatStateOf(0f) }
    var homeScreenHeight by rememberSaveable { mutableFloatStateOf(0f) }
//    var listBoxTopPadding by rememberSaveable { mutableFloatStateOf(0f) }

    val cardHeightPx = dimensionResource(R.dimen.home_card_max_height).toPx(density)
    val zoomMax = mainActivity.windowHeight / cardHeightPx

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
    val detailState by detailViewModel.detailState.collectAsStateWithLifecycle()

    if (locationListState.isEmpty()){
        HomeStartScreen(
            onNavigateToAddLocationScreen = onNavigateToAddLocationScreen
        )
    } else if (locationListState.first().locationId != -2) {
        Box(
            modifier = modifier.fillMaxSize()
                .onSizeChanged {
                    val width = it.width.toDp(density)
                    detailViewModel.updateInitialXAxisBounds(
                        detailScreenWidth = if (singlePane) width else width / 2
                    )
                }
        ) {
            var currentPage by rememberSaveable {
                mutableIntStateOf(0)
            }
            if (singlePane) {
                when (homePaneState.paneContent) {
                    HomePaneContent.LOCATIONLIST -> {
                        HomeScreen(
                            modifier = Modifier.fillMaxSize()
                                .onSizeChanged {
                                    homeScreenWidth = it.width.toFloat()
                                    homeScreenHeight = it.height.toFloat()
                                },
                            homeState = homeState,
                            locationListState = locationListState.toImmutableList(),
                            appSettings = appSettings,
                            weatherDataLoadStatus = weatherDataLoadStatus,
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
                            permanentDrawer = permanentDrawer,

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
                            onHandleClickSound = remember {
                                { currentRotation, previousRotation ->
                                    homeViewModel.handleClickSound(
                                        currentRotation,
                                        previousRotation
                                    )
                                }
                            },
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
                            },
//                        onEvaluateTopPaddingHeight = {
//                            listBoxTopPadding = it
//                        },
                            onMenuButtonClick = onMenuButtonClick,
                            onNavigateToDetailScreen = { locationsCount: Int, initialPageNumber: Int ->
                                homeViewModel.updateLocationsCount(count = locationsCount)
                                homeViewModel.updateInitialPage(page = initialPageNumber)
                                detailViewModel.setSliderThumbPositions()
                                homeViewModel.updateHomePaneContent(HomePaneContent.WEATHERDETAIL)
                            },
                            onNavigateToAddLocationScreen = onNavigateToAddLocationScreen,
                            onPrepareDeleteLocation = remember {
                                { locationId ->
                                    detailViewModel.prepareDeleteLocation(locationId)
                                }
                            },
                            onReloadALocationWeatherData = onReloadALocationWeatherData,
                            onUpdateClockGaugeLock = remember { homeViewModel::updateClockGaugeLock }
                        )
                    }

                    else -> {
                        val pagerState = rememberPagerState(
                            initialPage = homePaneState.initialPage,
                            pageCount = { homePaneState.listItemCount }
                        )
                        DetailScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(if (homePaneState.paneContent == HomePaneContent.WEATHERDETAIL) 1f else 0f),
                            detailState = detailState,
                            chartStateList = chartStateList,
                            dailyChartStateList = dailyChartStateList,
                            hourlyChartsData = hourlyChartsData,
                            dailyChartsData = dailyChartsData,
                            detailViewModel = detailViewModel,
                            appSettings = appSettings,
                            pagerState = pagerState,
//                    sharedTransitionScope = this@SharedTransitionLayout,
//                    animatedContentScope = this@composable,
                            onUpdateHomeSelectedItemIndex = { index: Int ->
                                homeViewModel.updateSelectedItemIndexOffset(index = index)
                            },
                            onNavigateUp = remember {
                                { pageNumber ->
                                    homeViewModel.updatePageNumberReturningFromDetail(page = pageNumber)
                                    homeViewModel.updateHomePaneContent(HomePaneContent.LOCATIONLIST)
//                    navController.previousBackStackEntry?.savedStateHandle?.set(LAST_PAGE_NUMBER, pageNumber)
//                    navController.navigateUp()
                                }
                            },
                            onNavigateToAQDetail = remember {
                                { pageNumber, scrollValue ->
                                    currentPage = pageNumber
//                            homeViewModel.updateInitialPage(page = pageNumber)
//                            detailViewModel.updateScrollValue(value = scrollValue, pageIndex = pageNumber)
                                    homeViewModel.updateHomePaneContent(HomePaneContent.AIRQUALITY)
                                }
                            }
                        )
                        if (homePaneState.paneContent == HomePaneContent.WEATHERDETAIL) {
                            BackHandler {
                                detailViewModel.updateNavigateBack(
                                    state = true,
                                    pageIndex = pagerState.currentPage
                                )
                            }
                        } else {
                            val location = detailViewModel.allLocations.getOrElse(
                                index = currentPage,
                                defaultValue = { Location() }
                            )
                            AirQualityDetailScreen(
                                detailState = detailState.getOrElse(
                                    index = currentPage,
                                    defaultValue = { DetailState() }),
                                location = location,
                                appSettings = appSettings,
                                onNavigateUp = {
                                    homeViewModel.updateHomePaneContent(HomePaneContent.WEATHERDETAIL)
                                }
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HomeScreen(
                        modifier = Modifier.fillMaxHeight().weight(1f)
                            .onSizeChanged {
                                homeScreenWidth = it.width.toFloat()
                                homeScreenHeight = it.height.toFloat()
                            },
                        homeState = homeState,
                        locationListState = locationListState.toImmutableList(),
                        appSettings = appSettings,
                        weatherDataLoadStatus = weatherDataLoadStatus,
                        isZooming = false,
                        homeScreenWidth = homeScreenWidth,
                        homeScreenHeight = homeScreenHeight,
//                    listBoxTopPadding = listBoxTopPadding,
                        zoom = 1f,
                        zoomMax = zoomMax,
                        itemTranslationX = 0f,
                        itemTranslationY = 0f,
                        windowRotation = 0f,
                        initialFirstItemIndex = homePaneState.pageNumberReturningFromDetail,
                        singlePane = false,
                        permanentDrawer = permanentDrawer,

                        onUpdateClockGaugeRotation = remember {
                            { rotation ->
                                homeViewModel.updateClockGaugeRotation(rotation)
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
                        onHandleClickSound = remember {
                            { currentRotation, previousRotation ->
                                homeViewModel.handleClickSound(currentRotation, previousRotation)
                            }
                        },
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
                                homeViewModel.navigateToItem(locationId, cardX, cardY, cardWidth)
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

                        onSwapLocations = remember {
                            { id1, id2 ->
                                val currentPageLocationId = homeViewModel.locationsState.value
                                    .getOrNull(homePaneState.initialPage)
                                    ?.locationId
                                homeViewModel.swapLocationCustomId(id1, id2)
                                detailViewModel.swapLocations(id1, id2)
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
//                    onEvaluateTopPaddingHeight = remember {{
//                        listBoxTopPadding = it
//                    }},
                        onMenuButtonClick = onMenuButtonClick,
                        onNavigateToDetailScreen = remember {
                            { locationsCount: Int, initialPageNumber: Int ->
                                homeViewModel.updateLocationsCount(count = locationsCount)
                                homeViewModel.updateInitialPage(page = initialPageNumber)
                                detailViewModel.setSliderThumbPositions()
                                homeViewModel.updateHomePaneContent(HomePaneContent.WEATHERDETAIL)
                            }
                        },
                        onNavigateToAddLocationScreen = onNavigateToAddLocationScreen,
                        onPrepareDeleteLocation = remember {
                            { locationId ->
                                detailViewModel.prepareDeleteLocation(locationId)
                            }
                        },
                        onReloadALocationWeatherData = onReloadALocationWeatherData,
                        onUpdateClockGaugeLock = remember { homeViewModel::updateClockGaugeLock }
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(color = Gray30)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .pointerInteropFilter {
                                val touchPosition = Offset(it.x, it.y)
                                if (detailState.isNotEmpty()) {
                                    if (detailState[homePaneState.initialPage].hourlyDiagramSettingOpen) {
                                        if (!detailState[homePaneState.initialPage].hourlyDiagramSettingRectangle.contains(
                                                touchPosition
                                            )
                                        ) {
                                            detailViewModel.updateHourlyDiagramSettingOpen(
                                                open = false,
                                                pageIndex = homePaneState.initialPage
                                            )
                                        }
                                        return@pointerInteropFilter !detailState[homePaneState.initialPage]
                                            .hourlyDiagramSettingRectangle.contains(touchPosition)
                                    }
                                    if (detailState[homePaneState.initialPage].dailyDiagramSettingOpen) {
                                        if (!detailState[homePaneState.initialPage].dailyDiagramSettingRectangle.contains(
                                                touchPosition
                                            )
                                        ) {
                                            detailViewModel.updateDailyDiagramSettingOpen(
                                                open = false,
                                                pageIndex = homePaneState.initialPage
                                            )
                                        }
                                        return@pointerInteropFilter !detailState[homePaneState.initialPage]
                                            .dailyDiagramSettingRectangle.contains(touchPosition)
                                    }
                                }
                                return@pointerInteropFilter false
                            },
                    ) {
                        if (homePaneState.initialPage >= 0 && detailState.isNotEmpty() &&
                            homePaneState.initialPage < detailState.size
                        ) {
                            DetailPagerScreen(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(if (homePaneState.paneContent == HomePaneContent.WEATHERDETAIL) 1f else 0f),
                                detailState = detailState.getOrElse(
                                    index = homePaneState.initialPage,
                                    defaultValue = { DetailState() }
                                ),
                                detailViewModel = detailViewModel,
                                hourlyChartState = chartStateList.getOrElse(
                                    index = homePaneState.initialPage,
                                    defaultValue = { HourlyChartState() }
                                ),
                                dailyChartState = dailyChartStateList.getOrElse(
                                    index = homePaneState.initialPage,
                                    defaultValue = { DailyChartState() }
                                ),
                                hourlyChartData = hourlyChartsData.getOrElse(
                                    index = homePaneState.initialPage,
                                    defaultValue = { emptyList() }
                                ),
                                dailyChartData = dailyChartsData.getOrElse(
                                    index = homePaneState.initialPage,
                                    defaultValue = { emptyList() }
                                ),
                                pageNumber = homePaneState.initialPage,
//                            scrollState = scrollStates[homePaneState.initialPage],
                                singlePane = false,
                                appSettings = appSettings,
                                onResetScrollStates = {},
                                onNavigateUp = remember {
                                    { pageNumber ->
                                        homeViewModel.updatePageNumberReturningFromDetail(page = pageNumber)
                                        homeViewModel.updateHomePaneContent(HomePaneContent.LOCATIONLIST)
                                    }
                                },
                                onNavigateToAQDetail = remember {
                                    { pageNumber, scrollValue ->
                                        currentPage = pageNumber
                                        homeViewModel.updateHomePaneContent(HomePaneContent.AIRQUALITY)
                                    }
                                }
                            )
                        }

                        /*val pagerState = rememberPagerState(
                        initialPage = homePaneState.initialPage,
                        pageCount = { homePaneState.listItemCount }
                    )
                    DetailScreen(
                        modifier = Modifier.fillMaxSize()
                            .alpha(if (homePaneState.paneContent == HomePaneContent.WEATHERDETAIL) 1f else 0f),
                        detailState = detailState,
                        detailViewModel = detailViewModel,
                        appSettings = appSettings,
                        pagerState = pagerState,
                        onUpdateHomeSelectedItemIndex = { index: Int ->
                            homeViewModel.updateSelectedItemIndexOffset(index = index)
                        },
                        onNavigateUp = remember {
                            { pageNumber ->
                                homeViewModel.updatePageNumberReturningFromDetail(page = pageNumber)
                                homeViewModel.updateHomePaneContent(HomePaneContent.LOCATIONLIST)
                            }
                        },
                        onNavigateToAQDetail = remember {{ pageNumber, scrollValue ->
                            currentPage = pageNumber
                            homeViewModel.updateHomePaneContent(HomePaneContent.AIRQUALITY)
                        }}
                    )*/
                        when (homePaneState.paneContent) {
                            HomePaneContent.WEATHERDETAIL -> {
                                /*BackHandler {
                                detailViewModel.updateNavigateBack(
                                    state = true,
                                    pageIndex = pagerState.currentPage
                                )
                            }*/
                            }

                            HomePaneContent.AIRQUALITY -> {
                                val location = detailViewModel.allLocations.getOrElse(
                                    index = currentPage,
                                    defaultValue = { Location() }
                                )
                                AirQualityDetailScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    detailState = detailState.getOrElse(
                                        index = currentPage,
                                        defaultValue = { DetailState() }),
                                    location = location,
                                    appSettings = appSettings,
                                    onNavigateUp = {
                                        homeViewModel.updateHomePaneContent(HomePaneContent.WEATHERDETAIL)
                                    }
                                )
                            }

                            else -> {
                                Spacer(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }

                }
            }
        }
    }
    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        detailViewModel.reInitialize()
    }

}