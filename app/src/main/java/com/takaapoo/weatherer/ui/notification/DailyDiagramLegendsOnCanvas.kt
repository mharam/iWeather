package com.takaapoo.weatherer.ui.notification

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Align
import android.graphics.Paint.Style
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.toTimeString
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.mainGridWidthDp
import com.takaapoo.weatherer.ui.theme.DiagramShadow
import com.takaapoo.weatherer.ui.theme.Gray20
import com.takaapoo.weatherer.utility.toPx
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min


fun dailyDiagramLegendsOnCanvas(
    canvas: Canvas,
    cornerRadius: Float,
    yAxisRange: ClosedFloatingPointRange<Float>?,
    horizontalPadding: Float,
    verticalPadding: Float,
    @ColorInt onAppSurfaceColor: Int,
    @ColorInt textColor: Int,
    dataMax: Float,
    dataMin: Float,
    density: Density,
    timeFont: Typeface,
    yAxesFont: Typeface,
    chartQuantity: DailyWeatherQuantity,
    chartTitle: String,
    frameHeight: Float
) {
    val diagramRectangle = RectF(
        horizontalPadding,
        verticalPadding,
        canvas.width - horizontalPadding,
        min(canvas.height - verticalPadding, frameHeight + verticalPadding),
    )
//    val diagramBounds = Path().apply {
//        addRoundRect(diagramRectangle, cornerRadius, cornerRadius, Path.Direction.CW)
//    }
    val diagramWidth = diagramRectangle.width()
    val diagramHeight = diagramRectangle.height()
    val tickLength = 6.dp.toPx(density)
    val tickWidth = 2 * mainGridWidthDp.toPx(density)
    val mainVerticalBarCount = 5

    val now = LocalDate.now()
    drawHorizontalLegend(
        canvas = canvas,
        tickColor = onAppSurfaceColor,
        now = now,
        mainVerticalBarCount = mainVerticalBarCount,
        diagramWidth = diagramWidth,
        density = density,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        tickLength = tickLength,
        tickWidth = tickWidth,
        textColor = textColor,
        fontSize = 14.sp.toPx(density),
        timeFont = timeFont,
        frameHeight = frameHeight
    )
    if (yAxisRange != null) {
        drawVerticalLegend(
            canvas = canvas,
            yAxisRange = yAxisRange,
            chartTitle = chartTitle,
            chartQuantity = chartQuantity,
            dataMax = dataMax,
            dataMin = dataMin,
            titleColor = textColor,
            tickColor = onAppSurfaceColor,
            diagramHeight = diagramHeight,
//            diagramBounds = diagramBounds,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            tickLength = tickLength,
            tickWidth = tickWidth,
            fontSize = 14.sp.toPx(density),
            font = yAxesFont
        )
    }

    canvas.drawRoundRect(
        diagramRectangle,
        cornerRadius,
        cornerRadius,
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Gray20.toArgb()
            style = Style.STROKE
            strokeWidth = mainGridWidthDp.toPx(density)
        }
    )

    innerShadowOnCanvas(
        canvas = canvas,
        color = DiagramShadow.copy(alpha = 0.25f).toArgb(),
        rect = diagramRectangle,
        cornersRadius = cornerRadius,
        spread = 1.dp.toPx(density),
        blur = 8.dp.toPx(density),
        offsetY = 3.dp.toPx(density),
        offsetX = 2.dp.toPx(density)
    )
}


private fun drawHorizontalLegend(
    canvas: Canvas,
    @ColorInt tickColor: Int,
    now: LocalDate,
    mainVerticalBarCount: Int,
    diagramWidth: Float,
    density: Density,
    horizontalPadding: Float,
    verticalPadding: Float,
    tickLength: Float,
    tickWidth: Float,
    @ColorInt textColor: Int,
    fontSize: Float,
    timeFont: Typeface,
    frameHeight: Float
) {
    val paint = Paint(ANTI_ALIAS_FLAG).apply {
        strokeWidth = tickWidth
        color = tickColor
    }
    val textPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
        typeface = timeFont
        color = textColor
        textSize = fontSize
        textAlign = Align.CENTER
    }

    val saved = canvas.save()
