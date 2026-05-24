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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Location

        if (geometry != other.geometry) return false
        if (properties != other.properties) return false
        if (!boundingBox.contentEquals(other.boundingBox)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = geometry.hashCode()
        result = 31 * result + properties.hashCode()
        result = 31 * result + (boundingBox?.contentHashCode() ?: 0)
        return result
    }
}

@Serializable
data class Geometry(
    val coordinates: DoubleArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Geometry

        return coordinates.contentEquals(other.coordinates)
    }

    override fun hashCode(): Int {
        return coordinates.contentHashCode()
    }
}

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
