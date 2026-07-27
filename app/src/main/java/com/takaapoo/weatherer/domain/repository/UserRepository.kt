package com.takaapoo.weatherer.domain.repository

import com.takaapoo.weatherer.data.remote.UserAuthResponse
import com.takaapoo.weatherer.data.remote.UserTrialCheckResponse
import com.takaapoo.weatherer.domain.MyResult

interface UserRepository {
    suspend fun verifyGoogleToken(idToken: String): MyResult<out UserAuthResponse>
    suspend fun getTrialExpired(googleId: String): MyResult<out UserTrialCheckResponse>
}