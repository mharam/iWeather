package com.takaapoo.weatherer.ui.screens.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
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
import com.takaapoo.weatherer.ui.screens.home.dialogs.DeleteAlertDialog
import com.takaapoo.weatherer.ui.screens.home.dialogs.EditDialog
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.viewModels.DialogType
import com.takaapoo.weatherer.utility.swap
import com.takaapoo.weatherer.utility.toDp
import com.takaapoo.weatherer.utility.toPx
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.io.File
import kotlin.math.PI
import kotlin.math.sin

enum class DragValue { Left, Center, Right }
val DRAG_MAX_DISPLACE = 130.dp
val CardBackIconSize = 60.dp

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun HomeScreen(
    // temporary parameter
    modifier: Modifier = Modifier,
    locations: List<Location> = emptyList(),
    homeState: HomeState,
    locationListState: ImmutableList<LocationsState>,
    appSettings: AppSettings,
    weatherDataLoadStatus: Map<Int, MyResult<*>>,
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
//    sharedTransitionScope: SharedTransitionScope,
//    animatedVisibilityScope: AnimatedVisibilityScope,

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
    onMenuButtonClick: () -> Unit,
    onNavigateToDetailScreen: (locationsCount: Int, initialPageNumber: Int) -> Unit,
    onNavigateToAddLocationScreen: () -> Unit,
    onNavigateUp: () -> Unit = {},
    onPrepareDeleteLocation: (locationId: Int) -> Unit,
    onReloadALocationWeatherData: (location: Location) -> Unit,
    onUpdateClockGaugeLock: (lock: Boolean) -> Unit,
    onUpdateItemDragged: (state: Boolean) -> Unit,

    onDownloadAndSaveLandscapes: (context: Context, picId: Int, picUrl: String, picName: String?) -> Unit,
    onUpdateInitialLandscape: (bitmap: Bitmap) -> Unit,
    onReInitialize: () -> Unit = {},
    onAdjustSettings: () -> Unit = {},
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current

    var sortedLocationListState by remember { mutableStateOf(locationListState) }
    val displayedLocationListState = sortedLocationListState.filter {
        it.locationName.contains(
            other = homeState.filterText.trim(),
            ignoreCase = true
        )
    }
//    val updatedDisplayedLocationList by rememberUpdatedState(displayedLocationListState)
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
//    var topAppBarHeight by remember{ mutableStateOf(64.dp) }
    val animateScrolling = remember { mutableStateOf(false) }
    val clockGaugeHideOffset = (clockManipulatorHeight + 56.dp).toPx(density)
    val clockGaugeYAnimatable = remember { Animatable(0f) }

    LaunchedEffect(listState, homeState.clockGaugeLock) {
        var previousTime = System.currentTimeMillis()
        var previousScroll = listState.firstVisibleItemScrollOffset + listState.firstVisibleItemIndex * cardHeightPx
        var visibility = true

        snapshotFlow { listState.firstVisibleItemScrollOffset to listState.firstVisibleItemIndex }
            .collect { (scrollOffset, scrollIndex) ->
                val currentScroll = scrollOffset + scrollIndex * cardHeightPx
                val currentTime = System.currentTimeMillis()
                val deltaTime = currentTime - previousTime
                if (deltaTime > 0) {
                    val velocity = (1000f * (currentScroll - previousScroll) / deltaTime)
                    previousScroll = currentScroll
                    previousTime = currentTime

                    if (!animateScrolling.value) {
                        val newVisibility = when {
                            !listState.canScrollForward || !listState.canScrollBackward -> true
                            velocity > 1100f -> false
                            velocity < -1100f -> true
                            else -> visibility
                        }
                        if (newVisibility != visibility || homeState.clockGaugeLock) {
                            visibility = newVisibility
                            clockGaugeYAnimatable.animateTo(
                                targetValue = if (homeState.clockGaugeLock || visibility) 0f else clockGaugeHideOffset,
                                animationSpec = tween(durationMillis = 300)
                            )
                        }
                    }
                }
            }
    }

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
    val clockGaugeRotationCoefficient = 360 / (PI.toFloat() * clockGaugeWidth.toPx(density))

    var cardAlpha by rememberSaveable { mutableStateOf(zoom > 1f) }
//    var selectedItemIndex by remember { mutableIntStateOf(0) }
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
    val cardScale by animateFloatAsState(
        targetValue = if (sortedLocationListUpdatePermission) 1f else 1.04f,
        visibilityThreshold = 0.0005f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )

    LaunchedEffect(key1 = locationListState, key2 = sortedLocationListUpdatePermission) {
        if (sortedLocationListUpdatePermission)
            sortedLocationListState = locationListState
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
                    homeState.editNameResult.data as String
                )
                onResetEditNameResult()
            }
            is MyResult.Error -> {
                snackbarHostState.showSnackbar(
                    homeState.editNameResult.message as String
                )
                onResetEditNameResult()
            }
            else -> {}
        }
    }
    LaunchedEffect(key1 = true) {
        initialFirstItemIndex?.let { index ->
            locationListState.getOrNull(index)?.locationId?.let { id ->
                onUpdateSelectedLocationId(id)
                val offset = listState.layoutInfo.visibleItemsInfo.find {
                    it.key == locationListState[index].locationId
                }?.offset
                offset?.let {
                    onUpdateVerticalOffsetDifference(homeState.selectedItemOffset - it.toFloat())
                }
            }
        }
        onResetZoom()
    }


    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchbarInteractionSource = remember { MutableInteractionSource() }
    val searchbarIsFocused by searchbarInteractionSource.collectIsFocusedAsState()
    var landscapeReload by rememberSaveable { mutableIntStateOf(0) }

    PullToRefreshBox(
        modifier = modifier,
        state = pullRefreshState,
        isRefreshing = homeState.isRefreshing,
        onRefresh = {
            landscapeReload++
            onRefreshWeatherData()
        },
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = pullRefreshState,
                isRefreshing = homeState.isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, end = 16.dp),
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
//                                        with(sharedTransitionScope){
                                        SmallFloatingActionButton(
                                            onClick = {
                                                onNavigateToAddLocationScreen()
                                            },
//                                                modifier = Modifier.sharedBounds(
//                                                    sharedContentState = rememberSharedContentState(
//                                                        key = FAB_EXPLODE_KEY
//                                                    ),
//                                                    animatedVisibilityScope = animatedVisibilityScope
//                                                ),
                                            shape = CircleShape,
                                            content = {
                                                Icon(
                                                    painter = painterResource(R.drawable.add_location_24px),
                                                    contentDescription = "Add Location"
                                                )
                                            }
                                        )
