package com.takaapoo.weatherer.domain.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize // This is because of error: "java.lang.RuntimeException: Parcel: unable to marshal value LocationsState(...)"
data class LocationsState(
    val locationId: Int = 0,
    val locationName: String = "",
    val latitude: Float = 0f,
    val longitude: Float = 0f,
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
    val day: Int = 1,
    val dayOfWeek: String = "Sat"
) : Parcelable
