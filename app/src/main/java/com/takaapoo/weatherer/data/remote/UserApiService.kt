package com.takaapoo.weatherer.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApiService {
    @POST("Weather/Users.php")
    suspend fun verifyGoogleToken(
        @Body request: UserAuthRequest
    ): Response<UserAuthResponse>

    @GET("Weather/UserTrialCheck.php")
    suspend fun getTrialExpired(
        @Query("google_id") googleId: String,
    ): Response<UserTrialCheckResponse>
}