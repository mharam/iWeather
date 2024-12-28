package com.takaapoo.weatherer.domain.model

import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.ui.utility.celsiusToUnit
import com.takaapoo.weatherer.ui.utility.kmphToUnit

data class WeatherDto(
    val locationId: Int = 0,
    val locationName: String,
    val latitude: Float = 0f,
    val longitude: Float = 0f,
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
    val sunRise: String? = null,
    val sunSet: String? = null,
    val usAQI: Int? = null
){
    fun applyUnits(appSettings: AppSettings) = this.copy(
        currentTemperature = currentTemperature?.celsiusToUnit(appSettings.temperatureUnit),
        currentWindSpeed = currentWindSpeed?.kmphToUnit(appSettings.speedUnit),
        todayMaxTemperature = todayMaxTemperature?.celsiusToUnit(appSettings.temperatureUnit),
        todayMinTemperature = todayMinTemperature?.celsiusToUnit(appSettings.temperatureUnit)
    )
}

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