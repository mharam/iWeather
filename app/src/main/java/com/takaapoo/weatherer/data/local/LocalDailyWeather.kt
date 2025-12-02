package com.takaapoo.weatherer.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.utility.celsiusToUnit
import com.takaapoo.weatherer.utility.kmphToUnit
import com.takaapoo.weatherer.utility.mmToUnit

@Entity(
    tableName = "daily_weather",
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
data class LocalDailyWeather(
    @ColumnInfo(name = "location_id") val locationId: Int,
    val time: String,
    @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,
    @ColumnInfo(name = "temperature_2m_max") val temperatureMax: Float? = null,
    @ColumnInfo(name = "temperature_2m_min") val temperatureMin: Float? = null,
    @ColumnInfo(name = "sun_rise") val sunRise: String? = null,
    @ColumnInfo(name = "sun_set") val sunSet: String? = null,
    @ColumnInfo(name = "uv_index_max") val uvIndexMax: Float? = null,
    @ColumnInfo(name = "precipitation_sum") val precipitationSum: Float? = null,
    @ColumnInfo(name = "precipitation_probability_max") val precipitationProbabilityMax: Float? = null,
    @ColumnInfo(name = "windspeed_10m_max") val windSpeedMax: Float? = null,
    @ColumnInfo(name = "windgusts_10m_max") val windGustsMax: Float? = null
) {
    fun setUnits(appSettings: AppSettings) = this.copy(
        temperatureMax = temperatureMax?.celsiusToUnit(appSettings.temperatureUnit),
        temperatureMin = temperatureMin?.celsiusToUnit(appSettings.temperatureUnit),
        precipitationSum = precipitationSum?.mmToUnit(appSettings.lengthUnit),
        windSpeedMax = windSpeedMax?.kmphToUnit(appSettings.speedUnit),
        windGustsMax = windGustsMax?.kmphToUnit(appSettings.speedUnit)
    )
}
