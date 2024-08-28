package com.takaapoo.weatherer.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE


@Entity(
    tableName = "hourly_weather",
    foreignKeys = [
        ForeignKey(entity = Location::class, parentColumns = ["id"], childColumns = ["location_id"],
            onDelete = CASCADE)
    ],
    primaryKeys = ["location_id", "time"]
)
data class LocalHourlyWeather(
    @ColumnInfo(name = "location_id") val locationId: Int,
    val time: String,
    @ColumnInfo(name = "temperature_2m") val temperature: Float? = null,
    @ColumnInfo(name = "temperature_2m_control1_x") val temperatureControl1X: Float? = null,
    @ColumnInfo(name = "temperature_2m_control1_y") val temperatureControl1Y: Float? = null,
    @ColumnInfo(name = "temperature_2m_control2_x") val temperatureControl2X: Float? = null,
    @ColumnInfo(name = "temperature_2m_control2_y") val temperatureControl2Y: Float? = null,

    @ColumnInfo(name = "relativehumidity_2m") val humidity: Float? = null,
    @ColumnInfo(name = "relativehumidity_2m_control1_x") val humidityControl1X: Float? = null,
    @ColumnInfo(name = "relativehumidity_2m_control1_y") val humidityControl1Y: Float? = null,
    @ColumnInfo(name = "relativehumidity_2m_control2_x") val humidityControl2X: Float? = null,
    @ColumnInfo(name = "relativehumidity_2m_control2_y") val humidityControl2Y: Float? = null,

    @ColumnInfo(name = "dewpoint_2m") val dewPoint: Float? = null,
    @ColumnInfo(name = "dewpoint_2m_control1_x") val dewPointControl1X: Float? = null,
    @ColumnInfo(name = "dewpoint_2m_control1_y") val dewPointControl1Y: Float? = null,
    @ColumnInfo(name = "dewpoint_2m_control2_x") val dewPointControl2X: Float? = null,
    @ColumnInfo(name = "dewpoint_2m_control2_y") val dewPointControl2Y: Float? = null,

    @ColumnInfo(name = "apparent_temperature") val apparentTemperature: Float? = null,
    @ColumnInfo(name = "apparent_temperature_control1_x") val apparentTemperatureControl1X: Float? = null,
    @ColumnInfo(name = "apparent_temperature_control1_y") val apparentTemperatureControl1Y: Float? = null,
    @ColumnInfo(name = "apparent_temperature_control2_x") val apparentTemperatureControl2X: Float? = null,
    @ColumnInfo(name = "apparent_temperature_control2_y") val apparentTemperatureControl2Y: Float? = null,

    @ColumnInfo(name = "precipitation_probability") val precipitationProbability: Float? = null,
    @ColumnInfo(name = "precipitation_probability_control1_x") val precipitationProbabilityControl1X: Float? = null,
    @ColumnInfo(name = "precipitation_probability_control1_y") val precipitationProbabilityControl1Y: Float? = null,
    @ColumnInfo(name = "precipitation_probability_control2_x") val precipitationProbabilityControl2X: Float? = null,
    @ColumnInfo(name = "precipitation_probability_control2_y") val precipitationProbabilityControl2Y: Float? = null,

    @ColumnInfo(name = "precipitation") val precipitation: Float? = null,
    @ColumnInfo(name = "rain") val rain: Float? = null,
    @ColumnInfo(name = "showers") val showers: Float? = null,
    @ColumnInfo(name = "snowfall") val snowfall: Float? = null,
    @ColumnInfo(name = "weathercode") val weatherCode: Int? = null,

    @ColumnInfo(name = "cloud_cover") val cloudCover: Float? = null,
    @ColumnInfo(name = "cloud_cover_control1_x") val cloudCoverControl1X: Float? = null,
    @ColumnInfo(name = "cloud_cover_control1_y") val cloudCoverControl1Y: Float? = null,
    @ColumnInfo(name = "cloud_cover_control2_x") val cloudCoverControl2X: Float? = null,
    @ColumnInfo(name = "cloud_cover_control2_y") val cloudCoverControl2Y: Float? = null,

    @ColumnInfo(name = "surface_pressure") val surfacePressure: Float? = null,
    @ColumnInfo(name = "surface_pressure_control1_x") val surfacePressureControl1X: Float? = null,
    @ColumnInfo(name = "surface_pressure_control1_y") val surfacePressureControl1Y: Float? = null,
    @ColumnInfo(name = "surface_pressure_control2_x") val surfacePressureControl2X: Float? = null,
    @ColumnInfo(name = "surface_pressure_control2_y") val surfacePressureControl2Y: Float? = null,

    @ColumnInfo(name = "visibility") val visibility: Float? = null,
    @ColumnInfo(name = "visibility_control1_x") val visibilityControl1X: Float? = null,
    @ColumnInfo(name = "visibility_control1_y") val visibilityControl1Y: Float? = null,
    @ColumnInfo(name = "visibility_control2_x") val visibilityControl2X: Float? = null,
    @ColumnInfo(name = "visibility_control2_y") val visibilityControl2Y: Float? = null,

    @ColumnInfo(name = "windspeed_10m") val windSpeed: Float? = null,
    @ColumnInfo(name = "windspeed_10m_control1_x") val windSpeedControl1X: Float? = null,
    @ColumnInfo(name = "windspeed_10m_control1_y") val windSpeedControl1Y: Float? = null,
    @ColumnInfo(name = "windspeed_10m_control2_x") val windSpeedControl2X: Float? = null,
    @ColumnInfo(name = "windspeed_10m_control2_y") val windSpeedControl2Y: Float? = null,

    @ColumnInfo(name = "winddirection_10m") val windDirection: Float? = null,
    @ColumnInfo(name = "winddirection_10m_control1_x") val windDirectionControl1X: Float? = null,
    @ColumnInfo(name = "winddirection_10m_control1_y") val windDirectionControl1Y: Float? = null,
    @ColumnInfo(name = "winddirection_10m_control2_x") val windDirectionControl2X: Float? = null,
    @ColumnInfo(name = "winddirection_10m_control2_y") val windDirectionControl2Y: Float? = null,

    @ColumnInfo(name = "uv_index") val uvIndex: Float? = null,
    @ColumnInfo(name = "uv_index_control1_x") val uvIndexControl1X: Float? = null,
    @ColumnInfo(name = "uv_index_control1_y") val uvIndexControl1Y: Float? = null,
    @ColumnInfo(name = "uv_index_control2_x") val uvIndexControl2X: Float? = null,
    @ColumnInfo(name = "uv_index_control2_y") val uvIndexControl2Y: Float? = null,

    @ColumnInfo(name = "is_day") val isDay: Int? = null,
    @ColumnInfo(name = "freezinglevel_height") val freezingLevelHeight: Float? = null,
    @ColumnInfo(name = "freezinglevel_height_control1_x") val freezingLevelHeightControl1X: Float? = null,
    @ColumnInfo(name = "freezinglevel_height_control1_y") val freezingLevelHeightControl1Y: Float? = null,
    @ColumnInfo(name = "freezinglevel_height_control2_x") val freezingLevelHeightControl2X: Float? = null,
    @ColumnInfo(name = "freezinglevel_height_control2_y") val freezingLevelHeightControl2Y: Float? = null,

    @ColumnInfo(name = "direct_radiation") val directRadiation: Float? = null,
    @ColumnInfo(name = "direct_radiation_control1_x") val directRadiationControl1X: Float? = null,
    @ColumnInfo(name = "direct_radiation_control1_y") val directRadiationControl1Y: Float? = null,
    @ColumnInfo(name = "direct_radiation_control2_x") val directRadiationControl2X: Float? = null,
    @ColumnInfo(name = "direct_radiation_control2_y") val directRadiationControl2Y: Float? = null,

    @ColumnInfo(name = "direct_normal_irradiance") val directNormalIrradiance: Float? = null,
    @ColumnInfo(name = "direct_normal_irradiance_control1_x") val directNormalIrradianceControl1X: Float? = null,
    @ColumnInfo(name = "direct_normal_irradiance_control1_y") val directNormalIrradianceControl1Y: Float? = null,
    @ColumnInfo(name = "direct_normal_irradiance_control2_x") val directNormalIrradianceControl2X: Float? = null,
    @ColumnInfo(name = "direct_normal_irradiance_control2_y") val directNormalIrradianceControl2Y: Float? = null

    )
