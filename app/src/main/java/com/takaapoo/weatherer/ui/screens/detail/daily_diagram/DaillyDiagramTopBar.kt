package com.takaapoo.weatherer.ui.screens.detail.daily_diagram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.ChartState
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.home.toSp
import com.takaapoo.weatherer.ui.theme.customColorScheme

@Composable
fun BoxScope.DailyDiagramTopBar(
    modifier: Modifier = Modifier,
    dailyHeaderTop: Float,
    barAlpha: Float,
    detailState: DetailState,
    chartState: ChartState,
    dailyChartState: DailyChartState,
    onUpdateDailyDiagramSettingOpen: (open: Boolean) -> Unit,
    onUpdateChartWeatherConditionVisibility: (visible: Boolean) -> Unit,
    onUpdateChartCurveShadowVisibility: (visible: Boolean) -> Unit,
    onUpdateChartGrids: (grids: ChartGrids) -> Unit,
    onUpdateChooseDiagramThemeDialogVisibility: () -> Unit,
    onUpdateDailyDiagramSettingRectangle: (rect: Rect) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .graphicsLayer {
                translationY = dailyHeaderTop
                alpha = barAlpha
            }
            .padding(
                horizontal = 8.dp,
                vertical = dimensionResource(id = R.dimen.detail_screen_subtitle_vertical_padding)
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Spacer(modifier = Modifier.width(48.dp))
        DailyDiagramsTitle(
            modifier = Modifier.height(48.dp)
        )
        Column(
            modifier = Modifier
                .width(48.dp)
                .height(if (detailState.dailyDiagramSettingOpen) 240.dp else 48.dp)
                .graphicsLayer {
                    shape = RoundedCornerShape(percent = 50)
                    shadowElevation = 8.dp.toPx()
                    clip = true
                }
                .zIndex(2f)
                .onGloballyPositioned {
                    onUpdateDailyDiagramSettingRectangle(it.boundsInRoot())
                }
                .background(color = MaterialTheme.customColorScheme.detailScreenSurface),
            verticalArrangement = Arrangement.Top
        ) {
            IconToggleButton(
                checked = detailState.dailyDiagramSettingOpen,
                onCheckedChange = {
                    onUpdateDailyDiagramSettingOpen(it)
                },
                enabled = barAlpha > 0.9f
            ) {
                Icon(
                    imageVector = if (detailState.dailyDiagramSettingOpen) Icons.Filled.Settings
                    else Icons.Outlined.Settings,
                    contentDescription = stringResource(id = R.string.settings)
                )
            }
            IconToggleButton(
                checked = chartState.curveShadowVisible,
                onCheckedChange = {
                    onUpdateChartCurveShadowVisibility(it)
                }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (chartState.curveShadowVisible) R.drawable.shadow_remove
                        else R.drawable.shadow_add
                    ),
                    contentDescription = stringResource(id = R.string.show_curve_shadow)
                )
            }
            IconToggleButton(
                checked = dailyChartState.weatherConditionIconsVisible,
                onCheckedChange = {
                    onUpdateChartWeatherConditionVisibility(it)
                }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (dailyChartState.weatherConditionIconsVisible) R.drawable.partly_cloudy_day_fill_24px
                        else R.drawable.partly_cloudy_day_24px
                    ),
                    contentDescription = stringResource(id = R.string.show_weather_condition_icon)
                )
            }
            IconButton(
                onClick = {
                    onUpdateChartGrids(
                        when (chartState.chartGrid){
                            ChartGrids.All -> ChartGrids.MAIN
                            ChartGrids.MAIN -> ChartGrids.NON
                            ChartGrids.NON -> ChartGrids.All
                        }
                    )
                }
            ) {
                Icon(
                    painter = painterResource(id = when (chartState.chartGrid){
                        ChartGrids.All -> R.drawable.grid_show
                        ChartGrids.MAIN -> R.drawable.grid_main_show
                        ChartGrids.NON -> R.drawable.grid_not_show
                    }),
                    tint = when (chartState.chartGrid){
                        ChartGrids.All -> MaterialTheme.colorScheme.primary
                        ChartGrids.MAIN -> LocalContentColor.current
                        ChartGrids.NON -> MaterialTheme.customColorScheme.noGridIcon
                    },
                    contentDescription = stringResource(id = R.string.show_hide_grids)
                )
            }
            IconButton(
                onClick = {
                    onUpdateChooseDiagramThemeDialogVisibility()
                    onUpdateDailyDiagramSettingOpen(false)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.theme),
                    contentDescription = stringResource(id = R.string.theme)
                )
            }
        }

    }
}


@Composable
fun DailyDiagramsTitle(
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    Box(modifier = modifier) {
        Text(
            modifier = Modifier
                .height(dimensionResource(id = R.dimen.detail_screen_subtitle_height))
                .graphicsLayer {
                    shape = RoundedCornerShape(percent = 50)
                    shadowElevation = 8.dp.toPx()
                    clip = true
                }
                .background(color = MaterialTheme.colorScheme.tertiaryContainer)
                .wrapContentHeight()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .align(Alignment.Center),
            text = "Daily Diagrams",
            fontSize = dimensionResource(id = R.dimen.detail_screen_subtitle_font_size).toSp(density),
            fontFamily = Font(R.font.cmu_typewriter_bold).toFontFamily(),
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

