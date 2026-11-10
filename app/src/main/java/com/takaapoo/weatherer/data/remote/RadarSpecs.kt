package com.takaapoo.weatherer.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RadarSpecs(
    @SerialName("crs_wkt") val crs: String,
    @SerialName("last_modified_time") val lastModifiedTime: String,
    @SerialName("reference_time") val referenceTime: String,
    @SerialName("valid_times") val validTimes: List<String>,
    val variables: List<String>
)