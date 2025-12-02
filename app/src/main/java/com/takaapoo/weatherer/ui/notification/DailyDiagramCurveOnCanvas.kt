package com.takaapoo.weatherer.ui.notification


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Align
import android.graphics.Paint.Style
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.graphics.Typeface
import android.text.TextPaint
import android.util.SizeF
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.graphics.withRotation
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.toTimeString
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.normalizedX
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.normalizedY
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.utility.toPx
import kotlin.math.min


fun dailyDiagramCurveOnCanvas(
    context: Context,
    canvas: Canvas,
    data: List<ClosedFloatingPointRange<Float>?>,
    dataMax: Float,
    dataMin: Float,
    firstPointX: Float,
    weatherQuantity: DailyWeatherQuantity,
    cornerRadius: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxisRange: ClosedFloatingPointRange<Float>,
    curveValueColor: Int,
    fontSize: Float,
    font: Typeface,
    frameHeight: Float
) {
    if (data.isEmpty()) return
    val myDensity = Density(context)

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

//    canvas.save()
    val saved = canvas.save()
    canvas.clipPath(diagramPath)
    canvas.saveLayer(RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat()), Paint())
    diagramBarOnCanvas(
        density = myDensity,
        canvas = canvas,
        weatherQuantity = weatherQuantity,
        data = data,
        dataMin = dataMin,
        dataMax = dataMax,
        curveValueColor = curveValueColor,
        firstPointX = firstPointX,
        diagramWidth = diagramWidth,
        diagramHeight = diagramHeight,
        xAxisRange = xAxisRange,
        yAxisRange = yAxisRange,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        fontSize = fontSize,
        font = font
    )
    canvas.restoreToCount(saved)
}



