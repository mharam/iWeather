package com.takaapoo.weatherer.domain.model

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.toArgb
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.theme.curveBlue
import com.takaapoo.weatherer.ui.theme.curveGreen
import com.takaapoo.weatherer.ui.theme.curveOrange
import com.takaapoo.weatherer.ui.theme.curvePink

//const val initialXAxisStart = -30f
//const val initialXAxisEnd = 48f

@Immutable
data class HourlyChartState(
    val initialXAxisStart: Float = -30f,
    val initialXAxisEnd: Float = 48f,
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
    val curvesColor: List<Int> = listOf(curveBlue, curvePink, curveGreen, curveOrange).map { it.toArgb() },
    val sliderThumbPosition: Float = 0f,
    val curveValueAtIndicator: List<Float?> = listOf(0f),
    val settingsUpdated: Boolean = false,
    val awaker: Int = 0, // This is used for when changing xAxisStart, ... to make hourlyChartState variable change
    val curvesSmoothness: Map<WeatherQuantity, Float> = mapOf(
        WeatherQuantity.TEMPERATURE to 1f,
        WeatherQuantity.HUMIDITY to 0f,
        WeatherQuantity.DEWPOINT to 1f,
        WeatherQuantity.APPARENTTEMP to 1f,
        WeatherQuantity.PRECIPITATIONPROBABILITY to 0.5f,
        WeatherQuantity.CLOUDCOVER to 0f,
        WeatherQuantity.SURFACEPRESSURE to 1f,
        WeatherQuantity.VISIBILITY to 0f,
        WeatherQuantity.WINDSPEED to 1f,
        WeatherQuantity.WINDDIRECTION to 0f,
        WeatherQuantity.UVINDEX to 1f,
        WeatherQuantity.FREEZINGLEVELHEIGHT to 1f,
        WeatherQuantity.DIRECTRADIATION to 1f,
        WeatherQuantity.DIRECTNORMALIRRADIANCE to 1f,
        WeatherQuantity.AQI to 0f,
        WeatherQuantity.PM10 to 1f,
        WeatherQuantity.PM2_5 to 1f,
        WeatherQuantity.CO to 1f,
        WeatherQuantity.NO2 to 1f,
        WeatherQuantity.SO2 to 1f,
        WeatherQuantity.Ozone to 1f,
    )
)

//const val initialDailyXAxisStart = -2.5f
//const val initialDailyXAxisEnd = 5.5f

@Immutable
data class DailyChartState(
    val initialDailyXAxisStart: Float = -2.5f,
    val initialDailyXAxisEnd: Float = 5.5f,
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
    val settingsUpdated: Boolean = false,
    val awaker: Int = 0
)