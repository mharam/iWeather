package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.lerp
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
    while (data[firstDataIndex] == null){
        firstDataIndex--
    }
    while (data[secondDataIndex] == null){
        secondDataIndex++
    }
    val k0 = Offset(x = firstPointX + firstDataIndex, y = data[firstDataIndex]!!)
    val k1 = Offset(
        x = controlPoints1[firstDataIndex]!!.x + firstPointX + firstDataIndex,
        y = controlPoints1[firstDataIndex]!!.y
    )
    val k2 = Offset(
        x = controlPoints2[firstDataIndex]!!.x + firstPointX + firstDataIndex,
        y = controlPoints2[firstDataIndex]!!.y
    )
    val k3 = Offset(x = firstPointX + secondDataIndex, y = data[secondDataIndex]!!)

    val a = -k0 + (k1 - k2)*3f + k3
    val b = k0*3f - k1*6f + k2*3f
    val c = (-k0 + k1)*3f
    val d = k0 - Offset(targetX, 0f)

    val p = (a*c*3f - b*b) / (a*a*3f)
    val q = (b*b*b*2f - a*b*c*9f + a*a*d*27f) / (a*a*a*27f)
    val discriminant = q * q / 4f + p * p * p / 27f
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
    return a.y * targetT.pow(3) + b.y * targetT.pow(2) + c.y * targetT + d.y
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
        || targetX > lastPointX ) return null

    val distance = targetX - firstPointX
    if (distance.isInteger() && data[distance.roundToInt()] != null) return data[distance.roundToInt()]

    val lowerTargetIndex = floor(targetX - firstPointX).roundToInt()
    val upperTargetIndex = ceil(targetX - firstPointX).roundToInt()
    var firstDataIndex = lowerTargetIndex
    var secondDataIndex = upperTargetIndex
    while (data[firstDataIndex] == null && firstDataIndex > firstNonNullIndex){
        firstDataIndex--
    }
    while (data[secondDataIndex] == null && secondDataIndex < lastNonNullIndex){
        secondDataIndex++
    }
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

//fun windDirectionCurveXtoY(
//    data: List<Float?>,
//    firstPointX: Float,
//    lastPointX: Float,
//    targetX: Float
//): Float?{
//    val firstNonNullIndex = data.indexOfFirst { it != null }
//    val lastNonNullIndex = data.indexOfLast { it != null }
//
//    if (data.isEmpty() || firstNonNullIndex == -1 || targetX < firstPointX + firstNonNullIndex
//        || targetX > lastPointX ) return null
//
//    val distance = targetX - firstPointX
//    if (distance.isInteger() && data[distance.roundToInt()] != null) return data[distance.roundToInt()]
//
//    val lowerTargetIndex = floor(targetX - firstPointX).roundToInt()
//    val upperTargetIndex = ceil(targetX - firstPointX).roundToInt()
//    var firstDataIndex = lowerTargetIndex
//    var secondDataIndex = upperTargetIndex
//    while (data[firstDataIndex] == null && firstDataIndex > firstNonNullIndex){
//        firstDataIndex--
//    }
//    while (data[secondDataIndex] == null && secondDataIndex < lastNonNullIndex){
//        secondDataIndex++
//    }
//    val firstDataX = firstDataIndex + firstPointX
//    val secondDataX = secondDataIndex + firstPointX
//    if (firstDataIndex < firstNonNullIndex || secondDataIndex > lastNonNullIndex)
//        return null
//
//    return lerp(
//        start = data[firstDataIndex]!!,
//        stop = data[secondDataIndex]!!,
//        fraction = (targetX - firstDataX) / (secondDataX - firstDataX)
//    )
//}

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
    while (data[secondDataIndex] == null){
        secondDataIndex++
    }
    return data[secondDataIndex]!!
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

