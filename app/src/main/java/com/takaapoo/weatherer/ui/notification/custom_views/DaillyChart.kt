package com.takaapoo.weatherer.ui.notification.custom_views

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.ui.notification.dailyDiagramCurveOnCanvas
import com.takaapoo.weatherer.ui.notification.dailyDiagramFrameOnCanvas
import com.takaapoo.weatherer.ui.notification.dailyDiagramLegendsOnCanvas
import com.takaapoo.weatherer.ui.notification.dailyDiagramWeatherIconOnCanvas
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.dailyQuantityData
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.dailyTimeToX
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.mainGridWidthDp
import com.takaapoo.weatherer.ui.theme.DiagramGrid
import com.takaapoo.weatherer.utility.toPx
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow


class DailyChart(
    context: Context,
    val dailyChartData: List<LocalDailyWeather> = emptyList(),
    val diagramHorPadding: Float = 16f,
    val diagramVertPadding: Float = 24f,
    val appSettings: AppSettings,
    val frameHeight: Float = Float.MAX_VALUE,
    val weatherIconSize: Dp = 24.dp
) : View(context) {

    val myDensity = Density(context)
    val cornerRadius = context.resources.getDimension(R.dimen.diagram_corner_radius)
    val legendTextColor = Color(
        Resources.getSystem().getIdentifier(
            "notification_primary_text_color_current",
            "color",
            "android"
        ).let { resourceId: Int ->
            context.getColor(resourceId)
        }
    ).toArgb()

    val xAxisStart = -0.5f
    val xAxisEnd = 4.5f
    var yAxisRange: ClosedFloatingPointRange<Float>? = getYAxisRange(
        appSettings.notificationDailyQuantity,
        dailyChartData
    )

    val data = dailyQuantityData(
        dailyData = dailyChartData,
        weatherQuantity = appSettings.notificationDailyQuantity
    )
    val dataMax = data.maxByOrNull { it?.endInclusive ?: -Float.MAX_VALUE }?.endInclusive ?: 1f
    val dataMin = data.minByOrNull { it?.start ?: Float.MAX_VALUE }?.start ?: 0f
    val firstPointX = dailyTimeToX(date = dailyChartData.getOrNull(0)?.time)?.toFloat()


    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)
        canvas.apply {
            dailyDiagramFrameOnCanvas(
                canvas = this,
                cornerRadius = cornerRadius,
                horizontalPadding = diagramHorPadding,
                verticalPadding = diagramVertPadding,
                mainBarColor = DiagramGrid.toArgb(),
                minorBarColor = DiagramGrid.toArgb(),
                minorBarVisible = appSettings.notificationDailyChartGrid == ChartGrids.All,
                majorBarVisible = appSettings.notificationDailyChartGrid != ChartGrids.NON,
                mainBarWidthPx = mainGridWidthDp.toPx(myDensity),
                minorBarWidthPx = mainGridWidthDp.toPx(myDensity),
                frameHeight = frameHeight
            )
            if (dailyChartData.isNotEmpty() && firstPointX != null && yAxisRange != null) {
                dailyDiagramCurveOnCanvas(
                    context = context,
                    canvas = this,
                    data = data,
                    dataMax = dataMax,
                    dataMin = dataMin,
                    firstPointX = firstPointX,
                    weatherQuantity = appSettings.notificationDailyQuantity,
                    cornerRadius = cornerRadius,
                    horizontalPadding = diagramHorPadding,
                    verticalPadding = diagramVertPadding,
                    xAxisRange = xAxisStart .. xAxisEnd,
                    yAxisRange = yAxisRange!!,
                    curveValueColor = legendTextColor,
                    fontSize = 14.sp.toPx(myDensity),
                    font = ResourcesCompat.getFont(context, R.font.cmu_serif)!!,
                    frameHeight = frameHeight
                )
            }
            if (appSettings.notificationDailyWeatherIconsVisible && dailyChartData.isNotEmpty()) {
                dailyDiagramWeatherIconOnCanvas(
                    canvas = canvas,
                    context = context,
                    weatherCodes = dailyChartData.map { it.weatherCode },
                    cornerRadius = cornerRadius,
                    iconSizePx = weatherIconSize.toPx(myDensity).toInt(),
                    horizontalPadding = diagramHorPadding,
                    verticalPadding = diagramVertPadding,
                )
            }

            val chartUnit = appSettings.notificationDailyQuantity.unit(appSettings).trim('°', ' ')
            val chartTitle = context.getString(appSettings.notificationDailyQuantity.nameId) +
                    if (chartUnit.isNotEmpty()) " [${chartUnit}]" else ""
            dailyDiagramLegendsOnCanvas(
                canvas = canvas,
                cornerRadius = cornerRadius,
                yAxisRange = yAxisRange,
                horizontalPadding = diagramHorPadding,
                verticalPadding = diagramVertPadding,
                onAppSurfaceColor = legendTextColor,
                textColor = legendTextColor,
                dataMax = dataMax,
                dataMin = dataMin,
                density = myDensity,
                timeFont = ResourcesCompat.getFont(context, R.font.cmu_typewriter_bold)!!,
                yAxesFont = ResourcesCompat.getFont(context, R.font.cmu_serif)!!,
                chartQuantity = appSettings.notificationDailyQuantity,
                chartTitle = chartTitle,
                frameHeight = min(frameHeight, height - 2 * diagramVertPadding),
            )
        }
    }





    fun getYAxisRange(
        quantity: DailyWeatherQuantity,
        dailyChartData: List<LocalDailyWeather>
    ): ClosedFloatingPointRange<Float>? {
        var quantityMin: Float?
        var quantityMax: Float?
        when (quantity ){
            DailyWeatherQuantity.SUNRISESET -> {
                quantityMin = 0f
                quantityMax = 24f
            }
            else -> {
                val dataMin = dailyQuantityData(
                    dailyData = dailyChartData,
                    weatherQuantity = quantity
                ).map { it?.start }
                quantityMin = dataMin.minOfOrNull { it ?: Float.MAX_VALUE }
                val dataMax = dailyQuantityData(
                    dailyData = dailyChartData,
                    weatherQuantity = quantity
                ).map { it?.endInclusive }
                quantityMax = dataMax.maxOfOrNull { it ?: -Float.MAX_VALUE }
            }
        }
        if (quantityMin == null || quantityMax == null) return null
        var step = (quantityMax - quantityMin) / 3
        return when {
            step == 0f -> {
                 floor(quantityMin) - 0.5f .. floor(quantityMin) + 3.5f
            }
            else -> {
                step = ((quantityMax - quantityMin) / 3).customCeil()
                val start = quantityMin.customFloor(step)
//                Log.i("range1", "step = $step , quantityMin = $quantityMin , quantityMax = $quantityMax , start = $start")
                start - 0.5f * step .. start + 3.5f * step
            }
        }
    }

    private fun Float.customCeil(): Float {
        val order = floor(log10(this))
        return ceil(this / (10f.pow(order))) * (10f.pow(order))
    }

    private fun Float.customFloor(step: Float): Float {
        if (step <= 0 && this <= 0) return floor(this)
        val order = floor(log10(abs(if (step > 0) step else this)))
        return floor((this) / (10f.pow(order))) * (10f.pow(order))
    }


}
