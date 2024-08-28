package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import androidx.annotation.StringRes
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.domain.model.ChartState
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.home.toPx
import com.takaapoo.weatherer.ui.theme.DiagramDarkTheme
import com.takaapoo.weatherer.ui.theme.DiagramGrid
import com.takaapoo.weatherer.ui.theme.DiagramLightTheme
import com.takaapoo.weatherer.ui.theme.OnDiagramDarkTheme
import com.takaapoo.weatherer.ui.theme.OnDiagramLightTheme
import com.takaapoo.weatherer.ui.theme.curveBlue
import com.takaapoo.weatherer.ui.theme.curveGreen
import com.takaapoo.weatherer.ui.theme.curveOrange
import com.takaapoo.weatherer.ui.theme.curvePink
import com.takaapoo.weatherer.ui.viewModels.DetailViewModel
import com.takaapoo.weatherer.ui.viewModels.timeFontFamily
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
    hourlyChartData: List<HourlyChartDto> = emptyList(),
    chartState: ChartState = ChartState(),
    onAddYAxis: (start: Float, end: Float, diagramHeight: Float, textMeasurer: TextMeasurer, axisIndex: Int?) ->
    Unit = { _ , _, _, _, _ -> },
    onMoveAxis: (Offset) -> Unit = {},
    onScaleAxis: (center:Offset, scaleX:Float, scaleY:Float) -> Unit = { _, _, _ -> },
    onScaleBack: (center:Offset) -> Unit = {},
    onCalculateHorizontalBarSeparation:
        (start: Float, end: Float, diagramHeight: Float, textMeasurer: TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation:
        (start: Float, end: Float, diagramWidth: Float, textMeasurer: TextMeasurer) -> Pair<Int, Float>,
    onUpdateCurveValueAtIndicator: (curveIndex: Int, value: Float) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val cornerRadius = dimensionResource(id = R.dimen.diagram_corner_radius)

    val textMeasurer: TextMeasurer = rememberTextMeasurer()
    var verticalDashLinePhase by remember{ mutableFloatStateOf(0f) }
    var horizontalDashLinePhase by remember{ mutableFloatStateOf(0f) }
    val hourlyData = hourlyChartData.map { it.hourlyWeather }

    LaunchedEffect(key1 = chartState.curveAnimator.size) {
        chartState.curveAnimator.lastOrNull()?.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                delayMillis = 500,
                easing = LinearEasing
            )
        )
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
        val appThemeDiagramOnSurfaceColor = if (isSystemInDarkTheme()) OnDiagramDarkTheme else OnDiagramLightTheme
        val sunRiseSetX = hourlyChartData.map { it.sunRise to it.sunSet }.distinct()
            .map { timeToX(it.first, 0) to timeToX(it.second, 0) }
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            chartState.chartQuantities.forEachIndexed { curveNumber, weatherQuantity ->
                if (chartState.yAxesStarts.getOrNull(curveNumber) == null) {
                    when {
                        weatherQuantity == WeatherQuantity.TEMPERATURE &&
                                chartState.chartQuantities.contains(WeatherQuantity.APPARENTTEMP) ->
                            onAddYAxis(0f, 0f, 0f, textMeasurer,
                                chartState.chartQuantities.indexOf(WeatherQuantity.APPARENTTEMP))
                        weatherQuantity == WeatherQuantity.APPARENTTEMP &&
                                chartState.chartQuantities.contains(WeatherQuantity.TEMPERATURE) ->
                            onAddYAxis(0f, 0f, 0f, textMeasurer,
                                chartState.chartQuantities.indexOf(WeatherQuantity.TEMPERATURE))
                        else -> {
                            val data = quantityData(
                                hourlyChartData = hourlyChartData,
                                weatherQuantity = weatherQuantity
                            )
                            val quantityMax = data.maxOfOrNull { it ?: Float.MIN_VALUE }
                            val quantityMin = data.minOfOrNull { it ?: Float.MAX_VALUE }
                            if (quantityMin != null && quantityMax != null) {
                                val (modifiedQuantityMin, modifiedQuantityMax) =
                                    quantityMinMaxModifier(weatherQuantity, quantityMin, quantityMax)
                                onAddYAxis(modifiedQuantityMin, modifiedQuantityMax,
                                    size.height - 2*diagramVertPadding, textMeasurer, null)
                            }
                        }
                    }
                }
            }
            diagramFrame(
                sunRiseSetX = sunRiseSetX,
                xAxisRange = chartState.xAxisStart.value .. chartState.xAxisEnd.value,
                yAxesRanges = List(size = chartState.yAxesStarts.size){
                    chartState.yAxesStarts[it].value .. chartState.yAxesEnds[it].value
                },
                cornerRadius = cornerRadius.toPx(),
                horizontalPadding = diagramHorPadding,
                verticalPadding = diagramVertPadding,
                mainBarColor = DiagramGrid,
                minorBarColor = DiagramGrid,
                minorBarVisible = chartState.chartGrid == ChartGrids.All,
                majorBarVisible = chartState.chartGrid != ChartGrids.NON,
                theme = chartState.chartTheme,
                appSurfaceColor = appThemeDiagramSurfaceColor,
                textMeasurer = textMeasurer,
                verticalDashLinePhase = verticalDashLinePhase,
                horizontalDashLinePhase = horizontalDashLinePhase,
                onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation
            )
        }
        if (hourlyData.isNotEmpty()) {
            val time = hourlyData
                .map { it?.time }
                .map { LocalDateTime.parse(it) }
                .map { it.dayOfMonth * 24 + it.hour }

            chartState.chartQuantities.forEachIndexed { curveNumber, weatherQuantity ->
                chartState.yAxesStarts.getOrNull(curveNumber)?.let {
                    DiagramCurve(
                        hourlyData = hourlyChartData,
                        curveAnimatorProgress = chartState.curveAnimator.getOrNull(curveNumber)?.value
                            ?: 0f,
                        timeData = time,
                        weatherQuantity = weatherQuantity,
                        xAxisRange = chartState.xAxisStart.value..chartState.xAxisEnd.value,
                        yAxisRange = it.value..chartState.yAxesEnds[curveNumber].value,
                        cornerRadius = cornerRadius.toPx(density),
                        horizontalPadding = diagramHorPadding,
                        verticalPadding = diagramVertPadding,
                        curveColor = curveColors[curveNumber],
                        dotsVisible = chartState.dotsOnCurveVisible,
                        shadowVisible = chartState.curveShadowVisible,
                        dotType = DotType.entries[curveNumber],
                        sliderPosition = chartState.sliderThumbPosition,
                        onUpdateCurveValueAtIndicator = { value ->
                            onUpdateCurveValueAtIndicator(curveNumber, value)
                        }
                    )
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (chartState.sunRiseSetIconsVisible) {
                diagramSunRiseSetIcon(
                    context = context,
                    sunRiseSetX = sunRiseSetX,
                    xAxisRange = chartState.xAxisStart.value..chartState.xAxisEnd.value,
                    cornerRadius = cornerRadius.toPx(density),
                    horizontalPadding = diagramHorPadding,
                    verticalPadding = diagramVertPadding
                )
            }
            if (chartState.weatherConditionIconsVisible) {
                diagramWeatherIcon(
                    context = context,
                    hourlyChartData = hourlyChartData,
                    xAxisRange = chartState.xAxisStart.value..chartState.xAxisEnd.value,
                    cornerRadius = cornerRadius.toPx(density),
                    horizontalPadding = diagramHorPadding,
                    verticalPadding = diagramVertPadding
                )
            }
            diagramLegends(
                xAxisRange = chartState.xAxisStart.value .. chartState.xAxisEnd.value,
                yAxesRanges = List(size = chartState.yAxesStarts.size){
                    chartState.yAxesStarts[it].value .. chartState.yAxesEnds[it].value
                },
                yAxesColors = curveColors,
                chartTheme = chartState.chartTheme,
                sunRiseSetX = sunRiseSetX,
                cornerRadius = cornerRadius.toPx(),
                indicatorPosition = chartState.sliderThumbPosition,
                horizontalPadding = diagramHorPadding,
                verticalPadding = diagramVertPadding,
                onAppSurfaceColor = appThemeDiagramOnSurfaceColor,
                textMeasurer = textMeasurer,
                timeFontFamily = timeFontFamily,
                onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation
            )
        }
    }
}


