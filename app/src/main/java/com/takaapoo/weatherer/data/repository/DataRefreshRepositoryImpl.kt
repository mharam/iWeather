package com.takaapoo.weatherer.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.takaapoo.weatherer.domain.repository.DataRefreshRepository
import com.takaapoo.weatherer.ui.screens.home.RefreshWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject

class DataRefreshRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
): DataRefreshRepository {
    private val workManager = WorkManager.getInstance(context)
    override fun refreshData() {
        val networkConstraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val refreshBuilder = PeriodicWorkRequestBuilder<RefreshWorker>(
            repeatInterval = Duration.ofHours(8)
        ).apply {
            setConstraints(networkConstraint)
        }
        workManager.enqueueUniquePeriodicWork(
            "DataRefresh",
            ExistingPeriodicWorkPolicy.KEEP,
            refreshBuilder.build()
        )
    }
}