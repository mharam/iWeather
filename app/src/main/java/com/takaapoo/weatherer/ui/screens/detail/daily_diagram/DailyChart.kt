package com.takaapoo.weatherer.ui.screens.detail.daily_diagram

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.ChartState
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.home.toPx
import com.takaapoo.weatherer.ui.theme.DiagramDarkTheme
import com.takaapoo.weatherer.ui.theme.DiagramDarkThemeSecondary
import com.takaapoo.weatherer.ui.theme.DiagramGrid
import com.takaapoo.weatherer.ui.theme.DiagramLightTheme
import com.takaapoo.weatherer.ui.theme.DiagramLightThemeSecondary
import com.takaapoo.weatherer.ui.theme.OnDiagramDarkTheme
import com.takaapoo.weatherer.ui.theme.OnDiagramLightTheme
import com.takaapoo.weatherer.ui.viewModels.timeFontFamily
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
    dailyChartData: List<LocalDailyWeather> = emptyList(),
    utcOffset: Long?,
    chartState: ChartState,
    dailyChartState: DailyChartState,
    onUpdateDailyYAxis: (quantity: DailyWeatherQuantity) -> Unit = { _ -> },
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

    LaunchedEffect(key1 = dailyChartState.chartQuantity) {
        animateCurve(dailyChartState)
    }
    // This is needed for the first time detail screen opens
    LaunchedEffect(key1 = dailyChartData.isNotEmpty()) {
        if (dailyChartData.isNotEmpty()) {
            onUpdateDailyYAxis(dailyChartState.chartQuantity)
            animateCurve(dailyChartState)
        }
    }
    val data = dailyQuantityData(
        dailyChartData,
        utcOffset ?: 0,
        dailyChartState.chartQuantity
    )
    val dataMax = data.maxByOrNull { it?.endInclusive ?: -Float.MAX_VALUE }?.endInclusive ?: 1f
    val dataMin = data.minByOrNull { it?.start ?: Float.MAX_VALUE }?.start ?: 0f
    var barOrCurveGraph by rememberSaveable {
        mutableStateOf(false)
    }

    val firstPointX = dailyTimeToX(date = dailyChartData.getOrNull(0)?.time)?.toFloat()
    var curveValueMinAtIndicator: Float? = null
    var curveValueMaxAtIndicator: Float? = null
    if (!barOrCurveGraph && firstPointX != null) {
        val indicatorX = dailyChartState.xAxisStart.value + dailyChartState.sliderThumbPosition *
                (dailyChartState.xAxisEnd.value - dailyChartState.xAxisStart.value)
        data.getOrNull((indicatorX - firstPointX).roundToInt())?.let {
            curveValueMinAtIndicator = it.start
            curveValueMaxAtIndicator = it.endInclusive
            onUpdateCurveValueAtIndicator(curveValueMinAtIndicator!!, curveValueMaxAtIndicator!!)
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
        val appThemeDiagramSurfaceColor = if (isSystemInDarkTheme()) DiagramDarkTheme else DiagramLightTheme
        val appThemeDiagramSurfaceColorSecondary =
            if (isSystemInDarkTheme()) DiagramDarkThemeSecondary else DiagramLightThemeSecondary
        val appThemeDiagramOnSurfaceColor = if (isSystemInDarkTheme()) OnDiagramDarkTheme else OnDiagramLightTheme


        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val diagramWidth = size.width - 2 * diagramHorPadding
            val xAxisLength = dailyChartState.xAxisEnd.value - dailyChartState.xAxisStart.value
            barOrCurveGraph = (diagramWidth / xAxisLength).toDp() > 30.dp

            dailyDiagramFrame(
                xAxisRange = dailyChartState.xAxisStart.value .. dailyChartState.xAxisEnd.value,
                yAxesRanges = dailyChartState.yAxesStarts.value .. dailyChartState.yAxesEnds.value,
                cornerRadius = cornerRadius.toPx(),
                horizontalPadding = diagramHorPadding,
                verticalPadding = diagramVertPadding,
                mainBarColor = DiagramGrid,
                minorBarColor = DiagramGrid,
                minorBarVisible = chartState.chartGrid == ChartGrids.All,
                majorBarVisible = chartState.chartGrid != ChartGrids.NON,
                theme = chartState.chartTheme,
                chartQuantity = dailyChartState.chartQuantity,
                appSurfaceColor = appThemeDiagramSurfaceColor,
                appSurfaceColorSecondary = appThemeDiagramSurfaceColorSecondary,
                textMeasurer = textMeasurer,
                horizontalDashLinePhase = horizontalDashLinePhase,
                verticalDashLinePhase = verticalDashLinePhase,
                onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation
            )
        }
        if (dailyChartData.isNotEmpty()) {
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
                chartTheme = chartState.chartTheme,
                onAppSurfaceColor = appThemeDiagramOnSurfaceColor,
                cornerRadius = cornerRadius.toPx(density),
                horizontalPadding = diagramHorPadding,
                verticalPadding = diagramVertPadding,
                textMeasurer = textMeasurer,
                shadowVisible = chartState.curveShadowVisible,
                barOrCurveGraph = barOrCurveGraph,
                sliderPosition = dailyChartState.sliderThumbPosition,
            )
        }

        val chartUnit = dailyChartState.chartQuantity.unit(AppSettings()).trim('°', ' ')
        val chartTitle = stringResource(id = dailyChartState.chartQuantity.nameId) +
                if (chartUnit.isNotEmpty()) " [${chartUnit}]" else ""
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (dailyChartState.weatherConditionIconsVisible) {
                dailyDiagramWeatherIcon(
                    context = context,
                    dailyChartData = dailyChartData,
                    xAxisRange = dailyChartState.xAxisStart.value..dailyChartState.xAxisEnd.value,
                    cornerRadius = cornerRadius.toPx(density),
                    horizontalPadding = diagramHorPadding,
                    verticalPadding = diagramVertPadding
                )
            }
            dailyDiagramLegends(
                data = data,
                xAxisRange = dailyChartState.xAxisStart.value .. dailyChartState.xAxisEnd.value,
                yAxesRanges = dailyChartState.yAxesStarts.value .. dailyChartState.yAxesEnds.value,
                chartTheme = chartState.chartTheme,
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
                appSurfaceColor = appThemeDiagramSurfaceColor,
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
    return ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(date))
}


