package com.takaapoo.weatherer.domain.model

import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalHourlyWeather

data class WeatherDto(
    val locationId: Int = 0,
    val locationName: String,
    val latitude: Float = 0f,
    val longitude: Float = 0f,
    val utcOffset: Long? = null,
    val currentWeather: LocalHourlyWeather? = null,
    val currentAirQuality: LocalAirQuality? = null,
    val todayMaxTemperature: Float? = null,
    val todayMinTemperature: Float? = null,
    val sunRise: String? = null,
    val sunSet: String? = null,
)

data class HourlyChartDto(
    val hourlyWeather: LocalHourlyWeather? = LocalHourlyWeather(locationId = 0, time = ""),
    val airQuality: LocalAirQuality? = LocalAirQuality(locationId = 0, time = ""),
    val locationName: String,
    val utcOffset: Long? = null,
    val maxTemperature: Float? = null,
    val minTemperature: Float? = null,
    val sunRise: String?,
    val sunSet: String?,
)