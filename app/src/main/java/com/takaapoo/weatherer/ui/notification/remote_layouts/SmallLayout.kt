package com.takaapoo.weatherer.ui.notification.remote_layouts

import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.WeatherType.Companion.calculateMoonType
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.moonPhaseMap
import com.takaapoo.weatherer.ui.notification.NotificationLayout
import com.takaapoo.weatherer.ui.notification.custom_views.SmallNotification
import com.takaapoo.weatherer.utility.readJSONFromAssets
import com.takaapoo.weatherer.utility.renderViewToBitmap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

class SmallLayout(
    override val layout: RemoteViews,
    context: Context,
    notificationHourlyChartData: List<HourlyChartDto>,
    val appSettings: AppSettings
): NotificationLayout {
    val zone: ZoneId = when (appSettings.notificationDateTime) {
        HourlyDiagramTime.UTC -> ZoneId.of("UTC")
        HourlyDiagramTime.DEVICE -> ZoneId.systemDefault()
        HourlyDiagramTime.LOCATION -> {
            ZoneOffset.ofTotalSeconds((notificationHourlyChartData.getOrNull(0)?.utcOffset?.toInt() ?: 0) * 60)
        }
    }

    val now: LocalDateTime = LocalDateTime.now(zone)
    val dayOfWeek: String = now.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val month = now.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
    val dayOfMonth = now.dayOfMonth
    val clockText: String = now.format(DateTimeFormatter.ofPattern("HH\u202F:\u202Fmm"))
    val date = "$dayOfWeek., $month. $dayOfMonth,"

    val hourlyWeather = notificationHourlyChartData.find {
        it.hourlyWeather?.time == LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.HOURS).toString()
    }?.hourlyWeather

    init {
        if (notificationHourlyChartData.isNotEmpty()) {
            moonPhaseMap = Json.parseToJsonElement(readJSONFromAssets(context, "moon_phase.json"))
                .jsonObject.toMap()
            val weatherType = WeatherType.fromWMO(
                code = hourlyWeather?.weatherCode,
                isDay = hourlyWeather?.isDay != 0,
                moonType = calculateMoonType(now.toLocalDate())
            )

            val smallNotification = SmallNotification(
                context = context,
                hourlyChartData = notificationHourlyChartData.toImmutableList(),
                weatherType = weatherType,
                appSettings = appSettings
            ).apply {
                val widthSpec = View.MeasureSpec.makeMeasureSpec(
                    context.resources.getDimension(R.dimen.notification_temperature_width).toInt(),
                    View.MeasureSpec.EXACTLY
                )
                val heightSpec = View.MeasureSpec.makeMeasureSpec(
                    context.resources.getDimension(R.dimen.notification_weather_type_size).toInt(),
                    View.MeasureSpec.EXACTLY
                )
                measure(widthSpec, heightSpec)
                layout(0, 0, measuredWidth, measuredHeight)
            }
            val bitmap = renderViewToBitmap(smallNotification)

            layout.apply {
                setTextViewText(
                    R.id.location_name,
                    notificationHourlyChartData.getOrNull(0)?.locationName ?: ""
                )
                setTextViewText(R.id.date, date)
                setTextViewText(R.id.clock, clockText)
                setImageViewBitmap(R.id.temperature, bitmap)
            }
        }
    }

}