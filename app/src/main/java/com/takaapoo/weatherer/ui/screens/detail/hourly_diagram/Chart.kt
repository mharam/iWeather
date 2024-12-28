package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import androidx.annotation.StringRes
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
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.theme.curveBlue
import com.takaapoo.weatherer.ui.theme.curveGreen
import com.takaapoo.weatherer.ui.theme.curveOrange
import com.takaapoo.weatherer.ui.theme.curvePink
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.utility.cmToUnit
import com.takaapoo.weatherer.ui.utility.kmphToUnit
import com.takaapoo.weatherer.ui.utility.mToUnit
import com.takaapoo.weatherer.ui.utility.mmToUnit
import com.takaapoo.weatherer.ui.utility.paToUnit
import com.takaapoo.weatherer.ui.utility.toAppropriateUnit
import com.takaapoo.weatherer.ui.utility.toPx
import com.takaapoo.weatherer.ui.viewModels.DetailViewModel
import com.takaapoo.weatherer.ui.viewModels.timeFontFamily
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


enum class DotType{ SQUARE, CIRCLE, TRIANGLE, DIAMOND }
enum class ChartTheme(@StringRes val nameId: Int){
    LIGHT(nameId = R.string.light_theme),
    DARK(nameId = R.string.dark_theme),
    APPTHEME(nameId = R.string.app_theme),
    DAYNIGHT(nameId = R.string.day_night)
}
enum class ChartGrids{ All, MAIN, NON }
val curveColors = listOf(curveBlue, curvePink, curveGreen, curveOrange)
val mainGridWidthDp = 0.6f.dp
val minorGridWidthDp = 0.4f.dp


