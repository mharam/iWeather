package com.takaapoo.weatherer.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.utility.celsiusToUnit
import com.takaapoo.weatherer.utility.kmphToUnit
import com.takaapoo.weatherer.utility.mmToUnit

@Entity(
    tableName = "historical_daily_weather",
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["location_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["location_id", "time"]
)
data class LocalHistoricDailyWeather(
    @ColumnInfo(name = "location_id") val locationId: Int,
    val time: String,
    @ColumnInfo(name = "weather_code") val weatherCode: Int? = null,
    @ColumnInfo(name = "temperature_2m_max") val temperatureMax: Float? = null,
    @ColumnInfo(name = "temperature_2m_min") val temperatureMin: Float? = null,
    @ColumnInfo(name = "temperature_2m_mean") val temperatureMean: Float? = null,
    @ColumnInfo(name = "cloud_cover_mean") val cloudCoverMean: Float? = null,
    @ColumnInfo(name = "relative_humidity_mean") val relativeHumidityMean: Float? = null,
    @ColumnInfo(name = "precipitation_sum") val precipitationSum: Float? = null,
    @ColumnInfo(name = "wind_speed_10m_max") val windSpeedMax: Float? = null,
    @ColumnInfo(name = "wind_gusts_10m_max") val windGustsMax: Float? = null,
    @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis(),
) {
    fun quantityValue(quantity: DailyWeatherQuantity): Float? {
        return when (quantity) {
            DailyWeatherQuantity.TEMPERATUREMINMAX -> temperatureMax
            DailyWeatherQuantity.TEMPERATUREMEAN -> temperatureMean
            DailyWeatherQuantity.PRECIPITATIONSUM -> precipitationSum
            DailyWeatherQuantity.WINDSPEEDMAX -> windSpeedMax
            DailyWeatherQuantity.CLOUDCOVERMEAN -> cloudCoverMean
            DailyWeatherQuantity.HUMIDITYMEAN -> relativeHumidityMean
            else -> null
        }
    }

    fun applyUnits(appSettings: AppSettings): LocalHistoricDailyWeather {
        return this.copy(
            temperatureMax = temperatureMax?.celsiusToUnit(appSettings.temperatureUnit),
            temperatureMin = temperatureMin?.celsiusToUnit(appSettings.temperatureUnit),
            temperatureMean = temperatureMean?.celsiusToUnit(appSettings.temperatureUnit),
            precipitationSum = precipitationSum?.mmToUnit(appSettings.lengthUnit),
            windSpeedMax = windSpeedMax?.kmphToUnit(appSettings.speedUnit),
            windGustsMax = windGustsMax?.kmphToUnit(appSettings.speedUnit),
        )
    }
}



sealed class SingleQuantityDailyWeather {
    @Entity
    data class TemperatureMinMaxDailyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "temperature_2m_max") val temperatureMax: Float? = null,
        @ColumnInfo(name = "temperature_2m_min") val temperatureMin: Float? = null,
        @ColumnInfo(name = "weather_code") val weatherCode: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityDailyWeather()

    @Entity
    data class TemperatureMeanDailyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "temperature_2m_mean") val temperatureMean: Float? = null,
        @ColumnInfo(name = "weather_code") val weatherCode: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityDailyWeather()

    @Entity
    data class CloudCoverDailyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "cloud_cover_mean") val cloudCoverMean: Float? = null,
        @ColumnInfo(name = "weather_code") val weatherCode: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityDailyWeather()

    @Entity
    data class HumidityDailyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "relative_humidity_mean") val relativeHumidityMean: Float? = null,
        @ColumnInfo(name = "weather_code") val weatherCode: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityDailyWeather()

    @Entity
    data class PrecipitationDailyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "precipitation_sum") val precipitationSum: Float? = null,
        @ColumnInfo(name = "weather_code") val weatherCode: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityDailyWeather()

    @Entity
    data class WindSpeedDailyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "wind_speed_10m_max") val windSpeedMax: Float? = null,
        @ColumnInfo(name = "weather_code") val weatherCode: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityDailyWeather()

    @Entity
    data class WindGustsDailyWeather(
        @ColumnInfo(name = "location_id") val locationId: Int,
        val time: String,
        @ColumnInfo(name = "wind_gusts_10m_max") val windGustsMax: Float? = null,
        @ColumnInfo(name = "weather_code") val weatherCode: Int? = null,
        @ColumnInfo(name = "time_stamp") val timeStamp: Long = System.currentTimeMillis()
    ) : SingleQuantityDailyWeather()
}