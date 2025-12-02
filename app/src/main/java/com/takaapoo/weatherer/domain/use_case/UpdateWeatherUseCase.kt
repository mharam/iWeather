package com.takaapoo.weatherer.domain.use_case

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.local.LocationType
import com.takaapoo.weatherer.data.remote.AirQuality
import com.takaapoo.weatherer.data.remote.DailyWeather
import com.takaapoo.weatherer.data.remote.DailyWeatherPack
import com.takaapoo.weatherer.data.remote.HourlyWeather
import com.takaapoo.weatherer.data.remote.HourlyWeatherPack
import com.takaapoo.weatherer.data.remote.RemoteLocationType
import com.takaapoo.weatherer.data.remote.WholeAirQuality
import com.takaapoo.weatherer.data.remote.toLocalAirQuality
import com.takaapoo.weatherer.data.remote.toLocalDailyWeather
import com.takaapoo.weatherer.data.remote.toLocalHourlyWeather
import com.takaapoo.weatherer.di_modules.IoDispatcher
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.AirQualityRepository
import com.takaapoo.weatherer.domain.repository.LandScapeRepository
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.LocationRepository
import com.takaapoo.weatherer.domain.repository.RemoteWeatherRepository
import com.takaapoo.weatherer.utility.determineLocationType
import com.takaapoo.weatherer.utility.oldest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class UpdateWeatherUseCase @Inject constructor (
    @param:ApplicationContext val context: Context,
    private val remoteWeatherRepository: RemoteWeatherRepository,
    private val airQualityRepository: AirQualityRepository,
    private val localWeatherRepository: LocalWeatherRepository,
    private val locationRepository: LocationRepository,
    private val landScapeRepository: LandScapeRepository,
    private val addLocationUseCase: AddLocationUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    val defaultPastDays = 90
    val tryCounter: MutableMap<Int, Int> = mutableMapOf()
    suspend operator fun invoke(location: Location): MyResult<*> = withContext(ioDispatcher){
        try {
            if (!tryCounter.containsKey(location.id)) {
                tryCounter[location.id] = 3
            }
            var elevation = 0f
            var landType = 3
            var precipitation = 0f
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
//                    Log.i("size1", "Hourly job finished!")
                }
                launch {
                    val weatherResult = remoteWeatherRepository.getDailyWeatherFromServer(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        pastDays = defaultPastDays
                    )
                    if (weatherResult is MyResult.Success && isActive) {
                        elevation = weatherResult.data.elevation
                        locationRepository.updateLocationElevation(
                            name = location.name,
                            elevation = elevation
                        )
                        localWeatherRepository.addDailyWeather(
                            weatherResult.data.dailyWeather.toLocalDailyWeather(location.id)
                        )
                    } else {
                        throw (weatherResult as MyResult.Error).let { it.exception ?: Exception(it.message) }
                    }
//                    Log.i("size1", "Daily job finished!")
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
//                    Log.i("size1", "Air quality job finished!")
                }
                launch {
                    val locationTypeDeferred = async(context = Dispatchers.Default) {
                        determineLocationType(
                            context,
                            LatLng(location.latitude.toDouble(), location.longitude.toDouble())
                        )
                    }
                    val locationTypeResultDeferred = async {
                        withTimeoutOrNull(timeMillis = 30_000){
                            locationRepository.getLocationTypeFromServer(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        }
                    }
                    val locationTypes = awaitAll(locationTypeDeferred, locationTypeResultDeferred)
                    val internalLocationType = locationTypes[0] as LocationType
                    val externalLocationTypeResult = locationTypes[1] as MyResult<RemoteLocationType>?
//                    Log.i("loc1", "internalLocationType = $internalLocationType")
                    when {
                        internalLocationType == LocationType.BEACH || externalLocationTypeResult == null -> {
                            landType = internalLocationType.ordinal + 1
                            locationRepository.updateLocationType(
                                name = location.name,
                                locationType = internalLocationType
                            )
                        }
                        externalLocationTypeResult is MyResult.Success && isActive -> {
                            val externalLocationType = LocationType.entries.find {
                                it.name == externalLocationTypeResult.data.feature
                            }
//                            Log.i("loc1", "externalLocationType = $externalLocationType")
                            val locationType = when (externalLocationType) {
                                LocationType.LAND -> if (internalLocationType == LocationType.OCEAN)
                                    LocationType.BEACH else externalLocationType
                                LocationType.LAKE -> if (internalLocationType == LocationType.OCEAN)
                                    internalLocationType else externalLocationType
                                LocationType.OCEAN, LocationType.RIVER -> externalLocationType
                                else -> internalLocationType
                            }

                            landType = locationType.ordinal + 1
                            locationRepository.updateLocationType(
                                name = location.name,
                                locationType = locationType
                            )
                        }
                        else -> {
                            landType = internalLocationType.ordinal + 1
                            locationRepository.updateLocationType(
                                name = location.name,
                                locationType = internalLocationType
                            )
                        }
                    }
//                    Log.i("size1", "landType job finished!")
                }
                launch {
                    val yearlyWeatherResult = remoteWeatherRepository.getYearlyWeatherFromServer(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        weatherQuantities = listOf("precipitation_sum")
                    )
                    if (yearlyWeatherResult is MyResult.Success && isActive) {
                        precipitation = yearlyWeatherResult.data[0] ?: 300f
                        locationRepository.updateLocationPrecipitation(
                            name = location.name,
                            precipitation = yearlyWeatherResult.data[0]
                        )
                    } else {
                        throw (yearlyWeatherResult as MyResult.Error).let { it.exception ?: Exception(it.message) }
                    }
//                    Log.i("size1", "precipitation job finished!")
                }
                launch {
                    val timeResult = withTimeoutOrNull(60_000){
                        addLocationUseCase.setLocationTime(location)
                    }
                    if (timeResult == null || timeResult is MyResult.Error) {
//                        Log.i("size1", "exception timeResult: ${(timeResult.exception)?.message}")
                        throw timeResult?.let { it.exception ?: Exception(it.message) } ?: Exception("Timeout")
                    }
//                    Log.i("size1", "time job finished!")
                }
            }
            val landscapeJob = launch {
//                elevation = 1500f
//                precipitation = 800f
//                landType = 3

                val picturesResult = landScapeRepository.getLandScapeList(
                    latitude = location.latitude,
                    elevation = elevation,
                    precipitation = precipitation,
                    landType = landType
                )
                if (picturesResult is MyResult.Success) {
                    val landscapes = picturesResult.data
                    val allPreviousPicIds = locationRepository.getAllLocationsList().map {
                        it.picIdsList
                    }.flatten()
                    val previousIds = locationRepository.getLocation(location.id)?.picIdsList ?: emptyList()
                    val nonUsedLandscapes = landscapes.filterNot {
                        allPreviousPicIds.contains(it.id)
                    }
                    val selectedLandscape = nonUsedLandscapes.randomOrNull() ?: landscapes.random()

                    val updatedLocationPicIds =
                        if (nonUsedLandscapes.isEmpty()) listOf(selectedLandscape.id)
                        else previousIds + listOf(selectedLandscape.id)
                    locationRepository.updateLocationPicInfo(
                        name = location.name,
                        currentPicUrl = selectedLandscape.url,
                        picIds = updatedLocationPicIds,
                    )
                }
            }
            locationRepository.updateLocationLastModifiedTime(
                name = location.name,
                lastModifiedTime = LocalDateTime.now(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.SECONDS).toString()
            )
            landscapeJob.join()
            tryCounter.remove(location.id)
            return@withContext MyResult.Success(Unit)
        } catch (e: Exception){
            Log.i("size1", "tryCounter = ${tryCounter[location.id]}")
            if ((tryCounter[location.id] ?: 0) > 0 &&
                locationRepository.countLocationWithName(location.name) != 0) {
                delay(8_000)
                tryCounter[location.id] = tryCounter[location.id]!! - 1
                return@withContext invoke(location)
            } else {
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


    /*suspend fun updateAllLocationsWeather(){
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
            if (updateDeferred.isNotEmpty()) updateDeferred.awaitAll()
        }
    }*/

    /*suspend fun updateALocationWeather(
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
            throw (results[1] as MyResult.Error).let { it.exception ?: Exception(it.message) }
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
    }*/

    @Throws(Throwable::class)
    suspend fun updateAllLocationsWeatherBatch(){
        locationRepository.deleteFaultyLocations()
        val timeNow = LocalDateTime.now(ZoneId.of("UTC"))
        val allLocations = locationRepository.getAllLocationsList()
        if (allLocations.isEmpty()) return

        val pastDays = if (allLocations.map { it.lastModifiedTime }.contains(null)) defaultPastDays
        else (Duration.between(
            allLocations.map { LocalDateTime.parse(it.lastModifiedTime) }.oldest(),
            timeNow
        ).toDays() + 2).toInt().coerceAtMost(defaultPastDays)

        coroutineScope {
            if (!allLocations.map { it.lastModifiedTime }.contains(null) &&
                !allLocations.map { it.currentPictureUrl }.contains("") &&
                allLocations.maxOfOrNull {
                    Duration.between(LocalDateTime.parse(it.lastModifiedTime), timeNow).toHours()
                }!! < 3)
                return@coroutineScope

            val hourlyResult = async(ioDispatcher) {
                remoteWeatherRepository.getHourlyWeatherBatchFromServer(
                    latitude = allLocations.map { it.latitude },
                    longitude = allLocations.map { it.longitude },
                    pastDays = pastDays
                )
            }
            val dailyResult = async(ioDispatcher) {
                remoteWeatherRepository.getDailyWeatherBatchFromServer(
                    latitude = allLocations.map { it.latitude },
                    longitude = allLocations.map { it.longitude },
                    pastDays = pastDays
                )
            }
            val wholeAirQualityResult = async(ioDispatcher) {
                airQualityRepository.getAirQualityBatchFromServer(
                    latitude = allLocations.map { it.latitude },
                    longitude = allLocations.map { it.longitude },
                    pastDays = pastDays
                )
            }
            val results = awaitAll(hourlyResult, dailyResult, wholeAirQualityResult)
            launch {
                if (results[0] is MyResult.Success) {
                    val data = (results[0] as MyResult.Success<List<HourlyWeatherPack>>).data
                    data.forEachIndexed { i, hourlyWeatherPack ->
                        localWeatherRepository.addHourlyWeather(
                            hourlyWeatherPack.hourlyWeather.toLocalHourlyWeather(
                                locationId = allLocations[i].id,
                                lastLocalHourlyWeather = null
                            )
                        )
                    }
                } else {
                    throw (results[0] as MyResult.Error).let { it.exception ?: Exception(it.message) }
                }
            }
            launch {
                if (results[1] is MyResult.Success) {
                    val data = (results[1] as MyResult.Success<List<DailyWeatherPack>>).data
                    data.forEachIndexed { i, dailyWeatherPack ->
                        localWeatherRepository.addDailyWeather(
                            dailyWeatherPack.dailyWeather.toLocalDailyWeather(
                                locationId = allLocations[i].id,
                            )
                        )
                    }
                } else {
                    throw (results[1] as MyResult.Error).let { it.exception ?: Exception(it.message) }
                }
            }
            launch {
                if (results[2] is MyResult.Success) {
                    val data = (results[2] as MyResult.Success<List<WholeAirQuality>>).data
                    data.forEachIndexed { i, wholeAirQuality ->
                        airQualityRepository.addAirQuality(
                            wholeAirQuality.airQuality.toLocalAirQuality(allLocations[i].id)
                        )
                    }
                } else {
                    throw (results[2] as MyResult.Error).let { it.exception ?: Exception(it.message) }
                }
            }

            val setTimeDeferred = allLocations.map { location ->
                async(ioDispatcher) {
                    val addResult = addLocationUseCase.setLocationTime(location)
                    if (addResult is MyResult.Success) {
                        locationRepository.updateLocationLastModifiedTime(
                            name = location.name,
                            lastModifiedTime = LocalDateTime.now(ZoneId.of("UTC"))
                                .truncatedTo(ChronoUnit.SECONDS).toString()
                        )
                    } else {
                        Log.i("await1", "${addResult as MyResult.Error}")
                    }
                }
            }
            setTimeDeferred.awaitAll()
        }
    }
}