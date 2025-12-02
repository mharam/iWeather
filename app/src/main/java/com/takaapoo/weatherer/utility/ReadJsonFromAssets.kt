package com.takaapoo.weatherer.utility

import android.content.Context
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.indicesOf
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun readJSONFromAssets(context: Context, path: String): String {
    return try {
        val file = context.assets.open(path)
        val bufferedReader = BufferedReader(InputStreamReader(file))
        val stringBuilder = StringBuilder()
        bufferedReader.useLines { lines ->
            lines.forEach {
                stringBuilder.append(it)
            }
        }
        stringBuilder.toString()
    } catch (_: Exception) {
        ""
    }
}

fun readMoonPhaseFromAssets(context: Context, path: String): List<Float> {
    return try {
        val file = context.assets.open(path)
        val bufferedReader = BufferedReader(InputStreamReader(file))
        val phase: List<Float>
        bufferedReader.useLines { lines ->
            phase = lines.toList().map { it.toFloat() }
        }
        phase
    } catch (_: Exception) {
        emptyList()
    }
}

fun readMoonPhaseBinFromAssets(context: Context, path: String): List<Float> {
    return try {
        val inStream = context.assets.open(path)
        val fileBytes = ByteArray(size = inStream.available())
        inStream.read(fileBytes)
        val floatBuffer = ByteBuffer.wrap(fileBytes)
            .apply { order(ByteOrder.nativeOrder()) }
            .asFloatBuffer()

        floatBuffer.rewind()
        List(floatBuffer.remaining()) { floatBuffer.get() }
    } catch (_: Exception) {
        emptyList()
    }
}

fun readLandPolygonsFromAssets(context: Context, path: String): List<PolygonBVH> {
    return try {
        val file = context.assets.open(path)
        val bufferedReader = BufferedReader(InputStreamReader(file))
        val polygons = MutableList(0){ PolygonBVH(emptyList()) }
        bufferedReader.useLines { lines ->
            val linesList = lines.toList()
            val emptyIndices = listOf(-1) + linesList.indicesOf("*")
            emptyIndices.forEachIndexed { i, index ->
                if (i == emptyIndices.size - 1) return@forEachIndexed
                polygons.add(PolygonBVH(
                    linesList.subList(index + 1, emptyIndices[i+1]).map {
                        val coordinates = it.trim().split(',')
                        Point(coordinates[0].toFloat(), coordinates[1].toFloat())
                    }
                ))
            }
        }
        polygons
    } catch (_: Exception) {
        emptyList()
    }
}

fun readPolygonsBinFromAssets(context: Context, path: String = "land_polygons.bin"): List<Polygon> {
    return try {
        val inStream = context.assets.open(path)
        val fileBytes = ByteArray(size = inStream.available())
        inStream.read(fileBytes)
        val floatBuffer = ByteBuffer.wrap(fileBytes)
            .apply { order(ByteOrder.nativeOrder()) }
            .asFloatBuffer()

        floatBuffer.rewind()
        val dataList = List(floatBuffer.remaining()) { floatBuffer.get() }
        dataList.split(1000f).filter {
            it.isNotEmpty()
        }.map {
            val pointList = it.chunked(size = 2) { coordinates ->
                Point(coordinates[0], coordinates[1])
            }
            Polygon(pointList)
        }
    } catch (_: Exception) {
        emptyList()
    }
}