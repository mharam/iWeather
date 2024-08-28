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
import com.takaapoo.weatherer.ui.screens.home.toPx
import com.takaapoo.weatherer.ui.screens.home.toSp
import kotlin.math.PI
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
//        moveTo(x = 0f, y = 0f)
//        lineTo(x = 16.dp.toPx(density), y = 0f)
//        arcToRad(
//            rect = Rect(
//                offset = Offset(0f, height/2 - radius),
//                size = Size(2*radius, 2*radius)
//            ),
//            startAngleRadians = angel - PI.toFloat(),
//            sweepAngleRadians = -2 * angel,
//            forceMoveTo = false
//        )
//        lineTo(x = 0f, y = height)

//        moveTo(x = width - radius*(1 - cos(angel)), y = 0f)
//        arcToRad(
//            rect = Rect(
//                offset = Offset(width - 2 * radius, height/2 - radius),
//                size = Size(2*radius, 2*radius)
//            ),
//            startAngleRadians = -angel,
//            sweepAngleRadians = 2 * angel,
//            forceMoveTo = false
//        )
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