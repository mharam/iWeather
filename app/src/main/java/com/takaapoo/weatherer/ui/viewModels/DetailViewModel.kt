package com.takaapoo.weatherer.ui.viewModels

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.ChartState
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.initialDailyXAxisEnd
import com.takaapoo.weatherer.domain.model.initialDailyXAxisStart
import com.takaapoo.weatherer.domain.model.initialXAxisEnd
import com.takaapoo.weatherer.domain.model.initialXAxisStart
import com.takaapoo.weatherer.domain.repository.AirQualityRepository
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.LocationRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.use_case.GetHourlyChartDataUseCase
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.daySeconds
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.timeToX
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

const val Extra_Data_Loading_Days = 2
const val Extra_Data_Loading_Days_For_Daily_Diagrams = 7
val timeFontFamily = Font(R.font.cmu_typewriter_bold).toFontFamily()
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

    private val locationsCount: Int = savedStateHandle["locationsCount"] ?: 1
    private val _detailState = MutableStateFlow(
        List(size = locationsCount){ DetailState() }
    )
    val detailState = _detailState.asStateFlow()

    private val _chartState = MutableStateFlow(
        List(size = locationsCount){ ChartState() }
    )
    val chartState = _chartState.asStateFlow()

    private val _dailyChartState = MutableStateFlow(
        List(size = locationsCount){ DailyChartState() }
    )
    val dailyChartState = _dailyChartState.asStateFlow()

    var locationsHourlyChartData: List<StateFlow<List<HourlyChartDto>>> = emptyList()
    var locationsDailyChartData: List<StateFlow<List<LocalDailyWeather>>> = emptyList()
    var allLocations: List<Location> = emptyList()
    private val initialYAxesStarts: List<MutableList<Float>> = List(locationsCount){ mutableListOf() }
    private val initialYAxesEnds: List<MutableList<Float>> = List(locationsCount){ mutableListOf() }
    private val initialDailyYAxesStarts: MutableList<Float> = MutableList(locationsCount){ 0f }
    private val initialDailyYAxesEnds: MutableList<Float> = MutableList(locationsCount){ 0f }

    var pagerScreenScroll: Int = 0

    init {
        viewModelScope.launch {
            val appSettings = preferenceRepository.getSettings()
            updateHourlyChartWeatherIconVisibility(appSettings.hourlyDiagramWeatherConditionIconVisible)
            updateDailyChartWeatherIconVisibility(appSettings.dailyDiagramWeatherConditionIconVisible)
            updateHourlyChartDotsOnCurveVisibility(appSettings.hourlyDotsOnCurveVisible)
            updateHourlyChartCurveShadowVisibility(appSettings.hourlyCurveShadowVisible)
            updateHourlyChartGrid(appSettings.hourlyChartGrid)
            updateHourlyChartSunRiseSetIconsVisibility(appSettings.hourlySunRiseSetIconsVisible)
            updateHourlyChartTheme(appSettings.hourlyChartTheme)

            allLocations = locationRepository.getAllLocationsList().also {
                it.forEachIndexed { index, location ->
                    _detailState.update { detailStateList ->
                        detailStateList.toMutableList().apply {
                            this[index] = this[index].copy(
                                utcOffset = location.utcOffset
                            )
                        }
                    }
                }
            }
            launch(Dispatchers.Default) {
                while (true){
                    val utcCurrentTime = LocalDateTime.now(ZoneId.of("UTC"))
                    val utcCurrentDate = utcCurrentTime.toLocalDate()
                    val targetX = (utcCurrentTime.toLocalTime().toSecondOfDay() / daySeconds) * 24
                    allLocations.forEachIndexed { index, location ->
                        launch {
                            val hourlyWeather =
                                localWeatherRepository.getLocationHourlyWeather(
                                    locationId = location.id,
                                    startDate = utcCurrentDate.toString(),
                                    endDate = utcCurrentDate.plusDays(1).toString()
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
                                endDate = localDate
                            )
                            _detailState.update { detailStateList ->
                                detailStateList.toMutableList().apply {
                                    this[index] = this[index].copy(
                                        maxTemperature = dailyWeather.getOrNull(0)?.temperatureMax,
                                        minTemperature = dailyWeather.getOrNull(0)?.temperatureMin,
                                        sunRise = dailyWeather.getOrNull(0)?.sunRise,
                                        sunSet = dailyWeather.getOrNull(0)?.sunSet,
                                        currentDayHourlyWeather = hourlyWeather,
                                        currentDayHourlyAirQuality = hourlyAirQuality,
                                        targetX = targetX
                                    )
                                }
                            }
                        }
                    }
                    delay(60_000)
                }
            }
            launch(Dispatchers.Default) {
                while (true) {
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

            _chartState.update { chartStateList ->
                chartStateList.toMutableList().apply {
                    allLocations.forEachIndexed { index, location ->
                        val utcNow = LocalDateTime.now(ZoneId.of("UTC")).toString()
                        val nowX = timeToX(utcNow, location.utcOffset) ?: 0f
                        this[index] = this[index].copy(
                            sliderThumbPosition = ((nowX - initialXAxisStart) / (initialXAxisEnd - initialXAxisStart))
                        )
                    }
                }
            }
            _dailyChartState.update { chartStateList ->
                chartStateList.toMutableList().apply {
                    allLocations.forEachIndexed { index, _ ->
                        this[index] = this[index].copy(
                            sliderThumbPosition = ((0f - initialDailyXAxisStart) /
                                    (initialDailyXAxisEnd - initialDailyXAxisStart))
                        )
                    }
                }
            }

            locationsHourlyChartData = List(size = locationsCount){ index ->
                snapshotFlow {
                    _chartState.value.map{ it.xAxisStart.value to it.xAxisEnd.value }
                }.flatMapLatest { listOfStartEndPairs ->
                    val startDate = LocalDate.now()
                        .plusDays(
                            (listOfStartEndPairs[index].first/24).toLong() - Extra_Data_Loading_Days
                        )
                        .toString()
                    val endDate = LocalDate.now()
                        .plusDays(
                            (listOfStartEndPairs[index].second/24).toLong() + Extra_Data_Loading_Days
                        )
                        .toString()
                    hourlyChartDataUseCase(
                        locationId = allLocations[index].id,
                        startDate = startDate,
                        endDate = endDate
                    )
                }.stateIn(
                    viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
            }
            locationsDailyChartData = List(size = locationsCount){ index ->
                snapshotFlow {
                    _dailyChartState.value.map{ it.xAxisStart.value to it.xAxisEnd.value }
                }.flatMapLatest { listOfStartEndPairs ->
                    val startDate = LocalDate.now()
                        .plusDays((listOfStartEndPairs[index].first).toLong() -
                                Extra_Data_Loading_Days_For_Daily_Diagrams
                        ).toString()
                    val endDate = LocalDate.now()
                        .plusDays((listOfStartEndPairs[index].second).toLong() +
                                Extra_Data_Loading_Days_For_Daily_Diagrams
                        ).toString()
                    localWeatherRepository.getLocationDailyWeatherFlow(
                        locationId = allLocations[index].id,
                        startDate = startDate,
                        endDate = endDate
                    )
                }.stateIn(
                    viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
            }
        }
    }

    fun addChartQuantity(quantity: WeatherQuantity, pageIndex: Int){
        if (!_chartState.value[pageIndex].chartQuantities.contains(quantity) &&
            _chartState.value[pageIndex].chartQuantities.size < 4) {
            _chartState.update { chartStateList ->
                chartStateList.toMutableList().apply {
                    this[pageIndex] = this[pageIndex].copy(
                        chartQuantities = this[pageIndex].chartQuantities + quantity,
                        curveValueAtIndicator = this[pageIndex].curveValueAtIndicator + 0f
                    )
                }
            }
        }
    }

    fun removeChartQuantity(quantity: WeatherQuantity, pageIndex: Int){
        val curveIndex = _chartState.value[pageIndex].chartQuantities.indexOf(quantity)
        if (curveIndex >= 0) {
            _chartState.update { chartStateList ->
                chartStateList.toMutableList().apply {
                    this[pageIndex] = this[pageIndex].copy(
                        chartQuantities = this[pageIndex].chartQuantities - quantity
                    )
                }
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
                if (quantityMax - quantityMin < 5)
                    quantityMin - 5 to quantityMax + 5
                else
                    quantityMin - rangeTenPercentage to quantityMax + rangeTenPercentage
            }
            DailyWeatherQuantity.SUNRISESET -> quantityMin to quantityMax
            DailyWeatherQuantity.UVINDEXMAX -> {
                -0.5f to 11f
            }
            DailyWeatherQuantity.PRECIPITATIONSUM -> {
                -(rangeTenPercentage.coerceAtLeast(0.2f)) to
                        (quantityMax + rangeTenPercentage).coerceAtLeast(1f)
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
        _dailyChartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    chartQuantity = quantity,
                    curveAnimator = Animatable(initialValue = 0f)
                )
            }
        }
        updateDailyYAxis(quantity, pageIndex)
    }

    private fun copyYAxis(pageIndex: Int, axisIndex: Int){
        _chartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    curveAnimator = this[pageIndex].curveAnimator + Animatable(initialValue = 0f),
                    yAxesStarts = this[pageIndex].yAxesStarts + Animatable(
                        initialValue = this[pageIndex].yAxesStarts[axisIndex].value),
                    yAxesEnds = this[pageIndex].yAxesEnds + Animatable(
                        initialValue = this[pageIndex].yAxesEnds[axisIndex].value)
                )
            }
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
        axisIndex: Int? = null
    ){
        if (axisIndex != null){
            copyYAxis(pageIndex, axisIndex)
            return
        }
        var newStart: Float
        val newEnd: Float
        val newStartAfterTransformation: Float
        val newEndAfterTransformation: Float

        if (_chartState.value[pageIndex].yAxesStarts.isEmpty()) {
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
            val majorBarSeparation =
                (yAxisLength / horizontalBarCount).verticalAxisCustomRound(false)
            val newYAxisLength = horizontalBarCount * majorBarSeparation

            y = (start / majorBarSeparation).roundToInt() * majorBarSeparation
            newStart = y - (extraSpaceBelowFirstBar / majorHorizontalBarSeparation) * majorBarSeparation
            if (newStart > start){
                y = (start / majorBarSeparation).toInt() * majorBarSeparation
                newStart = y - (extraSpaceBelowFirstBar / majorHorizontalBarSeparation) * majorBarSeparation
            }

            newEnd = newStart + newYAxisLength
            val y1Start = _chartState.value[pageIndex].yAxesStarts[0].value
            val y1End = _chartState.value[pageIndex].yAxesEnds[0].value
            val scale = (y1End - y1Start) / y0Length

            if (scale.isEqualTo(1f)){
                val yMovement = _chartState.value[pageIndex].yAxesStarts[0].value -
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
        _chartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    curveAnimator = this[pageIndex].curveAnimator + Animatable(initialValue = 0f),
                    yAxesEnds = this[pageIndex].yAxesEnds + Animatable(initialValue = newEndAfterTransformation),
                    yAxesStarts = this[pageIndex].yAxesStarts + Animatable(initialValue = newStartAfterTransformation),
                )
            }
        }
        initialYAxesStarts[pageIndex].add(newStart)
        initialYAxesEnds[pageIndex].add(newEnd)
    }

    fun updateDailyYAxis(quantity: DailyWeatherQuantity, pageIndex: Int){
        val quantityMin: Float?
        val quantityMax: Float?
        when (quantity ){
            DailyWeatherQuantity.TEMPERATUREMINMAX -> {
                val dataMin = locationsDailyChartData[pageIndex].value.map { it.temperatureMin }
                val dataMax =  locationsDailyChartData[pageIndex].value.map { it.temperatureMax }
                quantityMin = dataMin.minOfOrNull { it ?: Float.MAX_VALUE }
                quantityMax = dataMax.maxOfOrNull { it ?: Float.MIN_VALUE }
            }
            DailyWeatherQuantity.SUNRISESET -> {
                quantityMin = -2f
                quantityMax = 26f
            }
            else -> {
                val data = dailyQuantityData(locationsDailyChartData[pageIndex].value, quantity)
                quantityMax = data.maxOfOrNull { it ?: Float.MIN_VALUE }
                quantityMin = data.minOfOrNull { it ?: Float.MAX_VALUE }
            }
        }
        if (quantityMin != null && quantityMax != null) {
            val (modifiedQuantityMin, modifiedQuantityMax) =
                dailyQuantityMinMaxModifier(quantity, quantityMin, quantityMax)
            _dailyChartState.update { chartStateList ->
                chartStateList.toMutableList().apply {
                    this[pageIndex] = this[pageIndex].copy(
//                        curveAnimator = Animatable(initialValue = 0f),
                        yAxesEnds = Animatable(initialValue = modifiedQuantityMax),
                        yAxesStarts = Animatable(initialValue = modifiedQuantityMin),
                    )
                }
            }
            initialDailyYAxesStarts[pageIndex] = modifiedQuantityMin
            initialDailyYAxesEnds[pageIndex] = modifiedQuantityMax
        }
    }

    private fun removeYAxis(curveIndex: Int, pageIndex: Int){
        _chartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    yAxesStarts = this[pageIndex].yAxesStarts.filterIndexed { index, _ ->
                        index != curveIndex
                    },
                    yAxesEnds = this[pageIndex].yAxesEnds.filterIndexed { index, _ ->
                        index != curveIndex
                    },
                    curveAnimator = this[pageIndex].curveAnimator.filterIndexed { index, _ ->
                        index != curveIndex
                    },
                    curveValueAtIndicator =  this[pageIndex].curveValueAtIndicator.filterIndexed { index, _ ->
                        index != curveIndex
                    }
                )
            }
        }
        initialYAxesStarts[pageIndex].removeAt(curveIndex)
        initialYAxesEnds[pageIndex].removeAt(curveIndex)
    }

