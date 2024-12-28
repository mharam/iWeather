package com.takaapoo.weatherer.ui.utility

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    this[index1] = this[index2].also { this[index2] = this[index1] }
}

fun Int.toDp(density: Density) = with(density){
    this@toDp.toDp()
}

fun Dp.toSp(density: Density) = with(density){
    this@toSp.toSp()
}

fun Dp.toPx(density: Density) = with(density){
    this@toPx.toPx()
}

fun Float.toDp(density: Density) = with(density){
    this@toDp.toDp()
}

fun <T> Collection<T>.containsAny(elements: Collection<T>): Boolean {
    return elements.any { it in this }
}

fun Float.celsiusToUnit(unit: Temperature) = when (unit) {
    Temperature.CELSIUS -> this
    Temperature.FAHRENHEIT -> (this * 9 / 5) + 32
    Temperature.KELVIN -> this + 273.15f
}
fun Float.toCelsius(unit: Temperature) = when (unit) {
    Temperature.CELSIUS -> this
    Temperature.FAHRENHEIT -> (this - 32) * 5/9
    Temperature.KELVIN -> this - 273.15f
}
fun Float.kmphToUnit(unit: Speed) = when (unit) {
    Speed.KMPH -> this
    Speed.MPH -> this * 0.621371f
    Speed.MPS -> this / 3.6f
}
fun Float.toKmph(unit: Speed) = when (unit) {
    Speed.KMPH -> this
    Speed.MPH -> this / 0.621371f
    Speed.MPS -> this * 3.6f
}
fun Float.paToUnit(unit: Pressure) = when (unit) {
    Pressure.Pa -> this
    Pressure.BAR -> this / 1000
    Pressure.PSI -> this * 0.0145038f
    Pressure.ATM -> this * 0.0009869f
}
fun Float.toPa(unit: Pressure) = when (unit) {
    Pressure.Pa -> this
    Pressure.BAR -> this * 1000
    Pressure.PSI -> this * 68.9476f
    Pressure.ATM -> this * 1013.25f
}
fun Float.mmToUnit(unit: Length) = when (unit){
    Length.SI -> this
    Length.IMPERIAL -> this * 0.0393701f
}
fun Float.cmToUnit(unit: Length) = when (unit){
    Length.SI -> this
    Length.IMPERIAL -> this * 0.393701f
}
fun Float.kmToUnit(unit: Length) = when (unit) {
    Length.SI -> this
    Length.IMPERIAL -> this * 0.621371f
}
fun Float.mToUnit(unit: Length) = when (unit) {
    Length.SI -> this
    Length.IMPERIAL -> this * 3.28084f
}

fun Float.toAppropriateUnit(quantity: WeatherQuantity, appSettings: AppSettings): Float = when (quantity){
    WeatherQuantity.TEMPERATURE, WeatherQuantity.DEWPOINT, WeatherQuantity.APPARENTTEMP ->
        this.celsiusToUnit(appSettings.temperatureUnit)
    WeatherQuantity.PRECIPITATION, WeatherQuantity.RAIN, WeatherQuantity.SHOWERS ->
        this.mmToUnit(appSettings.lengthUnit)
    WeatherQuantity.SNOWFALL -> this.cmToUnit(appSettings.lengthUnit)
    WeatherQuantity.SURFACEPRESSURE -> this.paToUnit(appSettings.pressureUnit)
    WeatherQuantity.VISIBILITY -> this.kmToUnit(appSettings.lengthUnit)
    WeatherQuantity.WINDSPEED -> this.kmphToUnit(appSettings.speedUnit)
    WeatherQuantity.FREEZINGLEVELHEIGHT -> this.mToUnit(appSettings.lengthUnit)
//    WeatherQuantity.DIRECTRADIATION -> TODO()
//    WeatherQuantity.DIRECTNORMALIRRADIANCE -> TODO()
    else -> this
}

fun Color.lighter(factor: Float): Color {
    require(factor in 0f..1f) { "Lightness factor must be between 0 and 1" }
    // Convert to HSL
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val l = (max + min) / 2f

    val newLightness = (l + (1 - l) * factor).coerceIn(0f, 1f)
    return Color.hsl(
        hue = this.hue(),
        saturation = this.saturation(),
        lightness = newLightness
    )
}
fun Color.hue(): Float {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)

    if (max == min) return 0f // Achromatic (no hue)
    val delta = max - min
    return when (max) {
        red -> (60 * ((green - blue) / delta) + 360) % 360
        green -> (60 * ((blue - red) / delta) + 120) % 360
        blue -> (60 * ((red - green) / delta) + 240) % 360
        else -> 0f
    }
}
fun Color.saturation(): Float {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)

    if (max == 0f || max == min) return 0f // Fully desaturated (achromatic)
    return (max - min) / max
}

@Composable
fun BorderedText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    fontFamily: FontFamily = Font(R.font.cmu_serif).toFontFamily(),
    strokeColor: Color? = null,
    fillColor: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    strokeWidth: Float = 2f,
    alignment: Alignment = Alignment.CenterStart,
    style: TextStyle = LocalTextStyle.current
) {
    Box(modifier = modifier) {
        Text(
            modifier = Modifier.align(alignment),
            text = text,
            fontFamily = fontFamily,
            color = strokeColor ?: fillColor,
            fontSize = fontSize,
            lineHeight = fontSize * 1.5f,
            style = style.copy(drawStyle = Stroke(width = strokeWidth)),
        )
        Text(
            modifier = Modifier.align(alignment),
            text = text,
            fontFamily = fontFamily,
            color = fillColor,
            fontSize = fontSize,
            lineHeight = fontSize * 1.5f,
            style = style
        )
    }
}
