package com.takaapoo.weatherer.domain.repository

import com.takaapoo.weatherer.data.remote.LandScape
import com.takaapoo.weatherer.domain.MyResult

interface LandScapeRepository {
    suspend fun getLandScapeList(
        latitude: Float,
        elevation: Float,
        precipitation: Float,
        landType: Int,
    ): MyResult<out List<LandScape>>
}