package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.utility.BorderedText
import kotlinx.collections.immutable.ImmutableList

@Composable
fun HourlyDiagramLegend(
    modifier: Modifier = Modifier,
    chartQuantities: ImmutableList<WeatherQuantity>,
    dotsOnCurveVisible: Boolean,
    curveValueAtIndicator: ImmutableList<Float?>,
    appSettings: AppSettings,
    onRemoveChartQuantity: (quantity: WeatherQuantity) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        val text = remember {
            mutableStateListOf(
                AnnotatedString(""), AnnotatedString(""),
                AnnotatedString(""), AnnotatedString("")
            )
        }
        for (index in 0 until 4) {
            val weatherQuantity = chartQuantities.getOrNull(index)
            AnimatedVisibility(visible = weatherQuantity != null) {
                weatherQuantity?.let {
                    val quantityValue = when (it) {
                        WeatherQuantity.HUMIDITY, WeatherQuantity.PRECIPITATIONPROBABILITY,
                        WeatherQuantity.CLOUDCOVER -> curveValueAtIndicator[index]?.coerceIn(0f, 100f)

                        WeatherQuantity.WINDSPEED, WeatherQuantity.UVINDEX, WeatherQuantity.DIRECTRADIATION,
                        WeatherQuantity.DIRECTNORMALIRRADIANCE, WeatherQuantity.Ozone, WeatherQuantity.SO2,
                        WeatherQuantity.CO, WeatherQuantity.NO2, WeatherQuantity.PM10, WeatherQuantity.PM2_5 ->
                            curveValueAtIndicator[index]?.coerceAtLeast(0f)

                        else -> curveValueAtIndicator[index]
                    }
                    text[index] = buildAnnotatedString {
                        append(
                            if (it.nameId != null) AnnotatedString(stringResource(id = it.nameId))
                            else it.title!!
                        )
                        when (quantityValue) {
                            null -> append(":  ?")
                            0f -> append(":  0")
                            else -> append(":  ${"%.${it.floatingPointDigits(appSettings)}f".format(quantityValue)}")
                        }
                        append(it.unit(appSettings))
                    }
                }
                HourlyDiagramLegendRow(
                    modifier = Modifier.fillMaxWidth(),
                    curveIndex = index,
                    showDot = dotsOnCurveVisible,
                    text = text[index],
                    onRemoveQuantity = {
                        weatherQuantity?.let { onRemoveChartQuantity(it) }
                    }
                )
            }
        }
    }
}

@Composable
fun HourlyDiagramLegendRow(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    curveIndex: Int,
    showDot: Boolean,
    onRemoveQuantity: () -> Unit
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        LegendLineIcon(
            modifier = Modifier
                .width(32.dp)
                .height(24.dp),
            curveIndex = curveIndex,
            showDot = showDot
        )
        Spacer(modifier = Modifier.width(8.dp))
        BorderedText(
            modifier = Modifier.weight(1f),
            text = text,
            strokeColor = curveColors[curveIndex],
            fillColor = curveColors[curveIndex],
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            modifier = Modifier.size(24.dp),
            onClick = { onRemoveQuantity() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close_24px),
                contentDescription = "Remove"
            )
        }
    }
}

@Composable
fun LegendLineIcon(
    modifier: Modifier = Modifier,
    curveIndex: Int,
    showDot: Boolean
) {
    val pointSize = dimensionResource(id = R.dimen.hourly_curve_point_size)
    val color = curveColors[curveIndex]
    Canvas(
        modifier = modifier
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        if (showDot) {
            val center = Offset(size.width / 2, size.height / 2)
            when (val dotType = DotType.entries[curveIndex]) {
                DotType.TRIANGLE, DotType.DIAMOND -> {
                    drawOutline(
                        outline = pointOutline(
                            center = center,
                            dimension = pointSize.toPx(),
                            dotType = dotType
                        ),
                        color = color
                    )
                }

                DotType.SQUARE, DotType.CIRCLE -> {
                    drawPoints(
                        points = listOf(center),
                        pointMode = PointMode.Points,
                        cap = if (dotType == DotType.SQUARE) StrokeCap.Square else StrokeCap.Round,
                        color = color,
                        strokeWidth = pointSize.toPx()
                    )
                }
            }

        }
    }
}