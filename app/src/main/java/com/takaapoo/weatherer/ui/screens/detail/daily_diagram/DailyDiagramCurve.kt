package com.takaapoo.weatherer.ui.screens.detail.daily_diagram

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.drawIndicatorPoint
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.generateDailyStepGraph
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.nonNullDataSegments
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.normalizedX
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.normalizedY
import com.takaapoo.weatherer.ui.theme.OnDiagramDarkTheme
import com.takaapoo.weatherer.ui.theme.OnDiagramLightTheme
import com.takaapoo.weatherer.ui.theme.Transparent


@Composable
fun DailyDiagramCurve(
    data: List<ClosedFloatingPointRange<Float>?>,
    dataMax: Float,
    dataMin: Float,
    firstPointX: Float?,
    curveAnimatorProgress: Float,
    weatherQuantity: DailyWeatherQuantity,
    modifier: Modifier = Modifier,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxisRange: ClosedFloatingPointRange<Float>,
    curveValueMinAtIndicator: Float?,
    curveValueMaxAtIndicator: Float?,
    chartTheme: ChartTheme,
    onAppSurfaceColor: Color,
    cornerRadius: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
    textMeasurer: TextMeasurer,
    shadowVisible: Boolean,
    barOrCurveGraph: Boolean,
    sliderPosition: Float,
) {
//    Log.i("chart1", "DailyDiagramCurve")
    firstPointX ?: return
    val curveValueColor = when (chartTheme){
        ChartTheme.LIGHT, ChartTheme.DAYNIGHT -> OnDiagramLightTheme
        ChartTheme.DARK -> OnDiagramDarkTheme
        ChartTheme.APPTHEME -> onAppSurfaceColor
    }

    val pointSize = dimensionResource(id = R.dimen.daily_curve_point_size)
    val curveWidth = dimensionResource(id = R.dimen.diagram_curve_width)
    Spacer(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val diagramWidth = size.width - 2 * horizontalPadding
                val diagramHeight = size.height - 2 * verticalPadding
                val curveWidthPx = curveWidth.toPx()
                val pointSizePx = pointSize.toPx()

                onDrawBehind {
                    val diagramBounds = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = horizontalPadding,
                                top = (if (barOrCurveGraph) (size.height - 2*verticalPadding) * (1 - curveAnimatorProgress)
                                        else 0f) + verticalPadding,
                                right = horizontalPadding + (size.width - 2 * horizontalPadding) *
                                        (if (barOrCurveGraph) 1f else curveAnimatorProgress),
                                bottom = size.height - verticalPadding,
                                cornerRadius = CornerRadius(cornerRadius)
                            )
                        )
                    }
                    clipPath(diagramBounds) {
                        // This is necessary for translucent bar shadows
                        drawIntoCanvas {
                            it.saveLayer(Rect(0f, 0f, size.width, size.height), Paint())
                        }
                        if (barOrCurveGraph) {
                            diagramBar(
                                weatherQuantity = weatherQuantity,
                                data = data,
                                dataMax = dataMax,
                                dataMin = dataMin,
                                curveValueColor = curveValueColor,
                                firstPointX = firstPointX,
                                diagramWidth = diagramWidth,
                                diagramHeight = diagramHeight,
                                textMeasurer = textMeasurer,
                                xAxisRange = xAxisRange,
                                yAxisRange = yAxisRange,
                                horizontalPadding = horizontalPadding,
                                verticalPadding = verticalPadding
                            )
                        } else {
                            diagramCurve(
                                weatherQuantity = weatherQuantity,
                                data = data,
                                dataMax = dataMax,
                                dataMin = dataMin,
                                firstPointX = firstPointX,
                                diagramWidth = diagramWidth,
                                diagramHeight = diagramHeight,
                                xAxisRange = xAxisRange,
                                yAxisRange = yAxisRange,
                                horizontalPadding = horizontalPadding,
                                verticalPadding = verticalPadding,
                                curveWidth = curveWidthPx,
                                shadowVisible = shadowVisible
                            )
                        }
                        if (!barOrCurveGraph) {
                            val curveMaxColor = weatherQuantity.color(
                                if (weatherQuantity == DailyWeatherQuantity.UVINDEXMAX)
                                        (curveValueMaxAtIndicator ?: 0f)/11
                                else {
                                    if (dataMax == dataMin) 0f
                                    else (((curveValueMaxAtIndicator ?: 0f) - dataMin) / (dataMax - dataMin))
                                }
                            )
                            drawIndicatorPoint(
                                curveValueMaxAtIndicator,
                                curveMaxColor,
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
                            if (weatherQuantity == DailyWeatherQuantity.TEMPERATUREMINMAX ||
                                weatherQuantity == DailyWeatherQuantity.SUNRISESET){
                                val curveMinColor = weatherQuantity.color(
                                    if (dataMax == dataMin) 0f
                                    else ((curveValueMinAtIndicator ?: 0f) - dataMin) / (dataMax - dataMin)
                                )
                                drawIndicatorPoint(
                                    curveValueMinAtIndicator,
                                    curveMinColor,
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
                            }
                        }
                        drawIntoCanvas {
                            it.restore()
                        }
                    }
                }
            }
    )
}

