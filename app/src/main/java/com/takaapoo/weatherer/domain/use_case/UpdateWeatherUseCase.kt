package com.takaapoo.weatherer.domain.use_case

import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.remote.AirQuality
import com.takaapoo.weatherer.data.remote.DailyWeather
import com.takaapoo.weatherer.data.remote.DailyWeatherPack
import com.takaapoo.weatherer.data.remote.HourlyWeather
import com.takaapoo.weatherer.data.remote.HourlyWeatherPack
import com.takaapoo.weatherer.data.remote.WholeAirQuality
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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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
    val defaultPastDays = 90
    val tryCounter: MutableMap<Int, Int> = mutableMapOf<Int, Int>()
    suspend operator fun invoke(location: Location): MyResult<Unit> = withContext(ioDispatcher){
        try {
            if (!tryCounter.containsKey(location.id)) {
                tryCounter[location.id] = 4
            }
            coroutineScope {
                launch {
                    val weatherResult = remoteWeatherRepository.getHourlyWeatherFromServer(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        pastDays = defaultPastDays
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
                        longitude = location.longitude,
                        pastDays = defaultPastDays
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
                        longitude = location.longitude,
                        pastDays = defaultPastDays
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
            tryCounter.remove(location.id)
            return@withContext MyResult.Success(Unit)
        } catch (e: Exception){
            if ((tryCounter[location.id] ?: 0) > 0 &&
                locationRepository.countLocationWithName(location.name) != 0) {
                delay(10_000)
                tryCounter[location.id] = tryCounter[location.id]!! - 1
                return@withContext invoke(location)
            } else {
//                Log.i("error1", "exception: ${e.message}")
                tryCounter.remove(location.id)
                return@withContext MyResult.Error(exception = e)
            }
//            addNullData(location.id)
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
        val updateDeferred = mutableListOf<Deferred<Unit>>()
        coroutineScope {
            locationRepository.getAllLocationsList().forEach { location ->
                if (location.lastModifiedTime != null &&
                    abs(Duration.between(LocalDateTime.parse(location.lastModifiedTime), timeNow).toHours()) < 3)
                    return@forEach

                updateDeferred.add(
                    async {
                        try {
                            updateALocationWeather(location, timeNow)
                            locationRepository.updateLocationLastModifiedTime(
                                name = location.name,
                                lastModifiedTime = LocalDateTime.now(ZoneId.of("UTC"))
                                    .truncatedTo(ChronoUnit.SECONDS).toString()
                            )
                        } catch (_: Exception){ }
                    }
                )
            }
//            Log.i("def1", "Start wait: ${updateDeferred.size}")
            if (updateDeferred.isNotEmpty()) updateDeferred.awaitAll()
//            Log.i("def1", "End wait")
        }
    }

    suspend fun updateALocationWeather(
        location: Location,
        timeNow: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
    ) = coroutineScope {
        val pastDays = if (location.lastModifiedTime == null) defaultPastDays
        else (Duration.between(LocalDateTime.parse(location.lastModifiedTime), timeNow).toDays() + 2)
            .toInt().coerceAtMost(defaultPastDays)

        val hourlyResult = async(ioDispatcher) {
            remoteWeatherRepository.getHourlyWeatherFromServer(
                latitude = location.latitude,
                longitude = location.longitude,
                pastDays = pastDays
            )
        }
        val dailyResult = async(ioDispatcher) {
            remoteWeatherRepository.getDailyWeatherFromServer(
                latitude = location.latitude,
                longitude = location.longitude,
                pastDays = pastDays
            )
        }
        val wholeAirQualityResult = async(ioDispatcher) {
            airQualityRepository.getAirQualityFromServer(
                latitude = location.latitude,
                longitude = location.longitude,
                pastDays = pastDays
            )
        }

        val results = awaitAll(hourlyResult, dailyResult, wholeAirQualityResult)
        if (results[0] is MyResult.Success) {
            localWeatherRepository.addHourlyWeather(
                (results[0] as MyResult.Success<HourlyWeatherPack>).data.hourlyWeather
                    .toLocalHourlyWeather(
                        locationId = location.id,
                        lastLocalHourlyWeather = null
                    )
            )
        } else {
            throw (results[0] as MyResult.Error).let { it.exception ?: Exception(it.message) }
        }

        if (results[1] is MyResult.Success) {
            localWeatherRepository.addDailyWeather(
                (results[1] as MyResult.Success<DailyWeatherPack>).data.dailyWeather
                    .toLocalDailyWeather(location.id)
            )
        } else {
            throw (results[1] as MyResult.Error as MyResult.Error).let { it.exception ?: Exception(it.message) }
        }

        if (results[2] is MyResult.Success) {
            airQualityRepository.addAirQuality(
                (results[2] as MyResult.Success<WholeAirQuality>).data.airQuality
                    .toLocalAirQuality(location.id)
            )
        } else {
            throw (results[2] as MyResult.Error).let { it.exception ?: Exception(it.message) }
        }

        launch(ioDispatcher) {
            addLocationUseCase.setLocationTime(location)
        }
    }
}