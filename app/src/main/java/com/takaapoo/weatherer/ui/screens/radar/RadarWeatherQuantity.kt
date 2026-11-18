package com.takaapoo.weatherer.ui.screens.radar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
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
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.theme.AQGreen
import com.takaapoo.weatherer.ui.theme.AQMaroon
import com.takaapoo.weatherer.ui.theme.AQMaroonPlus
import com.takaapoo.weatherer.ui.theme.AQOrange
import com.takaapoo.weatherer.ui.theme.AQPurple
import com.takaapoo.weatherer.ui.theme.AQRed
import com.takaapoo.weatherer.ui.theme.AQYellow
import com.takaapoo.weatherer.ui.theme.Cmu
import com.takaapoo.weatherer.ui.theme.UVGreen1
import com.takaapoo.weatherer.ui.theme.UVGreen2
import com.takaapoo.weatherer.ui.theme.UVGreen3
import com.takaapoo.weatherer.ui.theme.UVMagenta1
import com.takaapoo.weatherer.ui.theme.UVMagenta2
import com.takaapoo.weatherer.ui.theme.UVOrange1
import com.takaapoo.weatherer.ui.theme.UVOrange2
import com.takaapoo.weatherer.ui.theme.UVRed1
import com.takaapoo.weatherer.ui.theme.UVRed2
import com.takaapoo.weatherer.ui.theme.UVRed3
import com.takaapoo.weatherer.ui.theme.UVYellow1
import com.takaapoo.weatherer.ui.theme.UVYellow2
import com.takaapoo.weatherer.ui.theme.UVYellow3
import com.takaapoo.weatherer.utility.celsiusToUnit
import com.takaapoo.weatherer.utility.kmToUnit
import com.takaapoo.weatherer.utility.kmphToUnit
import com.takaapoo.weatherer.utility.mToUnit
import com.takaapoo.weatherer.utility.mmToUnit
import kotlin.math.abs
import kotlin.math.pow

