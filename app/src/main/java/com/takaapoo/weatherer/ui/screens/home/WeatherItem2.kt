package com.takaapoo.weatherer.ui.screens.home

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.constraintlayout.compose.ConstraintLayout
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.LocationsState
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.pathInnerShadow
import com.takaapoo.weatherer.ui.screens.home.shapes.CardShape
import com.takaapoo.weatherer.ui.screens.home.shapes.ThermometerShape
import com.takaapoo.weatherer.ui.theme.BarBlue
import com.takaapoo.weatherer.ui.theme.BarRed
import com.takaapoo.weatherer.ui.theme.Glass
import com.takaapoo.weatherer.ui.theme.Gray20
import com.takaapoo.weatherer.ui.theme.Gray60
import com.takaapoo.weatherer.ui.theme.HomeCardDay
import com.takaapoo.weatherer.ui.theme.HomeCardNight
import com.takaapoo.weatherer.ui.theme.Lato
import com.takaapoo.weatherer.ui.theme.LoadingData
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.TransparentGray50
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.utility.BorderedText
import com.takaapoo.weatherer.ui.utility.toPx
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationGraphicsApi::class)
@Composable
fun WeatherItem2(
//    zoom: Float,
    cardMaxHeight: Dp,
//    windowRotation: Float,
    modifier: Modifier = Modifier,
    itemState: LocationsState,
    pageNumber: Int,
    onNavigateToItem: (cardX: Float, cardY: Float, cardWidth: Float) -> Unit,
    appSettings: AppSettings,
    dataLoadingStatus: MyResult<Unit> = MyResult.Error(),
    onDeleteIconPressed: (id: Int, name: String) -> Unit,
    onEditItem: (id: Int, name: String) -> Unit,
    dialogDisplayed: Boolean,
    toBeDeletedLocationId: Int,
    onDeleteItem: (id: Int) -> Unit,
    onReloadWeatherData: () -> Unit
) {
//    Log.i("comp1", "WeatherItem2: ${itemState.locationName}")
    val density : Density = LocalDensity.current
    var cardX by rememberSaveable { mutableFloatStateOf(0f) }
    var cardY by rememberSaveable { mutableFloatStateOf(0f) }
    var cardWidth by rememberSaveable { mutableFloatStateOf(0f) }
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
        saver = AnchoredDraggableState.Saver(
            snapAnimationSpec = animationSpec,
            decayAnimationSpec = exponentialDecay(),
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold
        )
    ) {
        AnchoredDraggableState(
//            anchors = anchors,
            initialValue = DragValue.Center,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold,
            snapAnimationSpec = animationSpec,
            decayAnimationSpec = exponentialDecay()
        )
    }.apply { updateAnchors(anchors) }

    LaunchedEffect(key1 = dialogDisplayed){
        if (dialogDisplayed)
            dragState.animateTo(targetValue = DragValue.Center)
    }
    Box(
        modifier = modifier
            .widthIn(
                min = dimensionResource(R.dimen.home_card_min_width),
                max = dimensionResource(R.dimen.home_card_max_width)
            )
            .fillMaxWidth()
            .height(if (itemState.locationId == toBeDeletedLocationId) cardHeight else cardMaxHeight)
            .onGloballyPositioned {
//                if (zoom == 1f) {
                cardWidth = it.size.width.toFloat()
                val cardPosition = it.parentLayoutCoordinates?.positionInParent()
                cardX = cardPosition?.x ?: 0f
                cardY = cardPosition?.y ?: 0f
//                }
            }
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
                contentDescription = "delete",
                tint = Gray60
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
                contentDescription = "edit",
                tint = Gray60
            )
        }

        val cardShape = CardShape(
            cardOverHead = dimensionResource(R.dimen.home_card_top_over_head).toPx(density),
            topRound = dimensionResource(R.dimen.home_card_corner_radius).toPx(density),
            overHeadDistance = dimensionResource(R.dimen.home_card_overhead_distance).toPx(density)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = if (/*zoom == 1f && */!dragState.offset.isNaN()) dragState.offset.roundToInt()
                        else 0,
                        y = 0
                    )
                }
                .shadow(
                    elevation = 2.dp,
                    shape = cardShape
                )
                .clip(cardShape)
                .background(
                    color = HomeCardDay,
                    shape = cardShape
                )
                .anchoredDraggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    enabled = /*zoom == 1f*/ true
                )

        ) {
            if (dataLoadingStatus !is MyResult.Success) {
                CardTitle(
                    modifier = Modifier.align(Alignment.TopCenter),
                    locationName = itemState.locationName,
                    color = HomeCardNight
                )
            }
            when (dataLoadingStatus) {
                is MyResult.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Icon(
                            modifier = Modifier.size(48.dp),
                            painter = painterResource(R.drawable.cloud_off),
                            tint = LoadingData,
                            contentDescription = "Error"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading data failed!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight(500),
                            color = LoadingData
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onReloadWeatherData) {
                            Text(
                                text = "Reload",
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = FontFamily.Default
                            )
                        }
                    }
                }
                is MyResult.Loading -> {
                    val infiniteTransition = rememberInfiniteTransition(label = "loading data")
                    val dotCount by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 3.99f,
                        animationSpec =  infiniteRepeatable(tween(
                            durationMillis = 1200,
                            easing = LinearEasing
                        )),
                        label = "loading..."
                    )
                    var loadingText = "Loading data "
                    repeat(
                        times = dotCount.toInt(),
                        action = { loadingText += "." }
                    )
                    Box(
                        modifier = Modifier.align(Alignment.Center)
                    ){
                        Text(
                            text = "Loading data ...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight(500),
                            color = Transparent
                        )
                        Text(
                            text = loadingText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight(500),
                            color = LoadingData
                        )
                    }
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimensionResource(R.dimen.home_card_overhead_distance) +
                                        dimensionResource(R.dimen.home_card_corner_radius)
                            )
                            .align(Alignment.TopCenter),
                        color = LoadingData,
                        strokeCap = StrokeCap.Round
                    )
                }
                is MyResult.Success -> {
                    Image(
                        painter = painterResource(
                            when(pageNumber){
                                0 -> R.drawable.ax2_c
                                1 -> R.drawable.ax3_c
                                else -> R.drawable.ax4_c
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        contentScale = ContentScale.FillHeight
                    )
                    val textColor = if (itemState.isDay) HomeCardNight else HomeCardDay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = dragState.currentValue == DragValue.Center) {
                                if (dragState.targetValue == DragValue.Center) {
                                    onNavigateToItem(cardX, cardY, cardWidth)
                                }
                            }
                    ) {
                        WindowLeft(
                            modifier = Modifier.fillMaxSize(),
                            isDay = itemState.isDay
                        )
                        WindowLeft(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(scaleX = -1f, scaleY = 1f),
                            isDay = itemState.isDay
                        )
                        CardTitle(
                            modifier = Modifier.align(Alignment.TopCenter),
                            locationName = itemState.locationName,
                            color = textColor
                        )
                        val weatherType = WeatherType.fromWMO(
                            code = itemState.currentWeatherCode,
                            isDay = itemState.isDay,
                            moonType = itemState.moonType
                        )
                        WeatherTypeData2(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = dimensionResource(R.dimen.home_card_top_over_head))
                                .size(2 * dimensionResource(R.dimen.home_card_clock_round)),
                            weatherType = weatherType
                        )
                        Clock(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = dimensionResource(R.dimen.home_card_top_over_head))
                                .size(2 * dimensionResource(R.dimen.home_card_clock_round)),
                            bigHandleRotation = itemState.clockBigHandleRotation,
                            smallHandleRotation = itemState.clockSmallHandleRotation,
                            dayClock = itemState.isDay,
                            am = itemState.am
                        )
                        WeatherData(
                            modifier = Modifier.fillMaxSize(),
                            currentTemperature = itemState.currentTemperature,
                            maxTemperature = itemState.todayMaxTemperature,
                            minTemperature = itemState.todayMinTemperature,
                            humidity = itemState.currentHumidity,
                            precipitationProbability = itemState.currentPrecipitationProbability,
                            windSpeed = itemState.currentWindSpeed,
                            windDirection = itemState.currentWindDirection,
                            airQuality = itemState.currentAirQuality,
                            appSettings = appSettings,
                            isDay = itemState.isDay
                        )
                        Text(
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.home_card_top_over_head))
                                .align(Alignment.TopStart)
                                .graphicsLayer(
                                    transformOrigin = TransformOrigin(0f, 1f),
                                    rotationZ = 90f
                                )
                                .padding(
                                    start = 2 * dimensionResource(R.dimen.home_card_clock_round) + 8.dp
                                )
                                .wrapContentHeight(align = Alignment.CenterVertically)
                            ,
                            text = "${itemState.dayOfWeek}., ${itemState.month} ${itemState.day}, ${itemState.year}",
                            fontFamily = Font(resId = R.font.cmu_typewriter_bold).toFontFamily(),
                            fontSize = 14.sp,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WindowLeft(
    modifier: Modifier = Modifier,
    isDay: Boolean
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
            lineTo(x = size.width / 2 + 5, y = 0f)
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
            color = if (isDay) HomeCardDay else HomeCardNight
        )
        drawPath(
            path = innerPath,
            color = Color.Black,
            blendMode = BlendMode.Clear
        )
        pathInnerShadow(
            path = innerPath,
            color = TransparentGray50,
            blur = 4.dp
        )
    }
}

