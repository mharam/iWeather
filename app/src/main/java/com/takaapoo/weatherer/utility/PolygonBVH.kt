package com.takaapoo.weatherer.utility

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

data class Point(val x: Float, val y: Float)
fun Offset.toPoint() = Point(x, y)

data class BoundingBox(val min: Point, val max: Point)

open class Polygon(val vertices: List<Point>){

    fun calculateBoundingBox(polygonVertices: List<Point> = vertices): BoundingBox {
        if (vertices.isEmpty()) throw IllegalArgumentException("Points list cannot be empty")

        val sortedX = polygonVertices.sortedBy { it.x }
        val sortedY = polygonVertices.sortedBy { it.y }

        val minX = sortedX.first().x
        val minY = sortedY.first().y
        val maxX = sortedX.last().x
        val maxY = sortedY.last().y

        /*for (point in vertices) {
            minX = min(minX, point.x)
            minY = min(minY, point.y)
            maxX = max(maxX, point.x)
            maxY = max(maxY, point.y)
        }*/

        return BoundingBox(Point(minX, minY), Point(maxX, maxY))
    }

    // Check if point is within a bounding box
    fun isPointInBoundingBox(point: Point, bBox: BoundingBox): Boolean {
        return point.x >= bBox.min.x && point.x <= bBox.max.x &&
                point.y >= bBox.min.y && point.y <= bBox.max.y
    }

    // Winding number algorithm for final point-in-polygon test
    fun windingNumber(point: Point): Int {
        var winding = 0
        val closedPolygon = vertices + vertices.first()

        for (i in 0 until vertices.size) {
            val p1 = closedPolygon[i]
            val p2 = closedPolygon[i + 1]

            if (p1.y <= point.y) {
                if (p2.y > point.y && isLeft(p1, p2, point) > 0) {
                    winding++
                }
            } else {
                if (p2.y <= point.y && isLeft(p1, p2, point) < 0) {
                    winding--
                }
            }
        }
        return winding
    }

    // Helper function to determine if point is to the left of the edge
    private fun isLeft(p1: Point, p2: Point, point: Point): Float {
        return (p2.x - p1.x) * (point.y - p1.y) - (point.x - p1.x) * (p2.y - p1.y)
    }

    open fun isPointInside(point: Point): Boolean {
        val bBox = calculateBoundingBox()
        if (!isPointInBoundingBox(point, bBox))
            return false
        return windingNumber(point) != 0
    }

    fun isPointOnEdge(point: Point): Boolean {
        val closedPolygon = vertices + vertices.first()

        for (i in 0 until vertices.size) {
            val p1 = closedPolygon[i]
            val p2 = closedPolygon[i + 1]

            if (abs(isLeft(p1, p2, point)) < 0.001f) {
                if ((p1.y <= point.y && p2.y > point.y) || (p1.y > point.y && p2.y <= point.y) ||
                    (p1.x <= point.x && p2.x > point.x) || (p1.x > point.x && p2.x <= point.x)) {
                    return true
                }
            }
        }
        return false
    }

}

class PolygonBVH(vertices: List<Point>): Polygon(vertices) {
    private val root: BVHNode

    init {
        root = buildBVH(vertices)
    }

    // BVH Node class
    private sealed class BVHNode {
        data class Leaf(val polygon: Polygon, val bBox: BoundingBox) : BVHNode()
        data class Branch(val left: BVHNode, val right: BVHNode, val bBox: BoundingBox) : BVHNode()
    }

    // Main public method to check if point is inside polygon
    override fun isPointInside(point: Point): Boolean {
        return pointInPolygonBVH(point, root)
    }

    // Build the BVH tree recursively
    private fun buildBVH(vertices: List<Point>, maxLeafSize: Int = 50): BVHNode {
        val bBox = calculateBoundingBox()

        // If polygon is small enough, create leaf node
        if (vertices.size <= maxLeafSize) {
            return BVHNode.Leaf(this, bBox)
        }

        // Split along the longest axis
        val splitAxis = if (bBox.max.x - bBox.min.x > bBox.max.y - bBox.min.y) 'x' else 'y'
        val sortedVertices = when (splitAxis) {
            'x' -> vertices.sortedBy { it.x }
            else -> vertices.sortedBy { it.y }
        }

        val midIndex = sortedVertices.size / 2
        val leftVertices = sortedVertices.subList(0, midIndex)
        val rightVertices = sortedVertices.subList(midIndex, sortedVertices.size)

        // Ensure we have valid splits
        if (leftVertices.isEmpty() || rightVertices.isEmpty()) {
            return BVHNode.Leaf(this, bBox)
        }

        val leftNode = buildBVH(leftVertices, maxLeafSize)
        val rightNode = buildBVH(rightVertices, maxLeafSize)

        return BVHNode.Branch(leftNode, rightNode, bBox)
    }

    // Recursive point-in-polygon check using BVH
    private fun pointInPolygonBVH(point: Point, node: BVHNode): Boolean {
        // First check bounding box - quick elimination
        if (!isPointInBoundingBox(point, when (node) {
                is BVHNode.Leaf -> node.bBox
                is BVHNode.Branch -> node.bBox
            })) {
            return false
        }

        return when (node) {
            is BVHNode.Leaf -> node.polygon.windingNumber(point) != 0
            is BVHNode.Branch -> {
                pointInPolygonBVH(point, node.left) || pointInPolygonBVH(point, node.right)
            }
        }
    }
}