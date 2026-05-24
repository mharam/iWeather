package com.takaapoo.weatherer.data.repository

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.takaapoo.weatherer.domain.repository.DataRefreshRepository
import com.takaapoo.weatherer.ui.screens.home.RefreshWorker
import java.time.Duration
import javax.inject.Inject

class DataRefreshRepositoryImpl @Inject constructor(
    private val workManager: WorkManager
): DataRefreshRepository {

    override fun refreshData() {
//        val networkConstraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val refreshWork = PeriodicWorkRequestBuilder<RefreshWorker>(
            repeatInterval = Duration.ofHours(1),
//            flexTimeInterval = Duration.ofMinutes(1)
        )
//            .setConstraints(networkConstraint)
//            .setExpedited(policy = OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniquePeriodicWork(
            /* uniqueWorkName = */ "DataRefresh",
            /* existingPeriodicWorkPolicy = */ ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            /* periodicWork = */ refreshWork
        )
    }
}