package com.takaapoo.weatherer.ui.notification.custom_views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.weatherCurveXtoY
import com.takaapoo.weatherer.utility.getColorScheme
import com.takaapoo.weatherer.utility.toPx
import com.takaapoo.weatherer.utility.toSpannableWithStyles
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class CurrentValue(
    context: Context,
    val hourlyChartData: List<HourlyChartDto> = emptyList(),
    val chartQuantity: HourlyWeatherQuantity,
    val appSettings: AppSettings,
    fontSize: Float,
): View(context) {
    val myDensity = Density(context)
    private val colorScheme = getColorScheme(context, appSettings.dynamicColor)
    val pointRadius = 4.dp.toPx(myDensity)

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorScheme.secondaryContainer.toArgb()
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = colorScheme.primaryContainer.toArgb()
        strokeWidth = 2.dp.toPx(myDensity)
    }
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorScheme.primaryContainer.toArgb()
    }
    private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        textSize = fontSize
        typeface = ResourcesCompat.getFont(context, R.font.cmu_serif)
        color = colorScheme.onSecondaryContainer.toArgb()
    }


    private val quantityValue: Float?
    init {
//        val data = quantityData(hourlyChartData = hourlyChartData, weatherQuantity = chartQuantity)
        val firstPointX = timeToX(
            time = if (chartQuantity.airQuality) hourlyChartData[0].airQuality!!.time
            else hourlyChartData[0].hourlyWeather!!.time,
            utcOffset = hourlyChartData[0].utcOffset ?: 0L,
            timeClock = appSettings.notificationHourlyDiagramTime
        )!!
        val lastPointX = timeToX(
            time = if (chartQuantity.airQuality) hourlyChartData.last {
                it.airQuality?.time != null
            }.airQuality!!.time
            else hourlyChartData.last { it.hourlyWeather?.time != null }.hourlyWeather!!.time,
            utcOffset = hourlyChartData[0].utcOffset ?: 0L,
            timeClock = appSettings.notificationHourlyDiagramTime
        )!!

//        val (controlPoints1, controlPoints2) = quantityControlPoints(
//            hourlyChartData = hourlyChartData,
//            weatherQuantity = chartQuantity
//        )
        val now = LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(hourlyChartData.getOrNull(0)?.utcOffset ?: 0L)

        val indicatorX = Duration.between(now.truncatedTo(ChronoUnit.HOURS), now).toMinutes() / 60f
        val curveValueAtIndicator = weatherCurveXtoY(
            quantity = chartQuantity,
            appSettings = appSettings,
            hourlyWeather = hourlyChartData.map { it.hourlyWeather },
            hourlyAirQuality = hourlyChartData.map { it.airQuality },
            firstPointX = firstPointX,
            lastPointX = lastPointX,
            targetX = indicatorX
        )
            /*when (chartQuantity.graphType()){
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
        quantityValue = when (chartQuantity) {
            HourlyWeatherQuantity.HUMIDITY, HourlyWeatherQuantity.PRECIPITATIONPROBABILITY,
            HourlyWeatherQuantity.CLOUDCOVER -> curveValueAtIndicator?.coerceIn(0f, 100f)

            HourlyWeatherQuantity.WINDSPEED, HourlyWeatherQuantity.UVINDEX, HourlyWeatherQuantity.DIRECTRADIATION,
            HourlyWeatherQuantity.DIRECTNORMALIRRADIANCE, HourlyWeatherQuantity.Ozone, HourlyWeatherQuantity.SO2,
            HourlyWeatherQuantity.CO, HourlyWeatherQuantity.NO2, HourlyWeatherQuantity.PM10, HourlyWeatherQuantity.PM2_5 ->
                curveValueAtIndicator?.coerceAtLeast(0f)

            else -> curveValueAtIndicator
        }
    }
    val spannable = buildAnnotatedString {
        when {
            quantityValue == null -> append("?")
            abs(quantityValue) < 0.001f -> append("0")
            else -> append("%.${chartQuantity.floatingPointDigits(appSettings)}f".format(quantityValue))
        }
        if (chartQuantity.unit(appSettings).isNotEmpty()) {
            append(chartQuantity.unit(appSettings))
        }
    }.toSpannableWithStyles(context)
    private lateinit var quantityValueText: StaticLayout
    val borderMargin = 1.dp.toPx(myDensity)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        quantityValueText = StaticLayout.Builder
            .obtain(spannable, 0, spannable.length, textPaint, measuredWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .build()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            drawCircle(pointRadius, height/2f, pointRadius, pointPaint)
            drawRoundRect(pointRadius, borderMargin, width - borderMargin, height - borderMargin,
                height/2f, height/2f, fillPaint)
            drawRoundRect(pointRadius + borderMargin, borderMargin, width - borderMargin, height - borderMargin,
                height/2f, height/2f, borderPaint)
            translate(0f, (height - quantityValueText.height)/2f)
            quantityValueText.draw(this)
        }
    }


}