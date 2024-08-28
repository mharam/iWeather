package com.takaapoo.weatherer.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val utcOffset: Long? = null
)
