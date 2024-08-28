package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import android.content.Context
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.WeatherType.Companion.drawWeatherIcon
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import java.time.LocalDateTime
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

fun DrawScope.diagramWeatherIcon(
    context: Context,
    hourlyChartData: List<HourlyChartDto>,
    xAxisRange: ClosedFloatingPointRange<Float>,
    cornerRadius: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
) {
    if (hourlyChartData.isEmpty() || hourlyChartData.getOrNull(0)?.hourlyWeather?.time == null)
        return
    val firstPointX = timeToX(
        time = hourlyChartData[0].hourlyWeather!!.time,
        utcOffset = hourlyChartData[0].utcOffset ?: 0L
    )!!

    val diagramBounds = Path().apply {
        addRoundRect(
            RoundRect(
                left = horizontalPadding,
                top = verticalPadding,
                right = size.width - horizontalPadding,
                bottom = size.height - verticalPadding,
                cornerRadius = CornerRadius(cornerRadius)
            )
        )
    }
    val iconSize = 40.dp.roundToPx()
    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val maxIconCount = ((size.width - 2*horizontalPadding) / (2f*iconSize)).toInt()
    val iconsSeparation = (xAxisLength / maxIconCount).iconRoundToInt()
    val weatherCodes = hourlyChartData.map { it.hourlyWeather?.weatherCode }
    val times = hourlyChartData.map { LocalDateTime.parse(it.hourlyWeather?.time) }
    var x = (xAxisRange.start / iconsSeparation - 1).toInt() * iconsSeparation
    clipPath(diagramBounds) {
        while (x <= xAxisRange.endInclusive + iconsSeparation) {
            val i = (x - firstPointX).toInt()
            weatherCodes.getOrNull(i)?.let {
                val topLeft = IntOffset(
                    x = (((firstPointX + i - xAxisRange.start) / xAxisLength) * (size.width - 2 * horizontalPadding) +
                            horizontalPadding).toInt() - iconSize / 2,
                    y = 30 + verticalPadding.toInt()
                )
                val isDay = if (hourlyChartData[i].sunRise.isNullOrEmpty() ||
                    hourlyChartData[i].sunSet.isNullOrEmpty()) true
                else
                    times[i].isAfter(LocalDateTime.parse(hourlyChartData[i].sunRise)) &&
                            times[i].isBefore(LocalDateTime.parse(hourlyChartData[i].sunSet))
                val moonType = WeatherType.calculateMoonType(times[i].toLocalDate())
                val weatherType = WeatherType.fromWMO(it, isDay, moonType)
                drawWeatherIcon(weatherType, context, topLeft, IntSize(iconSize, iconSize))
            }
            x += iconsSeparation
        }
    }
}

private fun Float.iconRoundToInt(): Int {
    return when {
        this <= 1.5f -> 1
        else -> 2f.pow(log2(this / 4).roundToInt()).roundToInt() * 3
    }
}