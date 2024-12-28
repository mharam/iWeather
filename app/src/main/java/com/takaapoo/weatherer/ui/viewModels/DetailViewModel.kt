package com.takaapoo.weatherer.ui.viewModels

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartState
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
import com.takaapoo.weatherer.ui.utility.mmToUnit
import com.takaapoo.weatherer.ui.utility.swap
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
import javax.inject.Inject
import kotlin.collections.map
import kotlin.collections.toMutableList
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

//const val Extra_Data_Loading_Days = 2
//const val Extra_Data_Loading_Days_For_Daily_Diagrams = 7
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

    private val _hourlyChartState: MutableStateFlow<List<HourlyChartState>> =
        MutableStateFlow(List(size = 0){ HourlyChartState() })
    var hourlyChartState: StateFlow<List<HourlyChartState>> = _hourlyChartState.asStateFlow()

    private val _dailyChartState: MutableStateFlow<List<DailyChartState>> =
        MutableStateFlow(List(size = 0){ DailyChartState() })
    var dailyChartState: StateFlow<List<DailyChartState>> = _dailyChartState.asStateFlow()

    val locationsHourlyChartData: StateFlow<List<List<HourlyChartDto>>> =
        combine(
            _hourlyChartState.map {
                it.map { hourlyState -> hourlyState.xAxisStart.value to hourlyState.xAxisEnd.value }
            },
            allLocationsFlow,
            settingsFlow
        ){ listOfStartEndPairs, locationList, appSettings ->
//            Log.i("daily1", "listOfStartEndPairs = $listOfStartEndPairs")
            if (listOfStartEndPairs.size == locationList.size) {
                val newDataList = List(size = locationList.size) { index ->
                    val extraDataLoadingDays = ceil((listOfStartEndPairs[index].second -
                            listOfStartEndPairs[index].first) / (1.5f * 24)).toLong()
                    val startDate = LocalDate.now().plusDays(
                        floor(listOfStartEndPairs[index].first / 24).toLong() - extraDataLoadingDays
                    ).toString()
                    val endDate = LocalDate.now().plusDays(
                        ceil(listOfStartEndPairs[index].second / 24).toLong() + extraDataLoadingDays
                    ).toString()

                    hourlyChartDataUseCase(
                        locationId = locationList[index].id,
                        startDate = startDate,
                        endDate = endDate,
                        appSettings = appSettings
                    )
                }
                updateHourlySettingsStatus(appSettings.toSettingsUnits() == settingsUnits)
                newDataList
            } else {
                emptyList()
            }
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    var locationsDailyChartData: StateFlow<List<List<LocalDailyWeather>>> =
        combine(
            _dailyChartState.map {
                it.map { dailyState -> dailyState.xAxisStart.value to dailyState.xAxisEnd.value }
            },
            allLocationsFlow,
            settingsFlow
        ) { listOfStartEndPairs, locationList, appSettings ->
            val newDataList = List(size = min(listOfStartEndPairs.size, locationList.size)) { index ->
                val extraDataLoadingDays = ceil((listOfStartEndPairs[index].second -
                        listOfStartEndPairs[index].first) / 1.5f).toLong()
                val startDate = LocalDate.now()
                    .plusDays(
                        (listOfStartEndPairs[index].first).toLong() - extraDataLoadingDays
                    ).toString()
                val endDate = LocalDate.now()
                    .plusDays(
                        (listOfStartEndPairs[index].second).toLong() + extraDataLoadingDays
                    ).toString()

                localWeatherRepository.getLocationDailyWeather(
                    locationId = locationList[index].id,
                    startDate = startDate,
                    endDate = endDate,
                    appSettings = appSettings
                )
            }
            updateDailySettingsStatus(appSettings.toSettingsUnits() == settingsUnits)
            newDataList
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private lateinit var initialYAxesStarts: List<MutableList<Float>>
    private lateinit var initialYAxesEnds: List<MutableList<Float>>
    private lateinit var initialDailyYAxesStarts: MutableList<Float>
    private lateinit var initialDailyYAxesEnds: MutableList<Float>


    init {
        initialize()
    }

    private fun initialize(){
        viewModelScope.launch {
            allLocations = locationRepository.getAllLocationsList()
            _detailState.value = List(size = allLocations.size){ DetailState() }
            _hourlyChartState.value = List(size = allLocations.size){ HourlyChartState() }
            _dailyChartState.value = List(size = allLocations.size){ DailyChartState() }
            initialYAxesStarts = List(allLocations.size){ mutableListOf() }
            initialYAxesEnds = List(allLocations.size){ mutableListOf() }
            initialDailyYAxesStarts = MutableList(allLocations.size){ 0f }
            initialDailyYAxesEnds = MutableList(allLocations.size){ 0f }

            val appSettings = preferenceRepository.getSettings()
            updateHourlyChartWeatherIconVisibility(appSettings.hourlyDiagramWeatherConditionIconVisible)
            updateDailyChartWeatherIconVisibility(appSettings.dailyDiagramWeatherConditionIconVisible)
            updateHourlyChartDotsOnCurveVisibility(appSettings.hourlyDotsOnCurveVisible)
            updateHourlyChartCurveShadowVisibility(appSettings.hourlyCurveShadowVisible)
            updateHourlyChartGrid(appSettings.hourlyChartGrid)
            updateHourlyChartSunRiseSetIconsVisibility(appSettings.hourlySunRiseSetIconsVisible)
            updateHourlyChartTheme(appSettings.hourlyChartTheme)

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
                        _hourlyChartState.value.forEachIndexed { index, _ ->
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
        }
    }

    private fun clearYAxis(index: Int){
        _hourlyChartState.update {
            it.toMutableList().apply {
                this[index] = this[index].copy(
                    yAxesStarts = emptyList(),
                    yAxesEnds = emptyList(),
                    curveAnimator = emptyList()
                )
            }
        }
        initialYAxesStarts[index].clear()
        initialYAxesEnds[index].clear()
    }

    fun reInitialize(){
        var deletedLocationIndex: Int?
        viewModelScope.launch {
            val newAllLocations = locationRepository.getAllLocationsList()
            if (newAllLocations.size == allLocations.size) return@launch
            val addedLocationsCount = newAllLocations.size - allLocations.size
            if(addedLocationsCount < 0) {
                deletedLocationIndex = allLocations.indexOfFirst { !newAllLocations.contains(it) }
                deletedLocationIndex.let { index ->
                    _detailState.update {
                        it - it[index]
                    }
                    _hourlyChartState.update {
                        it - it[index]
                    }
                    _dailyChartState.update {
                        it - it[index]
                    }
                    initialYAxesStarts = initialYAxesStarts.apply {
                        this.toMutableList().removeAt(index)
                    }
                    initialYAxesEnds = initialYAxesEnds.apply {
                        this.toMutableList().removeAt(index)
                    }
                    initialDailyYAxesStarts.removeAt(index)
                    initialDailyYAxesEnds.removeAt(index)
                }
                allLocations = newAllLocations
            } else {
                _detailState.update {
                    it + List(size = addedLocationsCount) { DetailState() }
                }
                _hourlyChartState.update {
                    it + List(size = addedLocationsCount) { HourlyChartState() }
                }
                _dailyChartState.update {
                    it + List(size = addedLocationsCount) { DailyChartState() }
                }
                initialYAxesStarts = initialYAxesStarts + List(addedLocationsCount) { mutableListOf() }
                initialYAxesEnds = initialYAxesEnds + List(addedLocationsCount) { mutableListOf() }
                initialDailyYAxesStarts.addAll(List(addedLocationsCount) { 0f })
                initialDailyYAxesEnds.addAll(List(addedLocationsCount) { 0f })
                allLocations = newAllLocations
                updateDetailState()
            }
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
            _hourlyChartState.update {
                it - it[deletedLocationIndex]
            }
            _dailyChartState.update {
                it - it[deletedLocationIndex]
            }
            initialYAxesStarts = initialYAxesStarts.apply {
                this.toMutableList().removeAt(deletedLocationIndex)
            }
            initialYAxesEnds = initialYAxesEnds.apply {
                this.toMutableList().removeAt(deletedLocationIndex)
            }
            initialDailyYAxesStarts.removeAt(deletedLocationIndex)
            initialDailyYAxesEnds.removeAt(deletedLocationIndex)

            allLocations = allLocations.toMutableList().apply { removeAt(deletedLocationIndex) }
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
        _hourlyChartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                allLocations.forEachIndexed { index, location ->
                    val utcNow = LocalDateTime.now(ZoneId.of("UTC")).toString()
                    val nowX = timeToX(utcNow, location.utcOffset) ?: 0f
                    this[index] = this[index].copy(
                        sliderThumbPosition = ((nowX - _hourlyChartState.value[index].initialXAxisStart) /
                                (_hourlyChartState.value[index].initialXAxisEnd -
                                        _hourlyChartState.value[index].initialXAxisStart))
                    )
                }
            }
        }
        _dailyChartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                allLocations.forEachIndexed { index, _ ->
                    this[index] = this[index].copy(
                        sliderThumbPosition = ((0f - _dailyChartState.value[index].initialDailyXAxisStart) /
                                (_dailyChartState.value[index].initialDailyXAxisEnd -
                                        _dailyChartState.value[index].initialDailyXAxisStart))
                    )
                }
            }
        }
    }

    fun addChartQuantity(quantity: WeatherQuantity, pageIndex: Int){
        if (!_hourlyChartState.value[pageIndex].chartQuantities.contains(quantity) &&
            _hourlyChartState.value[pageIndex].chartQuantities.size < 4) {
            _hourlyChartState.update { chartStateList ->
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
        val curveIndex = _hourlyChartState.value[pageIndex].chartQuantities.indexOf(quantity)
        if (curveIndex >= 0) {
            _hourlyChartState.update { chartStateList ->
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
        _dailyChartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    chartQuantity = quantity,
                    curveAnimator = Animatable(initialValue = 0f)
                )
            }
        }
//        updateDailyYAxis(quantity, pageIndex)
    }

    private fun copyYAxis(pageIndex: Int, axisIndex: Int){
        _hourlyChartState.update { chartStateList ->
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

        if (_hourlyChartState.value[pageIndex].yAxesStarts.isEmpty()) {
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
            val y1Start = _hourlyChartState.value[pageIndex].yAxesStarts[0].value
            val y1End = _hourlyChartState.value[pageIndex].yAxesEnds[0].value
            val scale = (y1End - y1Start) / y0Length

            if (scale.isEqualTo(1f)){
                val yMovement = _hourlyChartState.value[pageIndex].yAxesStarts[0].value -
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
        _hourlyChartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                this[pageIndex] = this[pageIndex].copy(
                    curveAnimator = this[pageIndex].curveAnimator + Animatable(initialValue = curveAnimatorInitialValue),
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
                val dataMin = locationsDailyChartData.value[pageIndex].map { it.temperatureMin }
                val dataMax = locationsDailyChartData.value[pageIndex].map { it.temperatureMax }
                quantityMin = dataMin.minOfOrNull { it ?: Float.MAX_VALUE }
                quantityMax = dataMax.maxOfOrNull { it ?: Float.MIN_VALUE }
            }
            DailyWeatherQuantity.SUNRISESET -> {
                quantityMin = -2f
                quantityMax = 26f
            }
            else -> {
                val data = dailyQuantityData(locationsDailyChartData.value[pageIndex], quantity)
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
        _hourlyChartState.update { chartStateList ->
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
                _hourlyChartState.value[pageIndex].let { it.xAxisEnd.value - it.xAxisStart.value }
            val xAxisMovement = offset.x * xAxisLength
            launch {
                _hourlyChartState.update { chartStateList ->
                    chartStateList.toMutableList().apply {
                        this[pageIndex] = this[pageIndex].copy(
                            awaker = this[pageIndex].awaker + 1
                        )
                    }
                }
                _hourlyChartState.value[pageIndex].xAxisStart.snapTo(
                    _hourlyChartState.value[pageIndex].xAxisStart.value - xAxisMovement
                )
                _hourlyChartState.value[pageIndex].xAxisEnd.snapTo(
                    _hourlyChartState.value[pageIndex].xAxisEnd.value - xAxisMovement
                )
            }

            val curveCount = _hourlyChartState.value[pageIndex].yAxesStarts.size
            for (i in 0 until curveCount) {
                val yAxisLength =
                    _hourlyChartState.value[pageIndex].let { it.yAxesEnds[i].value - it.yAxesStarts[i].value }
                val yAxisMovement = offset.y * yAxisLength
                launch {
                    _hourlyChartState.value[pageIndex].yAxesStarts[i].snapTo(
                        _hourlyChartState.value[pageIndex].yAxesStarts[i].value + yAxisMovement
                    )
                    _hourlyChartState.value[pageIndex].yAxesEnds[i].snapTo(
                        _hourlyChartState.value[pageIndex].yAxesEnds[i].value + yAxisMovement
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
                _dailyChartState.update { chartStateList ->
                    chartStateList.toMutableList().apply {
                        this[pageIndex] = this[pageIndex].copy(
                            awaker = this[pageIndex].awaker + 1
                        )
                    }
                }
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
        val initialXAxisLength = _hourlyChartState.value[pageIndex].let {
            it.initialXAxisEnd - it.initialXAxisStart
        }
        val xAxisLengthMin = initialXAxisLength / 20
        val xAxisLengthMax = initialXAxisLength * 9

        val xAxisLength = _hourlyChartState.value[pageIndex].let { it.xAxisEnd.value - it.xAxisStart.value }
        val centerX = _hourlyChartState.value[pageIndex].xAxisStart.value + center.x * xAxisLength
        val effectiveScaleX = when {
            (xAxisLength < xAxisLengthMin && scaleX > 1) ||
                    (xAxisLength > xAxisLengthMax && scaleX < 1) -> 1 + (scaleX - 1) / 5
            else -> scaleX
        }
        val xAxisStart = centerX - (centerX - _hourlyChartState.value[pageIndex].xAxisStart.value)/effectiveScaleX
        val xAxisEnd = centerX + (_hourlyChartState.value[pageIndex].xAxisEnd.value - centerX)/effectiveScaleX

        viewModelScope.launch {
            launch {
                _hourlyChartState.value[pageIndex].xAxisStart.snapTo(xAxisStart)
                _hourlyChartState.value[pageIndex].xAxisEnd.snapTo(xAxisEnd)
                _hourlyChartState.update { chartStateList ->
                    chartStateList.toMutableList().apply {
                        this[pageIndex] = this[pageIndex].copy(
                            awaker = this[pageIndex].awaker + 1
                        )
                    }
                }
            }

            val curveCount = _hourlyChartState.value[pageIndex].yAxesStarts.size
            for (i in 0 until curveCount) {
                val yAxisLength =
                    _hourlyChartState.value[pageIndex].let { it.yAxesEnds[i].value - it.yAxesStarts[i].value }
                val centerY = _hourlyChartState.value[pageIndex].yAxesEnds[i].value - center.y * yAxisLength
                val initialYAxesRange = initialYAxesEnds[pageIndex][i] - initialYAxesStarts[pageIndex][i]
                val effectiveScaleY = when {
                    (yAxisLength < initialYAxesRange / 10 && scaleX > 1) ||
                            (yAxisLength > initialYAxesRange * 10 && scaleX < 1) -> 1 + (scaleY - 1) / 5

                    else -> scaleY
                }
                val yAxisStart =
                    centerY - (centerY - _hourlyChartState.value[pageIndex].yAxesStarts[i].value) / effectiveScaleY
                val yAxisEnd =
                    centerY + (_hourlyChartState.value[pageIndex].yAxesEnds[i].value - centerY) / effectiveScaleY
                launch {
                    _hourlyChartState.value[pageIndex].yAxesStarts[i].snapTo(yAxisStart)
                    _hourlyChartState.value[pageIndex].yAxesEnds[i].snapTo(yAxisEnd)
                }
            }
        }
    }

    fun scaleDailyDiagramAxis(center: Offset, scaleX: Float, scaleY: Float, pageIndex: Int){
        val initialXAxisLength = _dailyChartState.value[pageIndex].let {
            it.initialDailyXAxisEnd - it.initialDailyXAxisStart
        }
        val xAxisLengthMin = initialXAxisLength / 2
        val xAxisLengthMax = initialXAxisLength * 36

        val xAxisLength = _dailyChartState.value[pageIndex].let { it.xAxisEnd.value - it.xAxisStart.value }
        val centerX = _dailyChartState.value[pageIndex].xAxisStart.value + center.x * xAxisLength
        val effectiveScaleX = when {
            (xAxisLength < xAxisLengthMin && scaleX > 1) ||
                    (xAxisLength > xAxisLengthMax && scaleX < 1) -> 1 + (scaleX - 1) / 5
            else -> scaleX
        }
        val xAxisStart = centerX - (centerX - _dailyChartState.value[pageIndex].xAxisStart.value)/effectiveScaleX
        val xAxisEnd = centerX + (_dailyChartState.value[pageIndex].xAxisEnd.value - centerX)/effectiveScaleX

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

        viewModelScope.launch {
            launch {
                _dailyChartState.value[pageIndex].xAxisStart.snapTo(xAxisStart)
                _dailyChartState.value[pageIndex].xAxisEnd.snapTo(xAxisEnd)
                _dailyChartState.update { chartStateList ->
                    chartStateList.toMutableList().apply {
                        this[pageIndex] = this[pageIndex].copy(
                            awaker = this[pageIndex].awaker + 1
                        )
                    }
                }
            }
            launch {
                _dailyChartState.value[pageIndex].yAxesStarts.snapTo(yAxisStart)
                _dailyChartState.value[pageIndex].yAxesEnds.snapTo(yAxisEnd)
            }
        }
    }

    suspend fun scaleToNormalHourlyDiagramAxis(center: Offset, pageIndex: Int){
        val initialXAxisLength = _hourlyChartState.value[pageIndex].let {
            it.initialXAxisEnd - it.initialXAxisStart
        }
        val xAxisLengthMin = initialXAxisLength / 20
        val xAxisLengthMax = initialXAxisLength * 9
        withContext(context = Dispatchers.Main){
            launch {
                val xAxisLength = _hourlyChartState.value[pageIndex].let { it.xAxisEnd.value - it.xAxisStart.value }
                val scaleX =
                    if (xAxisLength < xAxisLengthMin) xAxisLengthMin / xAxisLength
                    else if (xAxisLength > xAxisLengthMax) xAxisLengthMax / xAxisLength
                    else return@launch
                val centerX = _hourlyChartState.value[pageIndex].xAxisStart.value + center.x * xAxisLength
                val xAxisStart = centerX - scaleX * (centerX - _hourlyChartState.value[pageIndex].xAxisStart.value)
                val xAxisEnd = centerX + scaleX * (_hourlyChartState.value[pageIndex].xAxisEnd.value - centerX)

                launch {
                    _hourlyChartState.value[pageIndex].xAxisStart.animateTo(targetValue = xAxisStart)
                }
                launch {
                    _hourlyChartState.value[pageIndex].xAxisEnd.animateTo(targetValue = xAxisEnd)
                }
            }

            val curveCount = _hourlyChartState.value[pageIndex].yAxesStarts.size
            for (i in 0 until curveCount) {
                launch {
                    val yAxisLength =
                        _hourlyChartState.value[pageIndex].let { it.yAxesEnds[i].value - it.yAxesStarts[i].value }
                    val initialYAxesRange = initialYAxesEnds[pageIndex][i] - initialYAxesStarts[pageIndex][i]
                    val scaleY =
                        if (yAxisLength < initialYAxesRange / 10) initialYAxesRange / (10 * yAxisLength)
                        else if (yAxisLength > 10 * initialYAxesRange) 10 * initialYAxesRange / yAxisLength
                        else return@launch
                    val centerY =
                        _hourlyChartState.value[pageIndex].yAxesEnds[i].value - center.y * yAxisLength
                    val yAxisStart =
                        centerY - scaleY * (centerY - _hourlyChartState.value[pageIndex].yAxesStarts[i].value)
                    val yAxisEnd =
                        centerY + scaleY * (_hourlyChartState.value[pageIndex].yAxesEnds[i].value - centerY)

                    launch { _hourlyChartState.value[pageIndex].yAxesStarts[i].animateTo(yAxisStart) }
                    launch { _hourlyChartState.value[pageIndex].yAxesEnds[i].animateTo(yAxisEnd) }
                }
            }
        }
    }

    suspend fun scaleToNormalDailyDiagramAxis(center: Offset, pageIndex: Int){
        val initialXAxisLength = _dailyChartState.value[pageIndex].let {
            it.initialDailyXAxisEnd - it.initialDailyXAxisStart
        }
        val xAxisLengthMin = initialXAxisLength / 2
        val xAxisLengthMax = initialXAxisLength * 36
        withContext(context = Dispatchers.Main){
            launch {
                val xAxisLength = _dailyChartState.value[pageIndex].let { it.xAxisEnd.value - it.xAxisStart.value }
                val scaleX =
                    if (xAxisLength < xAxisLengthMin) xAxisLengthMin / xAxisLength
                    else if (xAxisLength > xAxisLengthMax) xAxisLengthMax / xAxisLength
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
        val animationSpec = tween<Float>(
            durationMillis = 1000,
            easing = CubicBezierEasing(0.6f, 0.6f, 0.4f, 1f)
        )
        withContext(context = Dispatchers.Default){
            launch {
                _hourlyChartState.value[pageIndex].xAxisStart.animateTo(
                    targetValue = _hourlyChartState.value[pageIndex].initialXAxisStart,
                    animationSpec = animationSpec,
                    block = {
                        _hourlyChartState.update { chartStateList ->
                            chartStateList.toMutableList().apply {
                                this[pageIndex] = this[pageIndex].copy(
                                    awaker = this[pageIndex].awaker + 1
                                )
                            }
                        }
                    }
                )
            }
            launch {
                _hourlyChartState.value[pageIndex].xAxisEnd.animateTo(
                    targetValue = _hourlyChartState.value[pageIndex].initialXAxisEnd,
                    animationSpec = animationSpec,
                )
            }

            val curveCount = _hourlyChartState.value[pageIndex].yAxesStarts.size
            for (i in 0 until curveCount) {
                launch {
                    _hourlyChartState.value[pageIndex].yAxesStarts[i].animateTo(
                        targetValue = initialYAxesStarts[pageIndex][i],
                        animationSpec = animationSpec
                    )
                }
                launch {
                    _hourlyChartState.value[pageIndex].yAxesEnds[i].animateTo(
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
                _dailyChartState.value[pageIndex].xAxisStart.animateTo(
                    targetValue = _dailyChartState.value[pageIndex].initialDailyXAxisStart,
                    animationSpec = animationSpec,
                    block = {
                        _dailyChartState.update { chartStateList ->
                            chartStateList.toMutableList().apply {
                                this[pageIndex] = this[pageIndex].copy(
                                    awaker = this[pageIndex].awaker + 1
                                )
                            }
                        }
                    }
                )
            }
            launch {
                _dailyChartState.value[pageIndex].xAxisEnd.animateTo(
                    targetValue = _dailyChartState.value[pageIndex].initialDailyXAxisEnd,
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
        _hourlyChartState.update { chartStateList ->
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
        _hourlyChartState.update { chartStateList ->
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
        _hourlyChartState.update { chartStateList ->
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
        _hourlyChartState.update { chartStateList ->
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
        _hourlyChartState.update { chartStateList ->
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
        _hourlyChartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                for (i in this.indices) {
                    this[i] = this[i].copy(chartTheme = theme)
                }
            }
        }
    }

    fun updateSliderThumbPosition(movement: Float, pageIndex: Int){
        _hourlyChartState.update { chartStateList ->
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

    fun updateCurveValueAtIndicator(curveIndex: Int, value: Float?, pageIndex: Int){
        _hourlyChartState.update { chartStateList ->
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
//        locationsHourlyChartData.update {
//            it.toMutableList().apply {
//                add(index1, removeAt(index2))
//            }
//        }
//        locationsDailyChartData = locationsDailyChartData.toMutableList().apply {
//            add(index1, removeAt(index2))
//        }
        _detailState.update {
            it.toMutableList().apply {
                swap(index1, index2)
            }
        }
        _hourlyChartState.update {
            it.toMutableList().apply {
                swap(index1, index2)
            }
        }
        _dailyChartState.update {
            it.toMutableList().apply {
                swap(index1, index2)
            }
        }
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

        _hourlyChartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                for (i in this.indices) {
                    this[i] = this[i].copy(initialXAxisStart = initialHourlyXAxisStart)
                    this[i] = this[i].copy(initialXAxisEnd = initialHourlyXAxisEnd)
                }
            }
        }
        _dailyChartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                for (i in this.indices) {
                    this[i] = this[i].copy(initialDailyXAxisStart = initialDailyXAxisStart)
                    this[i] = this[i].copy(initialDailyXAxisEnd = initialDailyXAxisEnd)
                }
            }
        }
    }

    fun initializeXAxisBounds(pageIndex: Int){
        viewModelScope.launch {
            _hourlyChartState.update { chartStateList ->
                chartStateList.apply {
                    this[pageIndex].xAxisStart.snapTo(_hourlyChartState.value[pageIndex].initialXAxisStart)
                    this[pageIndex].xAxisEnd.snapTo(_hourlyChartState.value[pageIndex].initialXAxisEnd)
                    this[pageIndex].yAxesStarts.forEachIndexed { index, animatable ->
                        animatable.snapTo(initialYAxesStarts[pageIndex][index])
                    }
                    this[pageIndex].yAxesEnds.forEachIndexed { index, animatable ->
                        animatable.snapTo(initialYAxesEnds[pageIndex][index])
                    }
                }
            }
            _dailyChartState.update { chartStateList ->
                chartStateList.apply {
                    this[pageIndex].xAxisStart.snapTo(_dailyChartState.value[pageIndex].initialDailyXAxisStart)
                    this[pageIndex].xAxisEnd.snapTo(_dailyChartState.value[pageIndex].initialDailyXAxisEnd)
                }
            }
        }
    }

    fun updateHourlySettingsStatus(status: Boolean){
        _hourlyChartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                for (i in this.indices) {
                    this[i] = this[i].copy(settingsUpdated = status)
                }
            }
        }
    }

    fun updateDailySettingsStatus(status: Boolean){
        _dailyChartState.update { chartStateList ->
            chartStateList.toMutableList().apply {
                for (i in this.indices) {
                    this[i] = this[i].copy(settingsUpdated = status)
                }
            }
        }
    }

}