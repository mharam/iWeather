package com.takaapoo.weatherer.domain

import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.moonPhaseMap
import com.takaapoo.weatherer.ui.screens.detail.toDp
import com.takaapoo.weatherer.ui.screens.home.toDp
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate

sealed class WeatherType(
    val weatherDesc: String,
    @DrawableRes val icons: List<Int>
) {
    class ClearSky(val isDay: Boolean, moonType: Int?) : WeatherType(
        weatherDesc = "Clear sky",
        icons = listOf(if (isDay) R.drawable.sunny else moonDrawable(moonType))
    )
    class MainlyClear(val isDay: Boolean, val moonType: Int?) : WeatherType(
        weatherDesc = "Mainly clear",
        icons = listOf(if (isDay) R.drawable.sunny else moonDrawable(moonType), R.drawable.cloud1)
    )
    class PartlyCloudy(val isDay: Boolean,val moonType: Int?) : WeatherType(
        weatherDesc = "Partly cloudy",
        icons = listOf(R.drawable.cloud1, if (isDay) R.drawable.sunny else moonDrawable(moonType),
            R.drawable.cloud2)
    )
    data object Overcast : WeatherType(
        weatherDesc = "Overcast",
        icons = listOf(R.drawable.overcast)
    )
    data object Foggy : WeatherType(
        weatherDesc = "Fog",
        icons = listOf(R.drawable.overcast)
    )
    data object DepositingRimeFog : WeatherType(
        weatherDesc = "Depositing rime fog",
        icons = listOf(R.drawable.overcast)
    )
    data object LightDrizzle : WeatherType(
        weatherDesc = "Light drizzle",
        icons = listOf(R.drawable.light_drizzle)
    )
    data object ModerateDrizzle : WeatherType(
        weatherDesc = "Moderate drizzle",
        icons = listOf(R.drawable.moderate_drizzle)
    )
    data object DenseDrizzle : WeatherType(
        weatherDesc = "Dense drizzle",
        icons = listOf(R.drawable.dense_drizzle)
    )
    data object LightFreezingDrizzle : WeatherType(
        weatherDesc = "Light freezing drizzle",
        icons = listOf(R.drawable.slight_freezing_drizzle)
    )
    data object DenseFreezingDrizzle : WeatherType(
        weatherDesc = "Dense freezing drizzle",
        icons = listOf(R.drawable.dense_freezing_drizzle)
    )
    data object SlightRain : WeatherType(
        weatherDesc = "Slight rain",
        icons = listOf(R.drawable.slight_rain)
    )
    data object ModerateRain : WeatherType(
        weatherDesc = "Rainy",
        icons = listOf(R.drawable.rainy)
    )
    data object HeavyRain : WeatherType(
        weatherDesc = "Heavy rain",
        icons = listOf(R.drawable.heavy_rain)
    )
    data object HeavyFreezingRain: WeatherType(
        weatherDesc = "Heavy freezing rain",
        icons = listOf(R.drawable.heavy_freezing_rain)
    )
    data object SlightSnowFall: WeatherType(
        weatherDesc = "Slight snow fall",
        icons = listOf(R.drawable.slight_snow_fall)
    )
    data object ModerateSnowFall: WeatherType(
        weatherDesc = "Moderate snow fall",
        icons = listOf(R.drawable.moderate_snow_fall)
    )
    data object HeavySnowFall: WeatherType(
        weatherDesc = "Heavy snow fall",
        icons = listOf(R.drawable.heavy_snow_fall)
    )
    data object SnowGrains: WeatherType(
        weatherDesc = "Snow grains",
        icons = listOf(R.drawable.snow_grains)
    )
    data object SlightRainShowers: WeatherType(
        weatherDesc = "Slight rain showers",
        icons = listOf(R.drawable.slight_rain_showers)
    )
    data object ModerateRainShowers: WeatherType(
        weatherDesc = "Moderate rain showers",
        icons = listOf(R.drawable.moderate_rain_showers)
    )
    data object ViolentRainShowers: WeatherType(
        weatherDesc = "Violent rain showers",
        icons = listOf(R.drawable.violent_rain_showers)
    )
    data object SlightSnowShowers: WeatherType(
        weatherDesc = "Light snow showers",
        icons = listOf(R.drawable.slight_snow_fall)
    )
    data object HeavySnowShowers: WeatherType(
        weatherDesc = "Heavy snow showers",
        icons = listOf(R.drawable.heavy_snow_fall)
    )
    data object ModerateThunderstorm: WeatherType(
        weatherDesc = "Moderate thunderstorm",
        icons = listOf(R.drawable.moderate_thunderstorm)
    )
    data object SlightHailThunderstorm: WeatherType(
        weatherDesc = "Thunderstorm with slight hail",
        icons = listOf(R.drawable.thunderstorm_with_slight_hail)
    )
    data object HeavyHailThunderstorm: WeatherType(
        weatherDesc = "Thunderstorm with heavy hail",
        icons = listOf(R.drawable.thunderstorm_with_heavy_hail)
    )

    companion object {
        fun fromWMO(code: Int?, isDay: Boolean, moonType: Int?): WeatherType {
            return when(code) {
                0 -> ClearSky(isDay, moonType)
                1 -> MainlyClear(isDay, moonType)
                2 -> PartlyCloudy(isDay, moonType)
                3 -> Overcast
                45 -> Foggy
                48 -> DepositingRimeFog
                51 -> LightDrizzle
                53 -> ModerateDrizzle
                55 -> DenseDrizzle
                56 -> LightFreezingDrizzle
                57 -> DenseFreezingDrizzle
                61 -> SlightRain
                63 -> ModerateRain
                65 -> HeavyRain
                66 -> LightFreezingDrizzle
                67 -> HeavyFreezingRain
                71 -> SlightSnowFall
                73 -> ModerateSnowFall
                75 -> HeavySnowFall
                77 -> SnowGrains
                80 -> SlightRainShowers
                81 -> ModerateRainShowers
                82 -> ViolentRainShowers
                85 -> SlightSnowShowers
                86 -> HeavySnowShowers
                95 -> ModerateThunderstorm
                96 -> SlightHailThunderstorm
                99 -> HeavyHailThunderstorm
                else -> ClearSky(isDay, moonType)
            }
        }

        @Composable
        fun WeatherIcon(
            weatherType: WeatherType,
            modifier: Modifier = Modifier
        ) {
            val density = LocalDensity.current
            var iconSize by rememberSaveable { mutableIntStateOf(0) }
            when (weatherType){
                is MainlyClear -> {
                    Box(
                        modifier = modifier.onGloballyPositioned {
                            iconSize = it.size.width
                        },
                        contentAlignment = Alignment.Center
                    ){
                        if (weatherType.isDay) {
                            Image(
                                modifier = Modifier
                                    .scale(0.9f)
                                    .offset(
                                        x = (-iconSize / 10f).toDp(density),
                                        y = (-iconSize / 13f).toDp(density)
                                    ),
                                painter = painterResource(R.drawable.sunny),
                                contentDescription = null,
                            )
                        } else {
                            Image(
                                modifier = Modifier,
                                painter = painterResource(moonDrawable(weatherType.moonType)),
                                contentDescription = null,
                            )
                        }
                        Image(
                            modifier = Modifier,
                            painter = painterResource(R.drawable.cloud1),
                            contentDescription = "Mainly clear",
                        )
                    }
                }
                is PartlyCloudy -> {
                    Box(
                        modifier = modifier.onGloballyPositioned {
                            iconSize = it.size.width
                        },
                        contentAlignment = Alignment.Center
                    ){
                        Image(
                            modifier = Modifier
                                .offset(x = (-iconSize / 5f).toDp(density)),
                            painter = painterResource(R.drawable.cloud1),
                            contentDescription = "Mainly clear",
                        )
                        if (weatherType.isDay) {
                            Image(
                                modifier = Modifier
                                    .scale(0.9f)
                                    .offset(
                                        x = (-iconSize / 20f).toDp(density),
                                        y = (-iconSize / 13f).toDp(density)
                                    ),
                                painter = painterResource(R.drawable.sunny),
                                contentDescription = null,
                            )
                        } else {
                            Image(
                                modifier = Modifier
                                    .offset(x = (-iconSize / 20f).toDp(density)),
                                painter = painterResource(moonDrawable(weatherType.moonType)),
                                contentDescription = null,
                            )
                        }
                        Image(
                            modifier = Modifier,
                            painter = painterResource(R.drawable.cloud2),
                            contentDescription = "Mainly clear",
                        )
                    }
                }
                else -> {
                    Image(
                        modifier = modifier,
                        painter = painterResource(weatherType.icons.first()),
                        contentDescription = weatherType.weatherDesc,
                    )
                }
            }
        }

        fun DrawScope.drawWeatherIcon(
            weatherType: WeatherType,
            context: Context,
            topLeft: IntOffset,
            iconSize: IntSize
        ){
            when (weatherType){
                is MainlyClear -> {
                    if (weatherType.isDay) {
                        drawImage(
                            image = ImageBitmap.imageResource(context.resources, R.drawable.sunny),
                            dstOffset = topLeft + IntOffset(
                                x = (-iconSize.width / 10),
                                y = (-iconSize.width / 13)
                            ),
                            dstSize = iconSize.times(9).div(10)
                        )
                    } else {
                        drawImage(
                            image = ImageBitmap.imageResource(context.resources, moonDrawable(weatherType.moonType)),
                            dstOffset = topLeft,
                            dstSize = iconSize
                        )
                    }
                    drawImage(
                        image = ImageBitmap.imageResource(context.resources, R.drawable.cloud1),
                        dstOffset = topLeft,
                        dstSize = iconSize
                    )
                }
                is PartlyCloudy -> {
                    drawImage(
                        image = ImageBitmap.imageResource(context.resources, R.drawable.cloud1),
                        dstOffset = topLeft + IntOffset(
                            x = (-iconSize.width / 5),
                            y = 0
                        ),
                        dstSize = iconSize
                    )
                    if (weatherType.isDay) {
                        drawImage(
                            image = ImageBitmap.imageResource(context.resources, R.drawable.sunny),
                            dstOffset = topLeft + IntOffset(
                                x = (-iconSize.width / 20),
                                y = (-iconSize.width / 13)
                            ),
                            dstSize = iconSize.times(9).div(10)
                        )
                    } else {
                        drawImage(
                            image = ImageBitmap.imageResource(context.resources, moonDrawable(weatherType.moonType)),
                            dstOffset = topLeft + IntOffset(
                                x = (-iconSize.width / 20),
                                y = 0
                            ),
                            dstSize = iconSize
                        )
                    }
                    drawImage(
                        image = ImageBitmap.imageResource(context.resources, R.drawable.cloud2),
                        dstOffset = topLeft,
                        dstSize = iconSize
                    )
                }
                else -> {
                    drawImage(
                        image = ImageBitmap.imageResource(context.resources, weatherType.icons.first()),
                        dstOffset = topLeft,
                        dstSize = iconSize
                    )
                }
            }
        }

        @DrawableRes fun moonDrawable(moonType: Int?) = when (moonType){
            1 -> R.drawable.moon1
            2 -> R.drawable.moon2
            3 -> R.drawable.moon3
            4 -> R.drawable.moon4
            5 -> R.drawable.moon5
            6 -> R.drawable.moon6
            7 -> R.drawable.moon7
            8 -> R.drawable.moon8
            9 -> R.drawable.moon9
            10 -> R.drawable.moon10
            else -> R.drawable.moon11
        }

        fun calculateMoonType(date: LocalDate): Int?{
            val todayPhase = moonPhaseMap[date.toString()]?.jsonPrimitive?.double ?: return null
            val yesterdayPhase =
                moonPhaseMap[date.minusDays(1).toString()]?.jsonPrimitive?.double ?: return null

            return if (todayPhase > yesterdayPhase) (6 * todayPhase).toInt() + 1
            else 11 - (6 * todayPhase).toInt()
        }
    }
}