package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.utility.toDp


@Composable
fun HourlyDiagramSettingsOptions(
    hourlyChartState: HourlyChartState = HourlyChartState(),
    settingsOpen: Boolean,
    pageNumber: Int,
    onUpdateHourlyDiagramSettingOpen: (open: Boolean, pageIndex: Int) -> Unit,
    onUpdateChartWeatherConditionVisibility: (visible: Boolean) -> Unit,
    onUpdateChartDotsOnCurveVisibility: (visible: Boolean) -> Unit,
    onUpdateChartCurveShadowVisibility: (visible: Boolean) -> Unit,
    onUpdateChartSunRiseSetIconsVisibility: (visible: Boolean) -> Unit,
    onUpdateChartGrids: (grids: ChartGrids) -> Unit,
    onUpdateChartTheme: (theme: ChartTheme) -> Unit
) {
    val density = LocalDensity.current
    var gridMenuExpanded by remember { mutableStateOf(false) }
    var themeMenuExpanded by remember { mutableStateOf(false) }

    var menu1Position by remember { mutableStateOf(Offset.Zero) }
    var gridItemPosition by remember { mutableStateOf(Offset.Zero) }
    var themeItemPosition by remember { mutableStateOf(Offset.Zero) }

    var menu1Size by remember { mutableStateOf(IntSize.Zero) }
    var themeMenuSize by remember { mutableStateOf(IntSize.Zero) }
    var gridMenuSize by remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.End
    ) {
        IconButton(
            onClick = {
                onUpdateHourlyDiagramSettingOpen(!settingsOpen, pageNumber)
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.settings_24px),
                contentDescription = stringResource(id = R.string.settings)
            )
        }
        Box(
            modifier = Modifier,
        ) {
            DropdownMenu(
                modifier = Modifier
                    .onGloballyPositioned {
                        menu1Position = it.positionInParent()
                        menu1Size = it.size
                    }
                    .widthIn(min = 240.dp)
                    .padding(horizontal = 4.dp),
                expanded = settingsOpen,
                onDismissRequest = {
                    onUpdateHourlyDiagramSettingOpen(false, pageNumber)
                },
                shape = MenuDefaults.standaloneGroupShape,
                properties = PopupProperties()
            ) {
                DropdownMenuItem(
                    modifier = Modifier.clip(
                        MenuDefaults.leadingItemShape
                    ),
                    text = {
                        Text(
                            text = stringResource(
                                id = if (hourlyChartState.dotsOnCurveVisible) R.string.hide_data_points
                                else R.string.show_data_points
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.curve_with_dot),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onUpdateChartDotsOnCurveVisibility(!hourlyChartState.dotsOnCurveVisible)
                    }
                )
                DropdownMenuItem(
                    modifier = Modifier.clip(
                        MenuDefaults.middleItemShape
                    ),
                    text = {
                        Text(
                            text = stringResource(
                                id = if (hourlyChartState.curveShadowVisible) R.string.hide_curve_shadow
                                else R.string.show_curve_shadow
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.shadow_on),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onUpdateChartCurveShadowVisibility(!hourlyChartState.curveShadowVisible)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(8.dp))
                DropdownMenuItem(
                    modifier = Modifier.clip(
                        MenuDefaults.middleItemShape
                    ),
                    text = {
                        Text(
                            text = stringResource(
                                id = if (hourlyChartState.weatherConditionIconsVisible) R.string.hide_weather_condition
                                else R.string.show_weather_condition
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.partly_cloudy_day_24px),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onUpdateChartWeatherConditionVisibility(!hourlyChartState.weatherConditionIconsVisible)
                    }
                )
                DropdownMenuItem(
                    modifier = Modifier.clip(
                        MenuDefaults.middleItemShape
                    ),
                    text = {
                        Text(
                            text = stringResource(
                                id = if (hourlyChartState.sunRiseSetIconsVisible) R.string.hide_sun_rise_set
                                else R.string.show_sun_rise_set
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.sun_rise_set),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onUpdateChartSunRiseSetIconsVisibility(!hourlyChartState.sunRiseSetIconsVisible)
                    }
                )
                DropdownMenuItem(
                    modifier = Modifier
                        .onGloballyPositioned {
                            gridItemPosition = it.positionInParent()
                        }
                        .clip(
                            MenuDefaults.middleItemShape
                        ),
                    text = {
                        Text(
                            text = stringResource(id = R.string.grids),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.grid_show),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.rotate(90f),
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        gridMenuExpanded = true
                    }
                )
                DropdownMenuItem(
                    modifier = Modifier
                        .onGloballyPositioned {
                            themeItemPosition = it.positionInParent()
                        }
                        .clip(
                            MenuDefaults.trailingItemShape
                        ),
                    text = {
                        Text(
                            text = stringResource(id = R.string.theme),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.theme),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.rotate(90f),
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        themeMenuExpanded = true
                    }
                )
            }
            DropdownMenu(
                modifier = Modifier
                    .onSizeChanged {
                        gridMenuSize = it
                    },
                expanded = gridMenuExpanded,
                onDismissRequest = {
                    themeMenuExpanded = false
                    gridMenuExpanded = false
                },
                offset = DpOffset(
                    x = (menu1Position.x - menu1Size.width - gridMenuSize.width).toDp(density),
                    y = (menu1Position.y + gridItemPosition.y).toDp(density)
                ),
                shape = MenuDefaults.standaloneGroupShape
            ) {
                ChartGrids.entries.forEachIndexed { itemIndex, grid ->
                    MySelectableDropdownMenuItem(
                        modifier = Modifier,
                        selected = hourlyChartState.chartGrid == grid,
                        text = stringResource(id = grid.nameId),
                        itemIndex = itemIndex,
                        totalItemCount = ChartGrids.entries.size,
                        onClick = {
                            onUpdateChartGrids(grid)
                        }
                    )
                }
            }
            DropdownMenu(
                modifier = Modifier
                    .onGloballyPositioned {
                        themeMenuSize = it.size
                    },
                expanded = themeMenuExpanded,
                onDismissRequest = {
                    themeMenuExpanded = false
                    gridMenuExpanded = false
                },
                offset = DpOffset(
                    x = (menu1Position.x - menu1Size.width - themeMenuSize.width).toDp(density),
                    y = (menu1Position.y + themeItemPosition.y).toDp(density)
                ),
                shape = MenuDefaults.standaloneGroupShape
            ) {
                ChartTheme.entries.forEachIndexed { itemIndex, theme ->
                    MySelectableDropdownMenuItem(
                        modifier = Modifier,
                        selected = hourlyChartState.chartTheme == theme,
                        text = stringResource(id = theme.nameId),
                        itemIndex = itemIndex,
                        totalItemCount = ChartTheme.entries.size,
                        onClick = {
                            onUpdateChartTheme(theme)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MySelectableDropdownMenuItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    text: String,
    itemIndex: Int,
    totalItemCount: Int,
    onClick: () -> Unit,
) {
    val itemShape = MenuDefaults.itemShape(
        index = itemIndex,
        count = totalItemCount
    )
    SelectableDropdownMenuItem(
        modifier = modifier
//            .background(
//                color = if (selected) MaterialTheme.colorScheme.tertiaryContainer else Transparent,
//                shape = if (selected) itemShape.selectedShape else itemShape.shape
//            )
        ,
        selected = selected,
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        /*leadingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null
                )
            }
        },*/
        shapes = itemShape,
        colors = MenuDefaults.selectableItemColors(
            textColor = MaterialTheme.colorScheme.onSurface,
            containerColor = Transparent,
            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        onClick = onClick
    )
}