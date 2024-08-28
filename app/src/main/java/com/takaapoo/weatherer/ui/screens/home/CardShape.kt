package com.takaapoo.weatherer.ui.screens.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection


class CardShape(
    private val cardOverHead: Float,
    private val topRound: Float,
    private val overHeadDistance: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val bottomRound = topRound

        return Outline.Generic(Path().apply {
            moveTo(0f, cardOverHead + topRound)
            arcTo(
                rect = Rect(
                    center = Offset(topRound, cardOverHead + topRound),
                    radius = topRound
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            relativeLineTo(dx = overHeadDistance, dy = 0f)
            arcTo(
                rect = Rect(
                    center = Offset(topRound + overHeadDistance + cardOverHead, cardOverHead),
                    radius = cardOverHead
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(x = size.width - topRound - overHeadDistance, y = 0f)
            arcTo(
                rect = Rect(
                    center = Offset(size.width - topRound - overHeadDistance - cardOverHead, cardOverHead),
                    radius = cardOverHead
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(x = size.width - topRound, y = cardOverHead)
            arcTo(
                rect = Rect(
                    center = Offset(size.width - topRound, cardOverHead + topRound),
                    radius = topRound
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(x = size.width, y = size.height - bottomRound)
            arcTo(
                rect = Rect(
                    center = Offset(size.width - bottomRound, size.height - bottomRound),
                    radius = bottomRound
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(x =bottomRound, y = size.height)
            arcTo(
                rect = Rect(
                    center = Offset(bottomRound, size.height - bottomRound),
                    radius = bottomRound
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        })
    }
}