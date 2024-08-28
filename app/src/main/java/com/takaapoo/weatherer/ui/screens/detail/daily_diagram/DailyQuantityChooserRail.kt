package com.takaapoo.weatherer.ui.screens.detail.daily_diagram

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.DiagramRailShape
import com.takaapoo.weatherer.ui.screens.detail.diagramRailPath
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.pathInnerShadow
import com.takaapoo.weatherer.ui.screens.home.toPx
import com.takaapoo.weatherer.ui.theme.DiagramShadow
import com.takaapoo.weatherer.ui.theme.Gray20

@Composable
fun DailyQuantityChooserRail(
    modifier: Modifier = Modifier,
    quantity: DailyWeatherQuantity,
    onUpdateChartQuantity: (DailyWeatherQuantity) -> Unit
) {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    @DrawableRes val displayingIconId  = quantity.iconId
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
            painter = painterResource(displayingIconId),
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
                DailyWeatherQuantity.entries
                    .forEach { dailyWeatherQuantity ->
                        val isSelected = quantity  == dailyWeatherQuantity
//                        val chipColor = curveColors
//                            .getOrNull(quantities.indexOf(dailyWeatherQuantity))
//                            ?.copy(alpha = 0.5f) ?: Color.Unspecified
                        FilterChip(
                            modifier = Modifier
                                .height(32.dp)
                                /*.onGloballyPositioned {
                                    if (it.positionInRoot().x < 80.dp.toPx(density)) {
                                        displayingIconId = dailyWeatherQuantity.iconId
                                    }
                                }*/,
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) onUpdateChartQuantity(dailyWeatherQuantity)
                            },
//                            colors = FilterChipDefaults.filterChipColors(
//                                selectedContainerColor = chipColor
//                            ),
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
                                Text(text = stringResource(id = dailyWeatherQuantity.nameId))
                            }
                        )
                    }
                Spacer(modifier = Modifier)
            }
        }
    }
}