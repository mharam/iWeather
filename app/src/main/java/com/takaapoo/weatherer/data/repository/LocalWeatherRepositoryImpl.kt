package com.takaapoo.weatherer.data.repository

import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.LocalHistoricDailyWeather
import com.takaapoo.weatherer.data.local.LocalHistoricHourlyWeather
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.local.SingleQuantityDailyWeather.CloudCoverDailyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityDailyWeather.HumidityDailyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityDailyWeather.PrecipitationDailyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityDailyWeather.TemperatureMeanDailyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityDailyWeather.TemperatureMinMaxDailyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityDailyWeather.WindSpeedDailyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.ApparentTemperatureHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.CloudCoverHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.DewPointHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.HumidityHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.PrecipitationHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.RainHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.SnowfallHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.SurfacePressureHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.TemperatureHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.WindDirectionHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.WindSpeedHourlyWeather
import com.takaapoo.weatherer.data.local.WeatherDao
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.utility.celsiusToUnit
import com.takaapoo.weatherer.utility.cmToUnit
import com.takaapoo.weatherer.utility.kmToUnit
import com.takaapoo.weatherer.utility.kmphToUnit
import com.takaapoo.weatherer.utility.mToUnit
import com.takaapoo.weatherer.utility.mmToUnit
import com.takaapoo.weatherer.utility.paToUnit
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
        if (size < 1) return emptyList()

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



    override suspend fun getDailyWeather(startDate: String, endDate: String): Map<Int, List<LocalDailyWeather>> {
        return weatherDao.getDailyWeather(startDate, endDate).mapKeys { (location, _) -> location.id }
    }
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
        if (size < 1) return emptyList()

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

