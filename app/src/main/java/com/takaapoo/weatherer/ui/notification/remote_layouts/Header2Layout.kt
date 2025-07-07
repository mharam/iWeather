package com.takaapoo.weatherer.ui.notification.remote_layouts

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.ui.notification.NotificationLayout
import com.takaapoo.weatherer.ui.notification.custom_views.HourlyChart
import com.takaapoo.weatherer.ui.notification.custom_views.HourlyChartLabel
import com.takaapoo.weatherer.utility.getColorScheme
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.utility.renderViewToBitmap
import com.takaapoo.weatherer.utility.spToFloat
import com.takaapoo.weatherer.utility.toPx
import kotlinx.collections.immutable.toImmutableList

@SuppressLint("NewApi")
class Header2Layout(
    override val layout: RemoteViews,
    notificationWidth: Dp,
    context: Context,
    notificationHourlyChartData: List<HourlyChartDto>,
    val settings: AppSettings,
    chartQuantities: List<WeatherQuantity>,
): NotificationLayout {
    val density = Density(context)
    val colorScheme = getColorScheme(context, settings.dynamicColor)
    val labelWidth = notificationWidth.toPx(density) -
            context.resources.getDimension(R.dimen.notification_hour_diagram_label_margin_start)
    val labelHeight = context.resources.getDimension(R.dimen.notification_hour_diagram_label_height)
    val diagramHeight = context.resources.getDimension(R.dimen.notification_diagram_height)

    val hourlyChartLabel = HourlyChartLabel(
        context = context,
        hourlyChartData = notificationHourlyChartData,
        chartQuantities = chartQuantities,
        appSettings = settings,
        fontSize = spToFloat(context, 14f)
    ).apply {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(labelWidth.toInt(), View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(labelHeight.toInt(), View.MeasureSpec.EXACTLY)
        measure(widthSpec, heightSpec)
        layout(0, 0, measuredWidth, measuredHeight)
    }
    val labelBitmap = renderViewToBitmap(hourlyChartLabel)

    val hourlyChart = HourlyChart(
        context = context,
        hourlyChartData = notificationHourlyChartData.toImmutableList(),
        diagramHorPadding = 16.dp.toPx(density),
        diagramVertPadding = 16.dp.toPx(density),
        chartQuantities = chartQuantities,
        appSettings = settings,
    ).apply {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(
            notificationWidth.toPx(density).toInt(),
            View.MeasureSpec.EXACTLY
        )
        val heightSpec = View.MeasureSpec.makeMeasureSpec(
            diagramHeight.toInt(),
            View.MeasureSpec.EXACTLY
        )
        measure(widthSpec, heightSpec)
        layout(0, 0, measuredWidth, measuredHeight)
    }
    val diagramBitmap = renderViewToBitmap(hourlyChart)


    init {
        layout.apply {
            setViewVisibility(R.id.quantity1, View.GONE)
            setViewVisibility(R.id.quantity2, View.GONE)
            setViewVisibility(R.id.quantity3, View.GONE)
            setViewVisibility(R.id.quantity4, View.GONE)
            setViewVisibility(R.id.quantity5, View.GONE)
            setViewVisibility(R.id.quantity6, View.GONE)
            setViewVisibility(R.id.quantity7, View.GONE)
            setViewVisibility(R.id.daily_diagram, View.GONE)
            setViewVisibility(R.id.header1_indicator, View.INVISIBLE)
            setViewVisibility(R.id.header3_indicator, View.INVISIBLE)

            setTextColor(R.id.header1, colorScheme.onSurfaceVariant.toArgb())
            setTextColor(R.id.header2, colorScheme.onSurface.toArgb())
            setTextColor(R.id.header3, colorScheme.onSurfaceVariant.toArgb())
            setColorInt(
                R.id.header2_indicator,
                "setBackgroundColor",
                colorScheme.primary.toArgb(),
                colorScheme.primary.toArgb()
            )
            setColorInt(
                R.id.divider,
                "setBackgroundColor",
                colorScheme.outlineVariant.toArgb(),
                colorScheme.outlineVariant.toArgb()
            )

            setImageViewBitmap(R.id.diagram, diagramBitmap)
            setImageViewBitmap(R.id.quantities, labelBitmap)
        }
    }

}