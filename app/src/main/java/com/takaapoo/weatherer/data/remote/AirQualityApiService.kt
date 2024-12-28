package com.takaapoo.weatherer.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface AirQualityApiService {
    @GET("""/v1/air-quality?hourly=pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone,us_aqi&forecast_days=7""")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Float,
        @Query("longitude") longitude: Float,
        @Query("past_days") pastDays: Int = 2
    ): Response<WholeAirQuality>
}

