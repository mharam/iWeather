package com.takaapoo.weatherer

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.ui.screens.home.RefreshWorker
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@HiltAndroidTest
//@RunWith(AndroidJUnit4::class)
//@Config(application = HiltTestApplication::class)
//@CustomTestApplication(value = WeatherApplication::class)
class RefreshWorkerTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    private lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun refreshWorker_doWork_resultSuccess() {
        val worker = TestListenableWorkerBuilder<RefreshWorker>(context)
            .setWorkerFactory(HiltWorkerFactory.getDefaultWorkerFactory())
            .build()
        runBlocking {
            val result = worker.doWork()
            assertThat(result).isEqualTo(ListenableWorker.Result.Success())
        }
    }
}