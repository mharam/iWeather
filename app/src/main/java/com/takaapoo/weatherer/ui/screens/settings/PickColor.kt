package com.takaapoo.weatherer.ui.screens.settings

import android.graphics.Region
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.theme.Gray30
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.theme.timeFontFamily
import com.takaapoo.weatherer.utility.BorderedText
import com.takaapoo.weatherer.utility.hue
import com.takaapoo.weatherer.utility.lightness
import com.takaapoo.weatherer.utility.mostContrastColor
import com.takaapoo.weatherer.utility.plus
import com.takaapoo.weatherer.utility.saturation
import com.takaapoo.weatherer.utility.times
import com.takaapoo.weatherer.utility.toDp
import com.takaapoo.weatherer.utility.toPx
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

val preferredOuterRadius = 128.dp
val OffsetToVector: TwoWayConverter<Offset, AnimationVector2D> = TwoWayConverter(
    convertToVector = { offset -> AnimationVector2D(offset.x, offset.y) },
    convertFromVector = { vector -> Offset(vector.v1, vector.v2) }
)
val colorSaver = Saver<MutableState<Color>, Int>(
    save = { it.value.toArgb() },
    restore = { mutableStateOf(Color(it)) }
)
val ringColors = listOf(Color(0xffff0000), Color(0xffffff00), Color(0xff00ff00),
    Color(0xff00ffff), Color(0xff0000ff), Color(0xffff00ff),
    Color(0xffff0000))
const val pi = PI.toFloat()


