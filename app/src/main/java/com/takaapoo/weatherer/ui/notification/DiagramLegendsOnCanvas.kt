package com.takaapoo.weatherer.ui.notification

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Align
import android.graphics.Paint.Style
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.mainGridWidthDp
import com.takaapoo.weatherer.ui.theme.DiagramGrid
import com.takaapoo.weatherer.ui.theme.DiagramShadow
import com.takaapoo.weatherer.ui.theme.Gray20
import com.takaapoo.weatherer.utility.toPx
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.roundToInt

fun diagramLegendsOnCanvas(
    canvas: Canvas,
    yAxesRanges: List<ClosedFloatingPointRange<Float>>,
    yAxesColors: List<Color>,
    cornerRadius: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
    @ColorInt onAppSurfaceColor: Int,
    @ColorInt textColor: Int,
    utcOffset: Long,
    density: Density,
    timeFont: Typeface,
    yAxesFont: Typeface,
    zone: ZoneId,
    frameHeight: Float,
) {
    val diagramRectangle = RectF(
        horizontalPadding,
        verticalPadding,
        canvas.width - horizontalPadding,
        min(canvas.height - verticalPadding, frameHeight + verticalPadding),
    )
    val diagramBounds = Path().apply {
        addRoundRect(diagramRectangle, cornerRadius, cornerRadius, Path.Direction.CW)
    }
    val diagramWidth = diagramRectangle.width()
    val diagramHeight = diagramRectangle.height()
    val tickLength = 6.dp.toPx(density)
    val tickWidth = 2 * mainGridWidthDp.toPx(density)
    val mainVerticalBarCount = 7

    val now = LocalDateTime.now(zone).truncatedTo(ChronoUnit.HOURS)
    drawClockLegend(
        canvas = canvas,
        tickColor = onAppSurfaceColor,
        now = now,
        mainVerticalBarCount = mainVerticalBarCount,
        diagramWidth = diagramWidth,
        diagramBounds = diagramBounds,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        tickLength = tickLength,
        tickWidth = tickWidth,
        textColor = textColor,
        fontSize = 14.sp.toPx(density),
        timeFont = timeFont,
        frameHeight = frameHeight
    )
    drawDayLegend(
        canvas = canvas,
        now = now,
        diagramWidth = diagramWidth,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        textColor = textColor,
        density = density,
        fontSize = 14.sp.toPx(density),
        timeFont = timeFont
    )
    drawVerticalLegend(
        canvas = canvas,
        yAxesRanges = yAxesRanges,
        tickColor = onAppSurfaceColor,
        diagramHeight = diagramHeight,
        diagramBounds = diagramBounds,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        tickLength = tickLength,
        tickWidth = tickWidth,
        yAxesColors = yAxesColors,
        fontSize = 14.sp.toPx(density),
        font = yAxesFont
    )

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



private fun drawClockLegend(
    canvas: Canvas,
    @ColorInt tickColor: Int,
    now: LocalDateTime,
    mainVerticalBarCount: Int,
    diagramWidth: Float,
    diagramBounds: Path,
    horizontalPadding: Float,
    verticalPadding: Float,
    tickLength: Float,
    tickWidth: Float,
    @ColorInt textColor: Int,
    fontSize: Float,
    timeFont: Typeface,
    frameHeight: Float,
) {
    val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = tickWidth
        color = tickColor
    }

    val textPaint = TextPaint().apply {
        isAntiAlias = true
        typeface = timeFont
        color = textColor
        textSize = fontSize
    }

    val saved = canvas.save()
    canvas.clipPath(diagramBounds)
    for (i in 1..mainVerticalBarCount) {
        val lineX = (i / (mainVerticalBarCount + 1f)) * diagramWidth + horizontalPadding
        canvas.drawLine(lineX, verticalPadding, lineX, verticalPadding + tickLength, paint)
        canvas.drawLine(
            lineX,
            frameHeight + verticalPadding,
            lineX,
            frameHeight + verticalPadding - tickLength,
            paint
        )
    }
    canvas.restoreToCount(saved)

    val textBound = Rect()
    textPaint.getTextBounds("123", 0, 2, textBound)
    for (i in 1..mainVerticalBarCount) {
        val lineX = (i / (mainVerticalBarCount + 1f)) * diagramWidth + horizontalPadding
        val dateTime = now.plusHours((i * 6).toLong())
        val clock = dateTime.format(DateTimeFormatter.ofPattern("HH"))

        val textWidth = textPaint.measureText(clock)

        val clockTextLeft = lineX - textWidth / 2f
        val clockTextBaseline = frameHeight + verticalPadding + textBound.height() + tickLength
        canvas.drawText(clock, clockTextLeft, clockTextBaseline, textPaint)
    }
}

