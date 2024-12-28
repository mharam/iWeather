package com.takaapoo.weatherer.ui.screens.detail.daily_diagram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Dp
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.mainGridWidthDp
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.minorGridWidthDp
import com.takaapoo.weatherer.ui.theme.DiagramDarkTheme
import com.takaapoo.weatherer.ui.theme.DiagramGrid
import com.takaapoo.weatherer.ui.theme.DiagramLightTheme
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.TransparentGray5
import com.takaapoo.weatherer.ui.theme.customColorScheme
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DailyDiagramFrameCanvas(
    modifier: Modifier = Modifier,
    xAxisStart: Float,
    xAxisEnd: Float,
    yAxesStarts: Float,
    yAxesEnds: Float,
    minorBarVisible: Boolean,
    majorBarVisible: Boolean,
    cornerRadius: Dp,
    diagramHorPadding: Float,
    diagramVertPadding: Float,
    chartTheme: ChartTheme,
    chartQuantity: DailyWeatherQuantity,
    textMeasurer: TextMeasurer,
    verticalDashLinePhase: Float,
    horizontalDashLinePhase: Float,
    onCalculateHorizontalBarSeparation:
        (start: Float, end: Float, diagramHeight: Float, textMeasurer: TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation:
        (start: Float, end: Float, diagramWidth: Float, textMeasurer: TextMeasurer) -> Pair<Int, Float>
) {
    val appThemeDiagramSurfaceColor = MaterialTheme.customColorScheme.appThemeDiagramSurfaceColor

    Canvas(
        modifier = modifier.fillMaxSize()
            .clip(RectangleShape)   // This is to prevent DrawScope call unnecessarily
    ) {
//        Log.i("chart1", "dailyDiagramFrame")

        dailyDiagramFrame(
            xAxisRange = xAxisStart .. xAxisEnd,
            yAxesRanges = yAxesStarts .. yAxesEnds,
            cornerRadius = cornerRadius.toPx(),
            horizontalPadding = diagramHorPadding,
            verticalPadding = diagramVertPadding,
            mainBarColor = DiagramGrid,
            minorBarColor = DiagramGrid,
            minorBarVisible = minorBarVisible,
            majorBarVisible = majorBarVisible,
            theme = chartTheme,
            chartQuantity = chartQuantity,
            appSurfaceColor = appThemeDiagramSurfaceColor,
//            appSurfaceColorSecondary = appThemeDiagramSurfaceColorSecondary,
            textMeasurer = textMeasurer,
            horizontalDashLinePhase = horizontalDashLinePhase,
            verticalDashLinePhase = verticalDashLinePhase,
            onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
            onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation
        )
    }
}

fun DrawScope.dailyDiagramFrame(
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxesRanges: ClosedFloatingPointRange<Float>,
    cornerRadius: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
    mainBarColor: Color,
    minorBarColor: Color,
    minorBarVisible: Boolean,
    majorBarVisible: Boolean,
    theme: ChartTheme,
    chartQuantity: DailyWeatherQuantity,
    appSurfaceColor: Color,
//    appSurfaceColorSecondary: Color,
    textMeasurer: TextMeasurer,
    verticalDashLinePhase: Float,
    horizontalDashLinePhase: Float,
    onCalculateHorizontalBarSeparation:
        (start: Float, end: Float, diagramHeight: Float, textMeasurer: TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation:
        (start: Float, end: Float, diagramWidth: Float, textMeasurer: TextMeasurer) -> Pair<Int, Float>
) {
    val diagramRectangle = RoundRect(
        left = horizontalPadding,
        top = verticalPadding,
        right = size.width - horizontalPadding,
        bottom = size.height - verticalPadding,
        cornerRadius = CornerRadius(cornerRadius)
    )
    val diagramBounds = Path().apply { addRoundRect(diagramRectangle) }
    val diagramWidth = diagramRectangle.width
    val diagramHeight = diagramRectangle.height
    val mainBarWidthPx = mainGridWidthDp.toPx()
    val minorBarWidthPx = minorGridWidthDp.toPx()

    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val (verticalBarSeparation, minorVerticalBarSeparation) =
        calculateDailyVerticalBarSeparation(xAxisRange.start, xAxisRange.endInclusive, diagramWidth, textMeasurer)

    when (theme) {
        ChartTheme.LIGHT, ChartTheme.DAYNIGHT -> {
            drawRoundRect(
                color = DiagramLightTheme,
                topLeft = Offset(diagramRectangle.left, diagramRectangle.top),
                size = Size(diagramRectangle.width, diagramRectangle.height),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        }
        ChartTheme.DARK -> {
            drawRoundRect(
                color = DiagramDarkTheme,
                topLeft = Offset(diagramRectangle.left, diagramRectangle.top),
                size = Size(diagramRectangle.width, diagramRectangle.height),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        }
        ChartTheme.APPTHEME -> {
            drawRoundRect(
                color = appSurfaceColor,
                topLeft = Offset(diagramRectangle.left, diagramRectangle.top),
                size = Size(diagramRectangle.width, diagramRectangle.height),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        }
    }

    if (theme != ChartTheme.DARK) {
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(TransparentGray5, Transparent),
                start = Offset(horizontalPadding, verticalPadding),
                end = Offset(size.width, size.height)
            ),
            topLeft = Offset(diagramRectangle.left, diagramRectangle.top),
            size = Size(diagramRectangle.width, diagramRectangle.height),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )
    }

    var x = (xAxisRange.start / minorVerticalBarSeparation).toInt() * minorVerticalBarSeparation
    if (x <= xAxisRange.start) x += minorVerticalBarSeparation
    while (x < xAxisRange.endInclusive) {
        val lineX = ((x - xAxisRange.start) / xAxisLength) * diagramWidth + horizontalPadding
        if (x % verticalBarSeparation == 0) {
            if (majorBarVisible) {
                clipPath(path = diagramBounds) {
                    drawLine(
                        color = mainBarColor,
                        start = Offset(x = lineX, y = verticalPadding),
                        end = Offset(x = lineX, y = size.height - verticalPadding),
                        strokeWidth = mainBarWidthPx,
                    )
                }
            }
        } else if (minorBarVisible) {
            clipPath(path = diagramBounds) {
                drawLine(
                    color = minorBarColor,
                    start = Offset(x = lineX, y = verticalPadding),
                    end = Offset(x = lineX, y = size.height - verticalPadding),
                    strokeWidth = minorBarWidthPx,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(20f, 10f),
                        phase = verticalDashLinePhase
                    )
                )
            }
        }
        x += minorVerticalBarSeparation
    }

//    clipPath(path = diagramBounds){
//        var x = xAxisRange.start - 2
//        while (x < xAxisRange.endInclusive + 2){
//            if (x.toInt() % 2 == 0) {
//                drawRect(
//                    color = lightShadowColor,
//                    topLeft = Offset(
//                        x = normalizedX(
//                            x.toInt() - 0.5f,
//                            xAxisRange.start,
//                            xAxisLength,
//                            horizontalPadding,
//                            diagramWidth
//                        ),
//                        y = 0f
//                    ),
//                    size = Size(diagramWidth / xAxisLength, size.height)
//                )
//            }
//            x++
//        }
//    }

    val start = yAxesRanges.start
    val end = yAxesRanges.endInclusive
    val yAxisLength = end - start
    val (horizontalBarSeparation, minorHorizontalBarSeparation) =
        if (chartQuantity == DailyWeatherQuantity.SUNRISESET)
            onCalculateVerticalBarSeparation(start, end, diagramHeight, textMeasurer)
        else
            onCalculateHorizontalBarSeparation(start, end, diagramHeight, textMeasurer)

    var y = (start / minorHorizontalBarSeparation).toInt() * minorHorizontalBarSeparation
    while (y <= start) y += minorHorizontalBarSeparation
    clipPath(path = diagramBounds) {
        while (y < end) {
            val lineY = (1 - (y - start) / yAxisLength) * diagramHeight + verticalPadding
            val coefficient = y / horizontalBarSeparation.toFloat()
            if (abs(coefficient - coefficient.roundToInt()) < 0.01) {
                if (majorBarVisible) {
                    drawLine(
                        color = mainBarColor,
                        start = Offset(x = horizontalPadding, y = lineY),
                        end = Offset(x = size.width - horizontalPadding, y = lineY),
                        strokeWidth = mainBarWidthPx,
                    )
                }
            } else if (minorBarVisible) {
                drawLine(
                    color = minorBarColor,
                    start = Offset(x = horizontalPadding, y = lineY),
                    end = Offset(x = size.width - horizontalPadding, y = lineY),
                    strokeWidth = minorBarWidthPx,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(20f, 10f),
                        phase = horizontalDashLinePhase
                    )
                )
            }
            y += minorHorizontalBarSeparation
        }
    }
}