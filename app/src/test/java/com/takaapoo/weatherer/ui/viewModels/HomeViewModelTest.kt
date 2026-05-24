package com.takaapoo.weatherer.ui.viewModels

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.WeatherApplication
import com.takaapoo.weatherer.data.local.FakeWeatherDao
import com.takaapoo.weatherer.data.local.testLocation
import com.takaapoo.weatherer.data.repository.FakeLocalWeatherRepositoryImpl
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.repository.DataRefreshRepository
import com.takaapoo.weatherer.domain.repository.LocationRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.use_case.UpdateWeatherUseCase
import com.takaapoo.weatherer.moonPhase
import com.takaapoo.weatherer.utility.readMoonPhaseBinFromAssets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.time.Clock
import java.time.Instant
import java.time.ZoneId


@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {
    lateinit var context: Context
    private val dispatcher = StandardTestDispatcher()
    private val fakeLocalWeatherRepository = FakeLocalWeatherRepositoryImpl(FakeWeatherDao())
    private val fakeLocationRepository = mock<LocationRepository> {
        on { getAllLocations() } doReturn flowOf(listOf(testLocation))
    }
    private val fakePreferenceRepository = mock<PreferenceRepository> {
        on { getSettingsFlow() } doReturn flowOf(AppSettings())
        on { getSettings() } doReturn AppSettings()
    }
    private val fakeDataRefreshRepository = mock<DataRefreshRepository>()
    private val fakeUpdateWeatherUseCase = mock<UpdateWeatherUseCase>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        context = ApplicationProvider.getApplicationContext<WeatherApplication>()
        moonPhase = readMoonPhaseBinFromAssets(context, "moon_phase.bin")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fakeLocalWeatherRepository test`() = runTest {
        val hourlyWeather = fakeLocalWeatherRepository.getHourlyWeather(
            startDate = "2026-02-01",
            endDate = "2026-02-05"
        )
        val hourlyAirQuality = fakeLocalWeatherRepository.getHourlyAirQuality(
            startDate = "2026-02-01",
            endDate = "2026-02-05"
        )
        val dailyWeather = fakeLocalWeatherRepository.getDailyWeather(
            startDate = "2026-02-01",
            endDate = "2026-02-05"
        )
        assertThat(hourlyWeather[testLocation.id]?.size).isEqualTo(5 * 24)
        assertThat(hourlyAirQuality[testLocation.id]?.size).isEqualTo(5 * 24)
        assertThat(dailyWeather[testLocation.id]?.size).isEqualTo(5)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `locationsState locationId test`() = runTest {
        val fixedInstant = Instant.parse("2026-01-02T10:30:00Z")

        val homeViewModel = HomeViewModel(
            fakeLocalWeatherRepository,
            fakeLocationRepository,
            fakePreferenceRepository,
            fakeDataRefreshRepository,
            fakeUpdateWeatherUseCase,
            context,
            dispatcher,
            dispatcher,
            dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC")),
        )
        backgroundScope.launch(dispatcher) {
            homeViewModel.locationsState.collect {
                delay(3000)
                homeViewModel.dateTimeFlowEmissionPermission = false
            }
        }
        assertThat(homeViewModel.locationsState.value.first().locationId).isEqualTo(-2)
        advanceUntilIdle()
        assertThat(homeViewModel.locationsState.value.first().locationId).isEqualTo(testLocation.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `locationsState weather quantities assertion for Cubic and Linear curves`() = runTest {
        val fixedInstant = Instant.parse("2026-01-02T10:30:00Z")

        val homeViewModel = HomeViewModel(
            fakeLocalWeatherRepository,
            fakeLocationRepository,
            fakePreferenceRepository,
            fakeDataRefreshRepository,
            fakeUpdateWeatherUseCase,
            context,
            dispatcher,
            dispatcher,
            dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC")),
        )
        backgroundScope.launch(dispatcher) {
            homeViewModel.locationsState.collect {
                delay(30000)
                homeViewModel.dateTimeFlowEmissionPermission = false
            }
        }
        advanceUntilIdle()
        assertThat(homeViewModel.locationsState.value.first().currentTemperature)
            .isWithin(0.001f).of(6.8750577f) // value calculated by Copilot!
        assertThat(homeViewModel.locationsState.value.first().currentHumidity)
            .isEqualTo(50.5f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `locationsState weather quantities return null when data in not available`() = runTest {
        val fixedInstant = Instant.parse("2026-11-02T10:30:00Z")

        val homeViewModel = HomeViewModel(
            fakeLocalWeatherRepository,
            fakeLocationRepository,
            fakePreferenceRepository,
            fakeDataRefreshRepository,
            fakeUpdateWeatherUseCase,
            context,
            dispatcher,
            dispatcher,
            dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC")),
        )
        backgroundScope.launch(dispatcher) {
            homeViewModel.locationsState.collect {
                delay(30000)
                homeViewModel.dateTimeFlowEmissionPermission = false
            }
        }
        advanceUntilIdle()
        assertThat(homeViewModel.locationsState.value.first().currentTemperature).isNull()
        assertThat(homeViewModel.locationsState.value.first().currentHumidity).isNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `locationsState weather quantities assertion for when first data are null`() = runTest {
        val fixedInstant = Instant.parse("2025-11-10T12:30:00Z")

        val homeViewModel = HomeViewModel(
            fakeLocalWeatherRepository,
            fakeLocationRepository,
            fakePreferenceRepository,
            fakeDataRefreshRepository,
            fakeUpdateWeatherUseCase,
            context,
            dispatcher,
            dispatcher,
            dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC")),
        )
        backgroundScope.launch(dispatcher) {
            homeViewModel.locationsState.collect {
                delay(30000)
                homeViewModel.dateTimeFlowEmissionPermission = false
            }
        }
        advanceUntilIdle()
        assertThat(homeViewModel.locationsState.value.first().currentTemperature)
            .isWithin(0.001f).of(16.9119f) // value calculated by Copilot!
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `locationsState weather quantities assertion when clockGaugeRotation is not zero`() = runTest {
        val fixedInstant = Instant.parse("2026-01-02T10:30:00Z")

        val homeViewModel = HomeViewModel(
            fakeLocalWeatherRepository,
            fakeLocationRepository,
            fakePreferenceRepository,
            fakeDataRefreshRepository,
            fakeUpdateWeatherUseCase,
            context,
            dispatcher,
            dispatcher,
            dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC")),
        )
        backgroundScope.launch(dispatcher) {
            homeViewModel.locationsState.collect {
                delay(30000)
                homeViewModel.dateTimeFlowEmissionPermission = false
            }
        }
        homeViewModel.updateClockGaugeRotation(-180f)
        advanceUntilIdle()
        assertThat(homeViewModel.locationsState.value.first().currentTemperature)
            .isWithin(0.001f).of(3.39006f) // value calculated by Copilot!
        assertThat(homeViewModel.locationsState.value.first().currentHumidity)
            .isEqualTo(77.5f)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `locationsState weather quantities assertion when dayGaugeIndex is not zero`() = runTest {
        val fixedInstant = Instant.parse("2026-01-02T10:30:00Z")

        val homeViewModel = HomeViewModel(
            fakeLocalWeatherRepository,
            fakeLocationRepository,
            fakePreferenceRepository,
            fakeDataRefreshRepository,
            fakeUpdateWeatherUseCase,
            context,
            dispatcher,
            dispatcher,
            dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC")),
        )
        backgroundScope.launch(dispatcher) {
            homeViewModel.locationsState.collect {
                delay(30000)
                homeViewModel.dateTimeFlowEmissionPermission = false
            }
        }
        homeViewModel.updateDayGaugeIndex(1 + homeViewModel.homeState.value.dayGaugeNaturalIndex)
        advanceUntilIdle()
        assertThat(homeViewModel.locationsState.value.first().currentTemperature)
            .isWithin(0.001f).of(4.81977f) // value calculated by Copilot!
        assertThat(homeViewModel.locationsState.value.first().currentHumidity)
            .isEqualTo(70.5f)
    }


}