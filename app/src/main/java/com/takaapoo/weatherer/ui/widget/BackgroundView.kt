package com.takaapoo.weatherer.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.view.View
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.WeatherType.Companion.calculateMoonType
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.moonPhase
import com.takaapoo.weatherer.ui.notification.custom_views.HourlyChart
import com.takaapoo.weatherer.ui.notification.custom_views.HourlyChartLabel
import com.takaapoo.weatherer.ui.notification.custom_views.timeToX
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.weatherCurveXtoY
import com.takaapoo.weatherer.utility.getColorScheme
import com.takaapoo.weatherer.utility.readMoonPhaseBinFromAssets
import com.takaapoo.weatherer.utility.readMoonPhaseFromAssets
import com.takaapoo.weatherer.utility.spToFloat
import com.takaapoo.weatherer.utility.toPx
import kotlinx.collections.immutable.toImmutableList
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.roundToInt
import kotlin.math.sin


class BackgroundView(
    context: Context,
    val hourlyChartData: List<HourlyChartDto> = emptyList(),
    val dailyChartData: List<LocalDailyWeather> = emptyList(),
    val appSettings: AppSettings,
    val tab: Int = 1
) : View(context) {

    init {
//        moonPhaseMap = Json.parseToJsonElement(readJSONFromAssets(context, "moon_phase.json"))
//            .jsonObject.toMap()
        moonPhase = readMoonPhaseBinFromAssets(context, "moon_phase.bin")
    }

    val myDensity = Density(context)
    val weatherIconSize = 56.dp.toPx(myDensity).toInt()

    private val colorScheme = getColorScheme(context, appSettings.dynamicColor)
    private val pi = PI.toFloat()

    val firstPointX = timeToX(
        time = hourlyChartData[0].hourlyWeather!!.time,
        utcOffset = hourlyChartData[0].utcOffset ?: 0L,
        timeClock = appSettings.widgetDateTime
    )!!
    val lastPointX = timeToX(
        time = hourlyChartData.last { it.hourlyWeather?.time != null }.hourlyWeather!!.time,
        utcOffset = hourlyChartData[0].utcOffset ?: 0L,
        timeClock = appSettings.widgetDateTime
    )!!
    val zone: ZoneId = when (appSettings.widgetDateTime) {
        HourlyDiagramTime.UTC -> ZoneId.of("UTC")
        HourlyDiagramTime.DEVICE -> ZoneId.systemDefault()
        HourlyDiagramTime.LOCATION -> {
            ZoneOffset.ofTotalSeconds((hourlyChartData.getOrNull(0)?.utcOffset?.toInt() ?: 0) * 60)
        }
    }

    val now: LocalDateTime = LocalDateTime.now(zone)
    val dayOfWeek: String = now.dayOfWeek.getDisplayName(
        TextStyle.FULL,
        Locale.getDefault()
    )
    val clockText: String = now.format(DateTimeFormatter.ofPattern("HH\u202F:\u202Fmm"))
    val date = "$dayOfWeek, ${clockText}"
    val datePaint = TextPaint().apply {
        textSize = spToFloat(context, 14f)
        typeface = ResourcesCompat.getFont(context, R.font.cmu_typewriter_bold)
        color = colorScheme.onSurface.toArgb()
        isAntiAlias = true
    }


    val currentHourlyChartData = hourlyChartData.find {
        it.hourlyWeather?.time == now.truncatedTo(ChronoUnit.HOURS).toString()
    }
    val currentHourlyWeather = hourlyChartData.find {
        it.hourlyWeather?.time == LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.HOURS).toString()
    }?.hourlyWeather
    val weatherType = WeatherType.fromWMO(
        code = currentHourlyWeather?.weatherCode,
        isDay = currentHourlyWeather?.isDay != 0,
        moonType = calculateMoonType(now.toLocalDate())
    )

    val indicatorX = Duration.between(now.truncatedTo(ChronoUnit.HOURS), now).toMinutes() / 60f
    val curveValueAtIndicator = weatherCurveXtoY(
        quantity = HourlyWeatherQuantity.TEMPERATURE,
        appSettings = appSettings,
        hourlyWeather = hourlyChartData.map { it.hourlyWeather },
        hourlyAirQuality = hourlyChartData.map { it.airQuality },
        firstPointX = firstPointX,
        lastPointX = lastPointX,
        targetX = indicatorX
    )?.roundToInt()

    val locationName = hourlyChartData.getOrNull(0)?.locationName ?: ""
    val locationNamePaint = TextPaint().apply {
        textSize = spToFloat(context, 16f)
        typeface = ResourcesCompat.getFont(context, R.font.cmu_demi_condensed)
        color = colorScheme.primary.toArgb()
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
        strokeWidth = 1f
    }

    val currentTextPaint = TextPaint().apply {
        textSize = spToFloat(context, 46f)
        typeface = ResourcesCompat.getFont(context, R.font.cmu_serif)
        color = colorScheme.onSurface.toArgb()
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 3f
        isAntiAlias = true
        textAlign = Paint.Align.RIGHT
    }
    val currentTempText = "${curveValueAtIndicator ?: "?"}°"
    val minTempText = "${currentHourlyChartData?.minTemperature?.roundToInt() ?: "?"}°"
    val maxTempText = "${currentHourlyChartData?.maxTemperature?.roundToInt() ?: "?"}°"
    val maxTemperaturePaint = TextPaint().apply {
        textSize = spToFloat(context, 16f)
        typeface = ResourcesCompat.getFont(context, R.font.cmu_serif)
        color = Color.RED
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 1f
        isAntiAlias = true
        textAlign = Paint.Align.RIGHT
    }

    val locationNameBound = Rect()
    val currentTempBound = Rect()
    val dateBound = Rect()
    val maxTempBound = Rect()
    val minTempBound = Rect()