fun curveBrush(
    weatherQuantity: DailyWeatherQuantity,
    dataMin: Float,
    dataMax: Float,
    diagramHeight: Float,
    yAxisRange: ClosedFloatingPointRange<Float>,
    verticalPadding: Float
): Brush {
    val yAxisLength = yAxisRange.endInclusive - yAxisRange.start
    return when(weatherQuantity){
        DailyWeatherQuantity.TEMPERATUREMINMAX -> {
            Brush.verticalGradient(
                colors = weatherQuantity.brushColors,
                startY = normalizedY(
                    dataMax,
                    yAxisRange.start,
                    yAxisLength,
                    verticalPadding,
                    diagramHeight
                ),
                endY = normalizedY(
                    dataMin,
                    yAxisRange.start,
                    yAxisLength,
                    verticalPadding,
                    diagramHeight
                )
            )
        }
        DailyWeatherQuantity.SUNRISESET -> {
            Brush.verticalGradient(
                colors = weatherQuantity.brushColors,
                startY = normalizedY(
                    dataMax,
                    yAxisRange.start,
                    yAxisLength,
                    verticalPadding,
                    diagramHeight
                ),
                endY = normalizedY(
                    dataMin,
                    yAxisRange.start,
                    yAxisLength,
                    verticalPadding,
                    diagramHeight
                )
            )
        }
        DailyWeatherQuantity.UVINDEXMAX -> {
            Brush.verticalGradient(
                colors = weatherQuantity.brushColors,
                startY = normalizedY(
                    0f,
                    yAxisRange.start,
                    yAxisLength,
                    verticalPadding,
                    diagramHeight
                ),
                endY = normalizedY(
                    11f,
                    yAxisRange.start,
                    yAxisLength,
                    verticalPadding,
                    diagramHeight
                )
            )
        }
        DailyWeatherQuantity.PRECIPITATIONSUM -> {
            Brush.verticalGradient(
                colors = weatherQuantity.brushColors,
            )
        }
        DailyWeatherQuantity.PRECIPITATIONPROBABILITYMAX -> {
            Brush.verticalGradient(
                colors = weatherQuantity.brushColors,
                startY = normalizedY(
                    0f,
                    yAxisRange.start,
                    yAxisLength,
                    verticalPadding,
                    diagramHeight
                ),
                endY = normalizedY(
                    100f,
                    yAxisRange.start,
                    yAxisLength,
                    verticalPadding,
                    diagramHeight
                )
            )
        }
        DailyWeatherQuantity.WINDSPEEDMAX -> {
            Brush.verticalGradient(
                colors = weatherQuantity.brushColors,
            )
        }
    }
}

