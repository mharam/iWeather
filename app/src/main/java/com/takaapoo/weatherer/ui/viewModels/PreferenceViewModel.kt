package com.takaapoo.weatherer.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.domain.model.SettingsState
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
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

    private val _settingsState = MutableStateFlow<SettingsState>(SettingsState())
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

    fun updateThemeDialogVisibility(visible: Boolean){
        _settingsState.update {
            it.copy(themeDialogVisibility = visible)
        }
    }

    fun updateSilent(silent: Boolean) = viewModelScope.launch {
        preferenceRepository.setSilent(silent)
    }

    fun updateScreenOn(screenOn: Boolean) = viewModelScope.launch {
        preferenceRepository.setScreenOn(screenOn)
    }

    fun updateTheme(theme: AppTheme) = viewModelScope.launch {
        preferenceRepository.setTheme(theme)
    }

    fun updateClockGaugeVisibility(visible: Boolean) = viewModelScope.launch {
        preferenceRepository.setClockGaugeVisibility(visible)
    }

    fun updateClockGaugeLock(lock: Boolean) = viewModelScope.launch {
        preferenceRepository.setClockGaugeLock(lock)
    }

}