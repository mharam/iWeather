package com.takaapoo.weatherer.domain.repository

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    suspend fun updateDonateState(state: Boolean)
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
    suspend fun setVibration(vibrate: Boolean)
    suspend fun setScreenOn(screenOn: Boolean)
    suspend fun setTheme(theme: AppTheme)
    suspend fun setTimeAxis(time: HourlyDiagramTime)
    suspend fun setDynamicColor(dynamic: Boolean)
    suspend fun setClockGaugeVisibility(visible: Boolean)
    suspend fun setClockGaugeLock(lock: Boolean)
    suspend fun setNotificationVisibility(visible: Boolean)
    suspend fun setNotificationLocationId(id: Int?)
    suspend fun setLastNotificationHeaderSelected(header: Int)
    suspend fun setDisplayNotificationTrigger(value: Boolean)
    suspend fun setCardWeatherQuantities(quantities: List<HourlyWeatherQuantity>)
    suspend fun setHomeCardQuantityUnitsVisibility(visible: Boolean)
    suspend fun setCurvesColor(@IntRange(from = 0, to = 3) curveNumber: Int, color: Int)
    suspend fun setCurvesColor(colors: List<Int>)
    suspend fun setCurvesSmoothness(hourlyWeatherQuantity: HourlyWeatherQuantity,
                                    @FloatRange(from = 0.0, to = 1.0) newSmoothness: Float)
    suspend fun setNotificationCurrentWeatherQuantities(quantities: List<HourlyWeatherQuantity>)
    suspend fun setNotificationHourlyDiagramIconsVisibility(visible: Boolean)
    suspend fun setNotificationHourlyQuantities(quantities: List<HourlyWeatherQuantity>)
    suspend fun setNotificationHourlyCurveShadowVisibility(visible: Boolean)
    suspend fun setNotificationTimeAxis(time: HourlyDiagramTime)
    suspend fun setNotificationCurvesColor(@IntRange(from = 0, to = 1) curveNumber: Int, color: Int)
    suspend fun setNotificationCurvesColor(colors: List<Int>)

    suspend fun setNotificationDailyQuantity(quantity: DailyWeatherQuantity)
    suspend fun setNotificationDailyWeatherIconsVisible(visible: Boolean)
    suspend fun setNotificationDailyChartGrid(grids: ChartGrids)
    suspend fun setNotificationWeatherIconVisible(visible: Boolean)
    suspend fun setNotificationDateTime(time: HourlyDiagramTime)

    suspend fun setWidgetLocationId(id: Int?)
    suspend fun setWidgetDateTime(time: HourlyDiagramTime)
    suspend fun setWidgetHourlyDiagramQuantities(quantities: List<HourlyWeatherQuantity>)
    suspend fun setAddLocationHelpVisible(visible: Boolean)
    suspend fun setUserId(userId: String)
    suspend fun setUserName(userName: String?)
    suspend fun setUserPicture(userPicture: String?)


//    suspend fun setNotificationCount(count: Int)

    fun getSettingsFlow(): Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    fun getPreferredTempUnit(): Flow<Temperature>
    fun getPreferredLengthUnit(): Flow<Length>
}