fun DrawScope.diagramBar(
    weatherQuantity: DailyWeatherQuantity,
    data: List<ClosedFloatingPointRange<Float>?>,
    dataMin: Float,
    dataMax: Float,
    curveValueColor: Color,
    firstPointX: Float,
    diagramWidth: Float,
    diagramHeight: Float,
    textMeasurer: TextMeasurer,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxisRange: ClosedFloatingPointRange<Float>,
    horizontalPadding: Float,
    verticalPadding: Float
){
    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val yAxisLength = yAxisRange.endInclusive - yAxisRange.start
    var x = firstPointX

    data.forEach { dataRange ->
        dataRange?.let {
            val rectangleHeight = (it.endInclusive - it.start) * diagramHeight / yAxisLength
            val rectangleWidth = 8.dp.toPx()
            val rectangleShadowWidth = 6.dp.toPx()
            val cornerRadius = rectangleWidth / 2

            val left = normalizedX(
                x,
                xAxisRange.start,
                xAxisLength,
                horizontalPadding,
                diagramWidth
            ) - rectangleWidth/2
            val top = normalizedY(
                dataRange.endInclusive,
                yAxisRange.start,
                yAxisLength,
                verticalPadding,
                diagramHeight
            )

            val barBrush = curveBrush(weatherQuantity, dataMin, dataMax, diagramHeight,
                yAxisRange, verticalPadding)
            val shadowSize = Size(
                width = rectangleWidth + 2 * rectangleShadowWidth,
                height = rectangleHeight + 2 * rectangleShadowWidth
            )
            val eraseColor = Color(0xAFFFFFFF)
            when (weatherQuantity){
                DailyWeatherQuantity.TEMPERATUREMINMAX, DailyWeatherQuantity.SUNRISESET -> {
                    val eraserSize = Size(
                        width = rectangleWidth + 2 * rectangleShadowWidth + 2f,
                        height = rectangleHeight - 2 * cornerRadius
                    )
                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(x = left - rectangleShadowWidth, y = top - rectangleShadowWidth),
                        size = shadowSize,
                        cornerRadius = CornerRadius(shadowSize.width / 2),
//                        alpha = 0.7f
                    )
                    drawArc(
                        brush = Brush.radialGradient(
                            colors = listOf(eraseColor, Transparent),
                            center = Offset(left + cornerRadius, top + cornerRadius),
                            radius = cornerRadius + rectangleShadowWidth
                        ),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(x = left - rectangleShadowWidth - 1f, y = top - rectangleShadowWidth - 1f),
                        size = Size(
                            width = rectangleWidth + 2 * rectangleShadowWidth + 2f,
                            height = rectangleWidth + 2 * rectangleShadowWidth + 2f
                        ),
                        blendMode = BlendMode.DstIn
                    )
                    drawArc(
                        brush = Brush.radialGradient(
                            colors = listOf(eraseColor, Transparent),
                            center = Offset(left + cornerRadius, top + rectangleHeight - cornerRadius),
                            radius = cornerRadius + rectangleShadowWidth
                        ),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(
                            x = left - rectangleShadowWidth - 1f,
                            y = top + rectangleHeight - 2 * cornerRadius - rectangleShadowWidth - 1f
                        ),
                        size = Size(
                            width = rectangleWidth + 2 * rectangleShadowWidth + 2f,
                            height = rectangleWidth + 2 * rectangleShadowWidth + 2f
                        ),
                        blendMode = BlendMode.DstIn
                    )
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Transparent, eraseColor, Transparent),
                            startX = left - rectangleShadowWidth,
                            endX = left + rectangleWidth + rectangleShadowWidth
                        ),
                        topLeft = Offset(x = left - rectangleShadowWidth - 1f, y = top + cornerRadius),
                        size = eraserSize,
                        blendMode = BlendMode.DstIn
                    )
                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(x = left, y = top),
                        size = Size(width = rectangleWidth, height = rectangleHeight),
                        cornerRadius = CornerRadius(cornerRadius)
                    )
                }
                else -> {
                    if (rectangleHeight > 0.001f) {
                        val path = Path().apply {
                            moveTo(left, top + cornerRadius)
                            arcTo(
                                Rect(
                                    offset = Offset(left, top),
                                    size = Size(
                                        rectangleWidth,
                                        rectangleWidth.coerceAtMost(2 * rectangleHeight)
                                    )
                                ),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = 180f,
                                forceMoveTo = true
                            )
                            lineTo(left + rectangleWidth, top + rectangleHeight)
                            lineTo(left, top + rectangleHeight)
                            close()
                        }
                        val shadowPath = Path().apply {
                            moveTo(left - rectangleShadowWidth, top + cornerRadius)
                            arcTo(
                                Rect(
                                    offset = Offset(left - rectangleShadowWidth, top - rectangleShadowWidth),
                                    size = Size(
                                        rectangleWidth + 2 * rectangleShadowWidth,
                                        (rectangleWidth + 2 * rectangleShadowWidth)
                                            .coerceAtMost(2 * (rectangleHeight + rectangleShadowWidth))
                                    )
                                ),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = 180f,
                                forceMoveTo = true
                            )
                            lineTo(left + rectangleWidth + rectangleShadowWidth,
                                top + rectangleHeight)
                            lineTo(left - rectangleShadowWidth, top + rectangleHeight)
                            close()
                        }
                        drawPath(
                            path = shadowPath,
                            brush = barBrush
                        )
                        drawArc(
                            brush = Brush.radialGradient(
                                colors = listOf(eraseColor, Transparent),
                                center = Offset(left + cornerRadius, top + cornerRadius),
                                radius = cornerRadius + rectangleShadowWidth
                            ),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = true,
                            topLeft = Offset(x = left - rectangleShadowWidth - 1f, y = top - rectangleShadowWidth - 1f),
                            size = Size(
                                width = rectangleWidth + 2 * rectangleShadowWidth + 2f,
                                height = (rectangleWidth + 2 * rectangleShadowWidth)
                                    .coerceAtMost(2 * (rectangleHeight + rectangleShadowWidth)) + 2f
                            ),
                            blendMode = BlendMode.DstIn
                        )
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Transparent, eraseColor, Transparent),
                                startX = left - rectangleShadowWidth,
                                endX = left + rectangleWidth + rectangleShadowWidth
                            ),
                            topLeft = Offset(x = left - rectangleShadowWidth - 1f, y = top + cornerRadius),
                            size =  Size(
                                width = rectangleWidth + 2 * rectangleShadowWidth + 2f,
                                height = rectangleHeight - cornerRadius
                            ),
                            blendMode = BlendMode.DstIn
                        )
                        drawPath(
                            path = path,
                            brush = barBrush
                        )
                    }
                }
            }
            val maxValue = if (weatherQuantity == DailyWeatherQuantity.SUNRISESET)
                dataRange.endInclusive.toTimeString()
            else "%.${weatherQuantity.floatingPointDigits}f".format(dataRange.endInclusive)

            val minValue = if (weatherQuantity == DailyWeatherQuantity.SUNRISESET)
                dataRange.start.toTimeString()
            else "%.${weatherQuantity.floatingPointDigits}f".format(dataRange.start)

            rotate(
                degrees = 90f,
                pivot = Offset.Zero
            ) {
                drawThickText(
                    textMeasurer,
                    text = maxValue,
                    textColor = if (weatherQuantity == DailyWeatherQuantity.UVINDEXMAX) curveValueColor else
                    weatherQuantity.color(
                        fraction = if (dataMax == dataMin) 0f
                        else (dataRange.endInclusive - dataMin) / (dataMax - dataMin)
                    ),
                    textRight = top - 8.dp.toPx(),
                    textMiddle = -left - rectangleWidth/2,
                )
                if (weatherQuantity == DailyWeatherQuantity.TEMPERATUREMINMAX ||
                    weatherQuantity == DailyWeatherQuantity.SUNRISESET) {
                    drawThickText(
                        textMeasurer,
                        text = minValue,
                        textColor = weatherQuantity.color(
                            fraction = if (dataMax == dataMin) 0f
                            else (dataRange.start - dataMin) / (dataMax - dataMin)
                        ),
                        textLeft = top + rectangleHeight + 8.dp.toPx(),
                        textMiddle = -left - rectangleWidth / 2,
                    )
                }
            }
        }
        x++
    }
}

