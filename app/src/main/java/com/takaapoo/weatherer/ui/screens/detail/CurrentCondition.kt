package com.takaapoo.weatherer.ui.screens.detail

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.WeatherType.Companion.calculateMoonType
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.daySeconds
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.GraphTypes
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.cubicCurveXtoY
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.linearCurveXtoY
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.quantityData
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.stepCurveXtoY
import com.takaapoo.weatherer.ui.theme.Gray60
import com.takaapoo.weatherer.ui.theme.WeatherGaugeBackground
import com.takaapoo.weatherer.ui.theme.WeatherGaugeHandle
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.utility.BorderedText
import com.takaapoo.weatherer.ui.utility.toKmph
import com.takaapoo.weatherer.ui.utility.toPx
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

const val valueContainerAlpha = 0.3f

@JvmName("AnnotatedStringInputMethod")
@Composable
fun CurrentCondition(
    modifier: Modifier = Modifier,
    weatherQuantity: ImmutableList<WeatherQuantity>,
    currentDayHourlyWeather: ImmutableList<LocalHourlyWeather>,
    currentDayHourlyAirQuality: ImmutableList<LocalAirQuality>? = null,
    targetX: Float,
    appSettings: AppSettings,
    topBannerHeight: Float,
    scroll: Float = 0f
) {
    val leftPoints by remember {
        mutableStateOf(Array(size = weatherQuantity.size){Offset.Zero})
    }
    val leftPoints2 by remember {
        mutableStateOf(Array(size = weatherQuantity.size){Offset.Zero})
    }
    var rightPoint by remember { mutableStateOf(Offset.Zero) }
    val textMiddleX by remember {
        mutableStateOf(Array(size = weatherQuantity.size){0f})
    }
    var boxTopY by remember { mutableFloatStateOf(0f) }
    var boxWidth by remember { mutableFloatStateOf(0f) }
    val iconSize = dimensionResource(id = R.dimen.current_condition_icon_size)
    val density = LocalDensity.current
    val spaceOccupiedByValue = (dimensionResource(id = R.dimen.current_condition_value_width)
            + 32.dp).toPx(density)
    val topPadding = if (weatherQuantity.size > 1 &&
        4*textMiddleX[0] > (boxWidth - spaceOccupiedByValue)) 8.dp else 0.dp

    val firstPointX = 0f
    val lastPointX = currentDayHourlyWeather.size.toFloat() - 1


    Box(
        modifier = modifier
            .onGloballyPositioned {
                boxTopY = it.positionInParent().y + scroll
                boxWidth = it.size.width.toFloat()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding)
        ) {
            weatherQuantity.forEachIndexed { index, quantity ->
                val quantityName =
                    quantity.title ?: AnnotatedString(stringResource(id = quantity.nameId!!))
//                val (controlPoints1, controlPoints2) = quantityControlPoints(
//                    weatherData = detailState.currentDayHourlyWeather,
//                    airData = detailState.currentDayHourlyAirQuality,
//                    weatherQuantity = quantity
//                )
                val data = quantityData(
                    weatherData = currentDayHourlyWeather,
                    airData = currentDayHourlyAirQuality,
                    weatherQuantity = quantity
                )
                val quantityValue = when (quantity.graphType()){
//                    GraphTypes.CUBIC -> cubicCurveXtoY(
//                        data = data,
//                        controlPoints1 = controlPoints1,
//                        controlPoints2 = controlPoints2,
//                        firstPointX = firstPointX,
//                        lastPointX = lastPointX,
//                        targetX = detailState.targetX
//                    )?.coerceAtLeast(
//                        if (quantity == WeatherQuantity.DEWPOINT || quantity == WeatherQuantity.FREEZINGLEVELHEIGHT)
//                            -Float.MAX_VALUE
//                        else 0f
//                    )
                    GraphTypes.CUBIC, GraphTypes.LINEAR -> linearCurveXtoY(
                        data = data,
                        firstPointX = firstPointX,
                        lastPointX = lastPointX,
                        targetX = targetX
                    )
                    GraphTypes.STEP -> stepCurveXtoY(
                        data = data,
                        firstPointX = firstPointX,
                        lastPointX = lastPointX,
                        targetX = targetX
                    )
                }/*?.toAppropriateUnit(quantity, appSettings)*/
                val quantityValueText = buildAnnotatedString {
                    append(
                        if (quantityValue != null) {
                            if (quantityValue == 0f) "0"
                            else "%.${quantity.floatingPointDigits(appSettings)}f".format(quantityValue)
                        } else "?"
                    )
                    append(quantity.unit(appSettings = appSettings))
                }

                val isOnSeparateRow = 4*textMiddleX[index] > (boxWidth - spaceOccupiedByValue)
                if (isOnSeparateRow) {
                    QuantityName(
                        onUpdateTextMiddleX = { value ->
                            textMiddleX[index] = value
                        },
                        text = quantityName
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.current_condition_value_height)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isOnSeparateRow) {
                        SingleConnector(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .onGloballyPositioned {
                                    leftPoints[index] = Offset(0.75f * it.size.width, 0f)
                                },
                            color = MaterialTheme.colorScheme.primaryContainer,
                            leftPoint = Offset(
                                textMiddleX[index].coerceAtMost(leftPoints[index].x),
                                leftPoints[index].y
                            ),
                        )
                    } else {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QuantityName(
                                onUpdateTextMiddleX = {},
                                text = quantityName
                            )
                            SingleConnector(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            )
                        }
                    }
                    Text(
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.current_condition_value_width))
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer/*.copy(alpha = valueContainerAlpha)*/,
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .onGloballyPositioned {
                                val parentTop =
                                    it.parentLayoutCoordinates?.positionInParent()?.y?.plus(
                                        topPadding.toPx(density)
                                    ) ?: 0f
                                val y = it.size.height / 2 + it.positionInParent().y + parentTop
                                leftPoints2[index] = Offset(
                                    x = it.size.width + it.positionInParent().x,
                                    y = y
                                )
                                if (index == 0)
                                    rightPoint = Offset(
                                        x = (it.parentLayoutCoordinates?.size?.width ?: 0) - iconSize.toPx(density),
                                        y = (y + (topBannerHeight - boxTopY - parentTop)
                                            .coerceAtLeast(0f))
                                            .coerceAtMost(leftPoints2.last().y)
                                    )
                            }
                            .border(
                                width = dimensionResource(id = R.dimen.connector_line_width),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .padding(8.dp),
                        text = quantityValueText,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        MultiConnectorLeft(
            modifier = Modifier.fillMaxSize(),
            leftPoints = leftPoints2.toList(),
            rightPoint = rightPoint
        )
        Icon(
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    translationY = rightPoint.y - iconSize.toPx(density) / 2
                }
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .border(
                    width = dimensionResource(id = R.dimen.connector_line_width),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
                .padding(12.dp)
                .align(Alignment.TopEnd),
            painter = painterResource(id = weatherQuantity.first().iconId),
            contentDescription = "Precipitation",
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun QuantityName(
    modifier: Modifier = Modifier,
    onUpdateTextMiddleX: (Float) -> Unit = {},
    onUpdateTextMiddlePoint: (Offset) -> Unit = {},
    text: AnnotatedString
) {
    Text(
        modifier = modifier
//            .height(dimensionResource(id = R.dimen.current_condition_param_height))
            .defaultMinSize(
                minWidth = 80.dp,
                minHeight = dimensionResource(id = R.dimen.current_condition_param_height)
            )
            .background(
                color = MaterialTheme.customColorScheme.detailScreenSurface,
                shape = RoundedCornerShape(percent = 50)
            )
            .onGloballyPositioned {
                onUpdateTextMiddleX(it.size.width / 2f)
                onUpdateTextMiddlePoint(
                    Offset(
                        x = it.positionInParent().x + it.size.width / 2,
                        y = it.positionInParent().y + it.size.height
                    )
                )
            }
            .border(
                width = dimensionResource(id = R.dimen.connector_line_width),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(percent = 50)
            )
            .padding(8.dp),
        textAlign = TextAlign.Center,
        text = text,
        color = MaterialTheme.colorScheme.onSurface
    )
}

//@JvmName("StringInputMethod")
//@Composable
//fun CurrentCondition(
//    modifier: Modifier = Modifier,
//    quantityValuePairs: List<Pair<WeatherQuantity, String>>,
//    detailState: DetailState,
//    topBannerHeight: Float
//) {
//    CurrentCondition(
//        modifier = modifier,
//        quantityValuePairs = List(size = quantityValuePairs.size){
//            quantityValuePairs[it].first to buildAnnotatedString{append(quantityValuePairs[it].second)}
//        },
//        detailState = detailState,
//        topBannerHeight = topBannerHeight
//    )
//}
//
//@JvmName("StringAnnotatedStringInputMethod")
//@Composable
//fun CurrentCondition(
//    modifier: Modifier = Modifier,
//    quantityValuePairs: List<Pair<String, AnnotatedString>>,
//    @DrawableRes iconId: Int,
//    topBannerHeight: Float
//) {
//    CurrentCondition(
//        modifier = modifier,
//        quantityValuePairs = List(size = quantityValuePairs.size){
//            buildAnnotatedString{append(quantityValuePairs[it].first)} to quantityValuePairs[it].second
//        },
//        iconId = iconId,
//        topBannerHeight = topBannerHeight
//    )
//}

@Composable
fun WeatherCurrentCondition(
    modifier: Modifier = Modifier,
    weatherCode: Int?,
    sunRise: String?,
    sunSet: String?,
    utcOffset: Long,
    targetX: Float
) {
    val utcTime = LocalTime.ofSecondOfDay(((targetX / 24) * daySeconds).toLong())
    val utcDate = LocalDate.now(ZoneId.of("UTC"))
    val utcNow = LocalDateTime.of(utcDate, utcTime)
    val localCurrentTime = utcNow.plusMinutes(utcOffset)
    val weatherType = WeatherType.fromWMO(
        code = weatherCode,
        isDay = if (sunRise.isNullOrEmpty() || sunSet.isNullOrEmpty()) true
        else (localCurrentTime.isAfter(LocalDateTime.parse(sunRise)) &&
                localCurrentTime.isBefore(LocalDateTime.parse(sunSet))),
        moonType = calculateMoonType(utcNow.toLocalDate())
    )
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(percent = 50)
                )
                .padding(8.dp),
            text = weatherType.weatherDesc,
            color = MaterialTheme.colorScheme.onPrimaryContainer

        )
        SingleConnector(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.weight(1f)
        )
        WeatherType.WeatherIcon(
            modifier = Modifier
                .size(88.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = valueContainerAlpha),
                    shape = CircleShape
                )
                .border(
                    width = dimensionResource(id = R.dimen.connector_line_width),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
                .padding(8.dp),
            weatherType = weatherType,
        )
    }
}

