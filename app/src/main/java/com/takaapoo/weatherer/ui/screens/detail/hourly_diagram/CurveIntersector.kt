package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.lerp
import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asinh
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sinh
import kotlin.math.sqrt

fun cubicCurveXtoY(
    data: List<Float?>,
    controlPoints1: List<Offset?>,
    controlPoints2: List<Offset?>,
    firstPointX: Float,
    lastPointX: Float,
    targetX: Float
): Float?{
    val firstNonNullIndex = data.indexOfFirst { it != null }
    val lastNonNullIndex = data.indexOfLast { it != null }
    if (data.isEmpty() || firstNonNullIndex == -1 || targetX < firstPointX + firstNonNullIndex
        || targetX > lastPointX - (data.size - 1 - lastNonNullIndex)) return null

    val distance = targetX - firstPointX
    if (distance.isInteger() && data[distance.roundToInt()] != null) return data[distance.roundToInt()]

    val lowerTargetIndex = floor(targetX - firstPointX).roundToInt()
    val upperTargetIndex = ceil(targetX - firstPointX).roundToInt()
    var firstDataIndex = lowerTargetIndex
    var secondDataIndex = upperTargetIndex
    if (data[firstDataIndex] == null || data[secondDataIndex] == null)
        return null
//    while (data[firstDataIndex] == null){
//        firstDataIndex--
//    }
//    while (data[secondDataIndex] == null){
//        secondDataIndex++
//    }
//    Log.i("value1", "firstDataIndex = $firstDataIndex , secondDataIndex = $secondDataIndex , targetX = $targetX")
    try {
        val k0 = MyOffset(x = firstPointX.toDouble() + firstDataIndex, y = data[firstDataIndex]!!.toDouble())
        val k1 = MyOffset(
            x = controlPoints1[firstDataIndex]!!.x.toDouble() + firstPointX + firstDataIndex,
            y = controlPoints1[firstDataIndex]!!.y.toDouble()
        )
        val k2 = MyOffset(
            x = controlPoints2[firstDataIndex]!!.x.toDouble() + firstPointX + firstDataIndex,
            y = controlPoints2[firstDataIndex]!!.y.toDouble()
        )
        val k3 = MyOffset(x = firstPointX.toDouble() + secondDataIndex, y = data[secondDataIndex]!!.toDouble())

        val a = -k0 + (k1 - k2)*3.0 + k3
        val b = k0*3.0 - k1*6.0 + k2*3.0
        val c = (-k0 + k1)*3.0
        val d = k0 - MyOffset(targetX.toDouble(), 0.0)

        val p = (a*c*3.0 - b*b) / (a*a*3.0)
        val q = (b*b*b*2.0 - a*b*c*9.0 + a*a*d*27.0) / (a*a*a*27.0)
        val discriminant = q * q / 4.0 + p * p * p / 27.0
        var targetT = (k0.x + k3.x)/2

        val param = ((3*q.x) / (2*p.x)) * sqrt(-3 / p.x)
        when {
            p.x < 0 -> {
                if (abs(param) <= 1) {
                    for (k in 0..2) {
                        targetT = 2 * sqrt(-p.x / 3) * cos(acos(param) / 3 - (2 * PI * k / 3).toFloat())
                        if (targetT - b.x / (3*a.x) in 0f..1f) break
                    }
                } else if (discriminant.x > 0) {
                    targetT = -2 * sign(q.x) * sqrt(-p.x / 3) * cosh(acosh(-param * sign(q.x))/3)
                }
            }
            else -> {
                targetT = -2 * sqrt(p.x / 3) * sinh(asinh(((3*q.x) / (2*p.x)) * sqrt(3 / p.x))/3)
            }
        }
        targetT -= b.x / (3 * a.x)
        val result = a.y * targetT.pow(3) + b.y * targetT.pow(2) + c.y * targetT + d.y
//        Log.i("value1", "a = ${"%.6f".format(a.x)} , ${a.y} , b = ${"%.6f".format(b.x)} , ${b.y} , c = $c , d = $d")
        return result.toFloat()
    } catch (_: Throwable){
        return null
    }
}