private fun drawDayLegend(
    canvas: Canvas,
    now: LocalDateTime,
    diagramWidth: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
    @ColorInt textColor: Int,
    density: Density,
    fontSize: Float,
    timeFont: Typeface
) {
    val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 2f
        color = DiagramGrid.toArgb()
    }

    val textPaint = TextPaint().apply {
        isAntiAlias = true
        color = textColor
        typeface = timeFont
        textSize = fontSize
    }

    val x1 = ChronoUnit.HOURS.between(now, now.toLocalDate().atStartOfDay().plusDays(1))

    for (i in -1..1) {
        if (x1 + i * 24 >= 0 && x1 + i * 24 <= 48) {
            val lineX = ((x1 + i * 24) / 48f) * diagramWidth + horizontalPadding
            canvas.drawLine(lineX, 0f, lineX, verticalPadding - 4.dp.toPx(density), paint)
        }
    }

    val dayOfWeek = now.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
    val dayTextWidth = textPaint.measureText(dayOfWeek)
    val arrowGap = 8.dp.toPx(density)
    val arrowY = (verticalPadding - 4.dp.toPx(density)) / 2
    val dayOfWeekBound = Rect()
    textPaint.getTextBounds(dayOfWeek, 0, dayOfWeek.length, dayOfWeekBound)
    val dayTextBaseline = arrowY + dayOfWeekBound.height() / 2f

    if ((x1 / 48f) * diagramWidth > dayTextWidth + 24.dp.toPx(density)) {
        val dayTextLeft = minOf(
            (x1 / 48f) * diagramWidth - 24.dp.toPx(density) - dayTextWidth,
            ((x1 / 48f) * diagramWidth - dayTextWidth) / 2
        ) + horizontalPadding

        canvas.drawText(dayOfWeek, dayTextLeft, dayTextBaseline, textPaint)

        // Draw left arrow
        if (dayTextLeft - 16.dp.toPx(density) > horizontalPadding) {
            drawHorizontalArrow(
                canvas = canvas,
                color = DiagramGrid.toArgb(),
                start = Offset(dayTextLeft - arrowGap, arrowY),
                end = Offset(horizontalPadding, arrowY),
                strokeWidth = mainGridWidthDp.toPx(density),
                density = density
            )
        }
        // Draw right arrow
        drawHorizontalArrow(
            canvas = canvas,
            color = DiagramGrid.toArgb(),
            start = Offset(dayTextLeft + dayTextWidth + arrowGap, arrowY),
            end = Offset((x1 / 48f) * diagramWidth + horizontalPadding, arrowY),
            strokeWidth = mainGridWidthDp.toPx(density),
            density = density
        )
    }

    val nextDay = now.plusDays(1).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
    val nextDayTextWidth = textPaint.measureText(nextDay)
    val nextDayTextLeft = ((x1 + 12) / 48f) * diagramWidth + horizontalPadding - nextDayTextWidth / 2

    canvas.drawText(nextDay, nextDayTextLeft, dayTextBaseline, textPaint)

    // Draw left arrow for next day
    drawHorizontalArrow(
        canvas = canvas,
        color = DiagramGrid.toArgb(),
        start = Offset(nextDayTextLeft - arrowGap, arrowY),
        end = Offset((x1 / 48f) * diagramWidth + horizontalPadding, arrowY),
        strokeWidth = mainGridWidthDp.toPx(density),
        density = density
    )

    // Draw right arrow for next day
    drawHorizontalArrow(
        canvas = canvas,
        color = DiagramGrid.toArgb(),
        start = Offset(nextDayTextLeft + nextDayTextWidth + arrowGap, arrowY),
        end = Offset(((x1 + 24) / 48f) * diagramWidth + horizontalPadding, arrowY),
        strokeWidth = mainGridWidthDp.toPx(density),
        density = density
    )

    // Draw day after next label and arrows
    val dayAfterNext = now.plusDays(2).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
    val dayAfterNextTextWidth = textPaint.measureText(dayAfterNext)

    if ((1 - (x1 + 24) / 48f) * diagramWidth > dayAfterNextTextWidth + 24.dp.toPx(density)) {
        val dayAfterNextTextLeft = maxOf(
            ((x1 + 24) / 48f) * diagramWidth + 24.dp.toPx(density),
            (diagramWidth + ((x1 + 24) / 48f) * diagramWidth - dayAfterNextTextWidth) / 2
        ) + horizontalPadding

        // Draw day after next label
        canvas.drawText(dayAfterNext, dayAfterNextTextLeft, dayTextBaseline, textPaint)

        // Draw right arrow for day after next
        if (dayAfterNextTextLeft + dayAfterNextTextWidth + 16.dp.toPx(density) < diagramWidth + horizontalPadding) {
            drawHorizontalArrow(
                canvas = canvas,
                color = DiagramGrid.toArgb(),
                start = Offset(dayAfterNextTextLeft + dayAfterNextTextWidth + arrowGap, arrowY),
                end = Offset(diagramWidth + horizontalPadding, arrowY),
                strokeWidth = mainGridWidthDp.toPx(density),
                density = density
            )
        }
        // Draw left arrow for day after next
        drawHorizontalArrow(
            canvas = canvas,
            color = DiagramGrid.toArgb(),
            start = Offset(dayAfterNextTextLeft - arrowGap, arrowY),
            end = Offset(((x1 + 24) / 48f) * diagramWidth + horizontalPadding, arrowY),
            strokeWidth = mainGridWidthDp.toPx(density),
            density = density
        )
    }
}

