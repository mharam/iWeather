package com.takaapoo.weatherer.data.repository

import android.content.Context
import androidx.work.WorkManager
import com.takaapoo.weatherer.domain.repository.DataRefreshRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DataRefreshRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
): DataRefreshRepository {

    private val workManager = WorkManager.getInstance(context)

    override fun refreshData() {
//        Log.i("refresh1", "refreshData")
//        val networkConstraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
//        val refreshBuilder = PeriodicWorkRequestBuilder<RefreshWorker>(
//            repeatInterval = Duration.ofHours(6),
////            flexTimeInterval = Duration.ofMinutes(1)
//        ).apply {
//            setConstraints(networkConstraint)
////            setExpedited(policy = OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
//        }
//        workManager.enqueueUniquePeriodicWork(
//            /* uniqueWorkName = */ "DataRefresh",
//            /* existingPeriodicWorkPolicy = */ ExistingPeriodicWorkPolicy.REPLACE,
//            /* periodicWork = */ refreshBuilder.build()
//        )
    }
}