//    canvas.clipPath(diagramBounds)
    var previousMonthX = -Float.MAX_VALUE
    for (i in 0..mainVerticalBarCount-1) {
        val lineX = ((i + 0.5f) / mainVerticalBarCount) * diagramWidth + horizontalPadding
        canvas.drawLine(lineX, verticalPadding, lineX, verticalPadding + tickLength, paint)
        canvas.drawLine(
            lineX,
            canvas.height - verticalPadding,
            lineX,
            canvas.height - verticalPadding - tickLength,
            paint
        )
        val date = now.plusDays(i.toLong())
        val dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEE"))
        val dayOfMonth = date.format(DateTimeFormatter.ofPattern("MMM d")).uppercase()

        val textBound = Rect()
        textPaint.getTextBounds("123", 0, 2, textBound)
        val dayTextBaseline = frameHeight + verticalPadding + textBound.height() + tickLength
        canvas.drawText(dayOfWeek, lineX, dayTextBaseline, textPaint)
        if (lineX - previousMonthX > 1.7f * textPaint.measureText(dayOfMonth)) {
            canvas.drawText(dayOfMonth, lineX, verticalPadding - tickLength, textPaint)
            previousMonthX = lineX
        }
    }
    canvas.restoreToCount(saved)


}

private fun drawVerticalLegend(
    canvas: Canvas,
    yAxisRange: ClosedFloatingPointRange<Float>,
    chartTitle: String,
    chartQuantity: DailyWeatherQuantity,
    dataMax: Float,
    dataMin: Float,
    @ColorInt titleColor: Int,
    @ColorInt tickColor: Int,
    diagramHeight: Float,
//    diagramBounds: Path,
    horizontalPadding: Float,
    verticalPadding: Float,
    tickLength: Float,
    tickWidth: Float,
    fontSize: Float,
    font: Typeface
) {
    val tickPaint = Paint().apply {
        color = tickColor
        strokeWidth = tickWidth
        style = Style.STROKE
    }

    val textPaint = TextPaint().apply {
        textSize = fontSize
        typeface = font
        style = Style.FILL_AND_STROKE
        strokeWidth = 1f
        textAlign = Align.CENTER
    }
    for (i in 0..3) {
        val lineY = ((i + 0.5f) / 4f) * diagramHeight + verticalPadding
        canvas.drawLine(horizontalPadding, lineY, horizontalPadding + tickLength, lineY, tickPaint)
    }
    val textBounds = Rect()
    textPaint.getTextBounds("123", 0, 2, textBounds)

    canvas.save()
    canvas.rotate(90f, 0f, 0f)
    for (i in 0..3) {
        val lineY = ((i + 0.5f) / 4f) * diagramHeight + verticalPadding
        val separation = (yAxisRange.endInclusive - yAxisRange.start) / 4f
        val barValue = yAxisRange.endInclusive - (i + 0.5f) * separation
        val text = if (chartQuantity == DailyWeatherQuantity.SUNRISESET) barValue.toTimeString()
        else barValue.floatToString(separation)

        val textBaseline = -horizontalPadding + textBounds.height() + tickLength
        textPaint.color = chartQuantity.color(
            if (chartQuantity == DailyWeatherQuantity.UVINDEXMAX) barValue/11
            else {
                if (dataMax == dataMin) 0f else ((barValue - dataMin) / (dataMax - dataMin))
            }
        ).toArgb()
        canvas.drawText(text, lineY, textBaseline, textPaint)
    }
    canvas.restore()

    canvas.save()
    val textBaseline = canvas.width - horizontalPadding + textBounds.height() + tickLength
    canvas.rotate(-90f, 0f, 0f)
    textPaint.color = titleColor
    canvas.drawText(chartTitle, -(diagramHeight/2 + verticalPadding), textBaseline, textPaint)
    canvas.restore()

}