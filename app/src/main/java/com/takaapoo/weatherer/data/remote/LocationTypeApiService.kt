package com.takaapoo.weatherer.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url


interface LocationTypeApiService {
    @GET
    suspend fun getLocationType(
        @Url latLng: String,
    ): Response<RemoteLocationType>
}