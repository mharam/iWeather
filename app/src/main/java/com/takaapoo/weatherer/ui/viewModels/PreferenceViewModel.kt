package com.takaapoo.weatherer.ui.viewModels

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.domain.model.SettingsPaneContent
import com.takaapoo.weatherer.domain.model.SettingsState
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferenceViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
): ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState = _settingsState.asStateFlow()

    val appSettings = preferenceRepository.getSettingsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun updateTemperatureUnit(unit: Temperature) = viewModelScope.launch {
        preferenceRepository.setTemperatureUnit(unit)
    }
    fun updateLengthUnit(unit: Length) = viewModelScope.launch {
        preferenceRepository.setLengthUnit(unit)
    }
    fun updatePressureUnit(unit: Pressure) = viewModelScope.launch {
        preferenceRepository.setPressureUnit(unit)
    }
    fun updateSpeedUnit(unit: Speed) = viewModelScope.launch {
        preferenceRepository.setSpeedUnit(unit)
    }


    fun updateSilent(silent: Boolean) = viewModelScope.launch {
        preferenceRepository.setSilent(silent)
    }

    fun updateVibration(vibrate: Boolean) = viewModelScope.launch {
        preferenceRepository.setVibration(vibrate)
    }

    fun updateScreenOn(screenOn: Boolean) = viewModelScope.launch {
        preferenceRepository.setScreenOn(screenOn)
    }

    fun updateTheme(theme: AppTheme) = viewModelScope.launch {
        preferenceRepository.setTheme(theme)
    }

    fun updateTimeAxis(time: HourlyDiagramTime) = viewModelScope.launch {
        preferenceRepository.setTimeAxis(time)
    }

    fun updateDynamicColor(dynamic: Boolean) = viewModelScope.launch {
        preferenceRepository.setDynamicColor(dynamic)
    }

    fun updateClockGaugeVisibility(visible: Boolean) = viewModelScope.launch {
        preferenceRepository.setClockGaugeVisibility(visible)
    }

