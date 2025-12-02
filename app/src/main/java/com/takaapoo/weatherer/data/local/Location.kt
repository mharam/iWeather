package com.takaapoo.weatherer.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(
    tableName = "locations",
    indices = [Index(value = ["name"], unique = true)]
)
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "custom_id")
    val customId: Int = 0,
    val name: String = "?",
    val latitude: Float = 0f,
    val longitude: Float = 0f,
    @ColumnInfo(name = "modified_time")
    val lastModifiedTime: String? = null,
    @ColumnInfo(name = "utc_offset")
    val utcOffset: Long? = null,
    @ColumnInfo(name = "location_type")
    val locationType: LocationType = LocationType.LAND,
    val elevation: Float = -1000f,
    val yearlyPrecipitation: Float? = null,
    val currentPictureUrl: String = "",
    val picIdsList: List<Int> = emptyList(),
)

enum class LocationType {
    OCEAN, BEACH, LAND, LAKE, RIVER
}

// String Converters
class Converters {
    @TypeConverter
    fun fromString(value: String): List<Int> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { it.trim().toInt() }
    }

    @TypeConverter
    fun toString(list: List<Int>): String {
        return list.joinToString(",")
    }
}