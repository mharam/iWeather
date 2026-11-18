package com.takaapoo.weatherer.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaapoo.weatherer.di_modules.IoDispatcher
import com.takaapoo.weatherer.di_modules.UtcClock
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.RadarState
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.repository.RadarRepository
import com.takaapoo.weatherer.ui.screens.radar.RadarClocks
import com.takaapoo.weatherer.ui.screens.radar.RadarWeatherQuantity
import com.takaapoo.weatherer.ui.screens.radar.WeatherModels
import com.takaapoo.weatherer.utility.NetworkObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.floor
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RadarViewModel @Inject constructor(
    @param:ApplicationContext val applicationContext: Context,
    val radarRepository: RadarRepository,
    private val preferenceRepository: PreferenceRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @param:UtcClock private val utcClock: Clock,
): ViewModel() {
    private val _radarState = MutableStateFlow(RadarState())
    val radarState = _radarState.asStateFlow()

    private val networkObserver = NetworkObserver(applicationContext)
    val internetConnected: StateFlow<Boolean> = networkObserver.connectivityStatusFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = networkObserver.isConnectedToInternet()
        )


    init {
        viewModelScope.launch(ioDispatcher) {
            updateRadarInitializationStatus(MyResult.Loading())
            val settings = preferenceRepository.getSettings()
            _radarState.update {
                it.copy(
                    crossHairVisible = settings.radarCrossHairVisible,
                    elevationVisible = settings.radarElevationVisible,
                    clockGaugeVisible = settings.radarClockGaugeVisible,
                    clock = settings.radarClock.toClock()
                )
            }
//            internetConnected.collect { isConnected ->
//                initializeClockGauge()
//                launch {
//                    while (isConnected) {
//                        updateRadarSpecs()
//                        delay(30.minutes)
//                    }
//                }
//            }
            if (!internetConnected.value)
                updateRadarSpecs()

            internetConnected.flatMapLatest { isConnected ->
                initializeClockGauge()
                flow<Unit> {
                    while (isConnected) {
                        this@launch.updateRadarSpecs()
                        delay(30.minutes)
                    }
                }
            }.collect()
        }
    }

    private suspend fun CoroutineScope.updateRadarSpecs() {
        val specsResultDeferred =  WeatherModels.entries.map {
            async {
                radarRepository.getRadarSpecs(it.metadata)
            }
        }
        try {
            val radarSpecs = specsResultDeferred.awaitAll().mapIndexedNotNull { index, specsResult ->
                if (specsResult is MyResult.Success) WeatherModels.entries[index] to specsResult.data
                else null
            }.toMap()
            _radarState.update {
                it.copy(
                    radarSpecs = radarSpecs
                )
            }
            if (radarSpecs.isEmpty())
                updateRadarInitializationStatus(MyResult.Error(message = "No radar data"))
            else
                updateRadarInitializationStatus(MyResult.Success(""))
        } catch (e: Exception) {
            updateRadarInitializationStatus(MyResult.Error(e))
        }

    }

    fun initializeClockGauge() {
        val utcNow = LocalDateTime.now(utcClock)
        val now = LocalDateTime.now(_radarState.value.clock)
        val utcOffset = if (_radarState.value.clock == Clock.systemUTC()) 0f
            else ZonedDateTime.now(ZoneId.systemDefault()).offset.totalSeconds / 3600f

        _radarState.update {
            it.copy(
                clockGaugeNaturalRotation = - (utcNow.toLocalTime().hour + utcOffset + 1) * 15f,
                dayGaugeNaturalIndex = Int.MAX_VALUE / 2 + now.dayOfWeek.value - 1 +
                        (now.toLocalTime().hour + 1) / 24,
            )
        }
    }

    private fun updateRadarInitializationStatus(status: MyResult<out String>) {
        _radarState.update {
            it.copy(
                radarInitializationStatus = status
            )
        }
    }

    fun reloadRadarData() {
        updateRadarInitializationStatus(MyResult.Loading())
        viewModelScope.launch {
            updateRadarSpecs()
        }
    }

    fun updateRadarQuantity(quantity: RadarWeatherQuantity) {
        _radarState.update {
            it.copy(
                quantity = quantity
            )
        }
    }
    fun updateQuantityDialogVisible(expanded: Boolean) {
        _radarState.update {
            it.copy(
                quantityDialogVisible = expanded
            )
        }
    }
    fun updateLoadingIndicatorVisible(visible: Boolean) {
        _radarState.update {
            it.copy(
                loadingIndicatorVisible = visible
            )
        }
    }
    fun updateQuantityValue(value: Float?) {
        _radarState.update {
            it.copy(
                quantityValue = value
            )
        }
    }
    fun updateQuantityWeatherModels(quantity: RadarWeatherQuantity, model: WeatherModels) {
        _radarState.update {
            it.copy(
                quantityWeatherModels = it.quantityWeatherModels.toMutableMap().apply { set(quantity, model) }
            )
        }
    }
    fun updateElevation(elevation: Float?) {
        _radarState.update {
            it.copy(
                elevation = elevation
            )
        }
    }
    fun updateSettingsOpen(open: Boolean) {
        _radarState.update {
            it.copy(
                settingsOpen = open
            )
        }
    }
    fun updateCrossHairVisible(visible: Boolean) {
        viewModelScope.launch {
            preferenceRepository.setRadarCrossHairVisible(visible)
        }
        _radarState.update {
            it.copy(
                crossHairVisible = visible
            )
        }
    }
    fun updateElevationVisible(visible: Boolean) {
        viewModelScope.launch {
            preferenceRepository.setRadarElevationVisible(visible)
        }
        _radarState.update {
            it.copy(
                elevationVisible = visible
            )
        }
    }
    fun updateClockGaugeVisible(visible: Boolean) {
        viewModelScope.launch {
            preferenceRepository.setRadarClockGaugeVisible(visible)
        }
        _radarState.update {
            it.copy(
                clockGaugeVisible = visible
            )
        }
    }
    fun updateClock(clock: RadarClocks) {
        viewModelScope.launch {
            preferenceRepository.setRadarClock(clock)
        }
        _radarState.update {
            it.copy(
                clock = clock.toClock()
            )
        }
        initializeClockGauge()
    }


    fun updateVisibleDayIndex(newIndex: Int){
        _radarState.update {
            it.copy(
                visibleDayIndex = newIndex
            )
        }
    }
    fun updateDayGaugeIndex(newVisibleDay: Int){
        _radarState.update {
            val daysOfHours = floor(-(it.clockGaugeRotation.value + it.clockGaugeNaturalRotation) / 360f).toInt()
            it.copy(
                dayGaugeIndex = newVisibleDay - (it.dayGaugeNaturalIndex + daysOfHours)
            )
        }
    }
    fun updateClockGaugeRotation(rotation: Float){
        viewModelScope.launch {
            _radarState.value.clockGaugeRotation.snapTo(rotation)
        }
    }
    fun updateClockGaugeDragStopped(stopped: Boolean){
        _radarState.update {
            it.copy(
                clockGaugeDragStopped = stopped
            )
        }
    }

    fun balanceClockGaugeValues(){
        val daysOfHours = floor(
            -(_radarState.value.clockGaugeRotation.value + _radarState.value.clockGaugeNaturalRotation) / 360f
        ).toInt()
        _radarState.update {
            it.copy(
                dayGaugeIndex = _radarState.value.dayGaugeIndex + daysOfHours
            )
        }
        updateClockGaugeRotation(_radarState.value.clockGaugeRotation.value + daysOfHours * 360)
    }
    fun resetDayGauge(){
        _radarState.update {
            it.copy(dayGaugeIndex = 0)
        }
    }
    fun calculateDataIndex(weatherModel: WeatherModels): Int? {
        val validTimes = _radarState.value.radarSpecs.getOrDefault(weatherModel, null)
            ?.validTimes?.map { OffsetDateTime.parse(it).toLocalDateTime() }
        return validTimes?.indexOfFirst { _radarState.value.clockGaugeTime == it }
    }

    fun updateNoDataWarningVisible(visible: Boolean){
        _radarState.update {
            it.copy(
                noDataWarningVisible = visible
            )
        }
    }
    fun updateClockGaugeTime() {
        val utcNow = LocalDateTime.now(utcClock)
        _radarState.update {
            it.copy(
                clockGaugeTime = utcNow.truncatedTo(ChronoUnit.HOURS).plusHours(1)
                    .plusDays(_radarState.value.dayGaugeIndex.toLong())
                    .plusHours((-_radarState.value.clockGaugeRotation.value / 15).roundToLong())
            )
        }
    }

}