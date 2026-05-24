package com.takaapoo.weatherer.data.repository

import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.LocalHistoricDailyWeather
import com.takaapoo.weatherer.data.local.LocalHistoricHourlyWeather
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.local.WeatherDao
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeLocalWeatherRepositoryImpl(
    private val weatherDao: WeatherDao
): LocalWeatherRepository {
    override suspend fun addHourlyWeather(localHourlyWeather: List<LocalHourlyWeather>) {

    }

    override suspend fun addDailyWeather(dailyWeather: List<LocalDailyWeather>) {

    }

    override fun getAllHourlyWeather(): Flow<Map<Location, List<LocalHourlyWeather>>> {
        return emptyFlow()
    }

    override fun getLocationHourlyWeatherFlow(
        locationId: Int,
        startDate: String,
        endDate: String,
    ): Flow<List<LocalHourlyWeather>> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocationHourlyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
        appSettings: AppSettings,
    ): List<LocalHourlyWeather> {
        TODO("Not yet implemented")
    }

    override suspend fun getDailyWeather(
        startDate: String,
        endDate: String,
    ): Map<Int, List<LocalDailyWeather>> {
        return weatherDao.getDailyWeather(startDate, endDate).mapKeys { (location, _) -> location.id }
    }

    override suspend fun getLocationDailyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
        appSettings: AppSettings,
    ): List<LocalDailyWeather> {
        TODO("Not yet implemented")
    }

    override fun getAllTimes(): Flow<List<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocationHourlyWeatherForTime(
        locationId: Int,
        startTime: String,
        endTime: String,
    ): List<LocalHourlyWeather> {
        TODO("Not yet implemented")
    }

    override suspend fun getHourlyWeather(
        startDate: String,
        endDate: String,
    ): Map<Int, List<LocalHourlyWeather>> {
        return weatherDao.getHourlyWeather(startDate, endDate).mapKeys { (location, _) -> location.id }
    }

    override suspend fun getHourlyAirQuality(
        startDate: String,
        endDate: String,
    ): Map<Int, List<LocalAirQuality>> {
        return weatherDao.getHourlyAirQuality(startDate, endDate).mapKeys { (location, _) -> location.id }
    }

    override suspend fun getLocationHistoricHourlyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
    ): List<LocalHistoricHourlyWeather> {
        TODO("Not yet implemented")
    }

    override suspend fun getRowCounts(): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertHistoricHourlyColumn(
        quantity: HourlyWeatherQuantity,
        localHistoricHourlyWeather: List<LocalHistoricHourlyWeather>,
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteHistoricHourlyWeather() {
        TODO("Not yet implemented")
    }

    override suspend fun getLocationHistoricDailyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
    ): List<LocalHistoricDailyWeather> {
        TODO("Not yet implemented")
    }

    override suspend fun insertHistoricDailyColumn(
        quantity: DailyWeatherQuantity,
        localHistoricDailyWeather: List<LocalHistoricDailyWeather>,
    ) {
        TODO("Not yet implemented")
    }
}