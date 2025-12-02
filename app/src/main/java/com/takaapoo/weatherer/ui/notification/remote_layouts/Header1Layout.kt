package com.takaapoo.weatherer.ui.notification.remote_layouts

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.ui.notification.NotificationLayout
import com.takaapoo.weatherer.ui.notification.custom_views.CurrentIcon
import com.takaapoo.weatherer.ui.notification.custom_views.CurrentValue
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.utility.getColorScheme
import com.takaapoo.weatherer.utility.renderViewToBitmap
import com.takaapoo.weatherer.utility.toPx

@SuppressLint("NewApi")
class Header1Layout(
    override val layout: RemoteViews,
    notificationWidth: Dp,
    context: Context,
    notificationHourlyChartData: List<HourlyChartDto>,
    val settings: AppSettings,
    currentQuantities: List<HourlyWeatherQuantity>,
): NotificationLayout {
    val density = Density(context)
    val colorScheme = getColorScheme(context, settings.dynamicColor)
    val iconHeight = context.resources.getDimension(R.dimen.notification_icon_size)
    val iconWidth = context.resources.getDimension(R.dimen.notification_icon_width_with_point)
    val valueHeight = context.resources.getDimension(R.dimen.notification_icon_value_height)
    val valueWidth = context.resources.getDimension(R.dimen.notification_icon_value_width)

    val packageName: String? = context.packageName


    init {
        currentQuantities.forEachIndexed { i, quantity ->
            val icon = CurrentIcon(
                context = context,
                iconId = quantity.iconId,
                colorScheme = colorScheme
            ).apply {
                val widthSpec = View.MeasureSpec.makeMeasureSpec(
                    iconWidth.toInt(),
                    View.MeasureSpec.EXACTLY
                )
                val heightSpec = View.MeasureSpec.makeMeasureSpec(
                    iconHeight.toInt(),
                    View.MeasureSpec.EXACTLY
                )
                measure(widthSpec, heightSpec)
                layout(0, 0, measuredWidth, measuredHeight)
            }
            val iconBitmap = renderViewToBitmap(icon)

            val value = CurrentValue(
                context = context,
                hourlyChartData = notificationHourlyChartData,
                chartQuantity = quantity,
                appSettings = settings,
                fontSize = 14.sp.toPx(density)
            ).apply {
                val widthSpec = View.MeasureSpec.makeMeasureSpec(
                    valueWidth.toInt(),
                    View.MeasureSpec.EXACTLY
                )
                val heightSpec = View.MeasureSpec.makeMeasureSpec(
                    valueHeight.toInt(),
                    View.MeasureSpec.EXACTLY
                )
                measure(widthSpec, heightSpec)
                layout(0, 0, measuredWidth, measuredHeight)
            }
            val valueBitmap = renderViewToBitmap(value)

            layout.apply {
                val iconId = context.resources.getIdentifier("icon${i+1}", "id", packageName)
                val connectorId = context.resources.getIdentifier("icon${i+1}_connector", "id", packageName)
                val valueId = context.resources.getIdentifier("icon${i+1}_value", "id", packageName)
                if (iconId == 0) return@apply
                setImageViewBitmap(iconId, iconBitmap)
                setImageViewResource(connectorId, R.drawable.notif_connector)
                setColorInt(
                    connectorId,
                    "setColorFilter",
                    colorScheme.primaryContainer.toArgb(),
                    colorScheme.primaryContainer.toArgb()
                )
                setImageViewBitmap(valueId, valueBitmap)
            }
        }

        layout.apply {
            setViewVisibility(R.id.quantities, View.GONE)
            setViewVisibility(R.id.diagram, View.GONE)
            setViewVisibility(R.id.daily_diagram, View.GONE)
            setViewVisibility(R.id.header2_indicator, View.INVISIBLE)
            setViewVisibility(R.id.header3_indicator, View.INVISIBLE)

            setTextColor(R.id.header1, colorScheme.onSurface.toArgb())
            setTextColor(R.id.header2, colorScheme.onSurfaceVariant.toArgb())
            setTextColor(R.id.header3, colorScheme.onSurfaceVariant.toArgb())
            setColorInt(
                R.id.header1_indicator,
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
        }
    }

}