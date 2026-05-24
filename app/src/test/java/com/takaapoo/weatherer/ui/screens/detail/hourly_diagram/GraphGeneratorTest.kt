package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import com.google.common.truth.Truth
import org.junit.Test

class GraphGeneratorTest {

    @Test
    fun nonNullDataSegments_OnListWithNullOnBothEnds() {
        val testList = listOf(null, 1.5f, 2f, null, null)
        val expectedResult = mapOf(1 to listOf(1.5f, 2f))
        val realResult = testList.nonNullDataSegments()
        Truth.assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun nonNullDataSegments_OnListWithNullOnBothEndsAndBetweenElements() {
        val testList = listOf(null, 1.5f, 2f, null, 3f, 0f, 0.3f, null, null, 5f, null)
        val expectedResult =
            mapOf(1 to listOf(1.5f, 2f), 4 to listOf(3f, 0f, 0.3f), 9 to listOf(5f))
        val realResult = testList.nonNullDataSegments()
        Truth.assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun nonNullDataSegments_OnListWithNullOnStartAndBetweenElements() {
        val testList = listOf(null, 1f, 1.3f, 2.5f, null, 0.5f, 3f)
        val expectedResult =
            mapOf(1 to listOf(1f, 1.3f, 2.5f), 5 to listOf(0.5f, 3f))
        val realResult = testList.nonNullDataSegments()
        Truth.assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun nonNullDataSegments_OnListWithOutNull() {
        val testList = listOf(1.5f, 2f)
        val expectedResult = mapOf(0 to listOf(1.5f, 2f))
        val realResult = testList.nonNullDataSegments()
        Truth.assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun nonNullDataSegments_OnListWithJustNull() {
        val testList = listOf(null, null)
        val expectedResult = emptyMap<Int, List<Float>>()
        val realResult = testList.nonNullDataSegments()
        Truth.assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun nonNullDataSegments_OnEmptyList() {
        val testList = emptyList<Float?>()
        val expectedResult = emptyMap<Int, List<Float>>()
        val realResult = testList.nonNullDataSegments()
        Truth.assertThat(realResult).isEqualTo(expectedResult)
    }
}