private fun drawHorizontalArrow(
    canvas: Canvas,
    @ColorInt color: Int,
    start: Offset,
    end: Offset,
    strokeWidth: Float,
    density: Density
) {
    val paint = Paint().apply {
        isAntiAlias = true
        setColor(color)
        setStrokeWidth(strokeWidth)
        style = Style.STROKE
    }

    val direction = if (end.x > start.x) 1 else -1
    canvas.drawLine(start.x, start.y, end.x - direction * 4f, end.y, paint)

    val arrowHead = Path().apply {
        moveTo(end.x, end.y)
        lineTo(end.x - direction * 8.dp.toPx(density), end.y + 2.dp.toPx(density))
        lineTo(end.x - direction * 8.dp.toPx(density), end.y - 2.dp.toPx(density))
        close()
    }
    paint.style = Style.FILL
    canvas.drawPath(arrowHead, paint)
}

private fun drawVerticalLegend(
    canvas: Canvas,
    yAxesRanges: List<ClosedFloatingPointRange<Float>>,
    @ColorInt tickColor: Int,
    diagramHeight: Float,
    diagramBounds: Path,
    horizontalPadding: Float,
    verticalPadding: Float,
    tickLength: Float,
    tickWidth: Float,
    yAxesColors: List<Color>,
    fontSize: Float,
    font: Typeface
) {
    if (yAxesRanges.isNotEmpty()) {
        val paint = Paint().apply {
            color = tickColor
            strokeWidth = tickWidth
            style = Style.STROKE
        }

        val textPaint = TextPaint().apply {
            color = Color.Black.toArgb()
            textSize = fontSize
            typeface = font
            style = Style.FILL_AND_STROKE
            strokeWidth = 1f
            textAlign = Align.CENTER
        }

        for (i in 1..2) {
            val lineY = (i / 3f) * diagramHeight + verticalPadding
            canvas.save()
            canvas.clipPath(diagramBounds)
            canvas.drawLine(horizontalPadding, lineY, horizontalPadding + tickLength, lineY, paint)
            canvas.drawLine(
                canvas.width - horizontalPadding,
                lineY,
                canvas.width - horizontalPadding - tickLength,
                lineY,
                paint
            )
            canvas.restore()
        }

        val textBounds = Rect()
        textPaint.getTextBounds("123", 0, 2, textBounds)
        for (i in 0..3) {
            val lineY = (i / 3f) * diagramHeight + verticalPadding
            yAxesRanges.forEachIndexed { index, range ->
                canvas.save()
                val separation = (range.endInclusive - range.start) / 3f
                val barValue = range.endInclusive - i * separation
                val text = barValue.floatToString(separation)

                val textTop = when (index) {
                    0 -> -horizontalPadding + textBounds.height() + tickLength
                    1 -> canvas.width - horizontalPadding + textBounds.height() + tickLength
                    2 -> -2.5f * horizontalPadding
                    else -> canvas.width - 2.5f * horizontalPadding
                }

                canvas.rotate(if (index % 2 == 0) 90f else -90f, 0f, 0f)
                textPaint.color = yAxesColors[index].toArgb()
                canvas.drawText(
                    text,
                    (if (index % 2 == 0) 1 else -1) * lineY,
                    textTop,
                    textPaint
                )

                canvas.restore()
            }
        }
    }
}

fun Float.floatToString(separation: Float): String {
    val order = log10(separation)
    if (abs(this) / separation < 0.01) return "0"
    return when {
        order > 2 -> "%d".format(this.roundToInt())
        order > -0.01f -> {
            if (abs(this - this.roundToInt()) < 0.1f) "%d".format(this.roundToInt())
            else "%.1f".format(this)
        }
        else -> "%.${abs(floor(order)).toInt()}f".format(this)
    }
}