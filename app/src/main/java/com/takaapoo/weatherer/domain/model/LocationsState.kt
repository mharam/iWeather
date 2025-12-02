package com.takaapoo.weatherer.domain.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize // This is because of error: "java.lang.RuntimeException: Parcel: unable to marshal value LocationsState(...)"
data class LocationsState(
    val locationId: Int = 0,
    val locationName: String = "",
    val latitude: Float = 0f,
    val longitude: Float = 0f,
    val currentPictureUrl: String = "",
    val lastPicId: Int? = null,
    val picName: String = "1_1_1",

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
    val dayOfWeek: String = "Sat",
) : Parcelable {
    fun quantityValue(quantity: HourlyWeatherQuantity) = when (quantity){
        HourlyWeatherQuantity.TEMPERATURE -> currentTemperature
        HourlyWeatherQuantity.HUMIDITY -> currentHumidity
        HourlyWeatherQuantity.DEWPOINT -> currentDewPoint
        HourlyWeatherQuantity.APPARENTTEMP -> currentApparentTemperature
        HourlyWeatherQuantity.PRECIPITATIONPROBABILITY -> currentPrecipitationProbability
        HourlyWeatherQuantity.PRECIPITATION -> currentPrecipitationAmount
        HourlyWeatherQuantity.CLOUDCOVER -> currentCloudCover
        HourlyWeatherQuantity.SURFACEPRESSURE -> currentSurfacePressure
        HourlyWeatherQuantity.VISIBILITY -> currentVisibility
        HourlyWeatherQuantity.WINDSPEED -> currentWindSpeed
        HourlyWeatherQuantity.WINDDIRECTION -> currentWindDirection
        HourlyWeatherQuantity.UVINDEX -> currentUVIndex
        HourlyWeatherQuantity.FREEZINGLEVELHEIGHT -> currentFreezingLevelHeight
        HourlyWeatherQuantity.DIRECTRADIATION -> currentDirectRadiation
        HourlyWeatherQuantity.DIRECTNORMALIRRADIANCE -> currentDirectNormalIrradiance
        HourlyWeatherQuantity.AQI -> currentAirQuality?.toFloat()
        else -> null
    }
}
