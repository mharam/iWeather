package com.takaapoo.weatherer.ui.screens.detail

import android.graphics.Path
import android.graphics.RectF
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.layoutId
import androidx.core.content.res.ResourcesCompat
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.GraphTypes
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.cubicCurveXtoY
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.linearCurveXtoY
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.quantityControlPoints
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.quantityData
import com.takaapoo.weatherer.ui.theme.AQGreen
import com.takaapoo.weatherer.ui.theme.AQMaroon
import com.takaapoo.weatherer.ui.theme.AQOrange
import com.takaapoo.weatherer.ui.theme.AQPurple
import com.takaapoo.weatherer.ui.theme.AQRed
import com.takaapoo.weatherer.ui.theme.AQYellow
import com.takaapoo.weatherer.ui.theme.Gray60
import com.takaapoo.weatherer.ui.theme.WeatherGaugeBackground
import com.takaapoo.weatherer.ui.theme.WeatherGaugeHandle
import com.takaapoo.weatherer.ui.utility.BorderedText
import com.takaapoo.weatherer.ui.utility.toPx
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.PI
import kotlin.math.roundToInt

@Composable
fun AirQualityCurrentCondition(
    modifier: Modifier = Modifier,
    currentDayHourlyAirQuality: ImmutableList<LocalAirQuality>,
    targetX: Float,
    topBannerHeight: Float,
    appSettings: AppSettings,
    quantity: WeatherQuantity,
    scroll: Float = 0f,
    currentTemperature: Float = 273f,
    currentPressure: Float = 1000f
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val unit = WeatherQuantity.AQI.unit(appSettings)
    val spaceOccupiedByValue = (dimensionResource(id = R.dimen.current_condition_gauge_width)
            + 32.dp).toPx(density)
    val iconSize = dimensionResource(id = R.dimen.current_condition_icon_size)
    val iconSizePx = iconSize.toPx(density)
    var quantityMiddlePoint by remember { mutableStateOf(Offset.Zero) }
    var rightPoint1 by remember { mutableStateOf(Offset.Zero) }
    var rightPoint2 by remember { mutableStateOf(Offset.Zero) }
    val leftPoints2 by remember {
        mutableStateOf(Array(size = 2){Offset.Zero})
    }
    var rightPoint3 by remember { mutableStateOf(Offset.Zero) }
    var boxWidth by remember { mutableFloatStateOf(0f) }
    var boxTopY by remember { mutableFloatStateOf(0f) }

    val firstPointX = 0f
    val lastPointX = currentDayHourlyAirQuality.size.toFloat() - 1
    val data = quantityData(
        airData = currentDayHourlyAirQuality,
        weatherQuantity = quantity
    )
    val (controlPoints1, controlPoints2) = quantityControlPoints(
        airData = currentDayHourlyAirQuality,
        weatherQuantity = quantity
    )
    val quantityValue = when (quantity.graphType()){
        GraphTypes.CUBIC -> cubicCurveXtoY(
            data = data,
            controlPoints1 = controlPoints1,
            controlPoints2 = controlPoints2,
            firstPointX = firstPointX,
            lastPointX = lastPointX,
            targetX = targetX
        )
        else -> linearCurveXtoY(
            data = data,
            firstPointX = firstPointX,
            lastPointX = lastPointX,
            targetX = targetX
        )
    }
    val molarVolume = 22.4 * (currentTemperature / 273) / (currentPressure / 1000)
    val quantityLevels = when(quantity){
        WeatherQuantity.AQI -> listOf(50, 100, 150, 200, 300, 500)
        WeatherQuantity.PM10 -> listOf(54, 154, 254, 354, 424, 604)
        WeatherQuantity.PM2_5 -> listOf(12, 35, 55, 150, 250, 500)
        else -> quantity.quantityBreakPoints().map { (it * quantity.molWeight() / molarVolume).toInt() }
    }

    val quantityName = buildAnnotatedString {
        append(quantity.title ?: AnnotatedString(stringResource(id = quantity.nameId!!)))
        if (unit.isNotEmpty()) {
            append(" ［")
            append(unit)
            append("］")
        }
    }
    val index = if (quantityValue != null) quantityLevels.indexOfFirst { it > quantityValue } else 6

    val airQualityCondition = stringArrayResource(id = R.array.air_quality_description).getOrNull(
        if (index > -1) index else 5
    )

    val isOnSeparateRow = 4 * quantityMiddlePoint.x > boxWidth - spaceOccupiedByValue - 32.dp.toPx(density)
    val constraints = decoupledConstraints(isOnSeparateRow)
    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                boxWidth = it.size.width.toFloat()
                boxTopY = it.parentLayoutCoordinates!!.positionInParent().y + scroll
            },
        constraintSet = constraints
    ) {
        QuantityName(
            modifier = Modifier.layoutId("quantity"),
            onUpdateTextMiddlePoint = {
                quantityMiddlePoint = it
            },
            text = quantityName
        )
        Box(
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.current_condition_gauge_width))
                .background(
                    color = WeatherGaugeBackground,
                    shape = CircleShape
                )
                .zIndex(1f)
                .layoutId("gauge")
                .onGloballyPositioned {
                    rightPoint1 =
                        it.positionInParent() + Offset(x = 0f, y = it.size.height / 2f)
                    leftPoints2[0] =
                        it.positionInParent() + Offset(
                            x = it.size.width.toFloat(),
                            y = it.size.height / 2f
                        )
                    rightPoint3 = Offset(
                        x = (it.parentLayoutCoordinates?.size?.width ?: 0) - iconSizePx,
                        y = (rightPoint1.y + (topBannerHeight - (boxTopY + rightPoint1.y - iconSizePx / 2))
                            .coerceAtLeast(0f))
                            .coerceAtMost(leftPoints2.last().y)
                    )
                }
        ) {
            val aqBrushColors =
                listOf(AQGreen, AQYellow, AQOrange, AQRed, AQPurple, AQMaroon)
            val colorStops = Array(aqBrushColors.size) { index ->
                (0.75f * index / (aqBrushColors.size - 1) + 0.125f) to aqBrushColors[index]
            }
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        onDrawBehind {
                            drawIntoCanvas {
                                it.saveLayer(
                                    Rect(0f, 0f, this.size.width, this.size.height),
                                    Paint()
                                )
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
                                radius = 0.3f * size.width,
                                blendMode = BlendMode.Clear
                            )
                            val width = size.width
                            val height = size.height
                            val radius = 0.4f * width
                            val paint = Paint()
                                .asFrameworkPaint()
                                .apply {
                                    isAntiAlias = true
                                    typeface = ResourcesCompat.getFont(context, R.font.cmu_serif)
                                    textSize = 14.sp.toPx()
                                }
                            val textPath = Path()
                            val bounds = RectF()
                            val path = Path().apply {
                                addArc(
                                    RectF(
                                        0.1f * width,
                                        0.1f * height,
                                        0.9f * width,
                                        0.9f * height
                                    ),
                                    90f,
                                    360f
                                )
                            }
                            (listOf(0) + quantityLevels).forEachIndexed { index, value ->
                                val text = if (quantity == WeatherQuantity.CO)
                                    "%.1f".format(value / 1000f) else value.toString()
                                paint.getTextPath(text, 0, text.length, 0f, 0f, textPath)
                                textPath.computeBounds(bounds, true)
                                drawIntoCanvas {
                                    it.nativeCanvas.drawTextOnPath(
                                        text,
                                        path,
                                        ((index + 1) * PI * radius / 4).toFloat() - bounds.width() / 2,
                                        0f,
                                        paint
                                    )
                                }
                            }
                        }
                    }
            )

            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.air_quality_gauge),
                contentDescription = null
            )
            val rotation = when (index) {
                6 -> 0f
                -1 -> 285f
                else -> index * 45 + (quantityValue!! - quantityLevels.getOrElse(index-1) { 0 }) * 45f /
                        (quantityLevels[index] - quantityLevels.getOrElse(index-1) { 0 })
            }
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotation),
                painter = painterResource(id = R.drawable.uv_handle),
                contentDescription = null,
                colorFilter = ColorFilter.tint(WeatherGaugeHandle)
            )
            BorderedText(
                modifier = Modifier.align(Alignment.BottomCenter),
                text = buildAnnotatedString {
                    append(
                        if (quantityValue != null)
                            "%.${quantity.floatingPointDigits(appSettings)}f".format(quantityValue)
                        else "?"
                    )
                },
                fontSize = 16.sp,
                fillColor = Gray60,
                strokeWidth = 1.5f,
                alignment = Alignment.Center
            )
        }
        Text(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(percent = 50)
                )
                .onGloballyPositioned {
                    rightPoint2 =
                        it.positionInParent() + Offset(x = 0f, y = it.size.height / 2f)
                    leftPoints2[1] =
                        it.positionInParent() + Offset(
                            x = it.size.width.toFloat(),
                            y = it.size.height / 2f
                        )
                }
                .border(
                    width = dimensionResource(id = R.dimen.connector_line_width),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(percent = 50)
                )
                .widthIn(min = dimensionResource(id = R.dimen.current_condition_value_width))
                .padding(8.dp)
                .layoutId("condition"),
            text = airQualityCondition ?: "",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        if (isOnSeparateRow) {
            SingleConnector2(
                modifier = Modifier.fillMaxSize(),
                leftPoint = quantityMiddlePoint,
                rightPoint = rightPoint1
            )
            SingleConnector2(
                modifier = Modifier.fillMaxSize(),
                leftPoint = quantityMiddlePoint,
                rightPoint = rightPoint2,
                turnPoint = Offset(x = quantityMiddlePoint.x, y = rightPoint1.y)
            )
        } else {
            SingleConnector(modifier = Modifier.layoutId("connector1"))
            SingleConnector2(
                modifier = Modifier.fillMaxSize(),
                leftPoint = quantityMiddlePoint,
                rightPoint = rightPoint2
            )
        }
        MultiConnectorLeft(
            modifier = Modifier.fillMaxSize(),
            leftPoints = leftPoints2.toList(),
            rightPoint = rightPoint3
        )
        Icon(
            modifier = Modifier
                .size(iconSize)
                .layoutId("icon")
                .graphicsLayer {
                    translationY = rightPoint3.y - iconSizePx / 2
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
                .padding(12.dp),
            painter = painterResource(id = WeatherQuantity.AQI.iconId),
            contentDescription = quantityName.toString(),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

private fun decoupledConstraints(isOnSeparateRow: Boolean): ConstraintSet {
    return ConstraintSet {
        val quantityName = createRefFor("quantity")
        val gauge = createRefFor("gauge")
        val condition = createRefFor("condition")
        val connector1 = createRefFor("connector1")
        val icon = createRefFor("icon")

        if (isOnSeparateRow){
            constrain(quantityName) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
            constrain(gauge) {
                top.linkTo(quantityName.bottom, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        } else {
            constrain(quantityName) {
                top.linkTo(gauge.top)
                bottom.linkTo(gauge.bottom)
                start.linkTo(parent.start)
            }
            constrain(gauge) {
                top.linkTo(parent.top, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            constrain(connector1) {
                width = Dimension.fillToConstraints
                top.linkTo(quantityName.top)
                start.linkTo(quantityName.end)
                end.linkTo(gauge.start)
                bottom.linkTo(quantityName.bottom)
            }
        }
        constrain(condition) {
            top.linkTo(anchor = gauge.bottom, margin = 8.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
        constrain(icon) {
            top.linkTo(anchor = parent.top)
            end.linkTo(parent.end)
        }
    }
}


@Composable
fun AirQualityCurrentCondition2(
    modifier: Modifier = Modifier,
    detailState: DetailState,
    quantity: WeatherQuantity,
    currentTemperature: Float = 273f,
    currentPressure: Float = 1000f
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var quantityMiddlePoint by remember { mutableStateOf(Offset.Zero) }
    var conditionBottomPoint by remember { mutableStateOf(Offset.Zero) }
    var gaugeBottomPoint by remember { mutableStateOf(Offset.Zero) }

    val firstPointX = 0f
    val lastPointX = detailState.currentDayHourlyAirQuality.size.toFloat() - 1
    val data = quantityData(
        weatherData = detailState.currentDayHourlyWeather,
        airData = detailState.currentDayHourlyAirQuality,
        weatherQuantity = quantity
    )
    val (controlPoints1, controlPoints2) = quantityControlPoints(
        weatherData = detailState.currentDayHourlyWeather,
        airData = detailState.currentDayHourlyAirQuality,
        weatherQuantity = quantity
    )
    var quantityValue = when (quantity.graphType()){
        GraphTypes.CUBIC -> cubicCurveXtoY(
            data = data,
            controlPoints1 = controlPoints1,
            controlPoints2 = controlPoints2,
            firstPointX = firstPointX,
            lastPointX = lastPointX,
            targetX = detailState.targetX
        )
        else -> linearCurveXtoY(
            data = data,
            firstPointX = firstPointX,
            lastPointX = lastPointX,
            targetX = detailState.targetX
        )
    }
    if (quantity == WeatherQuantity.CO)
        quantityValue = quantityValue?.div(1000)
    val molarVolume = 22.4f * (currentTemperature / 273) / (currentPressure / 1000)
    val quantityLevels = when(quantity){
        WeatherQuantity.AQI -> listOf(50f, 100f, 150f, 200f, 300f, 500f)
        WeatherQuantity.PM10 -> listOf(54f, 154f, 254f, 354f, 424f, 604f)
        WeatherQuantity.PM2_5 -> listOf(12f, 35f, 55f, 150f, 250f, 500f)
        WeatherQuantity.CO -> quantity.quantityBreakPoints().map { it * quantity.molWeight() / (1000 * molarVolume) }
        else -> quantity.quantityBreakPoints().map { it * quantity.molWeight() / molarVolume }
    }

    val quantityName = buildAnnotatedString {
        append(quantity.title ?: AnnotatedString(stringResource(id = quantity.nameId!!)))
        if (quantity != WeatherQuantity.AQI) {
            withStyle(
                style = SpanStyle(
                    fontSize = 14.sp,
                    baselineShift = BaselineShift.Superscript
                )
            ) {
                if (quantity != WeatherQuantity.CO) append(" ✻")
                if (quantity == WeatherQuantity.CO) append(" ★")
                if (quantity == WeatherQuantity.PM10 || quantity == WeatherQuantity.PM2_5) append("†")
                else append("‡")
            }
        }
    }
    val index = if (quantityValue != null) quantityLevels.indexOfFirst { it > quantityValue } else 6
    val airQualityCondition = stringArrayResource(id = R.array.air_quality_description).getOrNull(
        if (index > -1) index else 5
    )
    val constraints = ConstraintSet {
        val quantityLabel = createRefFor("quantity")
        val gauge = createRefFor("gauge")
        val condition = createRefFor("condition")
        val connector1 = createRefFor("connector1")

        constrain(quantityLabel) {
            top.linkTo(gauge.top)
            bottom.linkTo(gauge.bottom)
            start.linkTo(parent.start)
        }
        constrain(gauge) {
            top.linkTo(parent.top, margin = 8.dp)
            end.linkTo(parent.end)
        }
        constrain(connector1) {
            width = Dimension.fillToConstraints
            top.linkTo(quantityLabel.top)
            start.linkTo(quantityLabel.end)
            end.linkTo(gauge.start)
            bottom.linkTo(quantityLabel.bottom)
        }
        constrain(condition) {
            top.linkTo(anchor = quantityLabel.bottom, margin = 8.dp)
            end.linkTo(gauge.start)
        }
    }
    val aqBrushColors = listOf(AQGreen, AQYellow, AQOrange, AQRed, AQPurple, AQMaroon)
    ConstraintLayout(
        modifier = modifier.fillMaxSize(),
        constraintSet = constraints
    ) {
        QuantityName(
            modifier = Modifier.layoutId("quantity"),
            onUpdateTextMiddlePoint = {
                quantityMiddlePoint = it
            },
            text = quantityName
        )
        val rotation = when (index) {
            6 -> 0f
            -1 -> 285f
            else -> index * 45 + (quantityValue!! - quantityLevels.getOrElse(index-1) { 0f }) * 45f /
                    (quantityLevels[index] - quantityLevels.getOrElse(index-1) { 0f })
        }
        Box(
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.current_condition_gauge_width))
                .background(
                    color = WeatherGaugeBackground,
                    shape = CircleShape
                )
                .onGloballyPositioned {
                    gaugeBottomPoint = it.positionInParent() + Offset(
                        x = it.size.width / 2f,
                        y = it.size.height.toFloat()
                    )
                }
                .zIndex(1f)
                .layoutId("gauge")
        ) {
            val colorStops = Array(aqBrushColors.size) { index ->
                (0.75f * index / (aqBrushColors.size - 1) + 0.125f) to aqBrushColors[index]
            }
            val arcWidth = dimensionResource(id = R.dimen.connector_line_width).toPx(density)
            val arcColor = MaterialTheme.colorScheme.primaryContainer
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        onDrawBehind {
                            drawIntoCanvas {
                                it.saveLayer(
                                    Rect(0f, 0f, this.size.width, this.size.height),
                                    Paint()
                                )
                            }
                            rotate(90f) {
                                drawArc(
                                    brush = Brush.sweepGradient(colorStops = colorStops),
                                    startAngle = 45f,
                                    sweepAngle = 270f,
                                    useCenter = true
                                )
                            }
                            drawArc(
                                color = arcColor,
                                startAngle = 45f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = Offset(x = arcWidth/2, y = arcWidth/2),
                                size = Size(this.size.width - arcWidth, this.size.height - arcWidth),
                                style = Stroke(width = arcWidth)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 0.3f * this.size.width,
                                blendMode = BlendMode.Clear
                            )
                            val width = size.width
                            val height = size.height
                            val radius = 0.4f * width
                            val paint = Paint()
                                .asFrameworkPaint()
                                .apply {
                                    isAntiAlias = true
                                    typeface = ResourcesCompat.getFont(context, R.font.cmu_serif)
                                    textSize = 14.sp.toPx()
                                }
                            val textPath = Path()
                            val bounds = RectF()
                            val path = Path().apply {
                                addArc(
                                    RectF(
                                        0.1f * width,
                                        0.1f * height,
                                        0.9f * width,
                                        0.9f * height
                                    ),
                                    90f,
                                    360f
                                )
                            }
                            (listOf(0f) + quantityLevels).forEachIndexed { index, value ->
                                val text = if (quantity == WeatherQuantity.CO)
                                    "%.1f".format(value) else value
                                    .roundToInt()
                                    .toString()
                                paint.getTextPath(text, 0, text.length, 0f, 0f, textPath)
                                textPath.computeBounds(bounds, true)
                                drawIntoCanvas {
                                    it.nativeCanvas.drawTextOnPath(
                                        text,
                                        path,
                                        ((index + 1) * PI * radius / 4).toFloat() - bounds.width() / 2,
                                        0f,
                                        paint
                                    )
                                }
                            }
                        }
                    }
            )
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.air_quality_gauge),
                contentDescription = null
            )
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotation),
                painter = painterResource(id = R.drawable.uv_handle),
                contentDescription = null,
                colorFilter = ColorFilter.tint(WeatherGaugeHandle)
            )
            val floatingPointDigits = if (quantity == WeatherQuantity.CO) 2 else 0
            BorderedText(
                modifier = Modifier.align(Alignment.BottomCenter),
                text = buildAnnotatedString {
                    append(
                        if (quantityValue != null) "%.${floatingPointDigits}f".format(quantityValue)
                        else "?"
                    )
                },
                fontSize = 16.sp,
                fillColor = Gray60,
                strokeWidth = 1.5f,
                alignment = Alignment.Center
            )
        }
        val color = lerpMultipleColors(aqBrushColors, (rotation / 270).coerceIn(0f, 1f))
        Text(
            modifier = Modifier
                .background(
                    color = color,
                    shape = RoundedCornerShape(percent = 50)
                )
                .border(
                    width = dimensionResource(id = R.dimen.connector_line_width),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(percent = 50)
                )
                .onGloballyPositioned {
                    conditionBottomPoint = it.positionInParent() + Offset(
                        x = it.size.width / 2f,
                        y = it.size.height.toFloat()
                    )
                }
                .zIndex(1f)
                .widthIn(min = dimensionResource(id = R.dimen.current_condition_value_width))
                .padding(8.dp)
                .layoutId("condition"),
            text = airQualityCondition ?: "",
            textAlign = TextAlign.Center,
            color = Gray60
        )
        SingleConnector(modifier = Modifier.layoutId("connector1"))
        SingleConnector3(
            modifier = Modifier.fillMaxSize(),
            leftPoint = conditionBottomPoint,
            rightPoint = gaugeBottomPoint
        )
    }
}

