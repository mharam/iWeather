package com.takaapoo.weatherer.ui.notification.custom_views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.takaapoo.weatherer.utility.toPx

class CurrentIcon(
    context: Context,
    @DrawableRes val iconId: Int,
    colorScheme: ColorScheme
): View(context) {
    val myDensity = Density(context)
    val pointRadius = 4.dp.toPx(myDensity)

    private val circleFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorScheme.primary.toArgb()
    }
    private val circleBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = colorScheme.primaryContainer.toArgb()
        strokeWidth = 2.dp.toPx(myDensity)
    }
    val iconDrawable = ContextCompat.getDrawable(context, iconId)
    val iconColorFilter = PorterDuffColorFilter(
        colorScheme.onPrimary.toArgb(),
        PorterDuff.Mode.SRC_IN
    )
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorScheme.primaryContainer.toArgb()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            drawCircle(height.toFloat(), height/2f, pointRadius, pointPaint)
            drawCircle(height/2f, height/2f, (height/2f) - 1.dp.toPx(myDensity), circleFillPaint)
            drawCircle(height/2f, height/2f, (height/2f) - 1.dp.toPx(myDensity), circleBorderPaint)
            iconDrawable?.apply {
                setBounds(height/4, height/4, 3*height/4, 3*height/4)
                colorFilter = iconColorFilter
                draw(canvas)
            }
        }
    }


}