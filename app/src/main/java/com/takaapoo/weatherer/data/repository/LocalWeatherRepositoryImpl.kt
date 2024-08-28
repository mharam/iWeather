package com.takaapoo.weatherer.data.repository

import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.local.WeatherDao
import com.takaapoo.weatherer.domain.model.WeatherDto
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalWeatherRepositoryImpl @Inject constructor(
    private val weatherDao: WeatherDao
) : LocalWeatherRepository{

    override suspend fun addHourlyWeather(localHourlyWeather: List<LocalHourlyWeather>) {
        weatherDao.addHourlyWeather(localHourlyWeather)
    }

    override suspend fun addDailyWeather(dailyWeather: List<LocalDailyWeather>) {
        weatherDao.addDailyWeather(dailyWeather)
    }

    override fun getAllHourlyWeather(): Flow<Map<Location, List<LocalHourlyWeather>>> {
        return weatherDao.getAllHourlyWeather()
    }

    override fun getLocationHourlyWeatherFlow(locationId: Int, startDate: String, endDate: String):
            Flow<List<LocalHourlyWeather>> {
        return weatherDao.getLocationHourlyWeatherFlow(locationId, startDate, endDate)
    }

    override suspend fun getLocationHourlyWeather(locationId: Int, startDate: String, endDate: String):
            List<LocalHourlyWeather> {
        return weatherDao.getLocationHourlyWeather(locationId, startDate, endDate)
    }



    override suspend fun getDailyWeather(time: String): Map<Location, LocalDailyWeather> {
        return weatherDao.getDailyWeather(time)
    }

    override fun getLocationDailyWeatherFlow(
        locationId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<LocalDailyWeather>> {
        return weatherDao.getLocationDailyWeatherFlow(locationId, startDate, endDate)
    }

    override suspend fun getLocationDailyWeather(
        locationId: Int,
        startDate: String,
        endDate: String
    ): List<LocalDailyWeather> {
        return weatherDao.getLocationDailyWeather(locationId, startDate, endDate)
    }

    override suspend fun getAllLocationsWeather(startDate: String, endDate: String): List<WeatherDto> {
        return weatherDao.getAllLocationsWeather(startDate, endDate)
    }

    override fun getAllTimes(): Flow<List<String>> {
        return weatherDao.getAllTimes()
    }

    override suspend fun getLocationHourlyWeatherForTime(
        locationId: Int,
        startTime: String,
        endTime: String
    ): List<LocalHourlyWeather> {
        return weatherDao.getLocationHourlyWeatherForTime(locationId, startTime, endTime)
    }


}
