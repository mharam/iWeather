package com.takaapoo.weatherer.ui.screens.settings

import android.content.Context
import android.view.LayoutInflater
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.WeatherApplication
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.databinding.NotificationSmallBinding
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.moonPhase
import com.takaapoo.weatherer.utility.readMoonPhaseBinFromAssets
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


@RunWith(AndroidJUnit4::class)
class SmallLayoutTest {
    lateinit var context: Context
    val location = Location(name = "Tehran", utcOffset = 210)
    val daysList = listOf<Long>(-1, 0, 1 ,2, 3)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<WeatherApplication>()
        moonPhase = readMoonPhaseBinFromAssets(context, "moon_phase.bin")
    }

    @Test
    fun smallLayout_NormalData() {
        val dailyData = daysList.map {
            LocalDailyWeather(
                locationId = 1,
                time = LocalDate.now().plusDays(it).toString(),
                temperatureMax = 28f + it,
                temperatureMin = -it.toFloat(),
                sunRise = "${LocalDate.now().plusDays(it)}T07:12",
                sunSet = "${LocalDate.now().plusDays(it)}T17:06"
            )
        }
        val hourlyData = daysList.map { dayIndex->
            List(size = 24) { hourIndex ->
                LocalHourlyWeather(
                    locationId = 1,
                    time = LocalDateTime.now().plusDays(dayIndex)
                        .plusHours(hourIndex.toLong()).toString(),
                    temperature = hourIndex*1f,
                    temperatureControl1X = 1/3f,
                    temperatureControl1Y = 1/3f,
                    temperatureControl2X = 2/3f,
                    temperatureControl2Y = 2/3f
                )
            }
        }.flatten()

        val hourlyChartData = List(size = hourlyData.size) { index ->
            val dailyIndex = index / 24
            HourlyChartDto(
                hourlyWeather = hourlyData.getOrNull(index),
                airQuality = null,
                locationName = location.name,
                utcOffset = location.utcOffset,
                maxTemperature = dailyData.getOrNull(dailyIndex)?.temperatureMax,
                minTemperature = dailyData.getOrNull(dailyIndex)?.temperatureMin,
                sunRise = dailyData.getOrNull(dailyIndex)?.sunRise,
                sunSet = dailyData.getOrNull(dailyIndex)?.sunSet
            )
        }
        val appSettings = AppSettings().copy(
            notificationDateTime = HourlyDiagramTime.LOCATION
        )

        val zone: ZoneId = when (appSettings.notificationDateTime) {
            HourlyDiagramTime.UTC -> ZoneId.of("UTC")
            HourlyDiagramTime.DEVICE -> ZoneId.systemDefault()
            HourlyDiagramTime.LOCATION -> {
                ZoneOffset.ofTotalSeconds((hourlyChartData.getOrNull(0)?.utcOffset?.toInt() ?: 0) * 60)
            }
        }
        val now = LocalDateTime.now(zone)

        val notificationSmallBinding = NotificationSmallBinding.inflate(LayoutInflater.from(context)).apply {
            smallLayout(
                context = context,
                hourlyChartData = hourlyChartData,
                appSettings = appSettings,
            )
        }
        assertThat(notificationSmallBinding.locationName.text).isEqualTo(location.name)
//        assertThat(notificationSmallBinding.date.text).isEqualTo(LocalDate.now().toString())
        assertThat(notificationSmallBinding.clock.text).isEqualTo(
            now.format(DateTimeFormatter.ofPattern("HH\u202F:\u202Fmm")))
    }
}