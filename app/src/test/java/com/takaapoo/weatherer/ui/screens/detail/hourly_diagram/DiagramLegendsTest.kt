package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DiagramLegendsTest {
    @Test
    fun `contain normal list`() {
        val testList = listOf(1f to 3f, -5f to -2f)
        val realValue = testList.contain(2f)
        assertThat(realValue).isFalse()
    }

    @Test
    fun `contain list with null value`() {
        val testList = listOf(1f to 3f, -5f to null)
        val realValue = testList.contain(-2f)
        assertThat(realValue).isTrue()
    }

    @Test
    fun `customToString 1`() {
        val separation = 5f
        val realValue = 15.54f.customToString(separation)
        assertThat(realValue).isEqualTo("16")
    }

    @Test
    fun `customToString 2`() {
        val separation = 0.5f
        val realValue = 15.54f.customToString(separation)
        assertThat(realValue).isEqualTo("15.5")
    }

    @Test
    fun `customToString 3`() {
        val separation = 20f
        val realValue = 15.54f.customToString(separation)
        assertThat(realValue).isEqualTo("16")
    }

    @Test
    fun `customToString 4`() {
        val separation = 2000f
        val realValue = 15.54f.customToString(separation)
        assertThat(realValue).isEqualTo("16")
    }

    @Test
    fun `customToString 5`() {
        val separation = 2f
        val realValue = 5.54f.customToString(separation)
        assertThat(realValue).isEqualTo("6")
    }
}