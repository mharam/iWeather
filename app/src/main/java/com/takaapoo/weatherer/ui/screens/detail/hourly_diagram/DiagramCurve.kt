package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.divide
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.dimensionResource
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.theme.Transparent

@Composable
fun DiagramCurve(
    hourlyData: List<HourlyChartDto?>,
    curveAnimatorProgress: Float,
    timeData: List<Int>,
    weatherQuantity: WeatherQuantity,
    modifier: Modifier = Modifier,
    initialXAxisRange: ClosedFloatingPointRange<Float>,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxisRange: ClosedFloatingPointRange<Float>,
    cornerRadius: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
    curveColor: Color,
    dotsVisible: Boolean,
    shadowVisible: Boolean,
    dotType: DotType,
    sliderPosition: Float,
    onUpdateCurveValueAtIndicator: (value: Float?) -> Unit
) {
    if (weatherQuantity.airQuality && hourlyData.getOrNull(0)?.airQuality?.time == null) return
    if (!weatherQuantity.airQuality && hourlyData.getOrNull(0)?.hourlyWeather?.time == null) return

    val firstPointX = timeToX(
        time = if (weatherQuantity.airQuality) hourlyData[0]!!.airQuality!!.time
        else hourlyData[0]!!.hourlyWeather!!.time,
        utcOffset = hourlyData[0]?.utcOffset ?: 0L
    )!!

    val lastPointX = timeToX(
        time = if (weatherQuantity.airQuality) hourlyData.last { it?.airQuality?.time != null }!!.airQuality!!.time
        else hourlyData.last{ it?.hourlyWeather?.time != null }!!.hourlyWeather!!.time,
        utcOffset = hourlyData[0]?.utcOffset ?: 0L
    )!!

    val data = quantityData(hourlyChartData = hourlyData, weatherQuantity = weatherQuantity).also {
        if (it.isEmpty()) return
    }
    val graphType = weatherQuantity.graphType()
    val (controlPoints1, controlPoints2) = quantityControlPoints(
        hourlyChartData = hourlyData,
        weatherQuantity = weatherQuantity
    )

    val indicatorX = xAxisRange.start + sliderPosition * (xAxisRange.endInclusive - xAxisRange.start)
    val curveValueAtIndicator = when (graphType){
        GraphTypes.CUBIC -> cubicCurveXtoY(
            data = data,
            controlPoints1 = controlPoints1,
            controlPoints2 = controlPoints2,
            firstPointX = firstPointX,
            lastPointX = lastPointX,
            targetX = indicatorX
        )
        GraphTypes.LINEAR -> linearCurveXtoY(
            data = data,
            firstPointX = firstPointX,
            lastPointX = lastPointX,
            targetX = indicatorX
        )
        GraphTypes.STEP -> stepCurveXtoY(
            data = data,
            firstPointX = firstPointX,
            lastPointX = lastPointX,
            targetX = indicatorX
        )
    }
    onUpdateCurveValueAtIndicator(curveValueAtIndicator)

    val pointSize = dimensionResource(id = R.dimen.hourly_curve_point_size)
    val curveWidth = dimensionResource(id = R.dimen.diagram_curve_width)
    Spacer(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val diagramWidth = size.width - 2 * horizontalPadding
                val diagramHeight = size.height - 2 * verticalPadding
                val pointSizePx = pointSize.toPx()
                val curveWidthPx = curveWidth.toPx()

                val filledPath = Path()
                var filledPathBrush = Brush.verticalGradient()
                val path = when (graphType) {
                    GraphTypes.STEP -> generateStepGraph(
                        data = data,
                        firstPointX = firstPointX,
                        diagramWidth = diagramWidth,
                        diagramHeight = diagramHeight,
                        xAxisRange = xAxisRange,
                        yAxisRange = yAxisRange,
                        horizontalPadding = horizontalPadding,
                        verticalPadding = verticalPadding
                    )
                    GraphTypes.LINEAR -> generateLinearGraph(
                        data = data,
                        firstPointX = firstPointX,
                        diagramWidth = diagramWidth,
                        diagramHeight = diagramHeight,
                        xAxisRange = xAxisRange,
                        yAxisRange = yAxisRange,
                        horizontalPadding = horizontalPadding,
                        verticalPadding = verticalPadding
                    )
                    GraphTypes.CUBIC -> generateCubicGraph(
                        data = data,
                        controlPoints1 = controlPoints1,
                        controlPoints2 = controlPoints2,
                        firstPointX = firstPointX,
                        diagramWidth = diagramWidth,
                        diagramHeight = diagramHeight,
                        xAxisRange = xAxisRange,
                        yAxisRange = yAxisRange,
                        horizontalPadding = horizontalPadding,
                        verticalPadding = verticalPadding
                    )
                }?.also { curvePath ->
                    val curveSegments = curvePath.divide()
                    curveSegments.forEach { curve ->
                        val curveBounds = curve.getBounds()
                        filledPath.addPath(curve)
                        filledPath.lineTo(x = curveBounds.right, y = size.height)
                        filledPath.lineTo(x = curveBounds.left, y = size.height)
                        filledPath.close()
                    }
                    val curvePathBounds = curvePath.getBounds()
                    filledPathBrush = Brush.verticalGradient(
                        0f to curveColor.copy(alpha = 0.3f),
                        1f to Transparent,
                        startY = curvePathBounds.top,
                        endY = curvePathBounds.bottom
                    )
                    /*filledPathBrush = Brush.verticalGradient(
                        0f to Color.Red.copy(alpha = 0.3f) ,
                        0.45f to Transparent,
                        0.5f to Transparent,
                        0.55f to Transparent,
                        1f to Color.Blue.copy(alpha = 0.3f),
                        startY = y0 - limit,
                        endY = y0 + limit
                    )*/
//                    Log.i("path1", "size = ${curvePath.divide().size}")
                }

                onDrawBehind {
                    val diagramBounds = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = horizontalPadding,
                                top = verticalPadding,
                                right = horizontalPadding + (size.width - 2 * horizontalPadding) *
                                        curveAnimatorProgress,
                                bottom = size.height - verticalPadding,
                                cornerRadius = CornerRadius(cornerRadius)
                            )
                        )
                    }
                    clipPath(diagramBounds) {
                        drawIntoCanvas {
                            it.saveLayer(Rect(0f, 0f, size.width, size.height), Paint())
                        }
                        path?.let { curvePath ->
                            drawPath(
                                path = curvePath,
                                color = curveColor,
                                style = Stroke(curveWidthPx)
                            )
                            if (shadowVisible) {
                                drawPath(
                                    path = filledPath,
                                    brush = filledPathBrush
                                )
                            }
                        }
                        if (dotsVisible && (graphType == GraphTypes.CUBIC || graphType == GraphTypes.LINEAR)) {
                            plotPoints(
                                data = data,
                                timeData = timeData,
                                firstPointX = firstPointX,
                                diagramWidth = diagramWidth,
                                diagramHeight = diagramHeight,
                                initialXAxisRange = initialXAxisRange,
                                xAxisRange = xAxisRange,
                                yAxisRange = yAxisRange,
                                horizontalPadding = horizontalPadding,
                                verticalPadding = verticalPadding,
                                dotType = dotType,
                                curveColor = curveColor,
                                pointSize = pointSizePx
                            )
                        }
                        drawIndicatorPoint(
                            curveValueAtIndicator,
                            curveColor,
                            sliderPosition,
                            yAxisRange.start,
                            yAxisRange.endInclusive,
                            horizontalPadding,
                            verticalPadding,
                            diagramWidth,
                            diagramHeight,
                            pointSizePx,
                            curveWidthPx
                        )
                        drawIntoCanvas {
                            it.restore()
                        }
                    }
                }
            }
    )
}


