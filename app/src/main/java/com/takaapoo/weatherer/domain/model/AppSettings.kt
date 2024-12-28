package com.takaapoo.weatherer.domain.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.datastore.core.Serializer
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
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

@Stable
@Serializable
data class AppSettings(
    val temperatureUnit: Temperature = Temperature.CELSIUS,
    val lengthUnit: Length = Length.SI,
    val pressureUnit: Pressure = Pressure.Pa,
    val speedUnit: Speed = Speed.KMPH,
    val silent: Boolean = false,
    val screenOn: Boolean = false,
    val theme: AppTheme = AppTheme.SYSTEM,
    val clockGaugeVisibility: Boolean = true,

    val hourlyDiagramWeatherConditionIconVisible: Boolean = false,
    val dailyDiagramWeatherConditionIconVisible: Boolean = false,
    val hourlyDotsOnCurveVisible: Boolean = false,
    val hourlyCurveShadowVisible: Boolean = true,
    val hourlySunRiseSetIconsVisible: Boolean = false,
    val hourlyChartGrid: ChartGrids = ChartGrids.All,
    val hourlyChartTheme: ChartTheme = ChartTheme.APPTHEME,
    val clockGaugeLock: Boolean = false
)

data class SettingsState(
    val themeDialogVisibility: Boolean = false
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
