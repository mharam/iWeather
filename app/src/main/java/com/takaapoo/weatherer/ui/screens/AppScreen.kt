package com.takaapoo.weatherer.ui.screens

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.os.Build
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerDefaults.scrimColor
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.updateAll
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.navigation3.runtime.NavKey
import androidx.window.core.layout.WindowSizeClass
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.WEATHER_CHANNEL_ID
import com.takaapoo.weatherer.WEATHER_NOTIFICATION_ID
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.LocationsState
import com.takaapoo.weatherer.ui.navigation.AboutUs
import com.takaapoo.weatherer.ui.navigation.AddLocation
import com.takaapoo.weatherer.ui.navigation.ContactUs
import com.takaapoo.weatherer.ui.navigation.Donate
import com.takaapoo.weatherer.ui.navigation.Home
import com.takaapoo.weatherer.ui.navigation.Navigator
import com.takaapoo.weatherer.ui.navigation.Radar
import com.takaapoo.weatherer.ui.navigation.RateUs
import com.takaapoo.weatherer.ui.navigation.Settings
import com.takaapoo.weatherer.ui.navigation.Start
import com.takaapoo.weatherer.ui.navigation.WeatherNavGraph
import com.takaapoo.weatherer.ui.navigation.rememberNavigationState
import com.takaapoo.weatherer.ui.notification.NotificationSender
import com.takaapoo.weatherer.ui.notification.NotificationSender.Companion.headerPendingIntent
import com.takaapoo.weatherer.ui.theme.TransparentBlack
import com.takaapoo.weatherer.ui.viewModels.AddLocationViewModel
import com.takaapoo.weatherer.ui.viewModels.AppViewModel
import com.takaapoo.weatherer.ui.viewModels.HomeViewModel
import com.takaapoo.weatherer.ui.viewModels.PreferenceViewModel
import com.takaapoo.weatherer.ui.widget.WeatherWidget1
import com.takaapoo.weatherer.ui.widget.WeatherWidget2
import com.takaapoo.weatherer.utility.toPx
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs


data class DrawerNavigationItem(
    val title: String,
    val screen: NavKey,
    @param:DrawableRes val selectedIcon: Int,
    @param:DrawableRes val unSelectedIcon: Int
)
val screenItems = listOf(
    DrawerNavigationItem(
        title = "Home",
        screen = Home,
        selectedIcon = R.drawable.home_filled_24px,
        unSelectedIcon = R.drawable.home_24px
    ),
    DrawerNavigationItem(
        title = "Radar",
        screen = Radar,
        selectedIcon = R.drawable.radar_24px,
        unSelectedIcon = R.drawable.radar_24px
    ),
    DrawerNavigationItem(
        title = "Settings",
        screen = Settings,
        selectedIcon = R.drawable.settings_filled_24px,
        unSelectedIcon = R.drawable.settings_24px
    ),
    DrawerNavigationItem(
        title = "Donate",
        screen = Donate,
        selectedIcon = R.drawable.donate_fill,
        unSelectedIcon = R.drawable.donate
    ),
    DrawerNavigationItem(
        title = "Contact us",
        screen = ContactUs,
        selectedIcon = R.drawable.mail_fill_24px,
        unSelectedIcon = R.drawable.mail_24px
    ),
    DrawerNavigationItem(
        title = "About us",
        screen = AboutUs,
        selectedIcon = R.drawable.info_fill_24px,
        unSelectedIcon = R.drawable.info_24px
    ),
    DrawerNavigationItem(
        title = "Rate us",
        screen = RateUs,
        selectedIcon = R.drawable.star_rate_24px,
        unSelectedIcon = R.drawable.star_rate_24px
    ),
)


