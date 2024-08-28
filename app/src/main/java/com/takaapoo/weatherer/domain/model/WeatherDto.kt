package com.takaapoo.weatherer.domain.model

import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalHourlyWeather

data class WeatherDto(
    val locationId: Int = 0,
    val locationName: String,
    val utcOffset: Long? = null,
    val time: String = "",
    val currentTemperature: Float? = null,
    val currentHumidity: Float? = null,
    val currentPrecipitationProbability: Float? = null,
    val currentWeatherCode: Int? = null,
    val currentWindSpeed: Float? = null,
    val currentWindDirection: Float? = null,
    val todayMaxTemperature: Float? = null,
    val todayMinTemperature: Float? = null,
    val sunRise: String?,
    val sunSet: String?,
    val usAQI: Int? = null
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