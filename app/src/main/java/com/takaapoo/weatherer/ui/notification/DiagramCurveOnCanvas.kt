package com.takaapoo.weatherer.ui.notification


import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.ui.notification.custom_views.timeToX
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.GraphTypes
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.generateCubicGraph
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.generateLinearGraph
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.generateStepGraph
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.quantityControlPoints
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.quantityData
import com.takaapoo.weatherer.ui.theme.Transparent


fun diagramCurveOnCanvas(
    canvas: Canvas,
    hourlyData: List<HourlyChartDto?>,
    hourlyWeatherQuantity: HourlyWeatherQuantity,
    cornerRadius: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxisRange: ClosedFloatingPointRange<Float>,
    curveColor: Color,
    appSettings: AppSettings,
    width: Float,
    height: Float,
    curveWidth: Float,
    timeClock: HourlyDiagramTime
) {
    if (hourlyWeatherQuantity.airQuality && hourlyData.getOrNull(0)?.airQuality?.time == null) return
    if (!hourlyWeatherQuantity.airQuality && hourlyData.getOrNull(0)?.hourlyWeather?.time == null) return

    val firstPointX = timeToX(
        time = if (hourlyWeatherQuantity.airQuality) hourlyData[0]!!.airQuality!!.time
        else hourlyData[0]!!.hourlyWeather!!.time,
        utcOffset = hourlyData[0]?.utcOffset ?: 0L,
        timeClock = timeClock
    )!!

    val data = quantityData(hourlyChartData = hourlyData, weatherQuantity = hourlyWeatherQuantity).also {
        if (it.isEmpty()) return
    }

    val diagramRect = RectF(
        horizontalPadding,
        verticalPadding,
        width - horizontalPadding,
        height - verticalPadding
    )

    val diagramPath = Path().apply {
        addRoundRect(diagramRect, cornerRadius, cornerRadius, Path.Direction.CW)
    }
    val diagramWidth = width - 2 * horizontalPadding
    val diagramHeight = height - 2 * verticalPadding

    canvas.save()
    canvas.clipPath(diagramPath)

    val graphType = hourlyWeatherQuantity.graphType(appSettings.curvesSmoothness.getOrElse(hourlyWeatherQuantity) { 0f })
    val curvePath =  when (graphType) {
        GraphTypes.STEP -> generateStepGraph(
            data = data,
            firstPointX = firstPointX,
            diagramWidth = diagramWidth,
            diagramHeight = diagramHeight,
            xAxisRange = xAxisRange,
            yAxisRange = yAxisRange,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding
        )
        GraphTypes.LINEAR -> generateLinearGraph(
            data = data,
            firstPointX = firstPointX,
            diagramWidth = diagramWidth,
            diagramHeight = diagramHeight,
            xAxisRange = xAxisRange,
            yAxisRange = yAxisRange,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding
        )
        GraphTypes.CUBIC -> {
            val (controlPoints1, controlPoints2) = quantityControlPoints(
                hourlyChartData = hourlyData,
                hourlyWeatherQuantity = hourlyWeatherQuantity,
                smoothnessFactor = appSettings.curvesSmoothness.getOrElse(hourlyWeatherQuantity){ 0f }
            )
            generateCubicGraph(
                data = data,
                controlPoints1 = controlPoints1,
                controlPoints2 = controlPoints2,
                firstPointX = firstPointX,
                diagramWidth = diagramWidth,
                diagramHeight = diagramHeight,
                xAxisRange = xAxisRange,
                yAxisRange = yAxisRange,
                horizontalPadding = horizontalPadding,
                verticalPadding = verticalPadding
            )
        }
    }?.asAndroidPath()
    if (curvePath != null) {
        val curveBounds = RectF()
        curvePath.computeBounds(curveBounds, true)

        if (appSettings.notificationHourlyCurveShadowVisible) {
            val filledPath = Path()
            filledPath.addPath(curvePath)
            filledPath.lineTo(curveBounds.right, height)
            filledPath.lineTo(curveBounds.left, height)
            filledPath.close()

            val gradient = LinearGradient(
                0f, curveBounds.top,
                0f, curveBounds.bottom,
                intArrayOf(
                    curveColor.copy(alpha = 0.3f).toArgb(),
                    Transparent.toArgb()
                ),
                floatArrayOf(0f, 0.9f),
                Shader.TileMode.CLAMP
            )

            val fillPaint = Paint(ANTI_ALIAS_FLAG).apply {
                shader = gradient
                style = Paint.Style.FILL
            }
            canvas.drawPath(filledPath, fillPaint)
        }

        val curvePaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = curveColor.toArgb()
            strokeWidth = curveWidth
            style = Paint.Style.STROKE
        }
        canvas.drawPath(curvePath, curvePaint)
    }

    canvas.restore()
}

// Helper function to divide a Path into segments (approximation)
fun Path.divideIntoCurves(): List<Path> {
    val segments = mutableListOf<Path>()
    val pathMeasure = PathMeasure(this, false)
    val step = 0.1f // Adjust this value for smoother or coarser segmentation
    val coords = FloatArray(2)

    var distance = 0f
    while (distance < pathMeasure.length) {
        pathMeasure.getPosTan(distance, coords, null)
        val segment = Path()
        if (distance == 0f) {
            segment.moveTo(coords[0], coords[1])
        } else {
            segment.lineTo(coords[0], coords[1])
        }
        segments.add(segment)
        distance += step
    }

    return segments
}