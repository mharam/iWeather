package com.takaapoo.weatherer.domain.repository

import com.takaapoo.weatherer.data.remote.DailyWeatherPack
import com.takaapoo.weatherer.data.remote.HourlyWeatherPack
import com.takaapoo.weatherer.domain.MyResult

interface RemoteWeatherRepository {
    suspend fun getHourlyWeatherFromServer(
        latitude: Float,
        longitude: Float,
//        currentWeather: Boolean,
        pastDays: Int
    ): MyResult<HourlyWeatherPack>
    suspend fun getDailyWeatherFromServer(
        latitude: Float,
        longitude: Float,
        pastDays: Int
    ): MyResult<DailyWeatherPack>
}