package com.takaapoo.weatherer.ui.notification

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.min

fun dailyDiagramFrameOnCanvas(
    canvas: Canvas,
    cornerRadius: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
    mainBarColor: Int,
    minorBarColor: Int,
    minorBarVisible: Boolean,
    majorBarVisible: Boolean,
    mainBarWidthPx: Float,
    minorBarWidthPx: Float,
    frameHeight: Float,
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

    if (majorBarVisible) {
        for (i in 0..4) {
            val lineX = ((i + 0.5f) / 5f) * diagramWidth + horizontalPadding
            canvas.drawLine(lineX, verticalPadding, lineX, canvas.height - verticalPadding, mainBarPaint)
        }
        for (i in 0..3) {
            val lineY = ((i + 0.5f) / 4f) * diagramHeight + verticalPadding
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
        for (i in 1..3) {
            val lineY = (i / 4f) * diagramHeight + verticalPadding
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