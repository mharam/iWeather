package com.takaapoo.weatherer.ui.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.WeatherType.Companion.calculateMoonType
import com.takaapoo.weatherer.domain.model.HomePaneContent
import com.takaapoo.weatherer.domain.model.HomePaneState
import com.takaapoo.weatherer.domain.model.HomeState
import com.takaapoo.weatherer.domain.model.LocationsState
import com.takaapoo.weatherer.domain.repository.DataRefreshRepository
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.LocationRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.use_case.UpdateWeatherUseCase
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.weatherCurveXtoY
import com.takaapoo.weatherer.utility.celsiusToUnit
import com.takaapoo.weatherer.utility.kmToUnit
import com.takaapoo.weatherer.utility.kmphToUnit
import com.takaapoo.weatherer.utility.landScapeName
import com.takaapoo.weatherer.utility.mToUnit
import com.takaapoo.weatherer.utility.mmToUnit
import com.takaapoo.weatherer.utility.paToUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlin.math.floor

enum class DialogType{
    DELETE, EDIT, /*DONATE, LOCATION_COUNT_LIMIT*/
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    localWeatherRepository: LocalWeatherRepository,
    private val locationRepository: LocationRepository,
    private val preferenceRepository: PreferenceRepository,
    dataRefreshRepository: DataRefreshRepository,
    private val updateWeatherUseCase: UpdateWeatherUseCase,
    @ApplicationContext val applicationContext: Context,
): ViewModel() {

    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState
        .onStart {
            initializeDayListState(
                firstVisibleItemScrollOffset = -(applicationContext.resources.getDimension(R.dimen.day_display_item_height)/2f).toInt()
            )
        }
        .stateIn(
            scope = viewModelScope,
            started =  SharingStarted.WhileSubscribed(5000),
            initialValue = HomeState()
        )

    private val _homePaneState = MutableStateFlow(HomePaneState())
    val homePaneState = _homePaneState.asStateFlow()

    val allLocations = locationRepository.getAllLocations().stateIn(
        scope = viewModelScope,
        started =  SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        dataRefreshRepository.refreshData()
        viewModelScope.launch{
            val appSettings = preferenceRepository.getSettings()
            updateClockGaugeLock(appSettings.clockGaugeLock)
        }
    }

    private val utcDateTimeFlow = flow {
        while (true) {
            emit(LocalDateTime.now(ZoneId.of("UTC")))
            val localNow = LocalDateTime.now()
            _homeState.update {
                it.copy(
                    clockGaugeNaturalRotation = - localNow.toLocalTime().toSecondOfDay() / 240f,
                    dayGaugeNaturalIndex = Int.MAX_VALUE / 2 + localNow.dayOfWeek.value - 1
                )
            }
            delay(10_000)
        }
    }
    private lateinit var localNow: LocalDateTime
//    private var weight1 = 0f
//    private var weight2 = 0f
    private val dayGaugeMinutesFlow = _homeState.map { it.dayGaugeIndex*1440 }/*.distinctUntilChanged()*/
    private val clockGaugeMinutesFlow = snapshotFlow { (-_homeState.value.clockGaugeRotation.value*4).toLong() }
//    private val allLocationsWeatherGrouped = localWeatherRepository.getAllLocationsWeather()
//        .transform {weatherDTOList ->
//            emit(weatherDTOList.groupBy { it.locationId })
//        }

    /*private val allLocationsWeatherGrouped = combine(
        locationRepository.getAllLocations(),
        utcDateTimeFlow,
        clockGaugeMinutesFlow,
        dayGaugeMinutesFlow,
    ) { loc, utcTime, clockMinuteIncremented, dayMinuteIncremented*//*, appSettings*//* ->
        val modifiedUtcDate = utcTime
            .plusMinutes(clockMinuteIncremented + dayMinuteIncremented)
            .toLocalDate()
        val startDate = modifiedUtcDate.minusDays(4).toString()
        val endDate = modifiedUtcDate.plusDays(4).toString()
        val hourlyWeatherDeferred = viewModelScope.async(Dispatchers.IO) {
            localWeatherRepository.getHourlyWeather(startDate, endDate)
        }
        val hourlyAirQualityDeferred = viewModelScope.async(Dispatchers.IO) {
            localWeatherRepository.getHourlyAirQuality(startDate, endDate)
        }
        val dailyWeatherDeferred = viewModelScope.async(Dispatchers.IO) {
            localWeatherRepository.getDailyWeather(startDate, endDate)
        }
        val weatherData = awaitAll(hourlyWeatherDeferred, hourlyAirQualityDeferred, dailyWeatherDeferred)
        val hourlyWeather = weatherData[0] as Map<Location, List<LocalHourlyWeather>>
        val hourlyAirQuality = weatherData[1] as Map<Location, List<LocalAirQuality>>
        val dailyWeather = weatherData[2] as Map<Location, List<LocalDailyWeather>>

        val out = loc.map { location ->
            hourlyWeather[location]?.map { currentHourlyWeather ->
                val todayWeather = dailyWeather[location]?.find {
                    it.time == currentHourlyWeather.time.substring(0..9)
                }
                WeatherDto(
                    locationId = location.id,
                    locationName = location.name,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    utcOffset = location.utcOffset,
                    currentWeather = currentHourlyWeather,
                    currentAirQuality = hourlyAirQuality[location]?.find { it.time == currentHourlyWeather.time },
                    todayMaxTemperature = todayWeather?.temperatureMax,
                    todayMinTemperature = todayWeather?.temperatureMin,
                    sunRise = todayWeather?.sunRise,
                    sunSet = todayWeather?.sunSet
                )
            } ?:
            listOf(WeatherDto(
                locationId = location.id,
                locationName = location.name,
                latitude = location.latitude,
                longitude = location.longitude,
                utcOffset = location.utcOffset
            ))
        }.flatten()
        out.groupBy { it.locationId }
    }*/

    /*val locationsState: StateFlow<List<LocationsState>> =
        combine(
            allLocationsWeatherGrouped,
            utcDateTimeFlow,
            clockGaugeMinutesFlow,
            dayGaugeMinutesFlow,
            preferenceRepository.getSettingsFlow()
        ){ weatherDTOMap , utcTime, clockMinuteIncremented, dayMinuteIncremented, appSettings ->
            if (weatherDTOMap.isEmpty()) return@combine emptyList()

            val locationCounts = weatherDTOMap.count()
            val modifiedUtcTime = utcTime.plusMinutes(
                clockMinuteIncremented + dayMinuteIncremented
            )

            return@combine withContext(Dispatchers.Default){
                List(size = locationCounts) { index ->
                    val allTimesValue = weatherDTOMap.values.elementAt(index).map { it.currentWeather?.time }
                    val timeIndex = allTimesValue.indexOfFirst {
                        try { LocalDateTime.parse(it) > modifiedUtcTime }
                        catch (_: Exception) { false }
                    }
                    val id = weatherDTOMap.keys.elementAt(index)
                    if (timeIndex < 2) {
                        val dto = weatherDTOMap[id]?.firstOrNull()
                        localNow = modifiedUtcTime.plusMinutes(dto?.utcOffset ?: 0)
                        LocationsState(
                            locationId = id,
                            locationName = dto!!.locationName,
                            latitude = dto.latitude,
                            longitude = dto.longitude,
                            clockBigHandleRotation = if (dto.utcOffset == null) null
                            else (localNow.minute + localNow.second / 60f) * 6f,
                            clockSmallHandleRotation = if (dto.utcOffset == null) null
                            else (localNow.hour % 12 + localNow.minute / 60f) * 30f,
                            year = localNow.year,
                            month = localNow.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                .uppercase(),
                            day = localNow.dayOfMonth,
                            dayOfWeek = localNow.dayOfWeek.getDisplayName(
                                TextStyle.SHORT,
                                Locale.US
                            )
                        )
                    } else {
                        val id = weatherDTOMap.keys.elementAt(index)
                        val dto = weatherDTOMap[id]?.firstOrNull()
                        localNow = modifiedUtcTime.plusMinutes((dto?.utcOffset ?: 0))
                        val dayTimeIndex = allTimesValue.indexOfFirst {
                            try { LocalDateTime.parse(it).toLocalDate() == localNow.toLocalDate() }
                            catch (_: DateTimeParseException) { false }
                        }
                        val firstPointX = 0f
                        val lastPointX = allTimesValue.size.toFloat() - 1
                        val targetX = Duration.between(LocalDateTime.parse(allTimesValue[0]), modifiedUtcTime)
                            .toSeconds() / 3600f
                        val hourlyWeather = weatherDTOMap[id]?.map { it.currentWeather }
                        val hourlyAirQuality = weatherDTOMap[id]?.map { it.currentAirQuality }

                        LocationsState(
                            locationId = id,
                            locationName = dto!!.locationName,
                            latitude = dto.latitude,
                            longitude = dto.longitude,
                            currentTemperature = weatherCurveXtoY(
                                quantity = HourlyWeatherQuantity.TEMPERATURE,
                                appSettings = appSettings,
                                smoothnessFactor = null,
                                hourlyWeather = hourlyWeather,
                                hourlyAirQuality = hourlyAirQuality,
                                firstPointX = firstPointX,
                                lastPointX = lastPointX,
                                targetX = targetX
                            )?.celsiusToUnit(appSettings.temperatureUnit),
                            currentHumidity =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.HUMIDITY))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.HUMIDITY,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentDewPoint =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.DEWPOINT))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.DEWPOINT,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.celsiusToUnit(appSettings.temperatureUnit)
                                else
                                    null,
                            currentApparentTemperature =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.APPARENTTEMP))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.APPARENTTEMP,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.celsiusToUnit(appSettings.temperatureUnit)
                                else
                                    null,
                            currentPrecipitationProbability =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.PRECIPITATIONPROBABILITY))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.PRECIPITATIONPROBABILITY,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentPrecipitationAmount =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.PRECIPITATION))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.PRECIPITATION,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.mmToUnit(appSettings.lengthUnit)
                                else
                                    null,
                            currentCloudCover =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.CLOUDCOVER))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.CLOUDCOVER,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentSurfacePressure =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.SURFACEPRESSURE))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.SURFACEPRESSURE,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.paToUnit(appSettings.pressureUnit)
                                else
                                    null,
                            currentVisibility =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.VISIBILITY))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.VISIBILITY,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.kmToUnit(appSettings.lengthUnit)
                                else
                                    null,
                            currentWeatherCode = weatherDTOMap[id]!![timeIndex - 1].currentWeather?.weatherCode,
                            currentWindSpeed =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.WINDSPEED))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.WINDSPEED,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.kmphToUnit(appSettings.speedUnit)
                                else
                                    null,
                            currentWindDirection =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.WINDSPEED))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.WINDDIRECTION,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentUVIndex =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.UVINDEX))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.UVINDEX,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentFreezingLevelHeight =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.FREEZINGLEVELHEIGHT))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.FREEZINGLEVELHEIGHT,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.mToUnit(appSettings.lengthUnit)
                                else
                                    null,
                            currentDirectRadiation =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.DIRECTRADIATION))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.DIRECTRADIATION,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentDirectNormalIrradiance =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.DIRECTNORMALIRRADIANCE))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.DIRECTNORMALIRRADIANCE,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentAirQuality =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.AQI))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.AQI,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = hourlyWeather,
                                        hourlyAirQuality = hourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.toInt()
                                else
                                    null,
                            todayMaxTemperature = weatherDTOMap[id]!!.getOrNull(dayTimeIndex)
                                ?.todayMaxTemperature?.celsiusToUnit(appSettings.temperatureUnit),
                            todayMinTemperature = weatherDTOMap[id]!!.getOrNull(dayTimeIndex)
                                ?.todayMinTemperature?.celsiusToUnit(appSettings.temperatureUnit),
                            clockBigHandleRotation = if (dto.utcOffset == null) null
                            else (localNow.minute + localNow.second / 60f) * 6f,
                            clockSmallHandleRotation = if (dto.utcOffset == null) null
                            else (localNow.hour % 12 + localNow.minute / 60f) * 30f,
                            isDay = if (weatherDTOMap[id]!![dayTimeIndex].sunRise.isNullOrEmpty() ||
                                weatherDTOMap[id]!![dayTimeIndex].sunSet.isNullOrEmpty()) true
                            else
                                localNow.isAfter(LocalDateTime.parse(weatherDTOMap[id]!![dayTimeIndex].sunRise)) &&
                                        localNow.isBefore(LocalDateTime.parse(weatherDTOMap[id]!![dayTimeIndex].sunSet)),
                            moonType = calculateMoonType(modifiedUtcTime.toLocalDate()),
                            am = if (dto.utcOffset == null) null else localNow.hour < 12,
                            year = localNow.year,
                            month = localNow.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                            day = localNow.dayOfMonth,
                            dayOfWeek = localNow.dayOfWeek.getDisplayName(
                                TextStyle.SHORT,
                                Locale.US
                            )
                        )
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope.plus(Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(LocationsState(locationId = -2))
        )*/

    val locationsState: StateFlow<List<LocationsState>> =
        combine(
            locationRepository.getAllLocations(),
            utcDateTimeFlow,
            clockGaugeMinutesFlow,
            dayGaugeMinutesFlow,
            preferenceRepository.getSettingsFlow()
        ){ locations, utcTime, clockMinuteIncremented, dayMinuteIncremented, appSettings ->
            if (locations.isEmpty()) return@combine emptyList()

            val modifiedUtcTime = utcTime.plusMinutes(
                clockMinuteIncremented + dayMinuteIncremented
            )
            val modifiedUtcDate = modifiedUtcTime.toLocalDate()
            val startDate = modifiedUtcDate.minusDays(4).toString()
            val endDate = modifiedUtcDate.plusDays(4).toString()
            val hourlyWeatherDeferred = viewModelScope.async(Dispatchers.IO) {
                localWeatherRepository.getHourlyWeather(startDate, endDate)
            }
            val hourlyAirQualityDeferred = viewModelScope.async(Dispatchers.IO) {
                localWeatherRepository.getHourlyAirQuality(startDate, endDate)
            }
            val dailyWeatherDeferred = viewModelScope.async(Dispatchers.IO) {
                localWeatherRepository.getDailyWeather(startDate, endDate)
            }
            val weatherData = awaitAll(hourlyWeatherDeferred, hourlyAirQualityDeferred, dailyWeatherDeferred)
            val hourlyWeather = weatherData[0] as Map<Int, List<LocalHourlyWeather>>
            val hourlyAirQuality = weatherData[1] as Map<Int, List<LocalAirQuality>>
            val dailyWeather = weatherData[2] as Map<Int, List<LocalDailyWeather>>

            return@combine withContext(Dispatchers.Default){
                locations.mapIndexed { index, location ->
                    val allTimesValue = hourlyWeather[location.id]?.map { it.time }
                    val timeIndex = allTimesValue?.indexOfFirst {
                        try { LocalDateTime.parse(it) > modifiedUtcTime }
                        catch (_: Exception) { false }
                    } ?: 0

                    localNow = modifiedUtcTime.plusMinutes(location.utcOffset ?: 0)
                    if (timeIndex < 2) {
                        LocationsState(
                            locationId = location.id,
                            locationName = location.name,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            currentPictureUrl = location.currentPictureUrl,
                            lastPicId = location.picIdsList.lastOrNull(),
                            clockBigHandleRotation = if (location.utcOffset == null) null
                            else (localNow.minute + localNow.second / 60f) * 6f,
                            clockSmallHandleRotation = if (location.utcOffset == null) null
                            else (localNow.hour % 12 + localNow.minute / 60f) * 30f,
                            year = localNow.year,
                            month = localNow.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                .uppercase(),
                            day = localNow.dayOfMonth,
                            dayOfWeek = localNow.dayOfWeek.getDisplayName(
                                TextStyle.SHORT,
                                Locale.US
                            )
                        )
                    } else {
                        val firstPointX = 0f
                        val lastPointX = allTimesValue!!.size.toFloat() - 1
                        val targetX = Duration.between(LocalDateTime.parse(allTimesValue[0]), modifiedUtcTime)
                            .toSeconds() / 3600f
                        val locationHourlyWeather = hourlyWeather[location.id]
                        val locationHourlyAirQuality = hourlyAirQuality[location.id]
                        val currentWeather = locationHourlyWeather?.get(timeIndex - 1)
                        val todayWeather = dailyWeather[location.id]?.find {
                            it.time == localNow.toLocalDate().toString()
                        }

                        LocationsState(
                            locationId = location.id,
                            locationName = location.name,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            currentPictureUrl = location.currentPictureUrl,
                            lastPicId = location.picIdsList.lastOrNull(),
                            picName = landScapeName(
                                todayWeather = todayWeather,
                                currentWeather = currentWeather,
                                utcOffset = location.utcOffset,
                                utcTime = modifiedUtcTime
                            ),
                            currentTemperature = weatherCurveXtoY(
                                quantity = HourlyWeatherQuantity.TEMPERATURE,
                                appSettings = appSettings,
                                smoothnessFactor = null,
                                hourlyWeather = locationHourlyWeather,
                                hourlyAirQuality = locationHourlyAirQuality,
                                firstPointX = firstPointX,
                                lastPointX = lastPointX,
                                targetX = targetX
                            ),
                            currentHumidity =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.HUMIDITY))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.HUMIDITY,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentDewPoint =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.DEWPOINT))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.DEWPOINT,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.celsiusToUnit(appSettings.temperatureUnit)
                                else
                                    null,
                            currentApparentTemperature =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.APPARENTTEMP))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.APPARENTTEMP,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.celsiusToUnit(appSettings.temperatureUnit)
                                else
                                    null,
                            currentPrecipitationProbability =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.PRECIPITATIONPROBABILITY))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.PRECIPITATIONPROBABILITY,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentPrecipitationAmount =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.PRECIPITATION))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.PRECIPITATION,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.mmToUnit(appSettings.lengthUnit)
                                else
                                    null,
                            currentCloudCover =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.CLOUDCOVER))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.CLOUDCOVER,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentSurfacePressure =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.SURFACEPRESSURE))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.SURFACEPRESSURE,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.paToUnit(appSettings.pressureUnit)
                                else
                                    null,
                            currentVisibility =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.VISIBILITY))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.VISIBILITY,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.kmToUnit(appSettings.lengthUnit)
                                else
                                    null,
                            currentWeatherCode = currentWeather?.weatherCode,
                            currentWindSpeed =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.WINDSPEED))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.WINDSPEED,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.kmphToUnit(appSettings.speedUnit)
                                else
                                    null,
                            currentWindDirection =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.WINDSPEED))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.WINDDIRECTION,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentUVIndex =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.UVINDEX))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.UVINDEX,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentFreezingLevelHeight =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.FREEZINGLEVELHEIGHT))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.FREEZINGLEVELHEIGHT,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.mToUnit(appSettings.lengthUnit)
                                else
                                    null,
                            currentDirectRadiation =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.DIRECTRADIATION))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.DIRECTRADIATION,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentDirectNormalIrradiance =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.DIRECTNORMALIRRADIANCE))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.DIRECTNORMALIRRADIANCE,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )
                                else
                                    null,
                            currentAirQuality =
                                if (appSettings.cardWeatherQuantities.contains(HourlyWeatherQuantity.AQI))
                                    weatherCurveXtoY(
                                        quantity = HourlyWeatherQuantity.AQI,
                                        appSettings = appSettings,
                                        smoothnessFactor = null,
                                        hourlyWeather = locationHourlyWeather,
                                        hourlyAirQuality = locationHourlyAirQuality,
                                        firstPointX = firstPointX,
                                        lastPointX = lastPointX,
                                        targetX = targetX
                                    )?.toInt()
                                else
                                    null,
                            todayMaxTemperature = todayWeather?.temperatureMax
                                ?.celsiusToUnit(appSettings.temperatureUnit),
                            todayMinTemperature = todayWeather?.temperatureMin
                                ?.celsiusToUnit(appSettings.temperatureUnit),
                            clockBigHandleRotation = if (location.utcOffset == null) null
                            else (localNow.minute + localNow.second / 60f) * 6f,
                            clockSmallHandleRotation = if (location.utcOffset == null) null
                            else (localNow.hour % 12 + localNow.minute / 60f) * 30f,
                            isDay = if (todayWeather?.sunRise.isNullOrEmpty() ||
                                todayWeather.sunSet.isNullOrEmpty()) true
                            else
                                localNow.isAfter(LocalDateTime.parse(todayWeather.sunRise)) &&
                                        localNow.isBefore(LocalDateTime.parse(todayWeather.sunSet)),
                            moonType = calculateMoonType(modifiedUtcTime.toLocalDate()),
                            am = if (location.utcOffset == null) null else localNow.hour < 12,
                            year = localNow.year,
                            month = localNow.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                            day = localNow.dayOfMonth,
                            dayOfWeek = localNow.dayOfWeek.getDisplayName(
                                TextStyle.SHORT,
                                Locale.US
                            )
                        )
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope.plus(Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(LocationsState(locationId = -2))
        )

    /*val locationsPicNameState = combine(
        locationRepository.getAllLocations(),
        utcDateTimeFlow,
    ) { loc, utcTime ->
        val roundedUtcTime = utcTime.truncatedTo(ChronoUnit.HOURS)*//*.plusHours(
            if (utcTime.minute > 30) 1 else 0
        )*//*
        val utcDate = utcTime.toLocalDate()
        val today = utcDate.toString()
        val tomorrow = utcDate.plusDays(1).toString()
        val yesterday = utcDate.minusDays(1).toString()

        val hourlyWeather = localWeatherRepository.getHourlyWeather(today, today)
        val dailyWeather = localWeatherRepository.getDailyWeather(yesterday, tomorrow)
        loc.map { location ->
            hourlyWeather[location]?.find { it.time == roundedUtcTime.toString() }?.let { currentWeather ->
                val sunRise = dailyWeather[location]?.find {
                    it.time == utcTime.plusMinutes(location.utcOffset ?: 0).toLocalDate().toString()
                }?.sunRise?.let { LocalDateTime.parse(it) }?.minusMinutes(location.utcOffset ?: 0)
                val sunSet = dailyWeather[location]?.find {
                    it.time == utcTime.plusMinutes(location.utcOffset ?: 0).toLocalDate().toString()
                }?.sunSet?.let { LocalDateTime.parse(it) }?.minusMinutes(location.utcOffset ?: 0)

                val isSunRise = sunRise?.let {
                    abs(Duration.between(utcTime, it).toMinutes()) <= 30
                } ?: false
                val isSunSet = sunSet?.let {
                    abs(Duration.between(utcTime, sunSet).toMinutes()) <= 30
                } ?: false
                val isDay = if (sunRise != null && sunSet != null)
                    utcTime.isAfter(sunRise) && utcTime.isBefore(sunSet)
                else (currentWeather.isDay == 1)

                val timeIndex = if (sunRise?.isEqual(sunSet) == true) {
                    if (currentWeather.isDay == 1) 1 else 3
                } else {
                    when {
                        isSunRise -> 4
                        isSunSet -> 2
                        isDay -> 1
                        else -> 3
                    }
                }
                val skyIndex = WeatherType.fromWMO(code = currentWeather.weatherCode, true, null).picIndex
                val groundIndex = if ((currentWeather.snowDepth ?: 0f) > 0.1f) 3 else 1
                "${skyIndex}_${groundIndex}_${timeIndex}"
            } ?: "1_1_1"
        }.toList()
    }.stateIn(
        scope = viewModelScope.plus(Dispatchers.Default),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("1_1_1")
    )*/

    private fun initializeDayListState(firstVisibleItemScrollOffset: Int){
        _homeState.update {
            it.copy(
                dayListState = LazyListState(
                    firstVisibleItemIndex = _homeState.value.dayGaugeNaturalIndex,
                    firstVisibleItemScrollOffset = firstVisibleItemScrollOffset
                )
            )
        }
    }

    fun balanceClockGaugeValues(){
        val daysOfHours = floor(
            -(_homeState.value.clockGaugeRotation.value + _homeState.value.clockGaugeNaturalRotation) / 360f
        ).toInt()
        _homeState.update {
            it.copy(
                dayGaugeIndex = _homeState.value.dayGaugeIndex + daysOfHours
            )
        }
        updateClockGaugeRotation(_homeState.value.clockGaugeRotation.value + daysOfHours * 360)
    }



    fun deleteLocation(locationId: Int) = viewModelScope.launch {
        locationRepository.deleteLocation(locationId)
    }

    fun updateLocation(id: Int, newLocationName: String) =
        viewModelScope.launch {
            if (locationRepository.countLocationWithName(newLocationName) == 0){
                locationRepository.updateLocationName(locationId = id, newLocationName = newLocationName)
                _homeState.update {
                    it.copy(
                        editNameResult = MyResult.Success(data = "Name Edited Successfully")
                    )
                }
            }else{
                _homeState.update {
                    it.copy(
                        editNameResult = MyResult.Error(message = "Name Already Exits!")
                    )
                }
            }
        }
    fun resetEditNameResult(){
        _homeState.update {
            it.copy(editNameResult = MyResult.Loading())
        }
    }

    fun refreshWeatherData(){
        viewModelScope.launch {
            _homeState.update { it.copy(isRefreshing = true) }
//            val timeMillis1 = measureTimeMillis {
//                updateWeatherUseCase.updateAllLocationsWeather()
//            }
//            Log.i("update1", "timeMillis1 = ${timeMillis1 / 1000f}")
            runCatching {
                updateWeatherUseCase.updateAllLocationsWeatherBatch()
            }.onFailure {
//                Log.i("await1", "${it.message}")
            }.onSuccess {
//                Log.i("await1", "Successful!")
            }
            _homeState.update { it.copy(isRefreshing = false) }
        }
    }

