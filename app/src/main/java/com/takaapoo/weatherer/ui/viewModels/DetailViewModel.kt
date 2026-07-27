package com.takaapoo.weatherer.ui.viewModels

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.LocalHistoricDailyWeather
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.di_modules.DefaultDispatcher
import com.takaapoo.weatherer.di_modules.IoDispatcher
import com.takaapoo.weatherer.di_modules.MainDispatcher
import com.takaapoo.weatherer.di_modules.SystemClock
import com.takaapoo.weatherer.di_modules.UtcClock
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.domain.model.HistoricDailyChartDataFlow
import com.takaapoo.weatherer.domain.model.HistoricDailyChartDto
import com.takaapoo.weatherer.domain.model.HistoricDailyChartStatePart
import com.takaapoo.weatherer.domain.model.HistoricHourlyChartDataFlow
import com.takaapoo.weatherer.domain.model.HistoricHourlyChartDto
import com.takaapoo.weatherer.domain.model.HistoricHourlyChartStatePart
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.domain.model.SettingsUnits
import com.takaapoo.weatherer.domain.model.toSettingsUnits
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.LocationRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.domain.use_case.GetHourlyChartDataUseCase
import com.takaapoo.weatherer.domain.use_case.HistoricalDailyChartDataUseCase
import com.takaapoo.weatherer.domain.use_case.HistoricalHourlyChartDataUseCase
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.DetailTabs
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.bitmapHeight
import com.takaapoo.weatherer.ui.screens.detail.bitmapWidth
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.daySeconds
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.timeToX
import com.takaapoo.weatherer.ui.theme.timeFontFamily
import com.takaapoo.weatherer.utility.isEqualTo
import com.takaapoo.weatherer.utility.landScapeName
import com.takaapoo.weatherer.utility.mmToUnit
import com.takaapoo.weatherer.utility.swap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val locationRepository: LocationRepository,
    private val localWeatherRepository: LocalWeatherRepository,
//    private val airQualityRepository: AirQualityRepository,
    private val hourlyChartDataUseCase: GetHourlyChartDataUseCase,
    private val historicalHourlyChartDataUseCase: HistoricalHourlyChartDataUseCase,
    private val historicalDailyChartDataUseCase: HistoricalDailyChartDataUseCase,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @param:SystemClock private val systemClock: Clock,
    @param:UtcClock private val utcClock: Clock,
//    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var allLocations: List<Location> = emptyList()
    val allLocationsFlow: StateFlow<List<Location>> = locationRepository.getAllLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val settingsFlow = preferenceRepository.getSettingsFlow()
    var settingsUnits: SettingsUnits = SettingsUnits()

    private val _detailState: MutableStateFlow<List<DetailState>> = MutableStateFlow(
        List(size = 0){ DetailState() }
    )
    val detailState: StateFlow<List<DetailState>> = _detailState.asStateFlow()
    var detailStateUpdatePermission = true

    // Used for when deleting or swapping locations
    private val _dataReadyForDisplay = MutableStateFlow(false)
    val dataReadyForDisplay = _dataReadyForDisplay.asStateFlow()

    private var _hourlyChartState: MutableList<MutableStateFlow<HourlyChartState>> =
        MutableList(size = 0){ MutableStateFlow(HourlyChartState()) }
    val hourlyChartState: List<StateFlow<HourlyChartState>>
        get() = _hourlyChartState.map { it.asStateFlow() }

