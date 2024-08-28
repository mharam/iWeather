package com.takaapoo.weatherer.domain.repository

import com.takaapoo.weatherer.data.remote.FullTime
import com.takaapoo.weatherer.domain.MyResult


interface TimeRepository {
    suspend fun getTime(
        latitude: Float,
        longitude: Float
    ): MyResult<FullTime>
}