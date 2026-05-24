package com.takaapoo.weatherer.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteLocationType (
    val isWater: Boolean,
    val feature: String,
    @SerialName(value = "lat") val latitude: Float,
    @SerialName(value = "lon") val longitude: Float
)