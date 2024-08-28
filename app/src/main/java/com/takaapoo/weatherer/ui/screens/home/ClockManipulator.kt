package com.takaapoo.weatherer.ui.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.theme.Gray20
import com.takaapoo.weatherer.ui.theme.Orange40
import com.takaapoo.weatherer.ui.theme.Orange60
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val dayList = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
const val RESET_BUTTON_ANGLE = -23f
const val RESET_BUTTON_ANGLE_RADIAN = RESET_BUTTON_ANGLE * PI.toFloat() / 180


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClockManipulator(
    modifier: Modifier = Modifier,
    gaugeSize: Dp,
    dragState: DraggableState,
    clockGaugeRotationAnimatable: Animatable<Float, AnimationVector1D>,
    clockGaugeNaturalRotation: Float,
    clockGaugeRotationCoefficient: Float,
    dayListState: LazyListState,
    dayScrollIndex: State<Int>,
    updateVisibleDayIndex: (indexChange: Int) -> Unit,
    updateDayGaugeIndex: (dayIncrease: Int) -> Unit,
    calculateDayScrollIndex: () -> Int,
    onHandleClickSound: (Float, Float ) -> Unit,
    onResetClockGauge: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    var initialScrollIndex = rememberSaveable { 0 }
    var previousRotationZ by remember { mutableFloatStateOf(0f) }
    var resetButtonPushed by rememberSaveable { mutableStateOf(false) }
    val resetButtonRadius by animateDpAsState(
        targetValue =
        if (resetButtonPushed) 0.5f * gaugeSize + dimensionResource(R.dimen.clock_gauge_reset_size) / 2
        else 0.5f * gaugeSize + 2 * dimensionResource(R.dimen.clock_gauge_reset_size) / 3,
        label = "resetButton",
        animationSpec = tween(durationMillis = 400)
    )


    val dayScrollHeight = dayScrollIndex.value * dimensionResource(R.dimen.day_display_item_height).toPx(density)
//    Log.i("index1", "dayScrollIndex = ${dayScrollIndex.value}")
    LaunchedEffect(key1 = dayScrollIndex.value){
        if (dayScrollIndex.value != 0) {
            coroutineScope.launch {
                dayListState.animateScrollBy(
                    value = dayScrollHeight,
                    animationSpec = tween(durationMillis = 700)
                )
                updateVisibleDayIndex(dayScrollIndex.value)
            }
        }
    }

    val scrollUp = dayListState.isScrollingUp()
    if (dayListState.isScrollInProgress && calculateDayScrollIndex() == 0){
        LaunchedEffect(key1 = dayListState.isScrollInProgress){
            initialScrollIndex =
                if (scrollUp) dayListState.firstVisibleItemIndex + 1
                else dayListState.firstVisibleItemIndex
//            Log.i("index1", "initial scroll = ${initialScrollIndex - Int.MAX_VALUE/2} , scroll up = ${scrollUp}")
        }
        DisposableEffect(Unit){
            onDispose {
                updateDayGaugeIndex(dayListState.firstVisibleItemIndex - initialScrollIndex)
                updateVisibleDayIndex(dayListState.firstVisibleItemIndex - initialScrollIndex)
//                Log.i("index1", "index = ${dayListState.firstVisibleItemIndex - Int.MAX_VALUE/2} , diff = ${dayListState.firstVisibleItemIndex - initialScrollIndex}")
            }
        }
    }

    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.clock_manipulator_height))
            .wrapContentSize(
                align = Alignment.TopCenter,
                unbounded = true
            )
    ){
        Image(
            modifier = Modifier
                .size(dimensionResource(R.dimen.clock_gauge_reset_size))
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    translationX = resetButtonRadius.toPx() * sin(RESET_BUTTON_ANGLE_RADIAN)
                    translationY = (0.5f*gaugeSize - resetButtonRadius * cos(RESET_BUTTON_ANGLE_RADIAN))
                        .toPx()
                    rotationZ = RESET_BUTTON_ANGLE
                },
            painter = painterResource(R.drawable.clock_reset2),
            contentDescription = "reset"
        )
        Image(
            painter = painterResource(R.drawable.gauge_number2),
            contentDescription = null,
            modifier = Modifier
                .size(gaugeSize)
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    rotationZ = clockGaugeRotationAnimatable.value + clockGaugeNaturalRotation
//                    Log.i("rot1", "rotationZ = $rotationZ , previousRotationZ = $previousRotationZ")
                    if (previousRotationZ != 0f)
                        onHandleClickSound(rotationZ, previousRotationZ)
                    previousRotationZ = rotationZ
                }
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape
                )
        )
        Image(
            painter = painterResource(R.drawable.clock_gauge_frame2),
            contentDescription = "Clock Manipulator",
            modifier = Modifier
                .size(gaugeSize)
                .align(Alignment.TopCenter)
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    startDragImmediately = true,
                    onDragStarted = {
                        clockGaugeRotationAnimatable.stop()
                    },
                    onDragStopped = { velocity ->
                        launch(Dispatchers.Default) {
                            clockGaugeRotationAnimatable.animateDecay(
                                initialVelocity = (velocity * clockGaugeRotationCoefficient)
                                    .coerceIn(-360f, 360f),
                                animationSpec = exponentialDecay(
                                    frictionMultiplier = 0.25f
                                )
                            )
                        }
                    }
                )
        )
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.clock_gauge_reset_size))
                .align(Alignment.TopCenter)
                .offset(
                    x = 0.5f * gaugeSize * sin(RESET_BUTTON_ANGLE_RADIAN),
                )
                .pointerInput(Unit){
                    detectTapGestures(
                        onPress = {
                            resetButtonPushed = true
                            tryAwaitRelease()
                            resetButtonPushed = false
                            onResetClockGauge()
                        }
                    )
                }
            )
        Box(
            modifier = Modifier
                .padding(top = 40.dp)
                .align(Alignment.TopCenter)
                .size(width = 2.dp, height = 14.dp)
                .border(
                    width = 0.5.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(percent = 50)
                )
        )
        Row(
            modifier = Modifier
                .padding(top = 88.dp - dimensionResource(R.dimen.day_display_height))
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ){
            AmPmText(
                text = "AM",
                clockGaugeRotationAnimatable = clockGaugeRotationAnimatable,
                clockGaugeNaturalRotation = clockGaugeNaturalRotation
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(
                        width = dimensionResource(R.dimen.day_display_height) * 2.5f ,
                        height = dimensionResource(R.dimen.day_display_height)
                    )
            ){
                Image(
                    painter = painterResource(R.drawable.day_gauge),
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "Day display",
                    contentScale = ContentScale.Fit
                )
                LazyColumn(
                    modifier = Modifier
                        .size(
                            width = dimensionResource(R.dimen.day_display_height) * 2.5f,
                            height = dimensionResource(R.dimen.day_display_item_height)
                        )
                        .align(Alignment.Center),
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
                                .wrapContentHeight(),
                            textAlign = TextAlign.Center,
                            fontFamily = Font(resId = R.font.cmu_serif).toFontFamily(),
                            fontSize = 18.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            AmPmText(
                text = "PM",
                clockGaugeRotationAnimatable = clockGaugeRotationAnimatable,
                clockGaugeNaturalRotation = clockGaugeNaturalRotation
            )
        }

    }
}

@Composable
fun AmPmText(
    modifier: Modifier = Modifier,
    text: String,
    clockGaugeRotationAnimatable: Animatable<Float, AnimationVector1D>,
    clockGaugeNaturalRotation: Float,
) {
    val mod = (clockGaugeRotationAnimatable.value + clockGaugeNaturalRotation).mod(360f)
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


