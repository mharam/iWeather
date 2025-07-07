package com.takaapoo.weatherer.ui.notification.custom_views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.weatherCurveXtoY
import com.takaapoo.weatherer.utility.toPx
import com.takaapoo.weatherer.utility.toSpannableWithStyles
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class HourlyChartLabel(
    context: Context,
    hourlyChartData: List<HourlyChartDto> = emptyList(),
    chartQuantities: List<WeatherQuantity>,
    val appSettings: AppSettings,
    curvesColor: List<Int>? = null,
    fontSize: Float,
    timeClock: HourlyDiagramTime = appSettings.notificationHourlyDiagramTime
) : View(context) {

    val quantitiesValue = mutableListOf<Float?>()
    init {
        for (i in chartQuantities.indices){
            val firstPointX = timeToX(
                time = if (chartQuantities[i].airQuality) hourlyChartData[0].airQuality!!.time
                else hourlyChartData[0].hourlyWeather!!.time,
                utcOffset = hourlyChartData[0].utcOffset ?: 0L,
                timeClock = timeClock
            )!!
            val lastPointX = timeToX(
                time = if (chartQuantities[i].airQuality) hourlyChartData.last {
                    it.airQuality?.time != null
                }.airQuality!!.time
                else hourlyChartData.last { it.hourlyWeather?.time != null }.hourlyWeather!!.time,
                utcOffset = hourlyChartData[0].utcOffset ?: 0L,
                timeClock = timeClock
            )!!

            val zone: ZoneId = when (timeClock) {
                HourlyDiagramTime.UTC -> ZoneId.of("UTC")
                HourlyDiagramTime.DEVICE -> ZoneId.systemDefault()
                HourlyDiagramTime.LOCATION -> {
                    ZoneOffset.ofTotalSeconds((hourlyChartData.getOrNull(0)?.utcOffset?.toInt() ?: 0) * 60)
                }
            }
            val now = LocalDateTime.now(zone)

            val indicatorX = Duration.between(now.truncatedTo(ChronoUnit.HOURS), now).toMinutes() / 60f
            val curveValueAtIndicator = weatherCurveXtoY(
                quantity = chartQuantities[i],
                appSettings = appSettings,
                hourlyWeather = hourlyChartData.map { it.hourlyWeather },
                hourlyAirQuality = hourlyChartData.map { it.airQuality },
                firstPointX = firstPointX,
                lastPointX = lastPointX,
                targetX = indicatorX
            )

            chartQuantities[i].let {
                quantitiesValue.add(
                    when (it) {
                        WeatherQuantity.HUMIDITY, WeatherQuantity.PRECIPITATIONPROBABILITY,
                        WeatherQuantity.CLOUDCOVER -> curveValueAtIndicator?.coerceIn(0f, 100f)

                        WeatherQuantity.WINDSPEED, WeatherQuantity.UVINDEX, WeatherQuantity.DIRECTRADIATION,
                        WeatherQuantity.DIRECTNORMALIRRADIANCE, WeatherQuantity.Ozone, WeatherQuantity.SO2,
                        WeatherQuantity.CO, WeatherQuantity.NO2, WeatherQuantity.PM10, WeatherQuantity.PM2_5 ->
                            curveValueAtIndicator?.coerceAtLeast(0f)

                        else -> curveValueAtIndicator
                    }
                )
            }
        }
    }

    val textPaints = (curvesColor ?: appSettings.notificationCurvesColor).map {
        TextPaint().apply {
            textSize = fontSize
            typeface = ResourcesCompat.getFont(context, R.font.cmu_serif)
            color = it
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
    }
    lateinit var quantitiesNameLayout: List<StaticLayout>

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        quantitiesNameLayout = appSettings.widgetHourlyDiagramQuantities.mapIndexed { index, quantity ->
            val spannable = buildAnnotatedString {
                append(quantity.nameId?.let { AnnotatedString(context.getString(it)) } ?: quantity.title!!)
                when {
                    quantitiesValue[index] == null -> append(" :  ?")
                    abs(quantitiesValue[index]!!) < 0.001f -> append(" :  0")
                    else -> append(" :  ${"%.${quantity.floatingPointDigits(appSettings)}f"
                        .format(quantitiesValue[index])}")
                }
                if (quantity.unit(appSettings).isNotEmpty()) {
                    append(quantity.unit(appSettings))
                }
            }.toSpannableWithStyles(context)
            StaticLayout.Builder
                .obtain(spannable, 0, spannable.length, textPaints[index], measuredWidth)
                .build()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            quantitiesNameLayout.getOrNull(0)?.draw(this)
            quantitiesNameLayout.getOrNull(0)?.let {
                translate(0f, it.height.toFloat() + 4.dp.toPx(Density(context)) )
                quantitiesNameLayout.getOrNull(1)?.draw(this)
            }
        }
    }

}