package com.takaapoo.weatherer.ui.screens.home

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.use_case.GetHourlyChartDataUseCase
import com.takaapoo.weatherer.domain.use_case.UpdateWeatherUseCase
import com.takaapoo.weatherer.setDataRefreshAlarm
import com.takaapoo.weatherer.ui.notification.notify
import com.takaapoo.weatherer.ui.screens.updateAllWidgets
import com.takaapoo.weatherer.utility.NetworkObserver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class RefreshWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val updateWeatherUseCase: UpdateWeatherUseCase,
    private val preferenceRepository: PreferenceRepository,
    private val localWeatherRepository: LocalWeatherRepository,
    private val hourlyChartDataUseCase: GetHourlyChartDataUseCase
): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            if (NetworkObserver(context).isConnectedToInternet())
                updateWeatherUseCase.updateAllLocationsWeatherBatch()

//            if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
            notify(
                context,
                header = null,
                preferenceRepository,
                localWeatherRepository,
                hourlyChartDataUseCase
            )
            updateAllWidgets(context)

//            }
            setDataRefreshAlarm(context)
            Result.success()
        } catch (_: Throwable) {
            Result.failure()
        }
    }

//    override suspend fun getForegroundInfo(): ForegroundInfo {
//        return ForegroundInfo(
//            NOTIFICATION_ID, createNotification()
//        )
//    }


}