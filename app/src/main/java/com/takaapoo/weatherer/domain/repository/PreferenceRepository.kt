package com.takaapoo.weatherer.domain.repository

import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    suspend fun setTemperatureUnit(unit: Temperature)
    suspend fun setLengthUnit(unit: Length)
    suspend fun setPressureUnit(unit: Pressure)
    suspend fun setSpeedUnit(unit: Speed)
    suspend fun setHourlyDiagramWeatherConditionIconVisibility(visible: Boolean)
    suspend fun setDailyDiagramWeatherConditionIconVisibility(visible: Boolean)
    suspend fun setHourlyDotsOnCurveVisibility(visible: Boolean)
    suspend fun setHourlyCurveShadowVisibility(visible: Boolean)
    suspend fun setHourlySunRiseSetIconsVisibility(visible: Boolean)
    suspend fun setHourlyChartGrid(gridType: ChartGrids)
    suspend fun setHourlyChartTheme(theme: ChartTheme)
    suspend fun setSilent(silent: Boolean)
    suspend fun setScreenOn(screenOn: Boolean)
    suspend fun setTheme(theme: AppTheme)
    suspend fun setClockGaugeVisibility(visible: Boolean)
    suspend fun setClockGaugeLock(lock: Boolean)


    fun getSettingsFlow(): Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    fun getPreferredTempUnit(): Flow<Temperature>
    fun getPreferredLengthUnit(): Flow<Length>
}