package com.takaapoo.weatherer.domain.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.Serializer
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.theme.NotifCurveGreen
import com.takaapoo.weatherer.ui.theme.NotifCurveRed
import com.takaapoo.weatherer.ui.theme.curveBlue
import com.takaapoo.weatherer.ui.theme.curveGreen
import com.takaapoo.weatherer.ui.theme.curveOrange
import com.takaapoo.weatherer.ui.theme.curvePink
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

enum class AppTheme(
    @StringRes val description: Int
) {
    LIGHT(description = R.string.light_theme),
    DARK(description = R.string.dark_theme),
    SYSTEM(description = R.string.default_theme)
}

enum class SettingsPaneContent {
    MAIN, GENERAL, UNIT, NOTIFICATION, WIDGET, CARD_WEATHER_QUANTITY, HOURLY_DIAGRAM
}

enum class HourlyDiagramTime(@StringRes val title: Int, @StringRes val description: Int) {
    UTC(title = R.string.hourly_diagram_utc_title , description = R.string.hourly_diagram_utc),
    DEVICE(title = R.string.hourly_diagram_device_title, description = R.string.hourly_diagram_device),
    LOCATION(title = R.string.hourly_diagram_location_title, description = R.string.hourly_diagram_location)
}


@Stable
@Serializable
data class AppSettings(
    val hasDonated: Boolean = false,
    val temperatureUnit: Temperature = Temperature.CELSIUS,
    val lengthUnit: Length = Length.SI,
    val pressureUnit: Pressure = Pressure.Pa,
    val speedUnit: Speed = Speed.KMPH,
    val silent: Boolean = false,
    val vibration: Boolean = true,
    val screenOn: Boolean = false,
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = false,
    val clockGaugeVisibility: Boolean = true,
    val notificationVisibility: Boolean = false,
    val notificationLocationId: Int = 5,
    val lastNotificationHeaderSelected: Int = 1,
    val displayNotificationTrigger: Boolean = false,
    val homeCardQuantityUnitsVisibility: Boolean = true,

//    val notificationCount: Int = 0,

    val hourlyDiagramWeatherConditionIconVisible: Boolean = false,
    val dailyDiagramWeatherConditionIconVisible: Boolean = false,
    val hourlyDotsOnCurveVisible: Boolean = false,
    val hourlyCurveShadowVisible: Boolean = true,
    val hourlySunRiseSetIconsVisible: Boolean = false,
    val hourlyChartGrid: ChartGrids = ChartGrids.All,
    val hourlyChartTheme: ChartTheme = ChartTheme.APPTHEME,
    val hourlyDiagramTime: HourlyDiagramTime = HourlyDiagramTime.LOCATION,
//    val hourlyDiagramLinearCurves: Set<WeatherQuantity> =
//        setOf(WeatherQuantity.CLOUDCOVER, WeatherQuantity.VISIBILITY, /*WeatherQuantity.WINDDIRECTION,*/
//            WeatherQuantity.AQI),
    val clockGaugeLock: Boolean = false,
    val cardWeatherQuantities: List<WeatherQuantity> =
        listOf(WeatherQuantity.HUMIDITY, WeatherQuantity.PRECIPITATIONPROBABILITY,
            WeatherQuantity.WINDSPEED, WeatherQuantity.AQI),
    val curvesColor: List<Int> = listOf(curveBlue, curvePink, curveGreen, curveOrange).map { it.toArgb() },
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
    ),
    val notificationCurrentQuantities: List<WeatherQuantity> = listOf(
        WeatherQuantity.APPARENTTEMP,
        WeatherQuantity.DIRECTNORMALIRRADIANCE,
        WeatherQuantity.VISIBILITY,
        WeatherQuantity.AQI,
        WeatherQuantity.SURFACEPRESSURE,
        WeatherQuantity.PRECIPITATION,
        WeatherQuantity.UVINDEX,
    ),
    val notificationHourlyDiagramIconsVisibility: Boolean = true,
    val notificationHourlyQuantities: List<WeatherQuantity> = listOf(
        WeatherQuantity.TEMPERATURE,
        WeatherQuantity.PRECIPITATIONPROBABILITY
    ),
    val notificationHourlyCurveShadowVisible: Boolean = true,
    val notificationHourlyDiagramTime: HourlyDiagramTime = HourlyDiagramTime.LOCATION,
    val notificationCurvesColor: List<Int> = listOf(NotifCurveRed, NotifCurveGreen).map { it.toArgb() },

    val notificationDailyQuantity: DailyWeatherQuantity = DailyWeatherQuantity.TEMPERATUREMINMAX,
    val notificationDailyWeatherIconsVisible: Boolean = true,
    val notificationDailyChartGrid: ChartGrids = ChartGrids.All,

    val notificationWeatherIconVisible: Boolean = true,
    val notificationDateTime: HourlyDiagramTime = HourlyDiagramTime.LOCATION,

    val widgetLocationId: Int = 5,
    val widgetDateTime: HourlyDiagramTime = HourlyDiagramTime.LOCATION,
    val widgetHourlyDiagramQuantities: List<WeatherQuantity> = listOf(
        WeatherQuantity.TEMPERATURE,
        WeatherQuantity.PRECIPITATIONPROBABILITY
    ),
)

data class SettingsState(
    val themeDialogVisibility: Boolean = false,
    val diagramThemeDialogVisibility: Boolean = false,
    val diagramGridDialogVisibility: Boolean = false,
    val timeAxisDialogVisibility: Boolean = false,
    val notificationTimeAxisDialogVisibility: Boolean = false,
    val notificationDiagramGridDialogVisibility: Boolean = false,
    val notificationDateTimeDialogVisibility: Boolean = false,
    val notificationLocationDialogVisibility: Boolean = false,
    val previousPaneContent: SettingsPaneContent? = null,
    val paneContent: SettingsPaneContent = SettingsPaneContent.MAIN,
)

data class SettingsUnits(
    val temperatureUnit: Temperature = Temperature.CELSIUS,
    val lengthUnit: Length = Length.SI,
    val pressureUnit: Pressure = Pressure.Pa,
    val speedUnit: Speed = Speed.KMPH
)

fun AppSettings.toSettingsUnits() = SettingsUnits(
    temperatureUnit = temperatureUnit,
    lengthUnit = lengthUnit,
    pressureUnit = pressureUnit,
    speedUnit = speedUnit
)

object AppSettingsSerializer : Serializer<AppSettings> {

    override val defaultValue = AppSettings()

    override suspend fun readFrom(input: InputStream): AppSettings {
        return try {
            Json.decodeFromString(
                deserializer = AppSettings.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = AppSettings.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}
