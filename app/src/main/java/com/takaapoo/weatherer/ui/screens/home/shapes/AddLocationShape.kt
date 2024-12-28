package com.takaapoo.weatherer.ui.screens.home.shapes

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class AddLocationShape: Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val svgPathData = "M20,10c0,4.6-5.2,9.8-7.5,11.4c-0.3,0.2-0.8,0.2-1.1,0C9.1,19.5,4,14.5,4,10c0-4.4,3.6-8,8-8S20,5.6,20,10z" // Example path data
        val scaleX = size.width / 24
        val scaleY = size.height / 24
        val scaleMatrix = Matrix().apply {
            reset()
            scale(x = scaleX, y = scaleY)
        }

        val addLocationPath = PathParser().parsePathString(svgPathData).toPath().apply {
            transform(
                matrix = scaleMatrix
            )
        }
        return Outline.Generic(addLocationPath)
    }
}