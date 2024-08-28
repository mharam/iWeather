package com.takaapoo.weatherer.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationApiService {
    @GET("autocomplete")
    suspend fun getLocation(
        @Query("text") text: String,
        @Query("limit") limit: Int = 10,
//        @Query("type") type: String = "city",
//        @Query("bias") bias: String = "proximity:51.447,35.775",
        @Query("apiKey") apiKey: String = "dde08922c84143d188d5322b0b3cc809"
    ): Response<LocationServerOutput>
}