@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    appSettings: AppSettings,
    locationListState: State<List<LocationsState>>,
    homeViewModel: HomeViewModel,
    addLocationViewModel: AddLocationViewModel,
    appViewModel: AppViewModel = hiltViewModel<AppViewModel>(
        viewModelStoreOwner = rememberViewModelStoreOwner()
    ),
    preferenceViewModel: PreferenceViewModel,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
) {
    val locationsBriefState by homeViewModel.locationsBriefState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val density = LocalDensity.current
    val mainActivity = LocalActivity.current as MainActivity
    val drawerWidthPx = dimensionResource(R.dimen.drawer_width).toPx(density)
    val navigationState = rememberNavigationState(
        startRoute = Home,
        topLevelRoutes = setOf(Start, Home, Settings, Donate, AddLocation, Radar)
    )
    val navigator = remember { Navigator(navigationState) }

    if (locationsBriefState.isEmpty()) {
        if (navigationState.topLevelRoute != AddLocation)
            navigationState.updateStacksInUse(newStartRoute = Start)
    } else if (navigationState.topLevelRoute == AddLocation && navigationState.startRoute != Home) {
        navigationState.updateStacksInUse(
            newStartRoute = Home,
            newTopLevelRoute = AddLocation
        )
    }

    val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
    val drawerOffset = remember { Animatable(-drawerWidthPx) }
    val scrimBoxVisible by remember {
        derivedStateOf { drawerOffset.value > -drawerWidthPx }
    }

    val scope = rememberCoroutineScope()
//    val donateViewModel = hiltViewModel<DonateViewModel>()
//    val donateState by donateViewModel.donateState.collectAsStateWithLifecycle()

    val notificationHourlyChartData by appViewModel.notificationHourlyChartData.collectAsStateWithLifecycle()
    val notificationDailyChartData by appViewModel.notificationDailyChartData.collectAsStateWithLifecycle()
    val locationsCount by addLocationViewModel.locationsCount.collectAsStateWithLifecycle()

    val locationIDs = locationsBriefState.map { it.locationId }
    LaunchedEffect(key1 = locationIDs) {
        if (locationsBriefState.firstOrNull()?.locationId != -2) {
            if (locationsBriefState.isEmpty()) {
                preferenceViewModel.updateNotificationLocationId(null)
                preferenceViewModel.updateNotificationVisibility(false)
                preferenceViewModel.updateWidgetLocationId(null)
            } else {
                if (appSettings.notificationLocationId == null ||
                    locationsBriefState.find { it.locationId == appSettings.notificationLocationId } == null
                ) {
                    preferenceViewModel.updateNotificationLocationId(
                        locationsBriefState.first().locationId
                    )
                }
                if (appSettings.widgetLocationId == null ||
                    locationsBriefState.find { it.locationId == appSettings.widgetLocationId } == null
                ) {
                    preferenceViewModel.updateWidgetLocationId(
                        locationsBriefState.first().locationId
                    )
                }
            }
        }
    }

    when {
        windowSizeClass.isWidthAtLeastBreakpoint(
            WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
        ) ->  {
            val modalDrawer by remember { derivedStateOf { mainActivity.windowWidth < 1000.dp.toPx(density) } }
            if (modalDrawer) {
                MyNavigationDrawer(
                    modifier = modifier,
                    enabled = locationsCount > 0,
                    drawerOffset = drawerOffset,
                    drawerWidthPx = drawerWidthPx,
                    drawerContent = {
                        WeatherDrawerContent(
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = if (drawerOffset.value > -drawerWidthPx + 1) 1f
                                    else 0f
                                }
                                .fillMaxSize()
                            /*.graphicsLayer {
                                val scale = lerp(1f, 1.1f, abs(drawerOffset.value)/drawerWidthPx)
                                translationX = drawerOffset.value
                                transformOrigin = TransformOrigin(1f, 0.5f)
                                scaleX = scale
                                scaleY = scale
                            }
                            .blur(
                                radius = lerp(0.dp, 8.dp, abs(drawerOffset.value)/drawerWidthPx)
                            )*/,
                            navigationItems = screenItems,
                            selectedItem = navigationState.topLevelRoute,
                            onSelectItem = {
                                navigator.navigate(it)
                                scope.launch {
//                            drawerState.close()
                                    drawerOffset.animateTo(-drawerWidthPx)
                                }
                            }
                        )
                    },
                    content = { drawerOpen ->

                        Box(modifier = Modifier.fillMaxSize()
                            .graphicsLayer {
                                val scale = lerp(0.8f, 1f, abs(drawerOffset.value)/drawerWidthPx)
                                transformOrigin = TransformOrigin(0f, 0.5f)
                                cameraDistance = 30f
                                scaleX = scale
                                scaleY = scale
//                            rotationY = lerp(-20f, 0f, abs(drawerOffset.value)/drawerWidthPx)
                                translationX = (drawerOffset.value + drawerWidthPx) / 1
                                clip = true
                                shape = RoundedCornerShape(if (drawerOffset.value > -drawerWidthPx) 16.dp else 0.dp)
                                shadowElevation = 4.dp.toPx()
                            }
                            .background(MaterialTheme.colorScheme.background)
//                        .blur(
//                            radius = lerp(8.dp, 0.dp, abs(drawerOffset.value)/drawerWidthPx)
//                        )
                        ) {
                            WeatherNavGraph(
//                                navController = navController,
                                navigator = navigator,
                                navigationState = navigationState,
                                appViewModel = appViewModel,
                                homeViewModel = homeViewModel,
                                addLocationViewModel = addLocationViewModel,
                                preferenceViewModel = preferenceViewModel,
//                                donateViewModel = donateViewModel,
//                                donateState = donateState,
                                appSettings = appSettings,
                                locationListState = locationListState,

                                singlePane = false,
                                permanentDrawer = false,
                                drawerOpen = drawerOpen,
                                onMenuButtonClick = {
                                    scope.launch {
//                                drawerState.open()
                                        drawerOffset.animateTo(0f)
                                    }
                                }
                            )
                            if (scrimBoxVisible) {
                                val finalScrimColor = scrimColor
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            colorFilter = ColorFilter.tint(
                                                color = lerp(
                                                    start = TransparentBlack,
                                                    stop = finalScrimColor,
                                                    fraction = (drawerOffset.value + drawerWidthPx) / drawerWidthPx,
                                                ),
                                                blendMode = BlendMode.Src
                                            )
                                        }
                                        .background(Color.White)
                                        .clickable(
                                            interactionSource = null,
                                            indication = null,
                                            onClick = {
                                                scope.launch {
                                                    drawerOffset.animateTo(-drawerWidthPx)
                                                }
                                            }
                                        )
                                ) {}
                            }
                        }
                    }
                )
            } else {
                PermanentNavigationDrawer(
                    modifier = Modifier.width(dimensionResource(R.dimen.drawer_width)),
                    drawerContent = {
                        WeatherDrawerContent(
                            navigationItems = screenItems,
                            selectedItem = navigationState.topLevelRoute,
                            onSelectItem = {
                                if (it !is Settings || locationsCount > 0) {
                                    navigator.navigate(it)
                                }
                            },
                            modifier = Modifier
                                .width(dimensionResource(R.dimen.drawer_width))
                                .fillMaxHeight()
                        )
                    }
                ) {
                    WeatherNavGraph(
//                        navController = navController,
                        navigator = navigator,
                        navigationState = navigationState,
                        appViewModel = appViewModel,
                        homeViewModel = homeViewModel,
                        addLocationViewModel = addLocationViewModel,
                        preferenceViewModel = preferenceViewModel,
//                        donateViewModel = donateViewModel,
//                        donateState = donateState,
                        appSettings = appSettings,
                        locationListState = locationListState,
//                        appUiState = uiState,
                        modifier = modifier.fillMaxSize(),
                        singlePane = false,
                        permanentDrawer = true,
                        drawerOpen = false,
                    )
                }
            }

