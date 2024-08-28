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
import com.takaapoo.weatherer.ui.screens.detail.BorderedText
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity

@Composable
fun HourlyDiagramLegend(
    modifier: Modifier = Modifier,
    chartQuantities: List<WeatherQuantity>,
    dotsOnCurveVisible: Boolean,
    curveValueAtIndicator: List<Float>,
    onRemoveChartQuantity: (quantity: WeatherQuantity) -> Unit
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp, end = 8.dp)
            .fillMaxWidth(),
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
                        WeatherQuantity.CLOUDCOVER -> curveValueAtIndicator[index].coerceIn(
                            0f,
                            100f
                        )

                        WeatherQuantity.WINDSPEED, WeatherQuantity.UVINDEX, WeatherQuantity.DIRECTRADIATION,
                        WeatherQuantity.DIRECTNORMALIRRADIANCE -> curveValueAtIndicator[index].coerceAtLeast(
                            0f
                        )

                        else -> curveValueAtIndicator[index]
                    }
                    text[index] = buildAnnotatedString {
                        append(
                            if (it.nameId != null) AnnotatedString(stringResource(id = it.nameId))
                            else it.title!!
                        )
                        append(":  ${"%.${it.floatingPointDigits}f".format(quantityValue)}")
                        append(it.unit(AppSettings()))
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