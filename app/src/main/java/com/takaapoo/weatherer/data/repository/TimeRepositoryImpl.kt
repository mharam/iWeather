package com.takaapoo.weatherer.data.repository

import com.takaapoo.weatherer.data.remote.FullTime
import com.takaapoo.weatherer.data.remote.TimeApiService
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.TimeRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class TimeRepositoryImpl @Inject constructor(
    private val timeApiService: TimeApiService
) : TimeRepository{
    override suspend fun getTime(latitude: Float, longitude: Float): MyResult<FullTime> {
        val response = try {
            timeApiService.getTime(latitude, longitude)
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        return if (response.isSuccessful && response.body() != null && response.headers()["date"] != null)
            MyResult.Success(
                FullTime(
                    localDateTime = LocalDateTime.parse(response.body()!!.dateTime).truncatedTo(
                        ChronoUnit.SECONDS),
                    utcTime = LocalDateTime.parse(
                        response.headers()["date"]!!.substringAfter(',')
                            .substringBeforeLast(' ').trim(),
                        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")
                    )
                )
            )
        else
            MyResult.Error(message = "Retrieving data from server was not successful")
    }
}