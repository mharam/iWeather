package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

enum class GraphTypes{
    STEP, LINEAR, CUBIC
}

fun generateCubicGraph2(
    data: List<Float?>,
    controlPoints1: List<Offset?>,
    controlPoints2: List<Offset?>,
    firstPointX: Float,
    diagramWidth: Float,
    diagramHeight: Float,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxisRange: ClosedFloatingPointRange<Float>,
    horizontalPadding: Float,
    verticalPadding: Float
): List<Path> {
    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val yAxisLength = yAxisRange.endInclusive - yAxisRange.start

    val dataList = data.nonNullDataSegments()
    val allPaths = mutableListOf<Path>()
    var x: Float

    dataList.forEach {
        x = firstPointX + it.key
        val path = Path()
        val firstPointY = it.value[0]
        path.moveTo(
            x = normalizedX(x, xAxisRange.start,xAxisLength, horizontalPadding, diagramWidth),
            y = normalizedY(firstPointY, yAxisRange.start, yAxisLength, verticalPadding, diagramHeight)
        )
        it.value.subList(1, it.value.size).forEachIndexed { i, y ->
            path.cubicTo(
                x1 = normalizedX(x + controlPoints1[i+it.key]!!.x, xAxisRange.start,
                    xAxisLength, horizontalPadding, diagramWidth),
                y1 = normalizedY(controlPoints1[i+it.key]!!.y, yAxisRange.start, yAxisLength,
                    verticalPadding, diagramHeight),
                x2 = normalizedX(x + controlPoints2[i+it.key]!!.x, xAxisRange.start,
                    xAxisLength, horizontalPadding, diagramWidth),
                y2 = normalizedY(controlPoints2[i+it.key]!!.y, yAxisRange.start, yAxisLength,
                    verticalPadding, diagramHeight),
                x3 = normalizedX(x + 1, xAxisRange.start,xAxisLength, horizontalPadding, diagramWidth),
                y3 = normalizedY(y, yAxisRange.start, yAxisLength, verticalPadding, diagramHeight)
            )
            x++
        }
        allPaths.add(path)
    }
    return allPaths
}

fun <E> Iterable<E>.indicesOf(e: E) = mapIndexedNotNull{ index, elem -> index.takeIf{ elem == e } }
fun List<Float?>.nonNullDataSegments(): Map<Int, List<Float>>{
    val firstNonNullDataIndex = indexOfFirst { it != null }
    val lastNonNullDataIndex = indexOfLast { it != null }
    if (firstNonNullDataIndex == -1 || lastNonNullDataIndex == -1)
        return emptyMap()

    val nullIndices = subList(firstNonNullDataIndex, lastNonNullDataIndex+1)
        .indicesOf(null)
        .map { it + firstNonNullDataIndex }

    val dataList = mutableMapOf<Int, List<Float>>()
    if (nullIndices.isNotEmpty()) {
        dataList[firstNonNullDataIndex] = subList(firstNonNullDataIndex, nullIndices[0]) as List<Float>
        for (i in 0 until nullIndices.size - 1) {
            if (nullIndices[i]+1 < nullIndices[i + 1])
                dataList[nullIndices[i]+1] = (subList(nullIndices[i] + 1, nullIndices[i + 1]) as List<Float>)
        }
        dataList[nullIndices.last() + 1] = subList(nullIndices.last() + 1, lastNonNullDataIndex + 1) as List<Float>
    } else
        dataList[firstNonNullDataIndex] = subList(firstNonNullDataIndex, lastNonNullDataIndex+1) as List<Float>

    return dataList
}

