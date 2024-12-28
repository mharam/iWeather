package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

fun DrawScope.plotPoints(
    data: List<Float?>,
    timeData: List<Int>,
    firstPointX: Float,
    diagramWidth: Float,
    diagramHeight: Float,
    initialXAxisRange: ClosedFloatingPointRange<Float>,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxisRange: ClosedFloatingPointRange<Float>,
    horizontalPadding: Float,
    verticalPadding: Float,
    dotType: DotType,
    curveColor: Color,
    pointSize: Float
) {
    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val yAxisLength = yAxisRange.endInclusive - yAxisRange.start
    val step = 2f.pow(log2(2 * xAxisLength / (initialXAxisRange.endInclusive - initialXAxisRange.start)).roundToInt())
        .coerceAtLeast(1f).roundToInt()

    var x = firstPointX
    data.filterIndexed { index, _ -> timeData[index] % step == 0 }
        .forEach { value ->
            value?.let { y ->
                val point = listOf(
                    Offset(
                        x = ((x - xAxisRange.start) / xAxisLength) * diagramWidth + horizontalPadding,
                        y = (1 - (y - yAxisRange.start) / yAxisLength) * diagramHeight + verticalPadding
                    )
                )
                when (dotType){
                    DotType.TRIANGLE, DotType.DIAMOND -> {
                        drawOutline(
                            outline = pointOutline(
                                center = point[0],
                                dimension = pointSize,
                                dotType = dotType
                            ),
                            color = curveColor
                        )
                    }
                    DotType.SQUARE, DotType.CIRCLE -> {
                        drawPoints(
                            points = point,
                            pointMode = PointMode.Points,
                            cap = if (dotType == DotType.SQUARE) StrokeCap.Square else StrokeCap.Round,
                            color = curveColor,
                            strokeWidth = pointSize
                        )
                    }
                }
            }
            x += step
        }
}


fun pointOutline(
    center: Offset,
    dimension: Float,
    dotType: DotType,
): Outline {
    val path =  when(dotType){
        DotType.TRIANGLE -> Path().apply {
            moveTo(center.x, center.y - 0.635f * dimension)
            lineTo(center.x - 0.55f * dimension, center.y + 0.317f * dimension)
            lineTo(center.x + 0.55f * dimension, center.y + 0.317f * dimension)
            close()
        }
        DotType.DIAMOND -> Path().apply {
            moveTo(center.x, center.y - 0.707f * dimension)
            lineTo(center.x - 0.707f * dimension, center.y)
            lineTo(center.x, center.y + 0.707f * dimension)
            lineTo(center.x + 0.707f * dimension, center.y)
            close()
        }
        else -> Path()
    }
    return Outline.Generic(path)
}