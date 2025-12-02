package com.takaapoo.weatherer.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Align
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.view.View
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.withSave
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.WeatherType.Companion.drawWeatherIconOnCanvas
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.HistoricalState
import com.takaapoo.weatherer.utility.getColorScheme
import com.takaapoo.weatherer.utility.toPx
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DailyView(
    context: Context,
    val dailyChartData: List<LocalDailyWeather> = emptyList(),
    val horPadding: Float = 16f,
    val vertPadding: Float = 24f,
    val appSettings: AppSettings,
    val topY: Float,
    val timeFont: Typeface,
    val numberFont: Typeface,
) : View(context) {

    val myDensity = Density(context)
    val unitsWidth = 12.dp.toPx(myDensity)
    val iconCircleSize = 38.dp.toPx(myDensity)
    val iconSize = 22.dp.toPx(myDensity)
    val dataRowHeight = 38.dp.toPx(myDensity)
    val dataRowPadding = 8.dp.toPx(myDensity)
    val minDataColumnWidth = 64.dp.toPx(myDensity)
    val weatherIconSizePx = 32.dp.toPx(myDensity).toInt()

    val colorScheme = getColorScheme(context, appSettings.dynamicColor)
    val iconColorFilter = PorterDuffColorFilter(
        colorScheme.onPrimary.toArgb(),
        PorterDuff.Mode.SRC_IN
    )
//    val temperatureIconDrawable = ContextCompat.getDrawable(context, R.drawable.temperature)
//    val windIconDrawable = ContextCompat.getDrawable(context, R.drawable.wind)
//    val precipitationIconDrawable = ContextCompat.getDrawable(context, R.drawable.precipitation)
//    val uvIconDrawable = ContextCompat.getDrawable(context, R.drawable.uv)


    val now: LocalDate = LocalDate.now()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableWidth = canvas.width - 2 * horPadding - unitsWidth - dataRowHeight
        val columnCount = (availableWidth / minDataColumnWidth).toInt()
        val dataColumnWidth = availableWidth / columnCount
//Log.i("column1", "horizontalWidth = $availableWidth , minDataColumnWidth = $minDataColumnWidth")


        val dateBottom = drawDateRibbon(
            canvas = canvas,
            now = now,
            columnCount = columnCount,
            columnWidth =  dataColumnWidth,
            topY = topY,
            fontSize = 14.sp.toPx(myDensity),
            timeFont = timeFont,
            textColor = colorScheme.onSurface.toArgb(),
            gap = 4.dp.toPx(myDensity)
        )

        var dataRowTopY = dateBottom + dataRowHeight

        DailyWeatherQuantity.entries
            .filter { it.historicalState != HistoricalState.HISTORIC_ONLY }
            .forEach { weatherQuantity ->
                if (dataRowTopY + dataRowHeight + dataRowPadding < canvas.height) {
                    drawDataRow(
                        canvas = canvas,
                        now = now,
                        dailyChartData = dailyChartData,
                        weatherQuantity = weatherQuantity,
                        columnWidth = dataColumnWidth,
                        topY = dataRowTopY,
                        numberFont = numberFont
                    )
                    dataRowTopY += (dataRowHeight + dataRowPadding)
                }
            }

        drawWeatherIcon(
            canvas = canvas,
            now = now,
            dailyChartData = dailyChartData,
            columnWidth = dataColumnWidth,
            topY = dateBottom,
        )


    }


    private fun drawDateRibbon(
        canvas: Canvas,
        now: LocalDate,
        columnCount: Int,
        columnWidth: Float,
        topY: Float,
        fontSize: Float,
        timeFont: Typeface,
        textColor: Int,
        gap: Float,
    ): Float {
        val startX = horPadding + dataRowHeight
        val textPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
            typeface = timeFont
            color = textColor
            textSize = fontSize
            textAlign = Align.CENTER
        }
        val textPaint2 = TextPaint(ANTI_ALIAS_FLAG).apply {
            typeface = timeFont
            color = textColor
            textSize = fontSize
            textAlign = Align.LEFT
        }
        val linePaint = Paint().apply {
            strokeWidth = 2.dp.toPx(myDensity)
            color = colorScheme.primary.toArgb()
            strokeCap = Paint.Cap.ROUND
        }

        var dayOfMonthX = startX - columnWidth / 2
        val textBound = Rect()
        for (i in 0 until columnCount) {
            val dayOfWeekX = startX + (i + 0.5f) * columnWidth
            val date = now.plusDays(i.toLong())
            val dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEE"))
            val dayOfMonth = date.format(DateTimeFormatter.ofPattern("MMM d")).uppercase()
            textPaint.getTextBounds(dayOfMonth, 0, dayOfMonth.length, textBound)
            if (textBound.width() < 0.7f * (dayOfWeekX - dayOfMonthX)) {
                canvas.drawText(dayOfMonth, dayOfWeekX, topY + textBound.height(), textPaint)
                dayOfMonthX = dayOfWeekX
            }
            canvas.drawText(dayOfWeek, dayOfWeekX - textBound.width()/2,
                topY + 2 * textBound.height() + gap, textPaint2)
        }
        val lineY = topY + 2 * textBound.height() + gap + 8.dp.toPx(myDensity)
        canvas.drawLine(
            horPadding + dataRowHeight/2, lineY,
            canvas.width - horPadding - unitsWidth, lineY,
            linePaint
        )
        return lineY
    }

    private fun drawDataRow(
        canvas: Canvas,
        now: LocalDate,
        dailyChartData: List<LocalDailyWeather>,
        weatherQuantity: DailyWeatherQuantity,
        columnWidth: Float,
        topY: Float,
        numberFont: Typeface,
    ) {
        val textPadding = 6.dp.toPx(myDensity)
        val startX = horPadding + dataRowHeight
        val dailyDataList = dailyChartData.filter {
            LocalDate.parse(it.time).isAfter(now.minusDays(1)) &&
                    LocalDate.parse(it.time).isBefore(now.plusDays(5))
        }
        val cornerRadius = 8.dp.toPx(myDensity)
        val rowPaint = Paint().apply {
            color = colorScheme.surfaceVariant.toArgb()
        }
        val circlePaint = Paint().apply {
            color = colorScheme.primary.toArgb()
        }
        val unitPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
            typeface = numberFont
            color = colorScheme.onSurface.toArgb()
            textSize = 14.sp.toPx(myDensity)
            textAlign = Align.CENTER
        }
        val valuePaint = TextPaint(ANTI_ALIAS_FLAG).apply {
            typeface = numberFont
            color = Color.RED
            textSize = 14.sp.toPx(myDensity)
            textAlign = Align.RIGHT
            strokeWidth = 1f
        }
        val iconDrawable = ContextCompat.getDrawable(context, weatherQuantity.iconId)
        val iconCenter = Offset(horPadding + dataRowHeight/2, topY + dataRowHeight/2)
        val textBound1 = Rect()
        val textBound2 = Rect()

        val rowPath = Path().apply {
            moveTo(horPadding + dataRowHeight/2, topY + dataRowHeight)
            arcTo(horPadding, topY, horPadding + dataRowHeight, topY + dataRowHeight, 90f, 180f, false)
            lineTo(canvas.width - horPadding - unitsWidth - cornerRadius, topY)
            arcTo(canvas.width - horPadding - unitsWidth - 2*cornerRadius, topY,
                canvas.width - horPadding - unitsWidth, topY + 2*cornerRadius, 270f, 90f, false)
            rLineTo(0f, dataRowHeight - 2*cornerRadius)
            arcTo(canvas.width - horPadding - unitsWidth - 2*cornerRadius, topY + dataRowHeight - 2*cornerRadius,
                canvas.width - horPadding - unitsWidth, topY + dataRowHeight, 0f, 90f, false)
            close()
        }
        canvas.apply {
            drawPath(rowPath, rowPaint)
            drawCircle(horPadding + dataRowHeight/2, topY + dataRowHeight/2, iconCircleSize/2, circlePaint)
            dailyDataList.forEachIndexed { i, dailyWeather ->
                when (weatherQuantity){
                    DailyWeatherQuantity.TEMPERATUREMINMAX -> {
                        val minTemperature = weatherQuantity.quantityValueText(
                            dailyWeather.temperatureMin, appSettings, false
                        ).toString() + "°"
                        val maxTemperature = weatherQuantity.quantityValueText(
                            dailyWeather.temperatureMax, appSettings, false
                        ).toString() + "°"
                        valuePaint.getTextBounds(minTemperature, 0, minTemperature.length, textBound1)
                        valuePaint.getTextBounds(maxTemperature, 0, maxTemperature.length, textBound2)

                        if (startX + i * columnWidth < canvas.width - horPadding - unitsWidth) {
                            drawText(
                                maxTemperature,
                                startX + (2 * i + 1) * columnWidth / 2 + textBound1.width() / 2,
                                topY + textBound1.height() + textPadding,
                                valuePaint.apply {
                                    color = Color.RED
                                    style = Paint.Style.FILL_AND_STROKE
                                }
                            )
                            drawText(
                                minTemperature,
                                startX + (2 * i + 1) * columnWidth / 2 + textBound1.width() / 2,
                                topY + dataRowHeight - textPadding,
                                valuePaint.apply { color = context.getColor(R.color.notif_min_temperature) }
                            )
                        }
                    }
                    DailyWeatherQuantity.PRECIPITATIONSUM -> {
                        val precipitation = weatherQuantity.quantityValueText(
                            dailyWeather.precipitationSum, appSettings, false
                        ).toString()
                        valuePaint.getTextBounds(precipitation, 0, precipitation.length, textBound1)
                        if (startX + i * columnWidth < canvas.width - horPadding - unitsWidth) {
                            drawText(
                                precipitation,
                                startX + (2 * i + 1) * columnWidth / 2 + textBound1.width() / 2,
                                topY + (dataRowHeight + textBound1.height()) / 2,
                                valuePaint.apply {
                                    color = colorScheme.onSurface.toArgb()
                                    style = Paint.Style.FILL
                                }
                            )
                        }
                    }
                    DailyWeatherQuantity.PRECIPITATIONPROBABILITYMAX -> {
                        val precipitation = weatherQuantity.quantityValueText(
                            dailyWeather.precipitationProbabilityMax, appSettings, false
                        ).toString()
                        valuePaint.getTextBounds(precipitation, 0, precipitation.length, textBound1)
                        if (startX + i * columnWidth < canvas.width - horPadding - unitsWidth) {
                            drawText(
                                precipitation,
                                startX + (2 * i + 1) * columnWidth / 2 + textBound1.width() / 2,
                                topY + (dataRowHeight + textBound1.height()) / 2,
                                valuePaint.apply { color = colorScheme.onSurface.toArgb() }
                            )
                        }
                    }
                    DailyWeatherQuantity.WINDSPEEDMAX -> {
                        val windSpeed = weatherQuantity.quantityValueText(
                            dailyWeather.windSpeedMax, appSettings, false
                        ).toString()
                        valuePaint.getTextBounds(windSpeed, 0, windSpeed.length, textBound1)
                        if (startX + i * columnWidth < canvas.width - horPadding - unitsWidth) {
                            drawText(
                                windSpeed,
                                startX + (2 * i + 1) * columnWidth / 2 + textBound1.width() / 2,
                                topY + (dataRowHeight + textBound1.height()) / 2,
                                valuePaint.apply { color = colorScheme.onSurface.toArgb() }
                            )
                        }
                    }
                    DailyWeatherQuantity.SUNRISESET -> {
                        val sunRise = dailyWeather.sunRise?.substringAfter('T') ?: "?"
                        valuePaint.getTextBounds(sunRise, 0, sunRise.length, textBound1)
                        val sunSet = dailyWeather.sunSet?.substringAfter('T') ?: "?"
                        valuePaint.getTextBounds(sunSet, 0, sunSet.length, textBound2)
                        if (startX + i * columnWidth < canvas.width - horPadding - unitsWidth) {
                            drawText(
                                sunSet,
                                startX + (2 * i + 1) * columnWidth / 2 + textBound1.width() / 2,
                                topY + textBound1.height() + textPadding,
                                valuePaint.apply { color = colorScheme.onSurface.toArgb() }
                            )
                            drawText(
                                sunRise,
                                startX + (2 * i + 1) * columnWidth / 2 + textBound1.width() / 2,
                                topY + dataRowHeight - textPadding,
                                valuePaint
                            )
                        }
                    }
                    DailyWeatherQuantity.UVINDEXMAX -> {
                        val uv = weatherQuantity.quantityValueText(
                            dailyWeather.uvIndexMax, appSettings, false
                        ).toString()
                        valuePaint.getTextBounds(uv, 0, uv.length, textBound1)
                    }
                    else -> {}
                }
            }


            withSave {
                rotate(-90f)
                drawText(
                    weatherQuantity.unit(appSettings).toString().trim(' ', '°'),
                    -(topY + dataRowHeight / 2),
                    canvas.width - horPadding,
                    unitPaint
                )
            }
        }
        iconDrawable?.apply {
            setBounds((iconCenter.x - iconSize/2).toInt(), (iconCenter.y - iconSize/2).toInt(),
                (iconCenter.x + iconSize/2).toInt(), (iconCenter.y + iconSize/2).toInt())
            colorFilter = iconColorFilter
            draw(canvas)
        }

    }

    private fun drawWeatherIcon(
        canvas: Canvas,
        now: LocalDate,
        dailyChartData: List<LocalDailyWeather>,
        columnWidth: Float,
        topY: Float,
    ) {
        val topPadding = 4.dp.toPx(myDensity)
        val startX = horPadding + dataRowHeight
        val dailyDataList = dailyChartData.filter {
            LocalDate.parse(it.time).isAfter(now.minusDays(1)) &&
                    LocalDate.parse(it.time).isBefore(now.plusDays(5))
        }

        canvas.apply {
            dailyDataList.forEachIndexed { i, dailyWeather ->
                if (startX + i * columnWidth < canvas.width - horPadding - unitsWidth) {
                    val weatherType = WeatherType.fromWMO(dailyWeather.weatherCode, true, null)
                    val topLeft = IntOffset(
                        x = (startX + (i + 0.5f) * columnWidth - weatherIconSizePx/2).toInt(),
                        y = (topY + topPadding).toInt()
                    )
                    drawWeatherIconOnCanvas(
                        canvas,
                        weatherType,
                        context,
                        topLeft,
                        iconSize = IntSize(weatherIconSizePx, weatherIconSizePx),
                        Paint()
                    )
                }
            }
        }
    }

    
}