package com.takaapoo.weatherer.ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.use_case.GetHourlyChartDataUseCase
import com.takaapoo.weatherer.ui.screens.setNotificationAlarm
import com.takaapoo.weatherer.ui.screens.updateAllWidgets
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@AndroidEntryPoint
class NotificationHeaderReceiver: BroadcastReceiver() {

    @Inject lateinit var preferenceRepository: PreferenceRepository
    @Inject lateinit var localWeatherRepository: LocalWeatherRepository
    @Inject lateinit var hourlyChartDataUseCase: GetHourlyChartDataUseCase

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "iWeather:NotificationWakeLock")

        val header = intent?.getIntExtra("header", -1)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                wakeLock.acquire(60_000)
                notify(
                    context,
                    header,
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

suspend fun notify(
    context: Context,
    header: Int?,
    preferenceRepository: PreferenceRepository,
    localWeatherRepository: LocalWeatherRepository,
    hourlyChartDataUseCase: GetHourlyChartDataUseCase
){
    val settings = preferenceRepository.getSettings()
    if (!settings.notificationVisibility || settings.notificationLocationId == null) return

    val nonNullHeader = if (header != null && header in 1 .. 3) header
    else settings.lastNotificationHeaderSelected

    val hourlyChartData = hourlyChartDataUseCase(
        locationId = settings.notificationLocationId,
        startDate = LocalDate.now(ZoneId.of("UTC")).minusDays(1).toString(),
        endDate = LocalDate.now(ZoneId.of("UTC")).plusDays(3).toString(),
        appSettings = settings
    )
    val dailyChartData = localWeatherRepository.getLocationDailyWeather(
        locationId = settings.notificationLocationId,
        startDate = LocalDate.now().toString(),
        endDate = LocalDate.now().plusDays(4).toString(),
        appSettings = settings
    )

//    preferenceRepository.setNotificationCount(settings.notificationCount + 1)
    NotificationSender(
        context = context,
        header = nonNullHeader,
        notificationHourlyChartData = hourlyChartData,
        notificationDailyChartData = dailyChartData,
        appSettings = settings,
        hourlyChartQuantities = settings.notificationHourlyQuantities,
        currentQuantities = settings.notificationCurrentQuantities
    ).sendNotification()

    preferenceRepository.setLastNotificationHeaderSelected(nonNullHeader)
    setNotificationAlarm(context)
}