fun generateCubicGraph(
    data: List<Float?>,
    controlPoints1: List<Offset?>,
    controlPoints2: List<Offset?>,
    firstPointX: Float,
    diagramWidth: Float,
    diagramHeight: Float,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxisRange: ClosedFloatingPointRange<Float>,
    horizontalPadding: Float,
    verticalPadding: Float
): Path? {
    val firstNonNullIndex = data.indexOfFirst { it != null }
    if (data.isEmpty() || firstNonNullIndex == -1) return null

    val path = Path()
    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val yAxisLength = yAxisRange.endInclusive - yAxisRange.start
    val firstPointY = data.first { it != null } as Float
    path.moveTo(
        x = normalizedX(firstPointX + firstNonNullIndex, xAxisRange.start,
            xAxisLength, horizontalPadding, diagramWidth),
        y = normalizedY(firstPointY, yAxisRange.start, yAxisLength, verticalPadding, diagramHeight)
    )
    data.forEachIndexed { i, y ->
        if (y != null && i != firstNonNullIndex){
            val x = firstPointX + i
            path.cubicTo(
                x1 = normalizedX(controlPoints1[i-1]!!.x + x - 1, xAxisRange.start,xAxisLength,
                    horizontalPadding, diagramWidth),
                y1 = normalizedY(controlPoints1[i-1]!!.y, yAxisRange.start, yAxisLength, verticalPadding, diagramHeight),
                x2 = normalizedX(controlPoints2[i-1]!!.x + x - 1, xAxisRange.start,xAxisLength,
                    horizontalPadding, diagramWidth),
                y2 = normalizedY(controlPoints2[i-1]!!.y, yAxisRange.start, yAxisLength, verticalPadding, diagramHeight),
                x3 = normalizedX(x, xAxisRange.start,xAxisLength, horizontalPadding, diagramWidth),
                y3 = normalizedY(y, yAxisRange.start, yAxisLength, verticalPadding, diagramHeight)
            )
        }
    }
    return path
}

fun generateStepGraph(
    data: List<Float?>,
    firstPointX: Float,
    diagramWidth: Float,
    diagramHeight: Float,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxisRange: ClosedFloatingPointRange<Float>,
    horizontalPadding: Float,
    verticalPadding: Float,
    reverse: Boolean = false,
    lastPointX: Float = 0f,
    phase: Float = 0f
): Path? {
    val newData = if (reverse) data.reversed() else data
    val firstNonNullIndex = newData.indexOfFirst { it != null }
    if (data.isEmpty() || firstNonNullIndex == -1) return null

    val path = Path()
    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val yAxisLength = yAxisRange.endInclusive - yAxisRange.start
    val firstPointY = newData[firstNonNullIndex + 1] as Float
    path.moveTo(
        x = normalizedX((if (reverse) lastPointX + phase else firstPointX - phase) + firstNonNullIndex,
            xAxisRange.start, xAxisLength, horizontalPadding, diagramWidth),
        y = normalizedY(firstPointY, yAxisRange.start, yAxisLength, verticalPadding, diagramHeight)
    )
    val oneStepDx = diagramWidth / xAxisLength
    val oneStepDy = diagramHeight / yAxisLength
    newData.forEachIndexed { i, y ->
        if (i < newData.size - 1 && i > firstNonNullIndex){
            path.relativeLineTo(dx = if (reverse) -oneStepDx else oneStepDx, dy = 0f)
            path.relativeLineTo(
                dx = 0f,
                dy = ((y?:0f) - (newData[i+1]?:0f)) * oneStepDy
            )
        }
    }
    path.relativeLineTo(dx = if (reverse) -oneStepDx else oneStepDx, dy = 0f)
    return path
}

fun generateLinearGraph(
    data: List<Float?>,
    firstPointX: Float,
    diagramWidth: Float,
    diagramHeight: Float,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxisRange: ClosedFloatingPointRange<Float>,
    horizontalPadding: Float,
    verticalPadding: Float,
    reverse: Boolean = false,
    lastPointX: Float = 0f
): Path? {
    val newData = if (reverse) data.reversed() else data
    val firstNonNullIndex = newData.indexOfFirst { it != null }
    if (newData.isEmpty() || firstNonNullIndex == -1) return null

    val path = Path()
    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val yAxisLength = yAxisRange.endInclusive - yAxisRange.start
    val firstPointY = newData[firstNonNullIndex]!!

    path.moveTo(
        x = normalizedX(if (reverse) lastPointX else firstPointX, xAxisRange.start, xAxisLength,
            horizontalPadding, diagramWidth),
        y = normalizedY(firstPointY, yAxisRange.start, yAxisLength, verticalPadding, diagramHeight)
    )
    var x = if (reverse) lastPointX else firstPointX
    newData.forEach { y ->
        if (y != null) {
            path.lineTo(
                x = normalizedX(x, xAxisRange.start, xAxisLength, horizontalPadding, diagramWidth),
                y = normalizedY(y, yAxisRange.start, yAxisLength, verticalPadding, diagramHeight)
            )
        }
        if (reverse) x-- else x++
    }
    return path
}

