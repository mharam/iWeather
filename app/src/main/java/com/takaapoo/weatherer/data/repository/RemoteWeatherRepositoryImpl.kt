package com.takaapoo.weatherer.data.repository

import com.takaapoo.weatherer.data.remote.DailyWeatherPack
import com.takaapoo.weatherer.data.remote.HourlyWeatherPack
import com.takaapoo.weatherer.data.remote.WeatherApiService
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.RemoteWeatherRepository
import javax.inject.Inject


class RemoteWeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService
) : RemoteWeatherRepository {

    override suspend fun getHourlyWeatherFromServer(
        latitude: Float,
        longitude: Float,
//        currentWeather: Boolean,
        pastDays: Int
    ): MyResult<HourlyWeatherPack> {
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
        } else
            MyResult.Error(message = "Retrieving data from server failed")
    }

    override suspend fun getDailyWeatherFromServer(
        latitude: Float,
        longitude: Float,
        pastDays: Int
    ): MyResult<DailyWeatherPack> {
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
}
