package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import android.graphics.BlurMaskFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.screens.detail.DiagramRailShape
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.diagramRailPath
import com.takaapoo.weatherer.ui.screens.home.toPx
import com.takaapoo.weatherer.ui.theme.DiagramShadow
import com.takaapoo.weatherer.ui.theme.Gray10
import com.takaapoo.weatherer.ui.theme.Gray20

@Composable
fun QuantityChooserRail(
    modifier: Modifier = Modifier,
    quantities: List<WeatherQuantity>,
    isAirQualityQuantities: Boolean,
    onRemoveChartQuantity: (WeatherQuantity) -> Unit,
    onAddChartQuantity: (WeatherQuantity) -> Unit
) {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    @DrawableRes var displayingIconId by rememberSaveable {
        mutableIntStateOf(R.drawable.temperature)
    }
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 24.dp,
                        bottomEnd = 24.dp,
                        bottomStart = 0.dp
                    )
                )
                .padding(12.dp),
            painter = painterResource(
                id = if (isAirQualityQuantities) R.drawable.mask
                else displayingIconId
            ),
            contentDescription = "weather related curves"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(DiagramRailShape())
                .drawWithContent {
                    val path = diagramRailPath(size.width, size.height, density)
                    drawContent()
                    drawPath(
                        path = path,
                        color = Gray20,
                        style = Stroke(3f)
                    )
                    pathInnerShadow(
                        color = DiagramShadow,
                        path = path,
                        blur = 4.dp
                    )
                }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(state = scrollState)
            ) {
                Spacer(modifier = Modifier.width(56.dp))
                WeatherQuantity.entries.filter { it.airQuality == isAirQualityQuantities }
                    .forEach { weatherQuantity ->
                        val isSelected = quantities.contains(weatherQuantity)
                        val chipColor = curveColors
                            .getOrNull(quantities.indexOf(weatherQuantity))
                            ?.copy(alpha = 0.5f) ?: Color.Unspecified
                        FilterChip(
                            modifier = Modifier
                                .height(32.dp)
                                .onGloballyPositioned {
                                    if (it.positionInRoot().x < 80.dp.toPx(density)) {
                                        displayingIconId = weatherQuantity.iconId
                                    }
                                },
                            selected = isSelected,
                            onClick = {
                                if (isSelected) onRemoveChartQuantity(weatherQuantity)
                                else onAddChartQuantity(weatherQuantity)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor
                            ),
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Done,
                                        contentDescription = "Done icon",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            },
                            label = {
                                if (weatherQuantity.nameId == null)
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            modifier = Modifier.align(Alignment.BottomCenter),
                                            text = weatherQuantity.title!!
                                        )
                                    }
                                else
                                    Text(text = stringResource(id = weatherQuantity.nameId))
                            }
                        )
                    }
                Spacer(modifier = Modifier)
            }
        }
    }
}

fun DrawScope.pathInnerShadow(
    color: Color = Gray10,
    horizontalPadding: Float = 0f,
    verticalPadding: Float = 0f,
    path: Path,
    blur: Dp = 6.dp,
){
    val rect = Rect(
        topLeft = Offset(horizontalPadding, verticalPadding),
        bottomRight = Offset(size.width - horizontalPadding, size.height - verticalPadding)
    )
    val paint = Paint()

    drawIntoCanvas {
        paint.color = color
        paint.isAntiAlias = true
        it.saveLayer(rect, paint)
        it.drawPath(
            path = path,
            paint = paint
        )
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        if (blur.toPx() > 0) {
            frameworkPaint.maskFilter = BlurMaskFilter(blur.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
        paint.color = Color.Black
        scale(
            scaleX = 0.995f,
            scaleY = 0.985f,
            pivot = Offset(0.7f * size.width, size.height)
        ){
            it.drawPath(
                path = path,
                paint = paint
            )
        }
        frameworkPaint.xfermode = null
        frameworkPaint.maskFilter = null
    }
}