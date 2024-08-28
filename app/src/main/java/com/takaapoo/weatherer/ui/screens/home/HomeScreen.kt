package com.takaapoo.weatherer.ui.screens.home

import android.util.Log
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.Screens
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.LocationsState
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.viewModels.HomeViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

enum class DragValue { Left, Center, Right }
val DRAG_MAX_DISPLACE = 130.dp
val CardBackIconSize = 60.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    navController: NavController = rememberNavController(),
    appSettings: AppSettings,
    initialFirstItemIndex: Int,
    onMenuButtonClick: () -> Unit
) {
    val context = LocalContext.current
    val density : Density = LocalDensity.current
    val homeState by homeViewModel.homeState.collectAsStateWithLifecycle()
    val locationListState by homeViewModel.locationsState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = homeState.selectedItemIndex,
        initialFirstVisibleItemScrollOffset = homeState.selectedItemOffset
    )


    val pullRefreshState = rememberPullToRefreshState()

    var screenWidth by remember { mutableStateOf(0.dp) }
    val roomWidth = remember(key1 = screenWidth) {
        (screenWidth.toPx(density) - 4 * context.resources.getDimension(R.dimen.home_card_spacing))
            .coerceAtLeast(0f)
    }
    val cardMaxHeight = dimensionResource(R.dimen.weather_item_data_height) +
            0.6f * (screenWidth - 4 * dimensionResource(R.dimen.home_card_spacing)) +
            2 * dimensionResource(R.dimen.home_card_spacing)
    val coroutineScope = rememberCoroutineScope()
    val dayScrollIndex = remember{
        derivedStateOf {
            val daysOfHours = floor(
                (-homeState.clockGaugeRotation.value - homeState.clockGaugeNaturalRotation) / 360f
            ).toInt()
            homeState.let {
                it.dayGaugeIndex + it.dayGaugeNaturalIndex + daysOfHours - it.visibleDayIndex
            }
        }
    }
    val clockManipulatorHeight = (dimensionResource(R.dimen.clock_manipulator_height) + 48.dp)
    val topAppBarHeight = 64.dp

    var clockGaugeShown by remember{ mutableStateOf(true) }
    val cardHeightPx = cardMaxHeight.toPx(density)
    when {
        listState.scrollVelocity(cardHeightPx) > 1000f ->
            clockGaugeShown = false
        listState.scrollVelocity(cardHeightPx) < -900f ->
            clockGaugeShown = true
    }
    if (!listState.canScrollForward) clockGaugeShown = true

    val clockGaugeY by animateFloatAsState(
        targetValue = if (clockGaugeShown) 0f else clockManipulatorHeight.toPx(density),
        label = "clock gauge offset",
        animationSpec = tween(durationMillis = 300)
    )


//    LaunchedEffect(Unit) {
//        homeViewModel.dayGaugeMinutesFlow.collectLatest {
//            Log.i("clock1", "minute = ${it}")
//        }
//    }
//    Log.i("clock1", "rotation state = ${homeViewModel.gaugeRotationState.value}")
//    Log.i("clock1", "rotation = ${homeState.dayGaugeIndex}")

