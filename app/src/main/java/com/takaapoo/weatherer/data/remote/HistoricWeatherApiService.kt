package com.takaapoo.weatherer.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HistoricWeatherApiService {
    @GET("""v1/archive/?""")
    suspend fun getHistoricHourlyWeather(
        @Query("latitude") latitude: Float,
        @Query("longitude") longitude: Float,
        @Query("timezone") timeZone: String = "GMT",
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("hourly") hourly: List<String>
    ): Response<HistoricHourlyWeatherPack>

    @GET("""v1/archive/?""")
    suspend fun getHistoricDailyWeather(
        @Query("latitude") latitude: Float,
        @Query("longitude") longitude: Float,
        @Query("timezone") timeZone: String = "auto",
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("daily") daily: List<String>
    ): Response<HistoricDailyWeatherPack>

    @GET("""v1/forecast/?daily=precipitation_sum""")
    suspend fun getHistoricDailyPrecipitationBatch(
        @Query("latitude") latitude: List<Float>,
        @Query("longitude") longitude: List<Float>,
        @Query("timezone") timeZone: String = "auto",
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): Response<List<HistoricDailyWeatherPack>>
}