fun quantityData(
    hourlyChartData: List<HourlyChartDto?>? = null,
    weatherData: List<LocalHourlyWeather?>? = null,
    airData: List<LocalAirQuality?>? = null,
    weatherQuantity: WeatherQuantity
): List<Float?>{
    val hourlyWeatherData = hourlyChartData?.map { it?.hourlyWeather } ?: weatherData!!
    val hourlyAirData = hourlyChartData?.map { it?.airQuality } ?: airData!!

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
    val hourlyWeatherData = hourlyChartData?.map { it?.hourlyWeather } ?: weatherData!!
    val airQualityData = hourlyChartData?.map { it?.airQuality } ?: airData!!

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
): Pair<Float, Float> {
    val rangeFivePercentage = (quantityMax - quantityMin) / 20
    return when (weatherQuantity) {
        WeatherQuantity.TEMPERATURE, WeatherQuantity.DEWPOINT, WeatherQuantity.APPARENTTEMP -> {
            if (quantityMax - quantityMin < 3)
                quantityMin - 5 to quantityMax + 5
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
        WeatherQuantity.PRECIPITATION, WeatherQuantity.RAIN, WeatherQuantity.SHOWERS, WeatherQuantity.SNOWFALL -> {
            if (quantityMax - quantityMin < 3)
                (quantityMin - 1).coerceAtLeast(-0.5f) to (quantityMax + 0.5f)
            else
                (quantityMin - rangeFivePercentage) to (quantityMax + rangeFivePercentage)
        }
        WeatherQuantity.SURFACEPRESSURE -> {
            if (quantityMax - quantityMin < 20)
                (quantityMin - 10).coerceAtLeast(0f) to (quantityMax + 10)
            else
                (quantityMin - rangeFivePercentage).coerceAtLeast(0f) to
                        (quantityMax + rangeFivePercentage)
        }
        WeatherQuantity.VISIBILITY -> {
            if (quantityMax - quantityMin < 5)
                (quantityMin - 5).coerceAtLeast(0f) to (quantityMax + 5)
            else
                (quantityMin - rangeFivePercentage).coerceAtLeast(0f) to
                        (quantityMax + rangeFivePercentage)
        }
        WeatherQuantity.WINDSPEED -> {
            if (quantityMax - quantityMin < 5)
                (quantityMin - 5).coerceAtLeast(0f) to (quantityMax + 5)
            else
                (quantityMin - rangeFivePercentage).coerceAtLeast(0f) to
                        (quantityMax + rangeFivePercentage)
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
            if (quantityMax - quantityMin < 300)
                (quantityMin - 500).coerceAtLeast(0f) to (quantityMax + 500)
            else
                (quantityMin - rangeFivePercentage).coerceAtLeast(0f) to
                        (quantityMax + rangeFivePercentage)
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
        onCalculateHorizontalBarSeparation = detailViewModel::calculateHorizontalBarSeparation,
        onCalculateVerticalBarSeparation = detailViewModel::calculateVerticalBarSeparation,
        onUpdateCurveValueAtIndicator = { _, _ -> }
    )
}