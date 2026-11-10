package com.takaapoo.weatherer.domain.repository

import com.takaapoo.weatherer.data.remote.RadarSpecs
import com.takaapoo.weatherer.domain.MyResult

interface RadarRepository {
    suspend fun getRadarSpecs(radarName: String): MyResult<out RadarSpecs>
}