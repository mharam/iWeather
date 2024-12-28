package com.takaapoo.weatherer.ui.screens.home.shapes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class DayGaugeShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width = size.width
        val height = size.height
        return Outline.Generic(Path().apply {
            addRect(
                rect = Rect(
                    topLeft = Offset(x = 0f, y = height/4),
                    bottomRight = Offset(x = width, y = 3*height/4)
                )
            )
        })
    }
}