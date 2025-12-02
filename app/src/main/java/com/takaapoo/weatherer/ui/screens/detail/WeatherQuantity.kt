package com.takaapoo.weatherer.ui.screens.detail

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
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
import com.takaapoo.weatherer.ui.theme.Cmu
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.UVGreen
import com.takaapoo.weatherer.ui.theme.UVMagenta
import com.takaapoo.weatherer.ui.theme.UVOrange
import com.takaapoo.weatherer.ui.theme.UVRed
import com.takaapoo.weatherer.ui.theme.UVYellow
import com.takaapoo.weatherer.ui.theme.curveBlue
import com.takaapoo.weatherer.ui.theme.curveGreen
import com.takaapoo.weatherer.ui.theme.curveMagenta
import com.takaapoo.weatherer.ui.theme.curveOrange
import com.takaapoo.weatherer.ui.theme.curvePink
import kotlin.math.abs
import kotlin.math.pow

interface WeatherQuantity {
    @get:StringRes
    val nameId: Int?

    @get:DrawableRes
    val iconId: Int

    val title: AnnotatedString?

    fun unit(appSettings: AppSettings): AnnotatedString
    fun floatingPointDigits(appSettings: AppSettings): Int
}

enum class HourlyWeatherQuantity(
    @param:StringRes override val nameId: Int? = null,
    override val title: AnnotatedString? = null,
    @param:StringRes val homeCardNameId: Int? = null,
    val airQuality: Boolean = false,
    val serverName: String? = null,
    val isHistorical: Boolean = false,
    @param:DrawableRes override val iconId: Int,
    val stepCurve: Boolean = false,
): WeatherQuantity {
    TEMPERATURE(
        nameId = R.string.temperature,
        isHistorical = true,
        iconId = R.drawable.temperature,
        serverName = "temperature_2m"
    ),
    HUMIDITY(
        nameId = R.string.humidity,
        homeCardNameId = R.string.humidity,
        isHistorical = true,
        iconId = R.drawable.humidity,
        serverName = "relativehumidity_2m"
    ),
    DEWPOINT(
        nameId = R.string.dew_point,
        homeCardNameId = R.string.dew_point,
        isHistorical = true,
        iconId = R.drawable.dew_point,
        serverName = "dewpoint_2m"
    ),
    APPARENTTEMP(
        nameId = R.string.apparent_temp,
        homeCardNameId = R.string.apparent_temp,
        isHistorical = true,
        iconId = R.drawable.temperature,
        serverName = "apparent_temperature"
    ),
    PRECIPITATIONPROBABILITY(
        nameId = R.string.precipitation_probability,
        homeCardNameId = R.string.precipitation_probability,
        iconId = R.drawable.precipitation
    ),
    PRECIPITATION(
        nameId = R.string.precipitation,
        homeCardNameId = R.string.precipitation_amount,
        isHistorical = true,
        iconId = R.drawable.precipitation,
        serverName = "precipitation",
        stepCurve = true
    ),
    RAIN(
        nameId = R.string.rain,
        isHistorical = true,
        iconId = R.drawable.precipitation,
        serverName = "rain",
        stepCurve = true
    ),
    SHOWERS(
        nameId = R.string.showers,
        iconId = R.drawable.precipitation,
        stepCurve = true
    ),
    SNOWFALL(
        nameId = R.string.snowfall,
        isHistorical = true,
        iconId = R.drawable.snowy_24px,
        serverName = "snowfall",
        stepCurve = true
    ),
    CLOUDCOVER(
        nameId = R.string.cloud_cover,
        homeCardNameId = R.string.cloud_cover,
        isHistorical = true,
        iconId = R.drawable.cloud_cover,
        serverName = "cloud_cover"
    ),
    SURFACEPRESSURE(
        nameId = R.string.surface_pressure,
        homeCardNameId = R.string.surface_pressure,
        isHistorical = true,
        iconId = R.drawable.pressure,
        serverName = "surface_pressure"
    ),
    VISIBILITY(
        nameId = R.string.visibility,
        homeCardNameId = R.string.visibility,
        iconId = R.drawable.visibility
    ),
    WINDSPEED(
        nameId = R.string.wind_speed,
        homeCardNameId = R.string.wind,
        isHistorical = true,
        iconId = R.drawable.wind,
        serverName = "windspeed_10m"
    ),
    WINDDIRECTION(
        nameId = R.string.wind_direction,
        isHistorical = true,
        iconId = R.drawable.wind,
        serverName = "winddirection_10m"
    ),
    UVINDEX(nameId = R.string.uv_index, homeCardNameId = R.string.uv_index, iconId = R.drawable.uv),
    FREEZINGLEVELHEIGHT(
        nameId = R.string.freezing_level_height,
        homeCardNameId = R.string.freezing_level_height,
        iconId = R.drawable.freeze_height
    ),
    DIRECTRADIATION(
        nameId = R.string.direct_radiation,
        homeCardNameId = R.string.direct_radiation,
        iconId = R.drawable.solar_radiation
    ),
    DIRECTNORMALIRRADIANCE(
        nameId = R.string.direct_normal_irradiance,
        homeCardNameId = R.string.direct_normal_irradiance,
        iconId = R.drawable.solar_radiation
    ),

    AQI(nameId = R.string.AQ, homeCardNameId = R.string.AQI, iconId = R.drawable.mask, airQuality = true),
    PM10(
        title = buildAnnotatedString {
            append("PM")
            withStyle(
                style = SpanStyle(
                    fontSize = 12.sp,
                    baselineShift = BaselineShift.Subscript
                )
            ) { append("10") }
        },
        iconId = R.drawable.mask,
        airQuality = true
    ),
    PM2_5(
        title = buildAnnotatedString {
            append("PM")
            withStyle(
                style = SpanStyle(
                    fontSize = 12.sp,
                    baselineShift = BaselineShift.Subscript
                )
            ) { append("2.5") }
        },
        iconId = R.drawable.mask,
        airQuality = true
    ),
    CO(nameId = R.string.CO, iconId = R.drawable.mask, airQuality = true),
    NO2(
        title = buildAnnotatedString {
            append("Nitrogen dioxide (NO")
            withStyle(
                style = SpanStyle(
                    fontSize = 12.sp,
                    baselineShift = BaselineShift.Subscript
                )
            ) { append("2") }
            append(")")
        },
        iconId = R.drawable.mask,
        airQuality = true
    ),
    SO2(
        title = buildAnnotatedString {
            append("Sulphur dioxide (SO")
            withStyle(
                style = SpanStyle(
                    fontSize = 12.sp,
                    baselineShift = BaselineShift.Subscript
                )
            ) { append("2") }
            append(")")
        },
        iconId = R.drawable.mask,
        airQuality = true
    ),
    Ozone(
        title = buildAnnotatedString {
            append("Ozone (O")
            withStyle(
                style = SpanStyle(
                    fontSize = 12.sp,
                    baselineShift = BaselineShift.Subscript
                )
            ) { append("3") }
            append(")")
        },
        iconId = R.drawable.mask,
        airQuality = true
    );


    override fun unit(appSettings: AppSettings): AnnotatedString{
        return buildAnnotatedString{
            when (this@HourlyWeatherQuantity){
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
                            fontFamily = Cmu,
                            fontStyle = FontStyle.Italic
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

    override fun floatingPointDigits(appSettings: AppSettings) =
        when (this@HourlyWeatherQuantity){
            TEMPERATURE, DEWPOINT, APPARENTTEMP, VISIBILITY, UVINDEX, PM10, PM2_5, NO2, SO2, Ozone -> 1
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

    fun quantityValueText(
        quantityValue: Float?,
        appSettings: AppSettings,
        unitVisible: Boolean = true
    ): AnnotatedString {
        val modifiedValue = when (this) {
            HUMIDITY, PRECIPITATIONPROBABILITY, CLOUDCOVER -> quantityValue?.coerceIn(0f, 100f)

            WINDSPEED, UVINDEX, DIRECTRADIATION, DIRECTNORMALIRRADIANCE, Ozone, SO2, CO, NO2, PM10,
            PM2_5 -> quantityValue?.coerceAtLeast(0f)

            else -> quantityValue
        }
        val floatingPointDigits = floatingPointDigits(appSettings)
        return buildAnnotatedString {
            append(
                if (modifiedValue != null) {
                    if (abs(modifiedValue) < 10f.pow(-floatingPointDigits)) "0"
                    else "%.${floatingPointDigits}f".format(modifiedValue)
                } else "?"
            )
            if (unitVisible)
                append(this@HourlyWeatherQuantity.unit(appSettings = appSettings))
        }
    }

    fun graphType(curveSmoothness: Float) = when {
        stepCurve -> GraphTypes.STEP
        curveSmoothness == 0f -> GraphTypes.LINEAR
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

enum class HistoricalState { HISTORIC_ONLY, FORECAST_ONLY, BOTH }

enum class DailyWeatherQuantity(
    @param:StringRes val nameId: Int,
    @param:DrawableRes val iconId: Int,
    val floatingPointDigits: Int = 1,
    val brushColors: List<Color>,
    val serverName: String? = null,
    val historicalState: HistoricalState = HistoricalState.BOTH,
) {
    TEMPERATUREMINMAX(
        nameId = R.string.temperature_MIN_MAX,
        iconId = R.drawable.temperature,
        brushColors = listOf(BarRed, BarBlue),
        serverName = "temperature_2m_max,temperature_2m_min",
    ),
    TEMPERATUREMEAN(
        nameId = R.string.temperature_mean,
        iconId = R.drawable.temperature,
        brushColors = listOf(curveOrange, curveOrange),
        serverName = "temperature_2m_mean",
        historicalState = HistoricalState.HISTORIC_ONLY
    ),
    PRECIPITATIONSUM(
        nameId = R.string.precipitation_sum,
        iconId = R.drawable.precipitation,
        brushColors = listOf(curveBlue, curveBlue),
        serverName = "precipitation_sum",
    ),
    PRECIPITATIONPROBABILITYMAX(
        nameId = R.string.precipitation_probability_MAX,
        iconId = R.drawable.precipitation,
        floatingPointDigits = 0,
        brushColors = listOf(curveGreen, curveGreen),
        historicalState = HistoricalState.FORECAST_ONLY
    ),
    WINDSPEEDMAX(
        nameId = R.string.wind_speed_MAX,
        iconId = R.drawable.wind,
        brushColors = listOf(curvePink, curvePink),
        serverName = "wind_speed_10m_max",
    ),
    SUNRISESET(
        nameId = R.string.sun_rise_set,
        iconId = R.drawable.sun_rise_set,
        brushColors = listOf(BarOrange, BarYellow, BarOrange),
        historicalState = HistoricalState.FORECAST_ONLY
    ),
    UVINDEXMAX(
        nameId = R.string.uv_index_MAX,
        iconId = R.drawable.uv,
        brushColors = listOf(
            UVGreen, UVGreen, UVGreen, UVYellow, UVYellow, UVYellow, UVOrange, UVOrange,
            UVRed, UVRed, UVRed, UVMagenta
        ),
        historicalState = HistoricalState.FORECAST_ONLY
    ),
    CLOUDCOVERMEAN(
        nameId = R.string.cloud_cover_mean,
        iconId = R.drawable.cloud_cover,
        brushColors = listOf(curveMagenta, curveMagenta),
        serverName = "cloud_cover_mean",
        historicalState = HistoricalState.HISTORIC_ONLY
    ),
    HUMIDITYMEAN(
        nameId = R.string.humidity_mean,
        iconId = R.drawable.humidity,
        brushColors = listOf(curveBlue, curveBlue),
        serverName = "relative_humidity_2m_mean",
        historicalState = HistoricalState.HISTORIC_ONLY
    );

    fun unit(appSettings: AppSettings): AnnotatedString {
        return buildAnnotatedString{
            when (this@DailyWeatherQuantity){
                TEMPERATUREMINMAX, TEMPERATUREMEAN -> {
                    append(when (appSettings.temperatureUnit){
                        Temperature.CELSIUS -> "° C"
                        Temperature.FAHRENHEIT -> "° F"
                        Temperature.KELVIN -> " K"
                    })
                }
                SUNRISESET, UVINDEXMAX -> append("")
                PRECIPITATIONPROBABILITYMAX, HUMIDITYMEAN, CLOUDCOVERMEAN -> append(" %")
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
        PRECIPITATIONSUM, HUMIDITYMEAN -> curveBlue
        PRECIPITATIONPROBABILITYMAX -> lerpMultipleColors(brushColors, fraction.coerceIn(0f, 1f))
        WINDSPEEDMAX -> curvePink
        CLOUDCOVERMEAN -> curveMagenta
        TEMPERATUREMEAN -> curveOrange
    }

    fun quantityValueText(
        quantityValue: Float?,
        appSettings: AppSettings,
        unitVisible: Boolean = true
    ): AnnotatedString {
        return buildAnnotatedString {
            append(
                if (quantityValue != null) {
                    if (abs(quantityValue) < 10f.pow(- this@DailyWeatherQuantity.floatingPointDigits)) "0"
                    else "%.${this@DailyWeatherQuantity.floatingPointDigits}f".format(quantityValue)
                } else "?"
            )
            if (unitVisible)
                append(this@DailyWeatherQuantity.unit(appSettings = appSettings))
        }
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