@Composable
fun PickColor(
    modifier: Modifier = Modifier,
    initialColor: Color = Color(0xFFBA3B98),
    initializeColor: Int = 0,

    onModifyColor: (color: Color) -> Unit,
    onSaveCurvesColor: (color: Color) -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var sizeScale by remember { mutableFloatStateOf(1f) }
    val outerRadius = 128.dp * sizeScale
    val outerRadiusPxSquared = outerRadius.toPx(density).let { it * it }
    val innerRadius = (preferredOuterRadius - 32.dp) * sizeScale
    val innerRadiusPx = innerRadius.toPx(density)
    val innerRadiusPxSquared = innerRadiusPx * innerRadiusPx


    val triangleCornerDiameter = 16.dp * sizeScale
    val triangleCornerPadding = 16.dp * sizeScale
    val trianglePointerSize = 24.dp * sizeScale
    val trianglePointerRadiusPx = trianglePointerSize.toPx(density) / 2

    val radius by remember(sizeScale) {
        mutableFloatStateOf((innerRadius - triangleCornerPadding).toPx(density))
    }

    val initialHue = initialColor.hue()
    val initialSaturation = initialColor.saturation()
    val initialLightness = initialColor.lightness()
    val initialTrianglePointerPosition = (
        Offset(
            innerRadiusPx - radius/2 +
                    initialSaturation * min(initialLightness, 1f - initialLightness) * 3 * radius,
            innerRadiusPx + (initialLightness - 0.5f) * sqrt(3f) * radius
        )
    )
    val k = if (initialHue <= 180) 0 else 1
    val initialAngle = ( ((-initialHue + k * 360)) * (pi/180) )

    var angle = remember { Animatable(initialAngle) }
    var inRegion by remember { mutableStateOf(false) }
    var inPointer by remember { mutableStateOf(false) }
    val ringPointerSize by animateDpAsState(
        if (inPointer) (outerRadius - innerRadius) + 8.dp * sizeScale
        else (outerRadius - innerRadius) + 16.dp * sizeScale
    )
    val ringPointerElevation by animateDpAsState(if (inPointer) 1.dp else 4.dp)




    val triangleCenter by remember(sizeScale) {
        mutableStateOf(Offset(innerRadiusPx, innerRadiusPx))
    }
    val point1 by remember(sizeScale) { mutableStateOf(triangleCenter + Offset(radius, 0f)) }
    val point2 by remember(sizeScale) {
        mutableStateOf(triangleCenter + Offset(radius * cos(2*pi/3), radius * sin(2*pi/3)))
    }
    val point3 by remember(sizeScale) {
        mutableStateOf(triangleCenter + Offset(radius * cos(4*pi/3), radius * sin(4*pi/3)))
    }

    val triangleHeight = 1.5f * radius

    val trianglePath by remember(sizeScale) {
        mutableStateOf(Path().apply {
            moveTo(point1.x, point1.y)
            lineTo(point2.x, point2.y)
            lineTo(point3.x, point3.y)
            close()
        })
    }
    val triangleRegion by remember(sizeScale) {
        mutableStateOf(Region().apply {
            setPath(
                trianglePath.asAndroidPath(),
                Region(0, 0, (2 * radius).toInt(), (2 * radius).toInt())
            )
        })
    }
    var trianglePointerPosition = remember(sizeScale) {
        Animatable(
            initialValue = initialTrianglePointerPosition,
            typeConverter = OffsetToVector
        )
    }
    var colorIndicatorWidth by remember { mutableStateOf(96.dp) }

    var onModifyColorPermission by rememberSaveable(initializeColor) { mutableStateOf(false) }
    val selectedButtonInitialColor by rememberSaveable(initializeColor, saver = colorSaver) {
        mutableStateOf(initialColor)
    }
    LaunchedEffect(initializeColor) {
        launch {
            onModifyColorPermission = false
            if (abs(initialAngle - angle.value) <= pi)
                angle.animateTo(initialAngle)
            else {
                angle.animateTo(
                    if (initialAngle > 0) initialAngle - 2 * pi
                    else initialAngle + 2 * pi
                )
                angle.snapTo(initialAngle)
            }
            onModifyColorPermission = true
        }
        launch {
            trianglePointerPosition.animateTo(initialTrianglePointerPosition)
        }
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                val dimension =
                    min(it.height, it.width - (colorIndicatorWidth).toPx(density).toInt())
                sizeScale =
                    (dimension - 2 * 16.dp.toPx(density)) / (preferredOuterRadius.toPx(density) * 2)
            },
    ) {
        val pointerColor = linearInterpolateColors(
            colors = ringColors,
            fraction = ((-angle.value + 2*pi) / (2*pi)) % 1f
        )
        val trianglePointerColor = trianglePointerPosition.value.trianglePointerColor(
            point1,
            point2,
            point3,
            baseColor = pointerColor,
            triangleHeight
        )

        if (onModifyColorPermission)
            onModifyColor(trianglePointerColor)

        Box(
            modifier = Modifier
                .size((outerRadius + 16.dp) * 2)
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    val center = this.size.width / 2
                    detectDragGestures(
                        onDrag = { pointerInput, dragAmount ->
                            pointerInput.consume()
                            if (inRegion) {
                                scope.launch {
                                    angle.snapTo(
                                        targetValue = atan2(
                                            y = center - pointerInput.position.y,
                                            x = pointerInput.position.x - center
                                        )
                                    )
                                }
                            } else
                                inPointer = false
                        },
                        onDragEnd = {
                            inRegion = false
                            inPointer = false
                            onSaveCurvesColor(
                                calculateTrianglePointerColor(
                                    angle = angle.value,
                                    point1,
                                    point2,
                                    point3,
                                    trianglePointerPosition.value,
                                    triangleHeight
                                )
                            )
                        }
                    )
                }
                .pointerInput(Unit) {
                    val center = this.size.width / 2
                    val midRadius = (outerRadius + innerRadius).toPx() / 2
                    detectTapGestures(
                        onPress = { offset ->
                            val y = center - offset.y
                            val x = offset.x - center
                            val pointerCenterOffset = Offset(
                                x = center + midRadius * cos(angle.value),
                                y = center - midRadius * sin(angle.value)
                            )
                            val distanceSquared = x * x + y * y
                            inRegion = distanceSquared < outerRadiusPxSquared &&
                                    distanceSquared > innerRadiusPxSquared
                            val pointerRadius = ringPointerSize.toPx() / 2
                            inPointer = pointerCenterOffset.distanceToSquared(offset) <
                                    pointerRadius * pointerRadius
                            if (inRegion && !inPointer)
                                scope.launch {
                                    val newAngle = atan2(y, x)
                                    inPointer = true
                                    if (abs(newAngle - angle.value) <= pi)
                                        angle.animateTo(newAngle)
                                    else {
                                        angle.animateTo(
                                            if (newAngle > 0) newAngle - 2 * pi
                                            else newAngle + 2 * pi
                                        )
                                        angle.snapTo(newAngle)
                                    }
                                    onSaveCurvesColor(
                                        calculateTrianglePointerColor(
                                            angle = angle.value,
                                            point1,
                                            point2,
                                            point3,
                                            trianglePointerPosition.value,
                                            triangleHeight
                                        )
                                    )
                                }
                            inPointer = !tryAwaitRelease()
                        }
                    )
                },
        ){
            Canvas(modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
            ) {
                drawIntoCanvas {
                    it.saveLayer(Rect(0f, 0f, size.width, size.height), Paint())
                }
                drawCircle(
                    radius = outerRadius.toPx(),
                    brush = Brush.sweepGradient(ringColors)
                )
                drawCircle(
                    color = Transparent,
                    radius = innerRadius.toPx(),
                    blendMode = BlendMode.Clear
                )
                drawIntoCanvas {
                    it.restore()
                }
            }
            Box(
                modifier = Modifier
                    .size(innerRadius * 2)
                    .align(Alignment.Center)
                    .graphicsLayer {
                        rotationZ = -angle.value * 180f / pi
                    }
            ){
                var pointerPressed by remember { mutableStateOf(false) }
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { pointerInput, _ ->
                                pointerInput.consume()
                                if (triangleRegion.contains(
                                        pointerInput.position.x.toInt(),
                                        pointerInput.position.y.toInt()
                                    )
                                ) {
                                    scope.launch {
                                        trianglePointerPosition.snapTo(pointerInput.position)
                                    }
                                } else {
                                    scope.launch {
                                        trianglePointerPosition.snapTo(
                                            findNearestPointOnTriangle(
                                                a = point1,
                                                b = point2,
                                                c = point3,
                                                p1 = pointerInput.position
                                            )
                                        )
                                    }
                                }
                            },
                            onDragEnd = {
                                pointerPressed = false
                                onSaveCurvesColor(
                                    calculateTrianglePointerColor(
                                        angle = angle.value,
                                        point1,
                                        point2,
                                        point3,
                                        trianglePointerPosition.value,
                                        triangleHeight
                                    )
                                )
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                if (triangleRegion.contains(offset.x.toInt(), offset.y.toInt())) {
                                    scope.launch {
                                        trianglePointerPosition.animateTo(offset)
                                        onSaveCurvesColor(
                                            calculateTrianglePointerColor(
                                                angle = angle.value,
                                                point1,
                                                point2,
                                                point3,
                                                trianglePointerPosition.value,
                                                triangleHeight
                                            )
                                        )
                                    }
                                    pointerPressed = true
                                }
                            }
                        )
                    }
                ) {
                    val paint0 = Paint().apply {
                        pathEffect = PathEffect.cornerPathEffect(triangleCornerDiameter.toPx())
                        color = Color(0xFF000000)
                        blendMode = BlendMode.Clear
                    }
                    val paint1 = Paint().apply {
                        pathEffect = PathEffect.cornerPathEffect(triangleCornerDiameter.toPx())
                        shader = LinearGradientShader(
                            from = point1,
                            to = (point2 + point3) / 2f,
                            colors = listOf(pointerColor, Color(0x00000000)),
                        )
                        blendMode = BlendMode.Plus
                    }
                    val paint2 = Paint().apply {
                        pathEffect = PathEffect.cornerPathEffect(triangleCornerDiameter.toPx())
                        shader = LinearGradientShader(
                            from = point2,
                            to = (point1 + point3) / 2f,
                            colors = listOf(Color.White, Color(0x00000000)),
                        )
                        blendMode = BlendMode.Plus
                    }
                    val paint3 = Paint().apply {
                        pathEffect = PathEffect.cornerPathEffect(triangleCornerDiameter.toPx())
                        shader = LinearGradientShader(
                            from = point3,
                            to = (point1 + point2) / 2f,
                            colors = listOf(Color.Black, Color(0x00000000)),
                        )
                        blendMode = BlendMode.Plus
                    }
                    drawIntoCanvas {
                        it.drawPath(trianglePath, paint0)
                        it.drawPath(trianglePath, paint1)
                        it.drawPath(trianglePath, paint2)
                        it.drawPath(trianglePath, paint3)
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .graphicsLayer {
                            translationX = trianglePointerPosition.value.x - trianglePointerRadiusPx
                            translationY = trianglePointerPosition.value.y - trianglePointerRadiusPx
                        }
//                        .padding(triangleCornerPadding + (triangleCornerDiameter - trianglePointerSize) / 2)
                        .size(trianglePointerSize)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape
                        )
                        .background(
                            color = trianglePointerColor,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp * sizeScale,
                            color = Gray30,
                            shape = CircleShape
                        )
                )

            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = -angle.value * 180f / pi
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .padding(
                            (16.dp - (ringPointerSize - (outerRadius - innerRadius)) / 2).coerceAtLeast(
                                0.dp
                            )
                        )
                        .shadow(
                            elevation = ringPointerElevation,
                            shape = CircleShape
                        )
                        .size(ringPointerSize)
                        .background(
                            color = pointerColor,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp * sizeScale,
                            color = Gray30,
                            shape = CircleShape
                        )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.TopStart)
                .onSizeChanged {
                    colorIndicatorWidth = it.width.toDp(density)
                },
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                modifier = Modifier.padding(16.dp),
                onClick = {
                    val initialHue = selectedButtonInitialColor.hue()
                    val initialSaturation = selectedButtonInitialColor.saturation()
                    val initialLightness = selectedButtonInitialColor.lightness()
                    val initialTrianglePointerPosition = Offset(
                        innerRadiusPx - radius/2 +
                                initialSaturation * min(initialLightness, 1f - initialLightness) * 3 * radius,
                        innerRadiusPx + (initialLightness - 0.5f) * sqrt(3f) * radius
                    )
                    val k = if (initialHue <= 180) 0 else 1
                    val initialAngle = ((-initialHue + k * 360)) * (pi/180)

                    scope.launch {
                        val angleJob = launch {
                            if (abs(initialAngle - angle.value) <= pi)
                                angle.animateTo(initialAngle)
                            else {
                                angle.animateTo(
                                    if (initialAngle > 0) initialAngle - 2 * pi
                                    else initialAngle + 2 * pi
                                )
                                angle.snapTo(initialAngle)
                            }
                        }
                        val trianglePointerJob = launch {
                            trianglePointerPosition.animateTo(initialTrianglePointerPosition)
                        }
                        listOf(angleJob, trianglePointerJob).joinAll()
                        onSaveCurvesColor(
                            calculateTrianglePointerColor(
                                angle = angle.value,
                                point1,
                                point2,
                                point3,
                                trianglePointerPosition.value,
                                triangleHeight
                            )
                        )
                    }
                }
            ) {
                Icon(painter = painterResource(R.drawable.undo), contentDescription = "Undo")
            }
            val shape = RoundedCornerShape(16.dp)
            BorderedText(
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 16.dp)
                    .shadow(elevation = 4.dp, shape = shape)
                    .border(
                        width = dimensionResource(R.dimen.connector_line_width),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = shape
                    )
                    .background(color = trianglePointerColor, shape = shape)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 18.sp,
                fontFamily = timeFontFamily,
                strokeWidth = 1f,
                fillColor = trianglePointerColor.mostContrastColor(),
                text = buildAnnotatedString {
                    append("R = %2X\nG = %2X\nB = %2X"
                        .format((trianglePointerColor.red * 255).roundToInt(),
                            (trianglePointerColor.green * 255).roundToInt(),
                            (trianglePointerColor.blue * 255).roundToInt()
                        )
                    )
                }
            )
        }
    }
}

