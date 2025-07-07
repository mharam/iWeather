package com.takaapoo.weatherer.data.repository

import android.content.Context
import androidx.annotation.IntRange
import com.takaapoo.weatherer.dataStore
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferenceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
): PreferenceRepository {
    override suspend fun updateDonateState(state: Boolean) {
        context.dataStore.updateData {
            it.copy(hasDonated = state)
        }
    }

    override suspend fun setTemperatureUnit(unit: Temperature) {
        context.dataStore.updateData {
            it.copy(temperatureUnit = unit)
        }
    }

    override suspend fun setLengthUnit(unit: Length) {
        context.dataStore.updateData {
            it.copy(lengthUnit = unit)
        }
    }

    override suspend fun setPressureUnit(unit: Pressure) {
        context.dataStore.updateData {
            it.copy(pressureUnit = unit)
        }
    }

    override suspend fun setSpeedUnit(unit: Speed) {
        context.dataStore.updateData {
            it.copy(speedUnit = unit)
        }
    }

    override suspend fun setHourlyDiagramWeatherConditionIconVisibility(visible: Boolean) {
        context.dataStore.updateData {
            it.copy(hourlyDiagramWeatherConditionIconVisible = visible)
        }
    }

    override suspend fun setDailyDiagramWeatherConditionIconVisibility(visible: Boolean) {
        context.dataStore.updateData {
            it.copy(dailyDiagramWeatherConditionIconVisible = visible)
        }
    }

    override suspend fun setHourlyDotsOnCurveVisibility(visible: Boolean) {
        context.dataStore.updateData {
            it.copy(hourlyDotsOnCurveVisible = visible)
        }
    }

    override suspend fun setHourlyCurveShadowVisibility(visible: Boolean) {
        context.dataStore.updateData {
            it.copy(hourlyCurveShadowVisible = visible)
        }
    }

    override suspend fun setHourlySunRiseSetIconsVisibility(visible: Boolean) {
        context.dataStore.updateData {
            it.copy(hourlySunRiseSetIconsVisible = visible)
        }
    }

    override suspend fun setHourlyChartGrid(gridType: ChartGrids) {
        context.dataStore.updateData {
            it.copy(hourlyChartGrid = gridType)
        }
    }

    override suspend fun setHourlyChartTheme(theme: ChartTheme) {
        context.dataStore.updateData {
            it.copy(hourlyChartTheme = theme)
        }
    }

    override suspend fun setSilent(silent: Boolean) {
        context.dataStore.updateData {
            it.copy(silent = silent)
        }
    }

    override suspend fun setVibration(vibrate: Boolean) {
        context.dataStore.updateData {
            it.copy(vibration = vibrate)
        }
    }

    override suspend fun setScreenOn(screenOn: Boolean) {
        context.dataStore.updateData {
            it.copy(screenOn = screenOn)
        }
    }

    override suspend fun setTheme(theme: AppTheme) {
        context.dataStore.updateData {
            it.copy(theme = theme)
        }
    }

    override suspend fun setTimeAxis(time: HourlyDiagramTime) {
        context.dataStore.updateData {
            it.copy(hourlyDiagramTime = time)
        }
    }

    override suspend fun setDynamicColor(dynamic: Boolean) {
        context.dataStore.updateData {
            it.copy(dynamicColor = dynamic)
        }
    }

    override suspend fun setClockGaugeVisibility(visible: Boolean) {
        context.dataStore.updateData {
            it.copy(clockGaugeVisibility = visible)
        }
    }

    override suspend fun setClockGaugeLock(lock: Boolean) {
        context.dataStore.updateData {
            it.copy(clockGaugeLock = lock)
        }
    }

    override suspend fun setNotificationVisibility(visible: Boolean) {
        context.dataStore.updateData {
            it.copy(notificationVisibility = visible)
        }
    }

    override suspend fun setNotificationLocationId(id: Int) {
        context.dataStore.updateData {
            it.copy(notificationLocationId = id)
        }
    }

    override suspend fun setLastNotificationHeaderSelected(header: Int) {
        context.dataStore.updateData {
            it.copy(lastNotificationHeaderSelected = header)
        }
    }

    override suspend fun setDisplayNotificationTrigger(value: Boolean) {
        context.dataStore.updateData {
            it.copy(displayNotificationTrigger = value)
        }
    }

    override suspend fun setCardWeatherQuantities(quantities: List<WeatherQuantity>) {
        context.dataStore.updateData {
            it.copy(cardWeatherQuantities = quantities)
        }
    }

    override suspend fun setHomeCardQuantityUnitsVisibility(visible: Boolean) {
        context.dataStore.updateData {
            it.copy(homeCardQuantityUnitsVisibility = visible)
        }
    }

    override suspend fun setCurvesColor(
        @IntRange(from = 0, to = 3) curveNumber: Int,
        color: Int
    ) {
        val appSettings = getSettings()
        val newCurvesColor = appSettings.curvesColor.toMutableList().apply {
            set(curveNumber, color)
        }
        context.dataStore.updateData {
            it.copy(curvesColor = newCurvesColor)
        }
    }

    override suspend fun setCurvesColor(colors: List<Int>) {
        context.dataStore.updateData {
            it.copy(curvesColor = colors)
        }
    }

    override suspend fun setCurvesSmoothness(
        weatherQuantity: WeatherQuantity,
        newSmoothness: Float,
    ) {
        val appSettings = getSettings()
        val newCurvesSmoothness = appSettings.curvesSmoothness.toMutableMap().apply {
            set(weatherQuantity, newSmoothness)
        }
        context.dataStore.updateData {
            it.copy(curvesSmoothness = newCurvesSmoothness)
        }
    }

    override suspend fun setNotificationCurrentWeatherQuantities(quantities: List<WeatherQuantity>) {
        context.dataStore.updateData {
            it.copy(notificationCurrentQuantities = quantities)
        }
    }

    override suspend fun setNotificationHourlyDiagramIconsVisibility(visible: Boolean) {
        context.dataStore.updateData {
            it.copy(notificationHourlyDiagramIconsVisibility = visible)
        }
    }

    override suspend fun setNotificationHourlyQuantities(quantities: List<WeatherQuantity>) {
        context.dataStore.updateData {
            it.copy(notificationHourlyQuantities = quantities)
        }
    }

    override suspend fun setNotificationHourlyCurveShadowVisibility(visible: Boolean) {
        context.dataStore.updateData {
            it.copy(notificationHourlyCurveShadowVisible = visible)
        }
    }

    override suspend fun setNotificationTimeAxis(time: HourlyDiagramTime) {
        context.dataStore.updateData {
            it.copy(notificationHourlyDiagramTime = time)
        }
    }

    override suspend fun setNotificationCurvesColor(curveNumber: Int, color: Int) {
        val appSettings = getSettings()
        val newCurvesColor = appSettings.notificationCurvesColor.toMutableList().apply {
            set(curveNumber, color)
        }
        context.dataStore.updateData {
            it.copy(notificationCurvesColor = newCurvesColor)
        }
    }

    override suspend fun setNotificationCurvesColor(colors: List<Int>) {
        context.dataStore.updateData {
            it.copy(notificationCurvesColor = colors)
        }
    }

    override suspend fun setNotificationDailyQuantity(quantity: DailyWeatherQuantity) {
        context.dataStore.updateData {
            it.copy(notificationDailyQuantity = quantity)
        }
    }

    override suspend fun setNotificationDailyWeatherIconsVisible(visible: Boolean) {
        context.dataStore.updateData {
            it.copy(notificationDailyWeatherIconsVisible = visible)
        }
    }

    override suspend fun setNotificationDailyChartGrid(grids: ChartGrids) {
        context.dataStore.updateData {
            it.copy(notificationDailyChartGrid = grids)
        }
    }

    override suspend fun setNotificationWeatherIconVisible(visible: Boolean) {
        context.dataStore.updateData {
            it.copy(notificationWeatherIconVisible = visible)
        }
    }

    override suspend fun setNotificationDateTime(time: HourlyDiagramTime) {
        context.dataStore.updateData {
            it.copy(notificationDateTime = time)
        }
    }

    override suspend fun setWidgetLocationId(id: Int) {
        context.dataStore.updateData {
            it.copy(widgetLocationId = id)
        }
    }

    override suspend fun setWidgetDateTime(time: HourlyDiagramTime) {
        context.dataStore.updateData {
            it.copy(widgetDateTime = time)
        }
    }

    override suspend fun setWidgetHourlyDiagramQuantities(quantities: List<WeatherQuantity>) {
        context.dataStore.updateData {
            it.copy(widgetHourlyDiagramQuantities = quantities)
        }
    }


    override fun getSettingsFlow(): Flow<AppSettings> {
        return context.dataStore.data
    }

    override suspend fun getSettings(): AppSettings {
        return context.dataStore.data.first()
    }

    override fun getPreferredTempUnit(): Flow<Temperature> {
        return context.dataStore.data.map { it.temperatureUnit }
    }

    override fun getPreferredLengthUnit(): Flow<Length> {
        return context.dataStore.data.map { it.lengthUnit }
    }
}