fun linearCurveXtoY(
    data: List<Float?>,
    firstPointX: Float,
    lastPointX: Float,
    targetX: Float
): Float?{
    val firstNonNullIndex = data.indexOfFirst { it != null }
    val lastNonNullIndex = data.indexOfLast { it != null }

    if (data.isEmpty() || firstNonNullIndex == -1 || targetX < firstPointX + firstNonNullIndex
        || targetX > lastPointX - (data.size - 1 - lastNonNullIndex)) return null

    val distance = targetX - firstPointX
    if (distance.isInteger() && data[distance.roundToInt()] != null) return data[distance.roundToInt()]

    val lowerTargetIndex = floor(targetX - firstPointX).roundToInt()
    val upperTargetIndex = ceil(targetX - firstPointX).roundToInt()
    var firstDataIndex = lowerTargetIndex
    var secondDataIndex = upperTargetIndex
    if (data[firstDataIndex] == null || data[secondDataIndex] == null)
        return null
//    while (data[firstDataIndex] == null && firstDataIndex > firstNonNullIndex){
//        firstDataIndex--
//    }
//    while (data[secondDataIndex] == null && secondDataIndex < lastNonNullIndex){
//        secondDataIndex++
//    }
    val firstDataX = firstDataIndex + firstPointX
    val secondDataX = secondDataIndex + firstPointX
    if (firstDataIndex < firstNonNullIndex || secondDataIndex > lastNonNullIndex)
        return null

    return lerp(
        start = data[firstDataIndex]!!,
        stop = data[secondDataIndex]!!,
        fraction = (targetX - firstDataX) / (secondDataX - firstDataX)
    )
}

fun stepCurveXtoY(
    data: List<Float?>,
    firstPointX: Float,
    lastPointX: Float,
    targetX: Float
): Float?{
    val firstNonNullIndex = data.indexOfFirst { it != null }
    val lastNonNullIndex = data.indexOfLast { it != null }
    if (data.isEmpty() || firstNonNullIndex == -1 || targetX < firstPointX + firstNonNullIndex
        || targetX > lastPointX - (data.size - 1 - lastNonNullIndex)) return null

    val distance = targetX - firstPointX
    if (distance.isInteger() && data[distance.roundToInt()] != null) return data[distance.roundToInt()]

    var secondDataIndex = ceil(targetX - firstPointX).roundToInt()
//    while (data[secondDataIndex] == null){
//        secondDataIndex++
//    }
    return data.getOrNull(secondDataIndex)
}

fun weatherCurveXtoY(
    quantity: WeatherQuantity,
    appSettings: AppSettings,
    smoothnessFactor: Float? = null,
    hourlyWeather: List<LocalHourlyWeather?>? = null,
    hourlyAirQuality: List<LocalAirQuality?>? = null,
    firstPointX: Float,
    lastPointX: Float,
    targetX: Float
): Float?{
    val data = quantityData(
        weatherData = hourlyWeather,
        airData = hourlyAirQuality,
        weatherQuantity = quantity
    )
    val (controlPoints1, controlPoints2) = quantityControlPoints(
        weatherData = hourlyWeather,
        airData = hourlyAirQuality,
        weatherQuantity = quantity,
        smoothnessFactor = smoothnessFactor ?: appSettings.curvesSmoothness.getOrElse(quantity) { 0f }
    )

    return when (quantity.graphType(smoothnessFactor ?: appSettings.curvesSmoothness.getOrElse(quantity) { 0f })){
        GraphTypes.CUBIC -> {
            val cubicValue = cubicCurveXtoY(
                data = data,
                controlPoints1 = controlPoints1,
                controlPoints2 = controlPoints2,
                firstPointX = firstPointX,
                lastPointX = lastPointX,
                targetX = targetX
            )
            if (cubicValue != Float.NaN && cubicValue != null) cubicValue
            else linearCurveXtoY(
                data = data,
                firstPointX = firstPointX,
                lastPointX = lastPointX,
                targetX = targetX
            )
        }
        GraphTypes.LINEAR -> linearCurveXtoY(
            data = data,
            firstPointX = firstPointX,
            lastPointX = lastPointX,
            targetX = targetX
        )
        GraphTypes.STEP -> stepCurveXtoY(
            data = data,
            firstPointX = firstPointX,
            lastPointX = lastPointX,
            targetX = targetX
        )
    }
}



fun Float.isInteger() = abs(this - this.roundToInt()) < 0.001f
operator fun Offset.times(secondOffset: Offset) = Offset(
    x = this.x * secondOffset.x,
    y = this.y * secondOffset.y
)
operator fun Offset.div(secondOffset: Offset) = Offset(
    x = this.x / secondOffset.x,
    y = this.y / secondOffset.y
)

class MyOffset(val x: Double, val y: Double){

    operator fun times(secondOffset: MyOffset) = MyOffset(
        x = this.x * secondOffset.x,
        y = this.y * secondOffset.y
    )

    operator fun times(value: Double) = MyOffset(
        x = this.x * value,
        y = this.y * value
    )

    operator fun div(secondOffset: MyOffset) = MyOffset(
        x = this.x / secondOffset.x,
        y = this.y / secondOffset.y
    )

    operator fun div(value: Double) = MyOffset(
        x = this.x / value,
        y = this.y / value
    )

    operator fun plus(secondOffset: MyOffset) = MyOffset(
        x = this.x + secondOffset.x,
        y = this.y + secondOffset.y
    )

    operator fun minus(secondOffset: MyOffset) = MyOffset(
        x = this.x - secondOffset.x,
        y = this.y - secondOffset.y
    )

    operator fun unaryMinus() = MyOffset(
        x = -this.x,
        y = -this.y
    )
}
