package com.takaapoo.weatherer.ui.screens.detail

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.screens.home.toPx

@Composable
fun MultiConnectorLeft (
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    hasLeftPoint: Boolean = true,
    hasRightPoint: Boolean = true,
    leftPoints: List<Offset>,
    rightPoint: Offset
) {
    val pointRadius = 4.dp
    val cornerRadius = dimensionResource(id = R.dimen.connector_corner_radius)
    val connectorWidth = dimensionResource(id = R.dimen.connector_line_width)
    val turnPointX = ((leftPoints.maxOfOrNull { it.x } ?: 0f) + rightPoint.x) / 2
    Canvas(modifier = modifier) {
        leftPoints.forEach { point ->
            if (hasLeftPoint) {
                drawCircle(
                    color = color,
                    radius = pointRadius.toPx(),
                    center = point
                )
            }
            val path = Path().apply {
                moveTo(x = point.x, y = point.y)
                lineTo(x = turnPointX, y = point.y)
                lineTo(x = turnPointX, y = rightPoint.y)
                lineTo(x = rightPoint.x, y = rightPoint.y)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = connectorWidth.toPx(),
                    pathEffect = PathEffect.cornerPathEffect(cornerRadius.toPx()),
                    cap = StrokeCap.Round
                )
            )
        }
        if (hasRightPoint) {
            drawCircle(
                color = color,
                radius = pointRadius.toPx(),
                center = rightPoint
            )
        }
    }
}

@Composable
fun SingleConnector2 (
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    hasLeftPoint: Boolean = true,
    hasRightPoint: Boolean = true,
    leftPoint: Offset,
    rightPoint: Offset,
    turnPoint: Offset = Offset.Zero
) {
    val pointRadius = 4.dp
    val cornerRadius = dimensionResource(id = R.dimen.connector_corner_radius)
    val connectorWidth = dimensionResource(id = R.dimen.connector_line_width)

    Canvas(modifier = modifier) {
        if (hasLeftPoint) {
            drawCircle(
                color = color,
                radius = pointRadius.toPx(),
                center = leftPoint
            )
        }
        val path = if (rightPoint.x > leftPoint.x + cornerRadius.toPx())
            Path().apply {
                moveTo(x = leftPoint.x, y = leftPoint.y)
                lineTo(x = leftPoint.x, y = rightPoint.y)
                lineTo(x = rightPoint.x, y = rightPoint.y)
            } else
            Path().apply {
                moveTo(x = leftPoint.x, y = leftPoint.y)
                lineTo(x = leftPoint.x, y = turnPoint.y)
                lineTo(x = rightPoint.x - 2 * cornerRadius.toPx(), y = turnPoint.y)
                lineTo(x = rightPoint.x - 2 * cornerRadius.toPx(), y = rightPoint.y)
                lineTo(x = rightPoint.x, y = rightPoint.y)
            }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = connectorWidth.toPx(),
                pathEffect = PathEffect.cornerPathEffect(cornerRadius.toPx()),
                cap = StrokeCap.Round
            )
        )
        if (hasRightPoint) {
            drawCircle(
                color = color,
                radius = pointRadius.toPx(),
                center = rightPoint
            )
        }
    }
}


@Composable
fun SingleConnector (
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    hasLeftPoint: Boolean = true,
    hasRightPoint: Boolean = true,
    leftPoint: Offset? = null,
) {
    val density = LocalDensity.current
    val pointRadius = 4.dp.toPx(density)
    val cornerRadius = dimensionResource(id = R.dimen.connector_corner_radius)
    val connectorWidth = dimensionResource(id = R.dimen.connector_line_width)

    Canvas(
        modifier = modifier
    ) {
        val leftPointNonNull = leftPoint ?: Offset(0f, size.height/2)
        if (hasLeftPoint) {
            drawCircle(
                color = color,
                radius = pointRadius,
                center = leftPointNonNull
            )
        }
        val path = Path().apply {
            moveTo(x = leftPointNonNull.x, y = leftPointNonNull.y)
            lineTo(x = leftPointNonNull.x, y = size.height/2)
            lineTo(x = size.width, y = size.height/2)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = connectorWidth.toPx(),
                pathEffect = PathEffect.cornerPathEffect(cornerRadius.toPx()),
                cap = StrokeCap.Round
            )
        )
        if (hasRightPoint) {
            drawCircle(
                color = color,
                radius = pointRadius,
                center = Offset(size.width, size.height/2)
            )
        }
    }
}


@Composable
fun SingleConnector3 (
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    hasLeftPoint: Boolean = true,
    hasRightPoint: Boolean = true,
    leftPoint: Offset,
    rightPoint: Offset,
) {
    val pointRadius = 4.dp
    val cornerRadius = dimensionResource(id = R.dimen.connector_corner_radius)
    val connectorWidth = dimensionResource(id = R.dimen.connector_line_width)

    Canvas(modifier = modifier) {
        if (hasLeftPoint) {
            drawCircle(
                color = color,
                radius = pointRadius.toPx(),
                center = leftPoint
            )
        }
        val path = Path().apply {
            moveTo(x = leftPoint.x, y = leftPoint.y)
            lineTo(x = leftPoint.x, y = leftPoint.y + 16.dp.toPx())
            lineTo(x = rightPoint.x, y = leftPoint.y + 16.dp.toPx())
            lineTo(x = rightPoint.x, y = rightPoint.y)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = connectorWidth.toPx(),
                pathEffect = PathEffect.cornerPathEffect(cornerRadius.toPx()),
                cap = StrokeCap.Round
            )
        )
        if (hasRightPoint) {
            drawCircle(
                color = color,
                radius = pointRadius.toPx(),
                center = rightPoint
            )
        }
    }
}


@Composable
fun SingleConnector4 (
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    hasTopPoint: Boolean = true,
    hasBottomPoint: Boolean = true,
    topPoint: Offset,
    bottomPoint: Offset,
) {
    val pointRadius = 4.dp
    val cornerRadius = dimensionResource(id = R.dimen.connector_corner_radius)
    val connectorWidth = dimensionResource(id = R.dimen.connector_line_width)

    Canvas(modifier = modifier) {
        if (hasTopPoint) {
            drawCircle(
                color = color,
                radius = pointRadius.toPx(),
                center = topPoint
            )
        }
        val path = Path().apply {
            moveTo(x = topPoint.x, y = topPoint.y)
            lineTo(x = bottomPoint.x - 16.dp.toPx(), y = topPoint.y)
            lineTo(x = bottomPoint.x - 16.dp.toPx(), y = bottomPoint.y)
            lineTo(x = bottomPoint.x, y = bottomPoint.y)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = connectorWidth.toPx(),
                pathEffect = PathEffect.cornerPathEffect(cornerRadius.toPx()),
                cap = StrokeCap.Round
            )
        )
        if (hasBottomPoint) {
            drawCircle(
                color = color,
                radius = pointRadius.toPx(),
                center = bottomPoint
            )
        }
    }
}