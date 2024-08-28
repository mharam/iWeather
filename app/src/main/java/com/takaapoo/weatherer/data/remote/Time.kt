package com.takaapoo.weatherer.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime

@Serializable
data class LocationTime(
//    val year: Int,
//    val month: Int,
//    val day: Int,
//    val hour: Int,
//    val minute: Int,
//    val seconds: Int,
//    val milliSeconds: Int,
    @SerialName(value = "dateTime") val dateTime: String,
//    val date: String,
//    val time: String,
//    val timeZone: String,
//    val dayOfWeek: String,
//    val dstActive: String
)

data class FullTime(
    val localDateTime: LocalDateTime,
    val utcTime: LocalDateTime
) {
    val utcOffset: Duration = Duration.between(utcTime, localDateTime)
}