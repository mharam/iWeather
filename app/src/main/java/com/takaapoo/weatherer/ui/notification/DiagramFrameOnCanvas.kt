package com.takaapoo.weatherer.ui.notification

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.min

fun diagramFrameOnCanvas(
    canvas: Canvas,
    cornerRadius: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
    x1: Long,
    mainBarColor: Int,
    minorBarColor: Int,
    minorBarVisible: Boolean,
    majorBarVisible: Boolean,
    mainBarWidthPx: Float,
    minorBarWidthPx: Float,
    frameHeight: Float,
    horizontalMinorBarCount: Int = 1
) {
    val diagramRect = RectF(
        horizontalPadding,
        verticalPadding,
        canvas.width - horizontalPadding,
        min(canvas.height - verticalPadding, frameHeight + verticalPadding)
    )

    val diagramPath = Path().apply {
        addRoundRect(diagramRect, cornerRadius, cornerRadius, Path.Direction.CW)
    }
    val diagramWidth = diagramRect.width()
    val diagramHeight = diagramRect.height()

    val dottedLine = floatArrayOf(2 * mainBarWidthPx, 4 * mainBarWidthPx)

    canvas.save()
    canvas.clipPath(diagramPath)

    val mainBarPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = mainBarColor
        strokeWidth = mainBarWidthPx
        style = Paint.Style.STROKE
    }

    val minorBarPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = minorBarColor
        strokeWidth = mainBarWidthPx
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(dottedLine, 0f)
    }
    val yellowRect = RectF(
        (x1 / 48f) * diagramWidth + horizontalPadding,
        -1f,
        ((x1 + 24) / 48f) * diagramWidth + horizontalPadding,
        canvas.height + 1f,
    )
    canvas.drawRect(
        yellowRect,
        Paint().apply { color = Color.Green.copy(alpha = 0.15f).toArgb() }
    )

    if (majorBarVisible) {
        for (i in 1..7) {
            val lineX = (i / 8f) * diagramWidth + horizontalPadding
            canvas.drawLine(lineX, verticalPadding, lineX, canvas.height - verticalPadding, mainBarPaint)
        }
        for (i in 1..2) {
            val lineY = (i / 3f) * diagramHeight + verticalPadding
            canvas.drawLine(
                horizontalPadding,
                lineY,
                canvas.width - horizontalPadding,
                lineY,
                mainBarPaint
            )
        }
    }

    if (minorBarVisible) {
        for (i in 0..7) {
            val lineX = ((i + 0.5f) / 8f) * diagramWidth + horizontalPadding
            canvas.drawLine(
                lineX,
                verticalPadding,
                lineX,
                canvas.height - verticalPadding,
                minorBarPaint
            )
        }
        for (i in 0 until 3 * (horizontalMinorBarCount + 1)) {
//            val lineY = ((i + 0.5f) / 3f) * diagramHeight + verticalPadding
            val lineY = ( (1f + i)/((horizontalMinorBarCount + 1) * 3f)) *
                    diagramHeight + verticalPadding
            canvas.drawLine(
                horizontalPadding,
                lineY,
                canvas.width - horizontalPadding,
                lineY,
                minorBarPaint
            )
        }
    }

    canvas.restore()
}