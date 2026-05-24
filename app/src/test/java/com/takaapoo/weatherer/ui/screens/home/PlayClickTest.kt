package com.takaapoo.weatherer.ui.screens.home

import android.media.MediaPlayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class PlayClickTest {
    private val dispatcher = StandardTestDispatcher()
    val clicksCount = 10
    private val clickStarted = MutableList(clicksCount){ false }


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
    fun `playClick should play sound and vibrate correct number of times`() = runTest {
        // Arrange
        val count = 6
        val mediaPlayers = List(clicksCount) { mock(MediaPlayer::class.java) }
        val hapticFeedback = mock(HapticFeedback::class.java)

        // Act
        val job1 = launch {
            playClick(
                clickSound = mediaPlayers,
                clickStarted = clickStarted,
                hapticFeedback = hapticFeedback,
                count = count,
                silent = false,
                vibration = true
            )
        }
        delay(10)
        val job2 = launch {
            playClick(
                clickSound = mediaPlayers,
                clickStarted = clickStarted,
                hapticFeedback = hapticFeedback,
                count = count,
                silent = false,
                vibration = true
            )
        }

        // Advance coroutine time to allow all delays to run
        advanceUntilIdle()
        joinAll(job1, job2)

        // Assert
        repeat(clicksCount) { index ->
            verify(mediaPlayers[index]).start()
        }
        verify(hapticFeedback, times(clicksCount))
            .performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `playClick should not play sound when silent is true`() = runTest {
        val count = 3
        val mediaPlayers = List(10) { mock(MediaPlayer::class.java) }
        val hapticFeedback = mock(HapticFeedback::class.java)

        val job = launch {
            playClick(
                clickSound = mediaPlayers,
                clickStarted = clickStarted,
                hapticFeedback = hapticFeedback,
                count = count,
                silent = true,
                vibration = false
            )
        }

        advanceUntilIdle()
        job.join()

        // Assert no sound
        mediaPlayers.forEach {
            verify(it, never()).start()
        }

        // Assert no vibration
        verify(hapticFeedback, never())
            .performHapticFeedback(any())
    }

}