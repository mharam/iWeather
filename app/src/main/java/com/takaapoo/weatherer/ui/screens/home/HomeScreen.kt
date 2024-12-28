package com.takaapoo.weatherer.ui.screens.home

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.domain.model.HomePaneContent
import com.takaapoo.weatherer.domain.model.HomeState
import com.takaapoo.weatherer.domain.model.LocationsState
import com.takaapoo.weatherer.ui.screens.add_location.SearchBarAdam
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.utility.swap
import com.takaapoo.weatherer.ui.utility.toDp
import com.takaapoo.weatherer.ui.utility.toPx
import com.takaapoo.weatherer.ui.viewModels.DialogType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.PI
import kotlin.math.sin

enum class DragValue { Left, Center, Right }
val DRAG_MAX_DISPLACE = 130.dp
val CardBackIconSize = 60.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeState: HomeState,
    locationListState: ImmutableList<LocationsState>,
    appSettings: AppSettings,
    weatherDataLoadStatus: Map<Int, MyResult<Unit>>,
    isZooming: Boolean,
    homeScreenWidth: Float,
    homeScreenHeight: Float,
//    listBoxTopPadding: Float,
    zoom: Float,
    zoomMax: Float,
    itemTranslationX: Float,
    itemTranslationY: Float,
    windowRotation: Float,
    initialFirstItemIndex: Int?,
    singlePane: Boolean = true,
    permanentDrawer: Boolean,

    onUpdateClockGaugeRotation: (rotation: Float) -> Unit,
    onRefreshWeatherData: () -> Unit,
    onUpdateNavigatedToDetailScreen: (navigated: Boolean) -> Unit,
    onUpdateVerticalOffsetDifference: (difference: Float) -> Unit,
    onResetEditNameResult: () -> Unit,
    onResetZoom: () -> Unit,
    onBalanceClockGaugeValues: () -> Unit,
    onResetDayGauge: () -> Unit,
    onUpdateVisibleDayIndex: (newIndex: Int) -> Unit,
    onUpdateDayGaugeIndex: (newVisibleDay: Int) -> Unit,
    onHandleClickSound: (currentRotation: Float, previousRotation: Float) -> Unit,
    onUpdateDialogVisibility: (dialogType: DialogType, visible: Boolean) -> Unit,
    onUpdateToBeDeletedLocationId: () -> Unit,
    onUpdateHomePaneContent: (content: HomePaneContent) -> Unit,
    onModifyInitialPage: (deletedPage: Int) -> Unit,
    onUpdateLocation: (id: Int, newName: String) -> Unit,
    onUpdateEditDialogLocationName: (locationName: String) -> Unit,
    onUpdateSelectedItemIndexOffset: (index: Int?, offset: Int?) -> Unit,
    onNavigateToItem: (locationId: Int, cardX: Float, cardY: Float, cardWidth: Float) -> Unit,
    onUpdateSelectedLocationId: (locationId: Int) -> Unit,
    onUpdateDialogData: (dialogType: DialogType, id: Int, locationName: String) -> Unit,
    onDeleteLocation: (id: Int) -> Unit,
    onUpdateFilterText: (text: String) -> Unit,

    onSwapLocations: (id1: Int, id2: Int) -> Unit,
//    onEvaluateTopPaddingHeight: (Float) -> Unit,
    onMenuButtonClick: () -> Unit,
    onNavigateToDetailScreen: (locationsCount: Int, initialPageNumber: Int) -> Unit,
    onNavigateToAddLocationScreen: () -> Unit,
    onPrepareDeleteLocation: (locationId: Int) -> Unit,
    onReloadALocationWeatherData: (location: Location) -> Unit,
    onUpdateClockGaugeLock: (lock: Boolean) -> Unit
) {
//    Log.i("comp1", "initialFirstVisibleItemIndex = ${(initialFirstItemIndex ?: homeState.selectedItemIndex)} , initialFirstVisibleItemScrollOffset = ${-homeState.selectedItemOffset}")
    val density : Density = LocalDensity.current
    var sortedLocationListState by remember { mutableStateOf(locationListState) }
    val displayedLocationListState = sortedLocationListState.filter {
        it.locationName.contains(
            other = homeState.filterText.trim(),
            ignoreCase = true
        )
    }
//    Log.i("loca1", "count = ${displayedLocationListState.size} , ${locationListState.size}")
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialFirstItemIndex?.plus(1) ?: homeState.selectedItemIndex),
        initialFirstVisibleItemScrollOffset = -homeState.selectedItemOffset
    )
    val reorderableLazyListState = rememberReorderableLazyListState(listState) { from, to ->
        val toIndex = sortedLocationListState.indexOfFirst { it.locationId == to.key }
        val fromIndex = sortedLocationListState.indexOfFirst { it.locationId == from.key }
        sortedLocationListState = sortedLocationListState.toMutableList().apply {
            swap(toIndex, fromIndex)
        }.toImmutableList()
        onSwapLocations(from.key as Int, to.key as Int)
    }
    var sortedLocationListUpdatePermission by rememberSaveable { mutableStateOf(true) }
