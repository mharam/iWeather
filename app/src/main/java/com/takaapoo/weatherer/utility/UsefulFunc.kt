package com.takaapoo.weatherer.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TypefaceSpan
import android.util.Size
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.Screen
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.data.local.LocationType
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.measureText
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.indicesOf
import com.takaapoo.weatherer.ui.theme.DarkColorScheme
import com.takaapoo.weatherer.ui.theme.LightColorScheme
import kotlinx.serialization.SerialName
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

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

fun TextUnit.toPx(density: Density) = with(density){
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

fun Float.toAppropriateUnit(quantity: HourlyWeatherQuantity, appSettings: AppSettings): Float = when (quantity){
    HourlyWeatherQuantity.TEMPERATURE, HourlyWeatherQuantity.DEWPOINT, HourlyWeatherQuantity.APPARENTTEMP ->
        this.celsiusToUnit(appSettings.temperatureUnit)
    HourlyWeatherQuantity.PRECIPITATION, HourlyWeatherQuantity.RAIN, HourlyWeatherQuantity.SHOWERS ->
        this.mmToUnit(appSettings.lengthUnit)
    HourlyWeatherQuantity.SNOWFALL -> this.cmToUnit(appSettings.lengthUnit)
    HourlyWeatherQuantity.SURFACEPRESSURE -> this.paToUnit(appSettings.pressureUnit)
    HourlyWeatherQuantity.VISIBILITY -> this.kmToUnit(appSettings.lengthUnit)
    HourlyWeatherQuantity.WINDSPEED -> this.kmphToUnit(appSettings.speedUnit)
    HourlyWeatherQuantity.FREEZINGLEVELHEIGHT -> this.mToUnit(appSettings.lengthUnit)
//    WeatherQuantity.DIRECTRADIATION -> TODO()
//    WeatherQuantity.DIRECTNORMALIRRADIANCE -> TODO()
    else -> this
}

fun Color.darker(factor: Float): Color {
    require(factor in 0f..1f) { "Lightness factor must be between 0 and 1" }
    // Convert to HSL
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val l = (max + min) / 2f

    val newLightness = (l + (0 - l) * factor).coerceIn(0f, 1f)
    return Color.hsl(
        hue = this.hue(),
        saturation = this.saturation(),
        lightness = newLightness
    )
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
    val l = this.lightness()

    if (max == 0f || max == min) return 0f // Fully desaturated (achromatic)
    if (l == 1f || l == 0f) return 0f
//    val hsv = FloatArray(size = 3){_ -> 0f}
//    android.graphics.Color.RGBToHSV((red*255).toInt(), (green*255).toInt(), (blue*255).toInt(), hsv)
    return ((max - min) / (1 - abs(2*l - 1))).coerceIn(0f, 1f)
}
fun Color.lightness(): Float {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    return ((max + min) / 2f).coerceIn(0f, 1f)
}

operator fun Color.times(factor: Float): Color {
    return this.copy(
        red = (this.red * factor).coerceIn(0f, 1f),
        green = (this.green * factor).coerceIn(0f, 1f),
        blue = (this.blue * factor).coerceIn(0f, 1f)
    )
}

operator fun Color.plus(other: Color): Color {
    return this.copy(
        red = (this.red + other.red).coerceIn(0f, 1f),
        green = (this.green + other.green).coerceIn(0f, 1f),
        blue = (this.blue + other.blue).coerceIn(0f, 1f)
    )
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
    style: TextStyle = LocalTextStyle.current,
) {
    Box(modifier = modifier) {
        Text(
            modifier = Modifier.align(alignment),
            text = text,
            fontFamily = fontFamily,
            color = strokeColor ?: fillColor,
            fontSize = fontSize,
            lineHeight = if (fontSize == TextUnit.Unspecified) TextUnit.Unspecified else fontSize * 1.5f,
            style = style.copy(drawStyle = Stroke(width = strokeWidth)),
        )
        Text(
            modifier = Modifier.align(alignment),
            text = text,
            fontFamily = fontFamily,
            color = fillColor,
            fontSize = fontSize,
            lineHeight = if (fontSize == TextUnit.Unspecified) TextUnit.Unspecified else fontSize * 1.5f,
            style = style
        )
    }
}

fun isDestinationInBackStack(navController: NavController, route: Screen): Boolean {
    return try {
        navController.getBackStackEntry(route)
        true // If no exception, the route is in the back stack
    } catch (_: IllegalArgumentException) {
        false // Route not found in the back stack
    }
}

fun renderViewToBitmap(view: View): Bitmap? {
    return try {
        createBitmap(view.measuredWidth, view.measuredHeight).also { bitmap ->
            val canvas = Canvas(bitmap)
            view.draw(canvas)
        }
    } catch (_: Exception) {
        null
    }
}

fun spToFloat(context: Context, sp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, // The unit to convert from (sp)
        sp, // The value in sp
        context.resources.displayMetrics // Display metrics for scaling
    )
}