//    private fun removeDailyYAxis(curveIndex: Int, pageIndex: Int){
//        _dailyChartState.update { chartStateList ->
//            chartStateList.toMutableList().apply {
//                this[pageIndex] = this[pageIndex].copy(
//                    yAxesStarts = this[pageIndex].yAxesStarts.filterIndexed { index, _ ->
//                        index != curveIndex
//                    },
//                    yAxesEnds = this[pageIndex].yAxesEnds.filterIndexed { index, _ ->
//                        index != curveIndex
//                    },
//                    curveAnimator = this[pageIndex].curveAnimator.filterIndexed { index, _ ->
//                        index != curveIndex
//                    },
//                    curveValueAtIndicator =  this[pageIndex].curveValueAtIndicator.filterIndexed { index, _ ->
//                        index != curveIndex
//                    }
//                )
//            }
//        }
//        initialDailyYAxesStarts[pageIndex].removeAt(curveIndex)
//        initialDailyYAxesEnds[pageIndex].removeAt(curveIndex)
//    }

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
                _chartState.value[pageIndex].let { it.xAxisEnd.value - it.xAxisStart.value }
            val xAxisMovement = offset.x * xAxisLength
            launch {
                _chartState.value[pageIndex].xAxisStart.snapTo(
                    _chartState.value[pageIndex].xAxisStart.value - xAxisMovement
                )
                _chartState.value[pageIndex].xAxisEnd.snapTo(
                    _chartState.value[pageIndex].xAxisEnd.value - xAxisMovement
                )
            }

            val curveCount = _chartState.value[pageIndex].yAxesStarts.size
            for (i in 0 until curveCount) {
                val yAxisLength =
                    _chartState.value[pageIndex].let { it.yAxesEnds[i].value - it.yAxesStarts[i].value }
                val yAxisMovement = offset.y * yAxisLength
                launch {
                    _chartState.value[pageIndex].yAxesStarts[i].snapTo(
                        _chartState.value[pageIndex].yAxesStarts[i].value + yAxisMovement
                    )
                    _chartState.value[pageIndex].yAxesEnds[i].snapTo(
                        _chartState.value[pageIndex].yAxesEnds[i].value + yAxisMovement
                    )
                }
            }
        }
    }

    fun moveDailyDiagramAxis(offset: Offset, pageIndex: Int) {
        viewModelScope.launch {
            val xAxisLength =
                _dailyChartState.value[pageIndex].let { it.xAxisEnd.value - it.xAxisStart.value }
            val xAxisMovement = offset.x * xAxisLength
            launch {
                _dailyChartState.value[pageIndex].xAxisStart.snapTo(
                    _dailyChartState.value[pageIndex].xAxisStart.value - xAxisMovement
                )
                _dailyChartState.value[pageIndex].xAxisEnd.snapTo(
                    _dailyChartState.value[pageIndex].xAxisEnd.value - xAxisMovement
                )
            }

            val yAxisLength =
                _dailyChartState.value[pageIndex].let { it.yAxesEnds.value - it.yAxesStarts.value }
            val yAxisMovement = offset.y * yAxisLength
            launch {
                _dailyChartState.value[pageIndex].yAxesStarts.snapTo(
                    _dailyChartState.value[pageIndex].yAxesStarts.value + yAxisMovement
                )
                _dailyChartState.value[pageIndex].yAxesEnds.snapTo(
                    _dailyChartState.value[pageIndex].yAxesEnds.value + yAxisMovement
                )
            }
        }
    }

    fun scaleHourlyDiagramAxis(center: Offset, scaleX: Float, scaleY: Float, pageIndex: Int){
        val xAxisLength = _chartState.value[pageIndex].let { it.xAxisEnd.value - it.xAxisStart.value }
        val centerX = _chartState.value[pageIndex].xAxisStart.value + center.x * xAxisLength
        val effectiveScaleX = when {
            (xAxisLength < 3 && scaleX > 1) || (xAxisLength > 14 * 24 && scaleX < 1) -> 1 + (scaleX - 1) / 5
            else -> scaleX
        }
        val xAxisStart = centerX - (centerX - _chartState.value[pageIndex].xAxisStart.value)/effectiveScaleX
        val xAxisEnd = centerX + (_chartState.value[pageIndex].xAxisEnd.value - centerX)/effectiveScaleX

        viewModelScope.launch {
            launch {
                _chartState.value[pageIndex].xAxisStart.snapTo(xAxisStart)
                _chartState.value[pageIndex].xAxisEnd.snapTo(xAxisEnd)
            }

            val curveCount = _chartState.value[pageIndex].yAxesStarts.size
            for (i in 0 until curveCount) {
                val yAxisLength =
                    _chartState.value[pageIndex].let { it.yAxesEnds[i].value - it.yAxesStarts[i].value }
                val centerY = _chartState.value[pageIndex].yAxesEnds[i].value - center.y * yAxisLength
                val initialYAxesRange = initialYAxesEnds[pageIndex][i] - initialYAxesStarts[pageIndex][i]
                val effectiveScaleY = when {
                    (yAxisLength < initialYAxesRange / 10 && scaleX > 1) ||
                            (yAxisLength > initialYAxesRange * 10 && scaleX < 1) -> 1 + (scaleY - 1) / 5

                    else -> scaleY
                }
                val yAxisStart =
                    centerY - (centerY - _chartState.value[pageIndex].yAxesStarts[i].value) / effectiveScaleY
                val yAxisEnd =
                    centerY + (_chartState.value[pageIndex].yAxesEnds[i].value - centerY) / effectiveScaleY
                launch {
                    _chartState.value[pageIndex].yAxesStarts[i].snapTo(yAxisStart)
                    _chartState.value[pageIndex].yAxesEnds[i].snapTo(yAxisEnd)
                }
            }
        }
    }

    fun scaleDailyDiagramAxis(center: Offset, scaleX: Float, scaleY: Float, pageIndex: Int){
        val xAxisLength = _dailyChartState.value[pageIndex].let { it.xAxisEnd.value - it.xAxisStart.value }
        val centerX = _dailyChartState.value[pageIndex].xAxisStart.value + center.x * xAxisLength
        val effectiveScaleX = when {
            (xAxisLength < 3 && scaleX > 1) || (xAxisLength > 60 && scaleX < 1) -> 1 + (scaleX - 1) / 5
            else -> scaleX
        }
        val xAxisStart = centerX - (centerX - _dailyChartState.value[pageIndex].xAxisStart.value)/effectiveScaleX
        val xAxisEnd = centerX + (_dailyChartState.value[pageIndex].xAxisEnd.value - centerX)/effectiveScaleX

        viewModelScope.launch {
            launch {
                _dailyChartState.value[pageIndex].xAxisStart.snapTo(xAxisStart)
                _dailyChartState.value[pageIndex].xAxisEnd.snapTo(xAxisEnd)
            }

            val yAxisLength =
                _dailyChartState.value[pageIndex].let { it.yAxesEnds.value - it.yAxesStarts.value }
            val centerY = _dailyChartState.value[pageIndex].yAxesEnds.value - center.y * yAxisLength
            val initialYAxesRange = initialDailyYAxesEnds[pageIndex] - initialDailyYAxesStarts[pageIndex]
            val effectiveScaleY = when {
                (yAxisLength < initialYAxesRange / 20 && scaleX > 1) ||
                        (yAxisLength > initialYAxesRange * 20 && scaleX < 1) -> 1 + (scaleY - 1) / 5

                else -> scaleY
            }
            val yAxisStart =
                centerY - (centerY - _dailyChartState.value[pageIndex].yAxesStarts.value) / effectiveScaleY
            val yAxisEnd =
                centerY + (_dailyChartState.value[pageIndex].yAxesEnds.value - centerY) / effectiveScaleY
            launch {
                _dailyChartState.value[pageIndex].yAxesStarts.snapTo(yAxisStart)
                _dailyChartState.value[pageIndex].yAxesEnds.snapTo(yAxisEnd)
            }

        }
    }

    suspend fun scaleToNormalHourlyDiagramAxis(center: Offset, pageIndex: Int){
        withContext(context = Dispatchers.Main){
            launch {
                val xAxisLength = _chartState.value[pageIndex].let { it.xAxisEnd.value - it.xAxisStart.value }
                val scaleX =
                    if (xAxisLength < 3) 3f / xAxisLength
                    else if (xAxisLength > 14 * 24) 14 * 24 / xAxisLength
                    else return@launch
                val centerX = _chartState.value[pageIndex].xAxisStart.value + center.x * xAxisLength
                val xAxisStart = centerX - scaleX * (centerX - _chartState.value[pageIndex].xAxisStart.value)
                val xAxisEnd = centerX + scaleX * (_chartState.value[pageIndex].xAxisEnd.value - centerX)

                launch { _chartState.value[pageIndex].xAxisStart.animateTo(xAxisStart) }
                launch { _chartState.value[pageIndex].xAxisEnd.animateTo(xAxisEnd) }
            }

            val curveCount = _chartState.value[pageIndex].yAxesStarts.size
            for (i in 0 until curveCount) {
                launch {
                    val yAxisLength =
                        _chartState.value[pageIndex].let { it.yAxesEnds[i].value - it.yAxesStarts[i].value }
                    val initialYAxesRange = initialYAxesEnds[pageIndex][i] - initialYAxesStarts[pageIndex][i]
                    val scaleY =
                        if (yAxisLength < initialYAxesRange / 10) initialYAxesRange / (10 * yAxisLength)
                        else if (yAxisLength > 10 * initialYAxesRange) 10 * initialYAxesRange / yAxisLength
                        else return@launch
                    val centerY =
                        _chartState.value[pageIndex].yAxesEnds[i].value - center.y * yAxisLength
                    val yAxisStart =
                        centerY - scaleY * (centerY - _chartState.value[pageIndex].yAxesStarts[i].value)
                    val yAxisEnd =
                        centerY + scaleY * (_chartState.value[pageIndex].yAxesEnds[i].value - centerY)

                    launch { _chartState.value[pageIndex].yAxesStarts[i].animateTo(yAxisStart) }
                    launch { _chartState.value[pageIndex].yAxesEnds[i].animateTo(yAxisEnd) }
                }
            }
        }
    }

    suspend fun scaleToNormalDailyDiagramAxis(center: Offset, pageIndex: Int){
        withContext(context = Dispatchers.Main){
            launch {
                val xAxisLength = _dailyChartState.value[pageIndex].let { it.xAxisEnd.value - it.xAxisStart.value }
                val scaleX =
                    if (xAxisLength < 3) 3f / xAxisLength
                    else if (xAxisLength > 60) 60 / xAxisLength
                    else return@launch
                val centerX = _dailyChartState.value[pageIndex].xAxisStart.value + center.x * xAxisLength
                val xAxisStart = centerX - scaleX * (centerX - _dailyChartState.value[pageIndex].xAxisStart.value)
                val xAxisEnd = centerX + scaleX * (_dailyChartState.value[pageIndex].xAxisEnd.value - centerX)

                launch { _dailyChartState.value[pageIndex].xAxisStart.animateTo(xAxisStart) }
                launch { _dailyChartState.value[pageIndex].xAxisEnd.animateTo(xAxisEnd) }
            }

            launch {
                val yAxisLength =
                    _dailyChartState.value[pageIndex].let { it.yAxesEnds.value - it.yAxesStarts.value }
                val initialYAxesRange = initialDailyYAxesEnds[pageIndex] - initialDailyYAxesStarts[pageIndex]
                val scaleY =
                    if (yAxisLength < initialYAxesRange / 20) initialYAxesRange / (20 * yAxisLength)
                    else if (yAxisLength > 20 * initialYAxesRange) 20 * initialYAxesRange / yAxisLength
                    else return@launch
                val centerY =
                    _dailyChartState.value[pageIndex].yAxesEnds.value - center.y * yAxisLength
                val yAxisStart =
                    centerY - scaleY * (centerY - _dailyChartState.value[pageIndex].yAxesStarts.value)
                val yAxisEnd =
                    centerY + scaleY * (_dailyChartState.value[pageIndex].yAxesEnds.value - centerY)

                launch { _dailyChartState.value[pageIndex].yAxesStarts.animateTo(yAxisStart) }
                launch { _dailyChartState.value[pageIndex].yAxesEnds.animateTo(yAxisEnd) }
            }
        }
    }

    suspend fun undoHourlyDiagramAxisMoveScale(pageIndex: Int){
        val animationSpec = tween<Float>(durationMillis = 600)
        withContext(context = Dispatchers.Main){
            launch {
                _chartState.value[pageIndex].xAxisStart.animateTo(
                    targetValue = initialXAxisStart,
                    animationSpec = animationSpec
                )
            }
            launch {
                _chartState.value[pageIndex].xAxisEnd.animateTo(
                    targetValue = initialXAxisEnd,
                    animationSpec = animationSpec
                )
            }

            val curveCount = _chartState.value[pageIndex].yAxesStarts.size
            for (i in 0 until curveCount) {
                launch {
                    _chartState.value[pageIndex].yAxesStarts[i].animateTo(
                        targetValue = initialYAxesStarts[pageIndex][i],
                        animationSpec = animationSpec
                    )
                }
                launch {
                    _chartState.value[pageIndex].yAxesEnds[i].animateTo(
                        targetValue = initialYAxesEnds[pageIndex][i],
                        animationSpec = animationSpec
                    )
                }
            }
        }
    }

    suspend fun undoDailyDiagramAxisMoveScale(pageIndex: Int){
        val animationSpec = tween<Float>(durationMillis = 600)
        withContext(context = Dispatchers.Main){
            launch {
                _dailyChartState.value[pageIndex].xAxisStart.animateTo(
                    targetValue = initialDailyXAxisStart,
                    animationSpec = animationSpec
                )
            }
            launch {
                _dailyChartState.value[pageIndex].xAxisEnd.animateTo(
                    targetValue = initialDailyXAxisEnd,
                    animationSpec = animationSpec
                )
            }

            launch {
                _dailyChartState.value[pageIndex].yAxesStarts.animateTo(
                    targetValue = initialDailyYAxesStarts[pageIndex],
                    animationSpec = animationSpec
                )
            }
            launch {
                _dailyChartState.value[pageIndex].yAxesEnds.animateTo(
                    targetValue = initialDailyYAxesEnds[pageIndex],
                    animationSpec = animationSpec
                )
            }
        }
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
        _chartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                for (i in this.indices) {
                    this[i] = this[i].copy(weatherConditionIconsVisible = visible)
                }
            }
        }
    }

    fun updateDailyChartWeatherIconVisibility(visible : Boolean){
        viewModelScope.launch {
            preferenceRepository.setDailyDiagramWeatherConditionIconVisibility(visible)
        }
        _dailyChartState.update { dailyChartStateList ->
            dailyChartStateList.toMutableList().apply {
                for (i in this.indices) {
                    this[i] = this[i].copy(weatherConditionIconsVisible = visible)
                }
            }
        }
    }

    fun updateHourlyChartDotsOnCurveVisibility(visible : Boolean){
        viewModelScope.launch {
            preferenceRepository.setHourlyDotsOnCurveVisibility(visible)
        }
        _chartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                for (i in this.indices) {
                    this[i] = this[i].copy(dotsOnCurveVisible = visible)
                }
            }
        }
    }

    fun updateHourlyChartCurveShadowVisibility(visible : Boolean){
        viewModelScope.launch {
            preferenceRepository.setHourlyCurveShadowVisibility(visible)
        }
        _chartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                for (i in this.indices) {
                    this[i] = this[i].copy(curveShadowVisible = visible)
                }
            }
        }
    }

    fun updateHourlyChartSunRiseSetIconsVisibility(visible : Boolean){
        viewModelScope.launch {
            preferenceRepository.setHourlySunRiseSetIconsVisibility(visible)
        }
        _chartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                for (i in this.indices) {
                    this[i] = this[i].copy(sunRiseSetIconsVisible = visible)
                }
            }
        }
    }

    fun updateHourlyChartGrid(gridType: ChartGrids){
        viewModelScope.launch {
            preferenceRepository.setHourlyChartGrid(gridType)
        }
        _chartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                for (i in this.indices) {
                    this[i] = this[i].copy(chartGrid = gridType)
                }
            }
        }
    }

    fun updateHourlyChartTheme(theme: ChartTheme){
        viewModelScope.launch {
            preferenceRepository.setHourlyChartTheme(theme)
        }
        _chartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                for (i in this.indices) {
                    this[i] = this[i].copy(chartTheme = theme)
                }
            }
        }
    }

    fun updateSliderThumbPosition(movement: Float, pageIndex: Int){
        _chartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    sliderThumbPosition = (this[pageIndex].sliderThumbPosition + movement).coerceIn(0f, 1f)
                )
            }
        }
    }

    fun updateDailySliderThumbPosition(movement: Float, pageIndex: Int){
        _dailyChartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    sliderThumbPosition = (this[pageIndex].sliderThumbPosition + movement).coerceIn(0f, 1f)
                )
            }
        }
    }

    fun updateCurveValueAtIndicator(curveIndex: Int, value: Float, pageIndex: Int){
        _chartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    curveValueAtIndicator = this[pageIndex].curveValueAtIndicator.toMutableList().apply {
                        this[curveIndex] = value
                    }
                )
            }
        }
    }

    fun updateDailyCurveValueAtIndicator(valueMin: Float, valueMax: Float, pageIndex: Int){
        _dailyChartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    curveValueMinAtIndicator = valueMin,
                    curveValueMaxAtIndicator = valueMax
                )
            }
        }
    }

    fun updateScrollValue(value: Int, pageIndex: Int){
        _detailState.update { detailList ->
            detailList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    scrollValue = value
                )
            }
        }
    }

    fun navigateToAQDetailScreen(pageIndex: Int){

    }

}