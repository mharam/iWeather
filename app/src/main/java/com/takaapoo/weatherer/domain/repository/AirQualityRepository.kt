package com.takaapoo.weatherer.domain.repository

import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.remote.WholeAirQuality
import com.takaapoo.weatherer.domain.MyResult
import kotlinx.coroutines.flow.Flow


interface AirQualityRepository {
    suspend fun addAirQuality(airQuality: List<LocalAirQuality>)
    fun getAllAirQuality(): Flow<Map<Location, List<LocalAirQuality>>>
    fun getLocationAirQualityFlow(locationId: Int, startDate: String, endDate: String):
            Flow<List<LocalAirQuality>>
    suspend fun getLocationAirQuality(locationId: Int, startDate: String, endDate: String): List<LocalAirQuality>



    // Remote server functions
    suspend fun getAirQualityFromServer(latitude: Float, longitude: Float): MyResult<WholeAirQuality>
}