package com.takaapoo.weatherer.ui.screens.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.theme.Gray10
import com.takaapoo.weatherer.ui.theme.Gray20
import com.takaapoo.weatherer.ui.theme.Gray30
import com.takaapoo.weatherer.ui.theme.Gray50
import com.takaapoo.weatherer.ui.theme.WeathererTheme

@Composable
fun IndicatorSlider(
    modifier: Modifier = Modifier,
    thumbPosition: Float,
    onUpdateThumbPosition: (movement: Float) -> Unit
){
    val railHeightDp = dimensionResource(id = R.dimen.diagram_slider_height)
    val gapHeightDp = dimensionResource(id = R.dimen.diagram_slider_gap_height)
    val thumbSizeDp = dimensionResource(id = R.dimen.diagram_slider_thumb_size)
    var thumbMovementLength by rememberSaveable { mutableFloatStateOf(0f) }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val railHeight = railHeightDp.toPx()
            val gapHeight = gapHeightDp.toPx()
            val gapMargin = (railHeight - gapHeight) / 2
            val thumbSize = thumbSizeDp.toPx()
            val railRect = Rect(
                left = thumbSize/2 - gapMargin,
                top = (size.height - railHeight) / 2,
                right = size.width - (thumbSize/2 - gapMargin),
                bottom = (size.height + railHeight) / 2
            )
            thumbMovementLength = railRect.width - 2 * gapMargin

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Gray20, Gray10),
                    startY = railRect.top,
                    endY = railRect.bottom
                ),
                topLeft = Offset(railRect.left, railRect.top),
                size = Size(width = railRect.width, height = railRect.height),
                cornerRadius = CornerRadius(railHeight / 2),
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Gray10, Gray30),
                    startY = railRect.top,
                    endY = railRect.bottom
                ),
                topLeft = Offset(railRect.left, railRect.top),
                size = Size(width = railRect.width, height = railRect.height),
                cornerRadius = CornerRadius(railHeight / 2),
                style = Stroke(width = 4f)
            )
            drawRoundRect(
                color = Gray50,
                topLeft = Offset(railRect.left + gapMargin, railRect.top + gapMargin),
                size = Size(width = railRect.width - 2 * gapMargin, height = gapHeight),
                cornerRadius = CornerRadius(gapHeight / 2),
            )
//            innerShadow(
//                color = DiagramShadow,
//                rect = railRect,
//                cornersRadius = (railHeight/2).toDp(),
//                blur = 3.dp
//            )
        }

        Image(
            modifier = Modifier
                .graphicsLayer {
                    translationX = thumbMovementLength * thumbPosition
                }
                .size(dimensionResource(id = R.dimen.diagram_slider_thumb_size))
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        onUpdateThumbPosition(dragAmount / thumbMovementLength)
                        change.consume()
                    }
                }
            ,
            painter = painterResource(id = R.drawable.slider_handle),
            contentDescription = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IndicatorSliderPreview(modifier: Modifier = Modifier) {
    WeathererTheme {
        IndicatorSlider(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .height(24.dp),
            thumbPosition = 0.3f,
            onUpdateThumbPosition = {}
        )
    }
}