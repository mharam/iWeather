package com.takaapoo.weatherer.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.use_case.GetHourlyChartDataUseCase
import com.takaapoo.weatherer.utility.Billing
import com.takaapoo.weatherer.utility.NetworkObserver
import com.takaapoo.weatherer.utility.User
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    @param:ApplicationContext val applicationContext: Context,
    preferenceRepository: PreferenceRepository,
    private val hourlyChartDataUseCase: GetHourlyChartDataUseCase,
    private val localWeatherRepository: LocalWeatherRepository,
    val billing: Billing,
    val user: User
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()

    private val networkObserver = NetworkObserver(applicationContext)
    val internetConnected: StateFlow<Boolean> = networkObserver.connectivityStatusFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = networkObserver.isConnectedToInternet()
        )

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

    fun updateNotifChannelDialogVisibility(visible: Boolean){
        _uiState.update {
            it.copy(notifChannelDialogVisibility = visible)
        }
    }

    init {
//        viewModelScope.launch {
//            preferenceRepository.apply {
//                setUserId(null)
//                setUserName(null)
//                setUserPicture(null)
//            }
//        }
        billing.startBillingConnection()

        internetConnected
            .onEach { isConnected ->
                if (isConnected) {
//                    billing.startBillingConnection()
                    user.initialize()
                }
            }
            .launchIn(viewModelScope)
    }

}

data class AppUiState(
    val notifChannelDialogVisibility: Boolean = false,
)