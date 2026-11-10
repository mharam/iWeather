package com.takaapoo.weatherer.ui.screens.home

import android.media.MediaPlayer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.HomeState
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.indicesOf
import com.takaapoo.weatherer.ui.screens.home.shapes.DayGaugeShape
import com.takaapoo.weatherer.ui.theme.Gray30
import com.takaapoo.weatherer.ui.theme.TenseGreen
import com.takaapoo.weatherer.ui.theme.TenseRed
import com.takaapoo.weatherer.ui.theme.timeFontFamily
import com.takaapoo.weatherer.utility.drawThickText
import com.takaapoo.weatherer.utility.isEqualTo
import com.takaapoo.weatherer.utility.toCustomString
import com.takaapoo.weatherer.utility.toPx
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sign
import kotlin.time.Duration.Companion.milliseconds

val dayList = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")


@Composable
fun ClockManipulator(
    modifier: Modifier = Modifier,
    gaugeSize: Dp,
//    @DrawableRes dayGaugeResource: Int,
    dayGaugeProvider: () -> Boolean,
    homeState: State<HomeState>,
//    clockGaugeRotation: Float,
//    clockGaugeNaturalRotation: Float,
    clockGaugeRotationCoefficient: Float,
//    dayListState: LazyListState,
//    dayGaugeIndex: Int,
//    dayGaugeNaturalIndex: Int,
//    visibleDayIndex: Int,
//    clockGaugeLock: Boolean,
    silent: Boolean,
    vibration: Boolean,
    dispatchers: CoroutineDispatcher = Dispatchers.Default,

    onUpdateVisibleDayIndex: (newVisibleDay: Int) -> Unit,
    onUpdateDayGaugeIndex: (newVisibleDay: Int) -> Unit,
    onUpdateClockGaugeRotation: (rotation: Float) -> Unit,
    onStopRotation: () -> Unit,
    onDecayRotation: (velocity: Float) -> Unit,
    onUpdateClockGaugeLock: (lock: Boolean) -> Unit,
    onUndoButtonPressed: () -> Unit
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

    val itemHeight = dimensionResource(R.dimen.day_display_item_height).toPx(density)
    val dayListState = rememberLazyListState(
        initialFirstVisibleItemIndex = homeState.value.dayGaugeNaturalIndex,
        initialFirstVisibleItemScrollOffset = -(itemHeight/2f).toInt(),
    )

    val daysOfHours by remember {
        derivedStateOf {
            floor(
                -(homeState.value.clockGaugeRotation.value + homeState.value.clockGaugeNaturalRotation) / 360f
            ).toInt()
        }
    }
    val dayScrollIndex = homeState.value.dayGaugeIndex + homeState.value.dayGaugeNaturalIndex +
            daysOfHours - homeState.value.visibleDayIndex
    val dragState = rememberDraggableState { delta ->
        onUpdateClockGaugeRotation(
            homeState.value.clockGaugeRotation.value + delta * clockGaugeRotationCoefficient
        )
    }
    val textMeasurer: TextMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val otherColor = MaterialTheme.colorScheme.onSecondaryContainer

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
                    rotationZ = homeState.value.clockGaugeRotation.value +
                            homeState.value.clockGaugeNaturalRotation
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
//                        onHandleClickSound(rotationZ, previousRotationZ)
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

            for ((counter, deg) in (0 .. 345 step 15).withIndex()) {
                rotate(degrees = deg.toFloat()) {
                    drawRoundRect(
                        color = if (deg == 0) primaryColor else otherColor,
                        topLeft = bigTickTopLeft,
                        size = bigTickSize,
                        cornerRadius = CornerRadius(bigTickSize.width / 4)
                    )
                    drawThickText(
                        textMeasurer = textMeasurer,
                        text = counter.toString(),
                        textColor = if (deg == 0) primaryColor else otherColor,
                        textCenter = gaugeSizePx / 2,
                        textTop = 10f + bigTickSize.height,
                        fontSize = 18.sp,
                        fontFamily = timeFontFamily,
                        borderWidth = if (deg == 0) 3f else 2f
                    )
                }
                for (i in 1 .. 3) {
                    rotate(degrees = (deg + i * 3.75f)) {
                        drawRoundRect(
                            color = Gray30,
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
                    },
                    onDragStopped = { velocity -> onDecayRotation(velocity) }
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
                    itemHeight = itemHeight
                )
                Spacer(modifier = Modifier.width(16.dp))
                IconToggleButton(
                    checked = homeState.value.clockGaugeLock,
                    onCheckedChange = onUpdateClockGaugeLock
                ) {
                    LockIcon(homeState.value.clockGaugeLock)
                }
            }
            TenseText(
                modifier = Modifier.offset(y = (-32).dp),
                hourAngleProvider = {
                    homeState.value.dayGaugeIndex * 360 - homeState.value.clockGaugeRotation.value
                },
//                dayGaugeIndex = dayGaugeIndex,
//                clockGaugeRotation = homeState.value.clockGaugeRotation.value
            )
        }


    }
}

