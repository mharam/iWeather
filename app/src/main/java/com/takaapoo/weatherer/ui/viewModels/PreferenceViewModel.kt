package com.takaapoo.weatherer.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferenceViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
): ViewModel() {

    val temperatureUnit = preferenceRepository.getPreferredTempUnit().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val appSettings = preferenceRepository.getSettingsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

//    fun setHourlyChartWeatherIconVisibility(visible: Boolean) = viewModelScope.launch {
//        preferenceRepository.setHourlyDiagramWeatherConditionIconVisibility(visible)
//    }
}