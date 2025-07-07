package com.takaapoo.weatherer.ui.notification.custom_views

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.text.TextPaint
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.weatherCurveXtoY
import com.takaapoo.weatherer.utility.spToFloat
import com.takaapoo.weatherer.utility.toPx
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.roundToInt


class SmallNotification(
    context: Context,
    hourlyChartData: ImmutableList<HourlyChartDto> = persistentListOf(),
    val weatherType: WeatherType?,
    val appSettings: AppSettings,
) : View(context) {

    val myDensity = Density(context)
    val weatherIconSize = context.resources.getDimension(R.dimen.notification_weather_type_size).toInt()
    val colorInt = Resources.getSystem().getIdentifier(
        "notification_primary_text_color_current",
        "color",
        "android"
    ).let { resourceId: Int ->
        context.getColor(resourceId)
    }

//    val data = quantityData(hourlyChartData = hourlyChartData, weatherQuantity = WeatherQuantity.TEMPERATURE)
    val firstPointX = timeToX(
        time = hourlyChartData[0].hourlyWeather!!.time,
        utcOffset = hourlyChartData[0].utcOffset ?: 0L,
        timeClock = appSettings.notificationDateTime
    )!!
    val lastPointX = timeToX(
        time = hourlyChartData.last{ it.hourlyWeather?.time != null }.hourlyWeather!!.time,
        utcOffset = hourlyChartData[0].utcOffset ?: 0L,
        timeClock = appSettings.notificationDateTime
    )!!
    val zone: ZoneId = when (appSettings.notificationDateTime) {
        HourlyDiagramTime.UTC -> ZoneId.of("UTC")
        HourlyDiagramTime.DEVICE -> ZoneId.systemDefault()
        HourlyDiagramTime.LOCATION -> {
            ZoneOffset.ofTotalSeconds((hourlyChartData.getOrNull(0)?.utcOffset?.toInt() ?: 0) * 60)
        }
    }

    val now: LocalDateTime = LocalDateTime.now(zone)
    val currentHourlyChartData = hourlyChartData.find {
        it.hourlyWeather?.time == now.truncatedTo(ChronoUnit.HOURS).toString()
    }

    val indicatorX = Duration.between(now.truncatedTo(ChronoUnit.HOURS), now).toMinutes() / 60f
    val curveValueAtIndicator = weatherCurveXtoY(
        quantity = WeatherQuantity.TEMPERATURE,
        appSettings = appSettings,
        hourlyWeather = hourlyChartData.map { it.hourlyWeather },
        hourlyAirQuality = hourlyChartData.map { it.airQuality },
        firstPointX = firstPointX,
        lastPointX = lastPointX,
        targetX = indicatorX
    )?.roundToInt()

    val currentTextPaint = TextPaint().apply {
        textSize = spToFloat(context, 20f)
        typeface = ResourcesCompat.getFont(context, R.font.cmu_serif)
        color = colorInt
        style = Paint.Style.FILL
        isAntiAlias = true
        textAlign = Paint.Align.RIGHT
    }
    val currentTempText = "${curveValueAtIndicator ?: "?"}°"
    val minTempText = "${currentHourlyChartData?.minTemperature?.roundToInt() ?: "?"}°"
    val maxTempText = "${currentHourlyChartData?.maxTemperature?.roundToInt() ?: "?"}°"

    val currentTempBound = Rect()
    val maxTempBound = Rect()
    val minTempBound = Rect()

    val arrowSize = 20.dp.toPx(myDensity).toInt()
    val arrowBitmap = AppCompatResources.getDrawable(context, R.drawable.south_24)!!.toBitmap(
        arrowSize, arrowSize, null
    )
    val arrowPaint = Paint()
    val upArrowColorFilter = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
    val downArrowColorFilter = PorterDuffColorFilter(
        context.getColor(R.color.notif_min_temperature),
        PorterDuff.Mode.SRC_IN
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            currentTextPaint.getTextBounds(currentTempText, 0, currentTempText.length - 1, currentTempBound)
            drawText(
                currentTempText,
                (width).toFloat(),
                (height + currentTempBound.height()) / 2f,
                currentTextPaint
            )
            save()
            scale(1f, -1f)
            val arrowLeft = width - currentTextPaint.measureText(currentTempText) -
                    /*8.dp.toPx(myDensity) -*/ arrowSize
            drawBitmap(
                arrowBitmap,
                arrowLeft,
                - arrowSize.toFloat(),
                arrowPaint.apply { colorFilter = upArrowColorFilter }
            )
            restore()
            drawBitmap(
                arrowBitmap,
                arrowLeft,
                height.toFloat() - arrowSize,
                arrowPaint.apply { colorFilter = downArrowColorFilter }
            )

            val maxTemperaturePaint = currentTextPaint.apply {
                textSize = spToFloat(context, 14f)
                color = Color.RED
                style = Paint.Style.FILL_AND_STROKE
                strokeWidth = 1f
            }
            maxTemperaturePaint.getTextBounds(maxTempText, 0, maxTempText.length, maxTempBound)
            maxTemperaturePaint.getTextBounds(minTempText, 0, minTempText.length, minTempBound)
            drawText(
                maxTempText,
                arrowLeft,
                (arrowSize + maxTempBound.height()) / 2f,
                maxTemperaturePaint
            )
            drawText(
                minTempText,
                arrowLeft,
                height - (arrowSize - maxTempBound.height()) / 2f,
                maxTemperaturePaint.apply { color = context.getColor(R.color.notif_min_temperature) }
            )

            if (appSettings.notificationWeatherIconVisible) {
                weatherType?.let {
                    WeatherType.drawWeatherIconOnCanvas(
                        canvas = this,
                        weatherType = it,
                        context = context,
                        topLeft = IntOffset(
                            (arrowLeft - max(
                                maxTempBound.width(),
                                minTempBound.width()
                            ) - 4.dp.toPx(
                                myDensity
                            )).toInt() - weatherIconSize,
                            0
                        ),
                        iconSize = IntSize(weatherIconSize, weatherIconSize),
                        paint = Paint()
                    )
                }
            }
        }
    }
}