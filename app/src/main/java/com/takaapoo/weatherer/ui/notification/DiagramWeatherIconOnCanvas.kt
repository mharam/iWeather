package com.takaapoo.weatherer.ui.notification

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.WeatherType.Companion.drawWeatherIconOnCanvas
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.utility.subList
import java.time.LocalDateTime
import kotlin.math.log2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

fun diagramWeatherIconOnCanvas(
    canvas: Canvas,
    context: Context,
    hourlyData: List<HourlyChartDto>,
    xAxisRange: ClosedFloatingPointRange<Float>,
    cornerRadius: Float,
    iconSizePx: Int,
    horizontalPadding: Float,
    verticalPadding: Float,
    firstPointX: Float,
    frameHeight: Float,
) {
    if (hourlyData.isEmpty() || hourlyData.getOrNull(0)?.hourlyWeather?.time == null)
        return
    val diagramRect = RectF(
        horizontalPadding,
        verticalPadding,
        canvas.width - horizontalPadding,
        min(canvas.height - verticalPadding, frameHeight + verticalPadding),
    )
    val diagramPath = Path().apply {
        addRoundRect(diagramRect, cornerRadius, cornerRadius, Path.Direction.CW)
    }

    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val maxIconCount = ((canvas.width - 2*horizontalPadding) / (1.7f*iconSizePx)).toInt()
    val iconsSeparation = (xAxisLength / maxIconCount).iconRoundToInt()
    val weatherCodes = hourlyData.map { it.hourlyWeather?.weatherCode }
    val times = hourlyData.map { LocalDateTime.parse(it.hourlyWeather?.time) }
    var x = (xAxisRange.start / iconsSeparation - 1).toInt() * iconsSeparation

    canvas.save()
    canvas.clipPath(diagramPath)

    while (x <= xAxisRange.endInclusive + iconsSeparation) {
        val i = (x - firstPointX).toInt()
        val maxWeatherCode = weatherCodes.subList(centerIndex = i, width = iconsSeparation)
            .filter { it != null }.maxByOrNull { it!! }
        maxWeatherCode?.let { it: Int ->
            val topLeft = IntOffset(
                x = (((firstPointX + i - xAxisRange.start) / xAxisLength) * (canvas.width - 2 * horizontalPadding) +
                        horizontalPadding).toInt() - iconSizePx / 2,
                y = 30 + verticalPadding.toInt()
            )
            val isDay = if (hourlyData[i].sunRise.isNullOrEmpty() ||
                hourlyData[i].sunSet.isNullOrEmpty()) true
            else
                times[i].isAfter(LocalDateTime.parse(hourlyData[i].sunRise)
                    .minusMinutes(hourlyData[i].utcOffset?.toLong() ?: 0)) &&
                        times[i].isBefore(LocalDateTime.parse(hourlyData[i].sunSet)
                            .minusMinutes(hourlyData[i].utcOffset?.toLong() ?: 0))

            val moonType = WeatherType.calculateMoonType(times[i].toLocalDate())
            val weatherType = WeatherType.fromWMO(it, isDay, moonType)
            drawWeatherIconOnCanvas(
                canvas,
                weatherType,
                context,
                topLeft,
                iconSize = IntSize(iconSizePx, iconSizePx),
                Paint()
            )
        }
        x += iconsSeparation
    }
    canvas.restore()
}

fun Float.iconRoundToInt(): Int {
    return when {
        this <= 1.5f -> 1
        else -> 2f.pow(log2(this / 4).roundToInt()).roundToInt() * 3
    }
}