fun getWindowSize(windowManager: WindowManager): Size {
    var windowHeight = 0
    var windowWidth = 0

    if (Build.VERSION.SDK_INT > 29) {
        windowHeight = windowManager.currentWindowMetrics.bounds.height()
        windowWidth = windowManager.currentWindowMetrics.bounds.width()
    } else {
        val outSize = Point()
        windowManager.defaultDisplay.getSize(outSize)
        windowHeight = outSize.y
        windowWidth = outSize.x
    }
    return Size(windowWidth, windowHeight)
}

/*fun windowDensity(windowManager: WindowManager): Float {
    return if (Build.VERSION.SDK_INT > 33) {
        windowManager.currentWindowMetrics.density
    } else {
        val displayMetrics = DisplayMetrics()
        windowManager.getDefaultDisplay().getMetrics(displayMetrics)
        displayMetrics.density
    }
}*/

fun getNotificationWidth(windowWidth: Dp, windowHeight: Dp): Dp {
    val compactWidthLimit = 600.dp
    val mediumWidthLimit = 840.dp
    val compactHeightLimit = 480.dp

    return when {
        windowWidth < compactWidthLimit -> windowWidth - (5 * 16 + 24).dp
        windowHeight < compactHeightLimit -> windowHeight - (5 * 16 + 24).dp
        windowWidth < mediumWidthLimit -> windowWidth * 0.65f - (5 * 16 + 24).dp
        windowWidth < 1500.dp -> windowWidth * 0.5f - (5 * 16 + 24).dp
        else -> {
            windowWidth * 0.35f - (5 * 16 + 24).dp
        }
    }
}

