package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import android.content.Context
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.WeatherType.Companion.drawWeatherIcon
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.utility.subList
import java.time.LocalDateTime
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

fun DrawScope.diagramWeatherIcon(
    context: Context,
    hourlyChartData: List<HourlyChartDto>,
    xAxisRange: ClosedFloatingPointRange<Float>,
    cornerRadius: Float,
    iconSize: Dp = 40.dp,
    horizontalPadding: Float,
    verticalPadding: Float,
    firstPointX: Float
) {
    if (hourlyChartData.isEmpty() || hourlyChartData.getOrNull(0)?.hourlyWeather?.time == null)
        return

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
    val iconSizePx = iconSize.roundToPx()
    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val maxIconCount = ((size.width - 2*horizontalPadding) / (2f*iconSizePx)).toInt()
    val iconsSeparation = (xAxisLength / maxIconCount).iconRoundToInt()
    val weatherCodes = hourlyChartData.map { it.hourlyWeather?.weatherCode }
    val times = hourlyChartData.map { LocalDateTime.parse(it.hourlyWeather?.time) }
    var x = (xAxisRange.start / iconsSeparation - 1).toInt() * iconsSeparation
    clipPath(diagramBounds) {
        while (x <= xAxisRange.endInclusive + iconsSeparation) {
            val i = (x - firstPointX).toInt()
            val maxWeatherCode = weatherCodes.subList(centerIndex = i, width = iconsSeparation)
                .filter { it != null }.maxByOrNull { it!! }
            maxWeatherCode?.let {
                val topLeft = IntOffset(
                    x = (((firstPointX + i - xAxisRange.start) / xAxisLength) * (size.width - 2 * horizontalPadding) +
                            horizontalPadding).toInt() - iconSizePx / 2,
                    y = 30 + verticalPadding.toInt()
                )
                val isDay = if (hourlyChartData.getOrNull(i)?.sunRise.isNullOrEmpty() ||
                    hourlyChartData.getOrNull(i)?.sunSet.isNullOrEmpty()) true
                else
                    times[i].isAfter(LocalDateTime.parse(hourlyChartData[i].sunRise)
                        .minusMinutes(hourlyChartData[i].utcOffset?.toLong() ?: 0)) &&
                            times[i].isBefore(LocalDateTime.parse(hourlyChartData[i].sunSet)
                                .minusMinutes(hourlyChartData[i].utcOffset?.toLong() ?: 0))
                val moonType = times.getOrNull(i)?.let {
                    WeatherType.calculateMoonType(it.toLocalDate())
                }
                val weatherType = WeatherType.fromWMO(it, isDay, moonType)
                drawWeatherIcon(weatherType, context, topLeft, IntSize(iconSizePx, iconSizePx))
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