package com.takaapoo.weatherer.domain.repository

import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.remote.LocationServerOutput
import com.takaapoo.weatherer.domain.MyResult
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun addLocation(location: Location): Int
    fun getAllLocations(): Flow<List<Location>>
    fun getLocation(locationId: Int): Flow<Location>
    suspend fun getAllLocationsList(): List<Location>
    suspend fun deleteLocation(locationName: String)
    suspend fun deleteLocation(locationId: Int)
    suspend fun countLocationWithName(locationName: String) : Int
    suspend fun updateLocation(location: Location)
    suspend fun updateLocationName(locationId: Int, newLocationName: String)
    suspend fun updateLocationLastModifiedTime(name: String, lastModifiedTime: String?)
    suspend fun updateLocationUtcOffset(name: String, utcOffset:Long?)
    suspend fun updateLocationCustomId(id: Int, customId: Int)
    suspend fun swapLocationCustomId(id1: Int, id2: Int)


    // Remote server functions
    suspend fun getLocation(text: String): MyResult<LocationServerOutput>
}