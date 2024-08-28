package com.takaapoo.weatherer.ui.screens.home

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.LocationsState
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.pathInnerShadow
import com.takaapoo.weatherer.ui.theme.Gray20
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.theme.customColorScheme
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationGraphicsApi::class)
@Composable
fun WeatherItem2(
    zoom: Float,
    cardMaxHeight: Dp,
    lazyColumnHeight: Float,
    roomWidth: Dp,
    windowRotation: Float,
    modifier: Modifier = Modifier,
    itemState: LocationsState,
    pageNumber: Int,
    lazyParentState: LazyListState,
    onNavigateToItem: (id: Int, centerX: Float, centerY: Float) -> Unit,
    appSettings: AppSettings,
    onDeleteIconPressed: (id: Int, name: String) -> Unit,
    onEditItem: (id: Int, name: String) -> Unit,
    dialogDisplayed: Boolean,
    toBeDeletedLocationId: Int,
    onDeleteItem: (id: Int) -> Unit
) {
    val density : Density = LocalDensity.current

    var centerX by rememberSaveable { mutableFloatStateOf(0f) }
    var centerY by rememberSaveable { mutableFloatStateOf(0f) }
    val cardHeight by animateDpAsState(
        targetValue = if(itemState.locationId == toBeDeletedLocationId) 0.dp else cardMaxHeight,
        label = "cardHeight",
        animationSpec = tween(
            durationMillis = 300,
            easing = CubicBezierEasing(0.43f, 0f, 0.35f, 1f)
        ),
        finishedListener = {
            if(itemState.locationId == toBeDeletedLocationId)
                onDeleteItem(itemState.locationId)
        }
    )
    val anchors = remember {
        DraggableAnchors {
            DragValue.Left at -DRAG_MAX_DISPLACE.toPx(density)
            DragValue.Center at 0f
            DragValue.Right at DRAG_MAX_DISPLACE.toPx(density)
        }
    }
    val positionalThreshold = { distance: Float -> distance * 0.5f }
    val velocityThreshold = { 100.dp.toPx(density) }
    val animationSpec = tween<Float>()
    val dragState = rememberSaveable(
        saver = AnchoredDraggableState.Saver(animationSpec, positionalThreshold, velocityThreshold)
    ) {
        AnchoredDraggableState(
//            anchors = anchors,
            initialValue = DragValue.Center,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold,
            animationSpec = animationSpec
        )
    }.apply { updateAnchors(anchors) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = dialogDisplayed){
        if (dialogDisplayed) {
            dragState.animateTo(
                targetValue = DragValue.Center,
                velocity = DRAG_MAX_DISPLACE.toPx(density) / 2
            )
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(
                min = dimensionResource(R.dimen.home_card_min_width),
                max = dimensionResource(R.dimen.home_card_max_width)
            )
            .height(if (itemState.locationId == toBeDeletedLocationId) cardHeight else cardMaxHeight)
//            .padding(
//                horizontal = 2 * dimensionResource(R.dimen.home_card_spacing),
//                vertical = dimensionResource(R.dimen.home_card_spacing)
//            )
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(
                    (cardHeight - dimensionResource(R.dimen.home_card_top_over_head))
                        .coerceAtLeast(0.dp)
                )
                .align(Alignment.BottomCenter)
                .background(
                    color = dragBackgroundColor(
                        try {
                            dragState.requireOffset() / DRAG_MAX_DISPLACE.toPx(density)
                        } catch (e: IllegalStateException) {
                            0f
                        }
                    ),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.home_card_corner_radius))
                )
                .clickable {
                    when (dragState.currentValue) {
                        DragValue.Right -> onDeleteIconPressed(
                            itemState.locationId,
                            itemState.locationName
                        )

                        DragValue.Left -> onEditItem(itemState.locationId, itemState.locationName)
                        DragValue.Center -> {}
                    }
                }
        ) {
            Icon(
                modifier = Modifier
                    .padding(horizontal = (DRAG_MAX_DISPLACE - 48.dp) / 2)
                    .size(48.dp)
                    .align(Alignment.CenterStart),
                painter = rememberAnimatedVectorPainter(
                    animatedImageVector = AnimatedImageVector.animatedVectorResource(R.drawable.trash_anim),
                    atEnd = dragState.currentValue == DragValue.Right
                ),
                contentDescription = "delete"
            )
            Icon(
                modifier = Modifier
                    .padding(horizontal = (DRAG_MAX_DISPLACE - CardBackIconSize) / 2)
                    .size(CardBackIconSize)
                    .align(Alignment.CenterEnd),
                painter = rememberAnimatedVectorPainter(
                    animatedImageVector = AnimatedImageVector.animatedVectorResource(R.drawable.edit_anim),
                    atEnd = dragState.currentValue == DragValue.Left
                ),
                contentDescription = "edit"
            )
        }

        val cardShape = CardShape(
            cardOverHead = dimensionResource(R.dimen.home_card_top_over_head).toPx(density),
            topRound = dimensionResource(R.dimen.home_card_corner_radius).toPx(density),
            overHeadDistance = dimensionResource(R.dimen.home_card_overhead_distance).toPx(density)
        )
        Card(
//            enabled = dragState.currentValue == DragValue.Center,
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = if (zoom == 1f && !dragState.offset.isNaN()) {
                            dragState.offset.roundToInt()
                        } else 0,
                        y = 0
                    )
                }
                .shadow(
                    elevation = 8.dp,
                    shape = cardShape
                )
                .clip(cardShape)
                .clickable(
                    enabled = dragState.currentValue == DragValue.Center
                ) {
                    val itemOffset = lazyParentState.layoutInfo.visibleItemsInfo.find {
                        it.key == itemState.locationId
                    }?.offset ?: 0

                    val scrollAmount = when {
                        itemOffset < 0 -> itemOffset.toFloat()
                        itemOffset + cardMaxHeight.toPx(density) > lazyColumnHeight ->
                            itemOffset + cardMaxHeight.toPx(density) - lazyColumnHeight

                        else -> 0f
                    }
                    if (dragState.targetValue == DragValue.Center) {
                        coroutineScope.launch {
                            if (zoom == 1f) {
                                lazyParentState.animateScrollBy(
                                    value = scrollAmount,
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = CubicBezierEasing(0.5f, 0f, 0.9f, 0.3f)
                                    )
                                )
                            }
                            onNavigateToItem(itemState.locationId, centerX, centerY)
                        }
                    }
                }
                .anchoredDraggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    enabled = zoom == 1f
                ),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.customColorScheme.cardSurface),
        ){
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(
                            when(pageNumber){
                                0 -> if (zoom == 1f) R.drawable.ax2_c else R.drawable.ax2
                                1 -> if (zoom == 1f) R.drawable.ax3_c else R.drawable.ax3
                                else -> if (zoom == 1f) R.drawable.ax4_c else R.drawable.ax4
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .graphicsLayer {
                                scaleX = 3f / zoom
                                scaleY = 3f / zoom
                            },
                        alignment = Alignment.TopCenter,
                        contentScale = ContentScale.Crop
                    )
//                    WeatherItemData(
//                        modifier = Modifier
//                            .padding(start = 8.dp, top = 0.59f * roomWidth)
//                            .align(Alignment.TopStart),
//                        humidity = itemState.currentHumidity,
//                        precipitationProbability = itemState.currentPrecipitationProbability,
//                        windSpeed = itemState.currentWindSpeed,
//                        windDirection = itemState.currentWindDirection,
//                        airQuality = itemState.currentAirQuality,
//                        weatherCode = itemState.currentWeatherCode,
//                        isDay = itemState.isDay,
//                        moonType = itemState.moonType
//                    )


//                    RoomClock(
//                        modifier = Modifier
//                            .align(Alignment.TopStart)
//                            .padding(roomWidth / 24)
//                            .width(roomWidth / 8),
//                        roomWidth = roomWidth,
//                        bigHandleRotation = itemState.clockBigHandleRotation,
//                        smallHandleRotation = itemState.clockSmallHandleRotation,
//                        dayClock = itemState.isDay,
//                        am = itemState.am
//                    )
                }
//                RoomRibbon(
//                    topPadding = 0.4f * roomWidth,
//                    circleWidth = 0.24f * roomWidth,
//                    locationName = itemState.locationName,
//                    currentTemperature = itemState.currentTemperature,
//                    maxTemperature = itemState.todayMaxTemperature,
//                    minTemperature = itemState.todayMinTemperature,
//                    temperatureUnit = appSettings.temperatureUnit,
//                    day = itemState.day,
//                    month = itemState.month,
//                    year = itemState.year
//                )

                WindowLeft(
                    modifier = Modifier.fillMaxSize()
                        .graphicsLayer(
                            transformOrigin = TransformOrigin(0f, 0.5f),
                            rotationY = 0f,
                            cameraDistance = 50f
                        )
                )
                WindowLeft(
                    modifier = Modifier.fillMaxSize()
                        .scale(scaleX = -1f, scaleY = 1f)
                        .graphicsLayer(
                            transformOrigin = TransformOrigin(0f, 0.5f),
                            rotationY = 0f,
                            cameraDistance = 50f
                        )
                )
            }
        }

    }
}

