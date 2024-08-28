package com.takaapoo.weatherer.data.remote

import com.takaapoo.weatherer.data.local.LocalDailyWeather
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

val DailyWeatherDefaultValues = List(NumberOfDays){ null }
val DailyWeatherDefaultTimes = List(17){ dayIndex ->
    LocalDate.now(ZoneId.of("UTC")).minusDays(1).plusDays(dayIndex.toLong()).toString()
}

@Serializable
data class DailyWeather(
    val time: List<String> = DailyWeatherDefaultTimes,
    @SerialName(value = "weathercode") val weatherCode: List<Int?> = DailyWeatherDefaultValues,
    @SerialName(value = "temperature_2m_max") val temperatureMax: List<Float?> = DailyWeatherDefaultValues,
    @SerialName(value = "temperature_2m_min") val temperatureMin: List<Float?> = DailyWeatherDefaultValues,
    @SerialName(value = "sunrise") val sunRise: List<String?> = DailyWeatherDefaultValues,
    @SerialName(value = "sunset") val sunSet: List<String?> = DailyWeatherDefaultValues,
    @SerialName(value = "uv_index_max") val uvIndexMax: List<Float?> = DailyWeatherDefaultValues,
    @SerialName(value = "precipitation_sum") val precipitationSum: List<Float?> = DailyWeatherDefaultValues,
    @SerialName(value = "precipitation_probability_max") val precipitationProbabilityMax: List<Float?> =
        DailyWeatherDefaultValues,
    @SerialName(value = "windspeed_10m_max") val windSpeedMax: List<Float?> = DailyWeatherDefaultValues,
    @SerialName(value = "windgusts_10m_max") val windGustsMax: List<Float?> = DailyWeatherDefaultValues,
)

fun DailyWeather.toLocalDailyWeather(locationId: Int): List<LocalDailyWeather>{
    return List(size = time.size) { i ->
        LocalDailyWeather(
            locationId = locationId,
            time = time[i],
            weatherCode = weatherCode[i],
            temperatureMax = temperatureMax[i],
            temperatureMin = temperatureMin[i],
            sunRise = sunRise[i],
            sunSet = sunSet[i],
            uvIndexMax = uvIndexMax[i],
            precipitationSum = precipitationSum[i],
            precipitationProbabilityMax = precipitationProbabilityMax[i],
            windSpeedMax = windSpeedMax[i],
            windGustsMax = windGustsMax[i]
        )
    }
}