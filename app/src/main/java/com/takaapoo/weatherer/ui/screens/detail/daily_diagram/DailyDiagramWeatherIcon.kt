package com.takaapoo.weatherer.ui.screens.detail.daily_diagram

import android.content.Context
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.WeatherType.Companion.drawWeatherIcon
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

fun DrawScope.dailyDiagramWeatherIcon(
    context: Context,
    dailyChartData: List<LocalDailyWeather>,
    xAxisRange: ClosedFloatingPointRange<Float>,
    cornerRadius: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
) {
    if (dailyChartData.isEmpty()) return
    val firstPointX = dailyTimeToX(date = dailyChartData[0].time)!!

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
    val weatherCodes = dailyChartData.map { it.weatherCode }
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
                val weatherType = WeatherType.fromWMO(
                    code = it,
                    isDay = true,
                    moonType = null
                )
                drawWeatherIcon(weatherType, context, topLeft, IntSize(iconSize, iconSize))
            }
            x += iconsSeparation
        }
    }
}

private fun Float.iconRoundToInt(): Int {
    return 2f.pow(log2(this).roundToInt()).roundToInt()
}