@Composable
fun CardTitle(
    modifier: Modifier = Modifier,
    locationName: String,
    color: Color
) {
    Text(
        modifier = modifier
            .padding(horizontal = 2 * dimensionResource(R.dimen.home_card_clock_round))
            .fillMaxWidth()
            .height(2 * dimensionResource(R.dimen.home_card_top_over_head))
            .wrapContentHeight(align = Alignment.CenterVertically),
        text = locationName,
        fontSize = 20.sp,
        fontFamily = Font(resId = R.font.cmu_demi_condensed).toFontFamily(),
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        color = color
    )
}

@Composable
fun WeatherData(
    modifier: Modifier = Modifier,
    currentTemperature: Float?,
    maxTemperature: Float?,
    minTemperature: Float?,
    humidity: Float?,
    precipitationProbability: Float?,
    windSpeed: Float?,
    windDirection: Float?,
    airQuality: Int?,
    appSettings: AppSettings,
    isDay: Boolean
) {
    val color = if (isDay) HomeCardNight else HomeCardDay
    val startEndPadding = (dimensionResource(R.dimen.home_card_top_over_head) -
            dimensionResource(R.dimen.home_card_icons_radius)).coerceAtLeast(0.dp)
    ConstraintLayout(
        modifier = modifier.padding(
            start = startEndPadding,
            top = 2 * dimensionResource(R.dimen.home_card_top_over_head),
            end = startEndPadding,
        )
    ) {
        val (column1, column2, column3, column4, column5) = createRefs()
        val columnGuidelines = List(size = 5){ i ->
            createGuidelineFromStart((i + 1) / 6f)
        }
        AirQualityData2(
            modifier = Modifier.constrainAs(column1){
                centerAround(columnGuidelines[0])
                bottom.linkTo(parent.bottom)
            },
            usAQI = airQuality,
            color = color
        )
        HumidityData2(
            modifier = Modifier.constrainAs(column2){
                centerAround(columnGuidelines[1])
                bottom.linkTo(parent.bottom)
            },
            humidity = humidity,
            color = color
        )
        TemperatureData2(
            modifier = Modifier.constrainAs(column3){
                centerAround(columnGuidelines[2])
                bottom.linkTo(parent.bottom)
            },
            currentTemperature = currentTemperature?.coerceIn(minTemperature, maxTemperature),
            maxTemperature = maxTemperature,
            minTemperature = minTemperature,
            appSettings = appSettings,
            color = color,
            isDay = isDay
        )
        PrecipitationData2(
            modifier = Modifier.constrainAs(column4){
                centerAround(columnGuidelines[3])
                bottom.linkTo(parent.bottom)
            },
            precipitationProbability = precipitationProbability,
            color = color
        )
        WindData2(
            modifier = Modifier.constrainAs(column5){
                centerAround(columnGuidelines[4])
                bottom.linkTo(parent.bottom)
            },
            windSpeed = windSpeed,
            windDirection = windDirection,
            appSettings = appSettings,
            color = color
        )
    }
}

