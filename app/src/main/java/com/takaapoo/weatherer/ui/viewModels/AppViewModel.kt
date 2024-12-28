package com.takaapoo.weatherer.ui.viewModels

import androidx.lifecycle.ViewModel
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.History
import com.takaapoo.weatherer.data.HomePane
import com.takaapoo.weatherer.data.Radar
import com.takaapoo.weatherer.data.Screen
import com.takaapoo.weatherer.data.Settings
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

    fun updateScreen(screen: Screen){
        _uiState.update {
            it.copy(currentScreen = screen)
        }
    }

}

data class AppUiState(
    val currentScreen: Screen = HomePane
)