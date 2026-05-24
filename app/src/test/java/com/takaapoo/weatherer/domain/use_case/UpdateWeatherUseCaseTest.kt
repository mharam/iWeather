package com.takaapoo.weatherer.domain.use_case

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.WeatherApplication
import com.takaapoo.weatherer.data.local.testLocation
import com.takaapoo.weatherer.data.remote.AirQuality
import com.takaapoo.weatherer.data.remote.DailyWeather
import com.takaapoo.weatherer.data.remote.DailyWeatherPack
import com.takaapoo.weatherer.data.remote.HourlyWeather
import com.takaapoo.weatherer.data.remote.HourlyWeatherPack
import com.takaapoo.weatherer.data.remote.RemoteLocationType
import com.takaapoo.weatherer.data.remote.WholeAirQuality
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.AirQualityRepository
import com.takaapoo.weatherer.domain.repository.LandScapeRepository
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.LocationRepository
import com.takaapoo.weatherer.domain.repository.RemoteWeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class UpdateWeatherUseCaseTest {

    lateinit var context: Context
    private val dispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<WeatherApplication>()
        Dispatchers.setMain(dispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke when setLocationTime return Error result`() = runTest {
        val fakeRemoteWeatherRepository = mock<RemoteWeatherRepository> {
            on { getHourlyWeatherFromServer(any(), any(), any()) } doReturn
                    MyResult.Success(
                        HourlyWeatherPack(
                            latitude = testLocation.latitude,
                            longitude = testLocation.longitude,
                            utcOffsetSeconds = testLocation.utcOffset?.toInt() ?: 0,
                            timeZone = "",
                            timeZoneAbbreviation = "",
                            elevation = testLocation.elevation,
                            hourlyWeather = HourlyWeather()
                        )
                    )
            on { getDailyWeatherFromServer(any(), any(), any()) } doReturn
                    MyResult.Success(
                        DailyWeatherPack(
                            latitude = testLocation.latitude,
                            longitude = testLocation.longitude,
                            utcOffsetSeconds = testLocation.utcOffset?.toInt() ?: 0,
                            timeZone = "",
                            timeZoneAbbreviation = "",
                            elevation = testLocation.elevation,
                            dailyWeather = DailyWeather()
                        )
                    )
            on { getYearlyWeatherFromServer(any(), any(), any()) } doReturn
                    MyResult.Success(listOf(testLocation.yearlyPrecipitation))
        }
        val fakeAirQualityRepository = mock<AirQualityRepository> {
            on { getAirQualityFromServer(any(), any(), any()) } doReturn
                    MyResult.Success(
                        WholeAirQuality(
                            latitude = testLocation.latitude,
                            longitude = testLocation.longitude,
                            utcOffsetSeconds = testLocation.utcOffset?.toInt() ?: 0,
                            timeZone = "",
                            timeZoneAbbreviation = "",
                            elevation = testLocation.elevation,
                            airQuality = AirQuality()
                        )
                    )
        }
        val fakeLocationRepository = mock<LocationRepository> {
            on { getLocationTypeFromServer(any(), any()) } doReturn
                    MyResult.Success(
                        RemoteLocationType(
                            isWater = false,
                            feature = testLocation.locationType.name,
                            latitude = testLocation.latitude,
                            longitude = testLocation.longitude
                        )
                    )
            on { countLocationWithName(any()) } doReturn 0
        }
        val fakeAddLocationUseCase = mock<AddLocationUseCase> {
            on { setLocationTime(any()) } doReturn MyResult.Error(message = "Failed to get time!")
        }
        val fakeLandScapeRepository = mock<LandScapeRepository> {}
        val fakeLocalWeatherRepository = mock<LocalWeatherRepository>()
        val updateWeatherUseCase = UpdateWeatherUseCase(
            context,
            remoteWeatherRepository = fakeRemoteWeatherRepository,
            airQualityRepository = fakeAirQualityRepository,
            localWeatherRepository = fakeLocalWeatherRepository,
            locationRepository = fakeLocationRepository,
            landScapeRepository = fakeLandScapeRepository,
            addLocationUseCase = fakeAddLocationUseCase,
            ioDispatcher = dispatcher,
            defaultDispatcher = dispatcher,
        )
        val realResult = updateWeatherUseCase(testLocation)
        advanceUntilIdle()
        assertThat(realResult).isInstanceOf(MyResult.Error::class.java)
        assertThat((realResult as MyResult.Error).exception?.message).isEqualTo("Failed to get time!")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke when setLocationTime do not return result in 60 seconds`() = runTest {
        val fakeRemoteWeatherRepository = mock<RemoteWeatherRepository> {
            on { getHourlyWeatherFromServer(any(), any(), any()) } doReturn
                    MyResult.Success(
                        HourlyWeatherPack(
                            latitude = testLocation.latitude,
                            longitude = testLocation.longitude,
                            utcOffsetSeconds = testLocation.utcOffset?.toInt() ?: 0,
                            timeZone = "",
                            timeZoneAbbreviation = "",
                            elevation = testLocation.elevation,
                            hourlyWeather = HourlyWeather()
                        )
                    )
            on { getDailyWeatherFromServer(any(), any(), any()) } doReturn
                    MyResult.Success(
                        DailyWeatherPack(
                            latitude = testLocation.latitude,
                            longitude = testLocation.longitude,
                            utcOffsetSeconds = testLocation.utcOffset?.toInt() ?: 0,
                            timeZone = "",
                            timeZoneAbbreviation = "",
                            elevation = testLocation.elevation,
                            dailyWeather = DailyWeather()
                        )
                    )
            on { getYearlyWeatherFromServer(any(), any(), any()) } doReturn
                    MyResult.Success(listOf(testLocation.yearlyPrecipitation))
        }
        val fakeAirQualityRepository = mock<AirQualityRepository> {
            on { getAirQualityFromServer(any(), any(), any()) } doReturn
                    MyResult.Success(
                        WholeAirQuality(
                            latitude = testLocation.latitude,
                            longitude = testLocation.longitude,
                            utcOffsetSeconds = testLocation.utcOffset?.toInt() ?: 0,
                            timeZone = "",
                            timeZoneAbbreviation = "",
                            elevation = testLocation.elevation,
                            airQuality = AirQuality()
                        )
                    )
        }
        val fakeLocationRepository = mock<LocationRepository> {
            on { getLocationTypeFromServer(any(), any()) } doReturn
                    MyResult.Success(
                        RemoteLocationType(
                            isWater = false,
                            feature = testLocation.locationType.name,
                            latitude = testLocation.latitude,
                            longitude = testLocation.longitude
                        )
                    )
            on { countLocationWithName(any()) } doReturn 0
        }
        val fakeAddLocationUseCase = mock<AddLocationUseCase> {
            on { setLocationTime(any()) } doSuspendableAnswer {
                delay(100_000)
                MyResult.Success("Good!")
            }
        }
        val fakeLandScapeRepository = mock<LandScapeRepository> {}
        val fakeLocalWeatherRepository = mock<LocalWeatherRepository>()
        val updateWeatherUseCase = UpdateWeatherUseCase(
            context,
            remoteWeatherRepository = fakeRemoteWeatherRepository,
            airQualityRepository = fakeAirQualityRepository,
            localWeatherRepository = fakeLocalWeatherRepository,
            locationRepository = fakeLocationRepository,
            landScapeRepository = fakeLandScapeRepository,
            addLocationUseCase = fakeAddLocationUseCase,
            ioDispatcher = dispatcher,
            defaultDispatcher = dispatcher,
        )
        val realResult = updateWeatherUseCase(testLocation)
        advanceTimeBy(61_000)
        assertThat(realResult).isInstanceOf(MyResult.Error::class.java)
        assertThat((realResult as MyResult.Error).exception?.message).isEqualTo("Timeout")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke when it returns Success`() = runTest {
        val fakeRemoteWeatherRepository = mock<RemoteWeatherRepository> {
            on { getHourlyWeatherFromServer(any(), any(), any()) } doReturn
                    MyResult.Success(
                        HourlyWeatherPack(
                            latitude = testLocation.latitude,
                            longitude = testLocation.longitude,
                            utcOffsetSeconds = testLocation.utcOffset?.toInt() ?: 0,
                            timeZone = "",
                            timeZoneAbbreviation = "",
                            elevation = testLocation.elevation,
                            hourlyWeather = HourlyWeather()
                        )
                    )
            on { getDailyWeatherFromServer(any(), any(), any()) } doReturn
                    MyResult.Success(
                        DailyWeatherPack(
                            latitude = testLocation.latitude,
                            longitude = testLocation.longitude,
                            utcOffsetSeconds = testLocation.utcOffset?.toInt() ?: 0,
                            timeZone = "",
                            timeZoneAbbreviation = "",
                            elevation = testLocation.elevation,
                            dailyWeather = DailyWeather()
                        )
                    )
            on { getYearlyWeatherFromServer(any(), any(), any()) } doReturn
                    MyResult.Success(listOf(testLocation.yearlyPrecipitation))
        }
        val fakeAirQualityRepository = mock<AirQualityRepository> {
            on { getAirQualityFromServer(any(), any(), any()) } doReturn
                    MyResult.Success(
                        WholeAirQuality(
                            latitude = testLocation.latitude,
                            longitude = testLocation.longitude,
                            utcOffsetSeconds = testLocation.utcOffset?.toInt() ?: 0,
                            timeZone = "",
                            timeZoneAbbreviation = "",
                            elevation = testLocation.elevation,
                            airQuality = AirQuality()
                        )
                    )
        }
        val fakeLocationRepository = mock<LocationRepository> {
            on { getLocationTypeFromServer(any(), any()) } doReturn
                    MyResult.Success(
                        RemoteLocationType(
                            isWater = false,
                            feature = testLocation.locationType.name,
                            latitude = testLocation.latitude,
                            longitude = testLocation.longitude
                        )
                    )
            on { countLocationWithName(any()) } doReturn 0
        }
        val fakeAddLocationUseCase = mock<AddLocationUseCase> {
            on { setLocationTime(any()) } doReturn MyResult.Success("Ok!")
        }
        val fakeLandScapeRepository = mock<LandScapeRepository> {}
        val fakeLocalWeatherRepository = mock<LocalWeatherRepository>()
        val updateWeatherUseCase = UpdateWeatherUseCase(
            context,
            remoteWeatherRepository = fakeRemoteWeatherRepository,
            airQualityRepository = fakeAirQualityRepository,
            localWeatherRepository = fakeLocalWeatherRepository,
            locationRepository = fakeLocationRepository,
            landScapeRepository = fakeLandScapeRepository,
            addLocationUseCase = fakeAddLocationUseCase,
            ioDispatcher = dispatcher,
            defaultDispatcher = dispatcher,
        )
        val realResult = updateWeatherUseCase(testLocation)
        advanceUntilIdle()
        assertThat(realResult).isInstanceOf(MyResult.Success::class.java)
    }

}