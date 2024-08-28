package com.takaapoo.weatherer.domain.model

import androidx.datastore.core.Serializer
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class AppSettings(
    val temperatureUnit: Temperature = Temperature.CELSIUS,
    val lengthUnit: Length = Length.METRIC,
    val pressureUnit: Pressure = Pressure.HPa,
    val hourlyDiagramWeatherConditionIconVisible: Boolean = false,
    val dailyDiagramWeatherConditionIconVisible: Boolean = false,
    val hourlyDotsOnCurveVisible: Boolean = false,
    val hourlyCurveShadowVisible: Boolean = true,
    val hourlySunRiseSetIconsVisible: Boolean = false,
    val hourlyChartGrid: ChartGrids = ChartGrids.All,
    val hourlyChartTheme: ChartTheme = ChartTheme.APPTHEME
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
