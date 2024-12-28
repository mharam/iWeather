package com.takaapoo.weatherer.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.LocationsState
import com.takaapoo.weatherer.ui.screens.home.shapes.CardShape
import com.takaapoo.weatherer.ui.theme.HomeCardDay
import com.takaapoo.weatherer.ui.theme.HomeCardNight
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.utility.toPx

@Composable
fun WeatherItemFake(
    windowRotation: Float,
    modifier: Modifier = Modifier,
    itemState: LocationsState,
    pageNumber: Int,
    appSettings: AppSettings,
) {
    val context = LocalContext.current
    val density : Density = LocalDensity.current
    val imageBitmap: ImageBitmap? by remember(key1 = pageNumber) {
        mutableStateOf(
            ContextCompat.getDrawable(
                context,
                when(pageNumber){
                    0 -> R.drawable.ax2
                    1 -> R.drawable.ax3
                    else -> R.drawable.ax4
                }
            )?.toBitmap()?.asImageBitmap()
        )
    }

    Box(
        modifier = modifier
            .widthIn(
                min = dimensionResource(R.dimen.home_card_min_width),
                max = dimensionResource(R.dimen.home_card_max_width)
            )
    ) {
        val cardShape = CardShape(
            cardOverHead = dimensionResource(R.dimen.home_card_top_over_head).toPx(density),
            topRound = dimensionResource(R.dimen.home_card_corner_radius).toPx(density),
            overHeadDistance = dimensionResource(R.dimen.home_card_overhead_distance).toPx(density)
        )
        Box(
            modifier = Modifier.fillMaxSize()
                .shadow(
                    elevation = 2.dp,
                    shape = cardShape
                )
        ) {
            imageBitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
//                        .clip(cardShape)
                    ,
                    contentScale = ContentScale.FillHeight
                )
            }

            /*Image(
                painter = painterResource(
                    when(pageNumber){
                        0 -> R.drawable.ax2
                        1 -> R.drawable.ax3
                        else -> R.drawable.ax4
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .clip(cardShape)
                ,
                contentScale = ContentScale.FillHeight
            )*/
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 0f)
                        rotationX = windowRotation
                        cameraDistance = 50f
                    }
            ) {
                val textColor = if (itemState.isDay) HomeCardNight else HomeCardDay
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



@Preview(showBackground = true)
@Composable
fun WeatherItemFakePreview() {
    WeathererTheme {
        WeatherItemFake(
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
            appSettings = AppSettings(),
        )
    }
}