package com.takaapoo.weatherer.main

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.viewModels.HomeViewModel
import com.takaapoo.weatherer.ui.viewModels.PreferenceViewModel
import com.takaapoo.weatherer.ui.widget.WeatherWidget1
import com.takaapoo.weatherer.ui.widget.Widget1ConfigurationScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Widget1ConfigurationActivity: ComponentActivity() {

    private val preferenceViewModel: PreferenceViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Transparent.toArgb(),
                darkScrim = Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Transparent.toArgb(),
                darkScrim = Transparent.toArgb()
            )
        )

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultValue)


        setContent {
            val appSettings by preferenceViewModel.appSettings.collectAsStateWithLifecycle()
            val allLocations by homeViewModel.allLocations.collectAsStateWithLifecycle()
            WeathererTheme(
                appSettings = appSettings
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Widget1ConfigurationScreen(
                        modifier = Modifier.fillMaxSize(),
                        allLocations = allLocations,
                        appSettings = appSettings,
                        onUpdateWidgetDateTime = preferenceViewModel::updateWidgetDateTime,
                        onUpdateWidgetLocationId = preferenceViewModel::updateWidgetLocationId,
                        onUpdateWidgetHourlyDiagramQuantities = preferenceViewModel::updateWidgetHourlyDiagramQuantities,
                        onSaveSettings = {
                            val manager = GlanceAppWidgetManager(this)
                            val widget = WeatherWidget1()
                            lifecycleScope.launch {
//                                updateAppWidgetState(
//                                    this@Widget1ConfigurationActivity,
//                                    manager.getGlanceIdBy(appWidgetId)
//                                ) { pref ->
//                                    pref[tabKey] = pref[tabKey] ?: 1
//                                }
                                widget.updateAll(this@Widget1ConfigurationActivity)
//                                val glanceIds = manager.getGlanceIds(widget.javaClass)
//                                glanceIds.forEach { glanceId ->
//                                    widget.update(this@Widget1ConfigurationActivity, glanceId)
//                                }
                                setResult(RESULT_OK, resultValue)
                                finish()
                            }
                        },
                    )
                }
            }
        }

    }
}