fun diagramBarOnCanvas(
    density: Density,
    canvas: Canvas,
    weatherQuantity: DailyWeatherQuantity,
    data: List<ClosedFloatingPointRange<Float>?>,
    dataMin: Float,
    dataMax: Float,
    @ColorInt curveValueColor: Int, // Use Int instead of Compose Color
    firstPointX: Float,
    diagramWidth: Float,
    diagramHeight: Float,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxisRange: ClosedFloatingPointRange<Float>,
    horizontalPadding: Float,
    verticalPadding: Float,
    fontSize: Float,
    font: Typeface
) {
    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val yAxisLength = yAxisRange.endInclusive - yAxisRange.start
    var x = firstPointX

    val rectangleWidth = 8.dp.toPx(density)
    val rectangleShadowWidth = 6.dp.toPx(density)
    val cornerRadius = rectangleWidth / 2

    val eraseColor = Color.argb(0xAF, 0xFF, 0xFF, 0xFF)
    val transparentColor = Transparent.toArgb()

    val barPaint = Paint(ANTI_ALIAS_FLAG).apply {
        style = Style.FILL
    }
    val erasePaint = Paint(ANTI_ALIAS_FLAG).apply {
        style = Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    data.forEach { dataRange ->
        dataRange?.let {
            val rectangleHeight = (it.endInclusive - it.start) * diagramHeight / yAxisLength

            val left = normalizedX(
                x,
                xAxisRange.start,
                xAxisLength,
                horizontalPadding,
                diagramWidth
            ) - rectangleWidth / 2
            val top = normalizedY(
                dataRange.endInclusive,
                yAxisRange.start,
                yAxisLength,
                verticalPadding,
                diagramHeight
            )

            val barBrush = curveShader(weatherQuantity, dataMin, dataMax, diagramHeight, yAxisRange, verticalPadding)
            barPaint.shader = barBrush

            val shadowSize = SizeF(
                rectangleWidth + 2 * rectangleShadowWidth,
                rectangleHeight + 2 * rectangleShadowWidth
            )

            when (weatherQuantity) {
                DailyWeatherQuantity.TEMPERATUREMINMAX, DailyWeatherQuantity.SUNRISESET -> {
                    canvas.drawRoundRect(
                        left - rectangleShadowWidth,
                        top - rectangleShadowWidth,
                        left - rectangleShadowWidth + shadowSize.width,
                        top - rectangleShadowWidth + shadowSize.height,
                        shadowSize.width / 2,
                        shadowSize.width / 2,
                        barPaint
                    )
                    drawEraseArcsAndRect(
                        canvas,
                        left,
                        top,
                        rectangleWidth,
                        rectangleHeight,
                        cornerRadius,
                        rectangleShadowWidth,
                        eraseColor,
                        transparentColor,
                        erasePaint
                    )
                    canvas.drawRoundRect(
                        left,
                        top,
                        left + rectangleWidth,
                        top + rectangleHeight,
                        cornerRadius,
                        cornerRadius,
                        barPaint
                    )
                }
                else -> {
                    if (rectangleHeight > 0.001f) {
                        val path = Path().apply {
                            moveTo(left, top + cornerRadius)
                            arcTo(
                                RectF(
                                    left,
                                    top,
                                    left + rectangleWidth,
                                    top + rectangleWidth.coerceAtMost(2 * rectangleHeight)
                                ),
                                180f,
                                180f,
                                true
                            )
                            lineTo(left + rectangleWidth, top + rectangleHeight)
                            lineTo(left, top + rectangleHeight)
                            close()
                        }

                        val shadowPath = Path().apply {
                            moveTo(left - rectangleShadowWidth, top + cornerRadius)
                            arcTo(
                                RectF(
                                    left - rectangleShadowWidth,
                                    top - rectangleShadowWidth,
                                    left + rectangleWidth + rectangleShadowWidth,
                                    top + (rectangleWidth + rectangleShadowWidth)
//                                        .coerceAtMost(2 * (rectangleHeight + rectangleShadowWidth))
                                ),
                                180f,
                                180f,
                                true
                            )
                            lineTo(left + rectangleWidth + rectangleShadowWidth, top + rectangleHeight)
                            lineTo(left - rectangleShadowWidth, top + rectangleHeight)
                            close()
                        }

                        canvas.drawPath(shadowPath, barPaint)
                        canvas.drawArc(
                            left - rectangleShadowWidth - 1f,
                            top - rectangleShadowWidth - 1f,
                            left + rectangleWidth + rectangleShadowWidth + 1f,
                            top + rectangleWidth + rectangleShadowWidth + 1.5f,
                            180f,
                            180f,
                            true,
                            erasePaint.apply {
                                shader = RadialGradient(
                                    left + cornerRadius,
                                    top + cornerRadius,
                                    cornerRadius + rectangleShadowWidth,
                                    eraseColor,
                                    transparentColor,
                                    TileMode.CLAMP
                                )
                            }
                        )
                        canvas.drawRect(
                            left - rectangleShadowWidth - 1f,
                            top + cornerRadius,
                            left + rectangleWidth + rectangleShadowWidth + 1f,
                            top + rectangleHeight,
                            erasePaint.apply {
                                shader = LinearGradient(
                                    left - rectangleShadowWidth,
                                    0f,
                                    left + rectangleWidth + rectangleShadowWidth,
                                    0f,
                                    intArrayOf(transparentColor, eraseColor, transparentColor),
                                    floatArrayOf(0f, 0.5f, 1f),
                                    TileMode.CLAMP
                                )
                            }
                        )
                        canvas.drawPath(path, barPaint)
                    }
                }
            }

            // Draw text
            val maxValue = if (weatherQuantity == DailyWeatherQuantity.SUNRISESET)
                dataRange.endInclusive.toTimeString()
            else "%.${weatherQuantity.floatingPointDigits}f".format(dataRange.endInclusive)

            val minValue = if (weatherQuantity == DailyWeatherQuantity.SUNRISESET)
                dataRange.start.toTimeString()
            else "%.${weatherQuantity.floatingPointDigits}f".format(dataRange.start)
            val textPaint = TextPaint().apply {
                textSize = fontSize
                typeface = font
                style = Style.FILL_AND_STROKE
                strokeWidth = 1f
            }

            val textBounds = Rect()
            canvas.withRotation(
                degrees = 90f,
                pivotX = 0f,
                pivotY = 0f
            ) {
                val zeroHeight = dataRange.endInclusive == dataRange.start && dataRange.start == 0f
                drawText(
                    maxValue,
                    if (zeroHeight) top - 20f else top,
                    if (zeroHeight) -left else -left - rectangleWidth - rectangleShadowWidth,
                    textPaint.apply {
                        color = if (weatherQuantity == DailyWeatherQuantity.UVINDEXMAX) curveValueColor
                        else weatherQuantity.color(
                            fraction = if (dataMax == dataMin) 0f
                            else (dataRange.endInclusive - dataMin) / (dataMax - dataMin)
                        ).toArgb()
                        textAlign = if (zeroHeight) Align.RIGHT else Align.LEFT
                    }
                )
                if (weatherQuantity == DailyWeatherQuantity.TEMPERATUREMINMAX ||
                    weatherQuantity == DailyWeatherQuantity.SUNRISESET) {
                    textPaint.getTextBounds(minValue, 0, minValue.length, textBounds)
                    drawText(
                        minValue,
                        top + rectangleHeight,
                        -left + rectangleShadowWidth + textBounds.height(),
                        textPaint.apply {
                            color = weatherQuantity.color(
                                fraction = if (dataMax == dataMin) 0f
                                else (dataRange.start - dataMin) / (dataMax - dataMin)
                            ).toArgb()
                            textAlign = Align.RIGHT
                        }
                    )
                }
            }
        }
        x++
    }
}



fun curveShader(
    weatherQuantity: DailyWeatherQuantity,
    dataMin: Float,
    dataMax: Float,
    diagramHeight: Float,
    yAxisRange: ClosedFloatingPointRange<Float>,
    verticalPadding: Float
): Shader {
    val yAxisLength = yAxisRange.endInclusive - yAxisRange.start

    return when (weatherQuantity) {
        DailyWeatherQuantity.TEMPERATUREMINMAX, DailyWeatherQuantity.SUNRISESET -> {
            val startY = normalizedY(
                dataMax,
                yAxisRange.start,
                yAxisLength,
                verticalPadding,
                diagramHeight
            )
            val endY = normalizedY(
                dataMin,
                yAxisRange.start,
                yAxisLength,
                verticalPadding,
                diagramHeight
            )
            LinearGradient(
                0f, startY, 0f, endY,
                weatherQuantity.brushColors.map { it.toArgb() }.toIntArray(),
                null,
                TileMode.CLAMP
            )
        }
        DailyWeatherQuantity.UVINDEXMAX -> {
            val startY = normalizedY(
                0f,
                yAxisRange.start,
                yAxisLength,
                verticalPadding,
                diagramHeight
            )
            val endY = normalizedY(
                11f,
                yAxisRange.start,
                yAxisLength,
                verticalPadding,
                diagramHeight
            )
            LinearGradient(
                0f, startY, 0f, endY,
                weatherQuantity.brushColors.map { it.toArgb() }.toIntArray(),
                null,
                TileMode.CLAMP
            )
        }
        DailyWeatherQuantity.PRECIPITATIONSUM, DailyWeatherQuantity.WINDSPEEDMAX,
        DailyWeatherQuantity.TEMPERATUREMEAN -> {
            LinearGradient(
                0f, 0f, 0f, diagramHeight,
                weatherQuantity.brushColors.map { it.toArgb() }.toIntArray(),
                null,
                TileMode.CLAMP
            )
        }
        DailyWeatherQuantity.PRECIPITATIONPROBABILITYMAX, DailyWeatherQuantity.CLOUDCOVERMEAN,
        DailyWeatherQuantity.HUMIDITYMEAN -> {
            val startY = normalizedY(
                0f,
                yAxisRange.start,
                yAxisLength,
                verticalPadding,
                diagramHeight
            )
            val endY = normalizedY(
                100f,
                yAxisRange.start,
                yAxisLength,
                verticalPadding,
                diagramHeight
            )
            LinearGradient(
                0f, startY, 0f, endY,
                weatherQuantity.brushColors.map { it.toArgb() }.toIntArray(),
                null,
                TileMode.CLAMP
            )
        }
    }
}


private fun drawEraseArcsAndRect(
    canvas: Canvas,
    left: Float,
    top: Float,
    rectangleWidth: Float,
    rectangleHeight: Float,
    cornerRadius: Float,
    rectangleShadowWidth: Float,
    eraseColor: Int,
    transparentColor: Int,
    erasePaint: Paint
) {
    // Draw top erase arc
    canvas.drawArc(
        left - rectangleShadowWidth - 1f,
        top - rectangleShadowWidth - 1f,
        left + rectangleWidth + rectangleShadowWidth + 1f,
        top + rectangleWidth + rectangleShadowWidth + 1.5f,
        180f,
        180f,
        true,
        erasePaint.apply {
            shader = RadialGradient(
                left + cornerRadius,
                top + cornerRadius,
                cornerRadius + rectangleShadowWidth,
                eraseColor,
                transparentColor,
                TileMode.CLAMP
            )
        }
    )

    // Draw bottom erase arc
    canvas.drawArc(
        left - rectangleShadowWidth - 1f,
        top + rectangleHeight - 2 * cornerRadius - rectangleShadowWidth - 1.5f,
        left + rectangleWidth + rectangleShadowWidth + 1f,
        top + rectangleHeight + rectangleWidth - 2 * cornerRadius + rectangleShadowWidth + 1f,
        0f,
        180f,
        true,
        erasePaint.apply {
            shader = RadialGradient(
                left + cornerRadius,
                top + rectangleHeight - cornerRadius,
                cornerRadius + rectangleShadowWidth,
                eraseColor,
                transparentColor,
                TileMode.CLAMP
            )
        }
    )

    // Draw middle erase rect
    canvas.drawRect(
        left - rectangleShadowWidth - 1f,
        top + cornerRadius,
        left + rectangleWidth + rectangleShadowWidth + 1f,
        top + rectangleHeight - cornerRadius,
        erasePaint.apply {
            shader = LinearGradient(
                left - rectangleShadowWidth,
                0f,
                left + rectangleWidth + rectangleShadowWidth,
                0f,
                intArrayOf(transparentColor, eraseColor, transparentColor),
                floatArrayOf(0f, 0.5f, 1f),
                TileMode.CLAMP
            )
        }
    )
}
