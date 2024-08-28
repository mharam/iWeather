package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.ChartState
import com.takaapoo.weatherer.ui.theme.customColorScheme


@Composable
fun RowScope.HourlyDiagramSettingsOptions(
    chartState: ChartState = ChartState(),
    settingsOpen: Boolean,
    pageNumber: Int,
    onUpdateHourlyDiagramSettingOpen: (open: Boolean, pageIndex: Int) -> Unit,
    onUpdateChartWeatherConditionVisibility: (visible: Boolean) -> Unit,
    onUpdateChartDotsOnCurveVisibility: (visible: Boolean) -> Unit,
    onUpdateChartCurveShadowVisibility: (visible: Boolean) -> Unit,
    onUpdateChartSunRiseSetIconsVisibility: (visible: Boolean) -> Unit,
    onUpdateChartGrids: (grids: ChartGrids) -> Unit,
//    onUpdateChooseDiagramThemeDialogVisibility: () -> Unit,
) {
    IconToggleButton(
        checked = settingsOpen,
        onCheckedChange = {
            onUpdateHourlyDiagramSettingOpen(it, pageNumber)
        }
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(id = R.string.settings)
        )
    }
    DropdownMenu(
        expanded = settingsOpen,
        onDismissRequest = {
            onUpdateHourlyDiagramSettingOpen(!settingsOpen, pageNumber)
        },
    ) {
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.show_curve_dots))
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (chartState.dotsOnCurveVisible) R.drawable.curve_with_dot
                        else R.drawable.curve_without_dot
                    ),
                    contentDescription = stringResource(id = R.string.show_curve_dots)
                )
            },
            onClick = {
                onUpdateChartDotsOnCurveVisibility(!chartState.dotsOnCurveVisible)
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.show_curve_shadow))
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (chartState.curveShadowVisible) R.drawable.shadow_remove
                        else R.drawable.shadow_add
                    ),
                    contentDescription = stringResource(id = R.string.show_curve_shadow)
                )
            },
            onClick = {
                onUpdateChartCurveShadowVisibility(!chartState.curveShadowVisible)
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.show_weather_condition_icon))
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (chartState.weatherConditionIconsVisible) R.drawable.partly_cloudy_day_fill_24px
                        else R.drawable.partly_cloudy_day_24px
                    ),
                    contentDescription = stringResource(id = R.string.show_weather_condition_icon)
                )
            },
            onClick = {
                onUpdateChartWeatherConditionVisibility(!chartState.weatherConditionIconsVisible)
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.show_sun_rise_set_icon))
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (chartState.sunRiseSetIconsVisible) R.drawable.sun_rise_set_fill
                        else R.drawable.sun_rise_set
                    ),
                    contentDescription = stringResource(id = R.string.show_sun_rise_set_icon)
                )
            },
            onClick = {
                onUpdateChartSunRiseSetIconsVisibility(!chartState.sunRiseSetIconsVisible)
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.show_hide_grids))
            },
            leadingIcon = {
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
            },
            onClick = {
                onUpdateChartGrids(
                    when (chartState.chartGrid){
                        ChartGrids.All -> ChartGrids.MAIN
                        ChartGrids.MAIN -> ChartGrids.NON
                        ChartGrids.NON -> ChartGrids.All
                    }
                )
            }
        )
    }
}