const val daySeconds = (24 * 60 * 60 - 1).toFloat()
private fun dailyQuantityData(
    dailyData: List<LocalDailyWeather?>,
    utcOffset: Long,
    weatherQuantity: DailyWeatherQuantity
): List<ClosedFloatingPointRange<Float>?>{
    return when (weatherQuantity){
        DailyWeatherQuantity.TEMPERATUREMINMAX -> dailyData.map {
            if (it?.temperatureMin != null && it.temperatureMax != null)
                it.temperatureMin .. it.temperatureMax
            else null
        }
        DailyWeatherQuantity.SUNRISESET -> dailyData.map {
            if (it?.sunRise != null && it.sunSet != null)
                (LocalDateTime.parse(it.sunRise).toLocalTime().toSecondOfDay() / daySeconds) * 24  ..
                        (LocalDateTime.parse(it.sunSet).toLocalTime().toSecondOfDay() / daySeconds) * 24
            else null
        }
        DailyWeatherQuantity.UVINDEXMAX -> dailyData.map {
            if (it?.uvIndexMax != null) 0f .. it.uvIndexMax
            else null
        }
        DailyWeatherQuantity.PRECIPITATIONSUM -> dailyData.map {
            if (it?.precipitationSum != null) 0f .. it.precipitationSum
            else null
        }
        DailyWeatherQuantity.PRECIPITATIONPROBABILITYMAX -> dailyData.map {
            if (it?.precipitationProbabilityMax != null) 0f .. it.precipitationProbabilityMax
            else null
        }
        DailyWeatherQuantity.WINDSPEEDMAX -> dailyData.map {
            if (it?.windSpeedMax != null) 0f .. it.windSpeedMax
            else null
        }
    }
}


fun DrawScope.drawThickText(
    textMeasurer: TextMeasurer,
    text: String,
    textColor: Color = Color.Unspecified,
    borderColor: Color? = null,
    textLeft: Float? = null,
    textRight: Float? = null,
    textCenter: Float? = null,
    textTop: Float? = null,
    textBottom: Float? = null,
    textMiddle: Float? = null,
    fontSize: TextUnit = 14.sp,
    fontFamily: FontFamily = Font(R.font.cmu_serif).toFontFamily(),
    borderWidth: Float = 1.5f,
    rectanglePadding: Dp = 4.dp,
    hasBackgroundRect: Boolean = false,
    backgroundColor: Color = Color.Unspecified,
    backgroundBorderColor: Color = Color.Unspecified,
    backgroundBorderWidth: Float = 4f
){
    val measuredTextOutline = measureText(
        textMeasurer = textMeasurer,
        text = text,
        fontSize = fontSize,
        fontFamily = fontFamily,
        color = borderColor ?: textColor,
        drawStyle = Stroke(width = borderWidth)
    )
    val measuredTextFill = measureText(
        textMeasurer = textMeasurer,
        text = text,
        fontSize = fontSize,
        fontFamily = fontFamily,
        color = textColor
    )

    val left = (textLeft ?: textCenter?.minus(measuredTextOutline.size.width / 2f)) ?:
    (textRight!! - measuredTextOutline.size.width)
    val top = (textTop ?: textMiddle?.minus(measuredTextOutline.size.height / 2f)) ?:
    (textBottom!! - measuredTextOutline.size.height)
    if (hasBackgroundRect) {
        val padding = rectanglePadding.toPx()
        val rectSize = Size(
            width = measuredTextOutline.size.width + 4 * padding,
            height = measuredTextOutline.size.height + 2 * padding
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(left - 2 * padding, top - padding),
            size = rectSize,
            cornerRadius = CornerRadius(measuredTextOutline.size.height / 2f + padding),
            blendMode = BlendMode.Clear
        )
        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(left - 2 * padding, top - padding),
            size = rectSize,
            cornerRadius = CornerRadius(measuredTextOutline.size.height / 2f + padding),
        )
        drawRoundRect(
            color = backgroundBorderColor,
            topLeft = Offset(left - 2 * padding, top - padding),
            size = rectSize,
            cornerRadius = CornerRadius(measuredTextOutline.size.height / 2f + padding),
            style = Stroke(backgroundBorderWidth)
        )
    }
    drawText(
        textLayoutResult = measuredTextOutline,
        topLeft = Offset(x = left, y = top)
    )
    drawText(
        textLayoutResult = measuredTextFill,
        topLeft = Offset(x = left, y = top)
    )
}

fun measureText(
    textMeasurer: TextMeasurer,
    text: String,
    fontSize: TextUnit = 14.sp,
    fontFamily: FontFamily = Font(R.font.cmu_serif).toFontFamily(),
    color: Color = Color.Unspecified,
    drawStyle: DrawStyle = Fill
): TextLayoutResult {
    return textMeasurer.measure(
        text = text,
        style = TextStyle(
            fontFamily = fontFamily,
            fontSize = fontSize,
            color = color,
            drawStyle = drawStyle
        )
    )
}

suspend fun animateCurve(dailyChartState: DailyChartState){
    dailyChartState.curveAnimator.animateTo(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 1200,
            delayMillis = 300,
            easing = LinearEasing
        )
    )
}