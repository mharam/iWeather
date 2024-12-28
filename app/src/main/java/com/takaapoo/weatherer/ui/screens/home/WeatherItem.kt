package com.takaapoo.weatherer.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.theme.Lato
import com.takaapoo.weatherer.ui.theme.customColorScheme
import kotlin.math.roundToInt

/*

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationGraphicsApi::class)
@Composable
fun WeatherItem(
    zoom: Float,
    cardMaxHeight: Dp,
    lazyColumnHeight: Float,
    roomWidth: Dp,
    windowRotation: Float,
    modifier: Modifier = Modifier,
    itemState: LocationsState,
    pageNumber: Int,
    lazyParentState: LazyListState,
//    interactionSource: MutableInteractionSource,
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
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = dialogDisplayed){
        if (dialogDisplayed) {
            dragState.animateTo(
                targetValue = DragValue.Center,
            )
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .size(
                width = roomWidth,
                height = if (itemState.locationId == toBeDeletedLocationId) cardHeight else cardMaxHeight
            )
            .padding(
                horizontal = 2 * dimensionResource(R.dimen.home_card_spacing),
                vertical = dimensionResource(R.dimen.home_card_spacing)
            )
            .background(
                color = dragBackgroundColor(
                    try {
                        dragState.requireOffset() / DRAG_MAX_DISPLACE.toPx(density)
                    } catch (e: IllegalStateException) {
                        0f
                    }
                ),
                shape = RoundedCornerShape(
                    bottomStart = 0.12f * roomWidth,
                    topStart = 16.dp,
                    bottomEnd = 16.dp,
                    topEnd = 16.dp
                )
            )
            .clickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = null
            ) {
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
        val cardShape = RoundedCornerShape(
            bottomStart = 0.12f * roomWidth,
            topStart = 16.dp,
            bottomEnd = 16.dp,
            topEnd = 16.dp
        )
        Card(
//            enabled = dragState.currentValue == DragValue.Center,
            modifier = Modifier
                .fillMaxWidth()
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.customColorScheme.searchbarSurface),
        ){
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
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
                            .padding(top = roomWidth / 40)
                            .size(width = 0.3f * roomWidth, height = 0.3f * roomWidth)
                            .graphicsLayer {
                                scaleX = 3f / zoom
                                scaleY = 3f / zoom
                            },
                        alignment = Alignment.TopCenter,
                        contentScale = ContentScale.FillHeight
                    )
                    WeatherItemData(
                        modifier = Modifier
                            .padding(start = 8.dp, top = 0.59f * roomWidth)
                            .align(Alignment.TopStart),
                        humidity = itemState.currentHumidity,
                        precipitationProbability = itemState.currentPrecipitationProbability,
                        windSpeed = itemState.currentWindSpeed,
                        windDirection = itemState.currentWindDirection,
                        airQuality = itemState.currentAirQuality,
                        weatherCode = itemState.currentWeatherCode,
                        isDay = itemState.isDay,
                        moonType = itemState.moonType
                    )
                    Image(
                        painter = painterResource(R.drawable.room1_2),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth,
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = roomWidth / 40)
                            .size(width = 0.3f * roomWidth, height = 0.3f * roomWidth)
                            .align(Alignment.TopCenter)
                            .onGloballyPositioned {
                                if (zoom == 1f) {
                                    centerX = it.positionInRoot().x + it.size.width / 2
                                    centerY = it.positionInRoot().y + it.size.height / 2
                                }
                            }
                    ) {
//                            Image(
//                                painter = painterResource(R.drawable.window3),
//                                contentDescription = null,
//                                modifier = Modifier
//                                    .size(width = 0.3f * roomWidth, height = 0.3f * roomWidth),
//                                contentScale = ContentScale.FillBounds
//                            )
                        Image(
                            painter = painterResource(R.drawable.window4_c),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 0.0132f * roomWidth, end = 0.03f * roomWidth)
                                .size(width = 0.12f * roomWidth, height = 0.26f * roomWidth)
                                .graphicsLayer {
                                    transformOrigin =
                                        TransformOrigin(
                                            pivotFractionX = 1f,
                                            pivotFractionY = 0.5f
                                        )
                                    rotationY = windowRotation
                                    cameraDistance = 20f
                                },
                            contentScale = ContentScale.FillBounds
                        )
                        Image(
                            painter = painterResource(R.drawable.window5_c),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 0.0132f * roomWidth, start = 0.03f * roomWidth)
                                .size(width = 0.12f * roomWidth, height = 0.26f * roomWidth)
                                .graphicsLayer {
                                    transformOrigin =
                                        TransformOrigin(
                                            pivotFractionX = 0f,
                                            pivotFractionY = 0.5f
                                        )
                                    rotationY = -windowRotation
                                    cameraDistance = 20f
                                },
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    RoomClock(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(roomWidth / 24)
                            .width(roomWidth / 8),
                        roomWidth = roomWidth,
                        bigHandleRotation = itemState.clockBigHandleRotation,
                        smallHandleRotation = itemState.clockSmallHandleRotation,
                        dayClock = itemState.isDay,
                        am = itemState.am
                    )
                }
                RoomRibbon(
                    topPadding = 0.4f * roomWidth,
                    circleWidth = 0.24f * roomWidth,
                    locationName = itemState.locationName,
                    currentTemperature = itemState.currentTemperature,
                    maxTemperature = itemState.todayMaxTemperature,
                    minTemperature = itemState.todayMinTemperature,
                    temperatureUnit = appSettings.temperatureUnit,
                    day = itemState.day,
                    month = itemState.month,
                    year = itemState.year
                )
            }
        }
    }
}
*/

