package com.takaapoo.weatherer.domain.use_case

import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.data.local.CsvParser
import com.takaapoo.weatherer.data.local.LocalHistoricHourlyWeather
import com.takaapoo.weatherer.data.local.parseCsvToLocalHourlyWeather
import com.takaapoo.weatherer.data.local.testLocation
import com.takaapoo.weatherer.data.remote.HistoricHourlyWeather
import com.takaapoo.weatherer.data.remote.HistoricHourlyWeatherPack
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HistoricHourlyChartDto
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.RemoteWeatherRepository
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.utility.timeIsBetween
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class HistoricalHourlyChartDataUseCaseTest {
    private fun loadCsvFromResources(fileName: String): List<List<String>> {
        // Load file from test/resources
        val inputStream: InputStream = javaClass.classLoader
            ?.getResourceAsStream(fileName)
            ?: throw IllegalArgumentException("File not found: $fileName")

        // Parse CSV (using manual parser or library)
        return CsvParser.parseCsv(inputStream, hasHeader = true)
    }

    private val dispatcher = StandardTestDispatcher()

    private val csvData = loadCsvFromResources("hourly_weather.csv")
    private val hourlyWeatherData = parseCsvToLocalHourlyWeather(csvData).filter {
        it.time.timeIsBetween("2025-12-01", "2025-12-05")
    }

    private val historicHourlyData = hourlyWeatherData.map {
        LocalHistoricHourlyWeather(
            locationId = testLocation.id,
            time = it.time,
            weatherCode = it.weatherCode,
            temperature = it.temperature
        )
    }

    private val historicHourlyWeatherPack = HistoricHourlyWeatherPack(
        latitude = testLocation.latitude,
        longitude = testLocation.longitude,
        utcOffsetSeconds = testLocation.utcOffset?.toInt() ?: 0,
        timeZone = "",
        timeZoneAbbreviation = "",
        elevation = testLocation.elevation,
        hourlyWeather = HistoricHourlyWeather(
            time = hourlyWeatherData.map { it.time },
            weatherCode = hourlyWeatherData.map { it.weatherCode },
            temperature = hourlyWeatherData.map { it.temperature }
        )
    )
    private val historicHourlyWeatherPack2 = HistoricHourlyWeatherPack(
        latitude = testLocation.latitude,
        longitude = testLocation.longitude,
        utcOffsetSeconds = testLocation.utcOffset?.toInt() ?: 0,
        timeZone = "",
        timeZoneAbbreviation = "",
        elevation = testLocation.elevation,
        hourlyWeather = HistoricHourlyWeather(
            time = hourlyWeatherData.map { it.time },
            weatherCode = hourlyWeatherData.map { it.weatherCode },
            temperature = hourlyWeatherData.map { it.temperature },
            humidity = hourlyWeatherData.map { it.humidity }
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
            on { getLocationHistoricHourlyWeather(any(), any(), any()) } doReturn historicHourlyData
        }
        val fakeRemoteWeatherRepository = mock<RemoteWeatherRepository> {}
        val historicalHourlyChartDataUseCase = HistoricalHourlyChartDataUseCase(
            fakeLocalWeatherRepository,
            fakeRemoteWeatherRepository,
            dispatcher
        )
        val xAxisStart = ChronoUnit.HOURS.between(
            LocalDate.now().atStartOfDay(),
            LocalDateTime.parse(historicHourlyData.first().time)
        )
        val xAxisEnd = ChronoUnit.HOURS.between(
            LocalDate.now().plusDays(1).atStartOfDay(),
            LocalDateTime.parse(historicHourlyData.last().time)
        )

        historicalHourlyChartDataUseCase(
            testLocation,
            xAxisStart.toFloat(),
            xAxisEnd.toFloat(),
            listOf(HourlyWeatherQuantity.TEMPERATURE),
            AppSettings()
        ){
            assertThat(it).isInstanceOf(MyResult.Success::class.java)
            assertThat(((it as MyResult.Success).data as List<HistoricHourlyChartDto>).first().hourlyWeather?.temperature)
                .isEqualTo(5.7f)
        }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke when data does not exist`() = runTest {
        val fakeLocalWeatherRepository = mock<LocalWeatherRepository> {
            on { getLocationHistoricHourlyWeather(any(), any(), any()) } doReturn emptyList()
        }
        val fakeRemoteWeatherRepository = mock<RemoteWeatherRepository> {
            on { getHistoricHourlyWeatherFromServer(
                any(),
                any(),
                any(),
                any(),
                any()
            ) } doReturn MyResult.Success(historicHourlyWeatherPack)
        }
        val historicalHourlyChartDataUseCase = HistoricalHourlyChartDataUseCase(
            fakeLocalWeatherRepository,
            fakeRemoteWeatherRepository,
            dispatcher
        )
        val xAxisStart = -100
        val xAxisEnd = -90

        val results = mutableListOf<MyResult<*>>()
        historicalHourlyChartDataUseCase(
            testLocation,
            xAxisStart.toFloat(),
            xAxisEnd.toFloat(),
            listOf(HourlyWeatherQuantity.TEMPERATURE),
            AppSettings()
        ){
            results.add(it)
        }
        assertThat(results.first()).isInstanceOf(MyResult.Loading::class.java)
        assertThat(results.last()).isInstanceOf(MyResult.Success::class.java)
        assertThat(((results.last() as MyResult.Success).data as List<HistoricHourlyChartDto>).first().hourlyWeather?.temperature)
            .isEqualTo(5.7f)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke when weather quantity does not exist`() = runTest {
        val fakeLocalWeatherRepository = mock<LocalWeatherRepository> {
            on { getLocationHistoricHourlyWeather(any(), any(), any()) } doReturn historicHourlyData
        }
        val fakeRemoteWeatherRepository = mock<RemoteWeatherRepository> {
            on { getHistoricHourlyWeatherFromServer(
                any(),
                any(),
                any(),
                any(),
                any()
            ) } doReturn MyResult.Success(historicHourlyWeatherPack2)
        }
        val historicalHourlyChartDataUseCase = HistoricalHourlyChartDataUseCase(
            fakeLocalWeatherRepository,
            fakeRemoteWeatherRepository,
            dispatcher
        )
        val xAxisStart = -100
        val xAxisEnd = -90

        val results = mutableListOf<MyResult<*>>()
        historicalHourlyChartDataUseCase(
            testLocation,
            xAxisStart.toFloat(),
            xAxisEnd.toFloat(),
            listOf(HourlyWeatherQuantity.HUMIDITY),
            AppSettings()
        ){
            results.add(it)
        }
        assertThat(results.first()).isInstanceOf(MyResult.Loading::class.java)
        assertThat(results.last()).isInstanceOf(MyResult.Success::class.java)
        assertThat(((results.last() as MyResult.Success).data as List<HistoricHourlyChartDto>).first().hourlyWeather?.humidity)
            .isEqualTo(39f)
    }
}