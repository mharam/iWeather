package com.takaapoo.weatherer.utility

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PolygonBVHTest {
    val myPolygon = Polygon(
        listOf(Point(0f, 0f), Point(10f, 0f), Point(1f, 1f),
            Point(0f, 10f))
    )
    val myPolygonBVH = PolygonBVH(
        listOf(Point(0f, 0f), Point(10f, 0f), Point(1f, 1f),
            Point(0f, 10f))
    )
    val bBox = myPolygon.calculateBoundingBox()

    @Test
    fun `isPointInBoundingBox point outSide`() {
        val testPoint = Point(11f, 5f)
        assertThat(myPolygon.isPointInBoundingBox(testPoint, bBox)).isFalse()
    }
    @Test
    fun `isPointInBoundingBox point inSide`() {
        val testPoint = Point(5f, 5f)
        assertThat(myPolygon.isPointInBoundingBox(testPoint, bBox)).isTrue()
    }
    @Test
    fun `isPointInBoundingBox point on edge`() {
        val testPoint = Point(0f, 5f)
        assertThat(myPolygon.isPointInBoundingBox(testPoint, bBox)).isTrue()
    }

    @Test
    fun `isPointInside point inside`() {
        val testPoint = Point(0.5f, 0.5f)
        assertThat(myPolygon.isPointInside(testPoint)).isTrue()
    }
    @Test
    fun `isPointInside point outside`() {
        val testPoint = Point(5f, 5f)
        assertThat(myPolygon.isPointInside(testPoint)).isFalse()
    }
    @Test
    fun `isPointInside point on edge`() {
        val testPoint = Point(5f, 0f)
        assertThat(myPolygon.isPointInside(testPoint)).isTrue()
    }

    @Test
    fun `isPointOnEdge point inside`() {
        val testPoint = Point(0.5f, 0.5f)
        assertThat(myPolygon.isPointOnEdge(testPoint)).isFalse()
    }
    @Test
    fun `isPointOnEdge point outside`() {
        val testPoint = Point(5f, 5f)
        assertThat(myPolygon.isPointOnEdge(testPoint)).isFalse()
    }
    @Test
    fun `isPointOnEdge point on edge`() {
        val testPoint = Point(5f, 0f)
        assertThat(myPolygon.isPointOnEdge(testPoint)).isTrue()
    }
    @Test
    fun `isPointOnEdge point on edge outside`() {
        val testPoint = Point(15f, 0f)
        assertThat(myPolygon.isPointOnEdge(testPoint)).isFalse()
    }


    @Test
    fun `isPointInside BVH point inside`() {
        val testPoint = Point(0.5f, 0.5f)
        assertThat(myPolygonBVH.isPointInside(testPoint)).isTrue()
    }
    @Test
    fun `isPointInside BVH point outside`() {
        val testPoint = Point(5f, 5f)
        assertThat(myPolygonBVH.isPointInside(testPoint)).isFalse()
    }
    @Test
    fun `isPointInside BVH point on edge`() {
        val testPoint = Point(5f, 0f)
        assertThat(myPolygonBVH.isPointInside(testPoint)).isTrue()
    }

}