fun AnnotatedString.toSpannableWithStyles(context: Context): Spannable {
    // Convert the AnnotatedString to a basic Spannable
    val spannable = SpannableString(this.text)
    val density = Density(context)

    // Iterate over all SpanStyle ranges
    this.spanStyles.forEach { (style, start, end) ->
        // Handle font size
        if (style.fontSize != TextUnit.Unspecified) {
            val fontSizePx = style.fontSize.toPx(density) // Convert Compose TextUnit to pixels

            spannable.setSpan(
                AbsoluteSizeSpan(fontSizePx.toInt(), true), // true for dip (density-independent pixels)
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Handle font family
        if (style.fontFamily != null && Build.VERSION.SDK_INT >= 28) {
            spannable.setSpan(
                TypefaceSpan(ResourcesCompat.getFont(context, R.font.cmu_serif_italic)!!),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Map SpanStyle properties to Android spans
        if (style.color != Color.Unspecified) {
            val color = android.graphics.Color.argb(
                (style.color.alpha * 255).toInt(),
                (style.color.red * 255).toInt(),
                (style.color.green * 255).toInt(),
                (style.color.blue * 255).toInt()
            )
            spannable.setSpan(
                ForegroundColorSpan(color),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        if (style.fontWeight != null || style.fontStyle != null) {
            val typefaceStyle = when {
                style.fontWeight == FontWeight.Bold && style.fontStyle == FontStyle.Italic -> Typeface.BOLD_ITALIC
                style.fontWeight == FontWeight.Bold -> Typeface.BOLD
                style.fontStyle == FontStyle.Italic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            spannable.setSpan(
                StyleSpan(typefaceStyle),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        if (style.baselineShift != BaselineShift.None) {
            val span = when {
                style.baselineShift == BaselineShift.Superscript -> SuperscriptSpan()
                style.baselineShift == BaselineShift.Subscript -> SubscriptSpan()
                else -> null
            }
            if (span != null) {
                spannable.setSpan(
                    span,
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // Add more mappings for other SpanStyle properties (e.g., textDecoration, fontSize, etc.)
    }

    return spannable
}

fun DrawScope.drawThickText(
    textMeasurer: TextMeasurer,
    text: String? = null,
    annotatedString: AnnotatedString? = null,
    textColor: Color = Color.Unspecified,
    borderColor: Color? = null,
    textLeft: Float? = null,
    textRight: Float? = null,
    textCenter: Float? = null,
    textTop: Float? = null,
    textBottom: Float? = null,
    textMiddle: Float? = null,
    fontSize: TextUnit = 14.sp,
    fontFamily: FontFamily = Font(R.font.cmu_serif).toFontFamily(),
    borderWidth: Float = 1.5f,
    rectanglePadding: Dp = 4.dp,
    hasBackgroundRect: Boolean = false,
    backgroundColor: Color = Color.Unspecified,
    backgroundBorderColor: Color = Color.Unspecified,
    backgroundBorderWidth: Float = 4f,
){
    val measuredTextOutline = measureText(
        textMeasurer = textMeasurer,
        text = text,
        annotatedString = annotatedString,
        fontSize = fontSize,
        fontFamily = fontFamily,
        color = borderColor ?: textColor,
        drawStyle = Stroke(width = borderWidth)
    )
    val measuredTextFill = measureText(
        textMeasurer = textMeasurer,
        text = text,
        annotatedString = annotatedString,
        fontSize = fontSize,
        fontFamily = fontFamily,
        color = textColor
    )

    val left = (textLeft ?: textCenter?.minus(measuredTextOutline.size.width / 2f)) ?:
    (textRight!! - measuredTextOutline.size.width)
    val top = (textTop ?: textMiddle?.minus(measuredTextOutline.size.height / 2f)) ?:
    (textBottom!! - measuredTextOutline.size.height)
    if (hasBackgroundRect) {
        val padding = rectanglePadding.toPx()
        val rectSize = androidx.compose.ui.geometry.Size(
            width = measuredTextOutline.size.width + 4 * padding,
            height = measuredTextOutline.size.height + 2 * padding
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(left - 2 * padding, top - padding),
            size = rectSize,
            cornerRadius = CornerRadius(measuredTextOutline.size.height / 2f + padding),
            blendMode = BlendMode.Companion.Clear
        )
        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(left - 2 * padding, top - padding),
            size = rectSize,
            cornerRadius = CornerRadius(measuredTextOutline.size.height / 2f + padding),
        )
        drawRoundRect(
            color = backgroundBorderColor,
            topLeft = Offset(left - 2 * padding, top - padding),
            size = rectSize,
            cornerRadius = CornerRadius(measuredTextOutline.size.height / 2f + padding),
            style = Stroke(backgroundBorderWidth)
        )
    }
    drawText(
        textLayoutResult = measuredTextOutline,
        topLeft = Offset(x = left, y = top)
    )
    drawText(
        textLayoutResult = measuredTextFill,
        topLeft = Offset(x = left, y = top)
    )
}

fun Bitmap.removeTransparentBorder(n: Int, minHeight: Int): Bitmap {
    val width = this.width
    val height = this.height

    var top = 0
    var bottom = height
    var left = 0
    var right = width

    // Scan from top to bottom to find the first non-transparent row
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (this.getPixel(x, y).and(0xFF000000.toInt()) != 0) { // Check if pixel is not transparent
                top = y
                break
            }
        }
        if (top != 0) break // Exit the outer loop
    }

    // Scan from bottom to top to find the first non-transparent row
    for (y in height - 1 downTo top) {
        for (x in 0 until width) {
            if (this[x, y].and(0xFF000000.toInt()) != 0) { // Check if pixel is not transparent
                bottom = y + 1 // Add 1 to include this row
                break
            }
        }
        if (bottom != height) break // Exit the outer loop
    }

    // Scan from left to right to find the first non-transparent column
    for (x in 0 until width) {
        for (y in top until bottom) {
            if (this.getPixel(x, y).and(0xFF000000.toInt()) != 0) { // Check if pixel is not transparent
                left = x
                break
            }
        }
        if (left != 0) break // Exit the outer loop
    }

    // Scan from right to left to find the first non-transparent column
    for (x in width - 1 downTo left) {
        for (y in top until bottom) {
            if (this.getPixel(x, y).and(0xFF000000.toInt()) != 0) { // Check if pixel is not transparent
                right = x + 1 // Add 1 to include this column
                break
            }
        }
        if (right != width) break // Exit the outer loop
    }

    // Adjust bounds to preserve 'n' pixels of transparent border
    top = (top - n).coerceAtLeast(0)
    bottom = (bottom + n).coerceAtMost(height)
    left = (left - n).coerceAtLeast(0)
    right = (right + n).coerceAtMost(width)

    val croppedHeight = bottom - top
    if (croppedHeight < minHeight){
        val extendedHeight = (minHeight - croppedHeight) / 2
        top -= extendedHeight
        bottom += extendedHeight
    }

    // Crop the bitmap to the adjusted bounds
    return if (left < right && top < bottom) {
        Bitmap.createBitmap(this, left, top, right - left, bottom - top)
    } else {
        // If no non-transparent area is found, return the original bitmap
        this
    }
}

fun Color.mostContrastColor(): Color {
    // Calculate relative luminance (WCAG formula)
    val luminance = 0.2126f * red + 0.7152f * green + 0.0722f * blue
    return if (luminance > 0.5f) Color.Black else Color.White
}

fun String.localToUtcTime(utcOffset: Long?) = LocalDateTime.parse(this).minusMinutes(utcOffset ?: 0).toString()

fun <T>List<T>.subList(centerIndex: Int, width: Int) = this.subList(
    fromIndex = (centerIndex - floor(width / 2f).roundToInt()).coerceIn(0, this.size),
    toIndex = (centerIndex + ceil(width / 2f).roundToInt()).coerceIn(0, this.size)
)

fun <T> List<T>.split(element: T): List<List<T>> {
    if (isEmpty()) return emptyList()
    val elementIndices = indicesOf(element).toMutableList()
    elementIndices.add(0, -1)
    elementIndices.add(size)
    val outList = MutableList<List<T>>(size = elementIndices.size - 1) { emptyList() }

    elementIndices.forEachIndexed { i, index ->
        if (i == elementIndices.size - 1) return@forEachIndexed
        outList[i] = subList(index + 1, elementIndices[i+1])
    }

    return outList
}

fun List<LocalDateTime>.oldest(): LocalDateTime? {
    if (this.isEmpty()) return null
    var oldest = this.first()
    for (i in 1 until this.size){
        if (this[i].isBefore(oldest))
            oldest = this[i]
    }
    return oldest
}












fun getColorScheme(context: Context, dynamicColor: Boolean = true): ColorScheme {
    val notificationMinTempColor = ContextCompat.getColor(context, R.color.notif_min_temperature)
    val systemDarkTheme = notificationMinTempColor == 0xFF009FFF.toInt()
    return when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (systemDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                context
            )
        }
        systemDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
}

fun determineLocationType(context: Context, location: LatLng): LocationType {
//    val t0 = System.currentTimeMillis()
    val landPolygons = readPolygonsBinFromAssets(context)
    val caspianPolygon = readPolygonsBinFromAssets(context, "caspian_polygon.bin").first()
    val surroundingPoints = calculateLocationSurroundingPoints(location, 16)
    val surroundingInLand = BooleanArray(size = surroundingPoints.size)
    val center = Point(location.longitude.toFloat(), location.latitude.toFloat())

//    val t1 = System.currentTimeMillis()
    var i = 0
    while (i < landPolygons.size && !landPolygons[i].isPointInside(center)) {
        i++
    }
    val centerInLand = i < landPolygons.size && !caspianPolygon.isPointInside(center)
//    val t2 = System.currentTimeMillis()

    surroundingPoints.forEachIndexed { index, latLng ->
        val surroundingPoint = Point(latLng.longitude.toFloat(), latLng.latitude.toFloat())
        var i = 0
        while (i < landPolygons.size &&
            !landPolygons[i].isPointInside(surroundingPoint)) {
            i++
        }
        surroundingInLand[index] = i < landPolygons.size && !caspianPolygon.isPointInside(surroundingPoint)
    }
//    val t3 = System.currentTimeMillis()
//    Log.i("loc1", "Dt1 = ${t1 - t0} , Dt2 = ${t2 - t1} , Dt3 = ${t3 - t2}")

    return when {
        centerInLand && !surroundingInLand.contains(false) -> LocationType.LAND
        centerInLand && surroundingInLand.contains(false) ||
                !centerInLand && surroundingInLand.contains(true) -> LocationType.BEACH
        else -> LocationType.OCEAN
    }
}

const val earthRadius = 6378
fun calculateLocationSurroundingPoints(location: LatLng, pointsCount: Int): List<LatLng> {
    val radius = 20f
    val delta = (radius / earthRadius)
    val theta0 = abs(location.latitude - 90) * PI/180
    val phi0 = ((location.longitude)) * PI/180
    val alpha = 2*PI/pointsCount
    return List(size = pointsCount) { i ->
        val t = i*alpha
        val lat = 90 - acos(cos(theta0)*cos(delta) + sin(theta0)*sin(delta)*cos(t)) * 180/PI
        val lng = (phi0 +
                atan2(sin(delta)*sin(t), sin(theta0)*cos(delta) - cos(theta0)*sin(delta)*cos(t))
                ) * 180/PI
        LatLng(lat, lng)
    }
}

@Suppress("UNCHECKED_CAST")
fun <R> getPropertyBySerialName(instance: Any, serialNameValue: String): R? {
    return instance::class.memberProperties
        .firstOrNull { property ->
            // Find the property that has a @SerialName annotation with the target value
            property.annotations
                .filterIsInstance<SerialName>()
                .any { it.value == serialNameValue }
        }
        ?.let { property ->
            // Get the property value and cast it to the expected type R
            (property as KProperty1<Any, *>).get(instance) as? R
        }
}

fun landScapeName(
    todayWeather: LocalDailyWeather?,
    currentWeather: LocalHourlyWeather?,
    utcOffset: Long?,
    utcTime: LocalDateTime
): String {
    val sunRise = todayWeather?.sunRise?.let { LocalDateTime.parse(it) }
        ?.minusMinutes(utcOffset ?: 0)
    val sunSet = todayWeather?.sunSet?.let { LocalDateTime.parse(it) }
        ?.minusMinutes(utcOffset ?: 0)

    val isSunRise = sunRise?.let {
        abs(Duration.between(utcTime, it).toMinutes()) <= 30
    } ?: false
    val isSunSet = sunSet?.let {
        abs(Duration.between(utcTime, it).toMinutes()) <= 30
    } ?: false
    val isDay = if (sunRise != null && sunSet != null)
        utcTime.isAfter(sunRise) && utcTime.isBefore(sunSet)
    else (currentWeather?.isDay == 1)

    val timeIndex = if (sunRise?.isEqual(sunSet) == true) {
        if (currentWeather?.isDay == 1) 1 else 3
    } else {
        when {
            isSunRise -> 4
            isSunSet -> 2
            isDay -> 1
            else -> 3
        }
    }
    val skyIndex = WeatherType.fromWMO(
        code = currentWeather?.weatherCode,
        isDay = true,
        moonType = null
    ).picIndex
    val groundIndex = if ((currentWeather?.snowDepth ?: 0f) > 0.1f) 3 else 1

    return "${skyIndex}_${groundIndex}_${timeIndex}"
}