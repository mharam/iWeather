package com.takaapoo.weatherer.data.repository

import android.util.Log
import com.takaapoo.weatherer.data.remote.LandScape
import com.takaapoo.weatherer.data.remote.LandScapeApiService
import com.takaapoo.weatherer.di_modules.Http
import com.takaapoo.weatherer.di_modules.Https
import com.takaapoo.weatherer.di_modules.callWithFallback
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.LandScapeRepository
import javax.inject.Inject

class LandScapeRepositoryImpl @Inject constructor(
    @param:Http private val landScapeApiServiceHttp: LandScapeApiService,
    @param:Https private val landScapeApiServiceHttps: LandScapeApiService
): LandScapeRepository {
    override suspend fun getLandScapeList(
        latitude: Float,
        elevation: Float,
        precipitation: Float,
        landType: Int,
    ): MyResult<out List<LandScape>> {
        val response = try {
            callWithFallback(
                httpsCall = {
                    landScapeApiServiceHttps.getLandScapeList(latitude, elevation, precipitation, landType)
                },
                httpCall = {
                    landScapeApiServiceHttp.getLandScapeList(latitude, elevation, precipitation, landType)
                }
            )
        } catch (e: Exception){
            Log.i("update1", "landScape Error: $e")
            return MyResult.Error(exception = e)
        }
        return if (response.isSuccessful && response.body() != null)
            MyResult.Success(response.body()!!)
        else
            MyResult.Error(message = "Retrieving data from server was not successful")
    }

}