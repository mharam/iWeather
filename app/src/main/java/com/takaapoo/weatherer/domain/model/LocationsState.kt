package com.takaapoo.weatherer.domain.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
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
    val currentDewPoint: Float? = null,
    val currentApparentTemperature: Float? = null,
    val currentPrecipitationProbability: Float? = null,
    val currentPrecipitationAmount: Float? = null,
    val currentCloudCover: Float? = null,
    val currentSurfacePressure: Float? = null,
    val currentVisibility: Float? = null,
    val currentWeatherCode: Int? = null,
    val currentWindSpeed: Float? = null,
    val currentWindDirection: Float? = null,
    val currentUVIndex: Float? = null,
    val currentFreezingLevelHeight: Float? = null,
    val currentDirectRadiation: Float? = null,
    val currentDirectNormalIrradiance: Float? = null,
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
) : Parcelable {
    fun quantityValue(quantity: WeatherQuantity) = when (quantity){
        WeatherQuantity.TEMPERATURE -> currentTemperature
        WeatherQuantity.HUMIDITY -> currentHumidity
        WeatherQuantity.DEWPOINT -> currentDewPoint
        WeatherQuantity.APPARENTTEMP -> currentApparentTemperature
        WeatherQuantity.PRECIPITATIONPROBABILITY -> currentPrecipitationProbability
        WeatherQuantity.PRECIPITATION -> currentPrecipitationAmount
        WeatherQuantity.CLOUDCOVER -> currentCloudCover
        WeatherQuantity.SURFACEPRESSURE -> currentSurfacePressure
        WeatherQuantity.VISIBILITY -> currentVisibility
        WeatherQuantity.WINDSPEED -> currentWindSpeed
        WeatherQuantity.WINDDIRECTION -> currentWindDirection
        WeatherQuantity.UVINDEX -> currentUVIndex
        WeatherQuantity.FREEZINGLEVELHEIGHT -> currentFreezingLevelHeight
        WeatherQuantity.DIRECTRADIATION -> currentDirectRadiation
        WeatherQuantity.DIRECTNORMALIRRADIANCE -> currentDirectNormalIrradiance
        WeatherQuantity.AQI -> currentAirQuality?.toFloat()
        else -> null
    }
}