//            NavigationRail {
//
//            }
        }
        else -> {
            val singlePane by remember { derivedStateOf { mainActivity.windowWidth < 720.dp.toPx(density) } }
            MyNavigationDrawer(
                modifier = modifier,
                enabled = locationsCount > 0,
                drawerOffset = drawerOffset,
                drawerWidthPx = drawerWidthPx,
                drawerContent = {
                    WeatherDrawerContent(
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = if (drawerOffset.value > -drawerWidthPx + 1) 1f
                                else 0f
                            }
                            .fillMaxSize()
                        /*.graphicsLayer {
                            val scale = lerp(1f, 1.1f, abs(drawerOffset.value)/drawerWidthPx)
                            translationX = drawerOffset.value
                            transformOrigin = TransformOrigin(1f, 0.5f)
                            scaleX = scale
                            scaleY = scale
                        }
                        .blur(
                            radius = lerp(0.dp, 8.dp, abs(drawerOffset.value)/drawerWidthPx)
                        )*/,
                        navigationItems = screenItems,
                        selectedItem = navigationState.topLevelRoute,
                        onSelectItem = {
                            navigator.navigate(it)
                            scope.launch {
//                            drawerState.close()
                                drawerOffset.animateTo(-drawerWidthPx)
                            }
                        }
                    )
                },
                content = { drawerOpen ->
                    Box(modifier = Modifier.fillMaxSize()
                        .graphicsLayer {
                            val scale = lerp(0.8f, 1f, abs(drawerOffset.value)/drawerWidthPx)
                            transformOrigin = TransformOrigin(0f, 0.5f)
                            cameraDistance = 30f
                            scaleX = scale
                            scaleY = scale
//                            rotationY = lerp(-20f, 0f, abs(drawerOffset.value)/drawerWidthPx)
                            translationX = (drawerOffset.value + drawerWidthPx) / 1
                            clip = true
                            shape = RoundedCornerShape(if (drawerOffset.value > -drawerWidthPx) 16.dp else 0.dp)
                            shadowElevation = 4.dp.toPx()
                        }
                        .background(MaterialTheme.colorScheme.background)
//                        .blur(
//                            radius = lerp(8.dp, 0.dp, abs(drawerOffset.value)/drawerWidthPx)
//                        )
                    ) {
                        WeatherNavGraph(
//                            navController = navController,
                            navigator = navigator,
                            navigationState = navigationState,
                            appViewModel = appViewModel,
                            homeViewModel = homeViewModel,
                            addLocationViewModel = addLocationViewModel,
                            preferenceViewModel = preferenceViewModel,
//                            donateViewModel = donateViewModel,
//                            donateState = donateState,
                            appSettings = appSettings,
                            locationListState = locationListState,

                            singlePane = singlePane,
                            permanentDrawer = false,
                            drawerOpen = drawerOpen,
                            onMenuButtonClick = {
                                scope.launch {
//                                drawerState.open()
                                    drawerOffset.animateTo(0f)
                                }
                            }
                        )
                        if (scrimBoxVisible) {
                            val finalScrimColor = scrimColor
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        colorFilter = ColorFilter.tint(
                                            color = lerp(
                                                start = TransparentBlack,
                                                stop = finalScrimColor,
                                                fraction = (drawerOffset.value + drawerWidthPx) / drawerWidthPx,
                                            ),
                                            blendMode = BlendMode.Src
                                        )
                                    }
                                    .background(Color.White)
                                    .clickable(
                                        interactionSource = null,
                                        indication = null,
                                        onClick = {
                                            scope.launch {
                                                drawerOffset.animateTo(-drawerWidthPx)
                                            }
                                        }
                                    )
                            ) {}
                        }
                    }
                }
            )


            /*ModalNavigationDrawer(
                modifier = Modifier.width(dimensionResource(R.dimen.drawer_width)),
                drawerState = drawerState,
                gesturesEnabled = drawerState.isOpen,
                drawerContent = {
                    WeatherDrawerContent(
                        navigationScreenItems = screenItems,
                        selectedItem = uiState.currentScreen,
                        onSelectItem = {
                            val screenAlreadyVisited = isDestinationInBackStack(navController, it)
                            appViewModel.updateScreen(screen = uiState.currentScreen)
                            if (screenAlreadyVisited)
                                navController.popBackStack(route = it, inclusive = false)
                            else
                                navController.navigate(route = it)

                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )
                }
            ) {
                val drawerWidthPx = dimensionResource(R.dimen.drawer_width).toPx(density)
                WeatherNavGraph(
                    navController = navController,
                    appViewModel = appViewModel,
                    homeViewModel = homeViewModel,
                    addLocationViewModel = addLocationViewModel,
                    preferenceViewModel = preferenceViewModel,
                    donateViewModel = donateViewModel,
                    appSettings = appSettings,
//                    appUiState = uiState,
                    modifier = modifier.fillMaxSize().blur(
                        radius = ((drawerState.currentOffset + drawerWidthPx) * 8 / drawerWidthPx).toDp(density)
                    ),
                    singlePane = singlePane,
                    permanentDrawer = false,
                    onMenuButtonClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            }*/
        }
    }

    var notificationDisplayed by rememberSaveable { mutableStateOf(appSettings.notificationVisibility) }
    val notificationManager = NotificationManagerCompat.from(mainActivity)
    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager


    if (appSettings.notificationVisibility){
        when {
            !notificationManager.areNotificationsEnabled() -> {
                mainActivity.requestNotificationPermission()
            }
            Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms() -> {
                ExactAlarmDialog(
                    onConfirmation = { mainActivity.goToAlarmSettings() },
                    onDismiss = { preferenceViewModel.updateNotificationVisibility(false) }
                )
            }
            Build.VERSION.SDK_INT >= 26 &&
                    notificationManager.getNotificationChannel(WEATHER_CHANNEL_ID)?.importance ==
                    NotificationManager.IMPORTANCE_NONE -> {
                appViewModel.updateNotifChannelDialogVisibility(true)
            }
            !notificationDisplayed && notificationHourlyChartData.isNotEmpty() -> {
                NotificationSender(
                    context = context,
                    header = appSettings.lastNotificationHeaderSelected,
                    notificationHourlyChartData = notificationHourlyChartData,
                    notificationDailyChartData = notificationDailyChartData,
                    appSettings = appSettings,
                    hourlyChartQuantities = appSettings.notificationHourlyQuantities,
                    currentQuantities = appSettings.notificationCurrentQuantities
                ).sendNotification()
                setNotificationAlarm(context)
                notificationDisplayed = true
            }
        }
    } else /*if (notificationDisplayed)*/ {
        resetNotificationAlarm(context)
        notificationManager.cancel(WEATHER_NOTIFICATION_ID)
        notificationDisplayed = false
    }

    if (uiState.notifChannelDialogVisibility){
        @SuppressLint("NewApi")
        NotificationChannelAlertDialog(
            onConfirmation = {
                mainActivity.goToNotificationSettings()
                appViewModel.updateNotifChannelDialogVisibility(false)
            },
            onDismiss = {
                preferenceViewModel.updateNotificationVisibility(false)
                appViewModel.updateNotifChannelDialogVisibility(false)
            }
        )
    }
}


