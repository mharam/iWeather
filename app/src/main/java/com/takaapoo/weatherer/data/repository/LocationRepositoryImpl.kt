package com.takaapoo.weatherer.data.repository

import androidx.room.withTransaction
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.local.LocationDao
import com.takaapoo.weatherer.data.local.LocationType
import com.takaapoo.weatherer.data.local.WeatherDatabase
import com.takaapoo.weatherer.data.remote.LocationApiService
import com.takaapoo.weatherer.data.remote.LocationServerOutput
import com.takaapoo.weatherer.data.remote.LocationTypeApiService
import com.takaapoo.weatherer.data.remote.RemoteLocationType
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val weatherDatabase: WeatherDatabase,
    private val locationDao: LocationDao,
    private val locationApiService: LocationApiService,
    private val locationTypeApiService: LocationTypeApiService
): LocationRepository {
    override suspend fun addLocation(location: Location): Int {
        locationDao.addLocation(location)
        return locationDao.getLocationIdWithName(locationName = location.name)
    }

    override fun getAllLocations(): Flow<List<Location>> {
        return locationDao.getAllLocations()
    }

    override fun getLocationFlow(locationId: Int): Flow<Location?> {
        return locationDao.getLocationFlow(locationId)
    }

    override suspend fun getLocation(locationId: Int): Location? {
        return locationDao.getLocation(locationId)
    }

    override suspend fun getAllLocationsList(): List<Location> {
        return locationDao.getAllLocationsList()
    }

    override suspend fun deleteLocation(locationName: String) {
        locationDao.deleteLocation(locationName)
    }

    override suspend fun deleteLocation(locationId: Int) {
        locationDao.deleteLocation(locationId)
    }

    override suspend fun countLocationWithName(locationName: String): Int {
        return locationDao.countLocationWithName(locationName)
    }

    override suspend fun updateLocation(location: Location) {
        locationDao.updateLocation(location)
    }

    override suspend fun updateLocationName(locationId: Int, newLocationName: String) {
        locationDao.updateLocationName(locationId, newLocationName)
    }

    override suspend fun updateLocationLastModifiedTime(name: String, lastModifiedTime: String?) {
        locationDao.updateLocationLastModifiedTime(name, lastModifiedTime)
    }

    override suspend fun updateLocationUtcOffset(name: String, utcOffset: Long?) {
        locationDao.updateLocationUtcOffset(name, utcOffset)
    }

    override suspend fun updateLocationCustomId(id: Int, customId: Int) {
        locationDao.updateLocationCustomId(id, customId)
    }

    override suspend fun swapLocationCustomId(id1: Int, id2: Int) {
        val customId1 = locationDao.getLocationCustomId(id1)
        val customId2 = locationDao.getLocationCustomId(id2)
        weatherDatabase.withTransaction {
            locationDao.updateLocationCustomId(id1, customId2)
            locationDao.updateLocationCustomId(id2, customId1)
        }
    }

    override suspend fun updateLocationType(name: String, locationType: LocationType) {
        locationDao.updateLocationType(name, locationType)
    }

    override suspend fun updateLocationElevation(name: String, elevation: Float) {
        locationDao.updateLocationElevation(name, elevation)
    }

    override suspend fun updateLocationPrecipitation(name: String, precipitation: Float?) {
        locationDao.updateLocationPrecipitation(name, precipitation)
    }

    override suspend fun updateLocationPicInfo(name: String, currentPicUrl: String, picIds: List<Int>) {
        locationDao.updateLocationPicIds(name, currentPicUrl, picIds)
    }

    override fun locationsCount(): Flow<Int> {
        return locationDao.locationCount()
    }

    override suspend fun deleteFaultyLocations() {
        locationDao.deleteFaultyLocations()
    }


    // Remote server functions
    override suspend fun getLocation(text: String): MyResult<out LocationServerOutput> {
        val response = try {
            locationApiService.getLocation(text)
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        return if (response.isSuccessful && response.body() != null)
            MyResult.Success(response.body()!!)
        else
            MyResult.Error(message = "Retrieving data from server was not successful")
    }

    override suspend fun getLocationTypeFromServer(
        latitude: Float,
        longitude: Float,
    ): MyResult<out RemoteLocationType> {
        val response = runCatching {
            locationTypeApiService.getLocationType(latLng = "$latitude/$longitude")
        }.onFailure {
            return MyResult.Error(exception = it as Exception?)
        }.getOrNull()

        return if (response!!.isSuccessful && response.body() != null)
            MyResult.Success(response.body()!!)
        else
            MyResult.Error(message = "Retrieving data from server was not successful")
    }
}
