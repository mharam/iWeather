package com.takaapoo.weatherer.ui.screens.detail.hourly_diagram

/*
@Composable
fun BoxScope.HourlyDiagramTopBar(
    modifier: Modifier = Modifier,
    endPadding: Dp,
    hourlyDiagramSettingOpen: Boolean,
    dotsOnCurveVisible: Boolean,
    curveShadowVisible: Boolean,
    weatherConditionIconsVisible: Boolean,
    sunRiseSetIconsVisible: Boolean,
    chartGrid: ChartGrids,
    onUpdateHourlyDiagramSettingOpen: (open: Boolean) -> Unit,
    onUpdateChartWeatherConditionVisibility: (visible: Boolean) -> Unit,
    onUpdateChartDotsOnCurveVisibility: (visible: Boolean) -> Unit,
    onUpdateChartCurveShadowVisibility: (visible: Boolean) -> Unit,
    onUpdateChartSunRiseSetIconsVisibility: (visible: Boolean) -> Unit,
    onUpdateChartGrids: (grids: ChartGrids) -> Unit,
    onUpdateChooseDiagramThemeDialogVisibility: () -> Unit,
    onUpdateHourlyDiagramSettingRectangle: (rect: Rect) -> Unit
) {
    var settingRectangle by remember {
        mutableStateOf(Rect(0f, 0f, 0f, 0f))
    }
    LaunchedEffect(settingRectangle) {
        onUpdateHourlyDiagramSettingRectangle(settingRectangle)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .padding(
                horizontal = 8.dp,
                vertical = dimensionResource(id = R.dimen.detail_screen_subtitle_vertical_padding)
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Spacer(modifier = Modifier.width(48.dp))
        HourlyDiagramsTitle(
            modifier = Modifier.height(48.dp)
        )
        Column(
            modifier = Modifier
                .padding(end = endPadding)
                .width(48.dp)
                .height(if (hourlyDiagramSettingOpen) 336.dp else 48.dp)
                .graphicsLayer {
                    shape = RoundedCornerShape(percent = 50)
                    shadowElevation = 4.dp.toPx()
                    clip = true
                }
                .zIndex(2f)
                .onGloballyPositioned {
                    val displace = it.parentLayoutCoordinates!!.parentLayoutCoordinates!!.positionInParent().y
                    settingRectangle = it.boundsInParent().copy(
                        top = it.boundsInParent().top + displace,
                        bottom = it.boundsInParent().bottom + displace
                    )
                }
                .background(color = MaterialTheme.customColorScheme.searchbarSurface),
            verticalArrangement = Arrangement.Top
        ) {
            IconToggleButton(
                checked = hourlyDiagramSettingOpen,
                onCheckedChange = {
                    onUpdateHourlyDiagramSettingOpen(it)
                },
//                enabled = barAlpha > 0.9f
            ) {
                Icon(
                    imageVector = if (hourlyDiagramSettingOpen) Icons.Filled.Settings
                    else Icons.Outlined.Settings,
                    contentDescription = stringResource(id = R.string.settings)
                )
            }
            IconToggleButton(
                checked = dotsOnCurveVisible,
                onCheckedChange = {
                    onUpdateChartDotsOnCurveVisibility(it)
                }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (dotsOnCurveVisible) R.drawable.curve_with_dot
                        else R.drawable.curve_without_dot
                    ),
                    contentDescription = stringResource(id = R.string.show_data_points)
                )
            }
            IconToggleButton(
                checked = curveShadowVisible,
                onCheckedChange = {
                    onUpdateChartCurveShadowVisibility(it)
                }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (curveShadowVisible) R.drawable.shadow_on
                        else R.drawable.shadow_off
                    ),
                    contentDescription = stringResource(id = R.string.show_curve_shadow)
                )
            }
            IconToggleButton(
                checked = weatherConditionIconsVisible,
                onCheckedChange = {
                    onUpdateChartWeatherConditionVisibility(it)
                }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (weatherConditionIconsVisible) R.drawable.partly_cloudy_day_fill_24px
                        else R.drawable.partly_cloudy_day_24px
                    ),
                    contentDescription = stringResource(id = R.string.show_weather_condition)
                )
            }
            IconToggleButton(
                checked = sunRiseSetIconsVisible,
                onCheckedChange = {
                    onUpdateChartSunRiseSetIconsVisibility(it)
                }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (sunRiseSetIconsVisible) R.drawable.sun_rise_set_fill
                        else R.drawable.sun_rise_set
                    ),
                    contentDescription = stringResource(id = R.string.show_sun_rise_set)
                )
            }
            IconButton(
                onClick = {
                    onUpdateChartGrids(
                        when (chartGrid){
                            ChartGrids.All -> ChartGrids.MAIN
                            ChartGrids.MAIN -> ChartGrids.NON
                            ChartGrids.NON -> ChartGrids.All
                        }
                    )
                }
            ) {
                Icon(
                    painter = painterResource(id = when (chartGrid){
                        ChartGrids.All -> R.drawable.grid_show
                        ChartGrids.MAIN -> R.drawable.grid_main_show
                        ChartGrids.NON -> R.drawable.grid_not_show
                    }),
                    tint = when (chartGrid){
                        ChartGrids.All -> MaterialTheme.colorScheme.primary
                        ChartGrids.MAIN -> LocalContentColor.current
                        ChartGrids.NON -> MaterialTheme.customColorScheme.noGridIcon
                    },
                    contentDescription = stringResource(id = R.string.grids)
                )
            }
            IconButton(
                onClick = {
                    onUpdateChooseDiagramThemeDialogVisibility()
                    onUpdateHourlyDiagramSettingOpen(false)
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
*/


/*
@Composable
fun HourlyDiagramsTitle(
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    Box(modifier = modifier) {
        Text(
            modifier = Modifier
                .height(dimensionResource(id = R.dimen.detail_screen_subtitle_height))
                .graphicsLayer {
                    shape = RoundedCornerShape(percent = 50)
                    shadowElevation = 4.dp.toPx()
                    clip = true
                }
                .background(color = MaterialTheme.colorScheme.tertiaryContainer)
                .wrapContentHeight()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .align(Alignment.Center),
            text = "Hourly Diagrams",
            fontSize = dimensionResource(id = R.dimen.detail_screen_subtitle_font_size).toSp(density),
            fontFamily = timeFontFamily,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}
*/