private fun calculateTrianglePointerColor(
    angle: Float,
    point1: Offset,
    point2: Offset,
    point3: Offset,
    trianglePointerPosition: Offset,
    triangleHeight: Float,
): Color {
    val pointerColor = linearInterpolateColors(
        colors = ringColors,
        fraction = ((-angle + 2*pi) / (2*pi)) % 1f
    )
    return trianglePointerPosition.trianglePointerColor(
        point1,
        point2,
        point3,
        baseColor = pointerColor,
        triangleHeight
    )
}

private fun linearInterpolateColors(colors: List<Color>, fraction: Float): Color {
    return when (colors.size){
        0 -> Transparent
        1 -> colors[0]
        2 -> Color(
            red = (1-fraction) * colors[0].red + fraction * colors[1].red,
            green = (1-fraction) * colors[0].green + fraction * colors[1].green,
            blue = (1-fraction) * colors[0].blue + fraction * colors[1].blue,
        )
        else -> {
            if (fraction == 1f) colors.last()
            else {
                val index1 = (fraction * (colors.size - 1)).toInt()
                val index2 = index1 + 1
                val newFrac = fraction * (colors.size - 1) - index1
                Color(
                    red = (1-newFrac) * colors[index1].red + newFrac * colors[index2].red,
                    green = (1-newFrac) * colors[index1].green + newFrac * colors[index2].green,
                    blue = (1-newFrac) * colors[index1].blue + newFrac * colors[index2].blue,
                )
            }
        }
    }
}

