package com.takaapoo.weatherer.data.local

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.utility.celsiusToUnit
import com.takaapoo.weatherer.utility.cmToUnit
import com.takaapoo.weatherer.utility.kmphToUnit
import com.takaapoo.weatherer.utility.mmToUnit
import com.takaapoo.weatherer.utility.paToUnit
import kotlinx.parcelize.Parcelize


@Entity(
    tableName = "historical_hourly_weather",
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["location_id"],
            onDelete = CASCADE,
            deferred = true
        )
    ],
    primaryKeys = ["location_id", "time"]
)
@Parcelize
data class LocalHistoricHourlyWeather(
    @ColumnInfo(name = "location_id") val locationId: Int,
    val time: String,
    @ColumnInfo(name = "temperature_2m") val temperature: Float? = null,
    @ColumnInfo(name = "relativehumidity_2m") val humidity: Float? = null,
    @ColumnInfo(name = "dewpoint_2m") val dewPoint: Float? = null,
    @ColumnInfo(name = "apparent_temperature") val apparentTemperature: Float? = null,
    @ColumnInfo(name = "precipitation") val precipitation: Float? = null,
    @ColumnInfo(name = "rain") val rain: Float? = null,
    @ColumnInfo(name = "snowfall") val snowfall: Float? = null,
    @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,
    @ColumnInfo(name = "cloud_cover") val cloudCover: Float? = null,
    @ColumnInfo(name = "surface_pressure") val surfacePressure: Float? = null,
    @ColumnInfo(name = "windspeed_10m") val windSpeed: Float? = null,
    @ColumnInfo(name = "winddirection_10m") val windDirection: Float? = null,
    @ColumnInfo(name = "is_day") val isDay: Int? = null,
    @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis(),
): Parcelable {
    fun quantityValue(quantity: HourlyWeatherQuantity): Float? {
        return when (quantity) {
            HourlyWeatherQuantity.TEMPERATURE -> temperature
            HourlyWeatherQuantity.HUMIDITY -> humidity
            HourlyWeatherQuantity.DEWPOINT -> dewPoint
            HourlyWeatherQuantity.APPARENTTEMP -> apparentTemperature
            HourlyWeatherQuantity.PRECIPITATION -> precipitation
            HourlyWeatherQuantity.RAIN -> rain
            HourlyWeatherQuantity.SNOWFALL -> snowfall
            HourlyWeatherQuantity.CLOUDCOVER -> cloudCover
            HourlyWeatherQuantity.SURFACEPRESSURE -> surfacePressure
            HourlyWeatherQuantity.WINDSPEED -> windSpeed
            HourlyWeatherQuantity.WINDDIRECTION -> windDirection
            else -> null
        }
    }

    fun accumulate(another: LocalHistoricHourlyWeather?): LocalHistoricHourlyWeather {
        return if (another == null) this
        else this.copy(
            temperature = temperature ?: another.temperature,
            humidity = humidity ?: another.humidity,
            dewPoint = dewPoint ?: another.dewPoint,
            apparentTemperature = apparentTemperature ?: another.apparentTemperature,
            precipitation = precipitation ?: another.precipitation,
            rain = rain ?: another.rain,
            snowfall = snowfall ?: another.snowfall,
            cloudCover = cloudCover ?: another.cloudCover,
            surfacePressure = surfacePressure ?: another.surfacePressure,
            windSpeed = windSpeed ?: another.windSpeed,
            windDirection = windDirection ?: another.windDirection,
        )
    }

    fun applyUnits(appSettings: AppSettings): LocalHistoricHourlyWeather {
        return this.copy(
            temperature = temperature?.celsiusToUnit(appSettings.temperatureUnit),
            dewPoint = dewPoint?.celsiusToUnit(appSettings.temperatureUnit),
            apparentTemperature = apparentTemperature?.celsiusToUnit(appSettings.temperatureUnit),
            precipitation = precipitation?.mmToUnit(appSettings.lengthUnit),
            rain = rain?.mmToUnit(appSettings.lengthUnit),
            snowfall = snowfall?.cmToUnit(appSettings.lengthUnit),
            surfacePressure = surfacePressure?.paToUnit(appSettings.pressureUnit),
            windSpeed = windSpeed?.kmphToUnit(appSettings.speedUnit)
        )
    }
}

fun List<LocalHistoricHourlyWeather>.accumulate(other: List<LocalHistoricHourlyWeather>):
        List<LocalHistoricHourlyWeather> {
    val thisMap = this.associateBy { it.time }
    val otherMap = other.associateBy { it.time }

    // We know first list has wider time range than other list
    return thisMap.keys.mapNotNull { time ->
        thisMap[time]?.accumulate(otherMap.getOrElse(time) { null })
    }
}

sealed class SingleQuantityHourlyWeather {
    @Entity
    data class TemperatureHourlyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "temperature_2m") val temperature: Float? = null,
        @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,
        @ColumnInfo(name = "is_day") val isDay: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityHourlyWeather()

    @Entity
    data class HumidityHourlyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "relativehumidity_2m") val humidity: Float? = null,
        @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,
        @ColumnInfo(name = "is_day") val isDay: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityHourlyWeather()

    @Entity
    data class DewPointHourlyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "dewpoint_2m") val dewPoint: Float? = null,
        @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,
        @ColumnInfo(name = "is_day") val isDay: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityHourlyWeather()

    @Entity
    data class ApparentTemperatureHourlyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "apparent_temperature") val apparentTemperature: Float?,
        @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,
        @ColumnInfo(name = "is_day") val isDay: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityHourlyWeather()

    @Entity
    data class PrecipitationHourlyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "precipitation") val precipitation: Float?,
        @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,
        @ColumnInfo(name = "is_day") val isDay: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityHourlyWeather()

    @Entity
    data class RainHourlyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "rain") val rain: Float?,
        @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,
        @ColumnInfo(name = "is_day") val isDay: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityHourlyWeather()

    @Entity
    data class SnowfallHourlyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "snowfall") val snowfall: Float?,
        @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,
        @ColumnInfo(name = "is_day") val isDay: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityHourlyWeather()

    @Entity
    data class WeatherCodeHourlyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "weathercode") val weatherCode: Int?,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityHourlyWeather()

    @Entity
    data class CloudCoverHourlyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "cloud_cover") val cloudCover: Float?,
        @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,
        @ColumnInfo(name = "is_day") val isDay: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityHourlyWeather()

    @Entity
    data class SurfacePressureHourlyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "surface_pressure") val surfacePressure: Float?,
        @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,
        @ColumnInfo(name = "is_day") val isDay: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityHourlyWeather()

    @Entity
    data class WindSpeedHourlyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "windspeed_10m") val windSpeed: Float?,
        @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,
        @ColumnInfo(name = "is_day") val isDay: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityHourlyWeather()

    @Entity
    data class WindDirectionHourlyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "winddirection_10m") val windDirection: Float?,
        @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,
        @ColumnInfo(name = "is_day") val isDay: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityHourlyWeather()

    @Entity
    data class IsDayHourlyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "is_day") val isDay: Int?,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityHourlyWeather()
}

