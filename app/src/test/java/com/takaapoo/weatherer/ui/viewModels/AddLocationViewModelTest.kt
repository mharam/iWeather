package com.takaapoo.weatherer.ui.viewModels

import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.remote.Geometry
import com.takaapoo.weatherer.data.remote.LocationServerOutput
import com.takaapoo.weatherer.data.remote.Properties
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.LocationRepository
import com.takaapoo.weatherer.domain.repository.RemoteWeatherRepository
import com.takaapoo.weatherer.domain.use_case.AddLocationUseCase
import com.takaapoo.weatherer.domain.use_case.UpdateWeatherUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@RunWith(MockitoJUnitRunner::class)
class AddLocationViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val testLocation = Location(id = 1, customId = 1, name = "Tehran")
    // Fake dependencies
    private val fakeUpdateWeatherUseCase = mock<UpdateWeatherUseCase>()
    private val fakeAddLocationUseCase = mock<AddLocationUseCase>()
    private val fakeLocationRepository = mock<LocationRepository> {
        on { locationsCount() } doReturn flowOf(0)
    }
    private val fakeRemoteWeatherRepository = mock<RemoteWeatherRepository>()

    private val viewModel = AddLocationViewModel(
        fakeUpdateWeatherUseCase,
        fakeAddLocationUseCase,
        fakeLocationRepository,
        fakeRemoteWeatherRepository,
        dispatcher
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `resetAddResult causes addResult parameter of mapState to become Loading`() {
        viewModel.resetAddResult()
        assertThat(viewModel.mapState.value.addResult).isInstanceOf(MyResult.Loading::class.java)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addLocationToDB should update mapState addResult with success`() = runTest {
        `when`(fakeAddLocationUseCase.invoke(any()))
            .thenReturn(MyResult.Success(1))
        `when`(fakeUpdateWeatherUseCase.invoke(any()))
            .thenReturn(MyResult.Success(""))

        viewModel.addLocationToDB(testLocation)
        advanceUntilIdle()

        assertThat(viewModel.mapState.value.addResult).isInstanceOf(MyResult.Success::class.java)
        assertThat((viewModel.mapState.value.addResult as MyResult.Success).data)
            .isEqualTo("Tehran added successfully")
        assertThat(viewModel.mapState.value.weatherDataLoadStatus[testLocation.id])
            .isInstanceOf(MyResult.Success::class.java)

    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addLocationToDB should update mapState addResult with error`() = runTest {
        `when`(fakeAddLocationUseCase.invoke(any()))
            .thenReturn(MyResult.Error(message = "${testLocation.name} already exists"))
        `when`(fakeUpdateWeatherUseCase.invoke(any()))
            .thenReturn(MyResult.Success(""))

        viewModel.addLocationToDB(testLocation)
        advanceUntilIdle()

        assertThat(viewModel.mapState.value.addResult).isInstanceOf(MyResult.Error::class.java)
        assertThat((viewModel.mapState.value.addResult as MyResult.Error).message)
            .isEqualTo("${testLocation.name} already exists")
        assertThat(viewModel.mapState.value.weatherDataLoadStatus).isEmpty()
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addLocationToDB should update mapState weatherDataLoadStatus with error`() = runTest {
        `when`(fakeAddLocationUseCase.invoke(any()))
            .thenReturn(MyResult.Success(1))
        `when`(fakeUpdateWeatherUseCase.invoke(any()))
            .thenReturn(MyResult.Error(exception = Exception("Data not available!")))

        viewModel.addLocationToDB(testLocation)
        advanceUntilIdle()

        assertThat(viewModel.mapState.value.addResult).isInstanceOf(MyResult.Success::class.java)
        assertThat((viewModel.mapState.value.addResult as MyResult.Success).data)
            .isEqualTo("Tehran added successfully")
        assertThat(viewModel.mapState.value.weatherDataLoadStatus[testLocation.id])
            .isInstanceOf(MyResult.Error::class.java)
        assertThat((viewModel.mapState.value.weatherDataLoadStatus[testLocation.id] as MyResult.Error).exception?.message)
            .isEqualTo("Data not available!")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `showIndicatingCircle should change indicatingCircleVisible true then false`() = runTest {
        viewModel.showIndicatingCircle()
        advanceTimeBy(1000)
        assertThat(viewModel.mapState.value.indicatingCircleVisible).isTrue()
        advanceTimeBy(7000)
        assertThat(viewModel.mapState.value.indicatingCircleVisible).isFalse()
    }
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `updateSearchQuery with getLocation return Success result`() = runTest {
        val testRemoteLocation = com.takaapoo.weatherer.data.remote.Location(
            geometry = Geometry(doubleArrayOf(1.0, 2.0)),
            properties = Properties(formatted = "Boorboor"),
        )
        `when`(fakeAddLocationUseCase.getLocation(any()))
            .thenReturn(MyResult.Success(
                LocationServerOutput(features = listOf(testRemoteLocation))
            ))
        viewModel.updateSearchQuery("Teh")
        assertThat(viewModel.mapState.value.searchQuery).isEqualTo("Teh")
        advanceUntilIdle()
        assertThat(viewModel.mapState.value.locations).isEqualTo(listOf(testRemoteLocation))
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `updateSearchQuery with getLocation return error result`() = runTest {
        val testRemoteLocation = com.takaapoo.weatherer.data.remote.Location(
            geometry = Geometry(doubleArrayOf(1.0, 2.0)),
            properties = Properties(formatted = "Boorboor"),
        )
        `when`(fakeAddLocationUseCase.getLocation(any()))
            .thenReturn(MyResult.Error(message = "No location found!"))
        viewModel.updateSearchQuery("Teh")
        assertThat(viewModel.mapState.value.searchQuery).isEqualTo("Teh")
        advanceUntilIdle()
        assertThat(viewModel.mapState.value.locations).isEmpty()

    }
}