//    val arrowSize = 20.dp.toPx(myDensity).toInt()
//    val arrowBitmap = AppCompatResources.getDrawable(context, R.drawable.south_24)!!.toBitmap(
//        arrowSize, arrowSize, null
//    )
//    val arrowPaint = Paint()
//    val upArrowColorFilter = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
//    val downArrowColorFilter = PorterDuffColorFilter(
//        context.getColor(R.color.notif_min_temperature),
//        PorterDuff.Mode.SRC_IN
//    )
    val weatherCircleRadius = context.resources.getDimension(R.dimen.widget1_circle_radius)
    val cornerRadius = 24.dp.toPx(myDensity)
    val smallCornerRadius = 4.dp.toPx(myDensity)
    val l = context.resources.getDimension(R.dimen.widget1_top_gap)
    val theta = acos((weatherCircleRadius - l) / weatherCircleRadius)

    val backgroundPaint1 = Paint().apply {
        color = colorScheme.primaryContainer.toArgb()
    }
    val backgroundPaint2 = Paint().apply {
        color = colorScheme.surface.toArgb()
    }
    val circleFillPaint = Paint().apply {
//        color = colorScheme.surface.copy(alpha = 0.5f).toArgb()
        color = colorScheme.surfaceTint.toArgb()
        style = Paint.Style.FILL
    }
    val circleStrokePaint = Paint().apply {
        color = colorScheme.surface.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 6.dp.toPx(myDensity)
        strokeCap = Paint.Cap.ROUND
    }

    val padding = 16.dp.toPx(myDensity)
    val minorPadding = 8.dp.toPx(myDensity)
    val gap = 2.dp.toPx(myDensity)
    var topPartHeight: Float = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        locationNamePaint.getTextBounds(locationName, 0, locationName.length, locationNameBound)
        datePaint.getTextBounds(date, 0, date.length, dateBound)
        currentTextPaint.getTextBounds(currentTempText, 0, currentTempText.length, currentTempBound)
        topPartHeight = 2 * padding + locationNameBound.height() + dateBound.height() + minorPadding

        canvas.apply {
            val backgroundPath1 = Path().apply {
                moveTo(0f, l+cornerRadius)
                arcTo(
                    RectF(0f, l, 2*cornerRadius, l + 2*cornerRadius),
                    180f, 90f, false
                )
                lineTo(width/2f - weatherCircleRadius * sin(theta), l)
                arcTo(
                    RectF(width/2f - weatherCircleRadius, 0f, width/2f + weatherCircleRadius, 2*weatherCircleRadius),
                    270f - theta*180/pi, 2*theta*180/pi - 360f, false
                )
                lineTo(width - cornerRadius, l)
                arcTo(
                    RectF(width - 2*cornerRadius, l, width.toFloat(), l + 2*cornerRadius),
                    270f, 90f, false
                )
                lineTo(width.toFloat(), l + topPartHeight - smallCornerRadius)
                arcTo(
                    RectF(width - 2*smallCornerRadius, l + topPartHeight - 2*smallCornerRadius,
                        width.toFloat(), l + topPartHeight),
                    0f, 90f, false
                )
                lineTo(smallCornerRadius, l + topPartHeight)
                arcTo(
                    RectF(0f, l + topPartHeight - 2*smallCornerRadius, 2*smallCornerRadius,
                        l + topPartHeight),
                    90f, 90f, false
                )
                close()
            }
            val y = l+topPartHeight+gap
            val backgroundPath2 = Path().apply {
                moveTo(0f, y)
                arcTo(
                    RectF(0f, y, 2*smallCornerRadius, y + 2*smallCornerRadius),
                    180f, 90f, false
                )
                lineTo(width - smallCornerRadius, y)
                arcTo(
                    RectF(width - 2*smallCornerRadius, y, width.toFloat(), y + 2*smallCornerRadius),
                    270f, 90f, false
                )
                lineTo(width.toFloat(), height - cornerRadius)
                arcTo(
                    RectF(width - 2*cornerRadius, height - 2*cornerRadius, width.toFloat(), height.toFloat()),
                    0f, 90f, false
                )
                lineTo(cornerRadius, height.toFloat())
                arcTo(
                    RectF(0f, height - 2*cornerRadius, 2*cornerRadius, height.toFloat()),
                    90f, 90f, false
                )
                close()
            }

            drawPath(backgroundPath1, backgroundPaint1)
            drawPath(backgroundPath2, backgroundPaint2)
            drawCircle(width/2f, weatherCircleRadius, weatherCircleRadius, circleFillPaint)
//            drawCircle(width/2f, weatherCircleRadius, weatherCircleRadius - 1.dp.toPx(myDensity),
//                circleStrokePaint)
//            drawArc(
//                RectF(width/2f - weatherCircleRadius, 0f, width/2f + weatherCircleRadius, 2*weatherCircleRadius),
//                225f, -270f, false, circleStrokePaint
//            )
            drawCircle(width/2f, weatherCircleRadius, weatherCircleRadius,
                circleStrokePaint.apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                    strokeWidth = 2.dp.toPx(myDensity)
                }
            )