//    override suspend fun getAllLocationsWeather(startDate: String, endDate: String): List<WeatherDto> {
//        return weatherDao.getAllLocationsWeather(startDate, endDate)
//    }

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

    override suspend fun getHourlyWeather(
        startDate: String,
        endDate: String
    ): Map<Int, List<LocalHourlyWeather>> {
        return weatherDao.getHourlyWeather(startDate, endDate).mapKeys { (location, _) -> location.id }
    }

    override suspend fun getHourlyAirQuality(
        startDate: String,
        endDate: String
    ): Map<Int, List<LocalAirQuality>> {
        return weatherDao.getHourlyAirQuality(startDate, endDate).mapKeys { (location, _) -> location.id }
    }

    override suspend fun getLocationHistoricHourlyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
    ): List<LocalHistoricHourlyWeather> {
        val rawList = weatherDao.getLocationHistoricHourlyWeather(locationId, startDate, endDate)

        val size = ChronoUnit.DAYS.between(
            LocalDate.parse(startDate),
            LocalDate.parse(endDate).plusDays(1)
        ).toInt() * 24
        if (size < 1) return emptyList()

        val times = List(size){
            LocalDate.parse(startDate).atStartOfDay().plusHours(it.toLong()).toString()
        }
        val hourlyWeather = MutableList(size){
            LocalHistoricHourlyWeather(locationId, times[it])
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

    override suspend fun getRowCounts(): Long {
        return weatherDao.getRowCounts()
    }

    override suspend fun insertHistoricHourlyColumn(
        quantity: HourlyWeatherQuantity,
        localHistoricHourlyWeather: List<LocalHistoricHourlyWeather>,
    ) {
        weatherDao.insertColumn(
            when (quantity) {
                HourlyWeatherQuantity.TEMPERATURE -> {
                    localHistoricHourlyWeather.map {
                        TemperatureHourlyWeather(it.locationId, it.time, it.temperature, it.weatherCode, it.isDay)
                    }
                }
                HourlyWeatherQuantity.HUMIDITY -> {
                    localHistoricHourlyWeather.map {
                        HumidityHourlyWeather(it.locationId, it.time, it.humidity, it.weatherCode, it.isDay)
                    }
                }
                HourlyWeatherQuantity.DEWPOINT -> {
                    localHistoricHourlyWeather.map {
                        DewPointHourlyWeather(it.locationId, it.time, it.dewPoint, it.weatherCode, it.isDay)
                    }
                }
                HourlyWeatherQuantity.APPARENTTEMP -> {
                    localHistoricHourlyWeather.map {
                        ApparentTemperatureHourlyWeather(it.locationId, it.time, it.apparentTemperature, it.weatherCode, it.isDay)
                    }
                }
                HourlyWeatherQuantity.PRECIPITATION -> {
                    localHistoricHourlyWeather.map {
                        PrecipitationHourlyWeather(it.locationId, it.time, it.precipitation, it.weatherCode, it.isDay)
                    }
                }
                HourlyWeatherQuantity.RAIN -> {
                    localHistoricHourlyWeather.map {
                        RainHourlyWeather(it.locationId, it.time, it.rain, it.weatherCode, it.isDay)
                    }
                }
                HourlyWeatherQuantity.SNOWFALL -> {
                    localHistoricHourlyWeather.map {
                        SnowfallHourlyWeather(it.locationId, it.time, it.snowfall, it.weatherCode, it.isDay)
                    }
                }
                HourlyWeatherQuantity.CLOUDCOVER -> {
                    localHistoricHourlyWeather.map {
                        CloudCoverHourlyWeather(it.locationId, it.time, it.cloudCover, it.weatherCode, it.isDay)
                    }
                }
                HourlyWeatherQuantity.SURFACEPRESSURE -> {
                    localHistoricHourlyWeather.map {
                        SurfacePressureHourlyWeather(it.locationId, it.time, it.surfacePressure, it.weatherCode, it.isDay)
                    }
                }
                HourlyWeatherQuantity.WINDSPEED -> {
                    localHistoricHourlyWeather.map {
                        WindSpeedHourlyWeather(it.locationId, it.time, it.windSpeed, it.weatherCode, it.isDay)
                    }
                }
                HourlyWeatherQuantity.WINDDIRECTION -> {
                    localHistoricHourlyWeather.map {
                        WindDirectionHourlyWeather(it.locationId, it.time, it.windDirection, it.weatherCode, it.isDay)
                    }
                }
                else -> emptyList()
            }
        )
    }

    override suspend fun deleteHistoricHourlyWeather() {
        weatherDao.deleteHistoricHourlyWeather()
    }

    override suspend fun getLocationHistoricDailyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
    ): List<LocalHistoricDailyWeather> {
        val rawList = weatherDao.getLocationHistoricDailyWeather(locationId, startDate, endDate)

        val size = ChronoUnit.DAYS.between(
            LocalDate.parse(startDate),
            LocalDate.parse(endDate).plusDays(1)
        ).toInt()
        if (size < 1) return emptyList()

        val dates = List(size){
            LocalDate.parse(startDate).plusDays(it.toLong()).toString()
        }
        val dailyWeather = MutableList(size){
            LocalHistoricDailyWeather(locationId, dates[it])
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

    override suspend fun insertHistoricDailyColumn(
        quantity: DailyWeatherQuantity,
        localHistoricDailyWeather: List<LocalHistoricDailyWeather>,
    ) {
        weatherDao.insertDailyColumn(
            when (quantity) {
                DailyWeatherQuantity.TEMPERATUREMINMAX -> {
                    localHistoricDailyWeather.map {
                        TemperatureMinMaxDailyWeather(it.locationId, it.time, it.temperatureMax,
                            it.temperatureMin, it.weatherCode)
                    }
                }
                DailyWeatherQuantity.TEMPERATUREMEAN -> {
                    localHistoricDailyWeather.map {
                        TemperatureMeanDailyWeather(it.locationId, it.time, it.temperatureMean, it.weatherCode)
                    }
                }
                DailyWeatherQuantity.PRECIPITATIONSUM -> {
                    localHistoricDailyWeather.map {
                        PrecipitationDailyWeather(it.locationId, it.time, it.precipitationSum, it.weatherCode)
                    }
                }
                DailyWeatherQuantity.WINDSPEEDMAX -> {
                    localHistoricDailyWeather.map {
                        WindSpeedDailyWeather(it.locationId, it.time, it.windSpeedMax, it.weatherCode)
                    }
                }
                DailyWeatherQuantity.CLOUDCOVERMEAN -> {
                    localHistoricDailyWeather.map {
                        CloudCoverDailyWeather(it.locationId, it.time, it.cloudCoverMean, it.weatherCode)
                    }
                }
                DailyWeatherQuantity.HUMIDITYMEAN -> {
                    localHistoricDailyWeather.map {
                        HumidityDailyWeather(it.locationId, it.time, it.relativeHumidityMean, it.weatherCode)
                    }
                }
                else -> emptyList()
            }
        )
    }

}