fun DrawScope.diagramCurve(
    weatherQuantity: DailyWeatherQuantity,
    data: List<ClosedFloatingPointRange<Float>?>,
    dataMin: Float,
    dataMax: Float,
    firstPointX: Float,
    diagramWidth: Float,
    diagramHeight: Float,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxisRange: ClosedFloatingPointRange<Float>,
    horizontalPadding: Float,
    verticalPadding: Float,
    curveWidth: Float,
    shadowVisible: Boolean
){
    var pathMin: Path? = null
    val pathMax: Path?
    val filledPath = Path()
    val lastPointX = firstPointX + data.size - 1
//    Log.i("first1", "data = ${data.map { it?.start }}")
    val yAxisLength = yAxisRange.endInclusive - yAxisRange.start
    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val curveBrush = curveBrush(weatherQuantity, dataMin, dataMax, diagramHeight,
        yAxisRange, verticalPadding)
    val phase = 0.5f

    when (weatherQuantity){
        DailyWeatherQuantity.TEMPERATUREMINMAX, DailyWeatherQuantity.SUNRISESET -> {
            pathMin = generateDailyStepGraph(
                data = data.map { it?.start },
                firstPointX = firstPointX,
                diagramWidth = diagramWidth,
                diagramHeight = diagramHeight,
                xAxisRange = xAxisRange,
                yAxisRange = yAxisRange,
                horizontalPadding = horizontalPadding,
                verticalPadding = verticalPadding,
                reverse = true,
                lastPointX = lastPointX,
                phase = phase
            )
            pathMax = generateDailyStepGraph(
                data = data.map { it?.endInclusive },
                firstPointX = firstPointX,
                diagramWidth = diagramWidth,
                diagramHeight = diagramHeight,
                xAxisRange = xAxisRange,
                yAxisRange = yAxisRange,
                horizontalPadding = horizontalPadding,
                verticalPadding = verticalPadding,
                phase = phase
            )
            val curveMinSegments = pathMin?.divide()
            val curveMaxSegments = pathMax?.divide()
            val minCurveData = data.map { it?.start }.nonNullDataSegments().values.toList()
            val maxCurveData = data.map { it?.endInclusive }.nonNullDataSegments().values.toList()
//Log.i("size1", "size = ${curveMinSegments?.size} , ${minCurveData.size} , data = ${data.map { it?.start }}")
            if (pathMin != null && pathMax != null && curveMinSegments?.size == curveMaxSegments?.size) {
                val size = curveMinSegments!!.size
                filledPath.apply {
                    curveMaxSegments!!.forEachIndexed { i, curve ->
                        val curveMinBounds = curveMinSegments[size-1 - i].getBounds()
                        val curveMaxBounds = curveMaxSegments[i].getBounds()
                        addPath(curve)
                        lineTo(
                            x = curveMinBounds.right,
                            y = normalizedY(
                                minCurveData[i].last(),
                                yAxisRange.start,
                                yAxisLength,
                                verticalPadding,
                                diagramHeight
                            )
                        )
                        addPath(curveMinSegments[size-1 - i])
                        lineTo(
                            x = curveMaxBounds.left,
                            y = normalizedY(
                                maxCurveData[i].first(),
                                yAxisRange.start,
                                yAxisLength,
                                verticalPadding,
                                diagramHeight
                            )
                        )
                        close()
                    }
                }
            }
        }
        else -> {
            pathMax = generateDailyStepGraph(
                data = data.map { it?.endInclusive },
                firstPointX = firstPointX,
                diagramWidth = diagramWidth,
                diagramHeight = diagramHeight,
                xAxisRange = xAxisRange,
                yAxisRange = yAxisRange,
                horizontalPadding = horizontalPadding,
                verticalPadding = verticalPadding,
                phase = phase
            )
            if (pathMax != null) {
                val curveMaxSegments = pathMax.divide()
                filledPath.apply {
                    curveMaxSegments.forEach { curve ->
                        val curveBounds = curve.getBounds()
                        val yZero = normalizedY(
                            0f,
                            yAxisRange.start,
                            yAxisLength,
                            verticalPadding,
                            diagramHeight
                        )
                        addPath(curve)
                        filledPath.lineTo(x = curveBounds.right, y = yZero)
                        filledPath.lineTo(x = curveBounds.left, y = yZero)
                        filledPath.close()
                    }
                }
            }
        }
    }
    if (shadowVisible) {
        drawPath(
            path = filledPath,
            brush = curveBrush
        )
        drawPath(
            path = filledPath,
            brush = Brush.verticalGradient(
                colors = when (weatherQuantity) {
                    DailyWeatherQuantity.TEMPERATUREMINMAX, DailyWeatherQuantity.SUNRISESET -> {
                        listOf(Color(0x4FFFFFFF), Transparent, Color(0x4FFFFFFF))
                    }
                    else -> listOf(Color(0x4FFFFFFF), Transparent)
                },
                startY = normalizedY(
                    dataMax,
                    yAxisRange.start,
                    yAxisLength,
                    verticalPadding,
                    diagramHeight
                ),
                endY = normalizedY(
                    dataMin,
                    yAxisRange.start,
                    yAxisLength,
                    verticalPadding,
                    diagramHeight
                )
            ),
            blendMode = BlendMode.DstIn
        )
    }
    pathMin?.let {
        drawPath(
            path = it,
            brush = curveBrush,
            style = Stroke(curveWidth)
        )
    }
    pathMax?.let {
        drawPath(
            path = it,
            brush = curveBrush,
            style = Stroke(curveWidth)
        )
    }

}