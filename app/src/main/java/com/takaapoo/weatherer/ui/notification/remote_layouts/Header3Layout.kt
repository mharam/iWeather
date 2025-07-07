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
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.ui.notification.NotificationLayout
import com.takaapoo.weatherer.ui.notification.custom_views.DailyChart
import com.takaapoo.weatherer.utility.getColorScheme
import com.takaapoo.weatherer.utility.renderViewToBitmap
import com.takaapoo.weatherer.utility.toPx

@SuppressLint("NewApi")
class Header3Layout(
    override val layout: RemoteViews,
    notificationWidth: Dp,
    context: Context,
    notificationDailyChartData: List<LocalDailyWeather>,
    val settings: AppSettings,
): NotificationLayout {
    val density = Density(context)
    val colorScheme = getColorScheme(context, settings.dynamicColor)
    val notificationHeight = context.resources.getDimension(R.dimen.notification_height)
    val headerHeight = context.resources.getDimension(R.dimen.notification_header_height)
    val headerMargin = context.resources.getDimension(R.dimen.notification_header_bottom_margin)
    val diagramHeight = notificationHeight - (headerHeight + headerMargin)

    val dailyChart = DailyChart(
        context = context,
        dailyChartData = notificationDailyChartData,
        diagramHorPadding = 16.dp.toPx(density),
        diagramVertPadding = 16.dp.toPx(density),
        appSettings = settings
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
    val diagramBitmap = renderViewToBitmap(dailyChart)

    init {
        layout.apply {
            setViewVisibility(R.id.quantity1, View.GONE)
            setViewVisibility(R.id.quantity2, View.GONE)
            setViewVisibility(R.id.quantity3, View.GONE)
            setViewVisibility(R.id.quantity4, View.GONE)
            setViewVisibility(R.id.quantity5, View.GONE)
            setViewVisibility(R.id.quantity6, View.GONE)
            setViewVisibility(R.id.quantity7, View.GONE)
            setViewVisibility(R.id.quantities, View.GONE)
            setViewVisibility(R.id.diagram, View.GONE)
            setViewVisibility(R.id.header1_indicator, View.INVISIBLE)
            setViewVisibility(R.id.header2_indicator, View.INVISIBLE)

            setTextColor(R.id.header1, colorScheme.onSurfaceVariant.toArgb())
            setTextColor(R.id.header2, colorScheme.onSurfaceVariant.toArgb())
            setTextColor(R.id.header3, colorScheme.onSurface.toArgb())
            setColorInt(
                R.id.header3_indicator,
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

            setImageViewBitmap(R.id.daily_diagram, diagramBitmap)
        }
    }

}