@Composable
fun HourlyChart(
    modifier: Modifier = Modifier,
    diagramHorPadding: Float = 16f,
    diagramVertPadding: Float = 24f,
    hourlyChartData: ImmutableList<HourlyChartDto> = persistentListOf(),
    hourlyChartState: HourlyChartState = HourlyChartState(),
    appSettings: AppSettings,
    onAddYAxis: (start: Float, end: Float, diagramHeight: Float, textMeasurer: TextMeasurer,
                 axisIndex: Int?, curveAnimatorInitialValue: Float) -> Unit = { _ , _, _, _, _, _ -> },
    onMoveAxis: (Offset) -> Unit = {},
    onScaleAxis: (center:Offset, scaleX:Float, scaleY:Float) -> Unit = { _, _, _ -> },
    onScaleBack: (center:Offset) -> Unit = {},
    onCalculateHorizontalBarSeparation:
        (start: Float, end: Float, diagramHeight: Float, textMeasurer: TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation:
        (start: Float, end: Float, diagramWidth: Float, textMeasurer: TextMeasurer) -> Pair<Int, Float>,
    onUpdateCurveValueAtIndicator: (curveIndex: Int, value: Float?) -> Unit
) {
    val density = LocalDensity.current
    val cornerRadius = dimensionResource(id = R.dimen.diagram_corner_radius)

    val textMeasurer: TextMeasurer = rememberTextMeasurer()
    var verticalDashLinePhase by remember{ mutableFloatStateOf(0f) }
    var horizontalDashLinePhase by remember{ mutableFloatStateOf(0f) }
    val hourlyData = hourlyChartData.map { it.hourlyWeather }
    var firstTimeAddingCurve by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(key1 = hourlyChartState.curveAnimator.size) {
        hourlyChartState.curveAnimator.forEach {
            if (it.value < 1f){
                launch(Dispatchers.Default){
                    it.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 1500,
                            delayMillis = 200,
                            easing = LinearEasing
                        )
                    )
                }
            }
        }
    }

    val frameHeight = dimensionResource(R.dimen.diagram_height).toPx(density) - 2*diagramVertPadding
    LaunchedEffect(
        key1 = hourlyChartState.chartQuantities.size,
        key2 = hourlyChartState.settingsUpdated
    ) {
        if (hourlyChartState.settingsUpdated) {
            prepareAddCurve(
                hourlyChartState, hourlyChartData, appSettings, textMeasurer, frameHeight,
                curveAnimatorInitialValue = if (firstTimeAddingCurve) 1f else 0f, onAddYAxis
            )
            firstTimeAddingCurve = false
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        firstTimeAddingCurve = true
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
        val sunRiseSetX = hourlyChartData.map { it.sunRise to it.sunSet }.distinct()
            .map { timeToX(it.first, 0) to timeToX(it.second, 0) }.toImmutableList()

        DiagramFrameCanvas(
            modifier = Modifier.fillMaxSize(),
            sunRiseSetX = sunRiseSetX,
            xAxisStart = hourlyChartState.xAxisStart.value,
            xAxisEnd = hourlyChartState.xAxisEnd.value,
            yAxesStarts = hourlyChartState.yAxesStarts.map { it.value }.toImmutableList(),
            yAxesEnds = hourlyChartState.yAxesEnds.map { it.value }.toImmutableList(),
            minorBarVisible = hourlyChartState.chartGrid == ChartGrids.All,
            majorBarVisible = hourlyChartState.chartGrid != ChartGrids.NON,
            cornerRadius = cornerRadius,
            diagramHorPadding = diagramHorPadding,
            diagramVertPadding = diagramVertPadding,
            hourlyChartTheme = hourlyChartState.chartTheme,
            textMeasurer = textMeasurer,
            verticalDashLinePhase = verticalDashLinePhase,
            horizontalDashLinePhase = horizontalDashLinePhase,
            onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
            onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation
        )

        if (hourlyData.isNotEmpty()) {
            val time = hourlyData
                .map { it?.time }
                .map { LocalDateTime.parse(it) }
                .map { it.dayOfMonth * 24 + it.hour }

            hourlyChartState.chartQuantities.forEachIndexed { curveNumber, weatherQuantity ->
                hourlyChartState.yAxesStarts.getOrNull(curveNumber)?.let {
                    DiagramCurve(
                        hourlyData = hourlyChartData,
                        curveAnimatorProgress = hourlyChartState.curveAnimator.getOrNull(curveNumber)?.value
                            ?: 0f,
                        timeData = time,
                        weatherQuantity = weatherQuantity,
                        initialXAxisRange = hourlyChartState.initialXAxisStart..hourlyChartState.initialXAxisEnd,
                        xAxisRange = hourlyChartState.xAxisStart.value..hourlyChartState.xAxisEnd.value,
                        yAxisRange = it.value..hourlyChartState.yAxesEnds[curveNumber].value,
                        cornerRadius = cornerRadius.toPx(density),
                        horizontalPadding = diagramHorPadding,
                        verticalPadding = diagramVertPadding,
                        curveColor = curveColors[curveNumber],
                        dotsVisible = hourlyChartState.dotsOnCurveVisible,
                        shadowVisible = hourlyChartState.curveShadowVisible,
                        dotType = DotType.entries[curveNumber],
                        sliderPosition = hourlyChartState.sliderThumbPosition,
                        onUpdateCurveValueAtIndicator = { value ->
                            onUpdateCurveValueAtIndicator(curveNumber, value)
                        }
                    )
                }
            }
        }
        DiagramIconsAndLegends(
            modifier = Modifier.fillMaxSize(),
            sunRiseSetIconsVisible = hourlyChartState.sunRiseSetIconsVisible,
            weatherConditionIconsVisible = hourlyChartState.weatherConditionIconsVisible,
            sunRiseSetX = sunRiseSetX,
            xAxisStart = hourlyChartState.xAxisStart.value,
            xAxisEnd = hourlyChartState.xAxisEnd.value,
            yAxesStarts = hourlyChartState.yAxesStarts.map { it.value }.toImmutableList(),
            yAxesEnds = hourlyChartState.yAxesEnds.map { it.value }.toImmutableList(),
            cornerRadius = cornerRadius,
            diagramHorPadding = diagramHorPadding,
            diagramVertPadding = diagramVertPadding,
            hourlyChartData = hourlyChartData,
            sliderThumbPosition = hourlyChartState.sliderThumbPosition,
            hourlyChartTheme = hourlyChartState.chartTheme,
            textMeasurer = textMeasurer,
            onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
            onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation
        )
    }
}

