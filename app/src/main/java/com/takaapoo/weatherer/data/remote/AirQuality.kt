package com.takaapoo.weatherer.data.remote

import com.takaapoo.weatherer.data.local.LocalAirQuality
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class AirQuality(
    val time: List<String> = HourlyWeatherDefaultTimes,
    @SerialName(value = "pm10") val pm10: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "pm2_5") val pm25: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "carbon_monoxide") val carbonMonoxide: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "nitrogen_dioxide") val nitrogenDioxide: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "sulphur_dioxide") val sulphurDioxide: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "ozone") val ozone: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "us_aqi") val airQuality: List<Int?> = HourlyWeatherDefaultValues
)

@Serializable
data class WholeAirQuality(
    val latitude: Float,
    val longitude: Float,
    @SerialName(value = "utc_offset_seconds") val utcOffsetSeconds: Int,
    @SerialName(value = "timezone") val timeZone: String,
    @SerialName(value = "timezone_abbreviation") val timeZoneAbbreviation: String,
    val elevation: Float,
    @SerialName(value = "hourly") val airQuality: AirQuality
)

fun AirQuality.toLocalAirQuality(locationId: Int): List<LocalAirQuality> {
    val time0 = LocalDateTime.parse(time[0])
    val (pm10Control1, pm10Control2) = generateControlPoints2(pm10)
    val (pm25Control1, pm25Control2) = generateControlPoints2(pm25)
    val (coControl1, coControl2) = generateControlPoints2(carbonMonoxide)
    val (no2Control1, no2Control2) = generateControlPoints2(nitrogenDioxide)
    val (so2Control1, so2Control2) = generateControlPoints2(sulphurDioxide)
    val (o3Control1, o3Control2) = generateControlPoints2(ozone)
    val (aqiControl1, aqiControl2) = generateControlPoints2(airQuality.map { it?.toFloat() })


    return List(size = time.size /*NumberOfDays * DayHoursCount*/){ i ->
        LocalAirQuality(
            locationId = locationId,
            time = /*time0.plusHours(i.toLong()).toString()*/time[i],
            pm10 = pm10.getOrNull(i),
            pm10Control1X = pm10Control1.getOrNull(i)?.x,
            pm10Control1Y = pm10Control1.getOrNull(i)?.y,
            pm10Control2X = pm10Control2.getOrNull(i)?.x,
            pm10Control2Y = pm10Control2.getOrNull(i)?.y,

            pm25 = pm25.getOrNull(i),
            pm25Control1X = pm25Control1.getOrNull(i)?.x,
            pm25Control1Y = pm25Control1.getOrNull(i)?.y,
            pm25Control2X = pm25Control2.getOrNull(i)?.x,
            pm25Control2Y = pm25Control2.getOrNull(i)?.y,

            carbonMonoxide = carbonMonoxide.getOrNull(i),
            coControl1X = coControl1.getOrNull(i)?.x,
            coControl1Y = coControl1.getOrNull(i)?.y,
            coControl2X = coControl2.getOrNull(i)?.x,
            coControl2Y = coControl2.getOrNull(i)?.y,

            nitrogenDioxide = nitrogenDioxide.getOrNull(i),
            no2Control1X = no2Control1.getOrNull(i)?.x,
            no2Control1Y = no2Control1.getOrNull(i)?.y,
            no2Control2X = no2Control2.getOrNull(i)?.x,
            no2Control2Y = no2Control2.getOrNull(i)?.y,

            sulphurDioxide = sulphurDioxide.getOrNull(i),
            so2Control1X = so2Control1.getOrNull(i)?.x,
            so2Control1Y = so2Control1.getOrNull(i)?.y,
            so2Control2X = so2Control2.getOrNull(i)?.x,
            so2Control2Y = so2Control2.getOrNull(i)?.y,

            ozone = ozone.getOrNull(i),
            o3Control1X = o3Control1.getOrNull(i)?.x,
            o3Control1Y = o3Control1.getOrNull(i)?.y,
            o3Control2X = o3Control2.getOrNull(i)?.x,
            o3Control2Y = o3Control2.getOrNull(i)?.y,

            airQuality = airQuality.getOrNull(i),
            aqiControl1X = aqiControl1.getOrNull(i)?.x,
            aqiControl1Y = aqiControl1.getOrNull(i)?.y,
            aqiControl2X = aqiControl2.getOrNull(i)?.x,
            aqiControl2Y = aqiControl2.getOrNull(i)?.y
        )
    }
}