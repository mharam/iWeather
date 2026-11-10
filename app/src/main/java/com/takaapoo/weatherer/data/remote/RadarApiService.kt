package com.takaapoo.weatherer.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface RadarApiService {
    @GET("data_spatial/{radarName}/latest.json")
    suspend fun getRadarSpecs(
        @Path("radarName") radarName: String
    ): Response<RadarSpecs>
}