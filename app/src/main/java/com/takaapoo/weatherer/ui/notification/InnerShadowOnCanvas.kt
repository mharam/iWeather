package com.takaapoo.weatherer.ui.notification

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.toArgb
import com.takaapoo.weatherer.ui.theme.Gray10

fun innerShadowOnCanvas(
    canvas: Canvas,
    @ColorInt color: Int = Gray10.toArgb(),
    rect: RectF,
    cornersRadius: Float,
    spread: Float = 1f,
    blur: Float = 8f,
    offsetY: Float = 3f,
    offsetX: Float = 2f
) {
    val paint = Paint().apply {
        isAntiAlias = true
    }

    val saved = canvas.saveLayer(rect, null)

    paint.color = color
    canvas.drawRoundRect(rect, cornersRadius, cornersRadius, paint)

    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    if (blur > 0) {
        paint.maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
    }

    val left = rect.left + (if (offsetX > 0) offsetX else 0f) + spread / 2
    val top = rect.top + (if (offsetY > 0) offsetY else 0f) + spread / 2
    val right = rect.right + (if (offsetX < 0) offsetX else 0f) + spread / 2
    val bottom = rect.bottom + (if (offsetY < 0) offsetY else 0f) + spread / 2

    paint.color = Color.BLACK
    canvas.drawRoundRect(
        left,
        top,
        right,
        bottom,
        cornersRadius,
        cornersRadius,
        paint
    )
    paint.xfermode = null
    paint.maskFilter = null

    canvas.restoreToCount(saved)
}