package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.drawThickText
import com.takaapoo.weatherer.ui.theme.DiagramShadow
import com.takaapoo.weatherer.ui.theme.Gray20
import com.takaapoo.weatherer.ui.theme.OnDiagramDarkTheme
import com.takaapoo.weatherer.ui.theme.OnDiagramLightTheme
import com.takaapoo.weatherer.ui.theme.indicatorLineColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

fun DrawScope.diagramLegends(
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxesRanges: List<ClosedFloatingPointRange<Float>>,
    sunRiseSetX: List<Pair<Float?, Float?>>,
    chartTheme: ChartTheme,
    yAxesColors: List<Color>,
    cornerRadius: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
    indicatorPosition: Float,
    onAppSurfaceColor: Color,
    textMeasurer: TextMeasurer,
    timeFontFamily: FontFamily,
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
    val tickLength = 8.dp.toPx()
    val tickWidth = 2 * mainGridWidthDp.toPx()

    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val verticalBarSeparation = onCalculateVerticalBarSeparation(xAxisRange.start,
        xAxisRange.endInclusive, size.width - 2*horizontalPadding, textMeasurer).first
    val now = LocalDate.now().atStartOfDay()


    val indicatorLineX = (indicatorPosition * diagramWidth) + horizontalPadding
    clipPath(path = diagramBounds) {
        // Indicator line
        drawLine(
            color = indicatorLineColor,
            start = Offset(x = indicatorLineX, y = verticalPadding),
            end = Offset(x = indicatorLineX, y = size.height - verticalPadding),
            strokeWidth = 1.4f * tickWidth,
        )
    }

    var x = (xAxisRange.start / verticalBarSeparation).toInt() * verticalBarSeparation
    while (x <= xAxisRange.start) x += verticalBarSeparation
    while (x < xAxisRange.endInclusive) {
        val lineX = ((x - xAxisRange.start) / xAxisLength) * diagramWidth + horizontalPadding
        val tickColor = when (chartTheme){
            ChartTheme.LIGHT -> OnDiagramLightTheme
            ChartTheme.DARK -> OnDiagramDarkTheme
            ChartTheme.APPTHEME -> onAppSurfaceColor
            ChartTheme.DAYNIGHT -> {
                if (sunRiseSetX.contain(x.toFloat())) OnDiagramDarkTheme
                else OnDiagramLightTheme
            }
        }
        clipPath(path = diagramBounds) {
            drawLine(
                color = tickColor,
                start = Offset(x = lineX, y = verticalPadding),
                end = Offset(x = lineX, y = verticalPadding + tickLength),
                strokeWidth = tickWidth,
            )
            drawLine(
                color = tickColor,
                start = Offset(x = lineX, y = size.height - verticalPadding),
                end = Offset(x = lineX, y = size.height - verticalPadding - tickLength),
                strokeWidth = tickWidth,
            )
        }
        val dateTime = now.plusHours(x.toLong())
        val year = dateTime.format(DateTimeFormatter.ofPattern("uuuu"))
        val month = dateTime.format(DateTimeFormatter.ofPattern("MMM d"))
            .uppercase()
        val clock = dateTime.format(DateTimeFormatter.ofPattern("HH\u200C:\u200Cmm"))
        val yearMeasuredText = textMeasurer.measure(
            text = year,
            style = TextStyle(fontFamily = timeFontFamily)
        )
        val monthMeasuredText = textMeasurer.measure(
            text = month,
            style = TextStyle(fontFamily = timeFontFamily)
        )
        val clockMeasuredText = textMeasurer.measure(
            text = clock,
            style = TextStyle(fontFamily = timeFontFamily)
        )

        val monthTextLeft = lineX - monthMeasuredText.size.width / 2f
        val monthTextTop = verticalPadding - monthMeasuredText.size.height
        val yearTextTop = verticalPadding - 1.75f * monthMeasuredText.size.height
        val clockTextLeft = lineX - clockMeasuredText.size.width / 2f
        val clockTextTop = size.height - verticalPadding

        if (min(monthTextLeft, clockTextLeft) > horizontalPadding &&
            max(
                monthTextLeft + monthMeasuredText.size.width,
                clockTextLeft + clockMeasuredText.size.width
            ) < size.width - horizontalPadding) {
            drawText(
                textLayoutResult = monthMeasuredText,
                topLeft = Offset(x = monthTextLeft, y = monthTextTop)
            )
            drawText(
                textLayoutResult = yearMeasuredText,
                topLeft = Offset(x = monthTextLeft, y = yearTextTop)
            )
            drawText(
                textLayoutResult = clockMeasuredText,
                topLeft = Offset(x = clockTextLeft, y = clockTextTop)
            )
        }
        x += verticalBarSeparation
    }

    val indicatorX = xAxisRange.start + indicatorPosition * xAxisLength
    val indicatorTime = now.plusSeconds((indicatorX * 3600).toLong())
    val indicatorTimeString = indicatorTime.format(
        DateTimeFormatter.ofPattern("EEE., MMMM d, uuuu, HH:mm")
    )
    val timeText = "time: $indicatorTimeString"
    drawThickText(
        textMeasurer = textMeasurer,
        text = timeText,
        textCenter = size.width / 2,
        textTop = size.height - verticalPadding / 2,
        fontSize = 16.sp,
        fontFamily = timeFontFamily,
        borderWidth = 1f
    )

    if (yAxesRanges.isNotEmpty()) {
        val start = yAxesRanges[0].start
        val end = yAxesRanges[0].endInclusive
        val yAxisLength = end - start
        val horizontalBarSeparation = onCalculateHorizontalBarSeparation(start, end,
            size.height - 2*verticalPadding, textMeasurer).first

        var y = (start / horizontalBarSeparation).toInt() * horizontalBarSeparation
        while (y <= start) y += horizontalBarSeparation
        clipPath(path = diagramBounds) {
            while (y < end) {
                val lineY = (1 - (y - start) / yAxisLength) * diagramHeight + verticalPadding
                val startTickColor = when (chartTheme){
                    ChartTheme.LIGHT -> OnDiagramLightTheme
                    ChartTheme.DARK -> OnDiagramDarkTheme
                    ChartTheme.APPTHEME -> onAppSurfaceColor
                    ChartTheme.DAYNIGHT -> {
                        if (sunRiseSetX.contain(xAxisRange.start)) OnDiagramDarkTheme
                        else OnDiagramLightTheme
                    }
                }
                val endTickColor = when (chartTheme){
                    ChartTheme.LIGHT -> OnDiagramLightTheme
                    ChartTheme.DARK -> OnDiagramDarkTheme
                    ChartTheme.APPTHEME -> onAppSurfaceColor
                    ChartTheme.DAYNIGHT -> {
                        if (sunRiseSetX.contain(xAxisRange.endInclusive)) OnDiagramDarkTheme
                        else OnDiagramLightTheme
                    }
                }
                drawLine(
                    color = startTickColor,
                    start = Offset(x = horizontalPadding, y = lineY),
                    end = Offset(x = horizontalPadding + tickLength, y = lineY),
                    strokeWidth = tickWidth,
                )
                drawLine(
                    color = endTickColor,
                    start = Offset(x = size.width - horizontalPadding, y = lineY),
                    end = Offset(x = size.width - horizontalPadding - tickLength, y = lineY),
                    strokeWidth = tickWidth,
                )
                y += horizontalBarSeparation
            }
        }

        yAxesRanges.forEachIndexed { index, range ->
            y = (start / horizontalBarSeparation).toInt() * horizontalBarSeparation
            if (y <= start) y += horizontalBarSeparation
            rotate(
                degrees = 90f * (-1f).pow(index),
                pivot = Offset.Zero
            ) {
                val length = range.endInclusive - range.start
                while (y < end) {
                    val lineY = diagramHeight - (y - start) * diagramHeight / yAxisLength + verticalPadding
                    val barValue = if (index == 0) y else (y - start) * length / yAxisLength + range.start
                    val text = barValue.customToString(horizontalBarSeparation * length/yAxisLength)

                    val textTop = when (index){
                        0 -> -horizontalPadding
                        1 -> size.width - horizontalPadding
                        2 -> -2.5f * horizontalPadding
                        else -> size.width - 2.5f * horizontalPadding
                    }
                    drawThickText(
                        textMeasurer,
                        text,
                        yAxesColors[index],
                        textCenter = (if (index%2 == 0) 1 else -1) * lineY,
                        textTop = textTop
                    )
                    y += horizontalBarSeparation
                }
            }
        }
    }
    drawRoundRect(
        color = Gray20,
        topLeft = Offset(horizontalPadding, verticalPadding),
        size = Size(size.width - 2 * horizontalPadding, size.height - 2 * verticalPadding),
        style = Stroke(mainGridWidthDp.toPx()),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )
    innerShadow(
        color = DiagramShadow,
        rect = Rect(
            topLeft = Offset(horizontalPadding, verticalPadding),
            bottomRight = Offset(size.width - horizontalPadding, size.height - verticalPadding)
        ),
        cornersRadius = cornerRadius.toDp(),
    )
}

// Returns true if value is in day time
fun List<Pair<Float?, Float?>>.contain(value: Float): Boolean{
    this.forEach {
        if (value > (it.first?:Float.MAX_VALUE) && value < (it.second?:(-100000f)))
            return false
    }
    return true
}


fun Float.customToString(separation: Float): String {
    val order = log10(separation)
    if (abs(this) /separation < 0.01) return "0"
    return when {
        order > 2 -> "%d".format(this.roundToInt())
        order > 0 -> {
            if (abs(separation - separation.roundToInt()) < 0.1f) "%d".format(this.roundToInt())
            else "%.1f".format(this)
        }
        else -> "%.${abs(floor(order)).toInt()}f".format(this)
    }
}