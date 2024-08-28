package com.takaapoo.weatherer.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE


@Entity(
    tableName = "air_quality",
    foreignKeys = [
        ForeignKey(entity = Location::class, parentColumns = ["id"], childColumns = ["location_id"],
            onDelete = CASCADE)
    ],
    primaryKeys = ["location_id", "time"]
)
data class LocalAirQuality(
    @ColumnInfo(name = "location_id") val locationId: Int,
    val time: String,
    @ColumnInfo(name = "pm10") val pm10: Float? = null,
    @ColumnInfo(name = "pm10_control1_x") val pm10Control1X: Float? = null,
    @ColumnInfo(name = "pm10_control1_y") val pm10Control1Y: Float? = null,
    @ColumnInfo(name = "pm10_control2_x") val pm10Control2X: Float? = null,
    @ColumnInfo(name = "pm10_control2_y") val pm10Control2Y: Float? = null,

    @ColumnInfo(name = "pm2_5") val pm25: Float? = null,
    @ColumnInfo(name = "pm2_5_control1_x") val pm25Control1X: Float? = null,
    @ColumnInfo(name = "pm2_5_control1_y") val pm25Control1Y: Float? = null,
    @ColumnInfo(name = "pm2_5_control2_x") val pm25Control2X: Float? = null,
    @ColumnInfo(name = "pm2_5_control2_y") val pm25Control2Y: Float? = null,

    @ColumnInfo(name = "carbon_monoxide") val carbonMonoxide: Float? = null,
    @ColumnInfo(name = "co_control1_x") val coControl1X: Float? = null,
    @ColumnInfo(name = "co_control1_y") val coControl1Y: Float? = null,
    @ColumnInfo(name = "co_control2_x") val coControl2X: Float? = null,
    @ColumnInfo(name = "co_control2_y") val coControl2Y: Float? = null,

    @ColumnInfo(name = "nitrogen_dioxide") val nitrogenDioxide: Float? = null,
    @ColumnInfo(name = "no2_control1_x") val no2Control1X: Float? = null,
    @ColumnInfo(name = "no2_control1_y") val no2Control1Y: Float? = null,
    @ColumnInfo(name = "no2_control2_x") val no2Control2X: Float? = null,
    @ColumnInfo(name = "no2_control2_y") val no2Control2Y: Float? = null,

    @ColumnInfo(name = "sulphur_dioxide") val sulphurDioxide: Float? = null,
    @ColumnInfo(name = "so2_control1_x") val so2Control1X: Float? = null,
    @ColumnInfo(name = "so2_control1_y") val so2Control1Y: Float? = null,
    @ColumnInfo(name = "so2_control2_x") val so2Control2X: Float? = null,
    @ColumnInfo(name = "so2_control2_y") val so2Control2Y: Float? = null,

    @ColumnInfo(name = "ozone") val ozone: Float? = null,
    @ColumnInfo(name = "o3_control1_x") val o3Control1X: Float? = null,
    @ColumnInfo(name = "o3_control1_y") val o3Control1Y: Float? = null,
    @ColumnInfo(name = "o3_control2_x") val o3Control2X: Float? = null,
    @ColumnInfo(name = "o3_control2_y") val o3Control2Y: Float? = null,

    @ColumnInfo(name = "us_aqi") val airQuality: Int? = null,
    @ColumnInfo(name = "aqi_control1_x") val aqiControl1X: Float? = null,
    @ColumnInfo(name = "aqi_control1_y") val aqiControl1Y: Float? = null,
    @ColumnInfo(name = "aqi_control2_x") val aqiControl2X: Float? = null,
    @ColumnInfo(name = "aqi_control2_y") val aqiControl2Y: Float? = null
)

