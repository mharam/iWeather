package com.takaapoo.weatherer.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import com.takaapoo.weatherer.ui.widget.WeatherWidget2.Companion.layoutHeight
import com.takaapoo.weatherer.ui.widget.WeatherWidget2.Companion.layoutWidth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherWidgetReceiver1: GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherWidget1()
}

class WeatherWidgetReceiver2: GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherWidget2()

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        val minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)

        val manager = GlanceAppWidgetManager(context)
        val glanceId = manager.getGlanceIdBy(appWidgetId)

        CoroutineScope(Dispatchers.Main).launch {
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[layoutWidth] = minWidth
                prefs[layoutHeight] = (minHeight + maxHeight) / 2
            }

            glanceAppWidget.update(context, glanceId)
        }

    }
}