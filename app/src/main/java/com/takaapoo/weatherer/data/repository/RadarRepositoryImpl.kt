package com.takaapoo.weatherer.data.repository

import com.takaapoo.weatherer.data.remote.RadarApiService
import com.takaapoo.weatherer.data.remote.RadarSpecs
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.RadarRepository
import javax.inject.Inject

class RadarRepositoryImpl @Inject constructor(
    private val radarApiService: RadarApiService
): RadarRepository {
    override suspend fun getRadarSpecs(radarName: String): MyResult<out RadarSpecs> {
        val response = try {
            radarApiService.getRadarSpecs(radarName)
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        return if (response.isSuccessful && response.body() != null) {
            MyResult.Success(response.body()!!)
        } else
            MyResult.Error(message = "Retrieving data from server was not successful")
    }
}