//    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    val cardMaxHeight = dimensionResource(R.dimen.home_card_max_height)
    val cardHeightPx = cardMaxHeight.toPx(density)

    val clockManipulatorHeight = (dimensionResource(R.dimen.clock_manipulator_height))
    var topAppBarHeight by remember{ mutableStateOf(64.dp) }
    var animateScrolling = remember { mutableStateOf(false) }
    var clockGaugeShown = listState.clockGaugeVisibility(cardHeightPx, animateScrolling)

    val clockGaugeY by animateFloatAsState(
        targetValue = if (homeState.clockGaugeLock || clockGaugeShown) 0f
        else (clockManipulatorHeight + 56.dp).toPx(density),
        label = "clock gauge offset",
        animationSpec = tween(durationMillis = 300)
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val topAppBarState = rememberTopAppBarState(
//        initialHeightOffset = homeState.topPaddingOffset
    )
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        state = topAppBarState,
        canScroll = { !homeState.dayListState.isScrollInProgress }
    )

    val clockGaugeWidth by remember(key1 = homeScreenWidth) {
        mutableStateOf((900.dp).coerceAtMost(homeScreenWidth.toDp(density) / sin(PI/6).toFloat()))
    }
    val clockGaugeRotationCoefficient = 360 / (3.14f * clockGaugeWidth.toPx(density))
