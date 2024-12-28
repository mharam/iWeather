package com.takaapoo.weatherer.ui.viewModels

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.WeatherType.Companion.calculateMoonType
import com.takaapoo.weatherer.domain.model.HomePaneContent
import com.takaapoo.weatherer.domain.model.HomePaneState
import com.takaapoo.weatherer.domain.model.HomeState
import com.takaapoo.weatherer.domain.model.LocationsState
import com.takaapoo.weatherer.domain.model.WeatherDto
import com.takaapoo.weatherer.domain.repository.DataRefreshRepository
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.LocationRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.use_case.UpdateWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sign

enum class DialogType{
    DELETE, EDIT
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

    init {
        dataRefreshRepository.refreshData()
        viewModelScope.launch{
            val appSettings = preferenceRepository.getSettings()
            updateClockGaugeLock(appSettings.clockGaugeLock)
        }
    }
    val locationsCount = locationRepository.locationCount()
        .stateIn(
            scope = viewModelScope,
            started =  SharingStarted.WhileSubscribed(5000),
            initialValue = -1
        )
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
    private var weight1 = 0f
    private var weight2 = 0f
    private val dayGaugeMinutesFlow = _homeState.map { it.dayGaugeIndex*1440 }/*.distinctUntilChanged()*/
    private val clockGaugeMinutesFlow = snapshotFlow { (-_homeState.value.clockGaugeRotation.value*4).toLong() }
//    private val allLocationsWeatherGrouped = localWeatherRepository.getAllLocationsWeather()
//        .transform {weatherDTOList ->
//            emit(weatherDTOList.groupBy { it.locationId })
//        }

