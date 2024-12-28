package com.takaapoo.weatherer.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "daily_weather",
    foreignKeys = [
        ForeignKey(entity = Location::class, parentColumns = ["id"], childColumns = ["location_id"],
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
)
