package com.takaapoo.weatherer.ui.screens.detail

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.GraphTypes
import com.takaapoo.weatherer.ui.theme.BarBlue
import com.takaapoo.weatherer.ui.theme.BarOrange
import com.takaapoo.weatherer.ui.theme.BarRed
import com.takaapoo.weatherer.ui.theme.BarYellow
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.UVGreen
import com.takaapoo.weatherer.ui.theme.UVMagenta
import com.takaapoo.weatherer.ui.theme.UVOrange
import com.takaapoo.weatherer.ui.theme.UVRed
import com.takaapoo.weatherer.ui.theme.UVYellow
import com.takaapoo.weatherer.ui.theme.curveBlue
import com.takaapoo.weatherer.ui.theme.curveGreen
import com.takaapoo.weatherer.ui.theme.curvePink

enum class WeatherQuantity(
    @StringRes val nameId: Int? = null,
    val title: AnnotatedString? = null,
    val airQuality: Boolean = false,
    @DrawableRes val iconId: Int
) {
    TEMPERATURE(nameId = R.string.temperature, iconId = R.drawable.temperature),
    HUMIDITY(nameId = R.string.humidity, iconId = R.drawable.humidity),
    DEWPOINT(nameId = R.string.dew_point, iconId = R.drawable.dew_point),
    APPARENTTEMP(nameId = R.string.apparent_temp, iconId = R.drawable.temperature),
    PRECIPITATIONPROBABILITY(nameId = R.string.precipitation_probability, iconId = R.drawable.precipitation),
    PRECIPITATION(nameId = R.string.precipitation, iconId = R.drawable.precipitation),
    RAIN(nameId = R.string.rain, iconId = R.drawable.precipitation),
    SHOWERS(nameId = R.string.showers, iconId = R.drawable.precipitation),
    SNOWFALL(nameId = R.string.snowfall, iconId = R.drawable.precipitation),
    CLOUDCOVER(nameId = R.string.cloud_cover, iconId = R.drawable.cloud_cover),
    SURFACEPRESSURE(nameId = R.string.surface_pressure, iconId = R.drawable.pressure),
    VISIBILITY(nameId = R.string.visibility, iconId = R.drawable.visibility),
    WINDSPEED(nameId = R.string.wind_speed, iconId = R.drawable.wind),
    WINDDIRECTION(nameId = R.string.wind_direction, iconId = R.drawable.wind),
    UVINDEX(nameId = R.string.uv_index, iconId = R.drawable.uv),
    FREEZINGLEVELHEIGHT(nameId = R.string.freezing_level_height, iconId = R.drawable.freeze_height),
    DIRECTRADIATION(nameId = R.string.direct_radiation, iconId = R.drawable.solar_radiation),
    DIRECTNORMALIRRADIANCE(nameId = R.string.direct_normal_irradiance, iconId = R.drawable.solar_radiation),

    AQI(nameId = R.string.AQI, iconId = R.drawable.mask, airQuality = true),
    PM10(title = buildAnnotatedString {
        append("PM")
        withStyle(
            style = SpanStyle(
                fontSize = 12.sp,
                baselineShift = BaselineShift.Subscript
            )
        ){ append("10") }
    }, iconId = R.drawable.mask, airQuality = true),
    PM2_5(title = buildAnnotatedString {
        append("PM")
        withStyle(
            style = SpanStyle(
                fontSize = 12.sp,
                baselineShift = BaselineShift.Subscript
            )
        ){ append("2.5") }
    }, iconId = R.drawable.mask, airQuality = true),
    CO(nameId = R.string.CO, iconId = R.drawable.mask, airQuality = true),
    NO2(title = buildAnnotatedString {
        append("Nitrogen dioxide (NO")
        withStyle(
            style = SpanStyle(
                fontSize = 12.sp,
                baselineShift = BaselineShift.Subscript
            )
        ){ append("2") }
        append(")")
    }, iconId = R.drawable.mask, airQuality = true),
    SO2(title = buildAnnotatedString {
        append("Sulphur dioxide (SO")
        withStyle(
            style = SpanStyle(
                fontSize = 12.sp,
                baselineShift = BaselineShift.Subscript
            )
        ){ append("2") }
        append(")")
    }, iconId = R.drawable.mask, airQuality = true),
    Ozone(title = buildAnnotatedString {
        append("Ozone (O")
        withStyle(
            style = SpanStyle(
                fontSize = 12.sp,
                baselineShift = BaselineShift.Subscript
            )
        ){ append("3") }
        append(")")
    }, iconId = R.drawable.mask, airQuality = true);


    fun unit(appSettings: AppSettings): AnnotatedString{
        return buildAnnotatedString{
            when (this@WeatherQuantity){
                TEMPERATURE, DEWPOINT, APPARENTTEMP -> {
                    append(when (appSettings.temperatureUnit){
                        Temperature.CELSIUS -> "° C"
                        Temperature.FAHRENHEIT -> "° F"
                        Temperature.KELVIN -> " K"
                    })
                }
                HUMIDITY, PRECIPITATIONPROBABILITY, CLOUDCOVER ->
                    append(" %")
                PRECIPITATION, RAIN, SHOWERS -> append(
                    when (appSettings.lengthUnit){
                        Length.SI -> " mm"
                        Length.IMPERIAL -> " in"
                    }
                )
                SNOWFALL -> append(
                    when (appSettings.lengthUnit){
                        Length.SI -> " cm"
                        Length.IMPERIAL -> " in"
                    }
                )
                SURFACEPRESSURE -> append(
                    when (appSettings.pressureUnit){
                        Pressure.Pa -> " hPa"
                        Pressure.BAR -> " bar"
                        Pressure.ATM -> " atm"
                        Pressure.PSI -> " psi"
                    }
                )
                VISIBILITY -> append(
                    when (appSettings.lengthUnit){
                        Length.SI -> " km"
                        Length.IMPERIAL -> " mi"
                    }
                )
                WINDSPEED -> append(
                    when (appSettings.speedUnit){
                        Speed.KMPH -> " km/h"
                        Speed.MPH -> " mph"
                        Speed.MPS -> " m/s"
                    }
                )
                WINDDIRECTION -> append("°")
                UVINDEX, AQI -> append("")
                FREEZINGLEVELHEIGHT -> append(
                    when (appSettings.lengthUnit){
                        Length.SI -> " m"
                        Length.IMPERIAL -> " ft"
                    }
                )
                DIRECTRADIATION, DIRECTNORMALIRRADIANCE -> {
                    append(" W/m")
                    withStyle(
                        style = SpanStyle(
                            fontSize = 12.sp,
                            baselineShift = BaselineShift.Superscript
                        )
                    ){
                        append("2")
                    }
                }
                else -> {
                    withStyle(
                        style = SpanStyle(
                            fontFamily = Font(R.font.cmu_serif_italic).toFontFamily()
                        )
                    ){ append(" µ") }
                    append("g/m")
                    withStyle(
                        style = SpanStyle(
                            fontSize = 12.sp,
                            baselineShift = BaselineShift.Superscript
                        )
                    ){ append("3") }
                }
            }
        }
    }

    fun floatingPointDigits(appSettings: AppSettings) =
        when (this@WeatherQuantity){
            TEMPERATURE, DEWPOINT, APPARENTTEMP, VISIBILITY, WINDSPEED, UVINDEX, PM10, PM2_5, NO2, SO2, Ozone -> 1
            PRECIPITATION, RAIN, SHOWERS -> when (appSettings.lengthUnit){
                Length.SI -> 1
                Length.IMPERIAL -> 3
            }
            SNOWFALL -> when (appSettings.lengthUnit){
                Length.SI -> 1
                Length.IMPERIAL -> 2
            }
            SURFACEPRESSURE -> when (appSettings.pressureUnit){
                Pressure.Pa -> 0
                Pressure.BAR -> 3
                Pressure.ATM -> 3
                Pressure.PSI -> 2
            }
            else -> 0
        }



    fun graphType() = when (this){
        PRECIPITATION, RAIN, SHOWERS, SNOWFALL -> GraphTypes.STEP
        CLOUDCOVER, VISIBILITY, WINDDIRECTION, AQI -> GraphTypes.LINEAR
        else -> GraphTypes.CUBIC
    }

    fun molWeight() = when (this){
        CO -> 28.01f
        NO2 -> 46.01f
        SO2 -> 64.07f
        Ozone -> 48f
        else -> 0f
    }

    fun quantityBreakPoints() = when (this){
        CO -> listOf(4500, 9500, 12500, 15500, 30500, 50500)
        NO2 -> listOf(54, 100, 360, 650, 1250, 2050)
        SO2 -> listOf(35, 75, 185, 305, 605, 1005)
        Ozone -> listOf(55, 125, 165, 205, 405, 605)
        else -> emptyList()
    }
}


