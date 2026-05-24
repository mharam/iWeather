package com.takaapoo.weatherer.data.repository

import com.takaapoo.weatherer.data.remote.DailyWeatherPack
import com.takaapoo.weatherer.data.remote.HistoricDailyWeatherPack
import com.takaapoo.weatherer.data.remote.HistoricHourlyWeatherPack
import com.takaapoo.weatherer.data.remote.HistoricWeatherApiService
import com.takaapoo.weatherer.data.remote.HourlyWeatherPack
import com.takaapoo.weatherer.data.remote.WeatherApiService
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.RemoteWeatherRepository
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.utility.getPropertyBySerialName
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject


class RemoteWeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val historicWeatherApiService: HistoricWeatherApiService
) : RemoteWeatherRepository {

    override suspend fun getHourlyWeatherFromServer(
        latitude: Float,
        longitude: Float,
//        currentWeather: Boolean,
        pastDays: Int
    ): MyResult<out HourlyWeatherPack> {
        val response = try {
            weatherApiService.getHourlyWeather(
                latitude = latitude,
                longitude = longitude,
                pastDays = pastDays
//                currentWeather = true
            )
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        return if (response.isSuccessful && response.body() != null) {
            MyResult.Success(response.body()!!)
        } else {
            MyResult.Error(message = "Retrieving data from server failed")
        }
    }

    override suspend fun getDailyWeatherFromServer(
        latitude: Float,
        longitude: Float,
        pastDays: Int
    ): MyResult<out DailyWeatherPack> {
        val response = try {
            weatherApiService.getDailyWeather(
                latitude = latitude,
                longitude = longitude,
                pastDays = pastDays
            )
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        return if (response.isSuccessful && response.body() != null) {
            MyResult.Success(response.body()!!)
        } else
            MyResult.Error(message = "Retrieving data from server failed")
    }

    override suspend fun getHourlyWeatherBatchFromServer(
        latitude: List<Float>,
        longitude: List<Float>,
        pastDays: Int,
    ): MyResult<out List<HourlyWeatherPack>> {
        if (latitude.size == 1) {
            val response = try {
                weatherApiService.getHourlyWeather(
                    latitude = latitude.first(),
                    longitude = longitude.first(),
                    pastDays = pastDays
                )
            } catch (e: Exception){
                return MyResult.Error(exception = e)
            }
            return if (response.isSuccessful && response.body() != null) {
                MyResult.Success(listOf(response.body()!!))
            } else
                MyResult.Error(message = "Retrieving data from server failed")
        } else {
            val response = try {
                weatherApiService.getHourlyWeatherBatch(
                    latitude = latitude,
                    longitude = longitude,
                    pastDays = pastDays
                )
            } catch (e: Exception) {
                return MyResult.Error(exception = e)
            }
            return if (response.isSuccessful && response.body() != null) {
                MyResult.Success(response.body()!!)
            } else
                MyResult.Error(message = "Retrieving data from server failed")
        }
    }

    override suspend fun getDailyWeatherBatchFromServer(
        latitude: List<Float>,
        longitude: List<Float>,
        pastDays: Int,
    ): MyResult<out List<DailyWeatherPack>> {
        if (latitude.size == 1) {
            val response = try {
                weatherApiService.getDailyWeather(
                    latitude = latitude.first(),
                    longitude = longitude.first(),
                    pastDays = pastDays
                )
            } catch (e: Exception) {
                return MyResult.Error(exception = e)
            }
            return if (response.isSuccessful && response.body() != null) {
                MyResult.Success(listOf(response.body()!!))
            } else
                MyResult.Error(message = "Retrieving data from server failed")
        } else {
            val response = try {
                weatherApiService.getDailyWeatherBatch(
                    latitude = latitude,
                    longitude = longitude,
                    pastDays = pastDays
                )
            } catch (e: Exception) {
                return MyResult.Error(exception = e)
            }
            return if (response.isSuccessful && response.body() != null) {
                MyResult.Success(response.body()!!)
            } else
                MyResult.Error(message = "Retrieving data from server failed")
        }
    }

    override suspend fun getHistoricHourlyWeatherFromServer(
        latitude: Float,
        longitude: Float,
        startDate: String,
        endDate: String,
        weatherQuantities: List<HourlyWeatherQuantity>,
    ): MyResult<out HistoricHourlyWeatherPack> {
        val response = try {
            historicWeatherApiService.getHistoricHourlyWeather(
                latitude = latitude,
                longitude = longitude,
                startDate = startDate,
                endDate = endDate,
                hourly = weatherQuantities.map { it.serverName!! } + listOf("is_day", "weathercode")
            )
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        return if (response.isSuccessful && response.body() != null) {
            MyResult.Success(response.body()!!)
        } else
            MyResult.Error(message = "Retrieving data from server failed! Check your internet connection and try again later.")
    }

    override suspend fun getHistoricDailyWeatherFromServer(
        latitude: Float,
        longitude: Float,
        startDate: String,
        endDate: String,
        weatherQuantity: DailyWeatherQuantity,
    ): MyResult<out HistoricDailyWeatherPack> {
        val response = try {
            historicWeatherApiService.getHistoricDailyWeather(
                latitude = latitude,
                longitude = longitude,
                startDate = startDate,
                endDate = endDate,
                daily = listOf("weather_code") + weatherQuantity.serverName!!.split(','),
            )
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        return if (response.isSuccessful && response.body() != null) {
            MyResult.Success(response.body()!!)
        } else
            MyResult.Error(message = "Retrieving data from server failed! Check your internet connection and try again later.")
    }

    override suspend fun getYearlyWeatherFromServer(
        latitude: Float,
        longitude: Float,
        weatherQuantities: List<String>,
    ): MyResult<out List<Float?>> {
        val startDate = "2006-01-01"
        val endDate = "2015-12-31"
        val daysCount = ChronoUnit.DAYS.between(
            LocalDate.parse(startDate),
            LocalDate.parse(endDate).plusDays(1)
        )
        val response = try {
            historicWeatherApiService.getHistoricDailyWeather(
                latitude = latitude,
                longitude = longitude,
                startDate = startDate,
                endDate = endDate,
                daily = weatherQuantities,
            )
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        return if (response.isSuccessful && response.body() != null) {
            val wholeQuantity = weatherQuantities.map { quantityString ->
                val propertyList = getPropertyBySerialName<List<Float?>>(
                    response.body()!!.dailyWeather,
                    quantityString
                ) ?: return@map null
                val sum = propertyList.filterNotNull().sum()
                val nullDays = propertyList.count { it == null }
                365 * sum / (daysCount - nullDays)
            }
            MyResult.Success(wholeQuantity)
        } else
            MyResult.Error(message = "Retrieving data from server failed! Check your internet connection and try again later.")
    }

    override suspend fun getYearlyPrecipitationFromServerBatch(
        latitude: List<Float>,
        longitude: List<Float>,
    ): MyResult<out List<Float?>> {
        val startDate = "2006-01-01"
        val endDate = "2015-12-31"
        val daysCount = ChronoUnit.DAYS.between(
            LocalDate.parse(startDate),
            LocalDate.parse(endDate).plusDays(1)
        )
        val response = try {
            historicWeatherApiService.getHistoricDailyPrecipitationBatch(
                latitude = latitude,
                longitude = longitude,
                startDate = startDate,
                endDate = endDate,
            )
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        return if (response.isSuccessful && response.body() != null) {
            val result = response.body()!!.map { dailyWeatherPack ->
                val sum = dailyWeatherPack.dailyWeather.precipitationSum.filterNotNull().sum()
                val nullDays = dailyWeatherPack.dailyWeather.precipitationSum.count { it == null }
                365 * sum / (daysCount - nullDays)
            }
            MyResult.Success(result)
        } else
            MyResult.Error(message = "Retrieving data from server failed! Check your internet connection and try again later.")

    }
}
