package com.takaapoo.weatherer.domain.repository

import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.LocalHistoricDailyWeather
import com.takaapoo.weatherer.data.local.LocalHistoricHourlyWeather
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import kotlinx.coroutines.flow.Flow

interface LocalWeatherRepository {
    suspend fun addHourlyWeather(localHourlyWeather: List<LocalHourlyWeather>)
    suspend fun addDailyWeather(dailyWeather: List<LocalDailyWeather>)
    fun getAllHourlyWeather(): Flow<Map<Location, List<LocalHourlyWeather>>>
    fun getLocationHourlyWeatherFlow(locationId: Int, startDate: String, endDate: String):
            Flow<List<LocalHourlyWeather>>
    suspend fun getLocationHourlyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
        appSettings: AppSettings
    ): List<LocalHourlyWeather>

    suspend fun getDailyWeather(startDate: String, endDate: String): Map<Int, List<LocalDailyWeather>>
//    fun getLocationDailyWeatherFlow(locationId: Int, startDate: String, endDate: String): Flow<List<LocalDailyWeather>>

    suspend fun getLocationDailyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
        appSettings: AppSettings
    ): List<LocalDailyWeather>

//    suspend fun getAllLocationsWeather(startDate: String, endDate: String) : List<WeatherDto>
    fun getAllTimes(): Flow<List<String>>

    suspend fun getLocationHourlyWeatherForTime(locationId: Int, startTime: String, endTime: String):
            List<LocalHourlyWeather>

    suspend fun getHourlyWeather(startDate: String, endDate: String): Map<Int, List<LocalHourlyWeather>>
    suspend fun getHourlyAirQuality(startDate: String, endDate: String): Map<Int, List<LocalAirQuality>>


    suspend fun getLocationHistoricHourlyWeather(locationId: Int, startDate: String, endDate: String):
            List<LocalHistoricHourlyWeather>

    suspend fun getRowCounts(): Long

    suspend fun insertHistoricHourlyColumn(
        quantity: HourlyWeatherQuantity,
        localHistoricHourlyWeather: List<LocalHistoricHourlyWeather>,
    )

    suspend fun deleteHistoricHourlyWeather()

    suspend fun getLocationHistoricDailyWeather(locationId: Int, startDate: String, endDate: String):
            List<LocalHistoricDailyWeather>

    suspend fun insertHistoricDailyColumn(
        quantity: DailyWeatherQuantity,
        localHistoricDailyWeather: List<LocalHistoricDailyWeather>,
    )
}