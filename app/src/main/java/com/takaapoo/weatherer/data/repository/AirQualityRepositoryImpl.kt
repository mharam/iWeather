package com.takaapoo.weatherer.data.repository

import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.local.WeatherDao
import com.takaapoo.weatherer.data.remote.AirQualityApiService
import com.takaapoo.weatherer.data.remote.WholeAirQuality
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.AirQualityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AirQualityRepositoryImpl @Inject constructor(
    private val weatherDao: WeatherDao,
    private val airQualityApiService: AirQualityApiService
): AirQualityRepository {
    override suspend fun addAirQuality(airQuality: List<LocalAirQuality>) {
        weatherDao.addAirQuality(airQuality)
    }

    override fun getAllAirQuality(): Flow<Map<Location, List<LocalAirQuality>>> {
        return weatherDao.getAllAirQuality()
    }

    override fun getLocationAirQualityFlow(
        locationId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<LocalAirQuality>> {
        return weatherDao.getLocationAirQualityFlow(locationId, startDate, endDate)
    }

    override suspend fun getLocationAirQuality(
        locationId: Int,
        startDate: String,
        endDate: String
    ): List<LocalAirQuality> {
        return weatherDao.getLocationAirQuality(locationId, startDate, endDate)
    }


    // Remote Server Functions
    override suspend fun getAirQualityFromServer(latitude: Float, longitude: Float): MyResult<WholeAirQuality> {
        val response = try {
            airQualityApiService.getAirQuality(latitude, longitude)
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        return if (response.isSuccessful && response.body() != null) {
            MyResult.Success(response.body()!!)
        } else
            MyResult.Error(message = "Retrieving data from server was not successful")
    }
}