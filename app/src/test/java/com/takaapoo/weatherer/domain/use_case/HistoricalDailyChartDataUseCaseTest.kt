package com.takaapoo.weatherer.domain.use_case

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.data.local.LocalHistoricDailyWeather
import com.takaapoo.weatherer.data.local.testLocation
import com.takaapoo.weatherer.data.remote.HistoricDailyWeather
import com.takaapoo.weatherer.data.remote.HistoricDailyWeatherPack
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HistoricDailyChartDto
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.RemoteWeatherRepository
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@RunWith(AndroidJUnit4::class)
class HistoricalDailyChartDataUseCaseTest {

    private val dispatcher = StandardTestDispatcher()
    private val historicDailyData = listOf(
        LocalHistoricDailyWeather(
            locationId = testLocation.id,
            time = "2025-02-01",
            weatherCode = 2,
            temperatureMean = 4.6f
        ),
        LocalHistoricDailyWeather(
            locationId = testLocation.id,
            time = "2025-02-02",
            weatherCode = 3,
            temperatureMean = 3.3f
        )
    )
    private val historicDailyWeatherPack = HistoricDailyWeatherPack(
        latitude = testLocation.latitude,
        longitude = testLocation.longitude,
        utcOffsetSeconds = testLocation.utcOffset?.toInt() ?: 0,
        timeZone = "",
        timeZoneAbbreviation = "",
        elevation = testLocation.elevation,
        dailyWeather = HistoricDailyWeather(
            time = listOf("2025-02-01", "2025-02-02"),
            weatherCode = listOf(2, 3),
            temperatureMean = listOf(4.6f, 3.3f)
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke when data already exist`() = runTest {
        val fakeLocalWeatherRepository = mock<LocalWeatherRepository> {
            on { getLocationHistoricDailyWeather(any(), any(), any()) } doReturn historicDailyData
        }
        val fakeRemoteWeatherRepository = mock<RemoteWeatherRepository> {}
        val historicalDailyChartDataUseCase = HistoricalDailyChartDataUseCase(
            fakeLocalWeatherRepository,
            fakeRemoteWeatherRepository,
            dispatcher
        )
        val xAxisStart = ChronoUnit.DAYS.between(
            LocalDate.now(),
            LocalDate.parse(historicDailyData.first().time)
        )
        val xAxisEnd = ChronoUnit.DAYS.between(
            LocalDate.now(),
            LocalDate.parse(historicDailyData.last().time)
        )

        historicalDailyChartDataUseCase(
            testLocation,
            xAxisStart.toFloat(),
            xAxisEnd.toFloat(),
            DailyWeatherQuantity.TEMPERATUREMEAN,
            AppSettings()
        ){
            advanceUntilIdle()
            assertThat(it).isInstanceOf(MyResult.Success::class.java)
            assertThat(((it as MyResult.Success).data as List<HistoricDailyChartDto>).first().dailyWeather?.temperatureMean)
                .isEqualTo(4.6f)
        }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke when data does not exist`() = runTest {
        val fakeLocalWeatherRepository = mock<LocalWeatherRepository> {
            on { getLocationHistoricDailyWeather(any(), any(), any()) } doReturn emptyList()
        }
        val fakeRemoteWeatherRepository = mock<RemoteWeatherRepository> {
            on { getHistoricDailyWeatherFromServer(
                any(),
                any(),
                any(),
                any(),
                any()
            ) } doReturn MyResult.Success(historicDailyWeatherPack)
        }
        val historicalDailyChartDataUseCase = HistoricalDailyChartDataUseCase(
            fakeLocalWeatherRepository,
            fakeRemoteWeatherRepository,
            dispatcher
        )
        val xAxisStart = ChronoUnit.DAYS.between(
            LocalDate.now(),
            LocalDate.parse(historicDailyWeatherPack.dailyWeather.time.first())
        )
        val xAxisEnd = ChronoUnit.DAYS.between(
            LocalDate.now(),
            LocalDate.parse(historicDailyWeatherPack.dailyWeather.time.last())
        )

        val results = mutableListOf<MyResult<*>>()
        historicalDailyChartDataUseCase(
            testLocation,
            xAxisStart.toFloat(),
            xAxisEnd.toFloat(),
            DailyWeatherQuantity.TEMPERATUREMEAN,
            AppSettings()
        ){
            results.add(it)
        }
        assertThat(results.first()).isInstanceOf(MyResult.Loading::class.java)
        assertThat(results.last()).isInstanceOf(MyResult.Success::class.java)
        assertThat(((results.last() as MyResult.Success).data as List<HistoricDailyChartDto>).first().dailyWeather?.temperatureMean)
            .isEqualTo(4.6f)
    }
}