package com.takaapoo.weatherer.ui.notification.custom_views

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.ui.notification.diagramCurveOnCanvas
import com.takaapoo.weatherer.ui.notification.diagramFrameOnCanvas
import com.takaapoo.weatherer.ui.notification.diagramLegendsOnCanvas
import com.takaapoo.weatherer.ui.notification.diagramWeatherIconOnCanvas
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.mainGridWidthDp
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.quantityData
import com.takaapoo.weatherer.ui.theme.DiagramGrid
import com.takaapoo.weatherer.utility.toPx
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow


class HourlyChart(
    context: Context,
    val hourlyChartData: List<HourlyChartDto> = emptyList(),
    val diagramHorPadding: Float = 16f,
    val diagramVertPadding: Float = 24f,
    val chartQuantities: List<WeatherQuantity>,
    val appSettings: AppSettings,
    val curvesColor: List<Int>? = null,
    val frameHeight: Float = Float.MAX_VALUE,
    val horizontalMinorBarCount: Int = 1,
    val weatherIconSize: Dp = 24.dp,
    val timeClock: HourlyDiagramTime = appSettings.notificationHourlyDiagramTime
) : View(context) {

    val myDensity = Density(context)
    val cornerRadius = context.resources.getDimension(R.dimen.diagram_corner_radius)
    val legendTextColor = Color(
        Resources.getSystem().getIdentifier(
            "notification_primary_text_color_current",
            "color",
            "android"
        ).let { resourceId: Int ->
            context.getColor(resourceId)
        }
    ).toArgb()

    val xAxisStart = 0f
    val xAxisEnd = 48f
    val yAxesStarts = mutableListOf<Float>()
    val yAxesEnds = mutableListOf<Float>()
    val zone: ZoneId = when (timeClock) {
        HourlyDiagramTime.UTC -> ZoneId.of("UTC")
        HourlyDiagramTime.DEVICE -> ZoneId.systemDefault()
        HourlyDiagramTime.LOCATION -> {
            ZoneOffset.ofTotalSeconds((hourlyChartData.getOrNull(0)?.utcOffset?.toInt() ?: 0) * 60)
        }
    }
    val filteredHourlyChartData = hourlyChartData.filter {
        LocalDateTime.parse(it.hourlyWeather?.time).isAfter(LocalDateTime.now(zone).minusHours(2)) &&
                LocalDateTime.parse(it.hourlyWeather?.time).isBefore(LocalDateTime.now(zone).plusDays(2))
    }

    init {
        if ((chartQuantities.getOrNull(0) == WeatherQuantity.TEMPERATURE &&
                    chartQuantities.getOrNull(1) == WeatherQuantity.APPARENTTEMP) ||
            (chartQuantities.getOrNull(0) == WeatherQuantity.APPARENTTEMP &&
                    chartQuantities.getOrNull(1) == WeatherQuantity.TEMPERATURE)){
            val data = quantityData(
                hourlyChartData = filteredHourlyChartData,
                weatherQuantity = chartQuantities[0]
            ) + quantityData(
                hourlyChartData = filteredHourlyChartData,
                weatherQuantity = chartQuantities[1]
            )
            populateYAxes(data, yAxesStarts, yAxesEnds)
            if (yAxesStarts.isNotEmpty()) {
                yAxesStarts.add(yAxesStarts[0])
                yAxesEnds.add(yAxesEnds[0])
            }
        } else {
            chartQuantities.forEach { quantity ->
                when (quantity) {
                    WeatherQuantity.HUMIDITY, WeatherQuantity.PRECIPITATIONPROBABILITY,
                    WeatherQuantity.CLOUDCOVER -> {
                        yAxesStarts.add(0f)
                        yAxesEnds.add(120f)
                    }
                    else -> {
                        val data = quantityData(
                            hourlyChartData = filteredHourlyChartData,
                            weatherQuantity = quantity
                        )
                        populateYAxes(data, yAxesStarts, yAxesEnds)
                    }
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            val now = LocalDateTime.now(zone).truncatedTo(ChronoUnit.HOURS)
            diagramFrameOnCanvas(
                canvas = this,
                cornerRadius = cornerRadius,
                horizontalPadding = diagramHorPadding,
                verticalPadding = diagramVertPadding,
                x1 = ChronoUnit.HOURS.between(now, now.toLocalDate().atStartOfDay().plusDays(1)),
                mainBarColor = DiagramGrid.toArgb(),
                minorBarColor = DiagramGrid.toArgb(),
                minorBarVisible = true,
                majorBarVisible = true,
                mainBarWidthPx = mainGridWidthDp.toPx(myDensity),
                minorBarWidthPx = mainGridWidthDp.toPx(myDensity),
                frameHeight = min(frameHeight, height - 2 * diagramVertPadding),
                horizontalMinorBarCount = horizontalMinorBarCount,
            )
            if (hourlyChartData.isNotEmpty() && yAxesStarts.size == chartQuantities.size) {
                chartQuantities.forEachIndexed { curveNumber, weatherQuantity ->
                    diagramCurveOnCanvas(
                        canvas = canvas,
                        hourlyData = hourlyChartData,
                        weatherQuantity = weatherQuantity,
                        cornerRadius = cornerRadius,
                        horizontalPadding = diagramHorPadding,
                        verticalPadding = diagramVertPadding,
                        xAxisRange = xAxisStart .. xAxisEnd,
                        yAxisRange = yAxesStarts[curveNumber]..yAxesEnds[curveNumber],
                        curveColor = Color((curvesColor ?: appSettings.notificationCurvesColor)[curveNumber]),
                        appSettings = appSettings,
                        width = width.toFloat(),
                        height = min(frameHeight + 2*diagramVertPadding, height.toFloat()),
                        curveWidth = context.resources.getDimension(R.dimen.diagram_curve_width),
                        timeClock = timeClock
                    )
                }
            }
            if (appSettings.notificationHourlyDiagramIconsVisibility) {
                diagramWeatherIconOnCanvas(
                    canvas = canvas,
                    context = context,
                    hourlyData = hourlyChartData,
                    xAxisRange = xAxisStart..xAxisEnd,
                    cornerRadius = cornerRadius,
                    iconSizePx = weatherIconSize.toPx(myDensity).toInt(),
                    horizontalPadding = diagramHorPadding,
                    verticalPadding = diagramVertPadding,
                    firstPointX = timeToX(
                        time = hourlyChartData[0].hourlyWeather!!.time,
                        utcOffset = hourlyChartData[0].utcOffset ?: 0L,
                        timeClock = timeClock
                    )!!,
                    frameHeight = min(frameHeight, height - 2 * diagramVertPadding)
                )
            }
            diagramLegendsOnCanvas(
                canvas = canvas,
                yAxesRanges = List(size = yAxesStarts.size){
                    yAxesStarts[it] .. yAxesEnds[it]
                },
                yAxesColors = (curvesColor ?: appSettings.notificationCurvesColor).map { Color(it) },
                cornerRadius = cornerRadius,
                horizontalPadding = diagramHorPadding,
                verticalPadding = diagramVertPadding,
                onAppSurfaceColor = legendTextColor,
                textColor = legendTextColor,
                utcOffset = hourlyChartData.getOrNull(0)?.utcOffset ?: 0L,
                density = myDensity,
                timeFont = ResourcesCompat.getFont(context, R.font.cmu_typewriter_bold)!!,
                yAxesFont = ResourcesCompat.getFont(context, R.font.cmu_serif)!!,
                zone = zone,
                frameHeight = min(frameHeight, height - 2 * diagramVertPadding),
            )
        }
    }

    private fun populateYAxes(
        data: List<Float?>,
        yAxesStarts: MutableList<Float>,
        yAxesEnds: MutableList<Float>,
    ) {
        var quantityMax = data.maxOfOrNull { it ?: Float.MIN_VALUE } ?: return
        var quantityMin = data.minOfOrNull { it ?: Float.MAX_VALUE } ?: return
        var step = (quantityMax - quantityMin) / 3
        when {
            step == 0f -> {
                val start = floor(quantityMin)
                yAxesStarts.add(start - 1)
                yAxesEnds.add(start + 2)
            }
            else -> {
//                quantityMax += 0.05f * step
//                quantityMin -= 0.05f * step
                step = ((quantityMax - quantityMin) / 3).customCeil()
                val start = quantityMin.customFloor(step)
                yAxesStarts.add(start)
                yAxesEnds.add(start + 3 * step)
            }
        }
    }

    private fun Float.customCeil(): Float {
        val order = floor(log10(this))
        return ceil(this / (10f.pow(order))) * (10f.pow(order))
    }

    private fun Float.customFloor(step: Float): Float {
//    val stepOrder = floor(log10(step))
        if (step <= 0 && this <= 0) return floor(this)
        val order = floor(log10(abs(if (step > 0) step else this)))
        return floor((this) / (10f.pow(order))) * (10f.pow(order))
    }

}

fun timeToX(time: String?, utcOffset: Long?, timeClock: HourlyDiagramTime): Float?{
    if (time == null || utcOffset == null) return null
    val deviceUtcOffset = ZonedDateTime.now(ZoneId.systemDefault()).offset.totalSeconds / 3600f
    val zone: ZoneId = when (timeClock) {
        HourlyDiagramTime.UTC -> ZoneId.of("UTC")
        HourlyDiagramTime.DEVICE -> ZoneId.systemDefault()
        HourlyDiagramTime.LOCATION -> ZoneOffset.ofTotalSeconds(utcOffset.toInt() * 60)
    }

    return Duration.between(
        LocalDateTime.now(zone).truncatedTo(ChronoUnit.HOURS),
        LocalDateTime.parse(time)
    ).toSeconds()/3600f + when (timeClock) {
        HourlyDiagramTime.UTC -> 0f
        HourlyDiagramTime.DEVICE -> deviceUtcOffset
        HourlyDiagramTime.LOCATION -> utcOffset / 60f
    }
}