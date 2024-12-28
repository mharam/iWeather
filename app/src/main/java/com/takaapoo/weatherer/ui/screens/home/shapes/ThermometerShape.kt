package com.takaapoo.weatherer.ui.screens.home.shapes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos


class ThermometerShape(
    private val bottomRadius: Float,
    private val topRadius: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width = size.width
        val height = size.height
        val bottomAngle = asin(topRadius / bottomRadius)


        return Outline.Generic(Path().apply {
            moveTo(x = size.width/2 - topRadius, y = height - bottomRadius * (1 + cos(bottomAngle)))
            arcToRad(
                rect = Rect(
                    center = Offset(width/2, height - bottomRadius),
                    radius = bottomRadius
                ),
                startAngleRadians = 3 * PI.toFloat() / 2 - bottomAngle,
                sweepAngleRadians = -2 * (PI.toFloat() - bottomAngle),
                forceMoveTo = false
            )
            lineTo(x = width/2 + topRadius, y = topRadius)
            arcTo(
                rect = Rect(
                    center = Offset(width/2, topRadius),
                    radius = topRadius
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            close()
        })
    }
}