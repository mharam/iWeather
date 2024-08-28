package com.takaapoo.weatherer.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentWeather(
    val temperature: Float?,
    @SerialName(value = "windspeed") val windSpeed: Float?,
    @SerialName(value = "winddirection") val windDirection: Float?,
    @SerialName(value = "weathercode") val weatherCode: Int?,
    @SerialName(value = "is_day") val isDay: Int?,
    val time: String?
)
