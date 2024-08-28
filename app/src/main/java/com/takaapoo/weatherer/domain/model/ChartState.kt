package com.takaapoo.weatherer.domain.model

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity

const val initialXAxisStart = -30f
const val initialXAxisEnd = 48f

data class ChartState(
    val chartQuantities: List<WeatherQuantity> = listOf(WeatherQuantity.TEMPERATURE),
    val weatherConditionIconsVisible: Boolean = false,
    val dotsOnCurveVisible: Boolean = false,
    val curveShadowVisible: Boolean = true,
    val sunRiseSetIconsVisible: Boolean = false,
    val chartGrid: ChartGrids = ChartGrids.All,
    val chartTheme: ChartTheme = ChartTheme.APPTHEME,
    val xAxisStart: Animatable<Float, AnimationVector1D> = Animatable(initialValue = initialXAxisStart),
    val xAxisEnd: Animatable<Float, AnimationVector1D> = Animatable(initialValue = initialXAxisEnd),
    val yAxesStarts: List<Animatable<Float, AnimationVector1D>> = emptyList(),
    val yAxesEnds: List<Animatable<Float, AnimationVector1D>> = emptyList(),
    val curveAnimator: List<Animatable<Float, AnimationVector1D>> = emptyList(),
    val sliderThumbPosition: Float = 0f,
    val curveValueAtIndicator: List<Float> = listOf(0f)
)

const val initialDailyXAxisStart = -2.5f
const val initialDailyXAxisEnd = 5.5f

data class DailyChartState(
    val chartQuantity: DailyWeatherQuantity = DailyWeatherQuantity.TEMPERATUREMINMAX,
    val weatherConditionIconsVisible: Boolean = false,
    val xAxisStart: Animatable<Float, AnimationVector1D> = Animatable(initialValue = initialDailyXAxisStart),
    val xAxisEnd: Animatable<Float, AnimationVector1D> = Animatable(initialValue = initialDailyXAxisEnd),
    val yAxesStarts: Animatable<Float, AnimationVector1D> = Animatable(initialValue = 0f),
    val yAxesEnds: Animatable<Float, AnimationVector1D> = Animatable(initialValue = 1f),
    val curveAnimator: Animatable<Float, AnimationVector1D> = Animatable(initialValue = 0f),
    val curveValueMinAtIndicator: Float = 0f,
    val curveValueMaxAtIndicator: Float = 0f,
    val sliderThumbPosition: Float = 0f,
)