package com.takaapoo.weatherer.ui.viewModels

import androidx.lifecycle.ViewModel
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.Screens
import com.takaapoo.weatherer.ui.screens.BottomNavigationItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()

    val screenItems = listOf(
        BottomNavigationItem(
            title = "Home",
            screen = Screens.HOME,
            selectedIcon = R.drawable.home_filled_24px,
            unSelectedIcon = R.drawable.home_24px
        ),
        BottomNavigationItem(
            title = "Radar",
            screen = Screens.RADAR,
            selectedIcon = R.drawable.radar_24px,
            unSelectedIcon = R.drawable.radar_24px
        ),
        BottomNavigationItem(
            title = "History",
            screen = Screens.HISTORY,
            selectedIcon = R.drawable.history_24px,
            unSelectedIcon = R.drawable.history_24px
        ),
        BottomNavigationItem(
            title = "Settings",
            screen = Screens.SETTING,
            selectedIcon = R.drawable.settings_filled_24px,
            unSelectedIcon = R.drawable.settings_24px
        )
    )

    fun updateScreen(screen: Screens){
        _uiState.update {
            it.copy(currentScreen = screen)
        }
    }

}

data class AppUiState(
    val currentScreen: Screens = Screens.HOME
)