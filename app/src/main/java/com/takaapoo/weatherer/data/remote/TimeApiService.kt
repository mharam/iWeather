package com.takaapoo.weatherer.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface TimeApiService {
    @GET("Time/current/coordinate")
    suspend fun getTime(
        @Query("latitude") latitude: Float,
        @Query("longitude") longitude: Float
    ): Response<LocationTime>
}