//            drawArc(
//                RectF(width/2f - weatherCircleRadius, 0f, width/2f + weatherCircleRadius, 2*weatherCircleRadius),
//                225f, -270f, false, circleStrokePaint.apply {
//                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
//                    strokeWidth = 2.dp.toPx(myDensity)
//                }
//            )

            drawText(
                locationName,
                padding,
                l + padding + locationNameBound.height(),
                locationNamePaint
            )

            drawText(
                date,
                padding,
                l + padding + locationNameBound.height() + dateBound.height() + minorPadding,
                datePaint
            )

            drawText(
                currentTempText,
                width - padding,
                l + padding + currentTempBound.height(),
                currentTextPaint
            )


            maxTemperaturePaint.getTextBounds(maxTempText, 0, maxTempText.length, maxTempBound)
            maxTemperaturePaint.getTextBounds(minTempText, 0, minTempText.length, minTempBound)
            val textRight = width - padding - currentTextPaint.measureText(currentTempText) - minorPadding
            drawText(
                maxTempText,
                textRight,
                l + padding + maxTempBound.height(),
                maxTemperaturePaint
            )
            drawText(
                minTempText,
                textRight,
                l + padding + locationNameBound.height() + dateBound.height() + minorPadding,
                maxTemperaturePaint.apply { color = context.getColor(R.color.notif_min_temperature) }
            )

            WeatherType.drawWeatherIconOnCanvas(
                canvas = this,
                weatherType = weatherType,
                context = context,
                topLeft = IntOffset((width - weatherIconSize)/2, 8.dp.toPx(myDensity).toInt()),
                iconSize = IntSize(weatherIconSize, weatherIconSize),
                paint = Paint()
            )


            when (tab) {
                2 -> {
                    val labelHeight =
                        context.resources.getDimension(R.dimen.notification_hour_diagram_label_height)
                    val labelTop = y + 48.dp.toPx(myDensity) + padding
                    val hourlyChartLabel = HourlyChartLabel(
                        context = context,
                        hourlyChartData = hourlyChartData,
                        chartQuantities = appSettings.widgetHourlyDiagramQuantities,
                        appSettings = appSettings,
                        fontSize = spToFloat(context, 14f),
                        timeClock = appSettings.widgetDateTime
                    ).apply {
                        val widthSpec = MeasureSpec.makeMeasureSpec(
                            (width - padding * 2).toInt(),
                            MeasureSpec.EXACTLY
                        )
                        val heightSpec = MeasureSpec.makeMeasureSpec(
                            labelHeight.toInt(),
                            MeasureSpec.EXACTLY
                        )
                        measure(widthSpec, heightSpec)
                        layout(0, 0, measuredWidth, measuredHeight)
                    }

                    val hourlyChartTop = labelTop + labelHeight
                    val hourlyChartHeight = height - hourlyChartTop - 3 * padding
                    val hourlyChart = HourlyChart(
                        context = context,
                        hourlyChartData = hourlyChartData.toImmutableList(),
                        diagramHorPadding = 2 * padding,
                        diagramVertPadding = padding,
                        chartQuantities = appSettings.widgetHourlyDiagramQuantities,
                        appSettings = appSettings,
                        frameHeight = hourlyChartHeight,
                        horizontalMinorBarCount = 3,
                        weatherIconSize = 28.dp,
                        timeClock = appSettings.widgetDateTime
                    ).apply {
                        val widthSpec = MeasureSpec.makeMeasureSpec(
                            (width - 2 * padding).toInt(),
                            MeasureSpec.EXACTLY
                        )
                        val heightSpec = MeasureSpec.makeMeasureSpec(
                            (height - hourlyChartTop - padding).toInt(),
                            MeasureSpec.EXACTLY
                        )
                        measure(widthSpec, heightSpec)
                        layout(0, 0, measuredWidth, measuredHeight)
                    }
                    save()
                    translate(2 * padding, labelTop)
                    hourlyChartLabel.draw(this)
                    restore()
                    translate(0f, hourlyChartTop)
                    hourlyChart.draw(this)
//                restore()
                }
                3 -> {
                    val chartTop = y + 48.dp.toPx(myDensity) + padding
//                    val chartHeight = height - chartTop - 3 * padding

//                    val dailyChart = DailyChart(
//                        context = context,
//                        dailyChartData = dailyChartData,
//                        diagramHorPadding = 2 * padding,
//                        diagramVertPadding = padding,
//                        appSettings = appSettings,
//                        frameHeight = chartHeight,
//                        weatherIconSize = 28.dp
//                    )
                    val dailyView = DailyView(
                        context = context,
                        dailyChartData = dailyChartData,
                        horPadding = padding,
                        vertPadding = minorPadding,
                        appSettings = appSettings,
                        topY = chartTop,
                        timeFont = ResourcesCompat.getFont(context, R.font.cmu_typewriter_bold)!!,
                        numberFont = ResourcesCompat.getFont(context, R.font.cmu_serif)!!
                    ).apply {
                        val widthSpec = MeasureSpec.makeMeasureSpec(
                            (this.width),
                            MeasureSpec.EXACTLY
                        )
                        val heightSpec = MeasureSpec.makeMeasureSpec(
                            this.height,
                            MeasureSpec.EXACTLY
                        )
                        measure(widthSpec, heightSpec)
                        layout(0, 0, measuredWidth, measuredHeight)
                    }
//                    translate(0f, chartTop)
                    dailyView.draw(this)
                }
            }





//            drawCircle(80f, 350f, 30f,
//                Paint().apply {
//                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
//                }
//            )


        }
    }


}