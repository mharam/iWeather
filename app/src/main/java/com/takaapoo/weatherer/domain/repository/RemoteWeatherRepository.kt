package com.takaapoo.weatherer.domain.repository

import com.takaapoo.weatherer.data.remote.DailyWeatherPack
import com.takaapoo.weatherer.data.remote.HistoricDailyWeatherPack
import com.takaapoo.weatherer.data.remote.HistoricHourlyWeatherPack
import com.takaapoo.weatherer.data.remote.HourlyWeatherPack
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity

interface RemoteWeatherRepository {
    suspend fun getHourlyWeatherFromServer(
        latitude: Float,
        longitude: Float,
//        currentWeather: Boolean,
        pastDays: Int
    ): MyResult<out HourlyWeatherPack>
    suspend fun getDailyWeatherFromServer(
        latitude: Float,
        longitude: Float,
        pastDays: Int
    ): MyResult<out DailyWeatherPack>

    suspend fun getHourlyWeatherBatchFromServer(
        latitude: List<Float>,
        longitude: List<Float>,
        pastDays: Int
    ): MyResult<out List<HourlyWeatherPack>>
    suspend fun getDailyWeatherBatchFromServer(
        latitude: List<Float>,
        longitude: List<Float>,
        pastDays: Int
    ): MyResult<out List<DailyWeatherPack>>

    suspend fun getHistoricHourlyWeatherFromServer(
        latitude: Float,
        longitude: Float,
        startDate: String,
        endDate: String,
        weatherQuantities: List<HourlyWeatherQuantity>
    ): MyResult<out HistoricHourlyWeatherPack>

    suspend fun getHistoricDailyWeatherFromServer(
        latitude: Float,
        longitude: Float,
        startDate: String,
        endDate: String,
        weatherQuantity: DailyWeatherQuantity
    ): MyResult<out HistoricDailyWeatherPack>

    suspend fun getYearlyWeatherFromServer(
        latitude: Float,
        longitude: Float,
        weatherQuantities: List<String>
    ): MyResult<out List<Float?>>
}