@Composable
fun WindowLeft(
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val clockRound = dimensionResource(R.dimen.home_card_clock_round).toPx(density)
    val cornerRadius = dimensionResource(R.dimen.home_card_corner_radius).toPx(density)
    val bottomRound = cornerRadius
    val cardOverHead = dimensionResource(R.dimen.home_card_top_over_head).toPx(density)
    val overHeadDistance = dimensionResource(R.dimen.home_card_overhead_distance).toPx(density)
    val borderWidth = cardOverHead
    val bottomBorderWidth = dimensionResource(R.dimen.home_card_bottom_border).toPx(density)
    val iconRadius = dimensionResource(R.dimen.home_card_icons_radius).toPx(density)
    val clockAngle = acos((clockRound - borderWidth) / clockRound)
    val thermometerAngle = asin(borderWidth / (2 * iconRadius))

    Canvas(
        modifier = modifier.fillMaxSize()
    ){
        val outerPath = Path().apply {
            moveTo(0f, cardOverHead + cornerRadius)
            arcTo(
                rect = Rect(
                    center = Offset(cornerRadius, cardOverHead + cornerRadius),
                    radius = cornerRadius
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            relativeLineTo(dx = overHeadDistance, dy = 0f)
            arcTo(
                rect = Rect(
                    center = Offset(cornerRadius + overHeadDistance + cardOverHead, cardOverHead),
                    radius = cardOverHead
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(x = size.width / 2, y = 0f)
            relativeLineTo(dx = 0f, dy = size.height)
            lineTo(x = bottomRound, y = size.height)
            arcTo(
                rect = Rect(
                    center = Offset(bottomRound, size.height - bottomRound),
                    radius = bottomRound
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }
        val innerPath = Path().apply {
            moveTo(borderWidth, cardOverHead + clockRound * (1 + sin(clockAngle)))
            arcToRad(
                rect = Rect(
                    center = Offset(clockRound, cardOverHead + clockRound),
                    radius = clockRound
                ),
                startAngleRadians = PI.toFloat() - clockAngle,
                sweepAngleRadians = -(3*PI.toFloat()/2 - 2*clockAngle),
                forceMoveTo = false
            )
            lineTo(x = (size.width - borderWidth - cornerRadius) / 2f , y = cardOverHead + borderWidth)
            arcTo(
                rect = Rect(
                    center = Offset(
                        (size.width - borderWidth - cornerRadius) / 2f,
                        cardOverHead + borderWidth + cornerRadius / 2
                    ),
                    radius = cornerRadius / 2
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(x = (size.width - borderWidth) / 2f, y = size.height - bottomBorderWidth - iconRadius)
            arcToRad(
                rect = Rect(
                    center = Offset(size.width / 2f, size.height - bottomBorderWidth),
                    radius = iconRadius
                ),
                startAngleRadians = - (PI/2 + thermometerAngle).toFloat(),
                sweepAngleRadians = - (PI/2 - thermometerAngle).toFloat(),
                forceMoveTo = false
            )
            val gap = (size.width - 2 * borderWidth - 5 * 2 * iconRadius) / 6
            val center1X = size.width / 2 - 2 * iconRadius - gap
            lineTo(x = center1X + iconRadius, y = size.height - bottomBorderWidth)
            arcTo(
                rect = Rect(
                    center = Offset(center1X, size.height - bottomBorderWidth),
                    radius = iconRadius
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            val center2X = center1X - gap - 2 * iconRadius
            lineTo(x = center2X + iconRadius, y = size.height - bottomBorderWidth)
            arcTo(
                rect = Rect(
                    center = Offset(center2X, size.height - bottomBorderWidth),
                    radius = iconRadius
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )

            lineTo(x = borderWidth + cornerRadius / 2, y = size.height - bottomBorderWidth)
            arcTo(
                rect = Rect(
                    center = Offset(
                        borderWidth + cornerRadius / 2,
                        size.height - bottomBorderWidth - cornerRadius / 2
                    ),
                    radius = cornerRadius / 2
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }
        drawIntoCanvas {
            it.saveLayer(Rect(0f, 0f, size.width, size.height), Paint())
        }
        drawPath(
            path = outerPath,
            color = Color.White
        )
        drawPath(
            path = innerPath,
            color = Color.Black,
            blendMode = BlendMode.Clear
        )
        pathInnerShadow(
            path = innerPath,
            color = Gray20
        )
    }
}



@Preview(showBackground = true)
@Composable
fun WeatherItemPreview() {
    WeathererTheme {
        val screenWidth = 390.dp
        WeatherItem2(
            zoom = 1f,
            cardMaxHeight = dimensionResource(R.dimen.home_card_max_height),
            lazyColumnHeight = 1000f,
            roomWidth = screenWidth - 4 * dimensionResource(R.dimen.home_card_spacing),
            windowRotation = 0f,
            modifier = Modifier/*.wrapContentHeight(align = Alignment.Top, unbounded = true)*/,
            itemState = LocationsState(
                locationName = "اردبیل",
                currentTemperature = 23.6f,
                currentHumidity = 45f,
                currentWeatherCode = 99,
                currentWindSpeed = 19f,
                currentWindDirection = 112f,
                currentPrecipitationProbability = 22f,
                todayMaxTemperature = 29f,
                todayMinTemperature = -9f,
                clockSmallHandleRotation = 70f,
                clockBigHandleRotation = 120f,
                day = 24,
                month = "DEC",
                year = 2023
            ),
            pageNumber = 0,
            lazyParentState = rememberLazyListState(),
//            interactionSource = remember {
//                MutableInteractionSource()
//            },
            onNavigateToItem = {_, _, _ -> },
            appSettings = AppSettings(),
            onDeleteIconPressed = { _, _ -> },
            onEditItem = {_, _ ->},
            dialogDisplayed = false,
            toBeDeletedLocationId = -10,
            onDeleteItem = {}
        )
    }
}