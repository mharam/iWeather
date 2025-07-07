package com.takaapoo.weatherer.ui.notification.custom_views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.weatherCurveXtoY
import com.takaapoo.weatherer.utility.toPx
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class StatusBarIcon(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val hourlyChartData: List<HourlyChartDto> = emptyList(),
    quantity: WeatherQuantity,
    appSettings: AppSettings
): AppCompatTextView(context, attrs, defStyleAttr) {

    val quantityValue: Float?

    init {
        val now = LocalDateTime.now(ZoneId.of("UTC"))
            .plusMinutes(hourlyChartData.getOrNull(0)?.utcOffset ?: 0L)
        val firstPointX = timeToX(
            time = if (quantity.airQuality) hourlyChartData[0].airQuality!!.time
            else hourlyChartData[0].hourlyWeather!!.time,
            utcOffset = hourlyChartData[0].utcOffset ?: 0L,
            timeClock = appSettings.notificationHourlyDiagramTime
        )!!
        val lastPointX = timeToX(
            time = if (quantity.airQuality) hourlyChartData.last {
                it.airQuality?.time != null
            }.airQuality!!.time
            else hourlyChartData.last{ it.hourlyWeather?.time != null }.hourlyWeather!!.time,
            utcOffset = hourlyChartData[0].utcOffset ?: 0L,
            timeClock = appSettings.notificationHourlyDiagramTime
        )!!

//        val graphType = quantity.graphType()
//        val (controlPoints1, controlPoints2) = quantityControlPoints(
//            hourlyChartData = hourlyChartData,
//            weatherQuantity = quantity
//        )
//        val data = quantityData(
//            hourlyChartData = hourlyChartData,
//            weatherQuantity = quantity
//        )

        val indicatorX = Duration.between(now.truncatedTo(ChronoUnit.HOURS), now).toMinutes() / 60f
        val curveValueAtIndicator = weatherCurveXtoY(
            quantity = quantity,
            appSettings = appSettings,
            hourlyWeather = hourlyChartData.map { it.hourlyWeather },
            hourlyAirQuality = hourlyChartData.map { it.airQuality },
            firstPointX = firstPointX,
            lastPointX = lastPointX,
            targetX = indicatorX
        )
            /*when (graphType){
            GraphTypes.CUBIC -> cubicCurveXtoY(
                data = data,
                controlPoints1 = controlPoints1,
                controlPoints2 = controlPoints2,
                firstPointX = firstPointX,
                lastPointX = lastPointX,
                targetX = indicatorX
            )
            GraphTypes.LINEAR -> linearCurveXtoY(
                data = data,
                firstPointX = firstPointX,
                lastPointX = lastPointX,
                targetX = indicatorX
            )
            GraphTypes.STEP -> stepCurveXtoY(
                data = data,
                firstPointX = firstPointX,
                lastPointX = lastPointX,
                targetX = indicatorX
            )
        }*/
        quantityValue = when (quantity) {
            WeatherQuantity.HUMIDITY, WeatherQuantity.PRECIPITATIONPROBABILITY,
            WeatherQuantity.CLOUDCOVER -> curveValueAtIndicator?.coerceIn(0f, 100f)

            WeatherQuantity.WINDSPEED, WeatherQuantity.UVINDEX, WeatherQuantity.DIRECTRADIATION,
            WeatherQuantity.DIRECTNORMALIRRADIANCE, WeatherQuantity.Ozone, WeatherQuantity.SO2,
            WeatherQuantity.CO, WeatherQuantity.NO2, WeatherQuantity.PM10, WeatherQuantity.PM2_5 ->
                curveValueAtIndicator?.coerceAtLeast(0f)

            else -> curveValueAtIndicator
        }
    }

    val myDensity = Density(context)
    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = ResourcesCompat.getFont(context, R.font.cmu_serif)
        textSize = 40.dp.toPx(myDensity)
        color = Color.WHITE
        textAlign = Align.CENTER
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 2f
        letterSpacing = -0.05f
    }
    val quantityText = (quantityValue?.roundToInt()?.toString() ?: "?") /*+ "°"*/
    val textBound = Rect()

    override fun onDraw(canvas: Canvas) {
        textPaint.getTextBounds(quantityText, 0, quantityText.length, textBound)
        canvas.apply {
            drawColor(Color.TRANSPARENT)
            drawText(
                quantityText,
                width / 2f,
                (height + textBound.height() + 4) / 2f,
                textPaint
            )
        }
    }

}