enum class DailyWeatherQuantity(
    @StringRes val nameId: Int,
    @DrawableRes val iconId: Int,
    val floatingPointDigits: Int = 1,
    val brushColors: List<Color>
) {
    TEMPERATUREMINMAX(
        nameId = R.string.temperature_MIN_MAX,
        iconId = R.drawable.temperature,
        brushColors = listOf(BarRed, BarBlue)
    ),
    SUNRISESET(
        nameId = R.string.sun_rise_set,
        iconId = R.drawable.sun_rise_set,
        brushColors = listOf(BarOrange, BarYellow, BarOrange)
    ),
    UVINDEXMAX(
        nameId = R.string.uv_index_MAX,
        iconId = R.drawable.uv,
        brushColors = listOf(
            UVGreen, UVGreen, UVGreen, UVYellow, UVYellow, UVYellow, UVOrange, UVOrange,
            UVRed, UVRed, UVRed, UVMagenta
        )
    ),
    PRECIPITATIONSUM(
        nameId = R.string.precipitation_sum,
        iconId = R.drawable.precipitation,
        brushColors = listOf(curveBlue, curveBlue)
    ),
    PRECIPITATIONPROBABILITYMAX(
        nameId = R.string.precipitation_probability_MAX,
        iconId = R.drawable.precipitation,
        floatingPointDigits = 0,
        brushColors = listOf(curveGreen, curveGreen)
    ),
    WINDSPEEDMAX(
        nameId = R.string.wind_speed_MAX,
        iconId = R.drawable.wind,
        brushColors = listOf(curvePink, curvePink)
    );

    fun unit(appSettings: AppSettings): AnnotatedString{
        return buildAnnotatedString{
            when (this@DailyWeatherQuantity){
                TEMPERATUREMINMAX -> {
                    append(when (appSettings.temperatureUnit){
                        Temperature.CELSIUS -> "° C"
                        Temperature.FAHRENHEIT -> "° F"
                        Temperature.KELVIN -> " K"
                    })
                }
                SUNRISESET, UVINDEXMAX -> append("")
                PRECIPITATIONPROBABILITYMAX -> append(" %")
                PRECIPITATIONSUM -> append(
                    when (appSettings.lengthUnit){
                        Length.SI -> " mm"
                        Length.IMPERIAL -> " inch"
                    }
                )
                WINDSPEEDMAX -> append(
                    when (appSettings.speedUnit){
                        Speed.KMPH -> " km/h"
                        Speed.MPH -> " mph"
                        Speed.MPS -> " m/s"
                    }
                )
            }
        }
    }

    fun color(fraction: Float = 0f) = when(this){
        TEMPERATUREMINMAX -> lerpMultipleColors(brushColors.reversed(), fraction.coerceIn(0f, 1f))
        SUNRISESET -> lerpMultipleColors(brushColors, fraction.coerceIn(0f, 1f))
        UVINDEXMAX -> lerpMultipleColors(brushColors, fraction.coerceIn(0f, 1f))
        PRECIPITATIONSUM -> curveBlue
        PRECIPITATIONPROBABILITYMAX -> lerpMultipleColors(brushColors, fraction.coerceIn(0f, 1f))
        WINDSPEEDMAX -> curvePink
    }
}

fun lerpMultipleColors(colors: List<Color>, fraction: Float): Color {
    return when (colors.size){
        0 -> Transparent
        1 -> colors[0]
        2 -> lerp(colors[0], colors[1], fraction)
        else -> {
            if (fraction == 1f) colors.last()
            else {
                val index1 = (fraction * (colors.size - 1)).toInt()
                val index2 = index1 + 1
                lerp(colors[index1], colors[index2], fraction * (colors.size - 1) - index1)
            }
        }
    }
}



