package com.takaapoo.weatherer.ui.screens.settings

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.utility.plus
import com.takaapoo.weatherer.utility.times
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class PickColorTest {

    val innerRadiusPx = 100f
    val radius = innerRadiusPx - 20f
    val triangleHeight = 1.5f * radius
    val triangleCenter = Offset(innerRadiusPx, innerRadiusPx)
    val point1 = triangleCenter + Offset(radius, 0f)
    val point2 = triangleCenter + Offset(radius * cos(2*pi/3), radius * sin(2*pi/3))
    val point3 = triangleCenter + Offset(radius * cos(4*pi/3), radius * sin(4*pi/3))

    @Test
    fun trianglePointerColor_OnCenterPoint() {
        val trianglePointerPosition = triangleCenter
        val realResult = trianglePointerPosition.trianglePointerColor(
            point1,
            point2,
            point3,
            Color.Red,
            triangleHeight
        )
        val expectedResult = Color(red = 2/3f, green = 1/3f, blue = 1/3f)
        assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun trianglePointerColor_OnMiddlePointOf_1_2() {
        val trianglePointerPosition = (point1 + point2) / 2f
        val realResult = trianglePointerPosition.trianglePointerColor(
            point1,
            point2,
            point3,
            Color.Green,
            triangleHeight
        )
        val expectedResult = Color(red = 1/2f, green = 1f, blue = 1/2f)
        assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun trianglePointerColor_OnVertex() {
        val trianglePointerPosition = point1
        val realResult = trianglePointerPosition.trianglePointerColor(
            point1,
            point2,
            point3,
            Color.Green,
            triangleHeight
        )
        val expectedResult = Color.Green
        assertThat(realResult).isEqualTo(expectedResult)
    }

    /*@Test
    fun trianglePointerColor_OnOutOf_Triangle() {
        val trianglePointerPosition = point1 + Offset(1f, 0f)
        val realResult = trianglePointerPosition.trianglePointerColor(
            point1,
            point2,
            point3,
            Color.Green,
            triangleHeight
        )
        val expectedResult = Color.White
        assertThat(realResult).isEqualTo(expectedResult)
    }*/

    @Test
    fun distanceToLineSegment_OnLineSegmentVertex() {
        val pointA = Offset(1f, 1f)
        val pointB = Offset(3f, -5f)
        val realResult = pointA.distanceToLineSegment(pointA, pointB)
        val expectedResult = 0f
        assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun distanceToLineSegment_OutOfLineSegment() {
        val pointA = Offset(1f, 1f)
        val pointB = Offset(1f, 5f)
        val pointC = Offset(3f, 20f)
        val realResult = pointC.distanceToLineSegment(pointA, pointB)
        val expectedResult = 2f
        assertThat(realResult).isWithin(0.001f).of(expectedResult)
    }

    @Test
    fun distanceToLineSegment_OnLineSegment() {
        val pointA = Offset(1f, 1f)
        val pointB = Offset(1f, 5f)
        val pointC = Offset(1f, 20f)
        val realResult = pointC.distanceToLineSegment(pointA, pointB)
        val expectedResult = 0f
        assertThat(realResult).isWithin(0.0001f).of(expectedResult)
    }

    @Test
    fun nearestPointOnLineSegment_ForPointOnLine() {
        val point = Offset(2f, 4f)
        val pointA = Offset(2f, 0f)
        val pointB = Offset(2f, 1f)
        val realResult = nearestPointOnLineSegment(pointA, pointB, point)
        val expectedResult = Offset(2f, 1f)
        assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun nearestPointOnLineSegment_ForPointOutOfLine() {
        val point = Offset(3f, 0.6f)
        val pointA = Offset(2f, 0f)
        val pointB = Offset(2f, 1f)
        val realResult = nearestPointOnLineSegment(pointA, pointB, point)
        val expectedResult = Offset(2f, 0.6f)
        assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun nearestPointOnLineSegment_ForZeroLengthLine() {
        val point = Offset(3f, 1f)
        val pointA = Offset(2f, 0f)
        val pointB = Offset(2f, 0f)
        val realResult = nearestPointOnLineSegment(pointA, pointB, point)
        val expectedResult = Offset(2f, 0f)
        assertThat(realResult).isEqualTo(expectedResult)
    }

    @Test
    fun linearInterpolateColors_On3Colors_FractionZero(){
        val colorList = listOf(Color.White, Color.Yellow, Color.Green)
        val fraction = 0f
        val realColor = linearInterpolateColors(colorList, fraction)
        val expectedColor = Color.White
        assertThat(realColor).isEqualTo(expectedColor)
    }

    @Test
    fun linearInterpolateColors_On3Colors_FractionOne(){
        val colorList = listOf(Color.White, Color.Yellow, Color.Green)
        val fraction = 1f
        val realColor = linearInterpolateColors(colorList, fraction)
        val expectedColor = Color.Green
        assertThat(realColor).isEqualTo(expectedColor)
    }

    @Test
    fun linearInterpolateColors_On3Colors_FractionHalf(){
        val colorList = listOf(Color.White, Color.Yellow, Color.Green)
        val fraction = 0.5f
        val realColor = linearInterpolateColors(colorList, fraction)
        val expectedColor = Color.Yellow
        assertThat(realColor).isEqualTo(expectedColor)
    }

    @Test
    fun linearInterpolateColors_On3Colors_Fraction_0_7(){
        val colorList = listOf(Color.White, Color.Yellow, Color.Green)
        val fraction = 0.7f
        val realColor = linearInterpolateColors(colorList, fraction)
        val expectedColor = Color.Yellow * 0.6f + Color.Green * 0.4f
        assertThat(realColor).isEqualTo(expectedColor)
    }

}