package com.takaapoo.weatherer.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.domain.model.SettingsState
import com.takaapoo.weatherer.ui.screens.detail.ChooseDiagramThemeDialog
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.HourlyChart
import com.takaapoo.weatherer.ui.screens.settings.dialogs.ChooseDiagramGridDialog
import com.takaapoo.weatherer.ui.screens.settings.dialogs.ChooseTimeAxisDialog
import com.takaapoo.weatherer.ui.theme.Gray30
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.utility.lighter
import com.takaapoo.weatherer.utility.mostContrastColor
import com.takaapoo.weatherer.utility.toDp
import com.takaapoo.weatherer.utility.toPx
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourlyDiagramCurvesScreen(
    modifier: Modifier = Modifier,
    appSettings: AppSettings,
    singlePane: Boolean,
    hourlyChartData: ImmutableList<HourlyChartDto>,
    hourlyChartState: HourlyChartState,
    settingsState: SettingsState,
    initialXAxisRange: ClosedFloatingPointRange<Float>,

//    onUpdateInitialXAxisBounds: (width: Dp) -> Unit,
    onAddYAxis: (start: Float, end: Float, diagramHeight: Float,
                 textMeasurer: TextMeasurer, axisIndex: Int?, curveAnimatorInitialValue: Float) -> Unit,
    onMoveHourlyDiagramAxis: (Offset) -> Unit,
    onScaleHourlyDiagramAxis: (Offset, Float, Float) -> Unit,
    onCalculateHorizontalBarSeparation: (Float, Float, Float, TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation: (Float, Float, Float, TextMeasurer) -> Pair<Int, Float>,
    onUpdateHourlyChartDotsOnCurveVisibility: (visible: Boolean) -> Unit,
    onUpdateHourlyChartCurveShadowVisibility: (visible: Boolean) -> Unit,
    onUpdateHourlyChartWeatherIconVisibility: (visible: Boolean) -> Unit,
    onUpdateHourlyChartSunRiseSetIconsVisibility: (visible: Boolean) -> Unit,
    onUpdateTimeAxisDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateThemeDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateGridDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateCurvesColor: (curveIndex: Int, color: Int) -> Unit,
    onUpdateWholeCurvesColor: (colors: List<Color>) -> Unit,
    onSaveCurvesColor: (curveIndex: Int, color: Color) -> Unit,
    onUpdateCurvesSmoothness: (quantity: HourlyWeatherQuantity, value: Float) -> Unit,
    onSaveCurvesSmoothness: (quantity: HourlyWeatherQuantity, value: Float) -> Unit,
    onAddRemoveQuantity: (quantity: HourlyWeatherQuantity, addRemove: Boolean) -> Unit,
    onUpdateTimeAxis: (time: HourlyDiagramTime) -> Unit,
    onUpdateChartTheme: (chartTheme: ChartTheme) -> Unit,
    onUpdateChartGrid: (grid: ChartGrids) -> Unit,

    onNavigateUp: () -> Unit
) {
    val settingScrollState = rememberScrollState()
    val colorSheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    var colorBottomSheetVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    var maxHeight by remember { mutableStateOf(0.dp) }
    val cardPartHeight = dimensionResource(R.dimen.diagram_height) + dimensionResource(R.dimen.home_card_spacing)
    val tooltipState = rememberTooltipState(isPersistent = true)
    val blurRadius by animateDpAsState(
        targetValue = if (tooltipState.isVisible) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
    )

    when {
        settingsState.timeAxisDialogVisibility -> {
            ChooseTimeAxisDialog(
                time = appSettings.hourlyDiagramTime,
                onDismiss = {
                    onUpdateTimeAxisDialogVisibility(false)
                },
                onConfirmation = {
                    onUpdateTimeAxis(it)
                }
            )
        }
        settingsState.diagramThemeDialogVisibility -> {
            ChooseDiagramThemeDialog(
                chartTheme = hourlyChartState.chartTheme,
                onConfirmation = {
                    onUpdateThemeDialogVisibility(false)
                },
                onSelectTheme = {
                    onUpdateChartTheme(it)
                }
            )
        }
        settingsState.diagramGridDialogVisibility -> {
            ChooseDiagramGridDialog(
                chartGrid = hourlyChartState.chartGrid,
                onDismiss = {
                    onUpdateGridDialogVisibility(false)
                },
                onConfirmation = {
                    onUpdateChartGrid(it)
                },
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Hourly diagram",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    if (singlePane) {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val density = LocalDensity.current
        Column(
            modifier = Modifier.blur(radius = blurRadius)
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                    top = paddingValues.calculateTopPadding()
                )
                .onSizeChanged {
                    maxHeight = it.height.toDp(density)
                }
                .verticalScroll(scrollState)
                .nestedScroll(SettingsScrollConnection(scrollState, coroutineScope))
        ) {
            val diagramHorPadding = dimensionResource(id = R.dimen.diagram_frame_hor_padding).toPx(density)
            val diagramVertPadding = dimensionResource(id = R.dimen.diagram_frame_ver_padding).toPx(density)
            val topBoxShape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 0.dp,
                bottomStart = 32.dp,
                bottomEnd = 32.dp
            )
            Box(
                modifier = Modifier
//                    .onSizeChanged {
//                        onUpdateInitialXAxisBounds(it.width.toDp(density))
//                    }
                    .shadow(
                        elevation = if (settingScrollState.canScrollBackward) 4.dp else 0.dp,
                        shape = topBoxShape
                    )
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = topBoxShape
                    )
                    .height(dimensionResource(R.dimen.diagram_height))
                    .padding(
                        start = dimensionResource(id = R.dimen.whole_diagram_hor_padding),
                        end = dimensionResource(id = R.dimen.whole_diagram_hor_padding),
                        bottom = dimensionResource(R.dimen.home_card_spacing)
                    )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    HourlyChart(
                        modifier = Modifier.fillMaxSize(),
                        diagramHorPadding = diagramHorPadding,
                        diagramVertPadding = diagramVertPadding,
                        hourlyChartData = hourlyChartData,
                        curveAnimator = hourlyChartState.curveAnimator,
                        chartQuantities = hourlyChartState.chartQuantities,
                        settingsUpdated = hourlyChartState.settingsUpdated,
                        xAxisAnimationTrigger = hourlyChartState.xAxisAnimationTrigger,
                        xAxisStart = hourlyChartState.xAxisStart,
                        xAxisEnd = hourlyChartState.xAxisEnd,
                        yAxesStarts = hourlyChartState.yAxesStarts,
                        yAxesEnds = hourlyChartState.yAxesEnds,
                        chartGrid = hourlyChartState.chartGrid,
                        chartTheme = hourlyChartState.chartTheme,
                        dataLoadingStatus = hourlyChartState.dataLoadingStatus,
                        curvesSmoothness = hourlyChartState.curvesSmoothness,
                        curvesColor = hourlyChartState.curvesColor,
                        dotsOnCurveVisible = hourlyChartState.dotsOnCurveVisible,
                        curveShadowVisible = hourlyChartState.curveShadowVisible,
                        sliderThumbPosition = hourlyChartState.sliderThumbPosition,
                        sunRiseSetIconsVisible = hourlyChartState.sunRiseSetIconsVisible,
                        weatherConditionIconsVisible = hourlyChartState.weatherConditionIconsVisible,
                        appSettings = appSettings,
                        initialXAxisRange = initialXAxisRange,
                        onAddYAxis = onAddYAxis,
                        onMoveAxis = onMoveHourlyDiagramAxis,
                        onScaleAxis = onScaleHourlyDiagramAxis,
//                    onScaleBack = onScaleBack,
                        onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                        onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation,
                        onUpdateCurveValueAtIndicator = { _, _ -> },
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth().height(maxHeight)
                    .verticalScroll(settingScrollState)
            ) {
                SettingsGroupTitle(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Diagram"
                )
                SettingsRow1(
                    modifier = Modifier.fillMaxWidth(),
                    icon = R.drawable.partly_cloudy_day_24px,
                    title = "Weather condition",
                    subTitle = if (hourlyChartState.weatherConditionIconsVisible) "Visible" else "Invisible",
                    onClick = {
                        onUpdateHourlyChartWeatherIconVisibility(!hourlyChartState.weatherConditionIconsVisible)
                    },
                    content = {
                        Switch(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            checked = hourlyChartState.weatherConditionIconsVisible,
                            onCheckedChange = onUpdateHourlyChartWeatherIconVisibility
                        )
                    }
                )
                SettingsRow1(
                    modifier = Modifier.fillMaxWidth(),
                    icon = R.drawable.sun_rise_set,
                    title = "Sun rise/set",
                    subTitle = if (hourlyChartState.sunRiseSetIconsVisible) "Visible" else "Invisible",
                    onClick = {
                        onUpdateHourlyChartSunRiseSetIconsVisibility(!hourlyChartState.sunRiseSetIconsVisible)
                    },
                    content = {
                        Switch(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            checked = hourlyChartState.sunRiseSetIconsVisible,
                            onCheckedChange = onUpdateHourlyChartSunRiseSetIconsVisibility
                        )
                    }
                )
                SettingsRow1(
                    modifier = Modifier.fillMaxWidth(),
                    icon = R.drawable.clock_icon,
                    title = "Time axis clock",
                    subTitle = stringResource(appSettings.hourlyDiagramTime.title),
                    onClick = { onUpdateTimeAxisDialogVisibility(true) },
                    content = {}
                )
                SettingsRow1(
                    modifier = Modifier.fillMaxWidth(),
                    icon = R.drawable.theme,
                    title = stringResource(R.string.diagram_theme),
                    subTitle = stringResource(hourlyChartState.chartTheme.nameId),
                    onClick = { onUpdateThemeDialogVisibility(true) },
                    content = {}
                )
                SettingsRow1(
                    modifier = Modifier.fillMaxWidth(),
                    icon = R.drawable.grid_show,
                    title = stringResource(R.string.diagram_grid),
                    subTitle = stringResource(hourlyChartState.chartGrid.nameId),
                    onClick = { onUpdateGridDialogVisibility(true) },
                    content = {}
                )

                SettingsGroupTitle(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Curves"
                )
                SettingsRow1(
                    modifier = Modifier.fillMaxWidth(),
                    icon = R.drawable.curve_with_dot,
                    title = "Dots",
                    subTitle = if (hourlyChartState.dotsOnCurveVisible) "Visible" else "Invisible",
                    onClick = {
                        onUpdateHourlyChartDotsOnCurveVisibility(!hourlyChartState.dotsOnCurveVisible)
                    },
                    content = {
                        Switch(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            checked = hourlyChartState.dotsOnCurveVisible,
                            onCheckedChange = onUpdateHourlyChartDotsOnCurveVisibility
                        )
                    }
                )
                SettingsRow1(
                    modifier = Modifier.fillMaxWidth(),
                    icon = R.drawable.shadow_on,
                    title = "Shadow",
                    subTitle = if (hourlyChartState.curveShadowVisible) "Visible" else "Invisible",
                    onClick = {
                        onUpdateHourlyChartCurveShadowVisibility(!hourlyChartState.curveShadowVisible)
                    },
                    content = {
                        Switch(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            checked = hourlyChartState.curveShadowVisible,
                            onCheckedChange = onUpdateHourlyChartCurveShadowVisibility
                        )
                    }
                )
                SettingsRow3(
                    modifier = Modifier.fillMaxWidth(),
                    icon = R.drawable.colorize_24px,
                    title = "Color",
                    onClick = {
                        colorBottomSheetVisible = true
                    },
                    subContent = {
                        Box{
                            val circleSize = 24.dp
                            appSettings.curvesColor.forEachIndexed { index, color ->
                                Box(modifier = Modifier
                                    .size(circleSize)
                                    .align(Alignment.TopStart)
                                    .offset(x = circleSize * index.coerceAtLeast(0) - 8.dp * index)
                                    .background(color = Color(color), shape = CircleShape)
                                )
                            }
                        }
                    }
                )

                SettingsGroupTitle(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Curves smoothness",
                    icon = {
                        TooltipBox(
                            modifier = Modifier,
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                positioning = TooltipAnchorPosition.Above,
                            ),
                            tooltip = {
                                RichTooltip(
//                                    title = {
//                                        Text(
//                                            text = stringResource(R.string.curve_smoothness_tooltip_title),
//                                            fontSize = 14.sp,
//                                            fontWeight = FontWeight(500),
//                                            fontFamily = FontFamily.Default,
//                                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                                        )
//                                            },
                                    action = {
                                        TextButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    tooltipState.dismiss()
                                                }
                                            }
                                        ) {
                                            Text(text = "Ok", fontFamily = FontFamily.Default)
                                        }
                                    },
//                                    caretSize = DpSize(32.dp, 16.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.curve_smoothness_tooltip_text),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight(400),
                                        fontFamily = FontFamily.Default,
//                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            state = tooltipState
                        ) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        tooltipState.show()
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.info_24px),
                                    contentDescription = "information",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                )


                appSettings.curvesSmoothness.keys.forEach { quantity ->
                    val interactionSource = remember { MutableInteractionSource() }
                    var isThumbPressed by remember { mutableStateOf(false) }
                    val quantityColor = hourlyChartState.curvesColor
                        .getOrNull(hourlyChartState.chartQuantities.indexOf(quantity))?.let { Color(it) }


                    SettingsRow3(
                        modifier = Modifier.fillMaxWidth(),
                        icon = quantity.iconId,
                        title = quantity.nameId?.let { stringResource(it) },
                        titleAnnotated = quantity.title,
                        color = quantityColor ?: Color.Unspecified,
                        onClick = {
//                            onAddRemoveQuantity(
//                                quantity,
//                                !hourlyChartState.chartQuantities.contains(quantity)
//                            )
                        },
                        subContent = {
                            Slider(
                                modifier = Modifier.fillMaxWidth(),
                                value = hourlyChartState.curvesSmoothness[quantity]!!,
                                onValueChange = {
//                                    if (!isThumbPressed && isDragging)
                                        onUpdateCurvesSmoothness(quantity, it)
                                },
                                onValueChangeFinished = {
                                    onSaveCurvesSmoothness(
                                        quantity,
                                        hourlyChartState.curvesSmoothness[quantity]!!
                                    )
                                },
                                interactionSource = interactionSource,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = quantityColor ?: Color.Unspecified,
                                    inactiveTrackColor = quantityColor?.lighter(0.7f) ?: Color.Unspecified
                                ),
                                thumb = {
                                    Column (
                                        modifier = Modifier.size(0.dp, 44.dp)
                                            .wrapContentSize(
                                                align = Alignment.BottomCenter,
                                                unbounded = true
                                            )
                                            .pointerInput(Unit) {
                                                awaitEachGesture {
                                                    do {
                                                        val event = awaitPointerEvent()
                                                        val press = event.changes.firstOrNull()
                                                        isThumbPressed = press?.pressed == true
                                                        if (press?.changedToUp() == true) {
                                                            isThumbPressed = false
                                                        }
                                                    } while (event.changes.any { pointerInputChange->
                                                        pointerInputChange.pressed })
                                                }
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ){
                                        AnimatedVisibility(
                                            visible = isThumbPressed || it.isDragging,
                                            enter = scaleIn(transformOrigin = TransformOrigin(0.5f, 1f)),
                                            exit = scaleOut(transformOrigin = TransformOrigin(0.5f, 1f))
                                        ) {
                                            Text(
                                                modifier = Modifier
                                                    .background(
                                                        color = MaterialTheme.colorScheme.inverseSurface,
                                                        shape = RoundedCornerShape(50)
                                                    )
                                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                                text = "${(hourlyChartState.curvesSmoothness[quantity]!!*100).roundToInt()} %",
                                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                                fontWeight = FontWeight(500),
                                                fontFamily = FontFamily.Default,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        SliderDefaults.Thumb(
                                            interactionSource = interactionSource,
                                            colors = SliderDefaults.colors(
                                                thumbColor = quantityColor ?: Color.Unspecified,
                                            )
                                        )
                                    }
                                }
                            )
                        },
                        content = {
                            Checkbox(
                                modifier = Modifier.size(48.dp),
                                checked = hourlyChartState.chartQuantities.contains(quantity),
                                onCheckedChange = {
                                    onAddRemoveQuantity(quantity, it)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = quantityColor ?: Color.Unspecified
                                )
                            )
                        }
                    )
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .layout { measurable, constraints ->
                            // Reading scrollState.value here happens during the layout phase, not composition.
                            val scrollOffset = scrollState.value
                            val heightPx = (paddingValues.calculateBottomPadding() + cardPartHeight + 16.dp).toPx() -
                                    scrollOffset
                            val height = heightPx.roundToInt().coerceAtLeast(0)

                            val placeable = measurable.measure(
                                constraints.copy(minHeight = height, maxHeight = height)
                            )
                            layout(placeable.width, placeable.height) {
                                placeable.placeRelative(0, 0)
                            }
                        }
//                        .height(
//                            paddingValues.calculateBottomPadding() + cardPartHeight + 16.dp -
//                                    scrollState.value.toDp(density)
//                        )
                )
            }
        }

        if (colorBottomSheetVisible) {
            ModalBottomSheet(
                sheetState = colorSheetState,
                onDismissRequest = {
                    colorBottomSheetVisible = false
                }
            ) {
                var selectedButton by rememberSaveable { mutableIntStateOf(0) }
                var colorInitializer by rememberSaveable { mutableIntStateOf(100) }
                var selectedButtonColor by rememberSaveable(selectedButton) {
                    mutableIntStateOf(appSettings.curvesColor[selectedButton])
                }
                var initialColor by remember(selectedButton) {
                    mutableStateOf(Color(appSettings.curvesColor[selectedButton]))
                }
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 8.dp)
                ) {
                    val items = listOf("C1", "C2", "C3", "C4")
                    items.forEachIndexed { index, curve ->
                        val shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size)
                        SegmentedButton(
                            shape = shape,
                            onClick = {
                                if (selectedButton != index)
                                    colorInitializer = index
                                selectedButton = index
                                      },
                            colors = SegmentedButtonDefaults.colors().copy(
                                activeContainerColor = Color(selectedButtonColor),
                                inactiveContainerColor = Color(appSettings.curvesColor[index]),
                                inactiveBorderColor = Transparent,
                                activeContentColor = Color(selectedButtonColor).mostContrastColor(),
                                inactiveContentColor = Color(appSettings.curvesColor[index]).mostContrastColor(),
                            ),
                            selected = selectedButton == index
                        ) {
                            Text(
                                text = curve,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.W500,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    PickColor(
                        modifier = Modifier.height(288.dp).fillMaxWidth(),
                        initialColor = initialColor,
                        initializeColor = colorInitializer,
                        onModifyColor = {
                            val colorInt = it.toArgb()
                            onUpdateCurvesColor(selectedButton, colorInt)
                            selectedButtonColor = colorInt
                        },
                        onSaveCurvesColor = {
                            onSaveCurvesColor(selectedButton, it)
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val listState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
//                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(top = 4.dp, start = 8.dp, end = 8.dp),
                        flingBehavior = rememberSnapFlingBehavior(
                            lazyListState = listState,
                            snapPosition = SnapPosition.Start
                        ),
                        state = listState
                    ) {
                        items(
                            items = predefinedHourlyCurveColor,
                            key = { item -> item.setId }
                        ) { curveColorSet ->
                            ColorSetRow(
                                modifier = Modifier
                                    .height(48.dp)
                                    .widthIn(max = 240.dp),
                                colorSet = curveColorSet,
                                onUpdateWholeCurvesColor = { colors ->
                                    onUpdateWholeCurvesColor(colors)
                                    initialColor = colors[selectedButton]
                                    colorInitializer++
                                }
                            )
                        }
                        item {
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .shadow(elevation = 1.dp, shape = SlotShadowShape())
                    ){
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(color = Gray30)
                            .align(Alignment.TopCenter)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        BottomSheetDefaults.ContainerColor.copy(alpha = 0f),
                                        BottomSheetDefaults.ContainerColor
                                    )
                                )
                            )
                    )
                }
            }
        }
    }

}

@Composable
fun ColorSetRow(
    modifier: Modifier = Modifier,
    colorSet: HourlyCurveColor,
    onUpdateWholeCurvesColor: (colors: List<Color>) -> Unit
) {
    val circleSize = 40.dp
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable {
                onUpdateWholeCurvesColor(colorSet.colors)
            }
            .padding(horizontal = 16.dp)
            .width(circleSize * (colorSet.colors.size - 1) * 2 / 3 + circleSize),
        contentAlignment = Alignment.CenterStart
    ) {
        colorSet.colors.forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .offset(x = circleSize * 2 / 3 * index)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ColorSetRowPreview() {
    WeathererTheme {
        ColorSetRow(
            modifier = Modifier
                .height(48.dp)
                .width(232.dp),
            colorSet = predefinedHourlyCurveColor[5],
            onUpdateWholeCurvesColor = { _ -> }
        )
    }
}