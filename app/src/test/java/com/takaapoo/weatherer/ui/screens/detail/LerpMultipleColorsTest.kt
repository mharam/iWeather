package com.takaapoo.weatherer.ui.screens.detail

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.utility.lerpMultipleColors
import org.junit.Test

class LerpMultipleColorsTest {
    @Test
    fun `lerpMultipleColors when fraction equals 03`() {
        val colors = listOf(Color.Blue, Color.Green, Color.Yellow)
        val fraction = 0.3f
        val realColor = lerpMultipleColors(colors, fraction)
        val expectedColor = lerp(Color.Blue, Color.Green, 0.6f)
        assertThat(realColor).isEqualTo(expectedColor)
    }
    @Test
    fun `lerpMultipleColors when fraction equals 09`() {
        val colors = listOf(Color.Blue, Color.Green, Color.Yellow)
        val fraction = 0.9f
        val realColor = lerpMultipleColors(colors, fraction)
        val expectedColor = lerp(Color.Green, Color.Yellow, 0.8f)
        assertThat(realColor).isEqualTo(expectedColor)
    }
    @Test
    fun `lerpMultipleColors when fraction equals 09 with 4 colors`() {
        val colors = listOf(Color.Blue, Color.Green, Color.Yellow, Color.Red)
        val fraction = 0.9f
        val realColor = lerpMultipleColors(colors, fraction)
        val expectedColor = lerp(Color.Yellow, Color.Red, (0.9f - 2/3f)/(1/3f))
        assertThat(realColor).isEqualTo(expectedColor)
    }
}