/*enum class Tense(val title: String) {
    PAST(title = "Past"),
    LIVE(title = "LIVE"), // ●
    FUTURE(title = "Future")
}*/
@Composable
fun TenseText(
    modifier: Modifier = Modifier,
    hourAngleProvider: () -> Float,
//    dayGaugeIndex: Int,
//    clockGaugeRotation: Float
) {
    val textMeasurer = rememberTextMeasurer()
    val infiniteTransition = rememberInfiniteTransition(label = "tense")
    val tenseVisibility by infiniteTransition.animateValue(
        initialValue = 0f,
        targetValue = 1.5f,
        typeConverter = Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tense alpha"
    )
//    val hourAngle = dayGaugeIndex * 360 - clockGaugeRotation
//    val tense = when {
//        abs(hourAngle) < 0.01f -> Tense.LIVE
//        hourAngle > 0 -> Tense.FUTURE
//        else -> Tense.PAST
//    }

    Box(
        modifier = modifier
            .width(dimensionResource(R.dimen.tense_width))
            .height(dimensionResource(R.dimen.tense_height))
            .padding(horizontal = 8.dp)
            .graphicsLayer {
                alpha = if (abs(hourAngleProvider()).isEqualTo(0f)) 1f
                else {
                    if (tenseVisibility < 0.5f) 0f else 1f
                }
            }
            .drawBehind {
                val hourAngle = hourAngleProvider()
                val isLive = abs(hourAngle).isEqualTo(0f)
                drawThickText(
                    textMeasurer = textMeasurer,
                    annotatedString = AnnotatedString(
                        if (isLive) "LIVE"
                        else Duration.ofMinutes((hourAngle * 4).toLong()).toCustomString()
                    ),
                    textCenter = size.width / 2,
                    textTop = 0f,
                    textColor = if (isLive) TenseGreen else TenseRed,
                    borderWidth = 1f,
                    fontFamily = timeFontFamily
                )
            }
    ) {
//        BorderedText(
//            modifier = Modifier.align(Alignment.Center)
//                .graphicsLayer {
//                    colorFilter = ColorFilter.tint(
//                        color = if (abs(hourAngle).isEqualTo(0f)) TenseGreen else TenseRed
//                    )
//                },
//            text = AnnotatedString(tense.title),
////            fillColor = color,
//            fontSize = 14.sp,
//            strokeWidth = 1f,
//            fontFamily =  timeFontFamily
//        )
    }
}

//@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
suspend fun playClick(
    clickSound: List<MediaPlayer>,
    clickStarted: MutableList<Boolean>,
    hapticFeedback: HapticFeedback,
    count: Int,
    silent: Boolean,
    vibration: Boolean,
){
    val toBePlayedIndices = clickStarted.indicesOf(false)
    val clickCount = count.coerceAtMost(toBePlayedIndices.size)
    for (i in 0 until clickCount){
        clickStarted[toBePlayedIndices[i]] = true
    }
    repeat(clickCount) { i ->
        if (!silent) clickSound[toBePlayedIndices[i]].start()
        if (vibration) hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        delay(20.milliseconds)
        clickStarted[toBePlayedIndices[i]] = false
    }
}

@Composable
fun DayGauge(
    modifier: Modifier = Modifier,
    dayGaugeProvider: () -> Boolean,
    dayListState: LazyListState,
    itemHeight: Float
) {
    Box(
        modifier = modifier.size(
            width = dimensionResource(R.dimen.day_display_height) * 2.5f,
            height = dimensionResource(R.dimen.day_display_height) * 2f
        )
    ){
        Image(
            painter = painterResource(R.drawable.day_gauge_light),
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .graphicsLayer {
                    colorFilter =
                        ColorFilter.tint(
                            color = if (dayGaugeProvider()) Color.White else Color.Black,
                            blendMode = BlendMode.Modulate
                        )
                },
            contentDescription = "Day display",
            contentScale = ContentScale.Fit
        )

        LazyColumn(
            modifier = Modifier
                .size(
                    width = dimensionResource(R.dimen.day_display_height) * 2.5f,
                    height = dimensionResource(R.dimen.day_display_item_height) * 2
                )
                .align(Alignment.Center)
                .clip(DayGaugeShape()),
            state = dayListState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = dayListState)
        ) {
            items(count = Int.MAX_VALUE){ dayIndex ->
                Text(
                    text = dayList[dayIndex % 7],
                    modifier = Modifier
                        .size(
                            width = dimensionResource(R.dimen.day_display_height) * 2.5f,
                            height = dimensionResource(R.dimen.day_display_item_height)
                        )
                        .graphicsLayer {
                            rotationX =
                                (-dayListState.firstVisibleItemScrollOffset / itemHeight + 0.5f
                                        - (dayListState.firstVisibleItemIndex - dayIndex + 1)) * 60f
                        }
                        .wrapContentHeight(),
                    textAlign = TextAlign.Center,
                    fontFamily = timeFontFamily,
                    fontSize = 18.sp
                )
            }
        }
    }
}
@Composable
fun LockIcon(isLocked: Boolean) {
    Icon(
        painter = if (isLocked) painterResource(id = R.drawable.lock)
        else painterResource(id = R.drawable.lock_open),
        contentDescription = "Lock clock",
    )
}


@Preview(showBackground = true)
@Composable
fun ClockManipulatorPreview() {
    ClockManipulator(
        gaugeSize = 300.dp,
//        dayGaugeResource = R.drawable.day_gauge_light,
        dayGaugeProvider = { true },
        homeState = remember { mutableStateOf(HomeState()) },
//        clockGaugeRotation = 0f,
//        clockGaugeNaturalRotation = 0f,
        clockGaugeRotationCoefficient = 1f,
//        dayListState = rememberLazyListState(),
//        dayGaugeIndex = 0,
//        dayGaugeNaturalIndex = 0,
//        visibleDayIndex = 0,
//        clockGaugeLock = false,
        silent = true,
        vibration = false,
        onUpdateVisibleDayIndex = { _ -> },
        onUpdateDayGaugeIndex = { _ -> },
        onUpdateClockGaugeRotation = { _ -> },
        onStopRotation = {},
        onDecayRotation = { _ -> },
        onUpdateClockGaugeLock = { _ -> },
        onUndoButtonPressed = {}
    )
}


/*@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}*/


