package com.takaapoo.weatherer.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationServerOutput(
    val features: List<Location>
)

@Serializable
data class Location(
    val geometry: Geometry,
    val properties: Properties,
    @SerialName(value = "bbox") val boundingBox: DoubleArray? = null
)

@Serializable
data class Geometry(
    val coordinates: DoubleArray
)

@Serializable
data class Properties(
    val name: String? = null,
    val country: String? = null,
    val state: String? = null,
    val province: String? = null,
    val county: String? = null,
    val city: String? = null,
    val village: String? = null,
    val formatted: String
){
    fun title(text: String): String{
        val index = formatted.indexOf(string = text, startIndex = 0, ignoreCase = true)
        val newName = if (index > -1)
                (formatted.substring(index).plus(" ")).substringBefore(delimiter = ' ')
        else null
        return name ?: newName ?: text
    }
}