//    fun updateClockGaugeLock(lock: Boolean) = viewModelScope.launch {
//        preferenceRepository.setClockGaugeLock(lock)
//    }

    fun updateNotificationVisibility(visible: Boolean) = viewModelScope.launch {
        preferenceRepository.setNotificationVisibility(visible)
    }

    fun updateNotificationLocationId(id: Int?) = viewModelScope.launch {
        preferenceRepository.setNotificationLocationId(id)
    }

    fun updateDisplayNotificationTrigger() = viewModelScope.launch {
        preferenceRepository.setDisplayNotificationTrigger(!appSettings.value.displayNotificationTrigger)
    }

    fun updateCardWeatherQuantities(quantity: HourlyWeatherQuantity, add: Boolean) = viewModelScope.launch {
        val selectedQuantities = appSettings.value.cardWeatherQuantities
        if (add){
            val allCardQuantities = HourlyWeatherQuantity.entries.filter { it.homeCardNameId != null }
            if (selectedQuantities.size < 4 && !selectedQuantities.contains(quantity)) {
                val updatedQuantities =
                    selectedQuantities.toMutableList().apply {
                        add(quantity)
                    }.sortedBy { allCardQuantities.indexOf(it) }
                preferenceRepository.setCardWeatherQuantities(updatedQuantities)
            }
        } else {
            if (selectedQuantities.contains(quantity)) {
                val updatedQuantities =
                    selectedQuantities.toMutableList().apply {
                        remove(quantity)
                    }
                preferenceRepository.setCardWeatherQuantities(updatedQuantities)
            }
        }
    }

    fun updateHomeCardQuantityUnitsVisibility(visible: Boolean) = viewModelScope.launch {
        preferenceRepository.setHomeCardQuantityUnitsVisibility(visible)
    }

    fun updateNotificationCurrentWeatherQuantities(quantity: HourlyWeatherQuantity, add: Boolean) = viewModelScope.launch {
        val selectedQuantities = appSettings.value.notificationCurrentQuantities
        if (add){
            val allQuantities = HourlyWeatherQuantity.entries.filter { it.homeCardNameId != null }
            if (selectedQuantities.size < 7 && !selectedQuantities.contains(quantity)) {
                val updatedQuantities =
                    selectedQuantities.toMutableList().apply {
                        add(quantity)
                    }.sortedBy { allQuantities.indexOf(it) }
                preferenceRepository.setNotificationCurrentWeatherQuantities(updatedQuantities)
            }
        } else {
            if (selectedQuantities.contains(quantity)) {
                val updatedQuantities =
                    selectedQuantities.toMutableList().apply {
                        remove(quantity)
                    }
                preferenceRepository.setNotificationCurrentWeatherQuantities(updatedQuantities)
            }
        }
    }

    fun updateNotificationHourlyDiagramIconsVisibility(visible: Boolean) = viewModelScope.launch {
        preferenceRepository.setNotificationHourlyDiagramIconsVisibility(visible)
    }

    fun updateNotificationHourlyQuantities(quantity: HourlyWeatherQuantity, add: Boolean) = viewModelScope.launch {
        val selectedQuantities = appSettings.value.notificationHourlyQuantities
        if (add){
            val allQuantities = HourlyWeatherQuantity.entries
            if (selectedQuantities.size < 2 && !selectedQuantities.contains(quantity)) {
                val updatedQuantities =
                    selectedQuantities.toMutableList().apply {
                        add(quantity)
                    }.sortedBy { allQuantities.indexOf(it) }
                preferenceRepository.setNotificationHourlyQuantities(updatedQuantities)
            }
        } else {
            if (selectedQuantities.contains(quantity)) {
                val updatedQuantities =
                    selectedQuantities.toMutableList().apply {
                        remove(quantity)
                    }
                preferenceRepository.setNotificationHourlyQuantities(updatedQuantities)
            }
        }
    }

    fun updateNotificationHourlyCurveShadowVisibility(visible: Boolean) = viewModelScope.launch {
        preferenceRepository.setNotificationHourlyCurveShadowVisibility(visible)
    }

    fun updateNotificationTimeAxis(time: HourlyDiagramTime) = viewModelScope.launch {
        preferenceRepository.setNotificationTimeAxis(time)
    }

    fun updateNotificationCurvesColor(curveNumber: Int, color: Color) = viewModelScope.launch {
        preferenceRepository.setNotificationCurvesColor(curveNumber, color.toArgb())
    }

    fun updateNotificationCurvesColor(colors: List<Color>) = viewModelScope.launch {
        preferenceRepository.setNotificationCurvesColor(colors.map { it.toArgb() })
    }

    fun updateNotificationDailyQuantity(quantity: DailyWeatherQuantity) = viewModelScope.launch {
        preferenceRepository.setNotificationDailyQuantity(quantity)
    }

    fun updateNotificationDailyWeatherIconsVisible(visible: Boolean) = viewModelScope.launch {
        preferenceRepository.setNotificationDailyWeatherIconsVisible(visible)
    }

    fun updateNotificationDailyChartGrid(grids: ChartGrids) = viewModelScope.launch {
        preferenceRepository.setNotificationDailyChartGrid(grids)
    }

    fun updateNotificationWeatherIconVisible(visible: Boolean) = viewModelScope.launch {
        preferenceRepository.setNotificationWeatherIconVisible(visible)
    }

    fun updateNotificationDateTime(time: HourlyDiagramTime) = viewModelScope.launch {
        preferenceRepository.setNotificationDateTime(time)
    }

    fun updateWidgetLocationId(id: Int?) = viewModelScope.launch {
        preferenceRepository.setWidgetLocationId(id)
    }

    fun updateWidgetDateTime(time: HourlyDiagramTime) = viewModelScope.launch {
        preferenceRepository.setWidgetDateTime(time)
    }

    fun updateWidgetHourlyDiagramQuantities(quantity: HourlyWeatherQuantity, add: Boolean) = viewModelScope.launch {
        val selectedQuantities = appSettings.value.widgetHourlyDiagramQuantities
        if (add){
            val allQuantities = HourlyWeatherQuantity.entries
            if (selectedQuantities.size < 2 && !selectedQuantities.contains(quantity)) {
                val updatedQuantities =
                    selectedQuantities.toMutableList().apply {
                        add(quantity)
                    }.sortedBy { allQuantities.indexOf(it) }
                preferenceRepository.setWidgetHourlyDiagramQuantities(updatedQuantities)
            }
        } else {
            if (selectedQuantities.contains(quantity)) {
                val updatedQuantities =
                    selectedQuantities.toMutableList().apply {
                        remove(quantity)
                    }
                preferenceRepository.setWidgetHourlyDiagramQuantities(updatedQuantities)
            }
        }
    }

    fun updateAddLocationHelpVisibility(visible: Boolean) = viewModelScope.launch {
        preferenceRepository.setAddLocationHelpVisible(visible)
    }



    fun updateThemeDialogVisibility(visible: Boolean){
        _settingsState.update {
            it.copy(themeDialogVisibility = visible)
        }
    }

    fun updateDiagramThemeDialogVisibility(visible: Boolean){
        _settingsState.update {
            it.copy(diagramThemeDialogVisibility = visible)
        }
    }

    fun updateTimeAxisDialogVisibility(visible: Boolean){
        _settingsState.update {
            it.copy(timeAxisDialogVisibility = visible)
        }
    }

    fun updateDiagramGridDialogVisibility(visible: Boolean){
        _settingsState.update {
            it.copy(diagramGridDialogVisibility = visible)
        }
    }

    fun updatePaneContent(
        content: SettingsPaneContent,
        previousContent: SettingsPaneContent? = null
    ) {
        _settingsState.update {
            it.copy(
                paneContent = content,
                previousPaneContent = previousContent
            )
        }
    }

    fun updateNotificationTimeAxisDialogVisibility(visible: Boolean){
        _settingsState.update {
            it.copy(notificationTimeAxisDialogVisibility = visible)
        }
    }

    fun updateNotificationDiagramGridDialogVisibility(visible: Boolean){
        _settingsState.update {
            it.copy(notificationDiagramGridDialogVisibility = visible)
        }
    }

    fun updateNotificationDateTimeDialogVisibility(visible: Boolean){
        _settingsState.update {
            it.copy(notificationDateTimeDialogVisibility = visible)
        }
    }

    fun updateNotificationLocationDialogVisibility(visible: Boolean){
        _settingsState.update {
            it.copy(notificationLocationDialogVisibility = visible)
        }
    }
}