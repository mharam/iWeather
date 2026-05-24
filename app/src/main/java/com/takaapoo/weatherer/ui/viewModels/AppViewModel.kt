package com.takaapoo.weatherer.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.use_case.GetHourlyChartDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    preferenceRepository: PreferenceRepository,
    private val hourlyChartDataUseCase: GetHourlyChartDataUseCase,
    private val localWeatherRepository: LocalWeatherRepository,
//    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()

    val settingsFlow = preferenceRepository.getSettingsFlow()
    val notificationHourlyChartData: StateFlow<List<HourlyChartDto>> =
        settingsFlow.map { appSettings ->
            val locationId = appSettings.notificationLocationId /*?:
            locationRepository.getAllLocationsList().firstOrNull()?.id*/
            if (locationId != null) {
                hourlyChartDataUseCase(
                    locationId = locationId,
                    startDate = LocalDate.now(ZoneId.of("UTC")).minusDays(1).toString(),
                    endDate = LocalDate.now(ZoneId.of("UTC")).plusDays(3).toString(),
                    appSettings = appSettings
                )
            } else
                emptyList()
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val notificationDailyChartData: StateFlow<List<LocalDailyWeather>> =
        settingsFlow.map { appSettings ->
            val locationId = appSettings.notificationLocationId /*?:
            locationRepository.getAllLocationsList().firstOrNull()?.id*/
            if (locationId != null) {
                localWeatherRepository.getLocationDailyWeather(
                    locationId = locationId,
                    startDate = LocalDate.now().toString(),
                    endDate = LocalDate.now().plusDays(4).toString(),
                    appSettings = appSettings
                )
            } else
                emptyList()
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    /*fun updateScreen(screen: Screen){
        _uiState.update {
            it.copy(currentScreen = screen)
        }
    }*/

    fun updateNotifChannelDialogVisibility(visible: Boolean){
        _uiState.update {
            it.copy(notifChannelDialogVisibility = visible)
        }
    }
    /*fun updateNotificationBitmaps(bitmaps: List<Bitmap?>){
        _uiState.update {
            it.copy(notificationBitmaps = bitmaps)
        }
    }
    fun updateNotificationHeader(header: Int){
        _uiState.update {
            it.copy(notificationHeader = header)
        }
    }*/


}

data class AppUiState(
//    val currentScreen: Screen = HomePane,
    val notifChannelDialogVisibility: Boolean = false,
//    val notificationBitmaps: List<Bitmap?> = emptyList(),
//    val notificationHeader: Int = 1
)