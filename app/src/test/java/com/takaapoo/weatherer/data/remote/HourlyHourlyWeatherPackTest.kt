package com.takaapoo.weatherer.data.remote

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.*

import org.junit.Test
import com.google.common.truth.Truth.assertThat


class HourlyHourlyWeatherPackTest {

    @Test
    fun generateControlPointsForNullableDataTest() {
        val data = listOf(null, 1f, 1.3f, 2.5f, 0.5f)
        val (controlPoint1, controlPoint2) =
            generateControlPoints(data.dropWhile { it == null } as List<Float>)
        val expectedControlPoints1 = mutableListOf<Offset?>(null).apply {
            addAll(controlPoint1)
        }
        val expectedControlPoints2 = mutableListOf<Offset?>(null).apply {
            addAll(controlPoint2)
        }
        val result = generateControlPointsForNullableData(data)
        assertThat(result).isEqualTo(expectedControlPoints1 to expectedControlPoints2)
    }

    @Test
    fun generateControlPointsForNullableDataTest2() {
        val data = listOf(null, 1f, 1.3f, 2.5f, null, 0.5f, 3f, null)
        val (controlPoint1, controlPoint2) = generateControlPoints(listOf(1f, 1.3f, 2.5f))
        val (controlPoint1_2, controlPoint2_2) = generateControlPoints(listOf(0.5f, 3f))
        val expectedControlPoints1 = mutableListOf<Offset?>(null).apply {
            addAll(controlPoint1)
            addAll(List(2){ null })
            addAll(controlPoint1_2)
            add(null)
        }
        val expectedControlPoints2 = mutableListOf<Offset?>(null).apply {
            addAll(controlPoint2)
            addAll(List(2){ null })
            addAll(controlPoint2_2)
            add(null)
        }
        val result = generateControlPointsForNullableData(data)
        assertThat(result).isEqualTo(expectedControlPoints1 to expectedControlPoints2)
    }
}