private fun findNearestPointOnTriangle(
    a: Offset,
    b: Offset,
    c: Offset,
    p1: Offset,
): Offset {
    // Find nearest point on each edge
    val nearestAB = nearestPointOnLineSegment(a, b, p1)
    val nearestBC = nearestPointOnLineSegment(b, c, p1)
    val nearestCA = nearestPointOnLineSegment(c, a, p1)

    // Find which of these is closest to p1
    val distances = listOf(
        p1.distanceToSquared(nearestAB) to nearestAB,
        p1.distanceToSquared(nearestBC) to nearestBC,
        p1.distanceToSquared(nearestCA) to nearestCA
    )

    return distances.minByOrNull { it.first }?.second ?: p1
}

private fun nearestPointOnLineSegment(a: Offset, b: Offset, p: Offset): Offset {
    val ab = b - a
    val ap = p - a

    val ab2 = ab.x * ab.x + ab.y * ab.y
    val apAb = ap.x * ab.x + ap.y * ab.y

    var t = apAb / ab2
    t = t.coerceIn(0.0f, 1.0f)

    return a + ab * t
}

private fun Offset.distanceToSquared(other: Offset): Float {
    val dx = this.x - other.x
    val dy = this.y - other.y
    return dx * dx + dy * dy
}

private fun Offset.distanceToLineSegment(a: Offset, b: Offset): Float {
    if (this == a || this == b) return 0f
    val ab = b - a
    val ap = this - a

    val ab2 = ab.x * ab.x + ab.y * ab.y
    val ap2 = ap.x * ap.x + ap.y * ap.y
    val apAb = ap.x * ab.x + ap.y * ab.y

    val cos2 = (apAb * apAb) / (ab2 * ap2)
    val sin2 = 1f - cos2
    return sqrt(ap2 * sin2)
}

private fun Offset.trianglePointerColor(
    pointer1: Offset,
    pointer2: Offset,
    pointer3: Offset,
    baseColor: Color,
    triangleHeight: Float,
): Color {
    val color1 = baseColor * (this.distanceToLineSegment(pointer2, pointer3) / triangleHeight)
    val color2 = Color.Black * (this.distanceToLineSegment(pointer1, pointer2) / triangleHeight)
    val color3 = Color.White * (this.distanceToLineSegment(pointer1, pointer3) / triangleHeight)

    return color1 + color2 + color3
}


@Preview(showBackground = true)
@Composable
fun PickColorPreview() {
    WeathererTheme{
        PickColor(
            modifier = Modifier
                .height(288.dp)
                .fillMaxWidth(),
            onModifyColor = {},
            onSaveCurvesColor = {}
        )
    }
}