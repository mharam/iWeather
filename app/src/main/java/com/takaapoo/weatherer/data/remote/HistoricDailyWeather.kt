package com.takaapoo.weatherer.data.remote

import com.takaapoo.weatherer.data.local.LocalHistoricDailyWeather
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class HistoricDailyWeather(
    val time: List<String> = emptyList(),
    @SerialName(value = "weather_code") val weatherCode: List<Int?> = emptyList(),
    @SerialName(value = "temperature_2m_max") val temperatureMax: List<Float?> = emptyList(),
    @SerialName(value = "temperature_2m_min") val temperatureMin: List<Float?> = emptyList(),
    @SerialName(value = "temperature_2m_mean") val temperatureMean: List<Float?> = emptyList(),
    @SerialName(value = "cloud_cover_mean") val cloudCoverMean: List<Float?> = emptyList(),
    @SerialName(value = "relative_humidity_2m_mean") val relativeHumidityMean: List<Float?> = emptyList(),
    @SerialName(value = "precipitation_sum") val precipitationSum: List<Float?> = emptyList(),
    @SerialName(value = "wind_speed_10m_max") val windSpeedMax: List<Float?> = emptyList(),
    @SerialName(value = "wind_gusts_10m_max") val windGustsMax: List<Float?> = emptyList(),
)

fun HistoricDailyWeather.toLocalHistoricDailyWeather(locationId: Int): List<LocalHistoricDailyWeather>{
    return List(size = time.size) { i ->
        LocalHistoricDailyWeather(
            locationId = locationId,
            time = time[i],
            weatherCode = weatherCode.getOrNull(i),
            temperatureMax = temperatureMax.getOrNull(i),
            temperatureMin = temperatureMin.getOrNull(i),
            temperatureMean = temperatureMean.getOrNull(i),
            cloudCoverMean = cloudCoverMean.getOrNull(i),
            relativeHumidityMean = relativeHumidityMean.getOrNull(i),
            precipitationSum = precipitationSum.getOrNull(i),
            windSpeedMax = windSpeedMax.getOrNull(i),
            windGustsMax = windGustsMax.getOrNull(i),
        )
    }
}