    private val allLocationsWeatherGrouped = combine(
        locationRepository.getAllLocations(),
        utcDateTimeFlow,
        clockGaugeMinutesFlow,
        dayGaugeMinutesFlow,
        preferenceRepository.getSettingsFlow()
    ) { loc, utcTime, clockMinuteIncremented, dayMinuteIncremented, appSettings ->
        val modifiedUtcDate = utcTime
            .plusMinutes(clockMinuteIncremented + dayMinuteIncremented)
            .toLocalDate()
        val startDate = modifiedUtcDate.minusDays(6).toString()
        val endDate = modifiedUtcDate.plusDays(6).toString()
        val baseData = loc.map {
            WeatherDto(
                locationId = it.id,
                locationName = it.name,
                latitude = it.latitude,
                longitude = it.longitude,
                utcOffset = it.utcOffset
            )
        }
        val out = localWeatherRepository.getAllLocationsWeather(
            startDate = startDate,
            endDate = endDate
        ).map {
            it.applyUnits(appSettings)
        }
        (baseData + out).groupBy { it.locationId }
    }

//    val clockGaugeClickCounter = clockGaugeMinutesFlow.map { it / 60 }.distinctUntilChanged()
    val locationsState: StateFlow<List<LocationsState>> =
        combine(
            allLocationsWeatherGrouped,
            utcDateTimeFlow,
            clockGaugeMinutesFlow,
            dayGaugeMinutesFlow
        ){ weatherDTOMap , utcTime, clockMinuteIncremented, dayMinuteIncremented ->
            if (weatherDTOMap.isEmpty())
                return@combine emptyList<LocationsState>()

            val locationCounts = weatherDTOMap.count()
            val modifiedUtcTime = utcTime.plusMinutes(
                clockMinuteIncremented + dayMinuteIncremented
            )

            return@combine List(size = locationCounts) { index ->
                val allTimesValue = weatherDTOMap.values.elementAt(index).map { it.time }
                val timeIndex = allTimesValue.indexOfFirst {
                    try { LocalDateTime.parse(it) > modifiedUtcTime }
                    catch (_: DateTimeParseException) { false }
                }
                if (timeIndex < 2) {
                    val id = weatherDTOMap.keys.elementAt(index)
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
                    weight2 = abs(
                        Duration.between(modifiedUtcTime, LocalDateTime.parse(allTimesValue[timeIndex - 1]))
                            .toSeconds()
                    ) / 3600f
                    weight1 = 1 - weight2
                    LocationsState(
                        locationId = id,
                        locationName = dto!!.locationName,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        currentTemperature =
                        if (weatherDTOMap[id]?.get(timeIndex - 1)?.currentTemperature == null
                            || weatherDTOMap[id]?.get(timeIndex)?.currentTemperature == null) null
                        else weight1 * weatherDTOMap[id]!![timeIndex - 1].currentTemperature!!
                                + weight2 * weatherDTOMap[id]!![timeIndex].currentTemperature!!,
                        currentHumidity =
                        if (weatherDTOMap[id]?.get(timeIndex - 1)?.currentHumidity == null
                            || weatherDTOMap[id]?.get(timeIndex)?.currentHumidity == null) null
                        else weight1 * weatherDTOMap[id]!![timeIndex - 1].currentHumidity!!
                                + weight2 * weatherDTOMap[id]!![timeIndex].currentHumidity!!,
                        currentPrecipitationProbability =
                        if (weatherDTOMap[id]?.get(timeIndex - 1)?.currentPrecipitationProbability == null
                            || weatherDTOMap[id]?.get(timeIndex)?.currentPrecipitationProbability == null) null
                        else weight1 * weatherDTOMap[id]!![timeIndex - 1].currentPrecipitationProbability!!
                                + weight2 * weatherDTOMap[id]!![timeIndex].currentPrecipitationProbability!!,
                        currentWeatherCode =
                        if (weight2 > 1) weatherDTOMap[id]!![timeIndex].currentWeatherCode
                        else weatherDTOMap[id]!![timeIndex - 1].currentWeatherCode,
                        currentWindSpeed =
                        if (weatherDTOMap[id]?.get(timeIndex - 1)?.currentWindSpeed == null
                            || weatherDTOMap[id]?.get(timeIndex)?.currentWindSpeed == null) null
                        else weight1 * weatherDTOMap[id]!![timeIndex - 1].currentWindSpeed!!
                                + weight2 * weatherDTOMap[id]!![timeIndex].currentWindSpeed!!,
                        currentWindDirection =
                        if (weatherDTOMap[id]?.get(timeIndex - 1)?.currentWindDirection == null
                            || weatherDTOMap[id]?.get(timeIndex)?.currentWindDirection == null) null
                        else weight1 * weatherDTOMap[id]!![timeIndex - 1].currentWindDirection!!
                                + weight2 * weatherDTOMap[id]!![timeIndex].currentWindDirection!!,
                        currentAirQuality = if (weatherDTOMap[id]?.get(timeIndex - 1)?.usAQI == null
                            || weatherDTOMap[id]?.get(timeIndex)?.usAQI == null) null
                        else (weight1 * weatherDTOMap[id]!![timeIndex - 1].usAQI!!
                                + weight2 * weatherDTOMap[id]!![timeIndex].usAQI!!).roundToInt(),
                        todayMaxTemperature = weatherDTOMap[id]!!.getOrNull(dayTimeIndex)?.todayMaxTemperature,
                        todayMinTemperature = weatherDTOMap[id]!!.getOrNull(dayTimeIndex)?.todayMinTemperature,
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
        }.stateIn(
            scope = viewModelScope.plus(Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(LocationsState(locationId = -2))
        )

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
            updateWeatherUseCase.updateAllLocationsWeather()
            _homeState.update { it.copy(isRefreshing = false) }
        }
    }

    fun updateALocationWeatherData(location: Location){
        viewModelScope.launch {
            updateWeatherUseCase.updateALocationWeather(location)
        }
    }

    fun updateDialogVisibility (dialogType: DialogType, visible: Boolean){
        _homeState.update {
            when (dialogType){
                DialogType.DELETE -> it.copy(deleteDialogVisible = visible)
                DialogType.EDIT -> it.copy(editDialogVisible = visible)
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



    private val clickSound = List(10){ MediaPlayer.create(applicationContext, R.raw.click1) }
    fun handleClickSound(currentRotation: Float, previousRotation: Float){
        viewModelScope.launch(Dispatchers.Default){
            val diff = abs((currentRotation / 3.75f).toInt() - (previousRotation / 3.75f).toInt())
            if (currentRotation.sign != previousRotation.sign){
                playClick(1)
            } else {
                playClick(diff)
            }
        }
    }
    private suspend fun playClick(count: Int){
        for (i in 0 until count.coerceAtMost(10)){
            clickSound[i].start()
            delay(20)
        }
    }

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
}