//                                        }
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
                        colors = TopAppBarDefaults.topAppBarColors(
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
                                .graphicsLayer { translationY = clockGaugeYAnimatable.value }
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
                                silent = appSettings.silent,
                                vibration = appSettings.vibration,

                                onUpdateVisibleDayIndex = remember { onUpdateVisibleDayIndex },
                                onUpdateDayGaugeIndex = remember { onUpdateDayGaugeIndex },
//                                onHandleClickSound = remember { onHandleClickSound },
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
//                                    onUpdateHomePaneContent(HomePaneContent.LOCATIONLIST)
                                    onNavigateUp()
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
                        var longPressedItemId by remember { mutableIntStateOf(0) }
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
                                itemsIndexed(
                                    items = displayedLocationListState,
                                    key = { _, item -> item.locationId }
                                ) { index, item ->
                                    ReorderableItem(
                                        state = reorderableLazyListState,
                                        key = item.locationId,
                                    ) { isDragging ->
                                        val itemModifier = if (cardAlpha) {
                                            Modifier.graphicsLayer {
                                                alpha =
                                                    if (homeState.selectedLocationId == item.locationId)
                                                        0f else 1f
                                            }
                                        } else {
                                            Modifier
                                                .longPressDraggableHandle(
                                                    enabled = homeState.itemDragged,
                                                    onDragStarted = {
                                                        longPressedItemId = item.locationId
                                                        hapticFeedback.performHapticFeedback(
                                                            HapticFeedbackType.LongPress
                                                        )
                                                        Toast.makeText(
                                                            context,
                                                            locations[index].lastModifiedTime,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        sortedLocationListUpdatePermission = false
                                                    },
                                                    onDragStopped = {
                                                        sortedLocationListUpdatePermission = true
                                                    }
                                                )
                                                .graphicsLayer {
                                                    alpha =
                                                        if (homeState.toBeDeletedLocationId == item.locationId)
                                                            0f else 1f
                                                    scaleX =
                                                        if (longPressedItemId == item.locationId) cardScale else 1f
                                                    scaleY =
                                                        if (longPressedItemId == item.locationId) cardScale else 1f
                                                }
                                        }
                                        WeatherItem2(
                                            cardMaxHeight = cardMaxHeight,
                                            modifier = itemModifier,
                                            itemState = item,
                                            landscapeReload = landscapeReload,
                                            onNavigateToItem = remember(
//                                                listState.layoutInfo.visibleItemsInfo,

                                            ) {
                                                { cardX, cardY, cardWidth ->
                                                    val itemOffset =
                                                        listState.layoutInfo.visibleItemsInfo.find {
                                                            it.key == item.locationId
                                                        }?.offset ?: 0

                                                    val scrollAmount = when {
                                                        itemOffset < topSpacerHeight.toPx(density) ->
                                                            itemOffset - (topSpacerHeight + cardSpacing).toPx(
                                                                density
                                                            )

                                                        itemOffset + (cardMaxHeight - topSpacerHeight).toPx(
                                                            density
                                                        ) > lazyColumnHeight ->
                                                            itemOffset + (cardMaxHeight - topSpacerHeight).toPx(
                                                                density
                                                            ) - lazyColumnHeight

                                                        else -> 0f
                                                    }
                                                    navigateToHomeScreenItem(
                                                        coroutineScope = coroutineScope,
                                                        singlePane = singlePane,
                                                        sortedLocationListState = sortedLocationListState,
                                                        homeState = homeState,
                                                        itemState = displayedLocationListState[index],
                                                        file = context.filesDir,

                                                        onAnimateScrollList = {
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
                                                        },
                                                        onUpdateAnimateScrolling = {
                                                            animateScrolling.value = it
                                                        },
                                                        onBalanceClockGaugeValues = onBalanceClockGaugeValues,
                                                        onResetDayGauge = onResetDayGauge,
                                                        onUpdateSelectedItem = {
                                                            selectedItem = it
                                                        },
                                                        onUpdateSelectedPage = {
                                                            selectedPage = it
                                                        },
                                                        onNavigateToItem = {
                                                            onNavigateToItem(
                                                                item.locationId,
                                                                cardX,
                                                                cardY - scrollAmount,
                                                                cardWidth
                                                            )
                                                        },
                                                        onUpdateSelectedLocationId = {
                                                            onUpdateSelectedLocationId(item.locationId)
                                                        },
                                                        onUpdateInitialLandscape = onUpdateInitialLandscape,
                                                        onNavigateToDetailScreen = {
                                                            onNavigateToDetailScreen(
                                                                locationListState.size,
                                                                locationListState.indexOfFirst {
                                                                    it.locationId == item.locationId
                                                                }
                                                            )
                                                        }
                                                    )


                                                    /*if (singlePane) {
                                                        coroutineScope.launch {
                                                            val scrollJob = launch {
                                                                animateScrolling.value = true
                                                                listState.animateScrollBy(
                                                                    value = scrollAmount,
                                                                    animationSpec = tween(
                                                                        durationMillis = 300,
                                                                        easing = CubicBezierEasing(
                                                                            0.5f,
                                                                            0f,
                                                                            0.9f,
                                                                            0.3f
                                                                        )
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

                                                            }
                                                            val gaugeRotateJob = launch {
                                                                onBalanceClockGaugeValues()
                                                                homeState.clockGaugeRotation.animateTo(
                                                                    targetValue = 0f,
                                                                    animationSpec = tween(durationMillis = 700)
                                                                )
                                                                onResetDayGauge()
                                                                delay(200)
                                                            }

                                                            joinAll(scrollJob, gaugeRotateJob)
                                                            selectedItem = displayedLocationListState[index]
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
                                                    }
                                                    else {
                                                        // Does this branch need onUpdateSelectedItemIndexOffset
                                                        onUpdateSelectedLocationId(item.locationId)
                                                        val selectedLocationPageNumber =
                                                            locationListState.indexOfFirst {
                                                                it.locationId == item.locationId
                                                            }
                                                        coroutineScope.launch {
                                                            onBalanceClockGaugeValues()
                                                            homeState.clockGaugeRotation.animateTo(
                                                                targetValue = 0f,
                                                                animationSpec = tween(durationMillis = 700)
                                                            )
                                                            onResetDayGauge()

                                                            val itemState = displayedLocationListState[index]
                                                            val picFile = File(
                                                                context.filesDir,
                                                                "pictures/pic${itemState.lastPicId}/${itemState.picName}.jpeg"
                                                            )
                                                            if (picFile.exists()) {
                                                                val bitmap =
                                                                    BitmapFactory.decodeFile(picFile.absolutePath)
                                                                onUpdateInitialLandscape(
                                                                    bitmap.copy(Bitmap.Config.ARGB_8888, false)
                                                                )
                                                            }
                                                            onNavigateToDetailScreen(
                                                                locationListState.size,
                                                                selectedLocationPageNumber
                                                            )
                                                        }
                                                    }*/
                                                }
                                            }
                                            ,
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
                                                    onUpdateDialogVisibility(
                                                        DialogType.DELETE,
                                                        true
                                                    )
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
                                                { locationId, picId ->
                                                    onDeleteLocation(locationId)
                                                    File(context.filesDir, "pictures/pic${picId}").deleteRecursively()
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
                                            },
                                            onDownloadAndSaveLandscapes = { picName ->
                                                item.lastPicId?.let { picId ->
                                                    onDownloadAndSaveLandscapes(
                                                        context,
                                                        picId,
                                                        item.currentPictureUrl,
                                                        picName
                                                    )
                                                }
                                            },
                                            onUpdateItemDragged = onUpdateItemDragged
                                        )
                                    }
                                }
                            }

                    }
                }
            }
            if (zoom > 1f && displayedLocationListState.indexOfFirst {
                    it.locationName == selectedItem.locationName } > -1)
            {
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
                        appSettings = appSettings,
                        onUpdateInitialLandscape = onUpdateInitialLandscape
                    )
                }
            } else {
                cardAlpha = false
            }
            /*PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )*/
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
//        onReInitialize()
        onAdjustSettings()
    }
}


