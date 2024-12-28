package com.takaapoo.weatherer.ui.screens.detail.daily_diagram

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.customToString
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.innerShadow
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.mainGridWidthDp
import com.takaapoo.weatherer.ui.theme.DiagramShadow
import com.takaapoo.weatherer.ui.theme.Gray20
import com.takaapoo.weatherer.ui.theme.OnDiagramDarkTheme
import com.takaapoo.weatherer.ui.theme.OnDiagramLightTheme
import com.takaapoo.weatherer.ui.theme.indicatorLineColor
import com.takaapoo.weatherer.ui.viewModels.timeFontFamily
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong


fun DrawScope.dailyDiagramLegends(
//    data: List<ClosedFloatingPointRange<Float>?>,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxesRanges: ClosedFloatingPointRange<Float>,
    chartTheme: ChartTheme,
    chartQuantity: DailyWeatherQuantity,
    chartTitle: String,
    cornerRadius: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
    dataMax: Float,
    dataMin: Float,
    curveValueMinAtIndicator: Float?,
    curveValueMaxAtIndicator: Float?,
    barOrCurveGraph: Boolean,
    indicatorPosition: Float,
    onAppSurfaceColor: Color,
//    appSurfaceColor: Color,
    textMeasurer: TextMeasurer,
    timeFontFamily: FontFamily,
    onCalculateHorizontalBarSeparation:
        (start: Float, end: Float, diagramHeight: Float, textMeasurer: TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation:
        (start: Float, end: Float, diagramWidth: Float, textMeasurer: TextMeasurer) -> Pair<Int, Float>
) {
//    Log.i("chart1", "dailyDiagramLegends")
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
    val verticalBarSeparation = calculateDailyVerticalBarSeparation(xAxisRange.start,
        xAxisRange.endInclusive, diagramWidth, textMeasurer).first
    val now = LocalDate.now()

    val curveValueColor = /*when (chartTheme){
        ChartTheme.LIGHT, ChartTheme.DAYNIGHT -> OnDiagramLightTheme
        ChartTheme.DARK -> OnDiagramDarkTheme
        ChartTheme.APPTHEME -> onAppSurfaceColor
    }*/Color.Black
    val textBackgroundColor = /*when (chartTheme){
        ChartTheme.LIGHT, ChartTheme.DAYNIGHT -> DiagramLightTheme
        ChartTheme.DARK -> DiagramDarkTheme
        ChartTheme.APPTHEME -> appSurfaceColor
    }*/Color.White

    val indicatorLineX = (indicatorPosition * diagramWidth) + horizontalPadding
    val indicatorWidth = 1.4f * tickWidth

    clipPath(path = diagramBounds) {
        drawIntoCanvas {
            it.saveLayer(Rect(0f, 0f, size.width, size.height), Paint())
        }
        // Indicator line
        drawLine(
            color = indicatorLineColor,
            start = Offset(x = indicatorLineX, y = verticalPadding),
            end = Offset(x = indicatorLineX, y = size.height - verticalPadding),
            strokeWidth = indicatorWidth,
        )
        if (!barOrCurveGraph){
            val maxValueY = verticalPadding + 24.dp.toPx()
            val minValueY = size.height - verticalPadding - 24.dp.toPx()
            drawCurveValues(
                weatherQuantity = chartQuantity,
                curveValueMinAtIndicator = curveValueMinAtIndicator,
                curveValueMaxAtIndicator = curveValueMaxAtIndicator,
                maxValueY = maxValueY,
                minValueY = minValueY,
                dataMax = dataMax,
                dataMin = dataMin,
                indicatorLineX = indicatorLineX,
                textMeasurer = textMeasurer,
                curveValueColor = curveValueColor,
                backgroundColor = textBackgroundColor,
                backgroundBorderColor = indicatorLineColor,
                backgroundBorderWidth = indicatorWidth
            )
        }
        drawIntoCanvas {
            it.restore()
        }
    }


    var x = (xAxisRange.start / verticalBarSeparation).toInt() * verticalBarSeparation
    while (x <= xAxisRange.start) x += verticalBarSeparation
    while (x < xAxisRange.endInclusive) {
        val lineX = ((x - xAxisRange.start) / xAxisLength) * diagramWidth + horizontalPadding
        val tickColor = when (chartTheme){
            ChartTheme.LIGHT, ChartTheme.DAYNIGHT -> OnDiagramLightTheme
            ChartTheme.DARK -> OnDiagramDarkTheme
            ChartTheme.APPTHEME -> onAppSurfaceColor
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
        val date = now.plusDays(x.toLong())
        val year = date.format(DateTimeFormatter.ofPattern("uuuu"))
        val month = date.format(DateTimeFormatter.ofPattern("MMM d"))
            .uppercase()
        val day = date.format(DateTimeFormatter.ofPattern("EEE"))
        val yearMeasuredText = textMeasurer.measure(
            text = year,
            style = TextStyle(fontFamily = timeFontFamily)
        )
        val monthMeasuredText = textMeasurer.measure(
            text = month,
            style = TextStyle(fontFamily = timeFontFamily)
        )
        val dayMeasuredText = textMeasurer.measure(
            text = day,
            style = TextStyle(fontFamily = timeFontFamily)
        )

        val monthTextLeft = lineX - monthMeasuredText.size.width / 2f
        val monthTextTop = verticalPadding - monthMeasuredText.size.height
        val yearTextTop = verticalPadding - 1.75f * monthMeasuredText.size.height
        val dayTextLeft = lineX - dayMeasuredText.size.width / 2f
        val dayTextTop = size.height - verticalPadding

        if (min(monthTextLeft, dayTextLeft) > horizontalPadding &&
            max(
                monthTextLeft + monthMeasuredText.size.width,
                dayTextLeft + dayMeasuredText.size.width
            ) < size.width - horizontalPadding) {
            drawText(
                textLayoutResult = monthMeasuredText,
                topLeft = Offset(x = monthTextLeft, y = monthTextTop),
                color = onAppSurfaceColor
            )
            drawText(
                textLayoutResult = yearMeasuredText,
                topLeft = Offset(x = monthTextLeft, y = yearTextTop),
                color = onAppSurfaceColor
            )
            drawText(
                textLayoutResult = dayMeasuredText,
                topLeft = Offset(x = dayTextLeft, y = dayTextTop),
                color = onAppSurfaceColor
            )
        }
        x += verticalBarSeparation
    }

    val indicatorX = xAxisRange.start + indicatorPosition * xAxisLength
    val indicatorDate = now.plusDays(indicatorX.roundToLong())
    val indicatorDateString = indicatorDate.format(
        DateTimeFormatter.ofPattern("EEE., MMMM d, uuuu")
    )
    val dateText = "date: $indicatorDateString"
    drawThickText(
        textMeasurer = textMeasurer,
        text = dateText,
        textCenter = size.width / 2,
        textTop = size.height - verticalPadding / 2,
        fontSize = 16.sp,
        fontFamily = timeFontFamily,
        borderWidth = 1f,
        textColor = onAppSurfaceColor
    )

    val start = yAxesRanges.start
    val end = yAxesRanges.endInclusive
    val yAxisLength = end - start
    val horizontalBarSeparation = if (chartQuantity == DailyWeatherQuantity.SUNRISESET)
        onCalculateVerticalBarSeparation(start, end, diagramHeight, textMeasurer).first.toFloat()
    else
        onCalculateHorizontalBarSeparation(start, end, diagramHeight, textMeasurer).first

    var y = (start / horizontalBarSeparation).toInt() * horizontalBarSeparation
    while (y <= start) y += horizontalBarSeparation
    clipPath(path = diagramBounds) {
        while (y < end) {
            val lineY = (1 - (y - start) / yAxisLength) * diagramHeight + verticalPadding
            val startTickColor = when (chartTheme){
                ChartTheme.LIGHT, ChartTheme.DAYNIGHT -> OnDiagramLightTheme
                ChartTheme.DARK -> OnDiagramDarkTheme
                ChartTheme.APPTHEME -> onAppSurfaceColor
            }
            val endTickColor = when (chartTheme){
                ChartTheme.LIGHT, ChartTheme.DAYNIGHT -> OnDiagramLightTheme
                ChartTheme.DARK -> OnDiagramDarkTheme
                ChartTheme.APPTHEME -> onAppSurfaceColor
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

    y = (start / horizontalBarSeparation).toInt() * horizontalBarSeparation
    if (y <= start) y += horizontalBarSeparation
    rotate(
        degrees = 90f,
        pivot = Offset.Zero
    ) {
        while (y < end) {
            val lineY = diagramHeight - (y - start) * diagramHeight / yAxisLength + verticalPadding
            val barValue = y
            val text = if (chartQuantity == DailyWeatherQuantity.SUNRISESET)
                barValue.toTimeString()
            else
                barValue.customToString(horizontalBarSeparation * yAxisLength / yAxisLength)

            val textColor = chartQuantity.color(
                if (chartQuantity == DailyWeatherQuantity.UVINDEXMAX) y/11
                else {
                    if (dataMax == dataMin) 0f else ((y - dataMin) / (dataMax - dataMin))
                }
            )
            drawThickText(
                textMeasurer,
                text,
                textColor,
                textCenter = lineY,
                textTop = -horizontalPadding
            )
            y += horizontalBarSeparation
        }
    }
    rotate(
        degrees = -90f,
        pivot = Offset.Zero
    ) {
//        val measuredTitleOutline = textMeasurer.measure(
//            text = chartTitle,
//            style = TextStyle(
//                fontFamily = Font(R.font.cmu_serif).toFontFamily(),
//                fontSize = 14.sp,
//                drawStyle = Stroke(width = 1.5f)
//            )
//        )
//        val measuredTitleFill = textMeasurer.measure(
//            text = chartTitle,
//            style = TextStyle(
//                fontFamily = Font(R.font.cmu_serif).toFontFamily(),
//                fontSize = 14.sp,
//                drawStyle = Fill
//            )
//        )
//        val titleLeft = -(diagramHeight / 2 + verticalPadding) - measuredTitleOutline.size.width / 2f
//        val titleTop = size.width - horizontalPadding
        drawThickText(
            textMeasurer = textMeasurer,
            text = chartTitle,
            textCenter = -(diagramHeight / 2 + verticalPadding),
            textTop = size.width - horizontalPadding,
            textColor = onAppSurfaceColor
        )
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

fun calculateDailyVerticalBarSeparation(
    xStart: Float,
    xEnd: Float,
    diagramWidth: Float,
    textMeasurer: TextMeasurer
): Pair<Int, Int> {
    val verticalBarRoughSeparation = textMeasurer.measure(
        text = "FEB 28",
        style = TextStyle(fontFamily = timeFontFamily)
    ).size.width * 2.2f
    val verticalBarCount = (diagramWidth / verticalBarRoughSeparation).toInt()
    val xAxisLength = xEnd - xStart
    val majorVerticalBarSeparation = (xAxisLength/verticalBarCount).customRoundToInt()
    val minorVerticalBarSeparation = (majorVerticalBarSeparation / 4f).toInt().coerceAtLeast(1)
    return majorVerticalBarSeparation to minorVerticalBarSeparation
}

private fun Float.customRoundToInt(): Int {
    return 2f.pow(log2(this).roundToInt()).roundToInt()
}

fun Float.toTimeString(): String {
    if (this < 0f || this > 24f) return ""
    return LocalTime.ofSecondOfDay((this * 3600).toLong().mod(24L * 3600))
        .format(DateTimeFormatter.ofPattern("HH\u200C:\u200Cmm"))
}

private fun DrawScope.drawCurveValues(
    weatherQuantity: DailyWeatherQuantity,
    curveValueMinAtIndicator: Float?,
    curveValueMaxAtIndicator: Float?,
    maxValueY: Float,
    minValueY: Float,
    dataMax: Float,
    dataMin: Float,
    indicatorLineX: Float,
    textMeasurer: TextMeasurer,
    curveValueColor: Color,
    backgroundColor: Color,
    backgroundBorderColor: Color,
    backgroundBorderWidth: Float
){
    val minValue = if (curveValueMinAtIndicator != null) {
        if (weatherQuantity == DailyWeatherQuantity.SUNRISESET)
            curveValueMinAtIndicator.toTimeString()
        else
            "%.${weatherQuantity.floatingPointDigits}f".format(curveValueMinAtIndicator)
    } else "?"
    val maxValue = if (curveValueMaxAtIndicator != null) {
        if (weatherQuantity == DailyWeatherQuantity.SUNRISESET)
            curveValueMaxAtIndicator.toTimeString()
        else
            "%.${weatherQuantity.floatingPointDigits}f".format(curveValueMaxAtIndicator)
    } else "?"

    curveValueMaxAtIndicator?.let {
        drawThickText(
            textMeasurer = textMeasurer,
            text = maxValue,
            textColor = if (weatherQuantity == DailyWeatherQuantity.UVINDEXMAX) curveValueColor else
                weatherQuantity.color(
                    fraction = if (dataMax == dataMin) 0f else (it - dataMin) / (dataMax - dataMin)
                ),
            textCenter = indicatorLineX,
            textTop = maxValueY,
            hasBackgroundRect = true,
            backgroundColor = backgroundColor/*.copy(alpha = 0.5f)*/,
            backgroundBorderColor = backgroundBorderColor,
            backgroundBorderWidth = backgroundBorderWidth
        )
    }
    if (weatherQuantity == DailyWeatherQuantity.TEMPERATUREMINMAX ||
        weatherQuantity == DailyWeatherQuantity.SUNRISESET) {
        curveValueMinAtIndicator?.let {
            drawThickText(
                textMeasurer = textMeasurer,
                text = minValue,
                textColor = weatherQuantity.color(
                    fraction = if (dataMax == dataMin) 0f else (it - dataMin) / (dataMax - dataMin)
                ),
                textCenter = indicatorLineX,
                textBottom = minValueY,
                hasBackgroundRect = true,
                backgroundColor = backgroundColor/*.copy(alpha = 0.5f)*/,
                backgroundBorderColor = backgroundBorderColor,
                backgroundBorderWidth = backgroundBorderWidth
            )
        }
    }
}