//    private var _hourlyChartCurveValue: MutableList<MutableStateFlow<List<Float?>>> =
//        MutableList(size = 0){ MutableStateFlow(listOf(null)) }
//    val hourlyChartCurveValue: List<StateFlow<List<Float?>>>
//        get() = _hourlyChartCurveValue.map { it.asStateFlow() }

    private var _dailyChartState: MutableList<MutableStateFlow<DailyChartState>> =
        MutableList(size = 0){ MutableStateFlow(DailyChartState()) }
    val dailyChartState: List<StateFlow<DailyChartState>>
        get() = _dailyChartState.map { it.asStateFlow() }

    private var _locationsHourlyChartData: MutableList<StateFlow<List<HourlyChartDto>>> =
        MutableList(size = 0){ MutableStateFlow(emptyList()) }
    val locationsHourlyChartData: List<StateFlow<List<HourlyChartDto>>>
        get() = _locationsHourlyChartData

    private var _locationsDailyChartData: MutableList<StateFlow<List<LocalDailyWeather>>> =
        MutableList(size = 0){ MutableStateFlow(emptyList()) }
    val locationsDailyChartData: List<StateFlow<List<LocalDailyWeather>>>
        get() = _locationsDailyChartData

    private var _historicalHourlyChartState: MutableList<MutableStateFlow<HourlyChartState>> =
        MutableList(size = 0) {
            MutableStateFlow(HourlyChartState( chartQuantities = emptyList() ))
        }
    val historicalHourlyChartState: List<StateFlow<HourlyChartState>>
        get() = _historicalHourlyChartState.map { it.asStateFlow() }

    var historicalHourlyChartData: MutableList<StateFlow<List<HistoricHourlyChartDto>>> =
        MutableList(size = 0){ MutableStateFlow(emptyList()) }

    private var _historicalDailyChartState: MutableList<MutableStateFlow<DailyChartState>> =
        MutableList(size = 0){ MutableStateFlow(DailyChartState()) }
    val historicalDailyChartState: List<StateFlow<DailyChartState>>
        get() = _historicalDailyChartState.map { it.asStateFlow() }

    var historicalDailyChartData: MutableList<StateFlow<List<HistoricDailyChartDto>>> =
        MutableList(size = 0){ MutableStateFlow(emptyList()) }

    var initialLandScape = createBitmap(bitmapWidth, bitmapHeight)

    var initialXAxisStart = -30f
    var initialXAxisEnd = 48f
    var historicInitialXAxisStart = -30f
    var historicInitialXAxisEnd = 48f
    private lateinit var initialYAxesStarts: List<MutableList<Float>>
    private lateinit var initialYAxesEnds: List<MutableList<Float>>
    private lateinit var initialHistoricYAxesStarts: List<MutableList<Float>>
    private lateinit var initialHistoricYAxesEnds: List<MutableList<Float>>
    private lateinit var initialDailyYAxesStarts: MutableList<Float>
    private lateinit var initialDailyYAxesEnds: MutableList<Float>
    private lateinit var initialHistoricDailyYAxesStarts: MutableList<Float>
    private lateinit var initialHistoricDailyYAxesEnds: MutableList<Float>
    private var initializedCompleted = false


    init {
        initialize()
    }

    private fun initialize(){
        viewModelScope.launch {
            allLocations = locationRepository.getAllLocationsList()
            _detailState.value = List(size = allLocations.size){ DetailState() }
            _hourlyChartState = MutableList(size = allLocations.size){
                MutableStateFlow(HourlyChartState())
            }
//            _hourlyChartCurveValue = MutableList(size = allLocations.size){
//                MutableStateFlow(listOf(null))
//            }
            _dailyChartState = MutableList(size = allLocations.size){ MutableStateFlow(DailyChartState()) }
            _historicalHourlyChartState = MutableList(size = allLocations.size){
                MutableStateFlow(HourlyChartState())
            }
            _historicalDailyChartState = MutableList(size = allLocations.size){
                MutableStateFlow(DailyChartState())
            }

            _locationsHourlyChartData = MutableList(size = allLocations.size){ i ->
                hourlyChartDataFlow(i)
            }
            _locationsDailyChartData = MutableList(size = allLocations.size){ i ->
                dailyChartDataFlow(i)
            }
            historicalHourlyChartData = MutableList(size = allLocations.size){ i ->
                historicHourlyChartDataFlow(i)
            }
            historicalDailyChartData = MutableList(size = allLocations.size){ i ->
                historicDailyChartDataFlow(i)
            }

            initialYAxesStarts = List(allLocations.size){ mutableListOf() }
            initialYAxesEnds = List(allLocations.size){ mutableListOf() }
            initialDailyYAxesStarts = MutableList(allLocations.size){ 0f }
            initialDailyYAxesEnds = MutableList(allLocations.size){ 0f }
            initialHistoricYAxesStarts = List(allLocations.size){ mutableListOf() }
            initialHistoricYAxesEnds = List(allLocations.size){ mutableListOf() }
            initialHistoricDailyYAxesStarts = MutableList(allLocations.size){ 0f }
            initialHistoricDailyYAxesEnds = MutableList(allLocations.size){ 0f }

            launch(defaultDispatcher) {
                while (detailStateUpdatePermission) {
                    updateDetailState()
                    delay(600_000)
                }
            }
            launch(defaultDispatcher) {
                while (detailStateUpdatePermission) {
//                    println("while!")
                    if (allLocations.size != _detailState.value.size) continue
                    allLocations.forEachIndexed { index, location ->
                        _detailState.update { detailStateList ->
                            detailStateList.toMutableList().apply {
                                this[index] = this[index].copy(
                                    localDateTime = LocalDateTime.now(utcClock)
                                        .plusMinutes(location.utcOffset ?: 0)
                                )
                            }
                        }
                    }
                    delay(400_000)
                }
            }
            initializeSliderThumbPositions()

            launch(defaultDispatcher) {
                settingsFlow.map { it.toSettingsUnits() }
                    .distinctUntilChanged()
                    .collectLatest {
                        settingsUnits = it
                        updateDetailState()
                        _hourlyChartState.forEachIndexed { index, _ ->
                            clearYAxis(index, false)
                        }
                        _historicalHourlyChartState.forEachIndexed { index, _ ->
                            clearYAxis(index, true)
                        }
                        updateHourlySettingsStatus(false)
                        updateDailySettingsStatus(false)
                    }
            }
            // This is needed when refreshing weather data to update current weather in detail screen instantly
            launch(defaultDispatcher) {
                allLocationsFlow.collectLatest {
                    modifyAllLocations(it)
                    updateDetailState()
                }
            }
            initializedCompleted = true
            adjustSettings()
            _dataReadyForDisplay.value = true
        }
    }

    private fun modifyAllLocations(newLocations: List<Location>) {
        val newAllLocations = allLocations.toMutableList()
        newLocations.forEach { location ->
            val index = allLocations.indexOfFirst { it.id == location.id }
            if (index != -1) {
                newAllLocations[index] = location
            }
        }
        allLocations = newAllLocations
    }

    private fun hourlyChartDataFlow(index: Int) =
        combine(
            _hourlyChartState.getOrElse(index){ flowOf(HourlyChartState()) }.flatMapLatest {
                snapshotFlow {
                    val extraDataLoadingDays = ceil(
                        (it.xAxisEnd.value - it.xAxisStart.value) / (1.5f * 24)
                    ).toLong()
                    val startDate = LocalDate.now().plusDays(
                        floor(it.xAxisStart.value / 24).toLong() - extraDataLoadingDays
                    ).toString()
                    val endDate = LocalDate.now().plusDays(
                        ceil(it.xAxisEnd.value / 24).toLong() + extraDataLoadingDays
                    ).toString()
                    startDate to endDate
                }.distinctUntilChanged()
            },
            settingsFlow
        ){ datePairs, appSettings ->

            /*val zone = when (appSettings.hourlyDiagramTime) {
                HourlyDiagramTime.UTC -> ZoneId.of("UTC")
                HourlyDiagramTime.DEVICE -> ZoneId.systemDefault()
                HourlyDiagramTime.LOCATION -> {
                    ZoneOffset.ofTotalSeconds((allLocations[index].utcOffset?.toInt() ?: 0) * 60)
                }
            }*/

            updateHourlySettingsStatus(appSettings.toSettingsUnits() == settingsUnits)
            allLocations.getOrNull(index)?.id?.let {
                hourlyChartDataUseCase(
                    locationId = it,
                    startDate = datePairs.first,
                    endDate = datePairs.second,
                    appSettings = appSettings
                )
            } ?: emptyList()

        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun dailyChartDataFlow(index: Int) = combine(
        _dailyChartState.getOrElse(index){ flowOf(DailyChartState()) }.flatMapLatest {
            snapshotFlow {
                val extraDataLoadingDays =
                    ceil((it.xAxisEnd.value - it.xAxisStart.value) / 1.5f).toLong()
                val startDate = LocalDate.now().plusDays(
                    (it.xAxisStart.value).toLong() - extraDataLoadingDays
                ).toString()
                val endDate = LocalDate.now().plusDays(
                    (it.xAxisEnd.value).toLong() + extraDataLoadingDays
                ).toString()
                startDate to endDate
            }.distinctUntilChanged()
        },
        settingsFlow
    ) { datePairs, appSettings ->
        updateDailySettingsStatus(appSettings.toSettingsUnits() == settingsUnits)
        allLocations.getOrNull(index)?.id?.let {
            localWeatherRepository.getLocationDailyWeather(
                locationId = allLocations[index].id,
                startDate = datePairs.first,
                endDate = datePairs.second,
                appSettings = appSettings
            )
        } ?: emptyList()
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(FlowPreview::class)
    private fun historicHourlyChartDataFlow(index: Int): StateFlow<List<HistoricHourlyChartDto>> {
        return combine(
            _historicalHourlyChartState.getOrElse(index){ flowOf(HourlyChartState()) }
                .flatMapLatest {
                    snapshotFlow {
                        HistoricHourlyChartStatePart(
                            chartQuantities = it.chartQuantities,
                            xAxisStart = it.xAxisStart.value,
                            xAxisEnd = it.xAxisEnd.value,
                            awaker = it.awaker,
                        )
                    }
                }
                .debounce(100)
                .distinctUntilChangedBy {
                    HistoricHourlyChartStatePart(
                        chartQuantities = it.chartQuantities,
                        xAxisStart = floor(it.xAxisStart/24),
                        xAxisEnd = ceil(it.xAxisEnd/24),
                        awaker = it.awaker,
                    )
                },
            settingsFlow,
            _detailState.map { it.getOrNull(index)?.detailPagerIndex }.distinctUntilChanged()
        ) { historicHourlyChartStatePart, appSettings, detailPagerIndex ->
            HistoricHourlyChartDataFlow(
                chartQuantities = historicHourlyChartStatePart.chartQuantities,
                xAxisStart = historicHourlyChartStatePart.xAxisStart,
                xAxisEnd = historicHourlyChartStatePart.xAxisEnd,
                appSettings = appSettings,
                detailPagerIndex = detailPagerIndex,
            )
        }
            .transformLatest {
                if (index == it.detailPagerIndex) {
                    updateHourlySettingsStatus(it.appSettings.toSettingsUnits() == settingsUnits)
                    val weatherQuantities = it.chartQuantities
                    if (weatherQuantities.isNotEmpty()) {
                        runCatching {
                            withTimeout(30_000) {
                                allLocations.getOrNull(index)?.let { location ->
                                    Log.i("data1", "historicalHourlyChartDataUseCase called")
                                    historicalHourlyChartDataUseCase(
                                        location = location,
                                        xAxisStart = it.xAxisStart,
                                        xAxisEnd = it.xAxisEnd,
                                        weatherQuantities = weatherQuantities,
                                        appSettings = it.appSettings,
                                        flowCollector = this@transformLatest
                                    )
                                }
                            }
                        }.onFailure { throwable ->
                            if (throwable is TimeoutCancellationException){
                                emit(MyResult.Error(message = "Connection timeout!"))
                            }
                        }
                        updateXAxisAnimationTrigger(index, true)
                    }
                }
            }.transform {
                updateDataLoadingStatus(
                    pageIndex = index,
                    status = it
                )
                if (it is MyResult.Success) {
                    emit(it.data as List<HistoricHourlyChartDto>)
                }
            }.stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    @OptIn(FlowPreview::class)
    private fun historicDailyChartDataFlow(index: Int): StateFlow<List<HistoricDailyChartDto>> {
        return combine(
            _historicalDailyChartState.getOrElse(index){ flowOf(DailyChartState()) }
                .flatMapLatest {
                    snapshotFlow {
                        HistoricDailyChartStatePart(
                            chartQuantity = it.chartQuantity,
                            xAxisStart = it.xAxisStart.value,
                            xAxisEnd = it.xAxisEnd.value,
                            awaker = it.awaker,
                        )
                    }
                }
                .debounce(100)
                .distinctUntilChanged(),
            settingsFlow,
            _detailState.map { it.getOrNull(index)?.detailPagerIndex }.distinctUntilChanged()
        ) { historicDailyChartStatePart, appSettings, detailPagerIndex ->
            HistoricDailyChartDataFlow(
                chartQuantity = historicDailyChartStatePart.chartQuantity,
                xAxisStart = historicDailyChartStatePart.xAxisStart,
                xAxisEnd = historicDailyChartStatePart.xAxisEnd,
                appSettings = appSettings,
                detailPagerIndex = detailPagerIndex,
            )
        }
            .transformLatest {
                if (index == it.detailPagerIndex) {
                    updateDailySettingsStatus(it.appSettings.toSettingsUnits() == settingsUnits)
                    runCatching {
                        withTimeout(30_000) {
                            allLocations.getOrNull(index)?.let { location ->
                                historicalDailyChartDataUseCase(
                                    location = location,
                                    xAxisStart = it.xAxisStart,
                                    xAxisEnd = it.xAxisEnd,
                                    weatherQuantity = it.chartQuantity,
                                    appSettings = it.appSettings,
                                    flowCollector = this@transformLatest
                                )
                            }
                        }
                    }.onFailure { throwable ->
                        if (throwable is TimeoutCancellationException){
                            emit(MyResult.Error(message = "Connection timeout!"))
                        }
                    }
                    updateDailyXAxisAnimationTrigger(index, true)
                }
            }.transform {
                updateDailyDataLoadingStatus(
                    pageIndex = index,
                    status = it
                )
                if (it is MyResult.Success) {
                    emit(it.data as List<HistoricDailyChartDto>)
                }
            }.stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    private fun clearYAxis(index: Int, isHistoric: Boolean){
        val chartState = if (isHistoric) _historicalHourlyChartState else _hourlyChartState
        chartState[index].update {
            it.copy(
                yAxesStarts = emptyList(),
                yAxesEnds = emptyList(),
                curveAnimator = emptyList()
            )
        }
        if (isHistoric) {
            initialHistoricYAxesStarts[index].clear()
            initialHistoricYAxesEnds[index].clear()
        } else {
            initialYAxesStarts[index].clear()
            initialYAxesEnds[index].clear()
        }
    }

    fun reInitialize(){
        viewModelScope.launch {
            while (!initializedCompleted)
                delay(20)

            val newAllLocations = locationRepository.getAllLocationsList()
            if (newAllLocations.size <= allLocations.size) return@launch
            _dataReadyForDisplay.value = false
            val addedLocationsCount = newAllLocations.size - allLocations.size

            _detailState.update {
                it + List(size = addedLocationsCount) { DetailState() }
            }
            _hourlyChartState.addAll(
                List(size = addedLocationsCount) { MutableStateFlow(HourlyChartState()) }
            )
//            _hourlyChartCurveValue.addAll(
//                List(size = addedLocationsCount) { MutableStateFlow(listOf(null)) }
//            )
            _dailyChartState.addAll(
                List(size = addedLocationsCount) { MutableStateFlow(DailyChartState()) }
            )
            _historicalHourlyChartState.addAll(
                List(size = addedLocationsCount) { MutableStateFlow(HourlyChartState()) }
            )
            _historicalDailyChartState.addAll(
                List(size = addedLocationsCount) { MutableStateFlow(DailyChartState()) }
            )
            initialYAxesStarts = initialYAxesStarts + List(addedLocationsCount) { mutableListOf() }
            initialYAxesEnds = initialYAxesEnds + List(addedLocationsCount) { mutableListOf() }
            initialDailyYAxesStarts.addAll(List(addedLocationsCount) { 0f })
            initialDailyYAxesEnds.addAll(List(addedLocationsCount) { 0f })
            initialHistoricYAxesStarts = initialHistoricYAxesStarts + List(addedLocationsCount) { mutableListOf() }
            initialHistoricYAxesEnds = initialHistoricYAxesEnds + List(addedLocationsCount) { mutableListOf() }
            initialHistoricDailyYAxesStarts.addAll(List(addedLocationsCount) { 0f })
            initialHistoricDailyYAxesEnds.addAll(List(addedLocationsCount) { 0f })

            allLocations = newAllLocations
            _locationsHourlyChartData.addAll(
                List(size = addedLocationsCount) {
                    hourlyChartDataFlow(it + _locationsHourlyChartData.size)
                }
            )
            _locationsDailyChartData.addAll(
                List(size = addedLocationsCount) {
                    dailyChartDataFlow(it + _locationsDailyChartData.size)
                }
            )
            historicalHourlyChartData.addAll(
                List(size = addedLocationsCount) {
                    historicHourlyChartDataFlow(it + historicalHourlyChartData.size)
                }
            )
            historicalDailyChartData.addAll(
                List(size = addedLocationsCount) {
                    historicDailyChartDataFlow(it + historicalDailyChartData.size)
                }
            )
            updateDetailState()
            adjustSettings()
            _dataReadyForDisplay.value = true
        }
    }

    fun prepareDeleteLocation(deletedLocationId: Int){
        var deletedLocationIndex: Int
        viewModelScope.launch {
            deletedLocationIndex = allLocations.indexOfFirst { it.id == deletedLocationId }
            if (deletedLocationIndex == -1) return@launch
            _dataReadyForDisplay.value = false
            _detailState.update {
                it - it[deletedLocationIndex]
            }
            _hourlyChartState.removeAt(deletedLocationIndex)
//            _hourlyChartCurveValue.removeAt(deletedLocationIndex)
            _dailyChartState.removeAt(deletedLocationIndex)
            _historicalHourlyChartState.removeAt(deletedLocationIndex)
            _historicalDailyChartState.removeAt(deletedLocationIndex)
            initialYAxesStarts = initialYAxesStarts.filterIndexed { index, _ ->
                index != deletedLocationIndex
            }
            initialYAxesEnds = initialYAxesEnds.filterIndexed { index, _ ->
                index != deletedLocationIndex
            }
            initialDailyYAxesStarts.removeAt(deletedLocationIndex)
            initialDailyYAxesEnds.removeAt(deletedLocationIndex)
            initialHistoricYAxesStarts = initialHistoricYAxesStarts.filterIndexed { index, _ ->
                index != deletedLocationIndex
            }
            initialHistoricYAxesEnds = initialHistoricYAxesEnds.filterIndexed { index, _ ->
                index != deletedLocationIndex
            }
            initialHistoricDailyYAxesStarts.removeAt(deletedLocationIndex)
            initialHistoricDailyYAxesEnds.removeAt(deletedLocationIndex)

            allLocations = allLocations.filterIndexed { index, _ ->
                index != deletedLocationIndex
            }

            allLocations.forEachIndexed { i, _ ->
                _locationsHourlyChartData[i] = hourlyChartDataFlow(i)
                _locationsDailyChartData[i] = dailyChartDataFlow(i)
                historicalHourlyChartData[i] = historicHourlyChartDataFlow(i)
                historicalDailyChartData[i] = historicDailyChartDataFlow(i)
            }

//            _locationsHourlyChartData = MutableList(size = allLocations.size){ i ->
//                hourlyChartDataFlow(i)
//            }
//            _locationsDailyChartData = MutableList(size = allLocations.size){ i ->
//                dailyChartDataFlow(i)
//            }
//
//            historicalHourlyChartData = MutableList(size = allLocations.size){ i ->
//                historicHourlyChartDataFlow(i)
//            }
//            historicalDailyChartData = MutableList(size = allLocations.size){ i ->
//                historicDailyChartDataFlow(i)
//            }
            delay(1000)
            _dataReadyForDisplay.value = true
            _locationsHourlyChartData.removeLastOrNull()
            _locationsDailyChartData.removeLastOrNull()
            historicalHourlyChartData.removeLastOrNull()
            historicalDailyChartData.removeLastOrNull()
        }
    }


    private suspend fun updateDetailState(){
        val utcCurrentTime = LocalDateTime.now(utcClock)
        val utcCurrentDate = utcCurrentTime.toLocalDate()
        val targetX = (utcCurrentTime.toLocalTime().toSecondOfDay() / daySeconds) * 24
        if (allLocations.size != _detailState.value.size) return

        val startDate = utcCurrentDate.toString()
        val endDate = utcCurrentDate.plusDays(1).toString()
        val hourlyWeatherDeferred = viewModelScope.async(ioDispatcher) {
            localWeatherRepository.getHourlyWeather(startDate, endDate)
        }
        val hourlyAirQualityDeferred = viewModelScope.async(ioDispatcher) {
            localWeatherRepository.getHourlyAirQuality(startDate, endDate)
        }
        val dailyWeatherDeferred = viewModelScope.async(ioDispatcher) {
            localWeatherRepository.getDailyWeather(
                utcCurrentDate.minusDays(1).toString(),
                utcCurrentDate.plusDays(1).toString()
            )
        }
        val weatherData = awaitAll(hourlyWeatherDeferred, hourlyAirQualityDeferred, dailyWeatherDeferred)
        val allHourlyWeather = weatherData[0] as Map<Int, List<LocalHourlyWeather>>
        val allHourlyAirQuality = weatherData[1] as Map<Int, List<LocalAirQuality>>
        val allDailyWeather = weatherData[2] as Map<Int, List<LocalDailyWeather>>

        coroutineScope {
            val appSettings = preferenceRepository.getSettings()
            allLocations.forEachIndexed { index, location ->
                val hourlyWeather = allHourlyWeather[location.id]?.map { it.setUnits(appSettings) }
                    ?: emptyList()
                val hourlyAirQuality = allHourlyAirQuality[location.id] ?: emptyList()
                val allTimesValue = hourlyWeather.map { it.time }
                val timeIndex = allTimesValue.indexOfFirst {
                    try { LocalDateTime.parse(it) > utcCurrentTime }
                    catch (_: Exception) { false }
                }
                if (timeIndex < 2) return@forEachIndexed

                val localDate = utcCurrentTime.plusMinutes(location.utcOffset ?: 0).toLocalDate().toString()
//                    val dailyWeather = localWeatherRepository.getLocationDailyWeather(
//                        locationId = location.id,
//                        startDate = localDate,
//                        endDate = localDate,
//                        appSettings = appSettings
//                    )
                val todayWeather = allDailyWeather[location.id]?.find {
                    it.time == localDate
                }?.setUnits(appSettings)
                val currentWeather = hourlyWeather.getOrNull(timeIndex - 1)
                _detailState.update { detailStateList ->
                    detailStateList.toMutableList().apply {
                        this[index] = this[index].copy(
                            locationName = location.name,
                            utcOffset = location.utcOffset,
                            maxTemperature = todayWeather?.temperatureMax,
                            minTemperature = todayWeather?.temperatureMin,
                            sunRise = todayWeather?.sunRise,
                            sunSet = todayWeather?.sunSet,
                            currentDayHourlyWeather = hourlyWeather,
                            currentDayHourlyAirQuality = hourlyAirQuality,
                            targetX = targetX,
                            localDateTime = LocalDateTime.now(utcClock).plusMinutes(location.utcOffset ?: 0),
                            currentPictureUrl = location.currentPictureUrl,
                            lastPicId = location.picIdsList.lastOrNull(),
                            picName = landScapeName(
                                todayWeather = todayWeather,
                                currentWeather = currentWeather,
                                utcOffset = location.utcOffset,
                                utcTime = utcCurrentTime
                            ),
                        )
                    }
                }
            }
        }
    }


    fun initializeSliderThumbPositions(diagramTime: HourlyDiagramTime? = null){
        val utcNow = LocalDateTime.now(utcClock).toString()
        viewModelScope.launch {
            // This while is used to make curve and thumb reposition synced when modifying diagram
            // time axis clock.
            while (diagramTime != null && diagramTime != preferenceRepository.getSettings().hourlyDiagramTime)
                delay(1)

            val hourlyDiagramTime =
                diagramTime ?: preferenceRepository.getSettings().hourlyDiagramTime
            allLocations.forEachIndexed { index, location ->
                val nowX = timeToX(utcNow, location.utcOffset, hourlyDiagramTime, utcClock) ?: 0f
                launch {
                    _hourlyChartState[index].value.sliderThumbPosition.snapTo(
                        ((nowX - initialXAxisStart) / (initialXAxisEnd - initialXAxisStart))
                    )
                    _historicalHourlyChartState[index].value.sliderThumbPosition.snapTo(0.5f)
                }
            }

            allLocations.forEachIndexed { index, _ ->
                launch {
                    val chartState = _dailyChartState[index].value
                    chartState.sliderThumbPosition.snapTo(
                        ((0f - chartState.initialDailyXAxisStart) /
                                (chartState.initialDailyXAxisEnd - chartState.initialDailyXAxisStart))
                    )
                }
                launch {
                    _historicalDailyChartState[index].value.sliderThumbPosition.snapTo(
                        0.5f
                    )
                }
            }
        }
    }

    fun addChartQuantity(quantity: HourlyWeatherQuantity, pageIndex: Int, isHistoric: Boolean){
        val chartState = if (isHistoric) _historicalHourlyChartState else _hourlyChartState
        val maxQuantity = if (isHistoric) 2 else 4
        if (!chartState[pageIndex].value.chartQuantities.contains(quantity) &&
            chartState[pageIndex].value.chartQuantities.size < maxQuantity) {
            chartState[pageIndex].update { chartState ->
                chartState.copy(
                    chartQuantities = chartState.chartQuantities + quantity,
                    curveValueAtIndicator = chartState.curveValueAtIndicator + null
                )
            }
//            if (!isHistoric)
//                _hourlyChartCurveValue[pageIndex].update { valueList ->
//                    valueList + null
//                }
        }
    }

    fun removeChartQuantity(quantity: HourlyWeatherQuantity, pageIndex: Int, isHistoric: Boolean){
        val chartState = if (isHistoric) _historicalHourlyChartState else _hourlyChartState
        val curveIndex = chartState[pageIndex].value.chartQuantities.indexOf(quantity)
        if (curveIndex >= 0) {
            chartState[pageIndex].update { chartState ->
                chartState.copy(
                    chartQuantities = chartState.chartQuantities - quantity
                )
            }
            removeYAxis(curveIndex, pageIndex, isHistoric = isHistoric)
        }
    }

    private fun dailyQuantityData(
        dailyData: List<LocalDailyWeather> = emptyList(),
        historicDailyData: List<LocalHistoricDailyWeather?> = emptyList(),
        weatherQuantity: DailyWeatherQuantity
    ): List<Float?> {
        return when (weatherQuantity){
            DailyWeatherQuantity.UVINDEXMAX -> dailyData.map { it.uvIndexMax }
            DailyWeatherQuantity.PRECIPITATIONSUM -> dailyData.map { it.precipitationSum }
                .takeIf { it.isNotEmpty() } ?: historicDailyData.map { it?.precipitationSum }
            DailyWeatherQuantity.PRECIPITATIONPROBABILITYMAX -> dailyData.map { it.precipitationProbabilityMax }
            DailyWeatherQuantity.WINDSPEEDMAX -> dailyData.map { it.windSpeedMax }
                .takeIf { it.isNotEmpty() } ?: historicDailyData.map { it?.windSpeedMax }

            DailyWeatherQuantity.TEMPERATUREMEAN -> historicDailyData.map { it?.temperatureMean }
            DailyWeatherQuantity.CLOUDCOVERMEAN -> historicDailyData.map { it?.cloudCoverMean }
            DailyWeatherQuantity.HUMIDITYMEAN -> historicDailyData.map { it?.relativeHumidityMean }
            else -> emptyList()
        }
    }

    private fun dailyQuantityMinMaxModifier(
        weatherQuantity: DailyWeatherQuantity,
        quantityMin: Float,
        quantityMax: Float,
    ): Pair<Float, Float> {
        val rangeTenPercentage = (quantityMax - quantityMin) / 10
        return when (weatherQuantity) {
            DailyWeatherQuantity.TEMPERATUREMINMAX -> {
                val coefficient = if (settingsUnits.temperatureUnit == Temperature.FAHRENHEIT) (9/5f) else 1f
                if (quantityMax - quantityMin < 5 * coefficient)
                    quantityMin - 5 * coefficient to quantityMax + 5 * coefficient
                else
                    quantityMin - rangeTenPercentage to quantityMax + rangeTenPercentage
            }
            DailyWeatherQuantity.TEMPERATUREMEAN -> {
                val coefficient = if (settingsUnits.temperatureUnit == Temperature.FAHRENHEIT) (9/5f) else 1f
                if (quantityMax - quantityMin < 5 * coefficient)
                    (quantityMin - 5 * coefficient).coerceAtMost(-coefficient) to
                            (quantityMax + 5 * coefficient).coerceAtLeast(coefficient)
                else
                    (quantityMin - rangeTenPercentage).coerceAtMost(-coefficient) to
                            (quantityMax + rangeTenPercentage).coerceAtLeast(coefficient)
            }
            DailyWeatherQuantity.SUNRISESET -> quantityMin to quantityMax
            DailyWeatherQuantity.UVINDEXMAX -> {
                -0.5f to 11f
            }
            DailyWeatherQuantity.PRECIPITATIONSUM -> {
                -(rangeTenPercentage.coerceAtLeast(0.2f.mmToUnit(settingsUnits.lengthUnit))) to
                        (quantityMax + rangeTenPercentage).coerceAtLeast(1f.mmToUnit(settingsUnits.lengthUnit))
            }
            DailyWeatherQuantity.PRECIPITATIONPROBABILITYMAX, DailyWeatherQuantity.HUMIDITYMEAN,
            DailyWeatherQuantity.CLOUDCOVERMEAN -> {
                -10f to 120f
            }
            DailyWeatherQuantity.WINDSPEEDMAX -> {
                -2f to quantityMax + rangeTenPercentage
            }
        }
    }

    fun updateDailyChartQuantity(quantity: DailyWeatherQuantity, pageIndex: Int, isHistoric: Boolean){
        val state = if (isHistoric) _historicalDailyChartState else _dailyChartState
        state.getOrNull(pageIndex)?.update { chartState ->
            chartState.copy(
                chartQuantity = quantity,
                curveAnimator = Animatable(initialValue = 0f)
            )
        }
//        updateDailyYAxis(quantity, pageIndex)
    }

    fun adjustSettings(){
        viewModelScope.launch {
            val appSettings = preferenceRepository.getSettings()
            updateHourlyChartWeatherIconVisibility(appSettings.hourlyDiagramWeatherConditionIconVisible)
            updateDailyChartWeatherIconVisibility(appSettings.dailyDiagramWeatherConditionIconVisible)
            updateHourlyChartDotsOnCurveVisibility(appSettings.hourlyDotsOnCurveVisible)
            updateHourlyChartCurveShadowVisibility(appSettings.hourlyCurveShadowVisible)
            updateHourlyChartGrid(appSettings.hourlyChartGrid)
            updateHourlyChartSunRiseSetIconsVisibility(appSettings.hourlySunRiseSetIconsVisible)
            updateHourlyChartTheme(appSettings.hourlyChartTheme)
            _hourlyChartState.forEach {
                it.update { chartState ->
                    chartState.copy(
                        curvesColor = appSettings.curvesColor,
                        curvesSmoothness = appSettings.curvesSmoothness
                    )
                }
            }
            _historicalHourlyChartState.forEach {
                it.update { chartState ->
                    chartState.copy(curvesColor = appSettings.curvesColor)
                }
            }
        }
    }

    private fun copyYAxis(pageIndex: Int, axisIndex: Int, isHistoric: Boolean){
        val chartState = if (isHistoric) _historicalHourlyChartState else _hourlyChartState
        chartState[pageIndex].update { chartState ->
            chartState.copy(
                curveAnimator = chartState.curveAnimator + Animatable(initialValue = 0f),
                yAxesStarts = chartState.yAxesStarts + Animatable(
                    initialValue = chartState.yAxesStarts[axisIndex].value
                ),
                yAxesEnds = chartState.yAxesEnds + Animatable(
                    initialValue = chartState.yAxesEnds[axisIndex].value
                )
            )
        }
        if (isHistoric) {
            initialHistoricYAxesStarts[pageIndex].add(initialHistoricYAxesStarts[pageIndex][axisIndex])
            initialHistoricYAxesEnds[pageIndex].add(initialHistoricYAxesEnds[pageIndex][axisIndex])
        } else {
            initialYAxesStarts[pageIndex].add(initialYAxesStarts[pageIndex][axisIndex])
            initialYAxesEnds[pageIndex].add(initialYAxesEnds[pageIndex][axisIndex])
        }
    }

    // Not tested!
    fun addYAxis(
        start: Float,
        end: Float,
        pageIndex: Int,
        diagramHeight: Float,
        textMeasurer: TextMeasurer,
        axisIndex: Int? = null,
        curveAnimatorInitialValue: Float = 0f,
        isHistoric: Boolean
    ){
        if (axisIndex != null){
            copyYAxis(pageIndex, axisIndex, isHistoric)
            return
        }
        var newStart: Float
        val newEnd: Float
        val newStartAfterTransformation: Float
        val newEndAfterTransformation: Float

        if ((_hourlyChartState[pageIndex].value.yAxesStarts.isEmpty() && !isHistoric) ||
            (_historicalHourlyChartState[pageIndex].value.yAxesStarts.isEmpty() && isHistoric)) {
            newStart = start
            newStartAfterTransformation = start
            newEnd = end
            newEndAfterTransformation = end
        } else {
            val y0Start = if (isHistoric) initialHistoricYAxesStarts[pageIndex][0]
            else initialYAxesStarts[pageIndex][0]
            val y0End = if (isHistoric) initialHistoricYAxesEnds[pageIndex][0]
            else initialYAxesEnds[pageIndex][0]

            val y0Length = y0End - y0Start
            val majorHorizontalBarSeparation =
                calculateHorizontalBarSeparation(y0Start, y0End, diagramHeight, textMeasurer).first
            var y = (y0Start / majorHorizontalBarSeparation).toInt() * majorHorizontalBarSeparation
            while (y <= y0Start) y += majorHorizontalBarSeparation
            val extraSpaceBelowFirstBar = y - y0Start

            val horizontalBarCount = y0Length / majorHorizontalBarSeparation
            val yAxisLength = end - start
            val majorBarSeparation = (yAxisLength / horizontalBarCount).verticalAxisCustomRound(false)
            val newYAxisLength = horizontalBarCount * majorBarSeparation

            y = (start / majorBarSeparation).roundToInt() * majorBarSeparation
            newStart = y - (extraSpaceBelowFirstBar / majorHorizontalBarSeparation) * majorBarSeparation
            if (newStart > start){
                y = (start / majorBarSeparation).toInt() * majorBarSeparation
                newStart = y - (extraSpaceBelowFirstBar / majorHorizontalBarSeparation) * majorBarSeparation
            }

            newEnd = newStart + newYAxisLength
            val y1Start = if (isHistoric) _historicalHourlyChartState[pageIndex].value.yAxesStarts[0].value
            else _hourlyChartState[pageIndex].value.yAxesStarts[0].value
            val y1End = if (isHistoric) _historicalHourlyChartState[pageIndex].value.yAxesEnds[0].value
            else _hourlyChartState[pageIndex].value.yAxesEnds[0].value
            val scale = (y1End - y1Start) / y0Length

            if (scale.isEqualTo(1f)){
                val yMovement = if (isHistoric) _historicalHourlyChartState[pageIndex].value.yAxesStarts[0].value -
                        initialHistoricYAxesStarts[pageIndex][0]
                else _hourlyChartState[pageIndex].value.yAxesStarts[0].value - initialYAxesStarts[pageIndex][0]
                newStartAfterTransformation = newStart + yMovement * newYAxisLength/y0Length
                newEndAfterTransformation = newEnd + yMovement * newYAxisLength/y0Length
            } else {
                val axis0Center = (y1Start - scale * y0Start) / (1 - scale)
                val newAxisCenter = newStart + (axis0Center - y0Start) * newYAxisLength/y0Length
                newStartAfterTransformation = (1 - scale) * newAxisCenter + scale * newStart
                newEndAfterTransformation = (1 - scale) * newAxisCenter + scale * newEnd
            }
        }
        if (isHistoric) {
            _historicalHourlyChartState[pageIndex].update { chartState ->
                chartState.copy(
                    curveAnimator = chartState.curveAnimator + Animatable(initialValue = curveAnimatorInitialValue),
                    yAxesEnds = chartState.yAxesEnds + Animatable(initialValue = newEndAfterTransformation),
                    yAxesStarts = chartState.yAxesStarts + Animatable(initialValue = newStartAfterTransformation),
                )
            }
            initialHistoricYAxesStarts[pageIndex].add(newStart)
            initialHistoricYAxesEnds[pageIndex].add(newEnd)
        } else {
            _hourlyChartState[pageIndex].update { chartState ->
                chartState.copy(
                    curveAnimator = chartState.curveAnimator + Animatable(initialValue = curveAnimatorInitialValue),
                    yAxesEnds = chartState.yAxesEnds + Animatable(initialValue = newEndAfterTransformation),
                    yAxesStarts = chartState.yAxesStarts + Animatable(initialValue = newStartAfterTransformation),
                )
            }
            initialYAxesStarts[pageIndex].add(newStart)
            initialYAxesEnds[pageIndex].add(newEnd)
        }
    }

    fun updateDailyYAxis(
        quantity: DailyWeatherQuantity,
        pageIndex: Int,
        isHistoric: Boolean,
        curveAnimatorInitialValue: Float = 0f,
    ){
        val quantityMin: Float?
        val quantityMax: Float?
        when (quantity){
            DailyWeatherQuantity.TEMPERATUREMINMAX -> {
                val dataMin = if (isHistoric)
                    historicalDailyChartData[pageIndex].value.map { it.dailyWeather?.temperatureMin }
                else
                    _locationsDailyChartData[pageIndex].value.map { it.temperatureMin }
                val dataMax = if (isHistoric)
                    historicalDailyChartData[pageIndex].value.map { it.dailyWeather?.temperatureMax }
                else
                    _locationsDailyChartData[pageIndex].value.map { it.temperatureMax }
                quantityMin = dataMin.minOfOrNull { it ?: Float.MAX_VALUE }
                quantityMax = dataMax.maxOfOrNull { it ?: -Float.MAX_VALUE }
            }
            DailyWeatherQuantity.SUNRISESET -> {
                quantityMin = -2f
                quantityMax = 26f
            }
            else -> {
                val data = dailyQuantityData(
                    dailyData = if (isHistoric) emptyList() else _locationsDailyChartData[pageIndex].value,
                    historicDailyData = historicalDailyChartData[pageIndex].value.map { it.dailyWeather },
                    weatherQuantity = quantity
                )
                quantityMax = data.maxOfOrNull { it ?: -Float.MAX_VALUE }
                quantityMin = data.minOfOrNull { it ?: Float.MAX_VALUE }
            }
        }
        if (quantityMin != null && quantityMax != null) {
            val (modifiedQuantityMin, modifiedQuantityMax) =
                dailyQuantityMinMaxModifier(quantity, quantityMin, quantityMax)
            if (isHistoric) {
                _historicalDailyChartState[pageIndex].update { chartState ->
                    chartState.copy(
                        yAxesEnds = Animatable(initialValue = modifiedQuantityMax),
                        yAxesStarts = Animatable(initialValue = modifiedQuantityMin),
                    )
                }
//                    _historicalDailyChartState[pageIndex].value.curveAnimator.snapTo(
//                        curveAnimatorInitialValue
//                    )
                initialHistoricDailyYAxesStarts[pageIndex] = modifiedQuantityMin
                initialHistoricDailyYAxesEnds[pageIndex] = modifiedQuantityMax
            } else {
                _dailyChartState[pageIndex].update { chartState ->
                    chartState.copy(
                        yAxesEnds = Animatable(initialValue = modifiedQuantityMax),
                        yAxesStarts = Animatable(initialValue = modifiedQuantityMin),
                    )
                }
//                    _dailyChartState[pageIndex].value.curveAnimator.snapTo(
//                        curveAnimatorInitialValue
//                    )
                initialDailyYAxesStarts[pageIndex] = modifiedQuantityMin
                initialDailyYAxesEnds[pageIndex] = modifiedQuantityMax
            }
        }
    }

    private fun removeYAxis(curveIndex: Int, pageIndex: Int, isHistoric: Boolean){
        val state = if (isHistoric) _historicalHourlyChartState else _hourlyChartState
        state.getOrNull(pageIndex)?.update { chartState ->
            chartState.copy(
                yAxesStarts = chartState.yAxesStarts.filterIndexed { index, _ ->
                    index != curveIndex
                },
                yAxesEnds = chartState.yAxesEnds.filterIndexed { index, _ ->
                    index != curveIndex
                },
                curveAnimator = chartState.curveAnimator.filterIndexed { index, _ ->
                    index != curveIndex
                },
                curveValueAtIndicator = chartState.curveValueAtIndicator.filterIndexed { index, _ ->
                    index != curveIndex
                }
            )
        }
//        if (!isHistoric)
//            _hourlyChartCurveValue.getOrNull(pageIndex)?.update { valueList ->
//                valueList.filterIndexed { index, _ -> index != curveIndex }
//            }

        runCatching {
            if (isHistoric) {
                initialHistoricYAxesStarts[pageIndex].removeAt(curveIndex)
                initialHistoricYAxesEnds[pageIndex].removeAt(curveIndex)
            } else {
                initialYAxesStarts[pageIndex].removeAt(curveIndex)
                initialYAxesEnds[pageIndex].removeAt(curveIndex)
            }
        }
    }

    fun calculateHorizontalBarSeparation(
        yStart: Float,
        yEnd: Float,
        diagramHeight: Float,
        textMeasurer: TextMeasurer
    ): Pair<Float, Float> {
        val horizontalBarRoughSeparation = textMeasurer.measure(
            text = "55.55",
            style = TextStyle(fontFamily = timeFontFamily)
        ).size.width * 2f
        val horizontalBarCount = (diagramHeight / horizontalBarRoughSeparation).toInt()
        val yAxisLength = yEnd - yStart
        if (yAxisLength == 0f) return 0f to 0f
        val majorHorizontalBarSeparation = (yAxisLength / horizontalBarCount).verticalAxisCustomRound()
        val minorHorizontalBarSeparation = majorHorizontalBarSeparation / 4f
        return majorHorizontalBarSeparation to minorHorizontalBarSeparation
    }
    private fun Float.verticalAxisCustomRound(roundDownward: Boolean = true): Float {
        val order = floor(log10(this))
        val approximateThis = 10f.pow(order)
        val a = this / approximateThis
        val b = if (roundDownward){
            when {
                a <= 1.5f -> 1
                a <= 3.5f -> 2
                a <= 8f -> 5
                else -> 10
            }
        } else {
            when {
                a <= 1f -> 1
                a <= 2f -> 2
                a <= 5f -> 5
                else -> 10
            }
        }
        return b * approximateThis
    }

    fun calculateVerticalBarSeparation(
        xStart: Float,
        xEnd: Float,
        diagramWidth: Float,
        textMeasurer: TextMeasurer
    ): Pair<Int, Float> {
        val verticalBarRoughSeparation = textMeasurer.measure(
            text = "FEB 28",
            style = TextStyle(fontFamily = timeFontFamily)
        ).size.width * 2.1f
        val verticalBarCount = (diagramWidth / verticalBarRoughSeparation).toInt()
        val xAxisLength = xEnd - xStart
        val majorVerticalBarSeparation = (xAxisLength/verticalBarCount).customRoundToInt()
        val minorVerticalBarSeparation = if (majorVerticalBarSeparation == 1 || majorVerticalBarSeparation > 24)
            majorVerticalBarSeparation / 4f else majorVerticalBarSeparation / 6f
        return majorVerticalBarSeparation to minorVerticalBarSeparation
    }
    private fun Float.customRoundToInt(): Int {
        if (this <= 1.5f) return 1
        return 2f.pow(log2(this / 3).roundToInt()).roundToInt() * 3
    }

    fun moveHourlyDiagramAxis(offset: Offset, pageIndex: Int, isHistoric: Boolean = false) {
        val chartState = if (isHistoric) _historicalHourlyChartState else _hourlyChartState
        viewModelScope.launch {
            val xAxisLength =
                chartState[pageIndex].value.let { it.xAxisEnd.value - it.xAxisStart.value }
            val xAxisMovement = offset.x * xAxisLength
            launch {
                /*chartState[pageIndex].update { chartState ->
                    chartState.copy(
                        awaker = chartState.awaker + 1
                    )
                }*/
                chartState[pageIndex].value.xAxisStart.snapTo(
                    chartState[pageIndex].value.xAxisStart.value - xAxisMovement
                )
                chartState[pageIndex].value.xAxisEnd.snapTo(
                    chartState[pageIndex].value.xAxisEnd.value - xAxisMovement
                )
            }

            val curveCount = chartState[pageIndex].value.yAxesStarts.size
            for (i in 0 until curveCount) {
                val yAxisLength =
                    chartState[pageIndex].value.let { it.yAxesEnds[i].value - it.yAxesStarts[i].value }
                val yAxisMovement = offset.y * yAxisLength
                launch {
                    chartState[pageIndex].value.yAxesStarts[i].snapTo(
                        chartState[pageIndex].value.yAxesStarts[i].value + yAxisMovement
                    )
                    chartState[pageIndex].value.yAxesEnds[i].snapTo(
                        chartState[pageIndex].value.yAxesEnds[i].value + yAxisMovement
                    )
                }
            }
        }
    }

    suspend fun animateHourlyDiagramAxis(date: LocalDate, pageIndex: Int) {
        val animationSpec = tween<Float>(
            durationMillis = 1000,
            easing = CubicBezierEasing(0.6f, 0.6f, 0.4f, 1f)
        )
        val oldXAxisCenter = _historicalHourlyChartState[pageIndex].value.let {
            it.xAxisEnd.value + it.xAxisStart.value
        } / 2
        val newXAxisCenter = Duration.between(LocalDate.now(utcClock).atStartOfDay(), date.atStartOfDay()).toHours()
        val xAxisLength = _historicalHourlyChartState[pageIndex].value.let {
            it.xAxisEnd.value - it.xAxisStart.value
        }
        withContext(mainDispatcher) {
            launch {
                _historicalHourlyChartState[pageIndex].value.xAxisStart.animateTo(
                    targetValue = newXAxisCenter - xAxisLength/2,
                    animationSpec = animationSpec,
                )
//                _historicalHourlyChartState[pageIndex].update { chartState ->
//                    chartState.copy(
//                        awaker = chartState.awaker + 1
//                    )
//                }
                if (abs(oldXAxisCenter - newXAxisCenter) > 240) {
                    clearYAxis(pageIndex, true)
                    updateXAxisAnimationTrigger(pageIndex, false)
                }
            }
            launch {
                _historicalHourlyChartState[pageIndex].value.xAxisEnd.animateTo(
                    targetValue = newXAxisCenter + xAxisLength/2,
                    animationSpec = animationSpec,
                )
            }
        }
    }

    suspend fun animateDailyDiagramAxis(date: LocalDate, pageIndex: Int) {
        val animationSpec = tween<Float>(
            durationMillis = 1000,
            easing = CubicBezierEasing(0.6f, 0.6f, 0.4f, 1f)
        )
        val oldXAxisCenter = _historicalDailyChartState[pageIndex].value.let {
            it.xAxisEnd.value + it.xAxisStart.value
        } / 2
        val newXAxisCenter = Duration.between(LocalDate.now().atStartOfDay(), date.atStartOfDay())
            .toDays().toFloat()
        val xAxisLength = _historicalDailyChartState[pageIndex].value.let {
            it.xAxisEnd.value - it.xAxisStart.value
        }
        withContext(mainDispatcher) {
            launch {
                _historicalDailyChartState[pageIndex].value.xAxisStart.animateTo(
                    targetValue = newXAxisCenter - xAxisLength/2,
                    animationSpec = animationSpec,
                )
//                _historicalDailyChartState[pageIndex].update { chartState ->
//                    chartState.copy(
//                        awaker = chartState.awaker + 1
//                    )
//                }
                if (abs(oldXAxisCenter - newXAxisCenter) > 20) {
                    updateDailyXAxisAnimationTrigger(pageIndex, false)
                }
            }
            launch {
                _historicalDailyChartState[pageIndex].value.xAxisEnd.animateTo(
                    targetValue = newXAxisCenter + xAxisLength/2,
                    animationSpec = animationSpec,
                )
            }
        }
    }

    fun moveDailyDiagramAxis(offset: Offset, pageIndex: Int, isHistoric: Boolean = false) {
        val chartState = if (isHistoric) _historicalDailyChartState else _dailyChartState
        viewModelScope.launch {
            val xAxisLength =
                chartState[pageIndex].value.let { it.xAxisEnd.value - it.xAxisStart.value }
            val xAxisMovement = offset.x * xAxisLength
            launch {
                chartState[pageIndex].value.xAxisStart.snapTo(
                    chartState[pageIndex].value.xAxisStart.value - xAxisMovement
                )
                chartState[pageIndex].value.xAxisEnd.snapTo(
                    chartState[pageIndex].value.xAxisEnd.value - xAxisMovement
                )
//                chartState[pageIndex].update { chartState ->
//                    chartState.copy(
//                        awaker = chartState.awaker + 1
//                    )
//                }
            }

            val yAxisLength =
                chartState[pageIndex].value.let { it.yAxesEnds.value - it.yAxesStarts.value }
            val yAxisMovement = offset.y * yAxisLength
            launch {
                chartState[pageIndex].value.yAxesStarts.snapTo(
                    chartState[pageIndex].value.yAxesStarts.value + yAxisMovement
                )
                chartState[pageIndex].value.yAxesEnds.snapTo(
                    chartState[pageIndex].value.yAxesEnds.value + yAxisMovement
                )
            }
        }
    }

    fun scaleHourlyDiagramAxis(
        center: Offset,
        scaleX: Float,
        scaleY: Float,
        pageIndex: Int,
        isHistoric: Boolean = false,
    ){
        val chartState = if (isHistoric) _historicalHourlyChartState else _hourlyChartState
        val initialXAxisLength = initialXAxisEnd - initialXAxisStart

        val xAxisLengthMin = initialXAxisLength / 20
        val xAxisLengthMax = initialXAxisLength * 9

        val xAxisLength = chartState[pageIndex].value.let { it.xAxisEnd.value - it.xAxisStart.value }
        val centerX = chartState[pageIndex].value.xAxisStart.value + center.x * xAxisLength
        val effectiveScaleX = when {
            (xAxisLength < xAxisLengthMin && scaleX > 1) ||
                    (xAxisLength > xAxisLengthMax && scaleX < 1) -> 1 + (scaleX - 1) / 5
            else -> scaleX
        }
        val xAxisStart = centerX - (centerX - chartState[pageIndex].value.xAxisStart.value)/effectiveScaleX
        val xAxisEnd = centerX + (chartState[pageIndex].value.xAxisEnd.value - centerX)/effectiveScaleX

        viewModelScope.launch {
            launch {
                chartState[pageIndex].value.xAxisStart.snapTo(xAxisStart)
                chartState[pageIndex].value.xAxisEnd.snapTo(xAxisEnd)
//                chartState[pageIndex].update { chartState ->
//                    chartState.copy(awaker = chartState.awaker + 1)
//                }
            }

            val curveCount = chartState[pageIndex].value.yAxesStarts.size
            for (i in 0 until curveCount) {
                val yAxisLength =
                    chartState[pageIndex].value.let { it.yAxesEnds[i].value - it.yAxesStarts[i].value }
                val centerY = chartState[pageIndex].value.yAxesEnds[i].value - center.y * yAxisLength
                val initialYAxesRange = if (isHistoric)
                    initialHistoricYAxesEnds[pageIndex][i] - initialHistoricYAxesStarts[pageIndex][i]
                else initialYAxesEnds[pageIndex][i] - initialYAxesStarts[pageIndex][i]
                val effectiveScaleY = when {
                    (yAxisLength < initialYAxesRange / 10 && scaleX > 1) ||
                            (yAxisLength > initialYAxesRange * 10 && scaleX < 1) -> 1 + (scaleY - 1) / 5

                    else -> scaleY
                }
                val yAxisStart =
                    centerY - (centerY - chartState[pageIndex].value.yAxesStarts[i].value) / effectiveScaleY
                val yAxisEnd =
                    centerY + (chartState[pageIndex].value.yAxesEnds[i].value - centerY) / effectiveScaleY
                launch {
                    chartState[pageIndex].value.yAxesStarts[i].snapTo(yAxisStart)
                    chartState[pageIndex].value.yAxesEnds[i].snapTo(yAxisEnd)
                }
            }
        }
    }

    fun scaleDailyDiagramAxis(
        center: Offset,
        scaleX: Float,
        scaleY: Float,
        pageIndex: Int,
        isHistoric: Boolean = false,
    ){
        val chartState = if (isHistoric) _historicalDailyChartState else _dailyChartState
        val initialXAxisLength = chartState[pageIndex].value.let {
            it.initialDailyXAxisEnd - it.initialDailyXAxisStart
        }
        val xAxisLengthMin = initialXAxisLength / 2
        val xAxisLengthMax = initialXAxisLength * 36

        val xAxisLength = chartState[pageIndex].value.let { it.xAxisEnd.value - it.xAxisStart.value }
        val centerX = chartState[pageIndex].value.xAxisStart.value + center.x * xAxisLength
        val effectiveScaleX = when {
            (xAxisLength < xAxisLengthMin && scaleX > 1) ||
                    (xAxisLength > xAxisLengthMax && scaleX < 1) -> 1 + (scaleX - 1) / 5
            else -> scaleX
        }
        val xAxisStart = centerX - (centerX - chartState[pageIndex].value.xAxisStart.value)/effectiveScaleX
        val xAxisEnd = centerX + (chartState[pageIndex].value.xAxisEnd.value - centerX)/effectiveScaleX

        val yAxisLength =
            chartState[pageIndex].value.let { it.yAxesEnds.value - it.yAxesStarts.value }
        val centerY = chartState[pageIndex].value.yAxesEnds.value - center.y * yAxisLength
        val initialYAxesRange = if (isHistoric)
            initialHistoricDailyYAxesEnds[pageIndex] - initialHistoricDailyYAxesStarts[pageIndex]
        else
            initialDailyYAxesEnds[pageIndex] - initialDailyYAxesStarts[pageIndex]
        val effectiveScaleY = when {
            (yAxisLength < initialYAxesRange / 20 && scaleX > 1) ||
                    (yAxisLength > initialYAxesRange * 20 && scaleX < 1) -> 1 + (scaleY - 1) / 5

            else -> scaleY
        }
        val yAxisStart =
            centerY - (centerY - chartState[pageIndex].value.yAxesStarts.value) / effectiveScaleY
        val yAxisEnd =
            centerY + (chartState[pageIndex].value.yAxesEnds.value - centerY) / effectiveScaleY

        viewModelScope.launch {
            launch {
                chartState[pageIndex].value.xAxisStart.snapTo(xAxisStart)
                chartState[pageIndex].value.xAxisEnd.snapTo(xAxisEnd)
//                chartState[pageIndex].update { chartState ->
//                    chartState.copy(
//                        awaker = chartState.awaker + 1
//                    )
//                }
            }
            launch {
                chartState[pageIndex].value.yAxesStarts.snapTo(yAxisStart)
                chartState[pageIndex].value.yAxesEnds.snapTo(yAxisEnd)
            }
        }
    }

    suspend fun scaleToNormalHourlyDiagramAxis(center: Offset, pageIndex: Int, isHistoric: Boolean){
        val chartState = if (isHistoric) _historicalHourlyChartState else _hourlyChartState
        val initialXAxisLength = initialXAxisEnd - initialXAxisStart

        val xAxisLengthMin = initialXAxisLength / 20
        val xAxisLengthMax = initialXAxisLength * 9
        withContext(context = mainDispatcher){
            launch {
                val xAxisLength = chartState[pageIndex].value.let { it.xAxisEnd.value - it.xAxisStart.value }
                val scaleX =
                    if (xAxisLength < xAxisLengthMin) xAxisLengthMin / xAxisLength
                    else if (xAxisLength > xAxisLengthMax) xAxisLengthMax / xAxisLength
                    else return@launch
                val centerX = chartState[pageIndex].value.xAxisStart.value + center.x * xAxisLength
                val xAxisStart = centerX - scaleX * (centerX - chartState[pageIndex].value.xAxisStart.value)
                val xAxisEnd = centerX + scaleX * (chartState[pageIndex].value.xAxisEnd.value - centerX)

                launch {
                    chartState[pageIndex].value.xAxisStart.animateTo(targetValue = xAxisStart)
                }
                launch {
                    chartState[pageIndex].value.xAxisEnd.animateTo(targetValue = xAxisEnd)
                }
            }

            val curveCount = chartState[pageIndex].value.yAxesStarts.size
            for (i in 0 until curveCount) {
                launch {
                    val yAxisLength =
                        chartState[pageIndex].value.let { it.yAxesEnds[i].value - it.yAxesStarts[i].value }
                    val initialYAxesRange = if (isHistoric)
                        initialHistoricYAxesEnds[pageIndex][i] - initialHistoricYAxesStarts[pageIndex][i]
                    else initialYAxesEnds[pageIndex][i] - initialYAxesStarts[pageIndex][i]
                    val scaleY =
                        if (yAxisLength < initialYAxesRange / 10) initialYAxesRange / (10 * yAxisLength)
                        else if (yAxisLength > 10 * initialYAxesRange) 10 * initialYAxesRange / yAxisLength
                        else return@launch
                    val centerY =
                        chartState[pageIndex].value.yAxesEnds[i].value - center.y * yAxisLength
                    val yAxisStart =
                        centerY - scaleY * (centerY - chartState[pageIndex].value.yAxesStarts[i].value)
                    val yAxisEnd =
                        centerY + scaleY * (chartState[pageIndex].value.yAxesEnds[i].value - centerY)

                    launch { chartState[pageIndex].value.yAxesStarts[i].animateTo(yAxisStart) }
                    launch { chartState[pageIndex].value.yAxesEnds[i].animateTo(yAxisEnd) }
                }
            }
        }
    }

    suspend fun scaleToNormalDailyDiagramAxis(center: Offset, pageIndex: Int, isHistoric: Boolean){
        val chartState = if (isHistoric) _historicalDailyChartState else _dailyChartState
        val initialXAxisLength = chartState[pageIndex].value.let {
            it.initialDailyXAxisEnd - it.initialDailyXAxisStart
        }
        val xAxisLengthMin = initialXAxisLength / 2
        val xAxisLengthMax = initialXAxisLength * 36
        withContext(context = mainDispatcher){
            launch {
                val xAxisLength = chartState[pageIndex].value.let { it.xAxisEnd.value - it.xAxisStart.value }
                val scaleX =
                    if (xAxisLength < xAxisLengthMin) xAxisLengthMin / xAxisLength
                    else if (xAxisLength > xAxisLengthMax) xAxisLengthMax / xAxisLength
                    else return@launch
                val centerX = chartState[pageIndex].value.xAxisStart.value + center.x * xAxisLength
                val xAxisStart = centerX - scaleX * (centerX - chartState[pageIndex].value.xAxisStart.value)
                val xAxisEnd = centerX + scaleX * (chartState[pageIndex].value.xAxisEnd.value - centerX)

                launch { chartState[pageIndex].value.xAxisStart.animateTo(xAxisStart) }
                launch { chartState[pageIndex].value.xAxisEnd.animateTo(xAxisEnd) }
            }

            launch {
                val yAxisLength =
                    chartState[pageIndex].value.let { it.yAxesEnds.value - it.yAxesStarts.value }
                val initialYAxesRange = if (isHistoric)
                    initialHistoricDailyYAxesEnds[pageIndex] - initialHistoricDailyYAxesStarts[pageIndex]
                else initialDailyYAxesEnds[pageIndex] - initialDailyYAxesStarts[pageIndex]
                val scaleY =
                    if (yAxisLength < initialYAxesRange / 20) initialYAxesRange / (20 * yAxisLength)
                    else if (yAxisLength > 20 * initialYAxesRange) 20 * initialYAxesRange / yAxisLength
                    else return@launch
                val centerY =
                    chartState[pageIndex].value.yAxesEnds.value - center.y * yAxisLength
                val yAxisStart =
                    centerY - scaleY * (centerY - chartState[pageIndex].value.yAxesStarts.value)
                val yAxisEnd =
                    centerY + scaleY * (chartState[pageIndex].value.yAxesEnds.value - centerY)

                launch { chartState[pageIndex].value.yAxesStarts.animateTo(yAxisStart) }
                launch { chartState[pageIndex].value.yAxesEnds.animateTo(yAxisEnd) }
            }
        }
    }

    suspend fun undoHourlyDiagramAxisMoveScale(pageIndex: Int){
        val animationSpec = tween<Float>(
            durationMillis = 1000,
            easing = CubicBezierEasing(0.6f, 0.6f, 0.4f, 1f)
        )
        withContext(context = defaultDispatcher){
            launch {
                _hourlyChartState[pageIndex].value.xAxisStart.animateTo(
                    targetValue = initialXAxisStart,
                    animationSpec = animationSpec,
//                    block = {
//                        _hourlyChartState[pageIndex].update { chartState ->
//                            chartState.copy(
//                                awaker = chartState.awaker + 1
//                            )
//                        }
//                    }
                )
            }
            launch {
                _hourlyChartState[pageIndex].value.xAxisEnd.animateTo(
                    targetValue = initialXAxisEnd,
                    animationSpec = animationSpec,
                )
            }

            val curveCount = _hourlyChartState[pageIndex].value.yAxesStarts.size
            for (i in 0 until curveCount) {
                launch {
                    _hourlyChartState[pageIndex].value.yAxesStarts[i].animateTo(
                        targetValue = initialYAxesStarts[pageIndex][i],
                        animationSpec = animationSpec
                    )
                }
                launch {
                    _hourlyChartState[pageIndex].value.yAxesEnds[i].animateTo(
                        targetValue = initialYAxesEnds[pageIndex][i],
                        animationSpec = animationSpec
                    )
                }
            }
            launch {
                animalizeHourlySliderThumbPositions(pageIndex, animationSpec)
            }
        }
    }
    private suspend fun animalizeHourlySliderThumbPositions(
        pageIndex: Int,
        animationSpec: AnimationSpec<Float>,
    ){
        val nowX = timeToX(
            LocalDateTime.now(utcClock).toString(),
            allLocations[pageIndex].utcOffset,
            preferenceRepository.getSettings().hourlyDiagramTime,
            utcClock
        ) ?: 0f
        _hourlyChartState[pageIndex].value.sliderThumbPosition.animateTo(
            targetValue = ((nowX - initialXAxisStart) / (initialXAxisEnd - initialXAxisStart)),
            animationSpec = animationSpec,
        )
    }

    suspend fun undoDailyDiagramAxisMoveScale(pageIndex: Int){
        val animationSpec = tween<Float>(
            durationMillis = 1000,
            easing = CubicBezierEasing(0.6f, 0.6f, 0.4f, 1f)
        )
        withContext(context = defaultDispatcher){
            launch {
                _dailyChartState[pageIndex].value.xAxisStart.animateTo(
                    targetValue = _dailyChartState[pageIndex].value.initialDailyXAxisStart,
                    animationSpec = animationSpec,
//                    block = {
//                        _dailyChartState[pageIndex].update { chartState ->
//                            chartState.copy(
//                                awaker = chartState.awaker + 1
//                            )
//                        }
//                    }
                )
            }
            launch {
                _dailyChartState[pageIndex].value.xAxisEnd.animateTo(
                    targetValue = _dailyChartState[pageIndex].value.initialDailyXAxisEnd,
                    animationSpec = animationSpec
                )
            }

            launch {
                _dailyChartState[pageIndex].value.yAxesStarts.animateTo(
                    targetValue = initialDailyYAxesStarts[pageIndex],
                    animationSpec = animationSpec
                )
            }
            launch {
                _dailyChartState[pageIndex].value.yAxesEnds.animateTo(
                    targetValue = initialDailyYAxesEnds[pageIndex],
                    animationSpec = animationSpec
                )
            }
            launch {
                animalizeDailySliderThumbPositions(pageIndex, animationSpec)
            }
        }
    }
    private suspend fun animalizeDailySliderThumbPositions(
        pageIndex: Int,
        animationSpec: AnimationSpec<Float>,
    ){
        val chartState = _dailyChartState[pageIndex].value
        chartState.sliderThumbPosition.animateTo(
            targetValue = ((0f - chartState.initialDailyXAxisStart) /
                    (chartState.initialDailyXAxisEnd - chartState.initialDailyXAxisStart)),
            animationSpec = animationSpec
        )
    }

    fun updateNavigateBack(state: Boolean, pageIndex: Int){
        _detailState.update { detailStateList ->
            detailStateList.toMutableList().apply{
                this[pageIndex] = this[pageIndex].copy(
                    navigateBack = state
                )
            }
        }
    }

    fun updateHourlyDiagramSettingOpen(open: Boolean, pageIndex: Int){
        _detailState.update { detailStateList ->
            detailStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    hourlyDiagramSettingOpen = open
                )
            }
        }
    }

    fun updateDailyDiagramSettingOpen(open: Boolean, pageIndex: Int){
        _detailState.update { detailStateList ->
            detailStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    dailyDiagramSettingOpen = open
                )
            }
        }
    }

    fun updateChooseDiagramThemeDialogVisibility(visible: Boolean, pageIndex: Int){
        _detailState.update { detailStateList ->
            detailStateList.toMutableList().apply{
                this[pageIndex] = this[pageIndex].copy(
                    chooseDiagramThemeDialogVisible = visible
                )
            }
        }
    }

    fun updateSelectedTabIndex(tab: DetailTabs, pageIndex: Int){
        _detailState.update { detailStateList ->
            detailStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    selectedTab = tab
                )
            }
        }
    }

    fun updateDetailPagerIndex(pageIndex: Int){
        _detailState.update { detailStateList ->
            detailStateList.map {
                it.copy(detailPagerIndex = pageIndex)
            }
        }
    }

    fun updateHourlyChartWeatherIconVisibility(visible : Boolean){
        viewModelScope.launch {
            preferenceRepository.setHourlyDiagramWeatherConditionIconVisibility(visible)
        }
        _hourlyChartState.forEach {
            it.update { chartState ->
                chartState.copy(weatherConditionIconsVisible = visible)
            }
        }
    }

    fun updateDailyChartWeatherIconVisibility(visible : Boolean){
        viewModelScope.launch {
            preferenceRepository.setDailyDiagramWeatherConditionIconVisibility(visible)
        }
        _dailyChartState.forEach {
            it.update { chartState ->
                chartState.copy(weatherConditionIconsVisible = visible)
            }
        }
    }

    fun updateHourlyChartDotsOnCurveVisibility(visible : Boolean){
        viewModelScope.launch {
            preferenceRepository.setHourlyDotsOnCurveVisibility(visible)
        }
        _hourlyChartState.forEach {
            it.update { chartState ->
                chartState.copy(dotsOnCurveVisible = visible)
            }
        }
    }

    fun updateHourlyChartCurveShadowVisibility(visible : Boolean){
        viewModelScope.launch {
            preferenceRepository.setHourlyCurveShadowVisibility(visible)
        }
        _hourlyChartState.forEach {
            it.update { chartState ->
                chartState.copy(curveShadowVisible = visible)
            }
        }
    }

    fun updateHourlyChartSunRiseSetIconsVisibility(visible : Boolean){
        viewModelScope.launch {
            preferenceRepository.setHourlySunRiseSetIconsVisibility(visible)
        }
        _hourlyChartState.forEach {
            it.update { chartState ->
                chartState.copy(sunRiseSetIconsVisible = visible)
            }
        }
    }

    fun updateHourlyChartGrid(gridType: ChartGrids){
        viewModelScope.launch {
            preferenceRepository.setHourlyChartGrid(gridType)
        }
        _hourlyChartState.forEach {
            it.update { chartState ->
                chartState.copy(chartGrid = gridType)
            }
        }
    }

    fun updateHourlyChartTheme(theme: ChartTheme){
        viewModelScope.launch {
            preferenceRepository.setHourlyChartTheme(theme)
        }
        _hourlyChartState.forEach {
            it.update { chartState ->
                chartState.copy(chartTheme = theme)
            }
        }
    }

    fun updateCurvesColor(
        @IntRange(from = 0, to = 3) curveNumber: Int,
        color: Int,
    ) {
//        viewModelScope.launch {
//            preferenceRepository.setCurvesColor(curveNumber, color)
//        }
        val newCurvesColor = _hourlyChartState.getOrNull(0)?.value?.curvesColor?.toMutableList()?.apply {
            set(curveNumber, color)
        }
        _hourlyChartState.forEach {
            it.update { chartState ->
                chartState.copy(curvesColor = newCurvesColor!!)
            }
        }
    }

    fun updateCurvesColor(colors: List<Color>) {
        val colorsInt = colors.map { it.toArgb() }
        viewModelScope.launch {
            preferenceRepository.setCurvesColor(colorsInt)
        }
        _hourlyChartState.forEach {
            it.update { chartState ->
                chartState.copy(curvesColor = colorsInt)
            }
        }
    }

    fun saveCurvesColor(
        @IntRange(from = 0, to = 3) curveNumber: Int,
        color: Color,
    ) {
        viewModelScope.launch {
            preferenceRepository.setCurvesColor(curveNumber, color.toArgb())
        }
    }

    fun setSliderThumbPosition(movement: Float, pageIndex: Int, isHistoric: Boolean){
        val state = if (isHistoric) _historicalHourlyChartState else _hourlyChartState
        val thumbPosition = state.getOrNull(pageIndex)?.value?.sliderThumbPosition
        viewModelScope.launch {
            thumbPosition?.snapTo(
                (thumbPosition.value + movement).coerceIn(0f, 1f)
            )
        }

//        state.getOrNull(pageIndex)?.update { chartState ->
//            chartState.copy(
//                sliderThumbPosition = (chartState.sliderThumbPosition + movement).coerceIn(0f, 1f)
//            )
//        }
    }

    fun setDailySliderThumbPosition(movement: Float, pageIndex: Int, isHistoric: Boolean){
        val state = if (isHistoric) _historicalDailyChartState else _dailyChartState
        val thumbPosition = state.getOrNull(pageIndex)?.value?.sliderThumbPosition
        viewModelScope.launch {
            thumbPosition?.snapTo(
                (thumbPosition.value + movement).coerceIn(0f, 1f)
            )
        }
//        state.getOrNull(pageIndex)?.update { chartState ->
//            chartState.copy(
//                sliderThumbPosition = (chartState.sliderThumbPosition + movement).coerceIn(0f, 1f)
//            )
//        }
    }

    fun updateCurveValueAtIndicator(curveIndex: Int, value: Float?, pageIndex: Int, isHistoric: Boolean){
        val state = if (isHistoric) _historicalHourlyChartState else _hourlyChartState
        state.getOrNull(pageIndex)?.update { chartState ->
            chartState.copy(
                curveValueAtIndicator = chartState.curveValueAtIndicator.toMutableList().apply {
                    if (curveIndex in this.indices)
                        this[curveIndex] = value
                }
            )
        }
//        if (!isHistoric)
//            _hourlyChartCurveValue.getOrNull(pageIndex)?.update { valueList ->
//                valueList.toMutableList().apply {
//                    if (curveIndex in this.indices)
//                        this[curveIndex] = value
//                }
//            }
    }

    /*fun updateDailyCurveValueAtIndicator(valueMin: Float, valueMax: Float, pageIndex: Int, isHistoric: Boolean){
        val state = if (isHistoric) _historicalDailyChartState else _dailyChartState
        state.getOrNull(pageIndex)?.update { chartState ->
            chartState.copy(
                curveValueMinAtIndicator = valueMin,
                curveValueMaxAtIndicator = valueMax
            )
        }
    }*/

    fun updateCurveSmoothness(
        hourlyWeatherQuantity: HourlyWeatherQuantity,
        @FloatRange(from = 0.0, to = 1.0) newSmoothness: Float,
    ){
        val newCurvesSmoothness = _hourlyChartState.getOrNull(0)?.value?.curvesSmoothness?.toMutableMap()?.apply {
            set(hourlyWeatherQuantity, newSmoothness)
        }
        _hourlyChartState.forEach {
            it.update { chartState ->
                chartState.copy(curvesSmoothness = newCurvesSmoothness!!)
            }
        }
    }

    fun saveCurveSmoothness(
        hourlyWeatherQuantity: HourlyWeatherQuantity,
        @FloatRange(from = 0.0, to = 1.0) newSmoothness: Float,
    ) {
        viewModelScope.launch {
            preferenceRepository.setCurvesSmoothness(hourlyWeatherQuantity, newSmoothness)
        }
    }

    fun swapLocations(id1: Int, id2: Int){
        _dataReadyForDisplay.value = false
        val index1 = allLocations.indexOfFirst { it.id == id1 }
        val index2 = allLocations.indexOfFirst { it.id == id2 }
        allLocations = allLocations.toMutableList().apply {
            swap(index1, index2)
        }
        initialYAxesStarts = initialYAxesStarts.toMutableList().apply {
            swap(index1, index2)
        }
        initialYAxesEnds = initialYAxesEnds.toMutableList().apply {
            swap(index1, index2)
        }
        initialDailyYAxesStarts = initialDailyYAxesStarts.apply {
            swap(index1, index2)
        }
        initialDailyYAxesEnds = initialDailyYAxesEnds.apply {
            swap(index1, index2)
        }
        initialHistoricYAxesStarts = initialHistoricYAxesStarts.toMutableList().apply {
            swap(index1, index2)
        }
        initialHistoricYAxesEnds = initialHistoricYAxesEnds.toMutableList().apply {
            swap(index1, index2)
        }
        initialHistoricDailyYAxesStarts = initialHistoricDailyYAxesStarts.apply {
            swap(index1, index2)
        }
        initialHistoricDailyYAxesEnds = initialHistoricDailyYAxesEnds.apply {
            swap(index1, index2)
        }

        _detailState.update {
            it.toMutableList().apply {
                swap(index1, index2)
            }
        }
        _hourlyChartState.swap(index1, index2)
//        _hourlyChartCurveValue.swap(index1, index2)
        _locationsHourlyChartData[index1] = hourlyChartDataFlow(index1)
        _locationsHourlyChartData[index2] = hourlyChartDataFlow(index2)

        _dailyChartState.swap(index1, index2)
        _locationsDailyChartData[index1] = dailyChartDataFlow(index1)
        _locationsDailyChartData[index2] = dailyChartDataFlow(index2)

        _historicalHourlyChartState.swap(index1, index2)
        historicalHourlyChartData[index1] = historicHourlyChartDataFlow(index1)
        historicalHourlyChartData[index2] = historicHourlyChartDataFlow(index2)

        _historicalDailyChartState.swap(index1, index2)
        viewModelScope.launch {
            delay(1000)
            _dataReadyForDisplay.value = true
        }
    }

    fun updateInitialXAxisBounds(detailScreenWidth: Dp, appSettings: AppSettings = AppSettings()){
        val hourlyXAxisLength = (detailScreenWidth / 120.dp).coerceAtMost(6f)
        val initialHourlyXAxisStart = -hourlyXAxisLength * 24 / 3
        val initialHourlyXAxisEnd = hourlyXAxisLength * 2 * 24 / 3

        val dailyXAxisLength = (detailScreenWidth / 50.dp).coerceAtMost(14f)
        val initialDailyXAxisStart = -(dailyXAxisLength / 4).toInt() - 0.5f
        val initialDailyXAxisEnd = (dailyXAxisLength * 3 / 4).toInt() + 0.5f

        val historicHourlyXAxisLength = (detailScreenWidth / 40.dp).coerceAtMost(20f)
        val middleXAxis = /*timeToX(
                    time = LocalDate.of(2000, 1, 1).atStartOfDay().toString(),
                    utcOffset = allLocations[index].utcOffset,
                    timeClock = appSettings.hourlyDiagramTime
                ) ?:*/ Duration.between(LocalDate.now().atStartOfDay(), LocalDate.of(2000, 1, 1).atStartOfDay())
            .toHours().toFloat()

        val historicDailyXAxisLength = (detailScreenWidth / 40.dp).coerceAtMost(20f)
        val middleDailyXAxis = Duration.between(LocalDate.now().atStartOfDay(),
            LocalDate.of(2000, 1, 1).atStartOfDay()).toDays().toFloat()

        initialXAxisStart = initialHourlyXAxisStart
        initialXAxisEnd = initialHourlyXAxisEnd

        _dailyChartState.forEach {
            it.update { chartState ->
                chartState.copy(
                    initialDailyXAxisStart = initialDailyXAxisStart,
                    initialDailyXAxisEnd = initialDailyXAxisEnd
                )
            }
        }
        historicInitialXAxisStart = middleXAxis - historicHourlyXAxisLength * 24 / 2
        historicInitialXAxisEnd = middleXAxis + historicHourlyXAxisLength * 24 / 2

        _historicalDailyChartState.forEachIndexed { index, stateFlow ->
            stateFlow.update { chartState ->
                chartState.copy(
                    initialDailyXAxisStart = middleDailyXAxis - (historicDailyXAxisLength / 2).roundToInt() - 0.5f,
                    initialDailyXAxisEnd = middleDailyXAxis + (historicDailyXAxisLength / 2).roundToInt() + 0.5f
                )
            }
        }

        initializeSliderThumbPositions()
    }

    fun initializeXAxisBounds(){
        allLocations.forEachIndexed { pageIndex, _ ->
            viewModelScope.launch {
                _hourlyChartState[pageIndex].update { chartState ->
                    chartState.apply {
                        xAxisStart.snapTo(initialXAxisStart)
                        xAxisEnd.snapTo(initialXAxisEnd)
                        yAxesStarts.forEachIndexed { index, animatable ->
                            animatable.snapTo(initialYAxesStarts[pageIndex][index])
                        }
                        yAxesEnds.forEachIndexed { index, animatable ->
                            animatable.snapTo(initialYAxesEnds[pageIndex][index])
                        }
                    }
//                    chartState.copy(
//                        awaker = chartState.awaker + 1
//                    )
                }
                _dailyChartState[pageIndex].update { chartState ->
                    chartState.apply {
                        xAxisStart.snapTo(chartState.initialDailyXAxisStart)
                        xAxisEnd.snapTo(chartState.initialDailyXAxisEnd)
                    }
//                    chartState.copy(
//                        awaker = chartState.awaker + 1
//                    )
                }

                _historicalHourlyChartState[pageIndex].update { chartState ->
                    chartState.apply {
                        xAxisStart.snapTo(historicInitialXAxisStart)
                        xAxisEnd.snapTo(historicInitialXAxisEnd)
                        yAxesStarts.forEachIndexed { index, animatable ->
                            animatable.snapTo(initialHistoricYAxesStarts[pageIndex][index])
                        }
                        yAxesEnds.forEachIndexed { index, animatable ->
                            animatable.snapTo(initialHistoricYAxesEnds[pageIndex][index])
                        }
                    }
//                    chartState.copy(
//                        awaker = chartState.awaker + 1
//                    )
                }
                _historicalDailyChartState[pageIndex].update { chartState ->
                    chartState.apply {
                        xAxisStart.snapTo(chartState.initialDailyXAxisStart)
                        xAxisEnd.snapTo(chartState.initialDailyXAxisEnd)
                    }
//                    chartState.copy(
//                        awaker = chartState.awaker + 1
//                    )
                }
            }
        }
    }

    fun reloadHistoricalData(pageIndex: Int){
        _historicalHourlyChartState[pageIndex].update {
            it.copy(awaker = it.awaker + 1)
        }
    }

    fun reloadHistoricalDailyData(pageIndex: Int){
        _historicalDailyChartState[pageIndex].update {
            it.copy(awaker = it.awaker + 1)
        }
    }

    private fun updateHourlySettingsStatus(status: Boolean){
        _hourlyChartState.forEach { chartState ->
            chartState.update {
                it.copy(settingsUpdated = status)
            }
        }
        _historicalHourlyChartState.forEach { chartState ->
            chartState.update {
                it.copy(settingsUpdated = status)
            }
        }
    }

    private fun updateDailySettingsStatus(status: Boolean){
        _dailyChartState.forEach { chartState ->
            chartState.update {
                it.copy(settingsUpdated = status)
            }
        }
        _historicalDailyChartState.forEach { chartState ->
            chartState.update {
                it.copy(settingsUpdated = status)
            }
        }
    }

    private fun updateDataLoadingStatus(pageIndex: Int, status: MyResult<*>){
        _historicalHourlyChartState[pageIndex].update {
            it.copy(dataLoadingStatus = status)
        }
    }

    private fun updateDailyDataLoadingStatus(pageIndex: Int, status: MyResult<*>){
        _historicalDailyChartState[pageIndex].update {
            it.copy(dataLoadingStatus = status)
        }
    }

    private fun updateXAxisAnimationTrigger(pageIndex: Int, status: Boolean){
        _historicalHourlyChartState[pageIndex].update {
            it.copy(xAxisAnimationTrigger = status)
        }
    }

    private fun updateDailyXAxisAnimationTrigger(pageIndex: Int, status: Boolean){
        _historicalDailyChartState[pageIndex].update {
            it.copy(xAxisAnimationTrigger = status)
        }
    }

    fun updateInitialLandScape(bitmap: Bitmap){
        initialLandScape = bitmap
    }
}