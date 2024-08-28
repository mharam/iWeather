package com.takaapoo.weatherer.domain.use_case

import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.LocationRepository
import com.takaapoo.weatherer.domain.repository.TimeRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class AddLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val timeRepository: TimeRepository
){
    suspend operator fun invoke(location: Location): MyResult<Int>{
        val count = locationRepository.countLocationWithName(location.name)
        if (count == 0) {
            return try {
                val id = locationRepository.addLocation(
                    Location(
                        name = location.name,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                )
                locationRepository.updateLocationCustomId(id = id, customId = id)
                MyResult.Success(id)
            } catch (e: Exception){
                MyResult.Error(exception = e)
            }
        } else {
            return MyResult.Error(message = "${location.name} already exists")
        }
    }

    suspend fun setLocationTime(location: Location): MyResult<String>{
        try {
            val result = timeRepository.getTime(
                latitude = location.latitude,
                longitude = location.longitude
            )
            return if (result is MyResult.Success){
                locationRepository.updateLocationUtcOffset(
                    name = location.name,
                    utcOffset = result.data.utcOffset.toMinutes()
                )
                MyResult.Success("Location time set successfully")
            } else {
                MyResult.Error(message = "Error in retrieving ${location.name} time")
            }
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
    }

    suspend fun countLocationWithName(locationName: String) =
        locationRepository.countLocationWithName(locationName)

    suspend fun getLocation(text: String) = locationRepository.getLocation(text)
}