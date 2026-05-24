package com.takaapoo.weatherer.utility

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.WeatherApplication
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.data.remote.DailyWeather
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

@RunWith(AndroidJUnit4::class)
class UsefulFuncTest {
    lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<WeatherApplication>()
    }

    @Test
    fun `swap 2 elements in a mutableList`() {
        val myMutableList = mutableListOf(1,2,3,4,5)
        myMutableList.swap(0, 4)
        val expectedResult = mutableListOf(5,2,3,4,1)
        assertThat(myMutableList).isEqualTo(expectedResult)
    }

    @Test
    fun `containsAny contains an element`() {
        val containerList = listOf(1,2,3,4,5)
        val checkList = listOf(3,6,9)
        assertThat(containerList.containsAny(checkList)).isTrue()
    }

    @Test
    fun `containsAny does not contain any elements`() {
        val containerList = listOf(1,2,3,4,5)
        val checkList = listOf(7,6,9)
        assertThat(containerList.containsAny(checkList)).isFalse()
    }

    @Test
    fun `removeTransparentBorder for bitmap with no transparent border`() {
        val testBitmap = context.resources.getDrawable(R.drawable.day_gauge_light, context.theme).toBitmap()
        val realResult = testBitmap.removeTransparentBorder(n = 0, minHeight = 0)
        assertThat(realResult).isEqualTo(testBitmap)
    }

    @Test
    fun `removeTransparentBorder for bitmap with transparent border`() {
        val testBitmap = context.resources.getDrawable(R.drawable.moon6, context.theme).toBitmap(256, 256)
        val realResult = testBitmap.removeTransparentBorder(n = 0, minHeight = 0)
        val expectedResult =
            Bitmap.createBitmap(testBitmap, 38, 22, 220 - 38, 207 - 22)
        assertThat(realResult).isEqualTo(expectedResult)
    }


    @Test
    fun `subList center width`() {
        val testList = listOf(1,2,3,4,5,6,7,8,9,10)
        val realResult = testList.subList(centerIndex = 3, width = 2)
        val expectedResult = listOf(3,4)
        assertThat(realResult).isEqualTo(expectedResult)
    }
    @Test
    fun `subList segment partially out of bounds`() {
        val testList = listOf(1,2,3,4,5,6,7,8,9,10)
        val realResult = testList.subList(centerIndex = 2, width = 7)
        val expectedResult = listOf(1,2,3,4,5,6)
        assertThat(realResult).isEqualTo(expectedResult)
    }
    @Test
    fun `subList zero width`() {
        val testList = listOf(1,2,3,4,5,6,7,8,9,10)
        val realResult = testList.subList(centerIndex = 2, width = 0)
        val expectedResult = emptyList<Int>()
        assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun `split if list has the element`() {
        val testList = listOf(1,2,3,4,3,6,7,8,9,3)
        val realResult = testList.split(3)
        val expectedResult = listOf(listOf(1,2), listOf(4), listOf(6,7,8,9))
        assertThat(realResult).isEqualTo(expectedResult)
    }
    @Test
    fun `split if list does not have the element`() {
        val testList = listOf(1,2,3,4,3,6,7,8,9,3)
        val realResult = testList.split(0)
        val expectedResult = listOf(listOf(1,2,3,4,3,6,7,8,9,3))
        assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun `removeAfterFirst for list with 0 occurrence of element`() {
        val testList = mutableListOf(1,2,3,4,5,6)
        testList.removeAfterFirst(8)
        val expectedResult = mutableListOf(1,2,3,4,5,6)
        assertThat(testList).isEqualTo(expectedResult)
    }
    @Test
    fun `removeAfterFirst for list with 1 occurrence of element`() {
        val testList = mutableListOf(1,2,3,4,5,6)
        testList.removeAfterFirst(4)
        val expectedResult = mutableListOf(1,2,3,4)
        assertThat(testList).isEqualTo(expectedResult)
    }
    @Test
    fun `removeAfterFirst for list with 2 occurrence of element`() {
        val testList = mutableListOf(1,2,3,4,3,6)
        testList.removeAfterFirst(3)
        val expectedResult = mutableListOf(1,2,3)
        assertThat(testList).isEqualTo(expectedResult)
    }

    @Test
    fun `oldest of LocalDateTime`() {
        val time1 = LocalDateTime.of(2010, 1, 1, 0, 0)
        val time2 = LocalDateTime.now()
        val time3 = time1.minusSeconds(1)
        val realResult = listOf(time1, time2, time3).oldest()
        assertThat(realResult).isEqualTo(time3)
    }


    private fun LatLng.toCartesian(r: Double): Triple<Double, Double, Double> {
        val x = r * cos(latitude * PI / 180) * sin(longitude * PI / 180)
        val y = -r * cos(latitude * PI / 180) * cos(longitude * PI / 180)
        val z = r * sin(latitude * PI / 180)
        return Triple(x, y, z)
    }
    private fun dotProduct(vec1: Triple<Double, Double, Double>, vec2: Triple<Double, Double, Double>): Double {
        return vec1.first * vec2.first + vec1.second * vec2.second + vec1.third * vec2.third
    }
    @Test
    fun `calculateLocationSurroundingPoints for point on equator`() {
        val testPoint = LatLng(0.0, 0.0)
        val surroundingPoints = calculateLocationSurroundingPoints(testPoint, 20)
        val testPointCartesian = testPoint.toCartesian(1.0)
        surroundingPoints.forEach { surroundingPoint ->
            val surroundingPointCartesian = surroundingPoint.toCartesian(1.0)
            val cosPhi = dotProduct(testPointCartesian, surroundingPointCartesian)
            assertThat(earthRadius * acos(cosPhi)).isWithin(0.01).of(20.0)
        }
    }
    @Test
    fun `calculateLocationSurroundingPoints for point on equator 2`() {
        val testPoint = LatLng(0.0, 180.0)
        val surroundingPoints = calculateLocationSurroundingPoints(testPoint, 20)
        val testPointCartesian = testPoint.toCartesian(1.0)
        surroundingPoints.forEach { surroundingPoint ->
            val surroundingPointCartesian = surroundingPoint.toCartesian(1.0)
            val cosPhi = dotProduct(testPointCartesian, surroundingPointCartesian)
            assertThat(earthRadius * acos(cosPhi)).isWithin(0.01).of(20.0)
        }
    }
    @Test
    fun `calculateLocationSurroundingPoints for random point`() {
        val testPoint = LatLng(-89.0, 90.0)
        val surroundingPoints = calculateLocationSurroundingPoints(testPoint, 20)
        val testPointCartesian = testPoint.toCartesian(1.0)
        surroundingPoints.forEach { surroundingPoint ->
            val surroundingPointCartesian = surroundingPoint.toCartesian(1.0)
            val cosPhi = dotProduct(testPointCartesian, surroundingPointCartesian)
            assertThat(earthRadius * acos(cosPhi)).isWithin(0.05).of(20.0)
        }
    }
    @Test
    fun `calculateLocationSurroundingPoints for point on north pole`() {
        val testPoint = LatLng(90.0, 90.0)
        val surroundingPoints = calculateLocationSurroundingPoints(testPoint, 20)
        val testPointCartesian = testPoint.toCartesian(1.0)
        surroundingPoints.forEach { surroundingPoint ->
            val surroundingPointCartesian = surroundingPoint.toCartesian(1.0)
            val cosPhi = dotProduct(testPointCartesian, surroundingPointCartesian)
            assertThat(earthRadius * acos(cosPhi)).isWithin(0.1).of(20.0)
        }
    }

    @Test
    fun `getPropertyBySerialName for different dailyWeather`(){
        val testData = DailyWeather(
            weatherCode = listOf(2, 3, 6, 1),
            temperatureMax = listOf(-3f, 2f, 1.5f, -10.1f)
        )
        val realResultWeatherCode = getPropertyBySerialName<List<Float?>>(
            testData,
            "weathercode"
        )
        val realResultTemperatureMax = getPropertyBySerialName<List<Float?>>(
            testData,
            "temperature_2m_max"
        )
        val realResultNull = getPropertyBySerialName<List<Float?>>(
            testData,
            "temperature"
        )
        assertThat(realResultWeatherCode).isEqualTo(listOf(2, 3, 6, 1))
        assertThat(realResultTemperatureMax).isEqualTo(listOf(-3f, 2f, 1.5f, -10.1f))
        assertThat(realResultNull).isNull()
    }

    @Test
    fun `landScapeName test1`(){
        val todayWeather = LocalDailyWeather(
            locationId = 1,
            time = "",
            sunRise = LocalDateTime.now(ZoneId.of("UTC")).minusHours(6).toString(),
            sunSet = LocalDateTime.now(ZoneId.of("UTC")).plusHours(2).toString(),
        )
        val currentWeather = LocalHourlyWeather(
            locationId = 1,
            time = "",
            isDay = 0,
            weatherCode = 3,
            snowDepth = 0.05f
        )
        val realResult = landScapeName(todayWeather, currentWeather, 180,
            LocalDateTime.now(ZoneId.of("UTC")))
        val expectedResult = "3_1_3"
        assertThat(realResult).isEqualTo(expectedResult)
    }

}