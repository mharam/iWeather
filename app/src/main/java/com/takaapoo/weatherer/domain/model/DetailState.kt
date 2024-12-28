package com.takaapoo.weatherer.domain.model

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import java.time.LocalDateTime
import java.time.ZoneId

@Immutable
data class DetailState(
    val maxTemperature: Float? = null,
    val minTemperature: Float? = null,
    val currentDayHourlyWeather: List<LocalHourlyWeather> = emptyList(),
    val currentDayHourlyAirQuality: List<LocalAirQuality> = emptyList(),
    val sunRise: String? = null,
    val sunSet: String? = null,
    val utcOffset: Long? = 0,
    val localDateTime: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")),
    val targetX: Float = 0f,
    val navigateBack: Boolean = false,
    val hourlyDiagramSettingOpen: Boolean = false,
    val dailyDiagramSettingOpen: Boolean = false,
    val chooseDiagramThemeDialogVisible: Boolean = false,
    val hourlyDiagramSettingRectangle: Rect = Rect(0f, 0f, 0f, 0f),
    val dailyDiagramSettingRectangle: Rect = Rect(0f, 0f, 0f, 0f),
    val scrollState: ScrollState = ScrollState(initial = 0),
    val chartsVisibility: Boolean = false,
)
