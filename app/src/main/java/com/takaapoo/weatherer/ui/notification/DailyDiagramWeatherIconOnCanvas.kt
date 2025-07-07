package com.takaapoo.weatherer.ui.notification

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.WeatherType.Companion.drawWeatherIconOnCanvas


fun dailyDiagramWeatherIconOnCanvas(
    canvas: Canvas,
    context: Context,
    weatherCodes: List<Int?>,
    cornerRadius: Float,
    iconSizePx: Int,
    horizontalPadding: Float,
    verticalPadding: Float,
) {
    val diagramRect = RectF(
        horizontalPadding,
        verticalPadding,
        canvas.width - horizontalPadding,
        canvas.height - verticalPadding
    )
    val diagramPath = Path().apply {
        addRoundRect(diagramRect, cornerRadius, cornerRadius, Path.Direction.CW)
    }
    val diagramWidth = diagramRect.width()

    canvas.save()
    canvas.clipPath(diagramPath)
    for (i in 0..4) {
        val lineX = ((i + 0.5f) / 5f) * diagramWidth + horizontalPadding
        weatherCodes.getOrNull(i)?.let { weatherCode: Int ->
            val topLeft = IntOffset(
                x = lineX.toInt() - iconSizePx / 2,
                y = 30 + verticalPadding.toInt()
            )
            val weatherType = WeatherType.fromWMO(weatherCode, true, null)
            drawWeatherIconOnCanvas(
                canvas,
                weatherType,
                context,
                topLeft,
                iconSize = IntSize(iconSizePx, iconSizePx),
                Paint()
            )
        }
    }
    canvas.restore()
}
