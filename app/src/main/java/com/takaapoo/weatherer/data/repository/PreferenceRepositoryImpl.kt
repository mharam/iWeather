package com.takaapoo.weatherer.data.repository

import android.content.Context
import com.takaapoo.weatherer.dataStore
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
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


