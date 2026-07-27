package com.takaapoo.weatherer.domain.model

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.toArgb
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.theme.curveBlue
import com.takaapoo.weatherer.ui.theme.curveGreen
import com.takaapoo.weatherer.ui.theme.curveOrange
import com.takaapoo.weatherer.ui.theme.curvePink


@Immutable
data class HourlyChartState(
//    val initialXAxisStart: Float = -30f,
//    val initialXAxisEnd: Float = 48f,
    val chartQuantities: List<HourlyWeatherQuantity> = listOf(HourlyWeatherQuantity.TEMPERATURE),
    val weatherConditionIconsVisible: Boolean = false,
    val dotsOnCurveVisible: Boolean = false,
    val curveShadowVisible: Boolean = true,
    val sunRiseSetIconsVisible: Boolean = false,
    val chartGrid: ChartGrids = ChartGrids.All,
    val chartTheme: ChartTheme = ChartTheme.APPTHEME,
    val xAxisStart: Animatable<Float, AnimationVector1D> = Animatable(initialValue = -30f),
    val xAxisEnd: Animatable<Float, AnimationVector1D> = Animatable(initialValue = 48f),
    val yAxesStarts: List<Animatable<Float, AnimationVector1D>> = emptyList(),
    val yAxesEnds: List<Animatable<Float, AnimationVector1D>> = emptyList(),
    val curveAnimator: List<Animatable<Float, AnimationVector1D>> = emptyList(),
    val curvesColor: List<Int> = listOf(curveBlue, curvePink, curveGreen, curveOrange).map { it.toArgb() },
    val sliderThumbPosition: Animatable<Float, AnimationVector1D> = Animatable(initialValue = 0f),
    val curveValueAtIndicator: List<Float?> = listOf(null),
    val settingsUpdated: Boolean = false,
    val awaker: Int = 0, // This is used for when changing xAxisStart, ... to make hourlyChartState variable change
    val dataLoadingStatus: MyResult<*> = MyResult.Success(data = Unit),
    val xAxisAnimationTrigger: Boolean = true,
    val curvesSmoothness: Map<HourlyWeatherQuantity, Float> = mapOf(
        HourlyWeatherQuantity.TEMPERATURE to 1f,
        HourlyWeatherQuantity.HUMIDITY to 0f,
        HourlyWeatherQuantity.DEWPOINT to 1f,
        HourlyWeatherQuantity.APPARENTTEMP to 1f,
        HourlyWeatherQuantity.PRECIPITATIONPROBABILITY to 0.5f,
        HourlyWeatherQuantity.CLOUDCOVER to 0f,
        HourlyWeatherQuantity.SURFACEPRESSURE to 1f,
        HourlyWeatherQuantity.VISIBILITY to 0f,
        HourlyWeatherQuantity.WINDSPEED to 1f,
        HourlyWeatherQuantity.WINDDIRECTION to 0f,
        HourlyWeatherQuantity.UVINDEX to 1f,
        HourlyWeatherQuantity.FREEZINGLEVELHEIGHT to 1f,
        HourlyWeatherQuantity.DIRECTRADIATION to 1f,
        HourlyWeatherQuantity.DIRECTNORMALIRRADIANCE to 1f,
        HourlyWeatherQuantity.AQI to 0f,
        HourlyWeatherQuantity.PM10 to 1f,
        HourlyWeatherQuantity.PM2_5 to 1f,
        HourlyWeatherQuantity.CO to 1f,
        HourlyWeatherQuantity.NO2 to 1f,
        HourlyWeatherQuantity.SO2 to 1f,
        HourlyWeatherQuantity.Ozone to 1f,
    )
)

@Immutable
data class DailyChartState(
    val initialDailyXAxisStart: Float = -2.5f,
    val initialDailyXAxisEnd: Float = 5.5f,
    val chartQuantity: DailyWeatherQuantity = DailyWeatherQuantity.TEMPERATUREMINMAX,
    val weatherConditionIconsVisible: Boolean = false,
    val xAxisStart: Animatable<Float, AnimationVector1D> = Animatable(initialValue = initialDailyXAxisStart),
    val xAxisEnd: Animatable<Float, AnimationVector1D> = Animatable(initialValue = initialDailyXAxisEnd),
    val yAxesStarts: Animatable<Float, AnimationVector1D> = Animatable(initialValue = 0f),
    val yAxesEnds: Animatable<Float, AnimationVector1D> = Animatable(initialValue = 0f),
    val curveAnimator: Animatable<Float, AnimationVector1D> = Animatable(initialValue = 0f),
    val curveValueMinAtIndicator: Float = 0f,
    val curveValueMaxAtIndicator: Float = 0f,
    val sliderThumbPosition: Animatable<Float, AnimationVector1D> = Animatable(initialValue = 0f),
    val settingsUpdated: Boolean = false,
    val awaker: Int = 0,
    val dataLoadingStatus: MyResult<*> = MyResult.Success(data = Unit),
    val xAxisAnimationTrigger: Boolean = true,
)