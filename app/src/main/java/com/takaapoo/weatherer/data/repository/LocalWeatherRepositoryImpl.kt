package com.takaapoo.weatherer.data.repository

import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.local.WeatherDao
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.WeatherDto
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.ui.utility.celsiusToUnit
import com.takaapoo.weatherer.ui.utility.cmToUnit
import com.takaapoo.weatherer.ui.utility.kmToUnit
import com.takaapoo.weatherer.ui.utility.kmphToUnit
import com.takaapoo.weatherer.ui.utility.mToUnit
import com.takaapoo.weatherer.ui.utility.mmToUnit
import com.takaapoo.weatherer.ui.utility.paToUnit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.ChronoUnit
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

    override suspend fun getLocationHourlyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
        appSettings: AppSettings
    ): List<LocalHourlyWeather> {
        val rawList = weatherDao.getLocationHourlyWeather(locationId, startDate, endDate).map {
            it.copy(
                temperature = it.temperature?.celsiusToUnit(appSettings.temperatureUnit),
                temperatureControl1Y = it.temperatureControl1Y?.celsiusToUnit(appSettings.temperatureUnit),
                temperatureControl2Y = it.temperatureControl2Y?.celsiusToUnit(appSettings.temperatureUnit),
                dewPoint = it.dewPoint?.celsiusToUnit(appSettings.temperatureUnit),
                dewPointControl1Y = it.dewPointControl1Y?.celsiusToUnit(appSettings.temperatureUnit),
                dewPointControl2Y = it.dewPointControl2Y?.celsiusToUnit(appSettings.temperatureUnit),
                apparentTemperature = it.apparentTemperature?.celsiusToUnit(appSettings.temperatureUnit),
                apparentTemperatureControl1Y = it.apparentTemperatureControl1Y?.celsiusToUnit(appSettings.temperatureUnit),
                apparentTemperatureControl2Y = it.apparentTemperatureControl2Y?.celsiusToUnit(appSettings.temperatureUnit),
                precipitation = it.precipitation?.mmToUnit(appSettings.lengthUnit),
                rain = it.rain?.mmToUnit(appSettings.lengthUnit),
                showers = it.showers?.mmToUnit(appSettings.lengthUnit),
                snowfall = it.snowfall?.cmToUnit(appSettings.lengthUnit),
                surfacePressure = it.surfacePressure?.paToUnit(appSettings.pressureUnit),
                surfacePressureControl1Y = it.surfacePressureControl1Y?.paToUnit(appSettings.pressureUnit),
                surfacePressureControl2Y = it.surfacePressureControl2Y?.paToUnit(appSettings.pressureUnit),
                visibility = it.visibility?.kmToUnit(appSettings.lengthUnit),
                visibilityControl1Y = it.visibilityControl1Y?.kmToUnit(appSettings.lengthUnit),
                visibilityControl2Y = it.visibilityControl2Y?.kmToUnit(appSettings.lengthUnit),
                windSpeed = it.windSpeed?.kmphToUnit(appSettings.speedUnit),
                windSpeedControl1Y = it.windSpeedControl1Y?.kmphToUnit(appSettings.speedUnit),
                windSpeedControl2Y = it.windSpeedControl2Y?.kmphToUnit(appSettings.speedUnit),
                freezingLevelHeight = it.freezingLevelHeight?.mToUnit(appSettings.lengthUnit),
                freezingLevelHeightControl1Y = it.freezingLevelHeightControl1Y?.mToUnit(appSettings.lengthUnit),
                freezingLevelHeightControl2Y = it.freezingLevelHeightControl2Y?.mToUnit(appSettings.lengthUnit),
            )
        }

        val size = ChronoUnit.DAYS.between(
            LocalDate.parse(startDate),
            LocalDate.parse(endDate).plusDays(1)
        ).toInt() * 24
        val times = List(size){
            LocalDate.parse(startDate).atStartOfDay().plusHours(it.toLong()).toString()
        }
        val hourlyWeather = MutableList(size){
            LocalHourlyWeather(locationId, times[it])
        }

        var rawIndex = 0
        times.forEachIndexed { index, time ->
            if (rawList.getOrNull(rawIndex)?.time == time){
                hourlyWeather[index] = rawList[rawIndex]
                rawIndex++
            }
        }
        return hourlyWeather
    }



//    override suspend fun getDailyWeather(time: String): Map<Location, LocalDailyWeather> {
//        return weatherDao.getDailyWeather(time)
//    }
//
//    override fun getLocationDailyWeatherFlow(
//        locationId: Int,
//        startDate: String,
//        endDate: String
//    ): Flow<List<LocalDailyWeather>> {
//        return weatherDao.getLocationDailyWeatherFlow(locationId, startDate, endDate)
//    }

    override suspend fun getLocationDailyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
        appSettings: AppSettings
    ): List<LocalDailyWeather> {
        val rawList = weatherDao.getLocationDailyWeather(locationId, startDate, endDate).map {
            it.copy(
                temperatureMax = it.temperatureMax?.celsiusToUnit(appSettings.temperatureUnit),
                temperatureMin = it.temperatureMin?.celsiusToUnit(appSettings.temperatureUnit),
                precipitationSum = it.precipitationSum?.mmToUnit(appSettings.lengthUnit),
                windSpeedMax = it.windSpeedMax?.kmphToUnit(appSettings.speedUnit),
                windGustsMax = it.windGustsMax?.kmphToUnit(appSettings.speedUnit)
            )
        }

        val size = ChronoUnit.DAYS.between(
            LocalDate.parse(startDate),
            LocalDate.parse(endDate).plusDays(1)
        ).toInt()
        val dates = List(size){
            LocalDate.parse(startDate).plusDays(it.toLong()).toString()
        }
        val dailyWeather = MutableList(size){
            LocalDailyWeather(locationId, dates[it])
        }

        var rawIndex = 0
        dates.forEachIndexed { index, date ->
            if (rawList.getOrNull(rawIndex)?.time == date){
                dailyWeather[index] = rawList[rawIndex]
                rawIndex++
            }
        }
        return dailyWeather
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