@Composable
fun TemperatureData2(
    modifier: Modifier = Modifier,
    currentTemperature: Float?,
    maxTemperature: Float?,
    minTemperature: Float?,
    appSettings: AppSettings,
    color: Color,
    isDay: Boolean
) {
    val density = LocalDensity.current
    val currentTempString = currentTemperature?.let { "%.1f".format(it) }
    val minTempString = minTemperature?.let { "%.1f".format(it) }
    val maxTempString = maxTemperature?.let { "%.1f".format(it) }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val thermoShape = ThermometerShape(
            bottomRadius = dimensionResource(R.dimen.home_card_thermo_bottom_radius).toPx(density),
            topRadius = dimensionResource(R.dimen.home_card_thermo_top_radius).toPx(density)
        )
        Box(Modifier.weight(1f)) {
            val mercuryBottomSmallness = 1.5.dp
            if (minTemperature != null && maxTemperature != null && currentTemperature != null) {
                TemperatureMercury(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = mercuryBottomSmallness)
                        .fillMaxHeight()
                        .width(
                            2 * (dimensionResource(R.dimen.home_card_thermo_bottom_radius) -
                                    mercuryBottomSmallness)
                        ),
                    bottomRadius = (dimensionResource(R.dimen.home_card_thermo_bottom_radius) -
                            mercuryBottomSmallness).toPx(density),
                    topRadius = (dimensionResource(R.dimen.home_card_thermo_top_radius) - 1.dp).toPx(
                        density
                    ),
                    minTemperature = minTemperature,
                    maxTemperature = maxTemperature,
                    currentTemperature = currentTemperature
                )
            }
            Box(
                modifier = Modifier
                    .width(2 * dimensionResource(R.dimen.home_card_thermo_bottom_radius))
                    .fillMaxHeight()
                    .background(
                        color = Glass,
                        shape = thermoShape
                    )
                    .border(
                        width = 1.3.dp,
                        color = Gray20,
                        shape = thermoShape
                    )
                    .align(Alignment.Center)
            )
            val textHeight = 24.dp
            val tempWidth = 156.dp
            val tempHeight = 64.dp
            val maxTempHeight = 48.dp
//            val borderColor = if (isDay) HomeCardDay else HomeCardNight
            minTempString?.let {
                val shape = RoundedCornerShape(
                    topStart = textHeight/2,
                    topEnd = textHeight/2,
                    bottomEnd = textHeight/2,
                    bottomStart = 0.dp
                )
                Row(
                    modifier = Modifier
                        .width(tempWidth)
                        .height(tempHeight)
                        .align(Alignment.BottomCenter),
                    verticalAlignment = Alignment.Top
                ) {
                    val startHeight = 2 * dimensionResource(R.dimen.home_card_thermo_bottom_radius) +
                            dimensionResource(R.dimen.home_card_mercury_space_to_end) - mercuryBottomSmallness
                    val startOffset = dimensionResource(R.dimen.home_card_thermo_top_radius)
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        val widthPx = tempWidth.toPx()
                        val linePath = Path().apply {
                            moveTo(x = widthPx/2 + startOffset.toPx(), y = size.height - startHeight.toPx())
                            relativeLineTo(dx = 16.dp.toPx(), dy = 0f)
                            lineTo(x = size.width + 1, y = textHeight.toPx() - 1)
                        }
                        drawPath(
                            path = linePath,
                            brush = Brush.horizontalGradient(
                                colors = if (isDay) listOf(HomeCardNight, HomeCardDay)
                                else listOf(HomeCardDay, HomeCardDay) ,
                                startX = widthPx/2
                            ),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                    BorderedText(
                        modifier = Modifier
                            .height(textHeight)
                            .background(
                                color = HomeCardDay.copy(alpha = 0.7f),
                                shape = shape
                            )
                            .border(width = 1.dp, color = Color.White, shape = shape)
                            .padding(horizontal = 8.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        text = AnnotatedString("${it}°"),
                        fontSize = 15.sp,
                        fillColor = BarBlue,
                        strokeWidth = 2f
                    )
                }
            }
            maxTempString?.let {
                val shape = RoundedCornerShape(
                    topStart = textHeight/2,
                    topEnd = 0.dp,
                    bottomEnd = textHeight/2,
                    bottomStart = textHeight/2
                )
                Row(
                    modifier = Modifier
                        .width(tempWidth)
                        .height(maxTempHeight)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.Bottom
                ) {
                    BorderedText(
                        modifier = Modifier
                            .height(textHeight)
                            .background(
                                color = HomeCardDay.copy(alpha = 0.7f),
                                shape = shape
                            )
                            .border(width = 1.dp, color = Color.White, shape = shape)
                            .padding(horizontal = 8.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        text = AnnotatedString("${it}°"),
                        fontSize = 15.sp,
                        fillColor = BarRed,
                        strokeWidth = 2f
                    )
                    val startHeight = dimensionResource(R.dimen.home_card_mercury_space_to_end) +
                            dimensionResource(R.dimen.home_card_thermo_top_radius)
                    val startOffset = dimensionResource(R.dimen.home_card_thermo_top_radius)
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        val widthPx = tempWidth.toPx()
                        val linePath = Path().apply {
                            moveTo(x = size.width - widthPx/2 - startOffset.toPx(), y = startHeight.toPx())
                            relativeLineTo(dx = -16.dp.toPx(), dy = 0f)
                            lineTo(x = -1f, y = (maxTempHeight - textHeight).toPx() + 1)
                        }
                        drawPath(
                            path = linePath,
                            brush = Brush.horizontalGradient(
                                colors = if (isDay) listOf(HomeCardDay, HomeCardNight)
                                else listOf(HomeCardDay, HomeCardDay),
                                endX = size.width - widthPx/2 - startOffset.toPx()
                            ),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        BorderedText(
            modifier = Modifier
                .height(28.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
            text = if (currentTemperature != null)
                AnnotatedString("${currentTempString}${WeatherQuantity.TEMPERATURE.unit(appSettings)}")
            else AnnotatedString("?"),
            fontSize = 14.sp,
            fillColor = color,
            strokeColor = color,
            strokeWidth = 1.5f
        )
    }
}

@Composable
fun TemperatureMercury(
    modifier: Modifier = Modifier,
    bottomRadius: Float,
    topRadius: Float,
    minTemperature: Float,
    maxTemperature: Float,
    currentTemperature: Float
) {
    val spaceToEnd = dimensionResource(R.dimen.home_card_mercury_space_to_end)
    Canvas(
        modifier = modifier
    ){
        val width = size.width
        val height = size.height
        val bottomAngle = asin(topRadius / bottomRadius)
        val tempRange = maxTemperature - minTemperature
        val minTempHeight = height - 2 * bottomRadius - spaceToEnd.toPx()
        val maxTempHeight = topRadius + spaceToEnd.toPx()
        val currentTempHeight = minTempHeight + ((currentTemperature - minTemperature)/tempRange) *
                (maxTempHeight - minTempHeight)

        val path = Path().apply {
            moveTo(x = size.width/2 - topRadius, y = height - bottomRadius * (1 + cos(bottomAngle)))
            arcToRad(
                rect = Rect(
                    center = Offset(width/2, height - bottomRadius),
                    radius = bottomRadius
                ),
                startAngleRadians = 3 * PI.toFloat() / 2 - bottomAngle,
                sweepAngleRadians = -2 * (PI.toFloat() - bottomAngle),
                forceMoveTo = false
            )
            lineTo(x = width/2 + topRadius, y = currentTempHeight)
            relativeLineTo(dx = -2 * topRadius, dy = 0f)
            close()
        }

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(BarBlue, BarRed),
                startY = minTempHeight,
                endY = maxTempHeight
            )
        )
    }
}

@Composable
fun AirQualityData2(
    modifier: Modifier = Modifier,
    usAQI: Int?,
    color: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ComposeWithTextToolTip(
            tooltipText = "Air Quality Index"
        ) {
            Icon(
                painter = painterResource(R.drawable.mask),
                contentDescription = "temperature",
                modifier = Modifier
                    .width(2 * dimensionResource(R.dimen.home_card_icons_radius))
                    .padding(horizontal = 8.dp),
                tint = color
            )
        }
        Text(
            modifier = Modifier
                .height(28.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
            text = usAQI?.toString() ?: "?",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = color
        )
    }
}

@Composable
fun HumidityData2(
    modifier: Modifier = Modifier,
    humidity: Float?,
    color: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ComposeWithTextToolTip(
            tooltipText = "Humidity"
        ) {
            Icon(
                painter = painterResource(R.drawable.humidity),
                contentDescription = "humidity",
                modifier = Modifier
                    .width(2 * dimensionResource(R.dimen.home_card_icons_radius))
                    .padding(horizontal = 8.dp),
                tint = color
            )
        }
        Text(
            modifier = Modifier
                .height(28.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
            text = if (humidity != null) "${humidity.roundToInt()}%" else "?",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = color
        )
    }
}

@Composable
fun PrecipitationData2(
    modifier: Modifier = Modifier,
    precipitationProbability: Float?,
    color: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ComposeWithTextToolTip(
            tooltipText = "Precipitation Probability"
        ) {
            Icon(
                painter = painterResource(R.drawable.precipitation),
                contentDescription = "humidity",
                modifier = Modifier
                    .width(2 * dimensionResource(R.dimen.home_card_icons_radius))
                    .padding(horizontal = 8.dp),
                tint = color
            )
        }
        Text(
            modifier = Modifier
                .height(28.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
            text = if (precipitationProbability != null) "${precipitationProbability.roundToInt()}%" else "?",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = color
        )
    }
}

@Composable
fun WindData2(
    modifier: Modifier = Modifier,
    windSpeed: Float?,
    windDirection: Float?,
    appSettings: AppSettings,
    color: Color
) {
    Box(
        modifier = modifier.height(dimensionResource(R.dimen.home_card_icons_radius) +
                dimensionResource(R.dimen.home_card_bottom_border))
    ) {
        if (windDirection != null) {
            Image(
                painter = painterResource(R.drawable.wind_arrow3),
                contentDescription = "wind",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(2 * dimensionResource(R.dimen.home_card_icons_radius))
                    .graphicsLayer {
                        rotationZ = windDirection + 180
                    }
            )
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ComposeWithTextToolTip(
                tooltipText = "Wind"
            ) {
                Icon(
                    painter = painterResource(R.drawable.wind),
                    contentDescription = "wind",
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            }
            Text(
                modifier = Modifier
                    .height(28.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                text = if (windSpeed != null) "${windSpeed.roundToInt()}${WeatherQuantity.WINDSPEED.unit(appSettings)}"
                else "?",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = color
            )
        }
    }
}


@Composable
fun WeatherTypeData2(
    modifier: Modifier = Modifier,
    weatherType: WeatherType
) {
    Box(modifier = modifier.fillMaxSize()) {
        ComposeWithTextToolTip(tooltipText =  weatherType.weatherDesc) {
            WeatherType.WeatherIcon(
                modifier = Modifier.padding(4.dp),
                weatherType = weatherType
            )
        }
    }
}


@Composable
fun Clock(
    modifier: Modifier = Modifier,
    bigHandleRotation: Float?,
    smallHandleRotation: Float?,
    dayClock: Boolean,
    am: Boolean?
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = /*if (dayClock) HomeCardDay else HomeCardNight*/Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.card_clock_ticks),
            contentDescription = "clock",
            colorFilter = ColorFilter.tint(if (dayClock) HomeCardNight else HomeCardDay)
        )
        am?.let {
            Text(
                modifier = Modifier
                    .padding(top = 0.8f * dimensionResource(R.dimen.home_card_clock_round)),
                text = if (am) "A" else "P",
                fontSize = 12.sp,
                fontFamily = Lato,
                color = if (dayClock) Color.Black else Color.White,
                letterSpacing = 0.sp
            )
        }
        smallHandleRotation?.let {
            Image(
                painter = painterResource(
                    if (dayClock) R.drawable.small_handle_day_c else R.drawable.small_handle_night_c
                ),
                contentDescription = "clock",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = smallHandleRotation
                    }
            )
        }
        bigHandleRotation?.let {
            Image(
                painter = painterResource(
                    if (dayClock) R.drawable.big_handle_day_c else R.drawable.big_handle_night_c
                ),
                contentDescription = "clock",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = bigHandleRotation
                    }
            )
        }
    }
}




@Preview(showBackground = true)
@Composable
fun WeatherItemPreview() {
    WeathererTheme {
        WeatherItem2(
//            zoom = 1f,
            cardMaxHeight = dimensionResource(R.dimen.home_card_max_height),
//            lazyColumnHeight = 1000f,
//            windowRotation = 0f,
            modifier = Modifier/*.wrapContentHeight(align = Alignment.Top, unbounded = true)*/,
            itemState = LocationsState(
                locationName = "اردبیل",
                currentTemperature = 23.6f,
                currentHumidity = 45f,
                currentWeatherCode = 99,
                currentWindSpeed = 19f,
                currentWindDirection = 112f,
                currentPrecipitationProbability = 22f,
                currentAirQuality = 138,
                todayMaxTemperature = 29f,
                todayMinTemperature = -9f,
                clockSmallHandleRotation = 70f,
                clockBigHandleRotation = 120f,
                am = true,
                day = 24,
                month = "DEC",
                year = 2023
            ),
            pageNumber = 0,
//            lazyParentState = rememberLazyListState(),
            onNavigateToItem = {_, _, _ -> },
            appSettings = AppSettings(),
            onDeleteIconPressed = { _, _ -> },
            onEditItem = {_, _ ->},
            dialogDisplayed = false,
            toBeDeletedLocationId = -10,
            onDeleteItem = {},
            onReloadWeatherData = {}
        )
    }
}