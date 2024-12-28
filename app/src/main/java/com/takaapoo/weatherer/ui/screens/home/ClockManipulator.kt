package com.takaapoo.weatherer.ui.screens.home

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.drawThickText
import com.takaapoo.weatherer.ui.screens.home.shapes.DayGaugeShape
import com.takaapoo.weatherer.ui.theme.Gray20
import com.takaapoo.weatherer.ui.theme.Gray30
import com.takaapoo.weatherer.ui.theme.Orange40
import com.takaapoo.weatherer.ui.theme.Orange60
import com.takaapoo.weatherer.ui.theme.TenseGreen
import com.takaapoo.weatherer.ui.theme.TenseRed
import com.takaapoo.weatherer.ui.utility.BorderedText
import com.takaapoo.weatherer.ui.utility.toPx
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor

val dayList = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
//const val RESET_BUTTON_ANGLE = -23f
//const val RESET_BUTTON_ANGLE_RADIAN = RESET_BUTTON_ANGLE * PI.toFloat() / 180


@Composable
fun ClockManipulator(
    modifier: Modifier = Modifier,
    gaugeSize: Dp,
    @DrawableRes dayGaugeResource: Int,
    clockGaugeRotation: Float,
    clockGaugeNaturalRotation: Float,
    clockGaugeRotationCoefficient: Float,
    dayListState: LazyListState,
    dayGaugeIndex: Int,
    dayGaugeNaturalIndex: Int,
    visibleDayIndex: Int,
    clockGaugeLock: Boolean,

    onUpdateVisibleDayIndex: (newVisibleDay: Int) -> Unit,
    onUpdateDayGaugeIndex: (newVisibleDay: Int) -> Unit,
    onHandleClickSound: (Float, Float ) -> Unit,
    onUpdateClockGaugeRotation: (rotation: Float) -> Unit,
    onStopRotation: () -> Unit,
    onDecayRotation: (velocity: Float) -> Unit,
    onUpdateClockGaugeLock: (lock: Boolean) -> Unit,
    onUndoButtonPressed: () -> Unit,

) {
//    Log.i("comp1", "dayGaugeIndex = $dayGaugeIndex , clockGaugeRotation = $clockGaugeRotation")
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    var previousRotationZ by remember { mutableFloatStateOf(0f) }

    val daysOfHours = floor(
        -(clockGaugeRotation + clockGaugeNaturalRotation) / 360f
    ).toInt()
    val dayScrollIndex = dayGaugeIndex + dayGaugeNaturalIndex + daysOfHours - visibleDayIndex
    val dragState = rememberDraggableState { delta ->
        onUpdateClockGaugeRotation(
            clockGaugeRotation + delta * clockGaugeRotationCoefficient
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


//    val scrollUp = dayListState.isScrollingUp()
//    Log.i("index1", "scroll in progress = ${dayListState.isScrollInProgress} , ${calculateDayScrollIndex()}")
//    if (dayListState.isScrollInProgress && calculateDayScrollIndex() == 0){
//        LaunchedEffect(key1 = dayListState.isScrollInProgress){
//            initialScrollIndex =
//                if (scrollUp) dayListState.firstVisibleItemIndex + 1
//                else dayListState.firstVisibleItemIndex
//            Log.i("index1", "initial scroll = ${initialScrollIndex - Int.MAX_VALUE/2} , scroll up = ${scrollUp}")
//        }
//        DisposableEffect(Unit){
//            onDispose {
//                updateDayGaugeIndex(dayListState.firstVisibleItemIndex - initialScrollIndex)
//                updateVisibleDayIndex(dayListState.firstVisibleItemIndex - initialScrollIndex)
//                Log.i("index1", "index = ${dayListState.firstVisibleItemIndex - Int.MAX_VALUE/2} , diff = ${dayListState.firstVisibleItemIndex - initialScrollIndex}")
//            }
//        }
//    }

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
                    rotationZ = clockGaugeRotation + clockGaugeNaturalRotation
                    if (previousRotationZ != 0f)
                        onHandleClickSound(rotationZ, previousRotationZ)
                    previousRotationZ = rotationZ
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

            var counter  = 0
            for (deg in 0 .. 345 step 15) {
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
                        fontFamily =  Font(R.font.cmu_typewriter_bold).toFontFamily(),
                        borderWidth = if (deg == 0) 3f else 2f
                    )
                }
                counter++
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
                modifier = Modifier.offset(y = (-16).dp),
                verticalAlignment = Alignment.CenterVertically
            ){
//            AmPmText(
//                text = "AM",
//                clockGaugeRotation = clockGaugeRotation,
//                clockGaugeNaturalRotation = clockGaugeNaturalRotation
//            )

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
                Box(
                    modifier = Modifier
                        .size(
                            width = dimensionResource(R.dimen.day_display_height) * 2.5f,
                            height = dimensionResource(R.dimen.day_display_height) * 2f
                        )
                ){
                    Image(
                        painter = painterResource(dayGaugeResource),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(
                                width = dimensionResource(R.dimen.day_display_height) * 2.5f,
                                height = dimensionResource(R.dimen.day_display_height)
                            ),
                        contentDescription = "Day display",
                        contentScale = ContentScale.Fit
                    )
                    val itemHeight = dimensionResource(R.dimen.day_display_item_height).toPx(density)
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
                                fontFamily = Font(resId = R.font.cmu_serif).toFontFamily(),
                                fontSize = 18.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconToggleButton(
                    checked = clockGaugeLock,
                    onCheckedChange = onUpdateClockGaugeLock
                ) {
                    Icon(
                        painter = if (clockGaugeLock) painterResource(id = R.drawable.lock)
                        else painterResource(id = R.drawable.lock_open),
                        contentDescription = "Reset clock",
                    )
                }
//            AmPmText(
//                text = "PM",
//                clockGaugeRotation = clockGaugeRotation,
//                clockGaugeNaturalRotation = clockGaugeNaturalRotation
//            )
            }
            TenseText(
                modifier = Modifier.offset(y = (-32).dp),
                dayGaugeIndex = dayGaugeIndex,
                clockGaugeRotation = clockGaugeRotation
            )
        }


    }
}

