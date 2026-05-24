package com.takaapoo.weatherer.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.use_case.GetHourlyChartDataUseCase
import com.takaapoo.weatherer.domain.use_case.UpdateWeatherUseCase
import com.takaapoo.weatherer.setDataRefreshAlarm
import com.takaapoo.weatherer.ui.notification.notify
import com.takaapoo.weatherer.ui.screens.updateAllWidgets
import com.takaapoo.weatherer.utility.NetworkUtils
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.android.EarlyEntryPoints
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @EarlyEntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootReceiverEntryPoint {
        fun getPreferenceRepository(): PreferenceRepository
        fun getLocalWeatherRepository(): LocalWeatherRepository
        fun getHourlyChartDataUseCase(): GetHourlyChartDataUseCase
        fun getUpdateWeatherUseCase(): UpdateWeatherUseCase
    }

//    @Inject lateinit var preferenceRepository: PreferenceRepository
//    @Inject lateinit var localWeatherRepository: LocalWeatherRepository
//    @Inject lateinit var hourlyChartDataUseCase: GetHourlyChartDataUseCase
//    @Inject lateinit var updateWeatherUseCase: UpdateWeatherUseCase

    override fun onReceive(context: Context, intent: Intent?) {
        val entryPoint = EarlyEntryPoints.get(
            context.applicationContext,
            BootReceiverEntryPoint::class.java
        )
        val preferenceRepository = entryPoint.getPreferenceRepository()
        val localWeatherRepository = entryPoint.getLocalWeatherRepository()
        val hourlyChartDataUseCase = entryPoint.getHourlyChartDataUseCase()
        val updateWeatherUseCase = entryPoint.getUpdateWeatherUseCase()

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED){
            CoroutineScope(Dispatchers.IO).launch {
                context.let {
                    if (NetworkUtils.isConnectedToInternet(context = it))
                        updateWeatherUseCase.updateAllLocationsWeatherBatch()

                    setDataRefreshAlarm(context = it)
                    notify(
                        context,
                        header = null,
                        preferenceRepository,
                        localWeatherRepository,
                        hourlyChartDataUseCase
                    )
                    updateAllWidgets(it)
                }
            }
        }
    }
}