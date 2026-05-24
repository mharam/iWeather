package com.takaapoo.weatherer.data.repository

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class DataRefreshRepositoryImplTest {
    @Test
    fun `refreshData enqueues periodic work`() {
        val workManager = mock<WorkManager>()
        val dataRefreshRepository = DataRefreshRepositoryImpl(workManager)

        dataRefreshRepository.refreshData()

        verify(workManager).enqueueUniquePeriodicWork(
            eq("DataRefresh"),
            eq(ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE),
            any()
        )
    }

}