//    homeViewModel.moonPhaseMap = Json.parseToJsonElement(
//        readJSONFromAssets(context, "moon_phase.json")
//    ).jsonObject.toMap()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState(),
        canScroll = { !homeState.dayListState.isScrollInProgress }
    )


    val clockGaugeWidth = (900.dp).coerceAtMost(screenWidth / sin(PI/6).toFloat())
    val clockGaugeRotationCoefficient = 360 / (3.14f * clockGaugeWidth.toPx(density))
    val clockGaugeDragState = rememberDraggableState { delta ->
        homeViewModel.updateClockGaugeRotation(
            rotation = homeState.clockGaugeRotation.value + delta * clockGaugeRotationCoefficient
        )
    }

    val transition = updateTransition(
        targetState = homeState.zoom,
        label = "Transition"
    )

    val transitionDuration = 1600
    val zoomEasing = CubicBezierEasing(0.76f, 0f, 0.24f, 1f)
    val translationEasing = if (homeState.zoom) CubicBezierEasing(0f, 0f, 0.15f, 1f)
    else CubicBezierEasing(1f, 0f, 0.85f, 1f)

    val translationTransitionSpec =
        tween<Float>(durationMillis = transitionDuration, easing = translationEasing)
    val zoom by transition.animateFloat(
        transitionSpec = { tween(durationMillis = transitionDuration, easing = zoomEasing) },
        label = "Zoom",
        targetValueByState = { state -> if (state) 3f else 1f }
    )
    val scaffoldTranslationX by transition.animateFloat(
        transitionSpec = { translationTransitionSpec },
        label = "TranslationX") { state ->
        if (state) (context as MainActivity).windowWidth/2 - homeState.zoomCenterX
        else 0f
    }
    val scaffoldTranslationY by transition.animateFloat(
        transitionSpec = { translationTransitionSpec },
        label = "TranslationY",
        targetValueByState = { state ->
            if (state) (context as MainActivity).windowHeight/2 - homeState.zoomCenterY
            else 0f
        }
    )
    var scaffoldTranslationYMultiplier by rememberSaveable { mutableFloatStateOf(1f) }
    val windowRotation by transition.animateFloat(
        transitionSpec = { translationTransitionSpec },
        label = "windowRotation"
    ) { state -> if (state) 90f else 0f }


    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            homeViewModel.refreshWeatherData()
        }
    }
    LaunchedEffect(key1 = homeState.isRefreshing) {
        if (!homeState.isRefreshing)
            pullRefreshState.endRefresh()
    }
    LaunchedEffect(key1 = transition.isRunning) {
        if (zoom == 3f && !homeState.navigatedToDetailScreen) {
            val selectedLocationPageNumber = locationListState.indexOfFirst {
                it.locationId == homeState.selectedLocationId
            }
            navController.navigate(
                route = "${Screens.DETAIL.name}/${roomWidth}/${locationListState.size}/${selectedLocationPageNumber}"
            )
            homeViewModel.updateNavigatedToDetailScreen(true)
        } else if (zoom == 1f && homeState.navigatedToDetailScreen) {
            homeViewModel.updateNavigatedToDetailScreen(false)
            scaffoldTranslationYMultiplier = 1f
        }
    }
    LaunchedEffect(key1 = homeState.editNameResult){
        when(homeState.editNameResult){
            is MyResult.Success -> {
                snackbarHostState.showSnackbar(
                    (homeState.editNameResult as MyResult.Success<*>).data as String
                )
                homeViewModel.resetEditNameResult()
            }
            is MyResult.Error -> {
                snackbarHostState.showSnackbar(
                    (homeState.editNameResult as MyResult.Error<*>).message as String
                )
                homeViewModel.resetEditNameResult()
            }
            else -> {}
        }
    }
    LaunchedEffect(key1 = true) {
        delay(100) // This is because it is stupid and need some delay to scroll correctly
        listState.scrollToItem(
            index = initialFirstItemIndex,
            scrollOffset = -homeState.selectedItemOffset
        )
        locationListState.getOrNull(initialFirstItemIndex)?.locationId?.let {
            homeViewModel.updateSelectedLocationId(it)
        }
        val offset = listState.layoutInfo.visibleItemsInfo.find {
            it.key == locationListState[initialFirstItemIndex].locationId
        }?.offset
        offset?.let {
            homeViewModel.modifyZoomCenters(
                centerXChange = null,
                centerYChange = it.toFloat() - homeState.selectedItemOffset
            )
            if (scaffoldTranslationY != 0f)
                scaffoldTranslationYMultiplier =
                    (scaffoldTranslationY - it.toFloat() + homeState.selectedItemOffset) / scaffoldTranslationY
        }

        homeViewModel.resetZoom()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        val selectedItemIndex = locationListState.indexOfFirst {
            it.locationId == homeState.selectedLocationId
        }
        val selectedItemOffset = listState.layoutInfo.visibleItemsInfo.find {
            it.key == homeState.selectedLocationId
        }?.offset ?: 0
        homeViewModel.updateSelectedItemIndexOffset(
            index = selectedItemIndex,
            offset = selectedItemOffset
        )
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        clockGaugeShown = true
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(connection = pullRefreshState.nestedScrollConnection)
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .graphicsLayer(
                    scaleX = 5f * zoom - 4f,
                    scaleY = 5f * zoom - 4f,
                    transformOrigin = TransformOrigin(
                        pivotFractionX = homeState.zoomCenterX / (context as MainActivity).windowWidth,
                        pivotFractionY = homeState.zoomCenterY / context.windowHeight
                    ),
                    translationX = scaffoldTranslationX,
                    translationY = scaffoldTranslationY * scaffoldTranslationYMultiplier
                ),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                     title = {
                         Text("iWeather")
                     },
                     navigationIcon = {
                         IconButton(onClick = onMenuButtonClick) {
                             Icon(
                                 imageVector = Icons.Default.Menu,
                                 contentDescription = "Drawer"
                             )
                         }
                     },
                     actions = {
                         IconButton(
                             onClick = {
                                 navController.navigate(route = Screens.ADDLOCATION.name)
                             }
                         ) {
                             Icon(
                                 painter = painterResource(R.drawable.add_location_24px),
                                 contentDescription = "Add Location"
                             )
                         }
                     },
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                ClockManipulator(
                    modifier = Modifier
                        .graphicsLayer (translationY = clockGaugeY),
                    gaugeSize = clockGaugeWidth,
                    dragState = clockGaugeDragState,
                    clockGaugeRotationAnimatable = homeState.clockGaugeRotation,
                    clockGaugeNaturalRotation = homeState.clockGaugeNaturalRotation,
                    clockGaugeRotationCoefficient = clockGaugeRotationCoefficient,
                    dayListState = homeState.dayListState,
                    dayScrollIndex = dayScrollIndex,
                    updateVisibleDayIndex = homeViewModel::updateVisibleDayIndex,
                    updateDayGaugeIndex = homeViewModel::updateDayGaugeIndex,
                    calculateDayScrollIndex = homeViewModel::calculateDayScrollIndex,
                    onHandleClickSound = homeViewModel::handleClickSound,
                    onResetClockGauge = {
                        homeViewModel.balanceClockGaugeValues()
                        coroutineScope.launch {
                            homeState.clockGaugeRotation.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 700)
                            )
                        }
                        homeViewModel.resetDayGauge()
                    }
                )
            }
        ) { paddingValues ->
            when {
                homeState.deleteDialogVisible -> {
                    DeleteAlertDialog(
                        onDismiss = {
                            homeViewModel.updateDeleteDialogVisibility(visible = false)
                        },
                        onConfirmation = {
                            homeViewModel.updateToBeDeletedLocationId()
                            homeViewModel.updateDeleteDialogVisibility(visible = false)
                        },
                        locationName = homeState.deleteDialogLocationName
                    )
                }
                homeState.editDialogVisible -> {
                    EditDialog(
                        onDismiss =  {
                            homeViewModel.updateEditDialogVisibility(visible = false)
                        },
                        onConfirmation =  {
                            if (homeState.editDialogOldLocationName != homeState.editDialogNewLocationName) {
                                homeViewModel.updateLocation(
                                    id = homeState.editDialogLocationId,
                                    newLocationName = homeState.editDialogNewLocationName
                                )
                            }
                            homeViewModel.updateEditDialogVisibility(visible = false)
                        },
                        nameAlreadyExists = homeState.nameAlreadyExists and
                                (homeState.editDialogOldLocationName != homeState.editDialogNewLocationName),
                        locationName = homeState.editDialogNewLocationName,
                        onLocationNameChange = {
                            homeViewModel.updateEditDialogLocationName(locationName = it)
                        }
                    )
                }
            }
            val layoutDirection = LocalLayoutDirection.current
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .onGloballyPositioned {
                        screenWidth = it.size.width.toDp(density)
                    },
                contentPadding = PaddingValues(
                    start = paddingValues.calculateStartPadding(layoutDirection) + dimensionResource(R.dimen.home_card_spacing),
                    end = paddingValues.calculateEndPadding(layoutDirection) + dimensionResource(R.dimen.home_card_spacing),
                    top = paddingValues.calculateTopPadding() + dimensionResource(R.dimen.home_card_spacing),
                    bottom = paddingValues.calculateBottomPadding() + dimensionResource(R.dimen.home_card_spacing)
                ),
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = locationListState,
                    key = { item -> item.locationId }
                ) { item ->
                    WeatherItem2(
                        zoom = if (homeState.selectedLocationId == item.locationId) zoom else 1f,
                        cardMaxHeight = dimensionResource(R.dimen.home_card_max_height),
                        lazyColumnHeight = context.windowHeight -
                                (clockManipulatorHeight + topAppBarHeight + 32.dp).toPx(density),
                        roomWidth = (screenWidth - 4 * dimensionResource(R.dimen.home_card_spacing)).coerceAtLeast(0.dp),
                        windowRotation = if (homeState.selectedLocationId == item.locationId) windowRotation else 0f,
//                    modifier = Modifier.animateItemPlacement(
//                        animationSpec = tween(durationMillis = 1300, easing = zoomEasing)
//                    ),
                        modifier = Modifier
//                                .widthIn(max = 380.dp)
                            .alpha(if (homeState.toBeDeletedLocationId == item.locationId) 0f else 1f),
                        itemState = item,
                        pageNumber = locationListState.indexOfFirst { locationState ->
                            item.locationId == locationState.locationId
                        },
                        lazyParentState = listState,
                        onNavigateToItem = { id, centerX, centerY ->
                            homeViewModel.navigateToItem(id, centerX, centerY)
                        },
                        appSettings = appSettings,
                        onDeleteIconPressed = { id, locationName ->
                            homeViewModel.updateDeleteDialogData(id, locationName)
                            homeViewModel.updateDeleteDialogVisibility(visible = true)
                        },
                        onEditItem = { id, locationName ->
                            homeViewModel.updateEditDialogData(id, locationName)
                            homeViewModel.updateEditDialogLocationName(locationName = locationName)
                            homeViewModel.updateEditDialogVisibility(visible = true)
                        },
                        dialogDisplayed = homeState.deleteDialogVisible || homeState.editDialogVisible,
                        toBeDeletedLocationId = homeState.toBeDeletedLocationId,
                        onDeleteItem = { locationId ->
                            homeViewModel.deleteLocation(locationId)
                        }
                    )

                }
            }
        }
        PullToRefreshContainer(
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}



