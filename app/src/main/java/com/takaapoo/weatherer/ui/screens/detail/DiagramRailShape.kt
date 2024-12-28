package com.takaapoo.weatherer.ui.screens.detail

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.ui.utility.toPx
import kotlin.math.asin
import kotlin.math.cos

class DiagramRailShape: Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(diagramRailPath(size.width, size.height, density))
    }
}

fun diagramRailPath(width: Float, height: Float, density: Density): Path {
    val radius = height/2 + 8.dp.toPx(density)
    val angel = asin(height/(2*radius))
    val d = height/2 + radius * cos(angel)
    val path = Path().apply {
        moveTo(x = width, y = height)
        lineTo(x = d, y = height)
        arcToRad(
            rect = Rect(
                offset = Offset((-8).dp.toPx(density), height/2 - radius),
                size = Size(2*radius, 2*radius)
            ),
            startAngleRadians = angel,
            sweepAngleRadians = -2 * angel,
            forceMoveTo = false
        )
//        close()
        lineTo(x = width, y = 0f)
    }
    return path
}