@Composable
fun RoomClock(
    modifier: Modifier = Modifier,
    roomWidth: Dp,
    bigHandleRotation: Float?,
    smallHandleRotation: Float?,
    dayClock: Boolean,
    am: Boolean?
) {
    Box(
        modifier = modifier.shadow(
            elevation = 4.dp,
            shape = CircleShape
        ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                if (dayClock) R.drawable.clock_day_no_hand_c else R.drawable.clock_night_no_hand_c
            ),
            contentDescription = "clock"
        )
        am?.let {
            Text(
                modifier = Modifier.offset(y = roomWidth / 36f),
                text = if (am) "AM" else "PM",
                fontSize = 8.sp,
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
                modifier = Modifier.graphicsLayer {
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
                modifier = Modifier.graphicsLayer {
                    rotationZ = bigHandleRotation
                }
            )
        }
    }
}

@Composable
fun BoxScope.RoomRibbon(
    topPadding: Dp,
    circleWidth: Dp,
    locationName: String,
    currentTemperature: Float?,
    maxTemperature: Float?,
    minTemperature: Float?,
    temperatureUnit: Temperature,
    day: Int,
    month: String,
    year: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .align(Alignment.TopStart)
            .padding(top = topPadding)
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .size(circleWidth)
                .align(Alignment.CenterEnd),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = maxTemperature?.toString() ?: "?",
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = minTemperature?.toString() ?: "?",
                color = Color.White,
                fontSize = 16.sp
            )
        }
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.CenterStart),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier.width(2.8f * circleWidth),
                text = locationName,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontFamily = Font(resId = R.font.lato_black, weight = FontWeight.Black).toFontFamily(),
                overflow = TextOverflow.Ellipsis
            )
//                Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$year, $month $day",
                fontFamily = Font(resId = R.font.cmu_typewriter_bold).toFontFamily(),
                fontWeight = FontWeight(500),
                color = Color.White,
                fontSize = 16.sp
            )
        }

        Row(
            modifier = modifier
                .padding(8.dp)
                .width(96.dp)
                .align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = if (currentTemperature != null) "${(currentTemperature*10).roundToInt() / 10f}" else "?",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Text(
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp),
                text = if (temperatureUnit == Temperature.CELSIUS) "C" else "F",
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun WeatherItemData(
    modifier: Modifier = Modifier,
    humidity: Float?,
    precipitationProbability: Float?,
    windSpeed: Float?,
    windDirection: Float?,
    airQuality: Int?,
    weatherCode: Int?,
    isDay: Boolean,
    moonType: Int?
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.weather_item_data_height))
            .background(color = MaterialTheme.customColorScheme.searchbarSurface)
            .padding(top = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        WeatherTypeData(
            modifier = Modifier,
            weatherCode = weatherCode,
            isDay = isDay,
            moonType = moonType
        )
        Spacer(modifier = Modifier.weight(1f))
        AirQualityData(usAQI = airQuality)
        HumidityData(humidity = humidity)
        PrecipitationData(precipitationProbability = precipitationProbability)
        WindData(windSpeed = windSpeed, windDirection = windDirection)
    }
}

