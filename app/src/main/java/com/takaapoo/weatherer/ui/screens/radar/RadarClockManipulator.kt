package com.takaapoo.weatherer.ui.screens.radar

import android.media.MediaPlayer
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.RadarState
import com.takaapoo.weatherer.ui.screens.home.DayGauge
import com.takaapoo.weatherer.ui.screens.home.playClick
import com.takaapoo.weatherer.ui.theme.Gray30
import com.takaapoo.weatherer.ui.theme.timeFontFamily
import com.takaapoo.weatherer.utility.drawThickText
import com.takaapoo.weatherer.utility.measureText
import com.takaapoo.weatherer.utility.toPx
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sign

@Composable
fun RadarClockManipulator(
    modifier: Modifier = Modifier,
    gaugeSize: Dp,
    dayListState: LazyListState,
    dayGaugeProvider: () -> Boolean,
    radarState: State<RadarState>,
    clockGaugeRotationCoefficient: Float,
    silent: Boolean,
    vibration: Boolean,
    dispatchers: CoroutineDispatcher = Dispatchers.Default,
    weatherModel: WeatherModels,

    onUpdateVisibleDayIndex: (newVisibleDay: Int) -> Unit,
    onUpdateDayGaugeIndex: (newVisibleDay: Int) -> Unit,
    onUpdateClockGaugeRotation: (rotation: Float) -> Unit,
    onStopRotation: suspend () -> Unit,
    onDecayRotation: suspend (velocity: Float) -> Unit,
    onUndoButtonPressed: () -> Unit,
    onUpdateClockGaugeDragStopped: (Boolean) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    var previousRotationZ by remember { mutableFloatStateOf(0f) }
    val clicksCount = 10
    val clickSound = remember {
        List(clicksCount){
            MediaPlayer.create(context, R.raw.click1).apply {
                setVolume(0.15f, 0.15f)
            }
        }
    }
    val clickStarted = remember {
        MutableList(clicksCount){ false }
    }

//    val itemHeight = dimensionResource(R.dimen.day_display_item_height).toPx(density)
//    val dayListInitialIndex = radarState.value.dayGaugeNaturalIndex
//    val dayListState = rememberLazyListState(
//        initialFirstVisibleItemIndex = dayListInitialIndex,
//        initialFirstVisibleItemScrollOffset = -(itemHeight/2f).toInt(),
//    )

    val daysOfHours by remember {
        derivedStateOf {
            floor(
                -(radarState.value.clockGaugeRotation.value + radarState.value.clockGaugeNaturalRotation) / 360f
            ).toInt()
        }
    }
    val dayScrollIndex = radarState.value.dayGaugeIndex + radarState.value.dayGaugeNaturalIndex +
            daysOfHours - radarState.value.visibleDayIndex
    val dragState = rememberDraggableState { delta ->
        onUpdateClockGaugeRotation(
            radarState.value.clockGaugeRotation.value + delta * clockGaugeRotationCoefficient
        )
    }
    val textMeasurer: TextMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.onSecondaryContainer
    val otherColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)

    val dayScrollHeight = dayScrollIndex * dimensionResource(R.dimen.day_display_item_height).toPx(density)
    LaunchedEffect(key1 = dayScrollIndex){
        if (dayScrollIndex != 0) {
            coroutineScope.launch {
                dayListState.animateScrollBy(
                    value = dayScrollHeight,
                    animationSpec = tween(durationMillis = 500)
                )
            }
        }
    }
    LaunchedEffect(dayListState) {
        snapshotFlow { dayListState.firstVisibleItemIndex }.collect {
            onUpdateDayGaugeIndex(it + 1)
            onUpdateVisibleDayIndex(it + 1)
        }
    }

    var wholeGaugeRotation by rememberSaveable { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.clock_manipulator_height))
            .wrapContentSize(
                align = Alignment.TopCenter,
                unbounded = true
            )
    ){
        Canvas(
            modifier = Modifier
                .size(gaugeSize)
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    rotationZ = radarState.value.clockGaugeRotation.value +
                            radarState.value.clockGaugeNaturalRotation
                    wholeGaugeRotation = rotationZ
                    coroutineScope.launch(dispatchers) {
                        if (previousRotationZ != 0f) {
                            val diff =
                                abs((wholeGaugeRotation / 3.75f).toInt() - (previousRotationZ / 3.75f).toInt())
                            if (wholeGaugeRotation.sign != previousRotationZ.sign) {
                                playClick(
                                    clickSound = clickSound,
                                    clickStarted = clickStarted,
                                    hapticFeedback = hapticFeedback,
                                    count = 1,
                                    silent = silent,
                                    vibration = vibration
                                )
                            } else {
                                playClick(
                                    clickSound = clickSound,
                                    clickStarted = clickStarted,
                                    hapticFeedback = hapticFeedback,
                                    count = diff,
                                    silent = silent,
                                    vibration = vibration
                                )
                            }
                        }
                        previousRotationZ = wholeGaugeRotation
                    }
                }
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape
                )
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                )
        ){
            val gaugeSizePx = gaugeSize.toPx()
            val bigTickSize = Size(width = gaugeSizePx / 240, height = gaugeSizePx / 60)
            val bigTickTopLeft = Offset(x = (gaugeSizePx - bigTickSize.width) / 2, y = 10f)
            val smallTickSize = Size(width = gaugeSizePx / 300, height = gaugeSizePx / 90)
            val smallTickTopLeft = Offset(x = (gaugeSizePx - smallTickSize.width) / 2, y = 10f)

            val utcOffsetMinutes = if (radarState.value.clock == Clock.systemUTC()) 0
                else ZonedDateTime.now(ZoneId.systemDefault()).offset.totalSeconds / 60

            val validTimes = radarState.value.radarSpecs.getOrDefault(weatherModel, null)
                ?.validTimes?.map {
                    OffsetDateTime.parse(it).toLocalDateTime().plusMinutes(utcOffsetMinutes.toLong())
                }

            for ((counter, deg) in (0 .. (360*4 - 15) step 15).withIndex()) {
                val clockHours = when {
                    counter in 12 .. 84 -> daysOfHours
                    counter < 12 -> ((-radarState.value.clockGaugeRotation.value -
                            radarState.value.clockGaugeNaturalRotation) / 360f).roundToInt()
                    else -> ((-radarState.value.clockGaugeRotation.value -
                            radarState.value.clockGaugeNaturalRotation) / 360f).roundToInt() - 1
                }
                val clockGaugeDateTime = LocalDate.now(radarState.value.clock)
                    .plusDays((clockHours + radarState.value.dayGaugeIndex).toLong())
                    .atStartOfDay()

                val time = clockGaugeDateTime.plusSeconds(deg * 60L)
                val timeExist = validTimes?.contains(time) == true
//                val timeInBetween = (validTimes?.firstOrNull()?.let { time.isAfter(it) } ?: false) &&
//                        (validTimes.lastOrNull()?.let { time.isBefore(it) } ?: false)

                rotate(degrees = deg / 4f) {
                    if (counter % 4 == 0) {
                        drawThickText(
                            textMeasurer = textMeasurer,
                            text = (counter / 4).toString(),
                            textColor = if (timeExist) primaryColor else otherColor,
                            textCenter = gaugeSizePx / 2,
                            textTop = 10f + bigTickSize.height,
                            fontSize = 18.sp,
                            fontFamily = timeFontFamily,
                            borderWidth = 2f
                        )
                    }
                    if (timeExist) {
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = bigTickTopLeft,
                            size = bigTickSize,
                            cornerRadius = CornerRadius(bigTickSize.width / 4)
                        )
                    } else {
                        drawRoundRect(
                            color = otherColor,
                            topLeft = smallTickTopLeft,
                            size = smallTickSize,
                            cornerRadius = CornerRadius(smallTickSize.width / 2)
                        )
                    }
                }
            }
        }
        Spacer(
            modifier = Modifier
                .size(gaugeSize)
                .align(Alignment.TopCenter)
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    startDragImmediately = true,
                    onDragStarted = {
                        onStopRotation()
                        onUpdateClockGaugeDragStopped(false)
                    },
                    onDragStopped = { velocity ->
                        onUpdateClockGaugeDragStopped(true)
                        launch(Dispatchers.Default) {
                            onDecayRotation(velocity)
                        }
                    }
                )
        )
        Column(
            modifier = Modifier
                .padding(top = 40.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Clock Zero indicator
            Box(
                modifier = Modifier
                    .size(width = 2.5.dp, height = 14.dp)
                    .border(
                        width = 0.8.dp,
                        color = Gray30,
                        shape = RoundedCornerShape(percent = 50)
                    )
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .offset(y = (-16).dp)
                    .testTag("row"),
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(
                    modifier = Modifier,
                    onClick = onUndoButtonPressed
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.undo),
                        contentDescription = "Reset clock",
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                DayGauge(
                    dayGaugeProvider = dayGaugeProvider,
                    dayListState = dayListState,
                    itemHeight = dimensionResource(R.dimen.day_display_item_height).toPx(density)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Spacer(modifier = Modifier.width(48.dp))
            }
            DateText(
                modifier = Modifier.offset(y = (-32).dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                dateProvider = {
                    LocalDate.now(radarState.value.clock).plusDays(
                        (daysOfHours + radarState.value.dayGaugeIndex).toLong()
                    )
                },
                utcOffsetProvider = {
                    if (radarState.value.clock == Clock.systemUTC()) 0
                    else ZonedDateTime.now(ZoneId.systemDefault()).offset.totalSeconds / 60
                }
            )
        }


    }
}

@Composable
fun DateText(
    modifier: Modifier = Modifier,
    color: Color,
    dateProvider: () -> LocalDate,
    utcOffsetProvider: () -> Int,
) {
    val textMeasurer = rememberTextMeasurer()
    Box(
        modifier = modifier
            .width(200.dp)
            .height(dimensionResource(R.dimen.tense_height))
            .drawBehind {
                val date = dateProvider()
                val utcOffsetMinutes = utcOffsetProvider()

                val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
                val day = date.dayOfMonth
                val year = date.year

                val gmtText = if (utcOffsetMinutes == 0) "(GMT)"
                else "(GMT${if (utcOffsetMinutes > 0) "+" else "-"}${abs(utcOffsetMinutes)/60}:${abs(utcOffsetMinutes)%60})"

                val textLayoutResult = measureText(
                    textMeasurer = textMeasurer,
                    text = "$month ${day}, $year $gmtText",
                    fontSize = 14.sp,
                    fontFamily = timeFontFamily,
                    color = color
                )
                val textSize = textLayoutResult.size
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = (size.width - textSize.width) / 2f,
                        y = 0f
                    ),
                )
            }
    )
}

