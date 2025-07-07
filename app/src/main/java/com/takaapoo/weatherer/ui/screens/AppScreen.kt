package com.takaapoo.weatherer.ui.screens

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.updateAll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.WEATHER_CHANNEL_ID
import com.takaapoo.weatherer.WEATHER_NOTIFICATION_ID
import com.takaapoo.weatherer.data.Donate
import com.takaapoo.weatherer.data.History
import com.takaapoo.weatherer.data.HomePane
import com.takaapoo.weatherer.data.Radar
import com.takaapoo.weatherer.data.Screen
import com.takaapoo.weatherer.data.Settings
import com.takaapoo.weatherer.ui.navigation.WeatherNavGraph
import com.takaapoo.weatherer.ui.notification.NotificationSender
import com.takaapoo.weatherer.ui.notification.NotificationSender.Companion.headerPendingIntent
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.viewModels.AddLocationViewModel
import com.takaapoo.weatherer.ui.viewModels.AppViewModel
import com.takaapoo.weatherer.ui.viewModels.DonateViewModel
import com.takaapoo.weatherer.ui.viewModels.HomeViewModel
import com.takaapoo.weatherer.ui.viewModels.PreferenceViewModel
import com.takaapoo.weatherer.ui.widget.WeatherWidget1
import com.takaapoo.weatherer.ui.widget.WeatherWidget2
import com.takaapoo.weatherer.utility.isDestinationInBackStack
import com.takaapoo.weatherer.utility.toPx
import kotlinx.coroutines.launch
import java.util.Calendar


data class DrawerNavigationItem(
    val title: String,
    val screen: Screen,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unSelectedIcon: Int
)
val screenItems = listOf(
    DrawerNavigationItem(
        title = "Home",
        screen = HomePane,
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
        title = "History",
        screen = History,
        selectedIcon = R.drawable.history_24px,
        unSelectedIcon = R.drawable.history_24px
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
    )
)


@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    addLocationViewModel: AddLocationViewModel,
    appViewModel: AppViewModel = viewModel(),
    preferenceViewModel: PreferenceViewModel,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val mainActivity = LocalContext.current as MainActivity
    val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val appSettings by preferenceViewModel.appSettings.collectAsStateWithLifecycle()
    val donateViewModel = hiltViewModel<DonateViewModel>()
    val donateState by donateViewModel.donateState.collectAsStateWithLifecycle()

    val windowWidthSize = windowSizeClass.windowWidthSizeClass
    val windowHeightSize = windowSizeClass.windowHeightSizeClass

    val notificationHourlyChartData by appViewModel.notificationHourlyChartData
        .collectAsStateWithLifecycle()
    val notificationDailyChartData by appViewModel.notificationDailyChartData
        .collectAsStateWithLifecycle()

    LaunchedEffect(navBackStackEntry) {
        val screen = screenItems.find {
            it.screen::class.simpleName == navController.currentDestination?.route?.substringAfterLast('.')
        }?.screen
        screen?.let {
            appViewModel.updateScreen(screen = screen)
        }
    }

    when (windowWidthSize){
        WindowWidthSizeClass.COMPACT, WindowWidthSizeClass.MEDIUM -> {
            val singlePane by remember { derivedStateOf { mainActivity.windowWidth < 720.dp.toPx(density) } }
            ModalNavigationDrawer(
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
                WeatherNavGraph(
                    navController = navController,
                    appViewModel = appViewModel,
                    homeViewModel = homeViewModel,
                    addLocationViewModel = addLocationViewModel,
                    preferenceViewModel = preferenceViewModel,
                    donateViewModel = donateViewModel,
                    appSettings = appSettings,
//                    appUiState = uiState,
                    modifier = modifier.fillMaxSize(),
                    singlePane = singlePane,
                    permanentDrawer = false,
                    onMenuButtonClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            }
        }
        else -> {
            val modalDrawer by remember { derivedStateOf { mainActivity.windowWidth < 1000.dp.toPx(density) } }
            if (modalDrawer) {
                ModalNavigationDrawer(
                    modifier = Modifier.width(dimensionResource(R.dimen.drawer_width)),
                    drawerState = drawerState,
                    gesturesEnabled = drawerState.isOpen,
                    drawerContent = {
                        WeatherDrawerContent(
                            navigationScreenItems = screenItems,
                            selectedItem = uiState.currentScreen,
                            onSelectItem = {
                                val screenAlreadyVisited = isDestinationInBackStack(navController, it)
                                appViewModel.updateScreen(screen = it)
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
                    WeatherNavGraph(
                        navController = navController,
                        appViewModel = appViewModel,
                        homeViewModel = homeViewModel,
                        addLocationViewModel = addLocationViewModel,
                        preferenceViewModel = preferenceViewModel,
                        donateViewModel = donateViewModel,
                        appSettings = appSettings,
//                        appUiState = uiState,
                        modifier = modifier.fillMaxSize(),
                        singlePane = false,
                        permanentDrawer = false,
                        onMenuButtonClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }
            } else {
                PermanentNavigationDrawer(
                    modifier = Modifier.width(dimensionResource(R.dimen.drawer_width)),
                    drawerContent = {
                        WeatherDrawerContent(
                            navigationScreenItems = screenItems,
                            selectedItem = uiState.currentScreen,
                            onSelectItem = {
                                val screenAlreadyVisited = isDestinationInBackStack(navController, it)
                                appViewModel.updateScreen(screen = it)
                                if (screenAlreadyVisited)
                                    navController.popBackStack(route = it, inclusive = false)
                                else
                                    navController.navigate(route = it)
                            }
                        )
                    }
                ) {
                    WeatherNavGraph(
                        navController = navController,
                        appViewModel = appViewModel,
                        homeViewModel = homeViewModel,
                        addLocationViewModel = addLocationViewModel,
                        preferenceViewModel = preferenceViewModel,
                        donateViewModel = donateViewModel,
                        appSettings = appSettings,
//                        appUiState = uiState,
                        modifier = modifier.fillMaxSize(),
                        singlePane = false,
                        permanentDrawer = true
                    )
                }
            }

//            NavigationRail {
//
//            }
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

    if (donateState.purchaseSuccessDialogVisibility){
        PurchaseSuccessDialog(
            onDismiss = {
                donateViewModel.updatePurchaseSuccessDialogVisibility(false)
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
fun PurchaseSuccessDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss){
                Text(
                    text = "Ok",
                    fontFamily = FontFamily.Default
                )
            }
        },
        icon = {
            Icon(
                painter = painterResource(R.drawable.donate),
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(R.string.donate_success_dialog_title))
        },
        text = {
            Text(
                text = stringResource(R.string.donate_success_dialog_text),
                fontFamily = FontFamily.Default
            )
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}


@Composable
fun WeatherDrawerContent(
    navigationScreenItems: List<DrawerNavigationItem>,
    selectedItem: Screen,
    onSelectItem: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet (modifier = modifier.width(280.dp)) {
        Text("Drawer title", modifier = Modifier.padding(16.dp))
        HorizontalDivider()
        navigationScreenItems.forEach { navigationItem ->
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
                    Text(text = navigationItem.title)
                }
            )
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


