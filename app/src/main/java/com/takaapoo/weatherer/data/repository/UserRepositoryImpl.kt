package com.takaapoo.weatherer.data.repository

import com.takaapoo.weatherer.data.remote.UserApiService
import com.takaapoo.weatherer.data.remote.UserAuthRequest
import com.takaapoo.weatherer.data.remote.UserAuthResponse
import com.takaapoo.weatherer.data.remote.UserTrialCheckResponse
import com.takaapoo.weatherer.di_modules.Http
import com.takaapoo.weatherer.di_modules.Https
import com.takaapoo.weatherer.di_modules.callWithFallback
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.UserRepository
import org.json.JSONObject
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    @param:Http private val userApiServiceHttp: UserApiService,
    @param:Https private val userApiServiceHttps: UserApiService
): UserRepository {
    override suspend fun verifyGoogleToken(idToken: String): MyResult<out UserAuthResponse> {
        val response = try {
            callWithFallback(
                httpsCall = {
                    userApiServiceHttps.verifyGoogleToken(UserAuthRequest(idToken))
                },
                httpCall = {
                    userApiServiceHttp.verifyGoogleToken(UserAuthRequest(idToken))
                }
            )
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        val errorBody = response.errorBody()?.string()
        return if (response.isSuccessful && response.body() != null) {
            if (response.body()?.status == "success")
                MyResult.Success(response.body()!!)
            else
                MyResult.Error(message = response.body()!!.message)
        } else if (!errorBody.isNullOrEmpty()) {
            val jsonObject = JSONObject(errorBody)
            MyResult.Error(message = jsonObject.optString("message"))
        } else
            MyResult.Error(message = "Error in verifying ID Token: ${response.body()!!.message}")
    }

    override suspend fun getTrialExpired(googleId: String): MyResult<out UserTrialCheckResponse> {
        val response = try {
            callWithFallback(
                httpsCall = {
                    userApiServiceHttps.getTrialExpired(googleId)
                },
                httpCall = {
                    userApiServiceHttp.getTrialExpired(googleId)
                }
            )
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        val errorBody = response.errorBody()?.string()
        return if (response.isSuccessful && response.body() != null) {
            if (response.body()?.status == "success")
                MyResult.Success(response.body()!!)
            else
                MyResult.Error(message = response.body()!!.message)
        } else if (!errorBody.isNullOrEmpty()) {
            val jsonObject = JSONObject(errorBody)
            MyResult.Error(message = jsonObject.optString("message"))
        } else
            MyResult.Error(message = "Retrieving data from server was not successful: ${response.body()}")
    }
}