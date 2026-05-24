package com.takaapoo.weatherer.ui.screens.detail.daily_diagram

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.LocalHistoricDailyWeather
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.HistoricDailyChartDto
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.theme.timeFontFamily
import com.takaapoo.weatherer.utility.toPx
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


@Composable
fun DailyChart(
    modifier: Modifier = Modifier,
    diagramHorPadding: Float = 16f,
    diagramVertPadding: Float = 24f,
    dailyChartData: ImmutableList<LocalDailyWeather> = persistentListOf(),
    historicDailyChartData: ImmutableList<HistoricDailyChartDto> = persistentListOf(),
    appSettings: AppSettings,
//    utcOffset: Long?,
//    hourlyChartState: HourlyChartState,
    dailyChartState: DailyChartState,
    chartGrid: ChartGrids,
    chartTheme: ChartTheme,
    curveShadowVisible: Boolean,
    onUpdateDailyYAxis: (quantity: DailyWeatherQuantity, curveAnimatorInitialValue: Float) -> Unit = { _, _ -> },
    onMoveAxis: (Offset) -> Unit = {},
    onScaleAxis: (center: Offset, scaleX:Float, scaleY:Float) -> Unit = { _, _, _ -> },
    onScaleBack: (center: Offset) -> Unit = {},
    onCalculateHorizontalBarSeparation:
        (start: Float, end: Float, diagramHeight: Float, textMeasurer: TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation:
        (start: Float, end: Float, diagramWidth: Float, textMeasurer: TextMeasurer) -> Pair<Int, Float>,
    onUpdateCurveValueAtIndicator: (valueMin: Float, valueMax: Float) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val cornerRadius = dimensionResource(id = R.dimen.diagram_corner_radius)

    val textMeasurer: TextMeasurer = rememberTextMeasurer()
    var verticalDashLinePhase by remember{ mutableFloatStateOf(0f) }
    var horizontalDashLinePhase by remember{ mutableFloatStateOf(0f) }
    var firstTimeAddingCurve by rememberSaveable { mutableStateOf(true) }


    // This is needed for the first time detail screen opens
    LaunchedEffect(
        dailyChartData.isNotEmpty(),
        dailyChartState.chartQuantity,
        dailyChartState.settingsUpdated,
    ) {
        if (dailyChartData.isNotEmpty() && dailyChartState.settingsUpdated) {
            onUpdateDailyYAxis(
                dailyChartState.chartQuantity,
                if (firstTimeAddingCurve) 1f else 0f
            )
            dailyChartState.curveAnimator.snapTo(if (firstTimeAddingCurve) 1f else 0f)
            animateCurve(dailyChartState)

            firstTimeAddingCurve = false
        }
    }
    LaunchedEffect(
        dailyChartState.settingsUpdated,
        dailyChartState.xAxisAnimationTrigger,
        historicDailyChartData.getOrNull(0)?.weatherQuantity
    ) {
        if (!dailyChartState.xAxisAnimationTrigger)
            firstTimeAddingCurve = true
        else if (historicDailyChartData.isNotEmpty() && dailyChartState.settingsUpdated) {
            onUpdateDailyYAxis(
                dailyChartState.chartQuantity,
                if (firstTimeAddingCurve) 1f else 0f
            )
            dailyChartState.curveAnimator.snapTo(if (firstTimeAddingCurve) 1f else 0f)
            animateCurve(dailyChartState)

            firstTimeAddingCurve = false
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        firstTimeAddingCurve = true
    }

    val data = dailyQuantityData(
        dailyData = dailyChartData,
        historicDailyData = historicDailyChartData.map { it.dailyWeather },
        weatherQuantity = dailyChartState.chartQuantity
    )
    val dataMax = data.maxByOrNull { it?.endInclusive ?: -Float.MAX_VALUE }?.endInclusive ?: 1f
    val dataMin = data.minByOrNull { it?.start ?: Float.MAX_VALUE }?.start ?: 0f
    var barOrCurveGraph by rememberSaveable {
        mutableStateOf(false)
    }

    if (dailyChartData.getOrNull(0)?.time == null && historicDailyChartData.getOrNull(0)?.dailyWeather?.time == null)
        return
    val firstPointX = dailyTimeToX(
        date = dailyChartData.getOrNull(0)?.time ?: historicDailyChartData.getOrNull(0)?.dailyWeather?.time
    )?.toFloat()
    var curveValueMinAtIndicator: Float? = null
    var curveValueMaxAtIndicator: Float? = null
    if (/*!barOrCurveGraph && */firstPointX != null) {
        val indicatorX = dailyChartState.xAxisStart.value + dailyChartState.sliderThumbPosition *
                (dailyChartState.xAxisEnd.value - dailyChartState.xAxisStart.value)
        data.getOrNull((indicatorX - firstPointX).roundToInt())?.let {
            curveValueMinAtIndicator = it.start
            curveValueMaxAtIndicator = it.endInclusive
            onUpdateCurveValueAtIndicator(curveValueMinAtIndicator, curveValueMaxAtIndicator)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                val diagramRectangle = RoundRect(
                    left = diagramHorPadding,
                    top = diagramVertPadding,
                    right = size.width - diagramHorPadding,
                    bottom = size.height - diagramVertPadding,
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
                awaitEachGesture {
                    val firstPointer = awaitFirstDown().also { it.consume() }
                    if (diagramRectangle.contains(firstPointer.position)) {
                        var lastCentroid = Offset.Zero
                        do {
                            val event = awaitPointerEvent()
                            val pan = event.calculatePan()
                            verticalDashLinePhase -= pan.y
                            horizontalDashLinePhase -= pan.x
                            onMoveAxis(
                                Offset(
                                    x = pan.x / (size.width - 2 * diagramHorPadding),
                                    y = pan.y / (size.height - 2 * diagramVertPadding)
                                )
                            )
                            if (event.changes.size == 2) {
                                val centroid =
                                    (event.changes[0].position + event.changes[1].position) / 2f
                                val zoom = event.calculateZoom()
                                val (position1, position2) = event.changes.map { it.position }
                                val positionDifference = position1 - position2
                                val angel = atan2(
                                    y = positionDifference.y.absoluteValue,
                                    x = positionDifference.x.absoluteValue
                                )
                                onScaleAxis(
                                    Offset(
                                        x = centroid.x / size.width,
                                        y = centroid.y / size.height
                                    ),
                                    1 + (zoom - 1) * cos(angel),
                                    1 + (zoom - 1) * sin(angel)
                                )
                                lastCentroid = centroid
                            } else if (event.changes.size < 2 && event.changes.any { it.changedToUp() }) {
                                onScaleBack(
                                    Offset(
                                        x = lastCentroid.x / size.width,
                                        y = lastCentroid.y / size.height
                                    )
                                )
                            }
                            event.changes.forEach { it.consume() }
                        } while (event.changes.any { it.pressed })
                    }
                }
            }
    ) {
        val appThemeDiagramOnSurfaceColor = MaterialTheme.customColorScheme.appThemeDiagramOnSurfaceColor
        val appThemeDiagramSurfaceColor = MaterialTheme.customColorScheme.appThemeDiagramSurfaceColor

        DailyDiagramFrameCanvas(
            modifier = Modifier.fillMaxSize(),
            xAxisStart = dailyChartState.xAxisStart.value,
            xAxisEnd = dailyChartState.xAxisEnd.value,
            yAxesStarts = dailyChartState.yAxesStarts.value,
            yAxesEnds = dailyChartState.yAxesEnds.value,
            minorBarVisible = chartGrid == ChartGrids.All,
            majorBarVisible = chartGrid != ChartGrids.NON,
            cornerRadius = cornerRadius,
            diagramHorPadding = diagramHorPadding,
            diagramVertPadding = diagramVertPadding,
            chartTheme = chartTheme,
            chartQuantity = dailyChartState.chartQuantity,
            textMeasurer = textMeasurer,
            verticalDashLinePhase = verticalDashLinePhase,
            horizontalDashLinePhase = horizontalDashLinePhase,
            isLoading = dailyChartState.dataLoadingStatus is MyResult.Loading,
            onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
            onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation
        )

        if (data.isNotEmpty()) {
            DailyDiagramCurve(
                data = data,
                dataMax = dataMax,
                dataMin = dataMin,
                firstPointX = firstPointX,
                curveAnimatorProgress = dailyChartState.curveAnimator.value,
                weatherQuantity = dailyChartState.chartQuantity,
                xAxisRange = dailyChartState.xAxisStart.value..dailyChartState.xAxisEnd.value,
                yAxisRange = dailyChartState.yAxesStarts.value..dailyChartState.yAxesEnds.value,
                curveValueMinAtIndicator = curveValueMinAtIndicator,
                curveValueMaxAtIndicator = curveValueMaxAtIndicator,
                chartTheme = chartTheme,
                onAppSurfaceColor = appThemeDiagramOnSurfaceColor,
                cornerRadius = cornerRadius.toPx(density),
                horizontalPadding = diagramHorPadding,
                verticalPadding = diagramVertPadding,
                textMeasurer = textMeasurer,
                shadowVisible = curveShadowVisible,
                barOrCurveGraph = barOrCurveGraph,
                sliderPosition = dailyChartState.sliderThumbPosition,
            )
        }

        val chartUnit = dailyChartState.chartQuantity.unit(appSettings).trim('°', ' ')
        val chartTitle = stringResource(id = dailyChartState.chartQuantity.nameId) +
                if (chartUnit.isNotEmpty()) " [${chartUnit}]" else ""
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RectangleShape)   // This is to prevent DrawScope call unnecessarily
        ) {
            val diagramWidth = size.width - 2 * diagramHorPadding
            val xAxisLength = dailyChartState.xAxisEnd.value - dailyChartState.xAxisStart.value
            barOrCurveGraph = (diagramWidth / xAxisLength).toDp() > 30.dp

            if (dailyChartState.weatherConditionIconsVisible && firstPointX != null) {
                val weatherCodes = dailyChartData.map { it.weatherCode }.takeIf { it.isNotEmpty() }
                    ?: historicDailyChartData.map { it.dailyWeather?.weatherCode }
                dailyDiagramWeatherIcon(
                    context = context,
//                    dailyChartData = dailyChartData
                    weatherCodes = weatherCodes,
                    firstPointX = firstPointX,
                    xAxisRange = dailyChartState.xAxisStart.value..dailyChartState.xAxisEnd.value,
                    cornerRadius = cornerRadius.toPx(density),
                    horizontalPadding = diagramHorPadding,
                    verticalPadding = diagramVertPadding
                )
            }
            dailyDiagramLegends(
//                data = data,
                xAxisRange = dailyChartState.xAxisStart.value .. dailyChartState.xAxisEnd.value,
                yAxesRanges = dailyChartState.yAxesStarts.value .. dailyChartState.yAxesEnds.value,
                chartTheme = chartTheme,
                chartQuantity = dailyChartState.chartQuantity,
                chartTitle = chartTitle,
                cornerRadius = cornerRadius.toPx(),
                dataMax = dataMax,
                dataMin = dataMin,
                curveValueMinAtIndicator = curveValueMinAtIndicator,
                curveValueMaxAtIndicator = curveValueMaxAtIndicator,
                barOrCurveGraph = barOrCurveGraph,
                indicatorPosition = dailyChartState.sliderThumbPosition,
                horizontalPadding = diagramHorPadding,
                verticalPadding = diagramVertPadding,
                onAppSurfaceColor = appThemeDiagramOnSurfaceColor,
//                appSurfaceColor = appThemeDiagramSurfaceColor,
                textMeasurer = textMeasurer,
                timeFontFamily = timeFontFamily,
                onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation
            )
        }
    }
}

fun dailyTimeToX(date: String?): Long?{
    if (date == null) return null
    return ChronoUnit.DAYS.between(
        LocalDate.now(),
        LocalDate.parse(date)
    )
}


const val daySeconds = (24 * 60 * 60).toFloat()
fun dailyQuantityData(
    dailyData: List<LocalDailyWeather?> = emptyList(),
    historicDailyData: List<LocalHistoricDailyWeather?> = emptyList(),
    weatherQuantity: DailyWeatherQuantity
): List<ClosedFloatingPointRange<Float>?>{
    return when (weatherQuantity){
        DailyWeatherQuantity.TEMPERATUREMINMAX -> {
            dailyData.map {
                if (it?.temperatureMin != null && it.temperatureMax != null)
                    it.temperatureMin..it.temperatureMax
                else null
            }.takeIf { it.isNotEmpty() } ?:
            historicDailyData.map {
                if (it?.temperatureMin != null && it.temperatureMax != null)
                    it.temperatureMin..it.temperatureMax
                else null
            }
        }
        DailyWeatherQuantity.SUNRISESET -> dailyData.map {
            if (it?.sunRise != null && it.sunSet != null) {
                val sunRiseTime = it.sunRise.substringAfter('T')
                val sunSetTime = it.sunSet.substringAfter('T')
                if (sunRiseTime == "00:00" && sunSetTime == "00:00") return@map null

                val sunRiseIsYesterday = LocalDateTime.parse(it.sunRise).toLocalDate()
                    .isBefore(LocalDate.parse(it.time))
                val sunSetIsTomorrow = LocalDateTime.parse(it.sunSet).toLocalDate()
                    .isAfter(LocalDate.parse(it.time))

                val sunRiseHour = (LocalDateTime.parse(it.sunRise).toLocalTime().toSecondOfDay()
                        / daySeconds - if (sunRiseIsYesterday) 1 else 0) * 24
                val sunSetHour =(LocalDateTime.parse(it.sunSet).toLocalTime().toSecondOfDay()
                        / daySeconds + if (sunSetIsTomorrow) 1 else 0) * 24

                sunRiseHour..sunSetHour
            }
            else null
        }
        DailyWeatherQuantity.UVINDEXMAX -> dailyData.map {
            if (it?.uvIndexMax != null) 0f .. it.uvIndexMax
            else null
        }
        DailyWeatherQuantity.PRECIPITATIONSUM -> {
            dailyData.map {
                if (it?.precipitationSum != null) 0f..it.precipitationSum
                else null
            }.takeIf { it.isNotEmpty() } ?:
            historicDailyData.map {
                if (it?.precipitationSum != null) 0f..it.precipitationSum
                else null
            }
        }
        DailyWeatherQuantity.PRECIPITATIONPROBABILITYMAX -> dailyData.map {
            if (it?.precipitationProbabilityMax != null) 0f .. it.precipitationProbabilityMax
            else null
        }
        DailyWeatherQuantity.WINDSPEEDMAX -> {
            dailyData.map {
                if (it?.windSpeedMax != null) 0f..it.windSpeedMax
                else null
            }.takeIf { it.isNotEmpty() } ?:
            historicDailyData.map {
                if (it?.windSpeedMax != null) 0f..it.windSpeedMax
                else null
            }
        }
        DailyWeatherQuantity.TEMPERATUREMEAN -> {
            val minTemperature = historicDailyData.minByOrNull { it?.temperatureMean ?: 0f }
                ?.temperatureMean ?: 0f
            historicDailyData.map {
                if (it?.temperatureMean != null) minTemperature..it.temperatureMean
                else null
            }
        }
        DailyWeatherQuantity.CLOUDCOVERMEAN -> {
            historicDailyData.map {
                if (it?.cloudCoverMean != null) 0f..it.cloudCoverMean
                else null
            }
        }
        DailyWeatherQuantity.HUMIDITYMEAN -> {
            historicDailyData.map {
                if (it?.relativeHumidityMean != null) 0f..it.relativeHumidityMean
                else null
            }
        }
    }
}


suspend fun animateCurve(dailyChartState: DailyChartState){
    if (dailyChartState.curveAnimator.value != 1f) {
        dailyChartState.curveAnimator.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1200,
                delayMillis = 100,
                easing = LinearEasing
            )
        )
    }
}