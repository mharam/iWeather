package com.takaapoo.weatherer.ui.screens.detail.daily_diagram

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class DailyChartTest {

    @Test
    fun `dailyTimeToX return correct x for different dates`() {
        val x1 = dailyTimeToX(LocalDate.now().plusDays(3).toString())
        val x2 = dailyTimeToX(LocalDate.now().toString())
        val x3 = dailyTimeToX(LocalDate.now().minusDays(3).toString())
        val x4 = dailyTimeToX(null)

        assertThat(x1).isEqualTo(3)
        assertThat(x2).isEqualTo(0)
        assertThat(x3).isEqualTo(-3)
        assertThat(x4).isEqualTo(null)
    }

}