@Composable
fun NotificationChannelAlertDialog(
    modifier: Modifier = Modifier,
    onConfirmation: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirmation){
                Text(
                    text = "Activate",
                    fontFamily = FontFamily.Default
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss){
                Text(
                    text = "Cancel",
                    fontFamily = FontFamily.Default
                )
            }
        },
        icon = {
            Icon(
                painter = painterResource(R.drawable.notifications_off),
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(R.string.notification_channel_disable_dialog_title))
        },
        text = {
            Text(
                text = stringResource(R.string.notification_channel_disable_dialog_text),
                fontFamily = FontFamily.Default
            )
        }
    )
}

@Composable
fun ExactAlarmDialog(
    modifier: Modifier = Modifier,
    onConfirmation: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirmation){
                Text(
                    text = "Allow",
                    fontFamily = FontFamily.Default
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss){
                Text(
                    text = "Cancel",
                    fontFamily = FontFamily.Default
                )
            }
        },
        icon = {
            Icon(
                painter = painterResource(R.drawable.alarm_24px),
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(R.string.notification_alarm_permission_dialog_title))
        },
        text = {
            Text(
                text = stringResource(R.string.notification_alarm_permission_dialog_text),
                fontFamily = FontFamily.Default
            )
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}


@Composable
fun WeatherDrawerContent(
    navigationItems: List<DrawerNavigationItem>,
    selectedItem: NavKey,
    onSelectItem: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet (
        modifier = modifier/*width(dimensionResource(R.dimen.drawer_width)).fillMaxHeight()*/
    ) {
        Text("Drawer title", modifier = Modifier.padding(16.dp))
        HorizontalDivider()
        navigationItems.forEachIndexed { index, navigationItem ->
            if (index == 4)
                HorizontalDivider()

            val isSelected = navigationItem.screen == selectedItem
            NavigationDrawerItem(
                selected = isSelected,
                onClick = { onSelectItem(navigationItem.screen) },
                icon = {
                    Icon(
                        painter = painterResource(if (isSelected) navigationItem.selectedIcon
                        else navigationItem.unSelectedIcon),
                        contentDescription = navigationItem.title
                    )
                },
                label = {
                    Text(
                        text = navigationItem.title,
                        fontFamily = FontFamily.Default
                    )
                }
            )
        }
    }
}

