package com.takaapoo.weatherer.domain.repository

import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    suspend fun setPreferredTempUnit(unit: Temperature)
    suspend fun setPreferredLengthUnit(unit: Length)
    suspend fun setHourlyDiagramWeatherConditionIconVisibility(visible: Boolean)
    suspend fun setDailyDiagramWeatherConditionIconVisibility(visible: Boolean)
    suspend fun setHourlyDotsOnCurveVisibility(visible: Boolean)
    suspend fun setHourlyCurveShadowVisibility(visible: Boolean)
    suspend fun setHourlySunRiseSetIconsVisibility(visible: Boolean)
    suspend fun setHourlyChartGrid(gridType: ChartGrids)
    suspend fun setHourlyChartTheme(theme: ChartTheme)


    fun getSettingsFlow(): Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    fun getPreferredTempUnit(): Flow<Temperature>
    fun getPreferredLengthUnit(): Flow<Length>
}