fun DrawScope.drawIndicatorPoint(
    curveValueAtIndicator: Float?,
    curveColor: Color,
    sliderPosition: Float,
    yAxisStart: Float,
    yAxisEnd: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
    diagramWidth: Float,
    diagramHeight: Float,
    pointSizePx: Float,
    curveWidth: Float
){
    curveValueAtIndicator?.let {
        val indicatorPointX = sliderPosition * diagramWidth + horizontalPadding
        val indicatorPointY = normalizedY(
            it,
            yAxisStart,
            yAxisEnd - yAxisStart,
            verticalPadding,
            diagramHeight
        )
        val indicatorPointRadius = 0.65f * pointSizePx
        drawCircle(
            color = Color.Transparent,
            radius = indicatorPointRadius,
            center = Offset(x = indicatorPointX, y = indicatorPointY),
            blendMode = BlendMode.Clear
        )
        drawCircle(
            color = curveColor,
            radius = indicatorPointRadius,
            center = Offset(x = indicatorPointX, y = indicatorPointY),
            style = Stroke(curveWidth / 2)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to Transparent,
                    0.3f to Transparent,
                    0.31f to curveColor,
                    1f to Transparent
                ),
                radius = 2 * pointSizePx,
                center = Offset(x = indicatorPointX, y = indicatorPointY)
            ),
            radius = 2 * pointSizePx,
            center = Offset(x = indicatorPointX, y = indicatorPointY),
        )
    }
}