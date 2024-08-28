package com.takaapoo.weatherer.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherApiService {
    @GET("""v1/forecast/?hourly=temperature_2m,relativehumidity_2m,dewpoint_2m,apparent_temperature,precipitation_probability,precipitation,rain,showers,snowfall,cloud_cover,weathercode,surface_pressure,visibility,windspeed_10m,winddirection_10m,uv_index,is_day,freezinglevel_height,direct_radiation,direct_normal_irradiance&past_days=2&forecast_days=16""")
    suspend fun getHourlyWeather(
        @Query("latitude") latitude: Float,
        @Query("longitude") longitude: Float,
        @Query("current_weather") currentWeather: Boolean,
        @Query("timezone") timeZone: String = "GMT"
    ): Response<HourlyWeatherPack>

    @GET("""v1/forecast/?daily=weathercode,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_sum,precipitation_probability_max,windspeed_10m_max,windgusts_10m_max&past_days=2&forecast_days=16""")
    suspend fun getDailyWeather(
        @Query("latitude") latitude: Float,
        @Query("longitude") longitude: Float,
        @Query("timezone") timeZone: String = "auto"
    ): Response<DailyWeatherPack>
}