//    val updatingZoom by rememberUpdatedState(newValue = zoom)


    var cardAlpha by rememberSaveable { mutableStateOf(zoom > 1f) }
    var selectedItem by remember {
        mutableStateOf(sortedLocationListState.getOrElse(
            index = homeState.selectedItemIndex,
            defaultValue = { LocationsState() }
        ))
    }
    var selectedPage by remember {
        mutableIntStateOf(sortedLocationListState.indexOfFirst { locationState ->
            selectedItem.locationId == locationState.locationId
        })
    }




    LaunchedEffect(key1 = locationListState, key2 = sortedLocationListUpdatePermission) {
        if (sortedLocationListUpdatePermission)
            sortedLocationListState = locationListState
    }
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            onRefreshWeatherData()
        }
    }
    LaunchedEffect(key1 = homeState.isRefreshing, key2 = pullRefreshState.isRefreshing) {
        if (!homeState.isRefreshing)
            pullRefreshState.endRefresh()
    }
    LaunchedEffect(key1 = isZooming) {
        if (zoom == zoomMax && !homeState.navigatedToDetailScreen) {
//            val selectedLocationPageNumber = locationListState.indexOfFirst {
//                it.locationId == homeState.selectedLocationId
//            }
//            homeViewModel.updateTopPaddingOffset(topAppBarState.heightOffset)
            onNavigateToDetailScreen(locationListState.size, selectedPage)
            onUpdateNavigatedToDetailScreen(true)
        } else if (zoom == 1f && homeState.navigatedToDetailScreen) {
            onUpdateNavigatedToDetailScreen(false)
            onUpdateVerticalOffsetDifference(0f)
        }
    }
    LaunchedEffect(key1 = homeState.editNameResult){
        when(homeState.editNameResult){
            is MyResult.Success -> {
                snackbarHostState.showSnackbar(
                    (homeState.editNameResult as MyResult.Success<*>).data as String
                )
                onResetEditNameResult()
            }
            is MyResult.Error -> {
                snackbarHostState.showSnackbar(
                    (homeState.editNameResult as MyResult.Error<*>).message as String
                )
                onResetEditNameResult()
            }
            else -> {}
        }
    }
    LaunchedEffect(key1 = true) {
        initialFirstItemIndex?.let {
            locationListState.getOrNull(initialFirstItemIndex)?.locationId?.let {
                onUpdateSelectedLocationId(it)
            }
            val offset = listState.layoutInfo.visibleItemsInfo.find {
                it.key == locationListState[initialFirstItemIndex].locationId
            }?.offset
            offset?.let {
                onUpdateVerticalOffsetDifference(homeState.selectedItemOffset - it.toFloat())
            }
        }
        onResetZoom()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        clockGaugeShown = true
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchbarInteractionSource = remember { MutableInteractionSource() }
    val searchbarIsFocused by searchbarInteractionSource.collectIsFocusedAsState()
    val layoutDirection = LocalLayoutDirection.current

    Box(
        modifier = modifier
            .nestedScroll(connection = pullRefreshState.nestedScrollConnection)
    ) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    CenterAlignedTopAppBar(
                        modifier = Modifier
//                        .onSizeChanged { size -> topAppBarHeight = size.height.toDp(density) }
                        ,
                        title = {
                            SearchBarAdam(
                                query = homeState.filterText,
                                onQueryChange = onUpdateFilterText,
                                onSearch = {},
                                active = false,
                                hasResultTable = false,
                                permanentDrawer = permanentDrawer,
                                onBackPressed = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                },
                                placeholder = {
                                    Text(
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.customColorScheme.lowEmphasisText,
                                        text = "Filter here!"
                                    )
                                },
                                leadingIcon = if (!permanentDrawer) {
                                    {
                                        IconButton(
                                            onClick = {
                                                if (searchbarIsFocused) {
                                                    focusManager.clearFocus()
                                                    keyboardController?.hide()
                                                    onUpdateFilterText("")
                                                } else onMenuButtonClick()
                                            }
                                        ) {
                                            if (searchbarIsFocused)
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close"
                                                )
                                            else
                                                Icon(
                                                    imageVector = Icons.Default.Menu,
                                                    contentDescription = "Drawer"
                                                )

                                        }
                                    }
                                } else if (searchbarIsFocused) {
                                    {
                                        IconButton(
                                            onClick = {
                                                focusManager.clearFocus()
                                                keyboardController?.hide()
                                                onUpdateFilterText("")
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close"
                                            )
                                        }
                                    }
                                } else null,
                                trailingIcon = {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        SmallFloatingActionButton(
                                            onClick = onNavigateToAddLocationScreen,
                                            shape = CircleShape,
                                            content = {
                                                Icon(
                                                    painter = painterResource(R.drawable.add_location_24px),
                                                    contentDescription = "Add Location"
                                                )
                                            }
                                        )
                                    }
                                },
                                standardHeight = 48.dp,
                                interactionSource = searchbarInteractionSource,
                                content = { }
                            )
                        },
                        scrollBehavior = scrollBehavior,
                        windowInsets = WindowInsets(
                            left = 0,
                            top = TopAppBarDefaults.windowInsets.getTop(density),
                            right = 0,
                            bottom = 0
                        ),
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Transparent,
                            scrolledContainerColor = MaterialTheme.customColorScheme.statusBarScrimColor
                        )
                    )
                },
                bottomBar = {
                    if (appSettings.clockGaugeVisibility) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { translationY = clockGaugeY }
                                .padding(bottom = 8.dp)
                        ) {
                            val dayGaugeResource = when (appSettings.theme) {
                                AppTheme.LIGHT -> R.drawable.day_gauge_light
                                AppTheme.DARK -> R.drawable.day_gauge_dark
                                AppTheme.SYSTEM -> {
                                    if (isSystemInDarkTheme()) R.drawable.day_gauge_dark
                                    else R.drawable.day_gauge_light
                                }
                            }
                            ClockManipulator(
                                gaugeSize = clockGaugeWidth,
                                dayGaugeResource = dayGaugeResource,
                                clockGaugeRotation = homeState.clockGaugeRotation.value,
                                clockGaugeNaturalRotation = homeState.clockGaugeNaturalRotation,
                                clockGaugeRotationCoefficient = clockGaugeRotationCoefficient,
                                dayListState = homeState.dayListState,
                                dayGaugeIndex = homeState.dayGaugeIndex,
                                dayGaugeNaturalIndex = homeState.dayGaugeNaturalIndex,
                                visibleDayIndex = homeState.visibleDayIndex,
                                clockGaugeLock = homeState.clockGaugeLock,
                                onUpdateVisibleDayIndex = remember { onUpdateVisibleDayIndex },
                                onUpdateDayGaugeIndex = remember { onUpdateDayGaugeIndex },
                                onHandleClickSound = remember { onHandleClickSound },
                                onUpdateClockGaugeRotation = remember { onUpdateClockGaugeRotation },
                                onStopRotation = remember {
                                    { coroutineScope.launch { homeState.clockGaugeRotation.stop() } }
                                },
                                onDecayRotation = { velocity ->
                                    coroutineScope.launch(Dispatchers.Default) {
                                        homeState.clockGaugeRotation.animateDecay(
                                            initialVelocity = (velocity * clockGaugeRotationCoefficient)
                                                .coerceIn(-360f, 360f),
                                            animationSpec = exponentialDecay(frictionMultiplier = 0.25f)
                                        )
                                    }
                                },
                                onUpdateClockGaugeLock = onUpdateClockGaugeLock,
                                onUndoButtonPressed = {
                                    onBalanceClockGaugeValues()
                                    coroutineScope.launch {
                                        homeState.clockGaugeRotation.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 700)
                                        )
                                    }
                                    onResetDayGauge()
                                }
                            )
                            Spacer(
                                Modifier.windowInsetsBottomHeight(
                                    WindowInsets.systemBars
                                )
                            )
                        }
                    }
                }
            ) { paddingValues ->
//            onEvaluateTopPaddingHeight(paddingValues.calculateTopPadding().toPx(density))
                when {
                    homeState.deleteDialogVisible -> {
                        DeleteAlertDialog(
                            onDismiss = {
                                onUpdateDialogVisibility(DialogType.DELETE, false)
                            },
                            onConfirmation = {
                                onUpdateToBeDeletedLocationId()
                                onUpdateDialogVisibility(DialogType.DELETE, false)
                                if (homeState.selectedLocationId == homeState.deleteDialogLocationId) {
                                    onUpdateHomePaneContent(HomePaneContent.LOCATIONLIST)
                                }
                                onPrepareDeleteLocation(homeState.deleteDialogLocationId)
                                val deletedPage = sortedLocationListState.indexOfFirst {
                                    it.locationId == homeState.deleteDialogLocationId
                                }
                                if (deletedPage >= 0)
                                    onModifyInitialPage(deletedPage)
                            },
                            locationName = homeState.deleteDialogLocationName
                        )
                    }

                    homeState.editDialogVisible -> {
                        EditDialog(
                            onDismiss = {
                                onUpdateDialogVisibility(DialogType.EDIT, false)
                            },
                            onConfirmation = {
                                if (homeState.editDialogOldLocationName != homeState.editDialogNewLocationName) {
                                    onUpdateLocation(
                                        homeState.editDialogLocationId,
                                        homeState.editDialogNewLocationName
                                    )
                                }
                                onUpdateDialogVisibility(DialogType.EDIT, false)
                            },
                            nameAlreadyExists = homeState.nameAlreadyExists and
                                    (homeState.editDialogOldLocationName != homeState.editDialogNewLocationName),
                            locationName = homeState.editDialogNewLocationName,
                            onLocationNameChange = {
                                onUpdateEditDialogLocationName(it)
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (displayedLocationListState.isNotEmpty() &&
                        displayedLocationListState.first().locationId != -2) {
                        val cardSpacing = dimensionResource(R.dimen.home_card_spacing)
                        val topSpacerHeight = paddingValues.calculateTopPadding()
                        val lazyColumnHeight by rememberUpdatedState( homeScreenHeight -
                                (topSpacerHeight + paddingValues.calculateBottomPadding() + cardSpacing)
                                    .toPx(density)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = cardSpacing,
                                end = cardSpacing,
//                            top = cardSpacing,
                                bottom = cardSpacing + paddingValues.calculateBottomPadding()
                            ),
                            state = listState,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(cardSpacing)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(topSpacerHeight))
                            }
                            items(
                                items = displayedLocationListState,
                                key = { item -> item.locationId }
                            ) { item ->
//                            val itemZoom = if (homeState.selectedLocationId == item.locationId) zoom else 1f
                                ReorderableItem(
                                    reorderableLazyListState,
                                    item.locationId
                                ) { isDragging ->
                                    val itemModifier = if (cardAlpha) {
                                        Modifier.graphicsLayer {
                                            alpha =
                                                if (homeState.selectedLocationId == item.locationId)
                                                    0f else 1f
                                        }
                                    } else {
                                        Modifier
                                            .graphicsLayer {
                                                alpha =
                                                    if (homeState.toBeDeletedLocationId == item.locationId)
                                                        0f else 1f
                                            }
                                            .longPressDraggableHandle(
                                                onDragStarted = {
                                                    sortedLocationListUpdatePermission = false
                                                },
                                                onDragStopped = {
                                                    sortedLocationListUpdatePermission = true
                                                }
                                            )
                                    }
                                    WeatherItem2(
//                                    zoom = 1f,
                                        cardMaxHeight = cardMaxHeight,
//                                    windowRotation = 0f,
                                        modifier = itemModifier,
                                        itemState = item,
                                        pageNumber = sortedLocationListState.indexOfFirst { locationState ->
                                            item.locationId == locationState.locationId
                                        },
                                        onNavigateToItem = remember {
                                            { cardX, cardY, cardWidth ->
                                                val itemOffset =
                                                    listState.layoutInfo.visibleItemsInfo.find {
                                                        it.key == item.locationId
                                                    }?.offset ?: 0

                                                val scrollAmount = when {
                                                    itemOffset < topSpacerHeight.toPx(density) ->
                                                        itemOffset - (topSpacerHeight + cardSpacing).toPx(density)

                                                    itemOffset + (cardMaxHeight - topSpacerHeight).toPx(density)
                                                            > lazyColumnHeight ->
                                                        itemOffset + (cardMaxHeight - topSpacerHeight).toPx(
                                                            density
                                                        ) - lazyColumnHeight

                                                    else -> 0f
                                                }

                                                if (singlePane) {
                                                    coroutineScope.launch {
                                                        animateScrolling.value = true
                                                        listState.animateScrollBy(
                                                            value = scrollAmount,
                                                            animationSpec = tween(
                                                                durationMillis = 300,
                                                                easing = CubicBezierEasing(0.5f, 0f, 0.9f, 0.3f)
                                                            )
                                                        )
                                                        val selectedItemIndex =
                                                            sortedLocationListState.indexOfFirst {
                                                                it.locationId == item.locationId
                                                            }
                                                        val selectedItemOffset =
                                                            listState.layoutInfo.visibleItemsInfo.find {
                                                                it.key == item.locationId
                                                            }?.offset ?: 0
                                                        onUpdateSelectedItemIndexOffset(
                                                            selectedItemIndex,
                                                            selectedItemOffset
                                                        )
                                                        selectedItem =
                                                            sortedLocationListState[selectedItemIndex]
                                                        selectedPage =
                                                            sortedLocationListState.indexOfFirst { locationState ->
                                                                selectedItem.locationId == locationState.locationId
                                                            }
                                                        onNavigateToItem(
                                                            item.locationId,
                                                            cardX,
                                                            cardY - scrollAmount,
                                                            cardWidth
                                                        )
                                                    }
                                                } else {
                                                    // Does this branch need onUpdateSelectedItemIndexOffset
                                                    onUpdateSelectedLocationId(item.locationId)
                                                    val selectedLocationPageNumber =
                                                        locationListState.indexOfFirst {
                                                            it.locationId == item.locationId
                                                        }
                                                    onNavigateToDetailScreen(
                                                        locationListState.size,
                                                        selectedLocationPageNumber
                                                    )
                                                }
                                            }
                                        },
                                        appSettings = appSettings,
                                        dataLoadingStatus = weatherDataLoadStatus.getOrElse(
                                            key = item.locationId,
                                            defaultValue = { MyResult.Success(Unit) }
                                        ),
                                        onDeleteIconPressed = remember {
                                            { id, locationName ->
                                                onUpdateDialogData(
                                                    DialogType.DELETE,
                                                    id,
                                                    locationName
                                                )
                                                onUpdateDialogVisibility(DialogType.DELETE, true)
                                            }
                                        },
                                        onEditItem = remember {
                                            { id, locationName ->
                                                onUpdateDialogData(
                                                    DialogType.EDIT,
                                                    id,
                                                    locationName
                                                )
                                                onUpdateEditDialogLocationName(locationName)
                                                onUpdateDialogVisibility(DialogType.EDIT, true)
                                            }
                                        },
                                        dialogDisplayed = homeState.deleteDialogVisible || homeState.editDialogVisible,
                                        toBeDeletedLocationId = homeState.toBeDeletedLocationId,
                                        onDeleteItem = remember {
                                            { locationId ->
                                                onDeleteLocation(
                                                    locationId
                                                )
                                            }
                                        },
                                        onReloadWeatherData = {
                                            onReloadALocationWeatherData(
                                                Location(
                                                    id = item.locationId,
                                                    name = item.locationName,
                                                    latitude = item.latitude,
                                                    longitude = item.longitude
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (zoom > 1f && displayedLocationListState.indexOfFirst {
                    it.locationName == selectedItem.locationName
                } > -1) {
                cardAlpha = true
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(width = homeState.cardWidth.toDp(density), height = cardMaxHeight)
                        .graphicsLayer {
                            scaleX = zoom
                            scaleY = zoom
                            translationX = itemTranslationX + homeState.selectedCardX
                            translationY =
                                itemTranslationY + homeState.selectedCardY/* + listBoxTopPadding*/
                        }
                ) {
                    WeatherItemFake(
                        windowRotation = windowRotation,
                        modifier = Modifier.fillMaxSize(),
                        itemState = selectedItem,
                        pageNumber = selectedPage,
                        appSettings = appSettings,
                    )
                }
            } else {
                cardAlpha = false
            }
            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

}



/*@Composable
private fun LazyListState.scrollVelocity(itemSize: Float): Float {
    var previousTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var previousScroll by remember(this) {
        mutableFloatStateOf(firstVisibleItemScrollOffset + firstVisibleItemIndex * itemSize)
    }

    val currentScroll = remember {
        derivedStateOf { firstVisibleItemScrollOffset  + firstVisibleItemIndex * itemSize }
    }.value
    val currentTime = remember(key1 = currentScroll) { System.currentTimeMillis() }
    return 1000f * (currentScroll - previousScroll) / (currentTime - previousTime)
        .also {
            previousScroll = currentScroll
            previousTime = currentTime
        }
}*/

@Composable
fun LazyListState.clockGaugeVisibility(itemSize: Float, animateScrolling: MutableState<Boolean>): Boolean {
    var visibility by remember { mutableStateOf(true) }
    var previousTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var previousScroll by remember(this) {
        mutableFloatStateOf(firstVisibleItemScrollOffset + firstVisibleItemIndex * itemSize)
    }
    LaunchedEffect(this) {
        snapshotFlow { firstVisibleItemScrollOffset to firstVisibleItemIndex }
            .collect { (scrollOffset, scrollIndex) ->
                val currentScroll =  scrollOffset  + scrollIndex * itemSize
                val currentTime = System.currentTimeMillis()
                val velocity = (1000f * (currentScroll - previousScroll) / (currentTime - previousTime))
                    .also {
                        previousScroll = currentScroll
                        previousTime = currentTime
                    }
                if (!animateScrolling.value) {
                    when {
                        velocity > 1100f -> visibility = false
                        velocity < -1100f -> visibility = true
                    }
                    if (!canScrollForward || !canScrollBackward) visibility = true
                }
            }
    }
    return visibility
}



/*

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(modifier: Modifier = Modifier) {
    WeathererTheme {
        HomeScreen(
            appSettings = AppSettings(),
            initialFirstItemIndex = 0,
            homeState = HomeState(),
            isZooming = false,
            homeScreenWidth = 800f,
            homeScreenHeight = 1800f,
//            listBoxTopPadding = 280f,
            itemTranslationX = 0f,
            itemTranslationY = 0f,
            zoom = 1f,
            zoomMax = 10f,
            windowRotation = 0f,
            onSwapLocations = {_, _ -> },
//            onEvaluateTopPaddingHeight = { _ -> },
            onMenuButtonClick = {},
            onNavigateToDetailScreen = {_, _ ->},
            onNavigateToAddLocationScreen = {},
            onPrepareDeleteLocation = {}

        )
    }
}*/
