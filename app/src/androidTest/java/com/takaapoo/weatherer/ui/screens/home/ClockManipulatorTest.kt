package com.takaapoo.weatherer.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.domain.model.HomeState
import com.takaapoo.weatherer.utility.toPx
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.sin

@HiltAndroidTest
class ClockManipulatorTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

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

    @SuppressLint("ConfigurationScreenWidthHeight")
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clockManipulator_showsDay() = runComposeUiTest {
        setContent {
            val config = LocalConfiguration.current
            val density = LocalDensity.current
            val clockGaugeWidth =
                (900.dp).coerceAtMost(config.screenWidthDp.dp / sin(PI/6).toFloat())

            val clockGaugeRotationCoefficient = 360 / (PI.toFloat() * clockGaugeWidth.toPx(density))

            ClockManipulator(
                gaugeSize = 300.dp,
                dayGaugeProvider = { true },
//                dayGaugeResource = R.drawable.day_gauge_light,
                homeState = remember { mutableStateOf(HomeState()) },
//                clockGaugeRotation = 0f,
//                clockGaugeNaturalRotation = 0f,
                clockGaugeRotationCoefficient = clockGaugeRotationCoefficient,
//                dayListState = rememberLazyListState(
//                    initialFirstVisibleItemIndex = Int.MAX_VALUE / 2 + LocalDateTime.now().dayOfWeek.value - 1,
//                    initialFirstVisibleItemScrollOffset = -35
//                ),
//                dayGaugeIndex = 0,
//                dayGaugeNaturalIndex = 0,
//                visibleDayIndex = 0,
//                clockGaugeLock = false,
                silent = true,
                vibration = false,
                dispatchers = dispatcher,

                onUpdateVisibleDayIndex = { _ -> },
                onUpdateDayGaugeIndex = { _ -> },
                onUpdateClockGaugeRotation = { _ -> },
                onStopRotation = {},
                onDecayRotation = { _ -> },
                onUpdateClockGaugeLock = { _ -> },
                onUndoButtonPressed = {}
            )
        }
//        dispatcher.scheduler.advanceUntilIdle()
        onNode(hasTestTag("row"), true).printToLog("BottomRow")

        // ClockManipulator displays the day list using LazyColumn with items(count = Int.MAX_VALUE)
        // and dayList[dayIndex % 7]. dayList starts with "MON".
        onNodeWithText(dayList[LocalDateTime.now().dayOfWeek.value - 1]).assertIsDisplayed()
    }
}
