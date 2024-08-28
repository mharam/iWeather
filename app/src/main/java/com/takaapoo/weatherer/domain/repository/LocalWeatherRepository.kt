package com.takaapoo.weatherer.domain.repository

import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.WeatherDto
import kotlinx.coroutines.flow.Flow

interface LocalWeatherRepository {
    suspend fun addHourlyWeather(localHourlyWeather: List<LocalHourlyWeather>)
    suspend fun addDailyWeather(dailyWeather: List<LocalDailyWeather>)
    fun getAllHourlyWeather(): Flow<Map<Location, List<LocalHourlyWeather>>>
    fun getLocationHourlyWeatherFlow(locationId: Int, startDate: String, endDate: String):
            Flow<List<LocalHourlyWeather>>
    suspend fun getLocationHourlyWeather(locationId: Int, startDate: String, endDate: String): List<LocalHourlyWeather>
    suspend fun getDailyWeather(time: String): Map<Location, LocalDailyWeather>
    fun getLocationDailyWeatherFlow(locationId: Int, startDate: String, endDate: String): Flow<List<LocalDailyWeather>>
    suspend fun getLocationDailyWeather(locationId: Int, startDate: String, endDate: String): List<LocalDailyWeather>

    suspend fun getAllLocationsWeather(startDate: String, endDate: String) : List<WeatherDto>
    fun getAllTimes(): Flow<List<String>>

    suspend fun getLocationHourlyWeatherForTime(locationId: Int, startTime: String, endTime: String):
            List<LocalHourlyWeather>
}