/*@Composable
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
}*/


fun navigateToHomeScreenItem(
    coroutineScope: CoroutineScope,
    singlePane: Boolean,
    sortedLocationListState: ImmutableList<LocationsState>,
    homeState: HomeState,
    itemState: LocationsState,
    file: File,

    onAnimateScrollList: suspend (() -> Unit),
    onUpdateAnimateScrolling: (Boolean) -> Unit,
    onBalanceClockGaugeValues: () -> Unit,
    onResetDayGauge: () -> Unit,
    onUpdateSelectedItem: (LocationsState) -> Unit,
    onUpdateSelectedPage: (page: Int) -> Unit,
    onNavigateToItem: () -> Unit,
    onUpdateSelectedLocationId: () -> Unit,
    onUpdateInitialLandscape: (bitmap: Bitmap) -> Unit,
    onNavigateToDetailScreen: () -> Unit
) {
    if (singlePane) {
        coroutineScope.launch {
            val scrollJob = launch {
                onUpdateAnimateScrolling(true)
                onAnimateScrollList()
            }
            val gaugeRotateJob = launch {
                onBalanceClockGaugeValues()
                homeState.clockGaugeRotation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 700)
                )
                onResetDayGauge()
                delay(200)
            }

            joinAll(scrollJob, gaugeRotateJob)
            onUpdateSelectedItem(itemState)
            onUpdateSelectedPage(
                sortedLocationListState.indexOfFirst { locationState ->
                    itemState.locationId == locationState.locationId
                }
            )
            onNavigateToItem()
        }
    }
    else {
        // Does this branch need onUpdateSelectedItemIndexOffset
        onUpdateSelectedLocationId()
        coroutineScope.launch {
            onBalanceClockGaugeValues()
            homeState.clockGaugeRotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 700)
            )
            onResetDayGauge()

            val picFile = File(
                file,
                "pictures/pic${itemState.lastPicId}/${itemState.picName}.jpeg"
            )
            if (picFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(picFile.absolutePath)
                onUpdateInitialLandscape(
                    bitmap.copy(Bitmap.Config.ARGB_8888, false)
                )
            }
            onNavigateToDetailScreen()
        }
    }
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
