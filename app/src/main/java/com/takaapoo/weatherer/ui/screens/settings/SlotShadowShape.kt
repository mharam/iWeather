package com.takaapoo.weatherer.ui.screens.settings

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection


class SlotShadowShape() : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width = size.width
        val height = size.height

        return Outline.Generic(Path().apply {
            moveTo(0f, 0f)
            cubicTo(
                x1 = width / 10, y1 = 0f,
                x2 = 2 * width / 6, y2 = height,
                x3 = width / 2, y3 = height
            )
            cubicTo(
                x1 = 4 * width / 6, y1 = height,
                x2 = 9 * width / 10, y2 = 0f,
                x3 = width, y3 = 0f
            )
            close()
        })
    }
}