@Composable
fun WindCurrentCondition(
    modifier: Modifier = Modifier,
    currentDayHourlyWeather: ImmutableList<LocalHourlyWeather>,
    targetX: Float,
    appSettings: AppSettings
) {
//    Log.i("wind1", "WindCurrentCondition")
    val density = LocalDensity.current
    val iconSize = dimensionResource(id = R.dimen.current_condition_icon_size)
    val firstPointX = 0f
    val lastPointX = currentDayHourlyWeather.size.toFloat() - 1
    val controlPoints1 = currentDayHourlyWeather.map {
        if (it.windSpeedControl1X != null && it.windSpeedControl1Y != null)
            Offset(it.windSpeedControl1X, it.windSpeedControl1Y)
        else null }
    val controlPoints2 = currentDayHourlyWeather.map {
        if (it.windSpeedControl2X != null && it.windSpeedControl2Y != null)
            Offset(it.windSpeedControl2X, it.windSpeedControl2Y)
        else null}
    val windSpeed = cubicCurveXtoY(
        data = currentDayHourlyWeather.map { it.windSpeed },
        controlPoints1 = controlPoints1,
        controlPoints2 = controlPoints2,
        firstPointX = firstPointX,
        lastPointX = lastPointX,
        targetX = targetX
    )
    val windDirection = linearCurveXtoY(
        data = currentDayHourlyWeather.map { it.windDirection },
        firstPointX = firstPointX,
        lastPointX = lastPointX,
        targetX = targetX
    )

    val windSpeedKmph = windSpeed?.toKmph(appSettings.speedUnit) ?: 10f
    val infiniteTransition = rememberInfiniteTransition(label = "wind")
    val arrowMovement by infiniteTransition.animateValue(
        initialValue = 0f,
        targetValue = 1f,
        typeConverter = Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1000 * 50 / windSpeedKmph).coerceAtLeast(1000f).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wind arrow"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ){
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuantityName(
                text = AnnotatedString("Wind")
            )
            SingleConnector(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
        Box(modifier = Modifier
            .size(dimensionResource(id = R.dimen.current_condition_gauge_width))
//            .background(
//                color = MaterialTheme.colorScheme.secondaryContainer/*.copy(alpha = valueContainerAlpha)*/,
//                shape = CircleShape
//            )
            .zIndex(1f)
        ){
            val circleColor = MaterialTheme.colorScheme.secondaryContainer
            val size = dimensionResource(id = R.dimen.current_condition_gauge_width).toPx(density)
            val arrowWidth = size / 5.2f
            val path = windArrowPath(size, size, arrowWidth)
            Canvas(
                modifier = Modifier.fillMaxSize()
                    .graphicsLayer {
                        rotationZ = (windDirection ?: 0f) + 180
                    }
//                    .clip(CircleShape)
            ) {
                drawIntoCanvas {
                    it.saveLayer(
                        Rect(0f, 0f, this.size.width, this.size.height),
                        Paint()
                    )
                    drawCircle(color = circleColor)
                    if (windSpeed != null) {
                        for (i in 0..6) {
                            translate(top = -1.2f * ((i / 7f + arrowMovement) % 1) * size) {
                                drawPath(
                                    path = path,
                                    color = Color.Yellow,
                                    blendMode = BlendMode.Clear
                                )
                            }
                        }
                    }
                }
            }
//            Spacer(
//                modifier = Modifier.fillMaxSize()
//                    .graphicsLayer {
//                        rotationZ = (windDirection ?: 0f) + 180
//                    }
//                    .drawWithContent {
//
//                    }
//            )

            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.wind_compass),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSecondaryContainer)
            )
            /*windDirection?.let {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = it + 180
                        },
                    painter = painterResource(id = R.drawable.wind_compass_arrow2),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(curveGreen),
                    alpha = 0.7f
                )
            }*/
            Text(
                modifier = Modifier
                    .width(64.dp)
                    .padding(8.dp)
                    .align(Alignment.Center),
                text = buildAnnotatedString{
                    if (windSpeed != null)
                        append("%.${WeatherQuantity.WINDSPEED.floatingPointDigits(appSettings)}f".format(windSpeed))
                    else
                        append("?")
                    append("\n")
                    append(WeatherQuantity.WINDSPEED.unit(appSettings))
                },
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SingleConnector(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            Icon(
                modifier = Modifier
                    .size(iconSize)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .border(
                        width = dimensionResource(id = R.dimen.connector_line_width),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
                    .padding(12.dp),
                painter = painterResource(id = R.drawable.wind),
                contentDescription = "Wind",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

fun windArrowPath(
    width: Float,
    height: Float,
    arrowWidth: Float
):  androidx.compose.ui.graphics.Path {
    val xCenter = width/2
    val halfArrowWidth = arrowWidth/2
    val yal = 0.4375f * arrowWidth

    return Path().apply {
        moveTo(x = xCenter, y = height)
        lineTo(x = xCenter + halfArrowWidth, y = height + halfArrowWidth)
        relativeLineTo(dx = 0f, dy = yal)
        lineTo(x = xCenter, y = height + yal)
        lineTo(x = xCenter - halfArrowWidth, y = height + halfArrowWidth + yal)
        relativeLineTo(dx = 0f, dy = -yal)
        close()
    }
}

@Composable
fun UVCurrentCondition(
    modifier: Modifier = Modifier,
    currentDayHourlyWeather: ImmutableList<LocalHourlyWeather>,
    targetX: Float,
    appSettings: AppSettings
) {
    val iconSize = dimensionResource(id = R.dimen.current_condition_icon_size)
    val firstPointX = 0f
    val lastPointX = currentDayHourlyWeather.size.toFloat() - 1
    val controlPoints1 = currentDayHourlyWeather.map {
        if (it.uvIndexControl1X != null && it.uvIndexControl1Y != null)
            Offset(it.uvIndexControl1X, it.uvIndexControl1Y)
        else null }
    val controlPoints2 = currentDayHourlyWeather.map {
        if (it.uvIndexControl2X != null && it.uvIndexControl2Y != null)
            Offset(it.uvIndexControl2X, it.uvIndexControl2Y)
        else null}
    val uvIndex = cubicCurveXtoY(
        data = currentDayHourlyWeather.map { it.uvIndex },
        controlPoints1 = controlPoints1,
        controlPoints2 = controlPoints2,
        firstPointX = firstPointX,
        lastPointX = lastPointX,
        targetX = targetX
    )?.coerceAtLeast(0f)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ){
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuantityName(
                text = AnnotatedString("UV index")
            )
            SingleConnector(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
        Box(modifier = Modifier
            .size(dimensionResource(id = R.dimen.current_condition_gauge_width))
            .background(
                color = WeatherGaugeBackground,
                shape = CircleShape
            )
            .zIndex(1f)
        ){
            val unit = WeatherQuantity.UVINDEX.unit(appSettings)
            val size = DailyWeatherQuantity.UVINDEXMAX.brushColors.size
            val colorStops = Array(size) { index ->
                (0.75f * index / (size - 1) + 0.125f) to DailyWeatherQuantity.UVINDEXMAX.brushColors[index]
            }
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawIntoCanvas {
                            it.saveLayer(Rect(0f, 0f, this.size.width, this.size.height), Paint())
                        }
                        rotate(90f) {
                            drawArc(
                                brush = Brush.sweepGradient(colorStops = colorStops),
                                startAngle = 45f,
                                sweepAngle = 270f,
                                useCenter = true
                            )
                        }
                        drawCircle(
                            color = Color.White,
                            radius = 0.3f * this.size.width,
                            blendMode = BlendMode.Clear
                        )
                    },
            )
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.uv_gauge),
                contentDescription = null,
            )
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate((uvIndex ?: 0f) * 270 / 11),
                painter = painterResource(id = R.drawable.uv_handle),
                contentDescription = null,
                colorFilter = ColorFilter.tint(WeatherGaugeHandle)
            )
            BorderedText(
                modifier = Modifier.align(Alignment.BottomCenter),
                text = buildAnnotatedString{
                    append(
                        if (uvIndex != null)
                            "%.${WeatherQuantity.UVINDEX.floatingPointDigits(appSettings)}f".format(uvIndex)
                        else "?"
                    )
                },
                fontSize = 16.sp,
                strokeWidth = 1.5f,
                fillColor = Gray60,
                alignment = Alignment.Center
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SingleConnector(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            Icon(
                modifier = Modifier
                    .size(iconSize)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .border(
                        width = dimensionResource(id = R.dimen.connector_line_width),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
                    .padding(12.dp),
                painter = painterResource(id = R.drawable.uv),
                contentDescription = "UV index",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}





@Preview(showBackground = true)
@Composable
fun CurrentConditionPreview(modifier: Modifier = Modifier) {
    CurrentCondition(
        modifier = modifier.padding(8.dp),
        weatherQuantity = listOf(
            WeatherQuantity.PRECIPITATION,
            WeatherQuantity.PRECIPITATIONPROBABILITY
        ).toImmutableList(),
        currentDayHourlyWeather = persistentListOf(),
        currentDayHourlyAirQuality = persistentListOf(),
        targetX = 0f,
        appSettings = AppSettings(),
        topBannerHeight = 0f,
    )
}