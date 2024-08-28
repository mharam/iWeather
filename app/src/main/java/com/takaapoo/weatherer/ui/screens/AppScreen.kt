package com.takaapoo.weatherer.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.takaapoo.weatherer.data.Screens
import com.takaapoo.weatherer.ui.navigation.WeatherNavGraph
import com.takaapoo.weatherer.ui.viewModels.AddLocationViewModel
import com.takaapoo.weatherer.ui.viewModels.AppViewModel
import kotlinx.coroutines.launch


data class BottomNavigationItem(
    val title: String,
    val screen: Screens,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unSelectedIcon: Int
)

@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    addLocationViewModel: AddLocationViewModel,
    appViewModel: AppViewModel = viewModel(),
) {
    val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            WeatherDrawerContent(
                navigationScreenItems = appViewModel.screenItems,
                selectedItem = uiState.currentScreen,
                onSelectItem = {
                    appViewModel.updateScreen(screen = it)
//                    navController.navigate(it.name)
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        WeatherNavGraph(
            navController = navController,
            addLocationViewModel = addLocationViewModel,
            preferenceViewModel = hiltViewModel(),
            modifier = modifier.fillMaxSize(),
            onMenuButtonClick = {
                scope.launch {
                    drawerState.open()
                }
            }
        )
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
    selectedItem: Screens,
    onSelectItem: (Screens) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet (modifier = modifier.width(280.dp)) {
        Text("Drawer title", modifier = Modifier.padding(16.dp))
        Divider()
        navigationScreenItems.forEach { navigationItem ->
            val isSelected = navigationItem.screen == selectedItem
            NavigationDrawerItem(
                selected = false,
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