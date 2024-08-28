package com.takaapoo.weatherer.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class LocationsState(
    val locationId: Int = 0,
    val locationName: String,
    val currentTemperature: Float? = null,
    val currentHumidity: Float? = null,
    val currentPrecipitationProbability: Float? = null,
    val currentWeatherCode: Int? = null,
    val currentWindSpeed: Float? = null,
    val currentWindDirection: Float? = null,
    val currentAirQuality: Int? = null,
    val todayMaxTemperature: Float? = null,
    val todayMinTemperature: Float? = null,
    val clockBigHandleRotation: Float? = null,
    val clockSmallHandleRotation: Float? = null,
    val isDay: Boolean = true,
    val moonType: Int? = 1,
    val am: Boolean? = null,
    val year: Int = 2023,
    val month: String = "JAN",
    val day: Int = 1
)