enum class Tense(val title: String) {
    PAST(title = "Past"),
    LIVE(title = "LIVE"), // ●
    FUTURE(title = "Future")
}
@Composable
fun TenseText(
    modifier: Modifier = Modifier,
    dayGaugeIndex: Int,
    clockGaugeRotation: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tense")
    val tenseVisibility by infiniteTransition.animateValue(
        initialValue = 0f,
        targetValue = 1f,
        typeConverter = Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tense alpha"
    )
    val hourAngle = dayGaugeIndex * 360 - clockGaugeRotation
    val tense = when {
        abs(hourAngle) < 0.01f -> Tense.LIVE
        hourAngle > 0 -> Tense.FUTURE
        else -> Tense.PAST
    }
    val color = if (tense == Tense.LIVE) TenseGreen else TenseRed
    val tenseAlpha = if (tense == Tense.LIVE) 1f
    else {
        if (tenseVisibility < 0.5f) 0f else 1f
    }

    Box(
        modifier = modifier.width(dimensionResource(R.dimen.tense_width))
//            .border(
//                width = 2.dp,
//                color = color,
//                shape = RoundedCornerShape(percent = 25)
//            )
            .padding(horizontal = 8.dp)
            .graphicsLayer {
                alpha = tenseAlpha
            }
    ) {
        BorderedText(
            modifier = Modifier.align(Alignment.Center),
            text = AnnotatedString(tense.title),
            fillColor = color,
            fontSize = 14.sp,
            strokeWidth = 1f,
            fontFamily =  Font(R.font.cmu_typewriter_bold).toFontFamily()
        )
    }
}

@Composable
fun AmPmText(
    modifier: Modifier = Modifier,
    text: String,
    clockGaugeRotation: Float,
    clockGaugeNaturalRotation: Float,
) {
    val mod = (clockGaugeRotation + clockGaugeNaturalRotation).mod(360f)
    val isOn = if (text == "AM") mod > 180f || (mod < 0 && mod > -180)
    else mod <= -180f || (mod in 0f..180f)

    if (isOn) {
        Box {
            Text(
                modifier = modifier,
                text = text,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontFamily = Font(resId = R.font.lato_black).toFontFamily(),
                color = Orange60,
                style = TextStyle(
                    drawStyle = Stroke(width = 2f)
                )
            )
            Text(
                modifier = modifier,
                text = text,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontFamily = Font(resId = R.font.lato_black).toFontFamily(),
                color = Orange40,
                style = TextStyle(
                    shadow = Shadow(
                        color = Orange40,
                        blurRadius = 10f
                    )
                )
            )
        }
    } else {
        Box {
            Text(
                modifier = modifier,
                text = text,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontFamily = Font(resId = R.font.lato_black).toFontFamily(),
                color = Gray20,
                style = TextStyle(
                    drawStyle = Stroke(width = 2f)
                )
            )
//            Text(
//                modifier = modifier,
//                text = text,
//                textAlign = TextAlign.Center,
//                fontSize = 14.sp,
//                fontFamily = Font(resId = R.font.lato_black).toFontFamily(),
//                color = Color.White
//            )
        }
    }
}

@Composable
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
}


