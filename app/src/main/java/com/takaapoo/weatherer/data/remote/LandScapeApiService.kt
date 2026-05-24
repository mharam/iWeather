package com.takaapoo.weatherer.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LandScapeApiService {
    @GET("Weather/WeatherPicture.php")
    suspend fun getLandScapeList(
        @Query("latitude") latitude: Float,
        @Query("elevation") elevation: Float,
        @Query("precipitation") precipitation: Float,
        @Query("land_type") landType: Int,
    ): Response<List<LandScape>>
}