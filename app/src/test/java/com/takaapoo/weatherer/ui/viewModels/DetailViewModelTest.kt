package com.takaapoo.weatherer.ui.viewModels

import android.content.Context
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.WeatherApplication
import com.takaapoo.weatherer.data.local.FakeWeatherDao
import com.takaapoo.weatherer.data.local.testLocation
import com.takaapoo.weatherer.data.local.testLocation2
import com.takaapoo.weatherer.data.remote.AirQualityApiService
import com.takaapoo.weatherer.data.repository.AirQualityRepositoryImpl
import com.takaapoo.weatherer.data.repository.FakeLocalWeatherRepositoryImpl
import com.takaapoo.weatherer.data.repository.PreferenceRepositoryImpl
import com.takaapoo.weatherer.dataStore
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.domain.repository.LocationRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.domain.use_case.GetHourlyChartDataUseCase
import com.takaapoo.weatherer.domain.use_case.HistoricalDailyChartDataUseCase
import com.takaapoo.weatherer.domain.use_case.HistoricalHourlyChartDataUseCase
import com.takaapoo.weatherer.moonPhase
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.utility.readMoonPhaseBinFromAssets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.coroutines.CoroutineContext

@RunWith(AndroidJUnit4::class)
class DetailViewModelTest {
    lateinit var context: Context
    private val dispatcher = StandardTestDispatcher()
    private val fakeLocalWeatherRepository = FakeLocalWeatherRepositoryImpl(FakeWeatherDao())
    private val fakeAirQualityApiService = mock<AirQualityApiService> {

    }
    private val fakeAirQualityRepository = AirQualityRepositoryImpl(
        FakeWeatherDao(),
        fakeAirQualityApiService
    )
    private val fakeLocationRepository = mock<LocationRepository> {
        on { getAllLocations() } doReturn flowOf(listOf(testLocation, testLocation2))
        on { getAllLocationsList() } doReturn listOf(testLocation, testLocation2)
    }
    private val settingsFlow = MutableStateFlow(AppSettings())
    private val fakePreferenceRepository = mock<PreferenceRepository> {
        on { getSettingsFlow() } doReturn settingsFlow
        on { getSettings() } doAnswer { settingsFlow.value }
    }
    private val fakeHourlyChartDataUseCase = GetHourlyChartDataUseCase(
        locationRepository = fakeLocationRepository,
        airQualityRepository = fakeAirQualityRepository,
        localWeatherRepository = fakeLocalWeatherRepository
    )


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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `detailState test on time 2026-01-02T10-30`() = runTest {
        val fixedInstant = Instant.parse("2026-01-02T10:30:00Z")

        val fakeHistoricalHourlyChartDataUseCase = mock<HistoricalHourlyChartDataUseCase>()
        val fakeHistoricalDailyChartDataUseCase = mock<HistoricalDailyChartDataUseCase>()
        val detailViewModel = DetailViewModel(
            preferenceRepository = fakePreferenceRepository,
            locationRepository = fakeLocationRepository,
            localWeatherRepository = fakeLocalWeatherRepository,
            hourlyChartDataUseCase = fakeHourlyChartDataUseCase,
            historicalHourlyChartDataUseCase = fakeHistoricalHourlyChartDataUseCase,
            historicalDailyChartDataUseCase = fakeHistoricalDailyChartDataUseCase,
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher,
            defaultDispatcher = dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC")),
        )

        backgroundScope.launch(dispatcher) {
            detailViewModel.detailState.collect {
                delay(3000)
                detailViewModel.detailStateUpdatePermission = false
            }
        }
        assertThat(detailViewModel.detailState.value.size).isEqualTo(0)
        advanceUntilIdle()
        assertThat(detailViewModel.detailState.value.size).isEqualTo(1)
        assertThat(detailViewModel.detailState.value.first().targetX).isEqualTo(10.5f)
        assertThat(detailViewModel.detailState.value.first().sunRise).isEqualTo("2026-01-02T07:14")
        assertThat(detailViewModel.detailState.value.first().maxTemperature).isEqualTo(7.9f)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `detailState test on Settings change`() = runTest {
        val fixedInstant = Instant.parse("2026-01-02T10:30:00Z")

        val fakeHistoricalHourlyChartDataUseCase = mock<HistoricalHourlyChartDataUseCase>()
        val fakeHistoricalDailyChartDataUseCase = mock<HistoricalDailyChartDataUseCase>()
        val detailViewModel = DetailViewModel(
            preferenceRepository = fakePreferenceRepository,
            locationRepository = fakeLocationRepository,
            localWeatherRepository = fakeLocalWeatherRepository,
            hourlyChartDataUseCase = fakeHourlyChartDataUseCase,
            historicalHourlyChartDataUseCase = fakeHistoricalHourlyChartDataUseCase,
            historicalDailyChartDataUseCase = fakeHistoricalDailyChartDataUseCase,
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher,
            defaultDispatcher = dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC")),
        )

        backgroundScope.launch(dispatcher) {
            detailViewModel.detailState.collect {
                delay(3000)
                detailViewModel.detailStateUpdatePermission = false
            }
        }
        assertThat(detailViewModel.detailState.value.size).isEqualTo(0)
        advanceUntilIdle()
        assertThat(detailViewModel.detailState.value.first().maxTemperature).isEqualTo(7.9f)
        settingsFlow.update {
            it.copy(temperatureUnit = Temperature.FAHRENHEIT)
        }
        advanceUntilIdle()
        assertThat(detailViewModel.detailState.value.first().maxTemperature).isEqualTo(46.22f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `setSliderThumbPositions test on UTC`() = runTest {
        val fixedInstant = Instant.parse("2026-01-02T10:30:00Z")
        settingsFlow.update {
            it.copy(hourlyDiagramTime = HourlyDiagramTime.UTC)
        }
        val fakeHistoricalHourlyChartDataUseCase = mock<HistoricalHourlyChartDataUseCase>()
        val fakeHistoricalDailyChartDataUseCase = mock<HistoricalDailyChartDataUseCase>()
        val detailViewModel = DetailViewModel(
            preferenceRepository = fakePreferenceRepository,
            locationRepository = fakeLocationRepository,
            localWeatherRepository = fakeLocalWeatherRepository,
            hourlyChartDataUseCase = fakeHourlyChartDataUseCase,
            historicalHourlyChartDataUseCase = fakeHistoricalHourlyChartDataUseCase,
            historicalDailyChartDataUseCase = fakeHistoricalDailyChartDataUseCase,
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher,
            defaultDispatcher = dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC")),
        )
        detailViewModel.initializeSliderThumbPositions(HourlyDiagramTime.UTC)

        backgroundScope.launch(dispatcher) {
            detailViewModel.detailState.collect {
                delay(3000)
                detailViewModel.detailStateUpdatePermission = false
            }
        }
        assertThat(detailViewModel.hourlyChartState.size).isEqualTo(0)
        advanceUntilIdle()
        val nowX = 10.5f
        val expectedSliderPosition = (nowX - detailViewModel.initialXAxisStart) /
                (detailViewModel.initialXAxisEnd - detailViewModel.initialXAxisStart)
        assertThat(detailViewModel.hourlyChartState.first().value.sliderThumbPosition.value)
            .isEqualTo(expectedSliderPosition)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addChartQuantity test`() = runTest {
        val fixedInstant = Instant.parse("2026-01-02T10:30:00Z")

        val fakeHistoricalHourlyChartDataUseCase = mock<HistoricalHourlyChartDataUseCase>()
        val fakeHistoricalDailyChartDataUseCase = mock<HistoricalDailyChartDataUseCase>()
        val detailViewModel = DetailViewModel(
            preferenceRepository = fakePreferenceRepository,
            locationRepository = fakeLocationRepository,
            localWeatherRepository = fakeLocalWeatherRepository,
            hourlyChartDataUseCase = fakeHourlyChartDataUseCase,
            historicalHourlyChartDataUseCase = fakeHistoricalHourlyChartDataUseCase,
            historicalDailyChartDataUseCase = fakeHistoricalDailyChartDataUseCase,
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher,
            defaultDispatcher = dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC")),
        )

        backgroundScope.launch(dispatcher) {
            detailViewModel.detailState.collect {
                delay(3000)
                detailViewModel.detailStateUpdatePermission = false
            }
        }
        assertThat(detailViewModel.hourlyChartState.size).isEqualTo(0)
        advanceUntilIdle()
        assertThat(detailViewModel.hourlyChartState.first().value.chartQuantities)
            .isEqualTo(listOf(HourlyWeatherQuantity.TEMPERATURE))
        assertThat(detailViewModel.hourlyChartState.first().value.curveValueAtIndicator.size).isEqualTo(1)

        detailViewModel.addChartQuantity(HourlyWeatherQuantity.HUMIDITY, 0, false)

        assertThat(detailViewModel.hourlyChartState.first().value.chartQuantities)
            .isEqualTo(listOf(HourlyWeatherQuantity.TEMPERATURE, HourlyWeatherQuantity.HUMIDITY))
        assertThat(detailViewModel.hourlyChartState.first().value.curveValueAtIndicator.size).isEqualTo(2)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `removeChartQuantity test`() = runTest {
        val fixedInstant = Instant.parse("2026-01-02T10:30:00Z")

        val fakeHistoricalHourlyChartDataUseCase = mock<HistoricalHourlyChartDataUseCase>()
        val fakeHistoricalDailyChartDataUseCase = mock<HistoricalDailyChartDataUseCase>()
        val detailViewModel = DetailViewModel(
            preferenceRepository = fakePreferenceRepository,
            locationRepository = fakeLocationRepository,
            localWeatherRepository = fakeLocalWeatherRepository,
            hourlyChartDataUseCase = fakeHourlyChartDataUseCase,
            historicalHourlyChartDataUseCase = fakeHistoricalHourlyChartDataUseCase,
            historicalDailyChartDataUseCase = fakeHistoricalDailyChartDataUseCase,
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher,
            defaultDispatcher = dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC")),
        )

        backgroundScope.launch(dispatcher) {
            detailViewModel.detailState.collect {
                delay(3000)
                detailViewModel.detailStateUpdatePermission = false
            }
        }
        advanceUntilIdle()
        assertThat(detailViewModel.hourlyChartState.first().value.chartQuantities)
            .isEqualTo(listOf(HourlyWeatherQuantity.TEMPERATURE))
        assertThat(detailViewModel.hourlyChartState.first().value.curveValueAtIndicator.size).isEqualTo(1)

        detailViewModel.addChartQuantity(HourlyWeatherQuantity.HUMIDITY, 0, false)
        advanceUntilIdle()
        detailViewModel.removeChartQuantity(HourlyWeatherQuantity.TEMPERATURE, 0, false)

        assertThat(detailViewModel.hourlyChartState.first().value.chartQuantities)
            .isEqualTo(listOf(HourlyWeatherQuantity.HUMIDITY))
        assertThat(detailViewModel.hourlyChartState.first().value.curveValueAtIndicator.size).isEqualTo(1)
    }

    private val testFrameClock = object : MonotonicFrameClock {
        override val key: CoroutineContext.Key<*>
            get() = MonotonicFrameClock

        override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
            return onFrame(System.nanoTime())
        }
    }
    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
    @Test
    fun `animateHourlyDiagramAxis test`() = runTest {
        val fixedInstant = Instant.parse("2026-01-01T10:30:00Z")
        val utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))

        val fakeHistoricalHourlyChartDataUseCase = mock<HistoricalHourlyChartDataUseCase>()
        val fakeHistoricalDailyChartDataUseCase = mock<HistoricalDailyChartDataUseCase>()
        val detailViewModel = DetailViewModel(
            preferenceRepository = fakePreferenceRepository,
            locationRepository = fakeLocationRepository,
            localWeatherRepository = fakeLocalWeatherRepository,
            hourlyChartDataUseCase = fakeHourlyChartDataUseCase,
            historicalHourlyChartDataUseCase = fakeHistoricalHourlyChartDataUseCase,
            historicalDailyChartDataUseCase = fakeHistoricalDailyChartDataUseCase,
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher,
            defaultDispatcher = dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = utcClock,
        )

        backgroundScope.launch(dispatcher) {
            detailViewModel.detailState.collect {
                delay(3000)
                detailViewModel.detailStateUpdatePermission = false
            }
        }
        advanceUntilIdle()
        withContext(testFrameClock) {
            val preferredDate = LocalDate.of(2020, 1, 1)
            detailViewModel.animateHourlyDiagramAxis(preferredDate, 0)
            val newXAxisCenter = Duration.between(
                LocalDate.now(utcClock).atStartOfDay(),
                preferredDate.atStartOfDay()
            ).toHours()
            val xAxisLength = detailViewModel.historicalHourlyChartState.first().value.let {
                it.xAxisEnd.value - it.xAxisStart.value
            }
            advanceUntilIdle()
            assertThat(detailViewModel.historicalHourlyChartState.first().value.xAxisStart.value)
                .isEqualTo(newXAxisCenter - xAxisLength/2)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
    @Test
    fun `moveDailyDiagramAxis test`() = runTest {
        val fixedInstant = Instant.parse("2026-01-01T10:30:00Z")
        val utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))

        val fakeHistoricalHourlyChartDataUseCase = mock<HistoricalHourlyChartDataUseCase>()
        val fakeHistoricalDailyChartDataUseCase = mock<HistoricalDailyChartDataUseCase>()
        val detailViewModel = DetailViewModel(
            preferenceRepository = fakePreferenceRepository,
            locationRepository = fakeLocationRepository,
            localWeatherRepository = fakeLocalWeatherRepository,
            hourlyChartDataUseCase = fakeHourlyChartDataUseCase,
            historicalHourlyChartDataUseCase = fakeHistoricalHourlyChartDataUseCase,
            historicalDailyChartDataUseCase = fakeHistoricalDailyChartDataUseCase,
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher,
            defaultDispatcher = dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = utcClock,
        )

        backgroundScope.launch(dispatcher) {
            detailViewModel.detailState.collect {
                delay(3000)
                detailViewModel.detailStateUpdatePermission = false
            }
        }
        advanceUntilIdle()
        val offset = Offset(0.2f, 0.5f)
        val xAxisLength =
            detailViewModel.dailyChartState.first().value.let { it.xAxisEnd.value - it.xAxisStart.value }
        val xAxisMovement = offset.x * xAxisLength
        val initialXAxisStart = detailViewModel.dailyChartState.first().value.xAxisStart.value
        detailViewModel.moveDailyDiagramAxis(offset, 0)

        advanceUntilIdle()
        assertThat(detailViewModel.dailyChartState.first().value.xAxisStart.value)
            .isEqualTo( initialXAxisStart - xAxisMovement)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `updateHourlyChartWeatherIconVisibility test`() = runTest {
        val fixedInstant = Instant.parse("2026-01-01T10:30:00Z")
        val utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))

        val fakeHistoricalHourlyChartDataUseCase = mock<HistoricalHourlyChartDataUseCase>()
        val fakeHistoricalDailyChartDataUseCase = mock<HistoricalDailyChartDataUseCase>()
        val detailViewModel = DetailViewModel(
            preferenceRepository = PreferenceRepositoryImpl(context),
            locationRepository = fakeLocationRepository,
            localWeatherRepository = fakeLocalWeatherRepository,
            hourlyChartDataUseCase = fakeHourlyChartDataUseCase,
            historicalHourlyChartDataUseCase = fakeHistoricalHourlyChartDataUseCase,
            historicalDailyChartDataUseCase = fakeHistoricalDailyChartDataUseCase,
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher,
            defaultDispatcher = dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = utcClock,
        )

        backgroundScope.launch(dispatcher) {
            detailViewModel.detailState.collect {
                delay(3000)
                detailViewModel.detailStateUpdatePermission = false
            }
        }
        advanceUntilIdle()
        detailViewModel.updateHourlyChartWeatherIconVisibility(true)
        advanceUntilIdle()
        val dataList = context.dataStore.data.take(2).toList()
        assertThat(dataList[0].hourlyDiagramWeatherConditionIconVisible).isEqualTo( false)
        assertThat(dataList[1].hourlyDiagramWeatherConditionIconVisible).isEqualTo( true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `swapLocations test`() = runTest {
        val fixedInstant = Instant.parse("2026-01-01T10:30:00Z")
        val utcClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))

        val fakeHistoricalHourlyChartDataUseCase = mock<HistoricalHourlyChartDataUseCase>()
        val fakeHistoricalDailyChartDataUseCase = mock<HistoricalDailyChartDataUseCase>()
        val detailViewModel = DetailViewModel(
            preferenceRepository = PreferenceRepositoryImpl(context),
            locationRepository = fakeLocationRepository,
            localWeatherRepository = fakeLocalWeatherRepository,
            hourlyChartDataUseCase = fakeHourlyChartDataUseCase,
            historicalHourlyChartDataUseCase = fakeHistoricalHourlyChartDataUseCase,
            historicalDailyChartDataUseCase = fakeHistoricalDailyChartDataUseCase,
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher,
            defaultDispatcher = dispatcher,
            systemClock = Clock.fixed(fixedInstant, ZoneId.systemDefault()),
            utcClock = utcClock,
        )

        backgroundScope.launch(dispatcher) {
            detailViewModel.detailState.collect {
                delay(3000)
                detailViewModel.detailStateUpdatePermission = false
            }
        }
        advanceUntilIdle()
        assertThat(detailViewModel.allLocations).isEqualTo(listOf(testLocation, testLocation2))
        detailViewModel.swapLocations(testLocation.id, testLocation2.id)
        assertThat(detailViewModel.allLocations).isEqualTo(listOf(testLocation2, testLocation))
        assertThat(detailViewModel.detailState.value.map { it.locationName })
            .isEqualTo(listOf(testLocation2.name, testLocation.name))
    }
}