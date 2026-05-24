package com.takaapoo.weatherer.data.repository

import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.data.remote.LandScape
import com.takaapoo.weatherer.data.remote.LandScapeApiService
import com.takaapoo.weatherer.domain.MyResult
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
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import retrofit2.Response

@RunWith(MockitoJUnitRunner::class)
class LandScapeRepositoryImplTest {
    private val dispatcher = StandardTestDispatcher()

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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getLandScapeList return error`() = runTest {
        val landScapeApiService = mock<LandScapeApiService> {
            on { getLandScapeList(any(), any(), any(), eq(-1)) } doThrow(Exception("No internet!"))
            on { getLandScapeList(any(), any(), any(), eq(1)) } doReturn
                    Response.success(listOf(LandScape(id = 5, url = "URL")))
        }
        val landScapeRepository = LandScapeRepositoryImpl(
            landScapeApiService,
            landScapeApiService
        )
        val landScapeResult1 = landScapeRepository.getLandScapeList(
            23.7f,
            180f,
            425.3f,
            -1
        )
        val landScapeResult2 = landScapeRepository.getLandScapeList(
            23.7f,
            180f,
            425.3f,
            1
        )
        advanceUntilIdle()
        assertThat(landScapeResult1).isInstanceOf(MyResult.Error::class.java)
        assertThat((landScapeResult1 as MyResult.Error).exception?.message).isEqualTo("No internet!")

        assertThat((landScapeResult2 as MyResult.Success).data.first().id).isEqualTo(5)
    }

}