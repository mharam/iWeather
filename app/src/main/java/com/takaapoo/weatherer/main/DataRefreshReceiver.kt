package com.takaapoo.weatherer.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.use_case.GetHourlyChartDataUseCase
import com.takaapoo.weatherer.domain.use_case.UpdateWeatherUseCase
import com.takaapoo.weatherer.setDataRefreshAlarm
import com.takaapoo.weatherer.ui.notification.notify
import com.takaapoo.weatherer.ui.screens.updateAllWidgets
import com.takaapoo.weatherer.utility.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DataRefreshReceiver : BroadcastReceiver() {

    @Inject lateinit var updateWeatherUseCase: UpdateWeatherUseCase
    @Inject lateinit var preferenceRepository: PreferenceRepository
    @Inject lateinit var localWeatherRepository: LocalWeatherRepository
    @Inject lateinit var hourlyChartDataUseCase: GetHourlyChartDataUseCase

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "iWeather:DataRefreshWakeLock")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                wakeLock.acquire(60_000)
                setDataRefreshAlarm(context)
                if (NetworkUtils.isConnectedToInternet(context)) {
                    updateWeatherUseCase.updateAllLocationsWeatherBatch()
                }
                notify(
                    context,
                    header = null,
                    preferenceRepository,
                    localWeatherRepository,
                    hourlyChartDataUseCase
                )
                updateAllWidgets(context)
            } finally {
                wakeLock.release()
            }
        }
    }
}