//fun generateWindDirectionLinearGraph(
//    data: List<Float?>,
//    firstPointX: Float,
//    diagramWidth: Float,
//    diagramHeight: Float,
//    xAxisRange: ClosedFloatingPointRange<Float>,
//    yAxisRange: ClosedFloatingPointRange<Float>,
//    horizontalPadding: Float,
//    verticalPadding: Float,
//    reverse: Boolean = false,
//    lastPointX: Float = 0f
//): Path? {
//    val newData = if (reverse) data.reversed() else data
//    val firstNonNullIndex = newData.indexOfFirst { it != null }
//    if (newData.isEmpty() || firstNonNullIndex == -1) return null
//
//    val path = Path()
//    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
//    val yAxisLength = yAxisRange.endInclusive - yAxisRange.start
//    val firstPointY = newData[firstNonNullIndex]!!
//
//    path.moveTo(
//        x = normalizedX(if (reverse) lastPointX else firstPointX, xAxisRange.start, xAxisLength,
//            horizontalPadding, diagramWidth),
//        y = normalizedY(firstPointY, yAxisRange.start, yAxisLength, verticalPadding, diagramHeight)
//    )
//    var x = if (reverse) lastPointX else firstPointX
//    var previousY = firstPointY
//    var displace = 0f
//    newData.subList(firstNonNullIndex+1, newData.size).forEachIndexed { index, y ->
//        if (y != null) {
//            displace = (y - previousY)
//            while (displace > 180) displace -= 360
//            while (displace < -180) displace += 360
//
//            path.lineTo(
//                x = normalizedX(x, xAxisRange.start, xAxisLength, horizontalPadding, diagramWidth),
//                y = normalizedY(previousY + displace, yAxisRange.start, yAxisLength,
//                    verticalPadding, diagramHeight)
//            )
//            previousY += displace
//        }
//        if (reverse) x-- else x++
//    }
//    return path
//}

fun normalizedX(
    rawX: Float,
    xAxisStart: Float,
    xAxisLength: Float,
    horizontalPadding: Float,
    diagramWidth: Float
) = ((rawX - xAxisStart) / xAxisLength) * diagramWidth + horizontalPadding

fun normalizedY(
    rawY: Float,
    yAxisStart: Float,
    yAxisLength: Float,
    verticalPadding: Float,
    diagramHeight: Float
) = (1 - (rawY - yAxisStart) / yAxisLength) * diagramHeight + verticalPadding


fun generateDailyStepGraph(
    data: List<Float?>,
    firstPointX: Float,
    diagramWidth: Float,
    diagramHeight: Float,
    xAxisRange: ClosedFloatingPointRange<Float>,
    yAxisRange: ClosedFloatingPointRange<Float>,
    horizontalPadding: Float,
    verticalPadding: Float,
    reverse: Boolean = false,
    lastPointX: Float = 0f,
    phase: Float = 0f
): Path? {
    val newData = if (reverse) data.reversed() else data
    val firstNonNullIndex = newData.indexOfFirst { it != null }
    if (data.isEmpty() || firstNonNullIndex == -1) return null

    val path = Path()
    val xAxisLength = xAxisRange.endInclusive - xAxisRange.start
    val yAxisLength = yAxisRange.endInclusive - yAxisRange.start
    val firstPointY = newData[firstNonNullIndex] as Float
    path.moveTo(
        x = normalizedX((if (reverse) lastPointX + phase else firstPointX - phase) + firstNonNullIndex,
            xAxisRange.start, xAxisLength, horizontalPadding, diagramWidth),
        y = normalizedY(firstPointY, yAxisRange.start, yAxisLength, verticalPadding, diagramHeight)
    )
    val oneStepDx = diagramWidth / xAxisLength
    val oneStepDy = diagramHeight / yAxisLength
    newData.forEachIndexed { i, y ->
        if (i < newData.size - 1 && i >= firstNonNullIndex){
            path.relativeLineTo(dx = if (reverse) -oneStepDx else oneStepDx, dy = 0f)
            path.relativeLineTo(
                dx = 0f,
                dy = ((y?:0f) - (newData[i+1]?:0f)) * oneStepDy
            )
        }
    }
    path.relativeLineTo(dx = if (reverse) -oneStepDx else oneStepDx, dy = 0f)
    return path
}