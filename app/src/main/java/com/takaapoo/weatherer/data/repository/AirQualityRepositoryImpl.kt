package com.takaapoo.weatherer.data.repository

import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.local.WeatherDao
import com.takaapoo.weatherer.data.remote.AirQualityApiService
import com.takaapoo.weatherer.data.remote.WholeAirQuality
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.AirQualityRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
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
        val rawList = weatherDao.getLocationAirQuality(locationId, startDate, endDate)

        val size = LocalDate.parse(startDate).until(LocalDate.parse(endDate).plusDays(1)).days * 24
        val times = List(size){
            LocalDate.parse(startDate).atStartOfDay().plusHours(it.toLong()).toString()
        }
        val airQuality = MutableList(size){
            LocalAirQuality(locationId, times[it])
        }

        var rawIndex = 0
        times.forEachIndexed { index, time ->
            if (rawList.getOrNull(rawIndex)?.time == time){
                airQuality[index] = rawList[rawIndex]
                rawIndex++
            }
        }
        return airQuality
    }


    // Remote Server Functions
    override suspend fun getAirQualityFromServer(
        latitude: Float,
        longitude: Float,
        pastDays: Int
    ): MyResult<WholeAirQuality> {
        val response = try {
            airQualityApiService.getAirQuality(latitude, longitude, pastDays)
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        return if (response.isSuccessful && response.body() != null) {
            MyResult.Success(response.body()!!)
        } else
            MyResult.Error(message = "Retrieving data from server was not successful")
    }
}