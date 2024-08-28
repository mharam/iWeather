package com.takaapoo.weatherer.ui.screens.detail

import org.junit.Test
import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.nonNullDataSegments


class CubicGraphGeneratorTest {

    @Test
    fun nonNullDataSegmentsOnListWithNullOnBothEnds() {
        val testList = listOf(null, 1.5f, 2f, null, null)
        val expectedResult = mapOf(1 to listOf(1.5f, 2f))
        val realResult = testList.nonNullDataSegments()
        assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun nonNullDataSegmentsOnListWithNullOnBothEndsAndBetweenElements() {
        val testList = listOf(null, 1.5f, 2f, null, 3f, 0f, 0.3f, null, null, 5f, null)
        val expectedResult =
            mapOf(1 to listOf(1.5f, 2f), 4 to listOf(3f, 0f, 0.3f), 9 to listOf(5f))
        val realResult = testList.nonNullDataSegments()
        assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun nonNullDataSegmentsOnListWithNullOnStartAndBetweenElements() {
        val testList = listOf(null, 1f, 1.3f, 2.5f, null, 0.5f, 3f)
        val expectedResult =
            mapOf(1 to listOf(1f, 1.3f, 2.5f), 5 to listOf(0.5f, 3f))
        val realResult = testList.nonNullDataSegments()
        assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun nonNullDataSegmentsOnListWithOutNull() {
        val testList = listOf(1.5f, 2f)
        val expectedResult = mapOf(0 to listOf(1.5f, 2f))
        val realResult = testList.nonNullDataSegments()
        assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun nonNullDataSegmentsOnListWithJustNull() {
        val testList = listOf(null, null)
        val expectedResult = emptyMap<Int, List<Float>>()
        val realResult = testList.nonNullDataSegments()
        assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun nonNullDataSegmentsOnEmptyList() {
        val testList = emptyList<Float?>()
        val expectedResult = emptyMap<Int, List<Float>>()
        val realResult = testList.nonNullDataSegments()
        assertThat(realResult).isEqualTo(expectedResult)
    }
}