@Composable
fun WeatherTypeData(
    modifier: Modifier = Modifier,
    weatherCode: Int?,
    isDay: Boolean,
    moonType: Int?
) {
    val weatherType = WeatherType.fromWMO(weatherCode, isDay, moonType)
    ComposeWithTextToolTip(tooltipText =  weatherType.weatherDesc){
        WeatherType.WeatherIcon(
            modifier = modifier.size(80.dp),
            weatherType = weatherType
        )
    }
}


@Composable
fun HumidityData(
    modifier: Modifier = Modifier,
    humidity: Float?
) {
    ComposeWithTextToolTip(tooltipText = "Humidity"){
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.button2_c),
                    contentDescription = "temperature",
                    modifier = Modifier.size(40.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.humidity),
                    contentDescription = "temperature",
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = if (humidity != null) "${humidity.roundToInt()}%" else "?",
                fontSize = 14.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun WindData(
    modifier: Modifier = Modifier,
    windSpeed: Float?,
    windDirection: Float?
) {
    ComposeWithTextToolTip(tooltipText = "Wind"){
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.button2_c),
                    contentDescription = "wind",
                    modifier = Modifier.size(40.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.wind),
                    contentDescription = "temperature",
                    modifier = Modifier.size(24.dp)
                )
                if (windDirection != null) {
                    Image(
                        painter = painterResource(R.drawable.wind_arrow2_c),
                        contentDescription = "wind",
                        modifier = Modifier
                            .size(52.dp)
                            .graphicsLayer {
                                rotationZ = windDirection + 180
                            }
                    )
                }
            }
            Text(
                text = if (windSpeed != null) "${windSpeed.roundToInt()}\nkm/h" else "?",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
//            Text(text = "km/h", fontSize = 14.sp)
        }
    }
}

@Composable
fun PrecipitationData(
    modifier: Modifier = Modifier,
    precipitationProbability: Float?
) {
    ComposeWithTextToolTip(tooltipText = "Precipitation Probability"){
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.button2_c),
                    contentDescription = "temperature",
                    modifier = Modifier.size(40.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.precipitation),
                    contentDescription = "temperature",
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = if (precipitationProbability != null) "${precipitationProbability.roundToInt()}%" else "?",
                fontSize = 14.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun AirQualityData(
    modifier: Modifier = Modifier,
    usAQI: Int?
) {
    ComposeWithTextToolTip(tooltipText = "Air Quality Index"){
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.button2_c),
                    contentDescription = "temperature",
                    modifier = Modifier.size(40.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.mask),
                    contentDescription = "temperature",
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = usAQI?.toString() ?: "?",
                fontSize = 14.sp,
                lineHeight = 16.sp
            )
        }
    }
}

fun dragBackgroundColor(offset: Float): Color {
    return if (offset > 0)
        Color(1f, 0.13f, 0.42f, offset.coerceIn(0f, 1f))
    else
        Color(0.3f, 1f, 0.32f, (-offset).coerceIn(0f, 1f))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeWithTextToolTip(
    modifier: Modifier = Modifier,
    tooltipText: String,
    composeFunction: @Composable () -> Unit
) {
    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = rememberTooltipState()
    ){
        composeFunction()
    }
}

/*

@Preview(showBackground = true)
@Composable
fun WeatherItemPreview() {
    WeathererTheme {
        val screenWidth = 390.dp
        WeatherItem(
            zoom = 1f,
            cardMaxHeight = dimensionResource(R.dimen.weather_item_data_height) +
                    0.6f * (screenWidth - 4 * dimensionResource(R.dimen.home_card_spacing)) +
                    2 * dimensionResource(R.dimen.home_card_spacing),
            lazyColumnHeight = 1000f,
            roomWidth = screenWidth - 4 * dimensionResource(R.dimen.home_card_spacing),
            windowRotation = 0f,
            modifier = Modifier.wrapContentHeight(align = Alignment.Top, unbounded = true),
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
}*/