@Composable
private fun LazyListState.scrollVelocity(itemSize: Float): Float {
    var previousTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var previousScroll by remember(this) {
        mutableFloatStateOf(firstVisibleItemScrollOffset + firstVisibleItemIndex * itemSize)
    }
//    return remember(key1 = this) {
//        derivedStateOf {
//            val currentScroll = firstVisibleItemScrollOffset + firstVisibleItemIndex * 300
//            val currentTime = System.currentTimeMillis()
//            1000f * (currentScroll - previousScroll) / (currentTime - previousTime)
//            .also {
//                previousScroll = currentScroll
//                previousTime = currentTime
//            }
//        }
//    }.value
    val currentScroll = remember {
        derivedStateOf { firstVisibleItemScrollOffset  + firstVisibleItemIndex * itemSize }
    }.value
    val currentTime = remember(key1 = currentScroll) { System.currentTimeMillis() }
    return 1000f * (currentScroll - previousScroll) / (currentTime - previousTime)
        .also {
            previousScroll = currentScroll
            previousTime = currentTime
        }
}

fun Int.toDp(density: Density) = with(density){
    this@toDp.toDp()
}
fun Dp.toSp(density: Density) = with(density){
    this@toSp.toSp()
}
fun Dp.toPx(density: Density) = with(density){
    this@toPx.toPx()
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(modifier: Modifier = Modifier) {
    WeathererTheme {
        HomeScreen(
            appSettings = AppSettings(),
            initialFirstItemIndex = 0,
            onMenuButtonClick = {},

        )
    }
}