package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import android.content.Context
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R

fun DrawScope.diagramSunRiseSetIcon(
    context: Context,
    sunRiseSetX: List<Pair<Float?, Float?>>,
    xAxisRange: ClosedFloatingPointRange<Float>,
    cornerRadius: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
) {
    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
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
    val iconsSeparation = 1.2f*iconSize
    val sunRiseIconsPositions = /*hourlyChartData.map {
        it.sunRise?.let { sunRise ->
            timeToX(
                time = sunRise,
                utcOffset = hourlyChartData[0].utcOffset ?: 0L
            )!!
        }
    }.distinct()*/ sunRiseSetX.map { it.first }
    val sunSetIconsPositions = /*hourlyChartData.map {
        it.sunSet?.let { sunSet ->
            timeToX(
                time = sunSet,
                utcOffset = hourlyChartData[0].utcOffset ?: 0L
            )!!
        }
    }.distinct()*/ sunRiseSetX.map { it.second }

    var x = -Float.MAX_VALUE
    val iconTop = (size.height - verticalPadding - iconSize).toInt()
    clipPath(diagramBounds) {
        for (i in sunRiseIconsPositions.indices){
            sunRiseIconsPositions[i]?.let { sunRise ->
                val posX = ((sunRise - xAxisRange.start) / xAxisLength) * (size.width - 2 * horizontalPadding) +
                        horizontalPadding
                drawLine(
                    color = Color.Red,
                    start = Offset(x = posX, y = 0f),
                    end = Offset(x = posX, y = size.height),
                    strokeWidth = 2f
                )
                if (posX > x + iconsSeparation) {
                    drawImage(
                        image = ImageBitmap.imageResource(context.resources, R.drawable.sun_rise),
                        dstOffset = IntOffset(
                            x = (posX - iconSize / 2).toInt(),
                            y = iconTop
                        ),
                        dstSize = IntSize(iconSize, iconSize)
                    )
                    x = posX
                }
            }
            sunSetIconsPositions[i]?.let { sunSet ->
                val posX = ((sunSet - xAxisRange.start) / xAxisLength) * (size.width - 2 * horizontalPadding) +
                        horizontalPadding
                drawLine(
                    color = Color.Red,
                    start = Offset(x = posX, y = 0f),
                    end = Offset(x = posX, y = size.height),
                    strokeWidth = 2f
                )
                if (posX > x + iconsSeparation) {
                    drawImage(
                        image = ImageBitmap.imageResource(context.resources, R.drawable.sun_set),
                        dstOffset = IntOffset(
                            x = (posX - iconSize / 2).toInt(),
                            y = iconTop
                        ),
                        dstSize = IntSize(iconSize, iconSize)
                    )
                    x = posX
                }
            }
        }
    }
}