@Composable
fun MyNavigationDrawer(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    drawerOffset: Animatable<Float, AnimationVector1D>,
    drawerWidthPx: Float,
//    scrimColor: Color = DrawerDefaults.scrimColor,
    drawerContent: @Composable (() -> Unit),
    content: @Composable ((drawerOpen: Boolean) -> Unit),
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var firstDrawerOffset by remember { mutableFloatStateOf(-drawerWidthPx) }
    var firstTouchPoint by remember { mutableStateOf(Offset.Infinite) }
    val edgeTouchWidth = 32.dp.toPx(density)
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    val drawerOpen by remember {
        derivedStateOf { drawerOffset.value > -5f }
    }

    Box(
        modifier = modifier.fillMaxSize()
            .pointerInput(enabled) {
                awaitPointerEventScope {
                    while (enabled) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        when (event.motionEvent?.actionMasked){
                            MotionEvent.ACTION_DOWN -> {
                                firstTouchPoint = event.changes.first().position
                                firstDrawerOffset = drawerOffset.value
                            }
                            MotionEvent.ACTION_UP -> {
                                coroutineScope.launch {
                                    if (drawerOffset.value < -drawerWidthPx / 2)
                                        drawerOffset.animateTo(-drawerWidthPx)
                                    else
                                        drawerOffset.animateTo(0f)
                                }
                                firstTouchPoint = Offset.Infinite
                            }
                        }
                        if (firstTouchPoint.x < edgeTouchWidth &&
                            firstTouchPoint.y > statusBarHeight + 64.dp.toPx()) {
                            val delta = event.changes.first().positionChange().x
                            event.changes.forEach { it.consume() }
                            coroutineScope.launch {
                                if (!drawerOffset.isRunning) {
                                    drawerOffset.snapTo(
                                        targetValue = (drawerOffset.value + delta).coerceIn(
                                            minimumValue = -drawerWidthPx,
                                            maximumValue = 0f
                                        )
                                    )
                                }
                            }
                        } else if (firstDrawerOffset == 0f) {
                            val delta = event.changes.first().positionChange().x
                            if (drawerOffset.value < 0f)
                                event.changes.forEach { it.consume() }

                            coroutineScope.launch {
                                if (!drawerOffset.isRunning) {
                                    drawerOffset.snapTo(
                                        targetValue = (drawerOffset.value + delta).coerceIn(
                                            minimumValue = -drawerWidthPx,
                                            maximumValue = 0f
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
    ) {
        drawerContent()
        content(drawerOpen)


        BackHandler(enabled = drawerOpen) {
            coroutineScope.launch {
                drawerOffset.animateTo(-drawerWidthPx)
            }
        }
    }
}

fun setNotificationAlarm(context: Context){
    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
    val headerPendingIntent = headerPendingIntent(context, -1)
    alarmManager.cancel(headerPendingIntent)

    val calendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        add(Calendar.HOUR_OF_DAY, 1)
    }
    if (Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms())
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            headerPendingIntent
        )
    else
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            headerPendingIntent
        )
}

fun resetNotificationAlarm(context: Context){
    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
    val headerPendingIntent = headerPendingIntent(context, -1)
    alarmManager.cancel(headerPendingIntent)
}

suspend fun updateAllWidgets(context: Context) {
    WeatherWidget1().updateAll(context)
    WeatherWidget2().updateAll(context)
}


