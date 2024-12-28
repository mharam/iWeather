package com.takaapoo.weatherer.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HourlyWeatherPack(
    val latitude: Float,
    val longitude: Float,
    @SerialName(value = "utc_offset_seconds") val utcOffsetSeconds: Int,
    @SerialName(value = "timezone") val timeZone: String,
    @SerialName(value = "timezone_abbreviation") val timeZoneAbbreviation: String,
    val elevation: Float,
//    @SerialName(value = "current") val currentWeather: CurrentWeather,
    @SerialName(value = "hourly") val hourlyWeather: HourlyWeather,
)

@Serializable
data class DailyWeatherPack(
    val latitude: Float,
    val longitude: Float,
    @SerialName(value = "utc_offset_seconds") val utcOffsetSeconds: Int,
    @SerialName(value = "timezone") val timeZone: String,
    @SerialName(value = "timezone_abbreviation") val timeZoneAbbreviation: String,
    val elevation: Float,
    @SerialName(value = "daily") val dailyWeather: DailyWeather
)