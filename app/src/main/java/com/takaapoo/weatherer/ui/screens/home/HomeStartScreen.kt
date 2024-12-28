package com.takaapoo.weatherer.ui.screens.home

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.screens.home.shapes.AddLocationShape
import com.takaapoo.weatherer.ui.utility.BorderedText
import com.takaapoo.weatherer.ui.utility.lighter
import com.takaapoo.weatherer.ui.utility.toDp


@Composable
fun HomeStartScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddLocationScreen: () -> Unit,

) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "text")
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        val rad by infiniteTransition.animateValue(
            initialValue = -0.4f,
            targetValue = 0.8f,
            typeConverter = Float.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    delayMillis = 1000
                    durationMillis = 1000
                    -0.4f at 0 using CubicBezierEasing(0.57f, 0f, 0.4f, 1f)
                    -0.6f at 150 using CubicBezierEasing(0.57f, 0f, 0.57f, 1.25f)
                    -0.4f at 300 using LinearEasing
                    0.8f at 1000
                }
            ),
            label = "radius"
        )
        val colorStops = arrayOf(
            rad + 0.05f to Color.Transparent,
            rad + 0.2f to MaterialTheme.colorScheme.primary.copy(alpha = (0.8f - rad).coerceIn(0f, 1f)),
            rad + 0.45f to Color.Transparent,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .align(Alignment.Center)
                .graphicsLayer {
                    alpha = if (rad <= -0.4f) 0f else 1f
                    rotationX = 80f
                    cameraDistance = 10f
                }
                .background(
                    brush = Brush.radialGradient(colorStops = colorStops),
                    shape = CircleShape
                )
        )
        val size = dimensionResource(R.dimen.start_add_location)
        val shape = AddLocationShape()
        FilledIconButton(
            modifier = Modifier.size(size).align(Alignment.Center)
                .offset { IntOffset(0, (rad.coerceAtMost(-0.4f) * size.toPx()).toInt()) }
                .shadow(elevation = 8.dp, shape = shape)
            ,
            onClick = onNavigateToAddLocationScreen,
            shape = shape
        ) {
            Icon(
                modifier = Modifier.size(size * 0.75f).padding(bottom = 24.dp),
                painter = painterResource(R.drawable.add_24px),
                contentDescription = "Add Location"
            )
        }

        var textWidth by rememberSaveable { mutableFloatStateOf(0f) }
        val offsetWidth = textWidth / 2
        val offset = (10/8f) * rad * textWidth
        /*by infiniteTransition.animateValue(
            initialValue = - offsetWidth,
            targetValue = textWidth,
            typeConverter = Float.VectorConverter,
            animationSpec = infiniteRepeatable(tween(
                durationMillis = 900,
                delayMillis = 900,
                easing = LinearEasing
            )),
            label = "offset"
        )*/
        val textBrush = Brush.horizontalGradient(
            colors =  listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.lighter(0.5f),
                MaterialTheme.colorScheme.primary.lighter(0.5f),
                MaterialTheme.colorScheme.primary
            ),
            startX = offset,
            endX = offset + offsetWidth
        )
        val text = "Add your locations"
        val maxShift = 0.5f


        val annotatedText = AnnotatedString.Builder(capacity = text.length).apply {
            text.forEachIndexed { i, char ->
                val shift = shiftAmount(offset, offsetWidth, (i * textWidth / text.length), maxShift)
                val style = SpanStyle(
                    baselineShift = BaselineShift(shift),
                )
                withStyle(style = style){
                    append(char)
                }
            }
        }.toAnnotatedString()

        BorderedText(
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = 24.dp + WindowInsets.systemBars.getBottom(density).toDp(density))
                .onSizeChanged {
                    textWidth = it.width.toFloat()
                }
            ,
            text = annotatedText,
            fontSize = 28.sp,
            strokeWidth = 4f,
            style = TextStyle(
                brush = textBrush
            )
        )
    }
}

fun shiftAmount(offset: Float, offsetWidth: Float, charDistance: Float, maxShift: Float): Float {
    if (charDistance !in offset .. offset + offsetWidth) return 0f
    val ratio = (charDistance - offset) / offsetWidth
    return when {
        ratio < 1/3f -> (charDistance - offset) * 3 * maxShift / offsetWidth
        ratio < 2/3f -> maxShift
        else -> (offsetWidth - (charDistance - offset)) * 3 * maxShift / offsetWidth
    }
}