//    fun updateALocationWeatherData(location: Location){
//        viewModelScope.launch {
//            updateWeatherUseCase.updateALocationWeather(location)
//        }
//    }

    fun updateDialogVisibility (dialogType: DialogType, visible: Boolean){
        _homeState.update {
            when (dialogType){
                DialogType.DELETE -> it.copy(deleteDialogVisible = visible)
                DialogType.EDIT -> it.copy(editDialogVisible = visible)
//                DialogType.DONATE -> it.copy(donateDialogVisible = visible)
//                DialogType.LOCATION_COUNT_LIMIT -> it.copy(locationCountDialogVisible = visible)
            }
        }
    }
    fun updateDialogData(dialogType: DialogType, id: Int, locationName: String){
        _homeState.update {
            when (dialogType){
                DialogType.DELETE -> it.copy(
                    deleteDialogLocationId = id,
                    deleteDialogLocationName = locationName
                )
                DialogType.EDIT -> it.copy(
                    editDialogLocationId = id,
                    editDialogOldLocationName = locationName
                )
                else -> it
            }
        }
    }

    fun updateEditDialogLocationName(locationName: String){
        _homeState.update {
            it.copy(
                editDialogNewLocationName = locationName,
            )
        }
        viewModelScope.launch {
            _homeState.update {
                it.copy(
                    nameAlreadyExists = locationRepository.countLocationWithName(locationName) > 0
                )
            }
        }
    }

    fun updateToBeDeletedLocationId(){
        _homeState.update {
            it.copy(
                toBeDeletedLocationId = homeState.value.deleteDialogLocationId
            )
        }
    }
    fun updateClockGaugeRotation(rotation: Float){
        viewModelScope.launch {
            _homeState.value.clockGaugeRotation.snapTo(rotation)
        }
    }
    fun updateDayGaugeIndex(newVisibleDay: Int){
        _homeState.update {
            val daysOfHours = floor(-(it.clockGaugeRotation.value + it.clockGaugeNaturalRotation) / 360f).toInt()
            it.copy(
                dayGaugeIndex = newVisibleDay - (it.dayGaugeNaturalIndex + daysOfHours)
            )
        }
    }
    fun updateVisibleDayIndex(newIndex: Int){
        _homeState.update {
            it.copy(
                visibleDayIndex = newIndex
            )
        }
    }
    fun updateSelectedItemIndexOffset(index: Int? = null, offset: Int? = null){
        _homeState.update {
            it.copy(
                selectedItemIndex = index ?: homeState.value.selectedItemIndex,
                selectedItemOffset = offset ?: homeState.value.selectedItemOffset
            )
        }
    }
    fun updateNavigatedToDetailScreen(navigated: Boolean){
        _homeState.update {
            it.copy(navigatedToDetailScreen = navigated)
        }
    }

    /*
    fun modifyCardPosition(*//*centerXChange: Float?, *//*cardNewY:Float?){
        cardNewY?.let {
            _homeState.update {
                it.copy(selectedCardY = cardNewY)
            }
        }
    }*/

    fun resetDayGauge(){
        _homeState.update {
            it.copy(dayGaugeIndex = 0)
        }
    }

    fun swapLocationCustomId(id1: Int, id2: Int){
        viewModelScope.launch {
            locationRepository.swapLocationCustomId(id1, id2)
        }
    }



