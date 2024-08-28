package com.takaapoo.weatherer.domain.use_case

import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.remote.AirQuality
import com.takaapoo.weatherer.data.remote.DailyWeather
import com.takaapoo.weatherer.data.remote.HourlyWeather
import com.takaapoo.weatherer.data.remote.toLocalAirQuality
import com.takaapoo.weatherer.data.remote.toLocalDailyWeather
import com.takaapoo.weatherer.data.remote.toLocalHourlyWeather
import com.takaapoo.weatherer.di_modules.IoDispatcher
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.AirQualityRepository
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.LocationRepository
import com.takaapoo.weatherer.domain.repository.RemoteWeatherRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.abs

class UpdateWeatherUseCase @Inject constructor (
    private val remoteWeatherRepository: RemoteWeatherRepository,
    private val airQualityRepository: AirQualityRepository,
    private val localWeatherRepository: LocalWeatherRepository,
    private val locationRepository: LocationRepository,
    private val addLocationUseCase: AddLocationUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(location: Location) = withContext(ioDispatcher){
        try {
            coroutineScope {
                launch {
                    val weatherResult = remoteWeatherRepository.getHourlyWeatherFromServer(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        currentWeather = true
                    )
                    if (weatherResult is MyResult.Success && isActive) {
                        localWeatherRepository.addHourlyWeather(
                            weatherResult.data.hourlyWeather.toLocalHourlyWeather(locationId = location.id)
                        )
                    } else {
                        throw (weatherResult as MyResult.Error).let { it.exception ?: Exception(it.message) }
                    }
                }
                launch {
                    val weatherResult = remoteWeatherRepository.getDailyWeatherFromServer(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    if (weatherResult is MyResult.Success && isActive) {
                        localWeatherRepository.addDailyWeather(
                            weatherResult.data.dailyWeather.toLocalDailyWeather(location.id)
                        )
                    } else {
                        throw (weatherResult as MyResult.Error).let { it.exception ?: Exception(it.message) }
                    }
                }
                launch {
                    val wholeAirQualityResult = airQualityRepository.getAirQualityFromServer(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    if (wholeAirQualityResult is MyResult.Success && isActive) {
                        airQualityRepository.addAirQuality(
                            wholeAirQualityResult.data.airQuality.toLocalAirQuality(location.id)
                        )
                    } else {
                        throw (wholeAirQualityResult as MyResult.Error).let { it.exception ?: Exception(it.message) }
                    }
                }
                launch {
                    val timeResult = addLocationUseCase.setLocationTime(location)
                    if (timeResult is MyResult.Error)
                        throw timeResult.let { it.exception ?: Exception(it.message) }
                }
            }
            locationRepository.updateLocationLastModifiedTime(
                name = location.name,
                lastModifiedTime = LocalDateTime.now(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.SECONDS).toString()
            )
        } catch (_: Exception){
            addNullData(location.id)
        }
    }

    private suspend fun addNullData(locationId: Int){
        localWeatherRepository.addHourlyWeather(
            HourlyWeather().toLocalHourlyWeather(locationId)
        )
        localWeatherRepository.addDailyWeather(
            DailyWeather().toLocalDailyWeather(locationId)
        )
        airQualityRepository.addAirQuality(
            AirQuality().toLocalAirQuality(locationId)
        )
    }


    suspend fun updateAllLocationsWeather(){
        val timeNow = LocalDateTime.now(ZoneId.of("UTC"))
        coroutineScope {
            locationRepository.getAllLocationsList().forEach { location ->
                if (location.lastModifiedTime != null &&
                    abs(Duration.between(LocalDateTime.parse(location.lastModifiedTime), timeNow).toHours()) < 6)
                    return@forEach

                launch {
                    try {
                        updateALocationWeather(location)
                        locationRepository.updateLocationLastModifiedTime(
                            name = location.name,
                            lastModifiedTime = LocalDateTime.now(ZoneId.of("UTC"))
                                .truncatedTo(ChronoUnit.SECONDS).toString()
                        )
                    } catch (_: Exception){ }
                }
            }
        }
    }

    private suspend fun updateALocationWeather(location: Location) = coroutineScope {
        launch(ioDispatcher) {
            val weatherResult = remoteWeatherRepository.getHourlyWeatherFromServer(
                latitude = location.latitude,
                longitude = location.longitude,
                currentWeather = true
            )
            if (weatherResult is MyResult.Success) {
                val lastDateTime = weatherResult.data.hourlyWeather.time.getOrNull(0)?.let {
                    LocalDateTime.parse(it)
                }?.minusHours(1)
                val lastLocalHourlyWeather = lastDateTime?.let {
                    localWeatherRepository.getLocationHourlyWeatherForTime(
                        locationId = location.id,
                        startTime = it.toString(),
                        endTime = it.toString()
                    )
                }?.getOrNull(0)

                localWeatherRepository.addHourlyWeather(
                    weatherResult.data.hourlyWeather.toLocalHourlyWeather(
                        locationId = location.id,
                        lastLocalHourlyWeather = lastLocalHourlyWeather
                    )
                )
            } else {
                 throw (weatherResult as MyResult.Error).let { it.exception ?: Exception(it.message) }
            }
        }
        launch(ioDispatcher) {
            val weatherResult = remoteWeatherRepository.getDailyWeatherFromServer(
                latitude = location.latitude,
                longitude = location.longitude
            )
            if (weatherResult is MyResult.Success) {
                localWeatherRepository.addDailyWeather(
                    weatherResult.data.dailyWeather.toLocalDailyWeather(location.id)
                )
            } else {
                throw (weatherResult as MyResult.Error).let { it.exception ?: Exception(it.message) }
            }
        }
        launch(ioDispatcher) {
            val wholeAirQualityResult = airQualityRepository.getAirQualityFromServer(
                latitude = location.latitude,
                longitude = location.longitude
            )
            if (wholeAirQualityResult is MyResult.Success) {
                airQualityRepository.addAirQuality(
                    wholeAirQualityResult.data.airQuality.toLocalAirQuality(location.id)
                )
            } else {
                throw (wholeAirQualityResult as MyResult.Error).let { it.exception ?: Exception(it.message) }
            }
        }
        launch(ioDispatcher) {
            addLocationUseCase.setLocationTime(location)
        }
    }
}