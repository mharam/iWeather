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
    @ColumnInfo(name = "weathercode") val weatherCode: Int?,
    @ColumnInfo(name = "temperature_2m_max") val temperatureMax: Float?,
    @ColumnInfo(name = "temperature_2m_min") val temperatureMin: Float?,
    @ColumnInfo(name = "sun_rise") val sunRise: String?,
    @ColumnInfo(name = "sun_set") val sunSet: String?,
    @ColumnInfo(name = "uv_index_max") val uvIndexMax: Float?,
    @ColumnInfo(name = "precipitation_sum") val precipitationSum: Float?,
    @ColumnInfo(name = "precipitation_probability_max") val precipitationProbabilityMax: Float?,
    @ColumnInfo(name = "windspeed_10m_max") val windSpeedMax: Float?,
    @ColumnInfo(name = "windgusts_10m_max") val windGustsMax: Float?
)