@Composable
fun DiagramIconsAndLegends(
    modifier: Modifier = Modifier,
    sunRiseSetIconsVisible: Boolean,
    weatherConditionIconsVisible: Boolean,
    sunRiseSetX: ImmutableList<Pair<Float?, Float?>>,
    xAxisStart: Float,
    xAxisEnd: Float,
    yAxesStarts: ImmutableList<Float>,
    yAxesEnds: ImmutableList<Float>,
    cornerRadius: Dp,
    diagramHorPadding: Float,
    diagramVertPadding: Float,
    hourlyChartData: ImmutableList<HourlyChartDto>,
    hourlyChartTheme: ChartTheme,
    sliderThumbPosition: Float,
    textMeasurer: TextMeasurer,
    onCalculateHorizontalBarSeparation:
        (start: Float, end: Float, diagramHeight: Float, textMeasurer: TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation:
        (start: Float, end: Float, diagramWidth: Float, textMeasurer: TextMeasurer) -> Pair<Int, Float>
) {
    val context = LocalContext.current
    val appThemeDiagramOnSurfaceColor = MaterialTheme.customColorScheme.appThemeDiagramOnSurfaceColor
    val legendTextColor = MaterialTheme.customColorScheme.onDetailScreenSurface

    Canvas(
        modifier = modifier.fillMaxSize()
            .clip(RectangleShape)   // This is to prevent DrawScope call unnecessarily
    ) {
        if (sunRiseSetIconsVisible) {
            diagramSunRiseSetIcon(
                context = context,
                sunRiseSetX = sunRiseSetX,
                xAxisRange = xAxisStart .. xAxisEnd,
                cornerRadius = cornerRadius.toPx(),
                horizontalPadding = diagramHorPadding,
                verticalPadding = diagramVertPadding
            )
        }
        if (weatherConditionIconsVisible) {
            diagramWeatherIcon(
                context = context,
                hourlyChartData = hourlyChartData,
                xAxisRange = xAxisStart .. xAxisEnd,
                cornerRadius = cornerRadius.toPx(),
                horizontalPadding = diagramHorPadding,
                verticalPadding = diagramVertPadding
            )
        }
        diagramLegends(
            xAxisRange = xAxisStart .. xAxisEnd,
            yAxesRanges = List(size = yAxesStarts.size){
                yAxesStarts[it] .. yAxesEnds[it]
            },
            yAxesColors = curveColors,
            chartTheme = hourlyChartTheme,
            sunRiseSetX = sunRiseSetX,
            cornerRadius = cornerRadius.toPx(),
            indicatorPosition = sliderThumbPosition,
            horizontalPadding = diagramHorPadding,
            verticalPadding = diagramVertPadding,
            onAppSurfaceColor = appThemeDiagramOnSurfaceColor,
            textMeasurer = textMeasurer,
            textColor = legendTextColor,
            timeFontFamily = timeFontFamily,
            onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
            onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation
        )
    }
}


fun quantityData(
    hourlyChartData: List<HourlyChartDto?>? = null,
    weatherData: List<LocalHourlyWeather?>? = null,
    airData: List<LocalAirQuality?>? = null,
    weatherQuantity: WeatherQuantity
): List<Float?>{
    val hourlyWeatherData = hourlyChartData?.map { it?.hourlyWeather } ?: weatherData ?: emptyList()
    val hourlyAirData = hourlyChartData?.map { it?.airQuality } ?: airData ?: emptyList()

    return when (weatherQuantity){
        WeatherQuantity.TEMPERATURE -> hourlyWeatherData.map { it?.temperature }
        WeatherQuantity.HUMIDITY -> hourlyWeatherData.map { it?.humidity }
        WeatherQuantity.DEWPOINT -> hourlyWeatherData.map { it?.dewPoint }
        WeatherQuantity.APPARENTTEMP -> hourlyWeatherData.map { it?.apparentTemperature }
        WeatherQuantity.PRECIPITATIONPROBABILITY -> hourlyWeatherData.map { it?.precipitationProbability }
        WeatherQuantity.PRECIPITATION -> hourlyWeatherData.map { it?.precipitation }
        WeatherQuantity.RAIN -> hourlyWeatherData.map { it?.rain }
        WeatherQuantity.SHOWERS -> hourlyWeatherData.map { it?.showers }
        WeatherQuantity.SNOWFALL -> hourlyWeatherData.map { it?.snowfall }
        WeatherQuantity.CLOUDCOVER -> hourlyWeatherData.map { it?.cloudCover }
        WeatherQuantity.SURFACEPRESSURE -> hourlyWeatherData.map { it?.surfacePressure }
        WeatherQuantity.VISIBILITY -> hourlyWeatherData.map { it?.visibility }
        WeatherQuantity.WINDSPEED -> hourlyWeatherData.map { it?.windSpeed }
        WeatherQuantity.WINDDIRECTION -> hourlyWeatherData.map { it?.windDirection }
        WeatherQuantity.UVINDEX -> hourlyWeatherData.map { it?.uvIndex }
        WeatherQuantity.FREEZINGLEVELHEIGHT -> hourlyWeatherData.map { it?.freezingLevelHeight }
        WeatherQuantity.DIRECTRADIATION -> hourlyWeatherData.map { it?.directRadiation }
        WeatherQuantity.DIRECTNORMALIRRADIANCE -> hourlyWeatherData.map { it?.directNormalIrradiance }
        WeatherQuantity.AQI -> hourlyAirData.map { it?.airQuality?.toFloat() }
        WeatherQuantity.PM10 -> hourlyAirData.map { it?.pm10 }
        WeatherQuantity.PM2_5 -> hourlyAirData.map { it?.pm25 }
        WeatherQuantity.CO -> hourlyAirData.map { it?.carbonMonoxide }
        WeatherQuantity.NO2 -> hourlyAirData.map { it?.nitrogenDioxide }
        WeatherQuantity.SO2 -> hourlyAirData.map { it?.sulphurDioxide }
        WeatherQuantity.Ozone -> hourlyAirData.map { it?.ozone }
    }
}

fun quantityControlPoints(
    hourlyChartData: List<HourlyChartDto?>? = null,
    weatherData: List<LocalHourlyWeather?>? = null,
    airData: List<LocalAirQuality?>? = null,
    weatherQuantity: WeatherQuantity
): Pair<List<Offset?>, List<Offset?>>{
    val hourlyWeatherData = hourlyChartData?.map { it?.hourlyWeather } ?: weatherData ?: emptyList()
    val airQualityData = hourlyChartData?.map { it?.airQuality } ?: airData ?: emptyList()

    return when (weatherQuantity){
        WeatherQuantity.TEMPERATURE -> hourlyWeatherData.map {
            if (it?.temperatureControl1X != null && it.temperatureControl1Y != null)
                Offset(it.temperatureControl1X, it.temperatureControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.temperatureControl2X != null && it.temperatureControl2Y != null)
                Offset(it.temperatureControl2X, it.temperatureControl2Y)
            else null }
        WeatherQuantity.HUMIDITY -> hourlyWeatherData.map {
            if (it?.humidityControl1X != null && it.humidityControl1Y != null)
                Offset(it.humidityControl1X, it.humidityControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.humidityControl2X != null && it.humidityControl2Y != null)
                Offset(it.humidityControl2X, it.humidityControl2Y)
            else null }
        WeatherQuantity.DEWPOINT -> hourlyWeatherData.map {
            if (it?.dewPointControl1X != null && it.dewPointControl1Y != null)
                Offset(it.dewPointControl1X, it.dewPointControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.dewPointControl2X != null && it.dewPointControl2Y != null)
                Offset(it.dewPointControl2X, it.dewPointControl2Y)
            else null }
        WeatherQuantity.APPARENTTEMP -> hourlyWeatherData.map {
            if (it?.apparentTemperatureControl1X != null && it.apparentTemperatureControl1Y != null)
                Offset(it.apparentTemperatureControl1X, it.apparentTemperatureControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.apparentTemperatureControl2X != null && it.apparentTemperatureControl2Y != null)
                Offset(it.apparentTemperatureControl2X, it.apparentTemperatureControl2Y)
            else null }
        WeatherQuantity.PRECIPITATIONPROBABILITY -> hourlyWeatherData.map {
            if (it?.precipitationProbabilityControl1X != null && it.precipitationProbabilityControl1Y != null)
                Offset(it.precipitationProbabilityControl1X, it.precipitationProbabilityControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.precipitationProbabilityControl2X != null && it.precipitationProbabilityControl2Y != null)
                Offset(it.precipitationProbabilityControl2X, it.precipitationProbabilityControl2Y)
            else null }
        WeatherQuantity.CLOUDCOVER -> hourlyWeatherData.map {
            if (it?.cloudCoverControl1X != null && it.cloudCoverControl1Y != null)
                Offset(it.cloudCoverControl1X, it.cloudCoverControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.cloudCoverControl2X != null && it.cloudCoverControl2Y != null)
                Offset(it.cloudCoverControl2X, it.cloudCoverControl2Y)
            else null }
        WeatherQuantity.SURFACEPRESSURE -> hourlyWeatherData.map {
            if (it?.surfacePressureControl1X != null && it.surfacePressureControl1Y != null)
                Offset(it.surfacePressureControl1X, it.surfacePressureControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.surfacePressureControl2X != null && it.surfacePressureControl2Y != null)
                Offset(it.surfacePressureControl2X, it.surfacePressureControl2Y)
            else null }
        WeatherQuantity.VISIBILITY -> hourlyWeatherData.map {
            if (it?.visibilityControl1X != null && it.visibilityControl1Y != null)
                Offset(it.visibilityControl1X, it.visibilityControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.visibilityControl2X != null && it.visibilityControl2Y != null)
                Offset(it.visibilityControl2X, it.visibilityControl2Y)
            else null }
        WeatherQuantity.WINDSPEED -> hourlyWeatherData.map {
            if (it?.windSpeedControl1X != null && it.windSpeedControl1Y != null)
                Offset(it.windSpeedControl1X, it.windSpeedControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.windSpeedControl2X != null && it.windSpeedControl2Y != null)
                Offset(it.windSpeedControl2X, it.windSpeedControl2Y)
            else null }
        WeatherQuantity.WINDDIRECTION -> hourlyWeatherData.map {
            if (it?.windDirectionControl1X != null && it.windDirectionControl1Y != null)
                Offset(it.windDirectionControl1X, it.windDirectionControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.windDirectionControl2X != null && it.windDirectionControl2Y != null)
                Offset(it.windDirectionControl2X, it.windDirectionControl2Y)
            else null }
        WeatherQuantity.UVINDEX -> hourlyWeatherData.map {
            if (it?.uvIndexControl1X != null && it.uvIndexControl1Y != null)
                Offset(it.uvIndexControl1X, it.uvIndexControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.uvIndexControl2X != null && it.uvIndexControl2Y != null)
                Offset(it.uvIndexControl2X, it.uvIndexControl2Y)
            else null }
        WeatherQuantity.FREEZINGLEVELHEIGHT -> hourlyWeatherData.map {
            if (it?.freezingLevelHeightControl1X != null && it.freezingLevelHeightControl1Y != null)
                Offset(it.freezingLevelHeightControl1X, it.freezingLevelHeightControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.freezingLevelHeightControl2X != null && it.freezingLevelHeightControl2Y != null)
                Offset(it.freezingLevelHeightControl2X, it.freezingLevelHeightControl2Y)
            else null }
        WeatherQuantity.DIRECTRADIATION -> hourlyWeatherData.map {
            if (it?.directRadiationControl1X != null && it.directRadiationControl1Y != null)
                Offset(it.directRadiationControl1X, it.directRadiationControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.directRadiationControl2X != null && it.directRadiationControl2Y != null)
                Offset(it.directRadiationControl2X, it.directRadiationControl2Y)
            else null }
        WeatherQuantity.DIRECTNORMALIRRADIANCE -> hourlyWeatherData.map {
            if (it?.directNormalIrradianceControl1X != null && it.directNormalIrradianceControl1Y != null)
                Offset(it.directNormalIrradianceControl1X, it.directNormalIrradianceControl1Y)
            else null } to hourlyWeatherData.map {
            if (it?.directNormalIrradianceControl2X != null && it.directNormalIrradianceControl2Y != null)
                Offset(it.directNormalIrradianceControl2X, it.directNormalIrradianceControl2Y)
            else null }
        WeatherQuantity.AQI -> airQualityData.map {
            if (it?.aqiControl1X != null && it.aqiControl1Y != null) Offset(it.aqiControl1X, it.aqiControl1Y)
            else null } to airQualityData.map {
            if (it?.aqiControl2X != null && it.aqiControl2Y != null) Offset(it.aqiControl2X, it.aqiControl2Y)
            else null }
        WeatherQuantity.PM10 -> airQualityData.map {
            if (it?.pm10Control1X != null && it.pm10Control1Y != null) Offset(it.pm10Control1X, it.pm10Control1Y)
            else null } to airQualityData.map {
            if (it?.pm10Control2X != null && it.pm10Control2Y != null) Offset(it.pm10Control2X, it.pm10Control2Y)
            else null }
        WeatherQuantity.PM2_5 -> airQualityData.map {
            if (it?.pm25Control1X != null && it.pm25Control1Y != null) Offset(it.pm25Control1X, it.pm25Control1Y)
            else null } to airQualityData.map {
            if (it?.pm25Control2X != null && it.pm25Control2Y != null) Offset(it.pm25Control2X, it.pm25Control2Y)
            else null }
        WeatherQuantity.CO -> airQualityData.map {
            if (it?.coControl1X != null && it.coControl1Y != null) Offset(it.coControl1X, it.coControl1Y)
            else null } to airQualityData.map {
            if (it?.coControl2X != null && it.coControl2Y != null) Offset(it.coControl2X, it.coControl2Y)
            else null }
        WeatherQuantity.NO2 -> airQualityData.map {
            if (it?.no2Control1X != null && it.no2Control1Y != null) Offset(it.no2Control1X, it.no2Control1Y)
            else null } to airQualityData.map {
            if (it?.no2Control2X != null && it.no2Control2Y != null) Offset(it.no2Control2X, it.no2Control2Y)
            else null }
        WeatherQuantity.SO2 -> airQualityData.map {
            if (it?.so2Control1X != null && it.so2Control1Y != null) Offset(it.so2Control1X, it.so2Control1Y)
            else null } to airQualityData.map {
            if (it?.so2Control2X != null && it.so2Control2Y != null) Offset(it.so2Control2X, it.so2Control2Y)
            else null }
        WeatherQuantity.Ozone -> airQualityData.map {
            if (it?.o3Control1X != null && it.o3Control1Y != null) Offset(it.o3Control1X, it.o3Control1Y)
            else null } to airQualityData.map {
            if (it?.o3Control2X != null && it.o3Control2Y != null) Offset(it.o3Control2X, it.o3Control2Y)
            else null }
        else -> hourlyWeatherData.map { null } to hourlyWeatherData.map { null }
    }
}

private fun quantityMinMaxModifier(
    weatherQuantity: WeatherQuantity,
    quantityMin: Float,
    quantityMax: Float,
    appSettings: AppSettings
): Pair<Float, Float> {
    val rangeFivePercentage = (quantityMax - quantityMin) / 20
    return when (weatherQuantity) {
        WeatherQuantity.TEMPERATURE, WeatherQuantity.DEWPOINT, WeatherQuantity.APPARENTTEMP -> {
            val coefficient = if (appSettings.temperatureUnit == Temperature.FAHRENHEIT) (9/5) else 1
            if (quantityMax - quantityMin < 3 * coefficient)
                quantityMin - 5 * coefficient to quantityMax + 5 * coefficient
            else
                quantityMin - rangeFivePercentage to quantityMax + rangeFivePercentage
        }
        WeatherQuantity.HUMIDITY, WeatherQuantity.PRECIPITATIONPROBABILITY, WeatherQuantity.CLOUDCOVER -> {
            if (quantityMax - quantityMin < 10)
                (quantityMin - 2) to (quantityMax + 5).coerceAtMost(100f)
            else
                (quantityMin - rangeFivePercentage) to
                        (quantityMax + rangeFivePercentage).coerceAtMost(100f)
        }
        WeatherQuantity.PRECIPITATION, WeatherQuantity.RAIN, WeatherQuantity.SHOWERS -> {
            if (quantityMax - quantityMin < 3f.mmToUnit(appSettings.lengthUnit))
                (-0.5f).mmToUnit(appSettings.lengthUnit) to quantityMax + 0.5f.mmToUnit(appSettings.lengthUnit)
            else
                (quantityMin - rangeFivePercentage) to (quantityMax + rangeFivePercentage)
        }
        WeatherQuantity.SNOWFALL -> {
            if (quantityMax - quantityMin < 3f.cmToUnit(appSettings.lengthUnit))
                (-0.5f).cmToUnit(appSettings.lengthUnit) to (quantityMax + 0.5f.cmToUnit(appSettings.lengthUnit))
            else
                (quantityMin - rangeFivePercentage) to (quantityMax + rangeFivePercentage)
        }
        WeatherQuantity.SURFACEPRESSURE -> {
            if (quantityMax - quantityMin < 20f.paToUnit(appSettings.pressureUnit))
                (quantityMin - 10f.paToUnit(appSettings.pressureUnit)).coerceAtLeast(0f) to
                        (quantityMax + 10f.paToUnit(appSettings.pressureUnit))
            else
                (quantityMin - rangeFivePercentage) to (quantityMax + rangeFivePercentage)
        }
        WeatherQuantity.VISIBILITY -> {
            if (quantityMax - quantityMin < 5)
                (quantityMin - 5).coerceAtLeast(0f) to (quantityMax + 5)
            else
                (quantityMin - rangeFivePercentage).coerceAtLeast(0f) to (quantityMax + rangeFivePercentage)
        }
        WeatherQuantity.WINDSPEED -> {
            if (quantityMax - quantityMin < 5f.kmphToUnit(appSettings.speedUnit))
                (quantityMin - 5f.kmphToUnit(appSettings.speedUnit)).coerceAtLeast(0f) to
                        (quantityMax + 5f.kmphToUnit(appSettings.speedUnit))
            else
                (quantityMin - rangeFivePercentage).coerceAtLeast(0f) to (quantityMax + rangeFivePercentage)
        }
        WeatherQuantity.WINDDIRECTION -> {
            if (quantityMax - quantityMin < 5)
                (quantityMin - 5).coerceAtLeast(0f) to (quantityMax + 5).coerceAtMost(360f)
            else
                quantityMin to quantityMax
        }
        WeatherQuantity.UVINDEX -> {
            if (quantityMax - quantityMin < 3)
                (quantityMin - 2).coerceAtLeast(0f) to (quantityMax + 2).coerceAtMost(12f)
            else
                (quantityMin - rangeFivePercentage).coerceAtLeast(0f) to
                        (quantityMax + rangeFivePercentage).coerceAtMost(12f)
        }
        WeatherQuantity.FREEZINGLEVELHEIGHT -> {
            if (quantityMax - quantityMin < 300f.mToUnit(appSettings.lengthUnit))
                (quantityMin - 500f.mToUnit(appSettings.lengthUnit)).coerceAtLeast(0f) to
                        (quantityMax + 500f.mToUnit(appSettings.lengthUnit))
            else
                (quantityMin - rangeFivePercentage).coerceAtLeast(0f) to (quantityMax + rangeFivePercentage)
        }
        WeatherQuantity.DIRECTRADIATION, WeatherQuantity.DIRECTNORMALIRRADIANCE -> {
            if (quantityMax - quantityMin < 100)
                (quantityMin - 250).coerceAtLeast(0f) to (quantityMax + 250)
            else
                (quantityMin - rangeFivePercentage) to (quantityMax + rangeFivePercentage)
        }
        else -> {
            if (quantityMax - quantityMin < 3)
                (quantityMin - 3).coerceAtLeast(0f) to (quantityMax + 3)
            else
                (quantityMin - rangeFivePercentage) to (quantityMax + rangeFivePercentage)
        }
    }
}

private fun prepareAddCurve(
    hourlyChartState: HourlyChartState,
    hourlyChartData: List<HourlyChartDto?>,
    appSettings: AppSettings,
    textMeasurer: TextMeasurer,
    frameHeight: Float,
    curveAnimatorInitialValue: Float,
    onAddYAxis: (Float, Float, Float, TextMeasurer, Int?, Float) -> Unit
){
    hourlyChartState.chartQuantities.forEachIndexed { curveNumber, weatherQuantity ->
        if (hourlyChartState.yAxesStarts.getOrNull(curveNumber) == null) {
            val apparentTempIndex =
                hourlyChartState.chartQuantities.indexOf(WeatherQuantity.APPARENTTEMP)
            val tempIndex =
                hourlyChartState.chartQuantities.indexOf(WeatherQuantity.TEMPERATURE)
            when {
                weatherQuantity == WeatherQuantity.TEMPERATURE && -1 < apparentTempIndex &&
                        apparentTempIndex < curveNumber ->
                    onAddYAxis(0f, 0f, 0f, textMeasurer, apparentTempIndex, curveAnimatorInitialValue)

                weatherQuantity == WeatherQuantity.APPARENTTEMP && -1 < tempIndex &&
                        tempIndex < curveNumber ->
                    onAddYAxis(0f, 0f, 0f, textMeasurer, tempIndex, curveAnimatorInitialValue)

                else -> {
                    val data = quantityData(
                        hourlyChartData = hourlyChartData,
                        weatherQuantity = weatherQuantity
                    )
                    val quantityMax = data.maxOfOrNull { it ?: Float.MIN_VALUE }
                    val quantityMin = data.minOfOrNull { it ?: Float.MAX_VALUE }
                    if (quantityMin != null && quantityMax != null) {
                        val (modifiedQuantityMin, modifiedQuantityMax) =
                            quantityMinMaxModifier(
                                weatherQuantity,
                                quantityMin,
                                quantityMax,
                                appSettings
                            )
                        onAddYAxis(
                            modifiedQuantityMin,
                            modifiedQuantityMax,
                            frameHeight,
                            textMeasurer,
                            null,
                            curveAnimatorInitialValue
                        )
                    }
                }
            }
        }
    }
}

private fun Pair<Float, Float>.toAppropriateUnit(quantity: WeatherQuantity, appSettings: AppSettings) =
    first.toAppropriateUnit(quantity, appSettings) to second.toAppropriateUnit(quantity, appSettings)

fun timeToX(time: String?, utcOffset: Long?): Float?{
    if (time == null || utcOffset == null) return null
    return Duration.between(
        LocalDate.now().atStartOfDay(),
        LocalDateTime.parse(time)
    ).toSeconds()/3600f + utcOffset/60f
}




@Preview(showBackground = true)
@Composable
fun LinearChartPreview(
    modifier: Modifier = Modifier,
    detailViewModel: DetailViewModel = hiltViewModel()
) {
    HourlyChart(
        appSettings = AppSettings(),
        onCalculateHorizontalBarSeparation = detailViewModel::calculateHorizontalBarSeparation,
        onCalculateVerticalBarSeparation = detailViewModel::calculateVerticalBarSeparation,
        onUpdateCurveValueAtIndicator = { _, _ -> }
    )
}