//    private val clickSound = List(10){
//        MediaPlayer.create(applicationContext, R.raw.click1).apply {
//            setVolume(0.5f, 0.5f)
//        }
//    }
//    fun handleClickSound(currentRotation: Float, previousRotation: Float){
//        viewModelScope.launch(Dispatchers.Default){
//            val diff = abs((currentRotation / 3.75f).toInt() - (previousRotation / 3.75f).toInt())
//            if (currentRotation.sign != previousRotation.sign){
//                playClick(1)
//            } else {
//                playClick(diff)
//            }
//        }
//    }
//    private suspend fun playClick(count: Int){
//        for (i in 0 until count.coerceAtMost(10)){
//            clickSound[i].start()
//            delay(20)
//        }
//    }

    fun resetZoom(){
        _homeState.update {
            it.copy(zoom = false)
        }
    }

    fun navigateToItem(locationId: Int, cardX: Float, cardY: Float, cardWidth: Float){
        _homeState.update {
            it.copy(
                zoom = /*!_homeState.value.zoom*/ true,
                selectedCardX = cardX,
                selectedCardY = cardY,
                cardWidth = cardWidth,
                selectedLocationId = locationId
            )
        }
    }

    fun updateSelectedLocationId(locationId: Int){
        _homeState.update {
            it.copy(
                selectedLocationId = locationId
            )
        }
    }

    /*fun updateTopPaddingOffset(offset: Float){
        _homeState.update {
            it.copy(topPaddingOffset = offset)
        }
    }*/

    fun updateVerticalOffsetDifference(diff: Float){
        _homeState.update {
            it.copy(verticalOffsetDifference = diff)
        }
    }

    fun updateFilterText(text: String){
        _homeState.update {
            it.copy(filterText = text)
        }
    }

    fun updateClockGaugeLock(lock: Boolean){
        viewModelScope.launch {
            preferenceRepository.setClockGaugeLock(lock)
        }
        _homeState.update {
            it.copy(clockGaugeLock = lock)
        }
    }





    fun updateLocationsCount(count: Int){
        _homePaneState.update {
            it.copy(
                listItemCount = count
            )
        }
    }
    fun updateInitialPage(page: Int){
        _homePaneState.update {
            it.copy(
                initialPage = page
            )
        }
    }
    fun modifyInitialPage(deletedPage: Int){
        if (_homePaneState.value.initialPage >= deletedPage){
            _homePaneState.update {
                it.copy(
                    initialPage = _homePaneState.value.initialPage - 1
                )
            }
        }
    }
    fun updatePageNumberReturningFromDetail(page: Int){
        _homePaneState.update {
            it.copy(
                pageNumberReturningFromDetail = page
            )
        }
    }
    fun updateHomePaneContent(content: HomePaneContent){
        _homePaneState.update {
            it.copy(
                paneContent = content
            )
        }
    }

    val landScapeNames = listOf(
        "1_1_1", "1_1_2", "1_1_3", "1_1_4",
        "1_3_1", "1_3_2", "1_3_3", "1_3_4",
        "2_1_1", "2_1_2", "2_1_3", "2_1_4",
        "2_3_1", "2_3_2", "2_3_3", "2_3_4",
        "3_1_1", "3_1_2", "3_1_3", "3_1_4",
        "3_3_1", "3_3_2", "3_3_3", "3_3_4",
        "4_1_1", "4_1_2", "4_1_3", "4_1_4",
        "4_3_1", "4_3_2", "4_3_3", "4_3_4",
        "5_1_1", "5_1_2", "5_1_3", "5_1_4",
        "6_1_1", "6_1_2", "6_1_3", "6_1_4",
        "7_1_1", "7_1_2", "7_1_3", "7_1_4",
    )
    fun downloadAndSaveLandscapes(context: Context, picId: Int, picUrl: String, picName: String?) =
        viewModelScope.launch {
            try {
                val storageDir = File(context.filesDir, "pictures/pic${picId}")
                if (!storageDir.exists()) {
                    storageDir.mkdirs()
                }
                if (picName != null) {
                    val file = File(storageDir, "${picName}.jpeg")
                    val url = "${picUrl}/${picName}.jpeg"
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .target(
                            onError = {
                                file.deleteRecursively()
                            },
                            onSuccess = { image ->
                                file.outputStream().use { outputStream ->
                                    image.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                                }
                            }
                        )
                        .build()
                    context.imageLoader.execute(request)
                } else {
                    landScapeNames.map { name ->
                        val file = File(storageDir, "${name}.jpeg")
                        val url = "${picUrl}/${name}.jpeg"
                        val request = ImageRequest.Builder(context)
                            .data(url)
                            .target(
                                onError = {
                                    file.deleteRecursively()
                                },
                                onSuccess = { image ->
                                    file.outputStream().use { outputStream ->
                                        image.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                                    }
                                }
                            )
                            .build()
                        async(Dispatchers.IO) {
                            context.imageLoader.execute(request)
                        }
                    }.joinAll()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
    }
}