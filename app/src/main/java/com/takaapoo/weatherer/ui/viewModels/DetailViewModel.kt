package com.takaapoo.weatherer.ui.viewModels

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.domain.model.SettingsUnits
import com.takaapoo.weatherer.domain.model.toSettingsUnits
import com.takaapoo.weatherer.domain.repository.AirQualityRepository
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.LocationRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.domain.use_case.GetHourlyChartDataUseCase
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.daySeconds
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.timeToX
import com.takaapoo.weatherer.ui.theme.timeFontFamily
import com.takaapoo.weatherer.utility.mmToUnit
import com.takaapoo.weatherer.utility.swap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

const val EPSILON = 0.0001f

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val locationRepository: LocationRepository,
    private val localWeatherRepository: LocalWeatherRepository,
    private val airQualityRepository: AirQualityRepository,
    private val hourlyChartDataUseCase: GetHourlyChartDataUseCase,
    savedStateHandle: SavedStateHandle
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

    private val _detailState: MutableStateFlow<List<DetailState>> = MutableStateFlow(List(size = 0){ DetailState() })
    val detailState: StateFlow<List<DetailState>> = _detailState.asStateFlow()

//    private val _hourlyChartState: MutableStateFlow<List<HourlyChartState>> =
//        MutableStateFlow(List(size = 0){ HourlyChartState() })
//    val hourlyChartState: StateFlow<List<HourlyChartState>> = _hourlyChartState.asStateFlow()

    private var _hourlyChartState: MutableList<MutableStateFlow<HourlyChartState>> =
        MutableList(size = 0){ MutableStateFlow(HourlyChartState()) }
    val hourlyChartState: List<StateFlow<HourlyChartState>>
        get() = _hourlyChartState.map { it.asStateFlow() }

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


    private lateinit var initialYAxesStarts: List<MutableList<Float>>
    private lateinit var initialYAxesEnds: List<MutableList<Float>>
    private lateinit var initialDailyYAxesStarts: MutableList<Float>
    private lateinit var initialDailyYAxesEnds: MutableList<Float>
    private var initializedCompleted = false


    init {
        initialize()
    }

    private fun initialize(){
        viewModelScope.launch {
            allLocations = locationRepository.getAllLocationsList()
            _detailState.value = List(size = allLocations.size){ DetailState() }
            _hourlyChartState = MutableList(size = allLocations.size){ MutableStateFlow(HourlyChartState()) }
            _dailyChartState = MutableList(size = allLocations.size){ MutableStateFlow(DailyChartState()) }
            _locationsHourlyChartData = MutableList(size = allLocations.size){ i ->
                hourlyChartDataFlow(i)
            }
            _locationsDailyChartData = MutableList(size = allLocations.size){ i ->
                dailyChartDataFlow(i)
            }

            initialYAxesStarts = List(allLocations.size){ mutableListOf() }
            initialYAxesEnds = List(allLocations.size){ mutableListOf() }
            initialDailyYAxesStarts = MutableList(allLocations.size){ 0f }
            initialDailyYAxesEnds = MutableList(allLocations.size){ 0f }

            launch(Dispatchers.Default) {
                while (true) {
                    updateDetailState()
                    delay(60_000)
                }
            }
            launch(Dispatchers.Default) {
                while (true) {
                    if (allLocations.size != _detailState.value.size) continue
                    allLocations.forEachIndexed { index, location ->
                        _detailState.update { detailStateList ->
                            detailStateList.toMutableList().apply {
                                this[index] = this[index].copy(
                                    localDateTime = LocalDateTime.now(ZoneId.of("UTC"))
                                        .plusMinutes(location.utcOffset ?: 0)
                                )
                            }
                        }
                    }
                    delay(4_000)
                }
            }
            setSliderThumbPositions()

            launch(Dispatchers.Default) {
                settingsFlow.map { it.toSettingsUnits() }
                    .distinctUntilChanged()
                    .collectLatest {
                        settingsUnits = it
                        updateDetailState()
                        _hourlyChartState.forEachIndexed { index, _ ->
                            clearYAxis(index)
                        }
                        updateHourlySettingsStatus(false)
                        updateDailySettingsStatus(false)
                    }
            }
            // This is needed when refreshing weather data to update current weather in detail screen instantly
            launch(Dispatchers.Default) {
                allLocationsFlow.collectLatest {
                    updateDetailState()
                }
            }
            initializedCompleted = true
            adjustSettings()
        }
    }

    private fun hourlyChartDataFlow(index: Int) =
        combine(
            _hourlyChartState[index].map {
                it.xAxisStart.value to it.xAxisEnd.value
            },
            settingsFlow
        ){ startEndPairs, appSettings ->
            val extraDataLoadingDays = ceil(
                (startEndPairs.second - startEndPairs.first) / (1.5f * 24)
            ).toLong()

            val zone = when (appSettings.hourlyDiagramTime) {
                HourlyDiagramTime.UTC -> ZoneId.of("UTC")
                HourlyDiagramTime.DEVICE -> ZoneId.systemDefault()
                HourlyDiagramTime.LOCATION -> {
                    ZoneOffset.ofTotalSeconds((allLocations[index].utcOffset?.toInt() ?: 0) * 60)
                }
            }
            val startDate = LocalDate.now(zone).plusDays(
                floor(startEndPairs.first / 24).toLong() - extraDataLoadingDays
            ).toString()
            val endDate = LocalDate.now(zone).plusDays(
                ceil(startEndPairs.second / 24).toLong() + extraDataLoadingDays
            ).toString()
            updateHourlySettingsStatus(appSettings.toSettingsUnits() == settingsUnits)

            hourlyChartDataUseCase(
                locationId = allLocations[index].id,
                startDate = startDate,
                endDate = endDate,
                appSettings = appSettings
            )
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun dailyChartDataFlow(index: Int) = combine(
        _dailyChartState[index].map {
            it.xAxisStart.value to it.xAxisEnd.value
        },
        settingsFlow
    ) { startEndPairs, appSettings ->
        val extraDataLoadingDays = ceil((startEndPairs.second - startEndPairs.first) / 1.5f).toLong()
        val startDate = LocalDate.now().plusDays(
            (startEndPairs.first).toLong() - extraDataLoadingDays
        ).toString()
        val endDate = LocalDate.now().plusDays(
            (startEndPairs.second).toLong() + extraDataLoadingDays
        ).toString()
        updateDailySettingsStatus(appSettings.toSettingsUnits() == settingsUnits)

        localWeatherRepository.getLocationDailyWeather(
            locationId = allLocations[index].id,
            startDate = startDate,
            endDate = endDate,
            appSettings = appSettings
        )
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun clearYAxis(index: Int){
        _hourlyChartState[index].update {
            it.copy(
                yAxesStarts = emptyList(),
                yAxesEnds = emptyList(),
                curveAnimator = emptyList()
            )
        }
        initialYAxesStarts[index].clear()
        initialYAxesEnds[index].clear()
    }

    fun reInitialize(){
//        var deletedLocationIndex: Int?
        viewModelScope.launch {
            while (!initializedCompleted)
                delay(20)

            val newAllLocations = locationRepository.getAllLocationsList()
            if (newAllLocations.size <= allLocations.size) return@launch
            val addedLocationsCount = newAllLocations.size - allLocations.size
//            if(addedLocationsCount < 0) {
//                deletedLocationIndex = allLocations.indexOfFirst { !newAllLocations.contains(it) }
//                deletedLocationIndex.let { index ->
//                    _detailState.update {
//                        it - it[index]
//                    }
//                    _hourlyChartState.removeAt(index)
//                    _locationsHourlyChartData.removeAt(index)
//                    _dailyChartState.update {
//                        it - it[index]
//                    }
//                    initialYAxesStarts = initialYAxesStarts.apply {
//                        this.toMutableList().removeAt(index)
//                    }
//                    initialYAxesEnds = initialYAxesEnds.apply {
//                        this.toMutableList().removeAt(index)
//                    }
//                    initialDailyYAxesStarts.removeAt(index)
//                    initialDailyYAxesEnds.removeAt(index)
//                }
//                allLocations = newAllLocations
//            } else {
            _detailState.update {
                it + List(size = addedLocationsCount) { DetailState() }
            }
            _hourlyChartState.addAll(
                List(size = addedLocationsCount) { MutableStateFlow(HourlyChartState()) }
            )
            _dailyChartState.addAll(
                List(size = addedLocationsCount) { MutableStateFlow(DailyChartState()) }
            )
            initialYAxesStarts = initialYAxesStarts + List(addedLocationsCount) { mutableListOf() }
            initialYAxesEnds = initialYAxesEnds + List(addedLocationsCount) { mutableListOf() }
            initialDailyYAxesStarts.addAll(List(addedLocationsCount) { 0f })
            initialDailyYAxesEnds.addAll(List(addedLocationsCount) { 0f })
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
            updateDetailState()
//            }
        }
    }

    fun prepareDeleteLocation(deletedLocationId: Int){
        var deletedLocationIndex: Int
        viewModelScope.launch {
            deletedLocationIndex = allLocations.indexOfFirst { it.id == deletedLocationId }
            if (deletedLocationIndex == -1) return@launch

            _detailState.update {
                it - it[deletedLocationIndex]
            }
            _hourlyChartState.removeAt(deletedLocationIndex)
            _dailyChartState.removeAt(deletedLocationIndex)
            initialYAxesStarts = initialYAxesStarts.apply {
                this.toMutableList().removeAt(deletedLocationIndex)
            }
            initialYAxesEnds = initialYAxesEnds.apply {
                this.toMutableList().removeAt(deletedLocationIndex)
            }
            initialDailyYAxesStarts.removeAt(deletedLocationIndex)
            initialDailyYAxesEnds.removeAt(deletedLocationIndex)

            allLocations = allLocations.toMutableList().apply { removeAt(deletedLocationIndex) }
            _locationsHourlyChartData = MutableList(size = allLocations.size){ i ->
                hourlyChartDataFlow(i)
            }
            _locationsDailyChartData = MutableList(size = allLocations.size){ i ->
                dailyChartDataFlow(i)
            }
        }
    }


    private suspend fun updateDetailState(){
        val utcCurrentTime = LocalDateTime.now(ZoneId.of("UTC"))
        val utcCurrentDate = utcCurrentTime.toLocalDate()
        val targetX = (utcCurrentTime.toLocalTime().toSecondOfDay() / daySeconds) * 24
        if (allLocations.size != _detailState.value.size) return
        coroutineScope {
            val appSettings = preferenceRepository.getSettings()
            allLocations.forEachIndexed { index, location ->
                launch {
                    val hourlyWeather = localWeatherRepository.getLocationHourlyWeather(
                        locationId = location.id,
                        startDate = utcCurrentDate.toString(),
                        endDate = utcCurrentDate.plusDays(1).toString(),
                        appSettings = appSettings
                    )
                    val hourlyAirQuality = airQualityRepository.getLocationAirQuality(
                        locationId = location.id,
                        startDate = utcCurrentDate.toString(),
                        endDate = utcCurrentDate.plusDays(1).toString()
                    )
                    val localDate = utcCurrentTime
                        .plusMinutes(location.utcOffset ?: 0)
                        .toLocalDate().toString()
                    val dailyWeather = localWeatherRepository.getLocationDailyWeather(
                        locationId = location.id,
                        startDate = localDate,
                        endDate = localDate,
                        appSettings = appSettings
                    )
                    _detailState.update { detailStateList ->
                        detailStateList.toMutableList().apply {
                            this[index] = this[index].copy(
                                utcOffset = location.utcOffset,
                                maxTemperature = dailyWeather.getOrNull(0)?.temperatureMax,
                                minTemperature = dailyWeather.getOrNull(0)?.temperatureMin,
                                sunRise = dailyWeather.getOrNull(0)?.sunRise,
                                sunSet = dailyWeather.getOrNull(0)?.sunSet,
                                currentDayHourlyWeather = hourlyWeather,
                                currentDayHourlyAirQuality = hourlyAirQuality,
                                targetX = targetX,
                                localDateTime = LocalDateTime.now(ZoneId.of("UTC"))
                                    .plusMinutes(location.utcOffset ?: 0)
                            )
                        }
                    }
                }
            }
        }
    }


    fun setSliderThumbPositions(){
        val utcNow = LocalDateTime.now(ZoneId.of("UTC")).toString()
        viewModelScope.launch {
            val appSettings = preferenceRepository.getSettings()
            allLocations.forEachIndexed { index, location ->
                val nowX = timeToX(utcNow, location.utcOffset, appSettings.hourlyDiagramTime) ?: 0f
                _hourlyChartState[index].update { chartState ->
                    chartState.copy(
                        sliderThumbPosition = ((nowX - chartState.initialXAxisStart) /
                                (chartState.initialXAxisEnd - chartState.initialXAxisStart))
                    )
                }
            }
        }

        allLocations.forEachIndexed { index, location ->
            _dailyChartState[index].update { chartState ->
                chartState.copy(
                    sliderThumbPosition = ((0f - chartState.initialDailyXAxisStart) /
                            (chartState.initialDailyXAxisEnd - chartState.initialDailyXAxisStart))
                )
            }
        }
    }

    fun addChartQuantity(quantity: WeatherQuantity, pageIndex: Int){
        if (!_hourlyChartState[pageIndex].value.chartQuantities.contains(quantity) &&
            _hourlyChartState[pageIndex].value.chartQuantities.size < 4) {
            _hourlyChartState[pageIndex].update { chartState ->
                chartState.copy(
                    chartQuantities = chartState.chartQuantities + quantity,
                    curveValueAtIndicator = chartState.curveValueAtIndicator + 0f
                )
            }
        }
    }

    fun removeChartQuantity(quantity: WeatherQuantity, pageIndex: Int){
        val curveIndex = _hourlyChartState[pageIndex].value.chartQuantities.indexOf(quantity)
        if (curveIndex >= 0) {
            _hourlyChartState[pageIndex].update { chartState ->
                chartState.copy(
                    chartQuantities = chartState.chartQuantities - quantity
                )
            }
            removeYAxis(curveIndex, pageIndex)
        }
    }

    private fun dailyQuantityData(
        dailyData: List<LocalDailyWeather>,
        weatherQuantity: DailyWeatherQuantity
    ): List<Float?>{
        return when (weatherQuantity){
            DailyWeatherQuantity.TEMPERATUREMINMAX -> emptyList()
            DailyWeatherQuantity.SUNRISESET -> emptyList()
            DailyWeatherQuantity.UVINDEXMAX -> dailyData.map { it.uvIndexMax }
            DailyWeatherQuantity.PRECIPITATIONSUM -> dailyData.map { it.precipitationSum }
            DailyWeatherQuantity.PRECIPITATIONPROBABILITYMAX -> dailyData.map { it.precipitationProbabilityMax }
            DailyWeatherQuantity.WINDSPEEDMAX -> dailyData.map { it.windSpeedMax }
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
                val coefficient = if (settingsUnits.temperatureUnit == Temperature.FAHRENHEIT) (9/5) else 1
                if (quantityMax - quantityMin < 5 * coefficient)
                    quantityMin - 5 * coefficient to quantityMax + 5 * coefficient
                else
                    quantityMin - rangeTenPercentage to quantityMax + rangeTenPercentage
            }
            DailyWeatherQuantity.SUNRISESET -> quantityMin to quantityMax
            DailyWeatherQuantity.UVINDEXMAX -> {
                -0.5f to 11f
            }
            DailyWeatherQuantity.PRECIPITATIONSUM -> {
                -(rangeTenPercentage.coerceAtLeast(0.2f.mmToUnit(settingsUnits.lengthUnit))) to
                        (quantityMax + rangeTenPercentage).coerceAtLeast(1f.mmToUnit(settingsUnits.lengthUnit))
            }
            DailyWeatherQuantity.PRECIPITATIONPROBABILITYMAX -> {
                -(rangeTenPercentage.coerceAtLeast(0.5f)) to
                        (quantityMax + rangeTenPercentage).coerceAtLeast(5f)
            }
            DailyWeatherQuantity.WINDSPEEDMAX -> {
                -2f to quantityMax + rangeTenPercentage
            }
        }
    }

    fun updateDailyChartQuantity(quantity: DailyWeatherQuantity, pageIndex: Int){
        _dailyChartState[pageIndex].update { chartState ->
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
                    chartState.copy(curvesColor = appSettings.curvesColor)
                }
            }
            _hourlyChartState.forEach {
                it.update { chartState ->
                    chartState.copy(curvesSmoothness = appSettings.curvesSmoothness)
                }
            }
        }
    }

    private fun copyYAxis(pageIndex: Int, axisIndex: Int){
        _hourlyChartState[pageIndex].update { chartState ->
            chartState.copy(
                curveAnimator = chartState.curveAnimator + Animatable(initialValue = 0f),
                yAxesStarts = chartState.yAxesStarts + Animatable(
                    initialValue = chartState.yAxesStarts[axisIndex].value),
                yAxesEnds = chartState.yAxesEnds + Animatable(
                    initialValue = chartState.yAxesEnds[axisIndex].value)
            )
        }
        initialYAxesStarts[pageIndex].add(initialYAxesStarts[pageIndex][axisIndex])
        initialYAxesEnds[pageIndex].add(initialYAxesEnds[pageIndex][axisIndex])
    }

    fun addYAxis(
        start: Float,
        end: Float,
        pageIndex: Int,
        diagramHeight: Float,
        textMeasurer: TextMeasurer,
        axisIndex: Int? = null,
        curveAnimatorInitialValue: Float = 0f
    ){
        if (axisIndex != null){
            copyYAxis(pageIndex, axisIndex)
            return
        }
        var newStart: Float
        val newEnd: Float
        val newStartAfterTransformation: Float
        val newEndAfterTransformation: Float

        if (_hourlyChartState[pageIndex].value.yAxesStarts.isEmpty()) {
            newStart = start
            newStartAfterTransformation = start
            newEnd = end
            newEndAfterTransformation = end
        } else {
            val y0Start = initialYAxesStarts[pageIndex][0]
            val y0End = initialYAxesEnds[pageIndex][0]
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
            val y1Start = _hourlyChartState[pageIndex].value.yAxesStarts[0].value
            val y1End = _hourlyChartState[pageIndex].value.yAxesEnds[0].value
            val scale = (y1End - y1Start) / y0Length

            if (scale.isEqualTo(1f)){
                val yMovement = _hourlyChartState[pageIndex].value.yAxesStarts[0].value -
                        initialYAxesStarts[pageIndex][0]
                newStartAfterTransformation = newStart + yMovement * newYAxisLength/y0Length
                newEndAfterTransformation = newEnd + yMovement * newYAxisLength/y0Length
            } else {
                val axis0Center = (y1Start - scale * y0Start) / (1 - scale)
                val newAxisCenter = newStart + (axis0Center - y0Start) * newYAxisLength/y0Length
                newStartAfterTransformation = (1 - scale) * newAxisCenter + scale * newStart
                newEndAfterTransformation = (1 - scale) * newAxisCenter + scale * newEnd
            }
        }
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

    fun updateDailyYAxis(quantity: DailyWeatherQuantity, pageIndex: Int){
        val quantityMin: Float?
        val quantityMax: Float?
        when (quantity ){
            DailyWeatherQuantity.TEMPERATUREMINMAX -> {
                val dataMin = _locationsDailyChartData[pageIndex].value.map { it.temperatureMin }
                val dataMax = _locationsDailyChartData[pageIndex].value.map { it.temperatureMax }
                quantityMin = dataMin.minOfOrNull { it ?: Float.MAX_VALUE }
                quantityMax = dataMax.maxOfOrNull { it ?: -Float.MAX_VALUE }
            }
            DailyWeatherQuantity.SUNRISESET -> {
                quantityMin = -2f
                quantityMax = 26f
            }
            else -> {
                val data = dailyQuantityData(_locationsDailyChartData[pageIndex].value, quantity)
                quantityMax = data.maxOfOrNull { it ?: -Float.MAX_VALUE }
                quantityMin = data.minOfOrNull { it ?: Float.MAX_VALUE }
            }
        }
        if (quantityMin != null && quantityMax != null) {
            val (modifiedQuantityMin, modifiedQuantityMax) =
                dailyQuantityMinMaxModifier(quantity, quantityMin, quantityMax)
            _dailyChartState[pageIndex].update { chartState ->
                chartState.copy(
                    yAxesEnds = Animatable(initialValue = modifiedQuantityMax),
                    yAxesStarts = Animatable(initialValue = modifiedQuantityMin),
                )
            }
            initialDailyYAxesStarts[pageIndex] = modifiedQuantityMin
            initialDailyYAxesEnds[pageIndex] = modifiedQuantityMax
        }
    }

    private fun removeYAxis(curveIndex: Int, pageIndex: Int){
        _hourlyChartState[pageIndex].update { chartState ->
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
                curveValueAtIndicator =  chartState.curveValueAtIndicator.filterIndexed { index, _ ->
                    index != curveIndex
                }
            )
        }
        initialYAxesStarts[pageIndex].removeAt(curveIndex)
        initialYAxesEnds[pageIndex].removeAt(curveIndex)
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

    fun moveHourlyDiagramAxis(offset: Offset, pageIndex: Int) {
        viewModelScope.launch {
            val xAxisLength =
                _hourlyChartState[pageIndex].value.let { it.xAxisEnd.value - it.xAxisStart.value }
            val xAxisMovement = offset.x * xAxisLength
            launch {
                _hourlyChartState[pageIndex].update { chartState ->
                    chartState.copy(
                        awaker = chartState.awaker + 1
                    )
                }
                _hourlyChartState[pageIndex].value.xAxisStart.snapTo(
                    _hourlyChartState[pageIndex].value.xAxisStart.value - xAxisMovement
                )
                _hourlyChartState[pageIndex].value.xAxisEnd.snapTo(
                    _hourlyChartState[pageIndex].value.xAxisEnd.value - xAxisMovement
                )
            }

            val curveCount = _hourlyChartState[pageIndex].value.yAxesStarts.size
            for (i in 0 until curveCount) {
                val yAxisLength =
                    _hourlyChartState[pageIndex].value.let { it.yAxesEnds[i].value - it.yAxesStarts[i].value }
                val yAxisMovement = offset.y * yAxisLength
                launch {
                    _hourlyChartState[pageIndex].value.yAxesStarts[i].snapTo(
                        _hourlyChartState[pageIndex].value.yAxesStarts[i].value + yAxisMovement
                    )
                    _hourlyChartState[pageIndex].value.yAxesEnds[i].snapTo(
                        _hourlyChartState[pageIndex].value.yAxesEnds[i].value + yAxisMovement
                    )
                }
            }
        }
    }

    fun moveDailyDiagramAxis(offset: Offset, pageIndex: Int) {
        viewModelScope.launch {
            val xAxisLength =
                _dailyChartState[pageIndex].value.let { it.xAxisEnd.value - it.xAxisStart.value }
            val xAxisMovement = offset.x * xAxisLength
            launch {
                _dailyChartState[pageIndex].value.xAxisStart.snapTo(
                    _dailyChartState[pageIndex].value.xAxisStart.value - xAxisMovement
                )
                _dailyChartState[pageIndex].value.xAxisEnd.snapTo(
                    _dailyChartState[pageIndex].value.xAxisEnd.value - xAxisMovement
                )
                _dailyChartState[pageIndex].update { chartState ->
                    chartState.copy(
                        awaker = chartState.awaker + 1
                    )
                }
            }

            val yAxisLength =
                _dailyChartState[pageIndex].value.let { it.yAxesEnds.value - it.yAxesStarts.value }
            val yAxisMovement = offset.y * yAxisLength
            launch {
                _dailyChartState[pageIndex].value.yAxesStarts.snapTo(
                    _dailyChartState[pageIndex].value.yAxesStarts.value + yAxisMovement
                )
                _dailyChartState[pageIndex].value.yAxesEnds.snapTo(
                    _dailyChartState[pageIndex].value.yAxesEnds.value + yAxisMovement
                )
            }
        }
    }

    fun scaleHourlyDiagramAxis(center: Offset, scaleX: Float, scaleY: Float, pageIndex: Int){
        val initialXAxisLength = _hourlyChartState[pageIndex].value.let {
            it.initialXAxisEnd - it.initialXAxisStart
        }
        val xAxisLengthMin = initialXAxisLength / 20
        val xAxisLengthMax = initialXAxisLength * 9

        val xAxisLength = _hourlyChartState[pageIndex].value.let { it.xAxisEnd.value - it.xAxisStart.value }
        val centerX = _hourlyChartState[pageIndex].value.xAxisStart.value + center.x * xAxisLength
        val effectiveScaleX = when {
            (xAxisLength < xAxisLengthMin && scaleX > 1) ||
                    (xAxisLength > xAxisLengthMax && scaleX < 1) -> 1 + (scaleX - 1) / 5
            else -> scaleX
        }
        val xAxisStart = centerX - (centerX - _hourlyChartState[pageIndex].value.xAxisStart.value)/effectiveScaleX
        val xAxisEnd = centerX + (_hourlyChartState[pageIndex].value.xAxisEnd.value - centerX)/effectiveScaleX

        viewModelScope.launch {
            launch {
                _hourlyChartState[pageIndex].value.xAxisStart.snapTo(xAxisStart)
                _hourlyChartState[pageIndex].value.xAxisEnd.snapTo(xAxisEnd)
                _hourlyChartState[pageIndex].update { chartState ->
                    chartState.copy(awaker = chartState.awaker + 1)
                }
            }

            val curveCount = _hourlyChartState[pageIndex].value.yAxesStarts.size
            for (i in 0 until curveCount) {
                val yAxisLength =
                    _hourlyChartState[pageIndex].value.let { it.yAxesEnds[i].value - it.yAxesStarts[i].value }
                val centerY = _hourlyChartState[pageIndex].value.yAxesEnds[i].value - center.y * yAxisLength
                val initialYAxesRange = initialYAxesEnds[pageIndex][i] - initialYAxesStarts[pageIndex][i]
                val effectiveScaleY = when {
                    (yAxisLength < initialYAxesRange / 10 && scaleX > 1) ||
                            (yAxisLength > initialYAxesRange * 10 && scaleX < 1) -> 1 + (scaleY - 1) / 5

                    else -> scaleY
                }
                val yAxisStart =
                    centerY - (centerY - _hourlyChartState[pageIndex].value.yAxesStarts[i].value) / effectiveScaleY
                val yAxisEnd =
                    centerY + (_hourlyChartState[pageIndex].value.yAxesEnds[i].value - centerY) / effectiveScaleY
                launch {
                    _hourlyChartState[pageIndex].value.yAxesStarts[i].snapTo(yAxisStart)
                    _hourlyChartState[pageIndex].value.yAxesEnds[i].snapTo(yAxisEnd)
                }
            }
        }
    }

    fun scaleDailyDiagramAxis(center: Offset, scaleX: Float, scaleY: Float, pageIndex: Int){
        val initialXAxisLength = _dailyChartState[pageIndex].value.let {
            it.initialDailyXAxisEnd - it.initialDailyXAxisStart
        }
        val xAxisLengthMin = initialXAxisLength / 2
        val xAxisLengthMax = initialXAxisLength * 36

        val xAxisLength = _dailyChartState[pageIndex].value.let { it.xAxisEnd.value - it.xAxisStart.value }
        val centerX = _dailyChartState[pageIndex].value.xAxisStart.value + center.x * xAxisLength
        val effectiveScaleX = when {
            (xAxisLength < xAxisLengthMin && scaleX > 1) ||
                    (xAxisLength > xAxisLengthMax && scaleX < 1) -> 1 + (scaleX - 1) / 5
            else -> scaleX
        }
        val xAxisStart = centerX - (centerX - _dailyChartState[pageIndex].value.xAxisStart.value)/effectiveScaleX
        val xAxisEnd = centerX + (_dailyChartState[pageIndex].value.xAxisEnd.value - centerX)/effectiveScaleX

        val yAxisLength =
            _dailyChartState[pageIndex].value.let { it.yAxesEnds.value - it.yAxesStarts.value }
        val centerY = _dailyChartState[pageIndex].value.yAxesEnds.value - center.y * yAxisLength
        val initialYAxesRange = initialDailyYAxesEnds[pageIndex] - initialDailyYAxesStarts[pageIndex]
        val effectiveScaleY = when {
            (yAxisLength < initialYAxesRange / 20 && scaleX > 1) ||
                    (yAxisLength > initialYAxesRange * 20 && scaleX < 1) -> 1 + (scaleY - 1) / 5

            else -> scaleY
        }
        val yAxisStart =
            centerY - (centerY - _dailyChartState[pageIndex].value.yAxesStarts.value) / effectiveScaleY
        val yAxisEnd =
            centerY + (_dailyChartState[pageIndex].value.yAxesEnds.value - centerY) / effectiveScaleY

        viewModelScope.launch {
            launch {
                _dailyChartState[pageIndex].value.xAxisStart.snapTo(xAxisStart)
                _dailyChartState[pageIndex].value.xAxisEnd.snapTo(xAxisEnd)
                _dailyChartState[pageIndex].update { chartState ->
                    chartState.copy(
                        awaker = chartState.awaker + 1
                    )
                }
            }
            launch {
                _dailyChartState[pageIndex].value.yAxesStarts.snapTo(yAxisStart)
                _dailyChartState[pageIndex].value.yAxesEnds.snapTo(yAxisEnd)
            }
        }
    }

    suspend fun scaleToNormalHourlyDiagramAxis(center: Offset, pageIndex: Int){
        val initialXAxisLength = _hourlyChartState[pageIndex].value.let {
            it.initialXAxisEnd - it.initialXAxisStart
        }
        val xAxisLengthMin = initialXAxisLength / 20
        val xAxisLengthMax = initialXAxisLength * 9
        withContext(context = Dispatchers.Main){
            launch {
                val xAxisLength = _hourlyChartState[pageIndex].value.let { it.xAxisEnd.value - it.xAxisStart.value }
                val scaleX =
                    if (xAxisLength < xAxisLengthMin) xAxisLengthMin / xAxisLength
                    else if (xAxisLength > xAxisLengthMax) xAxisLengthMax / xAxisLength
                    else return@launch
                val centerX = _hourlyChartState[pageIndex].value.xAxisStart.value + center.x * xAxisLength
                val xAxisStart = centerX - scaleX * (centerX - _hourlyChartState[pageIndex].value.xAxisStart.value)
                val xAxisEnd = centerX + scaleX * (_hourlyChartState[pageIndex].value.xAxisEnd.value - centerX)

                launch {
                    _hourlyChartState[pageIndex].value.xAxisStart.animateTo(targetValue = xAxisStart)
                }
                launch {
                    _hourlyChartState[pageIndex].value.xAxisEnd.animateTo(targetValue = xAxisEnd)
                }
            }

            val curveCount = _hourlyChartState[pageIndex].value.yAxesStarts.size
            for (i in 0 until curveCount) {
                launch {
                    val yAxisLength =
                        _hourlyChartState[pageIndex].value.let { it.yAxesEnds[i].value - it.yAxesStarts[i].value }
                    val initialYAxesRange = initialYAxesEnds[pageIndex][i] - initialYAxesStarts[pageIndex][i]
                    val scaleY =
                        if (yAxisLength < initialYAxesRange / 10) initialYAxesRange / (10 * yAxisLength)
                        else if (yAxisLength > 10 * initialYAxesRange) 10 * initialYAxesRange / yAxisLength
                        else return@launch
                    val centerY =
                        _hourlyChartState[pageIndex].value.yAxesEnds[i].value - center.y * yAxisLength
                    val yAxisStart =
                        centerY - scaleY * (centerY - _hourlyChartState[pageIndex].value.yAxesStarts[i].value)
                    val yAxisEnd =
                        centerY + scaleY * (_hourlyChartState[pageIndex].value.yAxesEnds[i].value - centerY)

                    launch { _hourlyChartState[pageIndex].value.yAxesStarts[i].animateTo(yAxisStart) }
                    launch { _hourlyChartState[pageIndex].value.yAxesEnds[i].animateTo(yAxisEnd) }
                }
            }
        }
    }

    suspend fun scaleToNormalDailyDiagramAxis(center: Offset, pageIndex: Int){
        val initialXAxisLength = _dailyChartState[pageIndex].value.let {
            it.initialDailyXAxisEnd - it.initialDailyXAxisStart
        }
        val xAxisLengthMin = initialXAxisLength / 2
        val xAxisLengthMax = initialXAxisLength * 36
        withContext(context = Dispatchers.Main){
            launch {
                val xAxisLength = _dailyChartState[pageIndex].value.let { it.xAxisEnd.value - it.xAxisStart.value }
                val scaleX =
                    if (xAxisLength < xAxisLengthMin) xAxisLengthMin / xAxisLength
                    else if (xAxisLength > xAxisLengthMax) xAxisLengthMax / xAxisLength
                    else return@launch
                val centerX = _dailyChartState[pageIndex].value.xAxisStart.value + center.x * xAxisLength
                val xAxisStart = centerX - scaleX * (centerX - _dailyChartState[pageIndex].value.xAxisStart.value)
                val xAxisEnd = centerX + scaleX * (_dailyChartState[pageIndex].value.xAxisEnd.value - centerX)

                launch { _dailyChartState[pageIndex].value.xAxisStart.animateTo(xAxisStart) }
                launch { _dailyChartState[pageIndex].value.xAxisEnd.animateTo(xAxisEnd) }
            }

            launch {
                val yAxisLength =
                    _dailyChartState[pageIndex].value.let { it.yAxesEnds.value - it.yAxesStarts.value }
                val initialYAxesRange = initialDailyYAxesEnds[pageIndex] - initialDailyYAxesStarts[pageIndex]
                val scaleY =
                    if (yAxisLength < initialYAxesRange / 20) initialYAxesRange / (20 * yAxisLength)
                    else if (yAxisLength > 20 * initialYAxesRange) 20 * initialYAxesRange / yAxisLength
                    else return@launch
                val centerY =
                    _dailyChartState[pageIndex].value.yAxesEnds.value - center.y * yAxisLength
                val yAxisStart =
                    centerY - scaleY * (centerY - _dailyChartState[pageIndex].value.yAxesStarts.value)
                val yAxisEnd =
                    centerY + scaleY * (_dailyChartState[pageIndex].value.yAxesEnds.value - centerY)

                launch { _dailyChartState[pageIndex].value.yAxesStarts.animateTo(yAxisStart) }
                launch { _dailyChartState[pageIndex].value.yAxesEnds.animateTo(yAxisEnd) }
            }
        }
    }

    suspend fun undoHourlyDiagramAxisMoveScale(pageIndex: Int){
        val animationSpec = tween<Float>(
            durationMillis = 1000,
            easing = CubicBezierEasing(0.6f, 0.6f, 0.4f, 1f)
        )
        withContext(context = Dispatchers.Default){
            launch {
                _hourlyChartState[pageIndex].value.xAxisStart.animateTo(
                    targetValue = _hourlyChartState[pageIndex].value.initialXAxisStart,
                    animationSpec = animationSpec,
                    block = {
                        _hourlyChartState[pageIndex].update { chartState ->
                            chartState.copy(
                                awaker = chartState.awaker + 1
                            )
                        }
                    }
                )
            }
            launch {
                _hourlyChartState[pageIndex].value.xAxisEnd.animateTo(
                    targetValue = _hourlyChartState[pageIndex].value.initialXAxisEnd,
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
        }
        setSliderThumbPositions()
    }

    suspend fun undoDailyDiagramAxisMoveScale(pageIndex: Int){
        val animationSpec = tween<Float>(
            durationMillis = 1000,
            easing = CubicBezierEasing(0.6f, 0.6f, 0.4f, 1f)
        )
        withContext(context = Dispatchers.Default){
            launch {
                _dailyChartState[pageIndex].value.xAxisStart.animateTo(
                    targetValue = _dailyChartState[pageIndex].value.initialDailyXAxisStart,
                    animationSpec = animationSpec,
                    block = {
                        _dailyChartState[pageIndex].update { chartState ->
                            chartState.copy(
                                awaker = chartState.awaker + 1
                            )
                        }
                    }
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
        }
        setSliderThumbPositions()
    }


    private fun Float.isEqualTo(number: Float): Boolean {
        return abs(this - number) < EPSILON
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

    fun updateHourlyDiagramSettingRectangle(rect: Rect, pageIndex: Int){
        _detailState.update { detailStateList ->
            detailStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    hourlyDiagramSettingRectangle = rect
                )
            }
        }
    }

    fun updateDailyDiagramSettingRectangle(rect: Rect, pageIndex: Int){
        _detailState.update { detailStateList ->
            detailStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    dailyDiagramSettingRectangle = rect
                )
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

    fun updateSliderThumbPosition(movement: Float, pageIndex: Int){
        _hourlyChartState[pageIndex].update { chartState ->
            chartState.copy(
                sliderThumbPosition = (chartState.sliderThumbPosition + movement).coerceIn(0f, 1f)
            )
        }
    }

    fun updateDailySliderThumbPosition(movement: Float, pageIndex: Int){
        _dailyChartState[pageIndex].update { chartState ->
            chartState.copy(
                sliderThumbPosition = (chartState.sliderThumbPosition + movement).coerceIn(0f, 1f)
            )
        }
    }

    fun updateCurveValueAtIndicator(curveIndex: Int, value: Float?, pageIndex: Int){
        _hourlyChartState[pageIndex].update { chartState ->
            chartState.copy(
                curveValueAtIndicator = chartState.curveValueAtIndicator.toMutableList().apply {
                    this[curveIndex] = value
                }
            )
        }
    }

    fun updateDailyCurveValueAtIndicator(valueMin: Float, valueMax: Float, pageIndex: Int){
        _dailyChartState[pageIndex].update { chartState ->
            chartState.copy(
                curveValueMinAtIndicator = valueMin,
                curveValueMaxAtIndicator = valueMax
            )
        }
    }

    fun updateCurveSmoothness(
        weatherQuantity: WeatherQuantity,
        @FloatRange(from = 0.0, to = 1.0) newSmoothness: Float,
    ){
        val newCurvesSmoothness = _hourlyChartState.getOrNull(0)?.value?.curvesSmoothness?.toMutableMap()?.apply {
            set(weatherQuantity, newSmoothness)
        }
        _hourlyChartState.forEach {
            it.update { chartState ->
                chartState.copy(curvesSmoothness = newCurvesSmoothness!!)
            }
        }
    }

    fun saveCurveSmoothness(
        weatherQuantity: WeatherQuantity,
        @FloatRange(from = 0.0, to = 1.0) newSmoothness: Float,
    ) {
        viewModelScope.launch {
            preferenceRepository.setCurvesSmoothness(weatherQuantity, newSmoothness)
        }
    }

    fun swapLocations(id1: Int, id2: Int){
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
        initialDailyYAxesStarts = initialDailyYAxesStarts.toMutableList().apply {
            swap(index1, index2)
        }
        initialDailyYAxesEnds = initialDailyYAxesEnds.toMutableList().apply {
            swap(index1, index2)
        }

        _detailState.update {
            it.toMutableList().apply {
                swap(index1, index2)
            }
        }
        _hourlyChartState = _hourlyChartState.apply { swap(index1, index2) }
        _locationsHourlyChartData[index1] = hourlyChartDataFlow(index1)
        _locationsHourlyChartData[index2] = hourlyChartDataFlow(index2)

        _dailyChartState = _dailyChartState.apply { swap(index1, index2) }
        _locationsDailyChartData[index1] = dailyChartDataFlow(index1)
        _locationsDailyChartData[index2] = dailyChartDataFlow(index2)
    }

    fun updateChartsVisibility(visible: Boolean, pageIndex: Int){
        _detailState.update { detailList ->
            detailList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    chartsVisibility = visible
                )
            }
        }
    }

    fun updateInitialXAxisBounds(detailScreenWidth: Dp){
        val hourlyXAxisLength = detailScreenWidth / 120.dp
        val initialHourlyXAxisStart = -hourlyXAxisLength * 24 / 3
        val initialHourlyXAxisEnd = hourlyXAxisLength * 2 * 24 / 3

        val dailyXAxisLength = detailScreenWidth / 50.dp
        val initialDailyXAxisStart = -(dailyXAxisLength / 4).toInt() - 0.5f
        val initialDailyXAxisEnd = (dailyXAxisLength * 3 / 4).toInt() + 0.5f

        _hourlyChartState.forEach {
            it.update { chartState ->
                chartState.copy(
                    initialXAxisStart = initialHourlyXAxisStart,
                    initialXAxisEnd = initialHourlyXAxisEnd
                )
            }
        }
        _dailyChartState.forEach {
            it.update { chartState ->
                chartState.copy(
                    initialDailyXAxisStart = initialDailyXAxisStart,
                    initialDailyXAxisEnd = initialDailyXAxisEnd
                )
            }
        }
    }

    fun initializeXAxisBounds(pageIndex: Int){
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
            }
            _dailyChartState[pageIndex].update { chartState ->
                chartState.apply {
                    xAxisStart.snapTo(chartState.initialDailyXAxisStart)
                    xAxisEnd.snapTo(chartState.initialDailyXAxisEnd)
                }
            }
        }
    }

    fun updateHourlySettingsStatus(status: Boolean){
        _hourlyChartState.forEach { chartState ->
            chartState.update {
                it.copy(settingsUpdated = status)
            }
        }
    }

    fun updateDailySettingsStatus(status: Boolean){
        _dailyChartState.forEach { chartState ->
            chartState.update {
                it.copy(settingsUpdated = status)
            }
        }
    }

}