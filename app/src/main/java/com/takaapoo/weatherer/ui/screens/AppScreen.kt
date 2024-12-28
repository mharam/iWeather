package com.takaapoo.weatherer.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.History
import com.takaapoo.weatherer.data.HomePane
import com.takaapoo.weatherer.data.Radar
import com.takaapoo.weatherer.data.Screen
import com.takaapoo.weatherer.data.Settings
import com.takaapoo.weatherer.ui.navigation.WeatherNavGraph
import com.takaapoo.weatherer.ui.utility.toPx
import com.takaapoo.weatherer.ui.viewModels.AddLocationViewModel
import com.takaapoo.weatherer.ui.viewModels.AppViewModel
import com.takaapoo.weatherer.ui.viewModels.HomeViewModel
import com.takaapoo.weatherer.ui.viewModels.PreferenceViewModel
import kotlinx.coroutines.launch


data class BottomNavigationItem(
    val title: String,
    val screen: Screen,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unSelectedIcon: Int
)
val screenItems = listOf(
    BottomNavigationItem(
        title = "Home",
        screen = HomePane,
        selectedIcon = R.drawable.home_filled_24px,
        unSelectedIcon = R.drawable.home_24px
    ),
    BottomNavigationItem(
        title = "Radar",
        screen = Radar,
        selectedIcon = R.drawable.radar_24px,
        unSelectedIcon = R.drawable.radar_24px
    ),
    BottomNavigationItem(
        title = "History",
        screen = History,
        selectedIcon = R.drawable.history_24px,
        unSelectedIcon = R.drawable.history_24px
    ),
    BottomNavigationItem(
        title = "Settings",
        screen = Settings,
        selectedIcon = R.drawable.settings_filled_24px,
        unSelectedIcon = R.drawable.settings_24px
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
    val density = LocalDensity.current
    val mainActivity = LocalContext.current as MainActivity
    val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val windowWidthSize = windowSizeClass.windowWidthSizeClass
    val windowHeightSize = windowSizeClass.windowHeightSizeClass

    LaunchedEffect(navController.currentDestination) {
//        Log.i("route1", "route = ${navController.currentDestination?.route?.substringAfterLast('.')}")
        val screen = screenItems.find {
            it.screen.toString() == navController.currentDestination?.route?.substringAfterLast('.')
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
//                            appViewModel.updateScreen(screen = it)
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
                    homeViewModel = homeViewModel,
                    addLocationViewModel = addLocationViewModel,
                    preferenceViewModel = preferenceViewModel,
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
//                                appViewModel.updateScreen(screen = it)
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
                        homeViewModel = homeViewModel,
                        addLocationViewModel = addLocationViewModel,
                        preferenceViewModel = preferenceViewModel,
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
//                                appViewModel.updateScreen(screen = it)
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
                        homeViewModel = homeViewModel,
                        addLocationViewModel = addLocationViewModel,
                        preferenceViewModel = preferenceViewModel,
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


}

//@Composable
//fun WeatherNavigationBar(
//    navigationScreenItems: List<BottomNavigationItem>,
//    selectedItem: Screens,
//    onSelectItem: (Screens) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    NavigationBar(modifier = modifier) {
//        navigationScreenItems.forEach { navigationItem ->
//            val isSelected = navigationItem.screen == selectedItem
//            NavigationBarItem(
//                selected = isSelected,
//                onClick = { onSelectItem(navigationItem.screen) },
//                icon = {
//                    Icon(
//                        painter = painterResource(if (isSelected) navigationItem.selectedIcon
//                        else navigationItem.unSelectedIcon),
//                        contentDescription = navigationItem.title
//                    )
//                },
//                label = {
//                    Text(text = navigationItem.title)
//                },
//                alwaysShowLabel = false
//            )
//        }
//    }
//}

@Composable
fun WeatherDrawerContent(
    navigationScreenItems: List<BottomNavigationItem>,
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