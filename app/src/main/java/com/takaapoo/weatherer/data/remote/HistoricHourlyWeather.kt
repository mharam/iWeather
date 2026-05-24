package com.takaapoo.weatherer.data.remote

import com.takaapoo.weatherer.data.local.LocalHistoricHourlyWeather
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class HistoricHourlyWeather (
    val time: List<String> = emptyList(),
    @SerialName(value = "temperature_2m") val temperature: List<Float?> = emptyList(),
    @SerialName(value = "relativehumidity_2m") val humidity: List<Float?> = emptyList(),
    @SerialName(value = "dewpoint_2m") val dewPoint: List<Float?> = emptyList(),
    @SerialName(value = "apparent_temperature") val apparentTemperature: List<Float?> = emptyList(),
    @SerialName(value = "precipitation") val precipitation: List<Float?> = emptyList(),
    @SerialName(value = "rain") val rain: List<Float?> = emptyList(),
    @SerialName(value = "snowfall") val snowfall: List<Float?> = emptyList(),
    @SerialName(value = "cloud_cover") val cloudCover: List<Float?> = emptyList(),
    @SerialName(value = "weathercode") val weatherCode: List<Int?> = emptyList(),
    @SerialName(value = "surface_pressure") val surfacePressure: List<Float?> = emptyList(),
    @SerialName(value = "windspeed_10m") val windSpeed: List<Float?> = emptyList(),
    @SerialName(value = "winddirection_10m") val windDirection: List<Float?> = emptyList(),
    @SerialName(value = "is_day") val isDay: List<Int?> = emptyList(),
)

fun HistoricHourlyWeather.toLocalHistoricHourlyWeather(
    locationId: Int,
): List<LocalHistoricHourlyWeather> {
    return time.mapIndexed { index, time ->
        LocalHistoricHourlyWeather(
            locationId = locationId,
            time = time,
            temperature = temperature.getOrNull(index),
            humidity = humidity.getOrNull(index),
            dewPoint = dewPoint.getOrNull(index),
            apparentTemperature = apparentTemperature.getOrNull(index),
            precipitation = precipitation.getOrNull(index),
            rain = rain.getOrNull(index),
            snowfall = snowfall.getOrNull(index),
            weatherCode = weatherCode.getOrNull(index),
            cloudCover = cloudCover.getOrNull(index),
            surfacePressure = surfacePressure.getOrNull(index),
            windSpeed = windSpeed.getOrNull(index),
            windDirection = windDirection.getOrNull(index),
            isDay = isDay.getOrNull(index)
        )
    }
}