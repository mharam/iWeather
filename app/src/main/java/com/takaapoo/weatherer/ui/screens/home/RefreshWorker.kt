package com.takaapoo.weatherer.ui.screens.home

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.takaapoo.weatherer.domain.use_case.UpdateWeatherUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class RefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val updateWeatherUseCase: UpdateWeatherUseCase
): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
//        Log.i("refresh1", "doWork")
        return try {
            updateWeatherUseCase.updateAllLocationsWeather()
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