enum class RadarWeatherQuantity(
    @param:StringRes val nameId: Int? = null,
    val title: AnnotatedString? = null,
    val airQuality: Boolean = false,
    val serverName: String? = null,
    val serverName2: String? = null,
    @param:DrawableRes val iconId: Int,
    val defaultModel: WeatherModels = WeatherModels.NOAA_GFS_013,
) {
    TEMPERATURE(
        nameId = R.string.temperature,
        serverName = "temperature_2m",
        iconId = R.drawable.temperature,
        defaultModel = WeatherModels.NOAA_GFS_013,
    ),
    HUMIDITY(
        nameId = R.string.humidity,
        serverName = "relative_humidity_2m",
        iconId = R.drawable.humidity,
        defaultModel = WeatherModels.NOAA_GFS_013,
    ),
    DEWPOINT(
        nameId = R.string.dew_point,
        serverName = "dew_point_2m",
        iconId = R.drawable.dew_point,
        defaultModel = WeatherModels.ECMWF_IFS,
    ),
    PRECIPITATIONPROBABILITY(
        nameId = R.string.precipitation_probability,
        serverName = "precipitation_probability",
        iconId = R.drawable.precipitation,
        defaultModel = WeatherModels.ECMWF_IFS_025_ENSEMBLE,
    ),
    PRECIPITATION(
        nameId = R.string.precipitation,
        serverName = "precipitation",
        iconId = R.drawable.precipitation,
        defaultModel = WeatherModels.DWD_GLOBAL,
    ),
    RAIN(
        nameId = R.string.rain,
        serverName = "rain",
        iconId = R.drawable.precipitation,
        defaultModel = WeatherModels.DWD_GLOBAL,
    ),
    SHOWERS(
        nameId = R.string.showers,
        serverName = "showers",
        iconId = R.drawable.precipitation,
        defaultModel = WeatherModels.DWD_GLOBAL,
    ),
    SNOWFALL(
        nameId = R.string.snowfall_water_equivalent,
        iconId = R.drawable.snowy_24px,
        serverName = "snowfall_water_equivalent",
        defaultModel = WeatherModels.DWD_GLOBAL,
    ),
    CLOUDCOVER(
        nameId = R.string.cloud_cover,
        serverName = "cloud_cover",
        iconId = R.drawable.cloud_cover,
        defaultModel = WeatherModels.NOAA_GFS_013,
    ),
    VISIBILITY(
        nameId = R.string.visibility,
        iconId = R.drawable.visibility,
        serverName = "visibility",
        defaultModel = WeatherModels.NOAA_GFS_025,
    ),
    WIND(
        nameId = R.string.wind,
        serverName = "wind_u_component_10m",
        serverName2 = "wind_speed_10m",
        iconId = R.drawable.wind,
        defaultModel = WeatherModels.NOAA_GFS_013,
    ),
    UVINDEX(
        nameId = R.string.uv_index,
        serverName = "uv_index",
        iconId = R.drawable.uv,
        defaultModel = WeatherModels.NOAA_GFS_013,
    ),
    UVINDEXCLEARSKY(
        nameId = R.string.uv_index_clear_sky,
        serverName = "uv_index_clear_sky",
        iconId = R.drawable.uv,
        defaultModel = WeatherModels.NOAA_GFS_013,
    ),
    FREEZINGLEVELHEIGHT(
        nameId = R.string.freezing_level_height,
        serverName = "freezing_level_height",
        iconId = R.drawable.freeze_height,
        defaultModel = WeatherModels.NOAA_GFS_025,
    ),


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
        serverName = "pm10",
        iconId = R.drawable.mask,
        airQuality = true,
        defaultModel = WeatherModels.CAMS_GLOBAL,
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
        serverName = "pm2_5",
        iconId = R.drawable.mask,
        airQuality = true,
        defaultModel = WeatherModels.CAMS_GLOBAL,
    ),
    CO(
        title = buildAnnotatedString {
            append("CO")
        },
        serverName = "carbon_monoxide",
        iconId = R.drawable.mask,
        airQuality = true,
        defaultModel = WeatherModels.CAMS_GLOBAL,
    ),
    NO2(
        title = buildAnnotatedString {
            append("NO")
            withStyle(
                style = SpanStyle(
                    fontSize = 12.sp,
                    baselineShift = BaselineShift.Subscript
                )
            ) { append("2") }
        },
        serverName = "nitrogen_dioxide",
        iconId = R.drawable.mask,
        airQuality = true,
        defaultModel = WeatherModels.CAMS_GLOBAL,
    ),
    SO2(
        title = buildAnnotatedString {
            append("SO")
            withStyle(
                style = SpanStyle(
                    fontSize = 12.sp,
                    baselineShift = BaselineShift.Subscript
                )
            ) { append("2") }
        },
        serverName = "sulphur_dioxide",
        iconId = R.drawable.mask,
        airQuality = true,
        defaultModel = WeatherModels.CAMS_GLOBAL,
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
        serverName = "ozone",
        iconId = R.drawable.mask,
        airQuality = true,
        defaultModel = WeatherModels.CAMS_GLOBAL,
    );



    companion object {
        fun unit(quantity: RadarWeatherQuantity, appSettings: AppSettings): AnnotatedString {
            return buildAnnotatedString{
                when (quantity){
                    TEMPERATURE, DEWPOINT -> {
                        append(when (appSettings.temperatureUnit){
                            Temperature.CELSIUS -> "°C"
                            Temperature.FAHRENHEIT -> "°F"
                            Temperature.KELVIN -> "K"
                        })
                    }
                    HUMIDITY, PRECIPITATIONPROBABILITY, CLOUDCOVER ->
                        append("%")
                    PRECIPITATION, RAIN, SHOWERS, SNOWFALL -> append(
                        when (appSettings.lengthUnit){
                            Length.SI -> "mm"
                            Length.IMPERIAL -> "in"
                        }
                    )
                    FREEZINGLEVELHEIGHT -> append(
                        when (appSettings.lengthUnit){
                            Length.SI -> "m"
                            Length.IMPERIAL -> "ft"
                        }
                    )
                    VISIBILITY -> append(
                        when (appSettings.lengthUnit){
                            Length.SI -> "km"
                            Length.IMPERIAL -> "mi"
                        }
                    )
                    WIND -> append(
                        when (appSettings.speedUnit){
                            Speed.KMPH -> "km/h"
                            Speed.MPH -> "mph"
                            Speed.MPS -> "m/s"
                        }
                    )
                    UVINDEX, UVINDEXCLEARSKY -> append("")
                    else -> {
                        withStyle(
                            style = SpanStyle(
                                fontFamily = Cmu,
                                fontStyle = FontStyle.Italic
                            )
                        ){ append("µ") }
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

        fun coloBarFloatingPointDigits(quantity: RadarWeatherQuantity, appSettings: AppSettings) =
            when (quantity){
                PRECIPITATION, RAIN, SHOWERS, SNOWFALL -> when (appSettings.lengthUnit){
                    Length.SI -> 1
                    Length.IMPERIAL -> 3
                }
                VISIBILITY -> 1
                else -> 0
            }
        fun floatingPointDigits(quantity: RadarWeatherQuantity, appSettings: AppSettings) =
            when (quantity){
                TEMPERATURE, DEWPOINT, WIND, UVINDEX, UVINDEXCLEARSKY, NO2, SO2 -> 1
                PRECIPITATION, RAIN, SHOWERS, SNOWFALL -> when (appSettings.lengthUnit){
                    Length.SI -> 1
                    Length.IMPERIAL -> 3
                }
                VISIBILITY -> 2
                else -> 0
            }

        fun Float.applyUnit(quantity: RadarWeatherQuantity, appSettings: AppSettings) =
            when (quantity){
                TEMPERATURE, DEWPOINT -> celsiusToUnit(appSettings.temperatureUnit)
                PRECIPITATION, RAIN, SHOWERS, SNOWFALL -> mmToUnit(appSettings.lengthUnit)
                VISIBILITY -> (this / 1000f).kmToUnit(appSettings.lengthUnit)
                WIND -> (this * 3.6f).kmphToUnit(appSettings.speedUnit)
                FREEZINGLEVELHEIGHT -> mToUnit(appSettings.lengthUnit)
                else -> this
            }

        fun quantityValueText(
            quantity: RadarWeatherQuantity,
            quantityValue: Float?,
            appSettings: AppSettings,
        ): AnnotatedString {
            val modifiedValue = when (quantity) {
                HUMIDITY, CLOUDCOVER, PRECIPITATIONPROBABILITY -> quantityValue?.coerceIn(0f, 100f)
                PRECIPITATION, RAIN, SHOWERS, SNOWFALL, WIND, SO2, CO, NO2, PM10, PM2_5, Ozone ->
                    quantityValue?.coerceAtLeast(0f)
//                VISIBILITY -> quantityValue?.div(1000)
                else -> quantityValue
            }
            val floatingPointDigits = floatingPointDigits(quantity, appSettings)
            return buildAnnotatedString {
                append(
                    if (modifiedValue != null) {
                        val formatted = if (abs(modifiedValue) < 10f.pow(-floatingPointDigits)) "0"
                        else "%.${floatingPointDigits}f".format(modifiedValue)
                        if ('.' in formatted) {
                            formatted.trimEnd('0').trimEnd('.')
                        } else {
                            formatted
                        }
                    }
                    else ""
                )
            }
        }
        fun colorScale(
            quantity: RadarWeatherQuantity,
            isDark: Boolean,
        ): List<Color> {
            return when (quantity) {
                TEMPERATURE, DEWPOINT, FREEZINGLEVELHEIGHT -> if (isDark)
                    listOf(
                        Color(255, 0, 207),
                        Color(220, 0, 191),
                        Color(190, 0, 163),
                        Color(159, 0, 138),
                        Color(148, 0, 173),
                        Color(115, 0, 174),
                        Color(82, 0, 175),
                        Color(49, 0, 176),
                        Color(28, 0, 176),
                        Color(7, 0, 177),
                        Color(0, 15, 178),
                        Color(0, 36, 178),
                        Color(0, 59, 200),
                        Color(0, 87, 221),
                        Color(0, 117, 243),
                        Color(10, 149, 255),
                        Color(0, 242, 105),
                        Color(0, 217, 78),
                        Color(0, 191, 55),
                        Color(0, 166, 36),
                        Color(0, 140, 21),
                        Color(0, 115, 8),
                        Color(0, 90, 0),
                        Color(28, 112, 0),
                        Color(67, 134, 0),
                        Color(117, 156, 0),
                        Color(178, 178, 0),
                        Color(178, 166, 0),
                        Color(178, 153, 0),
                        Color(178, 141, 0),
                        Color(178, 128, 0),
                        Color(178, 116, 0),
                        Color(178, 97, 0),
                        Color(178, 77, 0),
                        Color(178, 58, 0),
                        Color(178, 38, 0),
                        Color(178, 19, 0),
                        Color(178, 0, 0),
                        Color(188, 0, 15),
                        Color(203, 0, 20),
                        Color(229, 0, 33),
                        Color(255, 0, 51)
                ) else
                    listOf(
                        Color(74, 13, 0),
                        Color(130, 1, 29),
                        Color(185, 4, 114),
                        Color(221, 6, 193),
                        Color(207, 6, 241),
                        Color(163, 5, 243),
                        Color(118, 4, 246),
                        Color(71, 3, 249),
                        Color(41, 2, 250),
                        Color(11, 1, 252),
                        Color(1, 22, 253),
                        Color(0, 52, 255),
                        Color(35, 99, 251),
                        Color(69, 139, 247),
                        Color(102, 171, 245),
                        Color(134, 197, 245),
                        Color(114, 232, 165),
                        Color(78, 232, 133),
                        Color(40, 233, 96),
                        Color(17, 220, 61),
                        Color(9, 191, 36),
                        Color(4, 160, 15),
                        Color(0, 128, 0),
                        Color(40, 160, 0),
                        Color(96, 192, 0),
                        Color(167, 223, 0),
                        Color(255, 255, 0),
                        Color(255, 237, 0),
                        Color(255, 219, 0),
                        Color(255, 201, 0),
                        Color(255, 183, 0),
                        Color(255, 165, 0),
                        Color(255, 138, 0),
                        Color(255, 110, 0),
                        Color(255, 83, 0),
                        Color(255, 55, 0),
                        Color(255, 27, 0),
                        Color(255, 0, 0),
                        Color(228, 0, 10),
                        Color(201, 0, 18),
                        Color(174, 0, 23),
                        Color(147, 0, 26)
                    )
                HUMIDITY -> listOf(
                    Color(0, 128, 0, 0),
                    Color(0, 128, 0, 25),
                    Color(0, 128, 0, 51),
                    Color(0, 128, 0, 76),
                    Color(0, 128, 0, 102),
                    Color(0, 128, 0, 127),
                    Color(0, 141, 28, 170),
                    Color(0, 153, 61, 212),
                    Color(0, 166, 100, 255),
                    Color(0, 179, 143, 255),
                    Color(0, 192, 192, 255),
                    Color(0, 163, 204, 255),
                    Color(0, 130, 217, 255),
                    Color(0, 92, 230, 255),
                    Color(0, 48, 242, 255),
                    Color(0, 0, 255, 255),
                )
                PRECIPITATIONPROBABILITY -> listOf(
                    Color(0, 0, 0, 0),
                    Color(3, 47, 74, 51),
                    Color(135, 206, 250, 91),
                    Color(91, 149, 230, 127),
                    Color(63, 236, 180, 145),
                    Color(81, 245, 32, 161),
                    Color(253, 188, 18, 191),
                    Color(248, 122, 71, 221),
                    Color(247, 80, 80, 242),
                    Color(144, 0, 0, 255)
                )
                PRECIPITATION, RAIN, SHOWERS, SNOWFALL -> listOf(
                    Color(243, 250, 255, 0),
                    Color(135, 206, 250, 102),
                    Color(76, 136, 251, 153),
                    Color(0, 0, 255, 159),
                    Color(4, 115, 227, 170),
                    Color(7, 200, 200, 181),
                    Color(9, 161, 47, 198),
                    Color(31, 163, 9, 211),
                    Color(87, 192, 7, 222),
                    Color(161, 223, 4, 232),
                    Color(255, 255, 0, 240),
                    Color(255, 198, 0, 251),
                    Color(255, 142, 0, 255),
                    Color(255, 71, 0, 255),
                    Color(255, 0, 0, 255),
                    Color(219, 0, 13, 255),
                    Color(147, 0, 26, 255),
                    Color(122, 0, 22, 255),
                    Color(83, 0, 15, 255)
                )
                CLOUDCOVER -> if (isDark)
                    listOf(
                        Color(255, 255, 255, 0),
                        Color(249, 251, 253, 36),
                        Color(244, 247, 250, 102),
                        Color(233, 239, 244, 121),
                        Color(220, 226, 233, 140),
                        Color(209, 213, 219, 159),
                        Color(188, 193, 202, 178),
                        Color(166, 173, 184, 198),
                        Color(138, 147, 162, 217),
                        Color(104, 115, 133, 236),
                        Color(31, 35, 33, 255)
                    ) else
                        listOf(
                            Color(11, 18, 32, 0),
                            Color(31, 43, 64, 36),
                            Color(59, 71, 89, 102),
                            Color(89, 100, 116, 121),
                            Color(120, 131, 148, 140),
                            Color(156, 163, 175, 159),
                            Color(177, 183, 193, 178),
                            Color(198, 203, 210, 198),
                            Color(214, 219, 226, 217),
                            Color(226, 232, 239, 236),
                            Color(241, 245, 249, 255)
                        )
                VISIBILITY -> listOf(
                    Color(106, 62, 0, 255),
                    Color(121, 71, 0, 255),
                    Color(136, 81, 0, 255),
                    Color(166, 101, 0, 255),
                    Color(255, 165, 0, 255),
                    Color(241, 192, 0, 255),
                    Color(192, 213, 0, 255),
                    Color(47, 156, 0, 242),
                    Color(0, 161, 117, 204),
                    Color(0, 39, 211, 127),
                    Color(62, 154, 255, 64),
                    Color(167, 235, 255, 0),
                    Color(255, 255, 255, 0),
                    Color(255, 255, 255, 0)
                )
                WIND -> listOf(
                    Color(255, 255, 255, 0),
                    Color(194, 232, 255, 19),
                    Color(133, 209, 255, 38),
                    Color(12, 162, 255, 76),
                    Color(0, 198, 137, 102),
                    Color(0, 128, 0, 127),
                    Color(96, 192, 0, 178),
                    Color(255, 255, 0, 255),
                    Color(255, 229, 0, 255),
                    Color(255, 197, 0, 255),
                    Color(255, 165, 0, 255),
                    Color(242, 130, 25, 255),
                    Color(231, 107, 48, 255),
                    Color(212, 91, 91, 255),
                    Color(224, 63, 63, 255),
                    Color(255, 0, 0, 255),
                    Color(192, 0, 0, 255),
                    Color(129, 0, 0, 255)
                )
                UVINDEX, UVINDEXCLEARSKY -> listOf(
                    UVGreen1, UVGreen2, UVGreen3, UVYellow1, UVYellow2, UVYellow3, UVOrange1, UVOrange2,
                    UVRed1, UVRed2, UVRed3, UVMagenta1, UVMagenta2
                )
                PM10, PM2_5, CO, NO2, SO2, Ozone -> listOf(
                    AQGreen, AQYellow, AQOrange, AQRed, AQPurple, AQMaroon, AQMaroonPlus
                )
            }
        }
        private val temperatureBreakPoints = listOf(
            -80f, -65f, -50f, -40f, -32f, -28f, -24f, -20f, -17.5f, -15f, -12.5f, -10f, -8f, -6f, -4f,
            -2f, 0f, 2f, 4f, 6f, 8f, 10f, 12f, 14f, 16f, 18f, 20f, 22f, 24f, 26f, 28f, 30f, 32f, 34f,
            36f, 38f, 40f, 42f, 44f, 46f, 48f, 50f
        )
        private const val MOLAR_VOLUME = 22.45f
        private fun molWeight(quantity: RadarWeatherQuantity) = when (quantity){
            CO -> 28.01f
            NO2 -> 46.01f
            SO2 -> 64.07f
            Ozone -> 48f
            else -> 0f
        }
        private fun quantityBreakPoints(quantity: RadarWeatherQuantity) = when (quantity){
            PM10 -> listOf(0f, 55f, 155f, 255f, 355f, 425f, 605f, 1500f)
            PM2_5 -> listOf(0f, 9f, 35.5f, 55.5f, 125.5f, 225.5f, 500f, 1300f)
            CO -> listOf(0f, 4500f, 9500f, 12500f, 15500f, 30500f, 50500f)
            NO2 -> listOf(0f, 54f, 100f, 360f, 650f, 1250f, 2050f)
            SO2 -> listOf(0f, 35f, 75f, 185f, 305f, 605f, 1005f)
            Ozone -> listOf(0f, 55f, 125f, 165f, 205f, 405f, 605f)
            else -> emptyList()
        }
        fun colorBreakpoints(quantity: RadarWeatherQuantity): List<Float> {
            return when (quantity) {
                TEMPERATURE, DEWPOINT -> temperatureBreakPoints
                HUMIDITY -> listOf(
                    0f, 10f, 20f, 30f, 40f, 50f, 55f, 60f, 65f, 70f, 75f, 80f, 85f, 90f, 95f, 100f
                )
                PRECIPITATIONPROBABILITY -> listOf(
                    0f, 1f, 5f, 10f, 20f, 30f, 45f, 60f, 75f, 100f
                )
                PRECIPITATION, RAIN, SHOWERS, SNOWFALL -> listOf(
                    0f, 0.1f, 0.5f, 1f, 2f, 3f, 4.5f, 6f, 8f, 10f, 12f, 16f, 20f, 25f, 30f,
                    40f, 60f, 85f, 125f
                )
                CLOUDCOVER -> listOf(
                    0f, 10f, 20f, 30f, 40f, 50f, 60f, 70f, 80f, 90f, 100f
                )
                VISIBILITY -> listOf(
                    0f, 50f, 100f, 200f, 500f, 1000f, 2000f, 4000f, 7000f, 10000f, 15000f, 20000f, 30000f, 500000f
                )
                WIND -> listOf(
                    0f, 0.5f, 1f, 2f, 3f, 4f, 6f, 8f, 10f, 12.5f, 15f, 17.5f, 20f, 25f, 30f, 40f, 50f, 60f
                )
                UVINDEX, UVINDEXCLEARSKY -> listOf(
                    0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f
                )
                FREEZINGLEVELHEIGHT -> temperatureBreakPoints.map { (it + 20) * 80 }
                PM10, PM2_5 -> quantityBreakPoints(quantity).mapIndexedNotNull { index, value ->
                    val breakPoints = quantityBreakPoints(quantity)
                    if (index < breakPoints.size - 1) (value + breakPoints[index + 1]) / 2f else null
                }
                CO, NO2, SO2, Ozone -> quantityBreakPoints(quantity).mapIndexedNotNull { index, value ->
                    val breakPoints = quantityBreakPoints(quantity)
                    if (index < breakPoints.size - 1) (value + breakPoints[index + 1]) * molWeight(quantity) / (2f * MOLAR_VOLUME)
                    else null
                }
            }
        }



    }
}