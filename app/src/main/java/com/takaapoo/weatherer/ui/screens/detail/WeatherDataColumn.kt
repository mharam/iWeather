package com.takaapoo.weatherer.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.DailyChart
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.DailyDiagramTopBar
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.DailyQuantityChooserRail
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.HourlyChart
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.HourlyDiagramLegend
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.HourlyDiagramLegendRow
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.HourlyDiagramTopBar
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.QuantityChooserRail
import com.takaapoo.weatherer.ui.theme.DiagramGrid
import com.takaapoo.weatherer.ui.theme.OnDiagramDarkTheme
import com.takaapoo.weatherer.ui.theme.OnDiagramLightTheme
import com.takaapoo.weatherer.ui.theme.Transparent10
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.utility.BorderedText
import com.takaapoo.weatherer.ui.utility.toDp
import com.takaapoo.weatherer.ui.utility.toPx
import com.takaapoo.weatherer.ui.utility.toSp
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow


@Composable
fun WeatherDataColumn(
    endPadding: Dp,
    hourlyChartData: List<HourlyChartDto>,
    dailyChartData: List<LocalDailyWeather>,
    appSettings: AppSettings,
    detailState: DetailState,
    hourlyChartState: HourlyChartState,
    dailyChartState: DailyChartState,
    modifier: Modifier = Modifier,
    spaceAboveTitle: Float,
    bottomPadding: Dp,
    headerHeight: Float,
    cornerRadius: Dp,
    timeAlpha: Float,
    onAddYAxis: (start: Float, end: Float, diagramHeight: Float,
                 textMeasurer: TextMeasurer, axisIndex: Int?, curveAnimatorInitialValue: Float) -> Unit,
    onUpdateDailyYAxis: (quantity: DailyWeatherQuantity) -> Unit,
    onMoveHourlyDiagramAxis: (Offset) -> Unit,
    onScaleHourlyDiagramAxis: (Offset, Float, Float) -> Unit,
    onScaleBack: (Offset) -> Unit,
    undoHourlyDiagramScaleMove: () -> Unit,
    onMoveDailyDiagramAxis: (Offset) -> Unit,
    onScaleDailyDiagramAxis: (Offset, Float, Float) -> Unit,
    onScaleBackDaily: (Offset) -> Unit,
    undoDailyDiagramScaleMove: () -> Unit,
    onCalculateHorizontalBarSeparation:
        (start: Float, end: Float, diagramHeight: Float, textMeasurer: TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation:
        (start: Float, end: Float, diagramWidth: Float, textMeasurer: TextMeasurer) -> Pair<Int, Float>,
    onAddChartQuantity: (quantity: WeatherQuantity) -> Unit,
    onRemoveChartQuantity: (quantity: WeatherQuantity) -> Unit,
    onUpdateDailyChartQuantity: (quantity: DailyWeatherQuantity) -> Unit,
    onUpdateHourlyDiagramSettingOpen: (open: Boolean) -> Unit,
    onUpdateDailyDiagramSettingOpen: (open: Boolean) -> Unit,
    onUpdateChartWeatherConditionVisibility: (visible: Boolean) -> Unit,
    onUpdateDailyChartWeatherConditionVisibility: (visible: Boolean) -> Unit,
    onUpdateChartDotsOnCurveVisibility: (visible: Boolean) -> Unit,
    onUpdateChartCurveShadowVisibility: (visible: Boolean) -> Unit,
    onUpdateChartSunRiseSetIconsVisibility: (visible: Boolean) -> Unit,
    onUpdateChartGrids: (grids: ChartGrids) -> Unit,
    onUpdateChooseDiagramThemeDialogVisibility: () -> Unit,
    onUpdateHourlyDiagramSettingRectangle: (rect: Rect) -> Unit,
    onUpdateDailyDiagramSettingRectangle: (rect: Rect) -> Unit,
    onUpdateHourlyThumbPosition: (movement: Float) -> Unit,
    onUpdateDailyThumbPosition: (movement: Float) -> Unit,
    onUpdateHourlyCurveValueAtIndicator: (curveIndex: Int, value: Float?) -> Unit,
    onUpdateDailyCurveValueAtIndicator: (valueMin: Float, valueMax: Float) -> Unit,
    onNavigateToAQDetailScreen: () -> Unit,
//    sharedTransitionScope: SharedTransitionScope,
//    animatedContentScope: AnimatedContentScope
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scrollState = detailState.scrollState
    val titleShrinkStartPoint = spaceAboveTitle - 0.3f * (context as MainActivity).windowHeight
    var titleWidth by rememberSaveable{ mutableIntStateOf(0) }
    val screenPadding = dimensionResource(R.dimen.detail_screen_padding).toPx(density)
    val titleMaxXMove = (detailScreenWidth - titleWidth)/2f - screenPadding

    var timeBottomSpacerDistanceFromHeader by remember{ mutableFloatStateOf(0f) }
    var hourlyBottomSpacerDistanceFromHeader by remember{ mutableFloatStateOf(Float.MAX_VALUE) }
    val subtitleVerticalSpace = (dimensionResource(id = R.dimen.detail_screen_subtitle_height) +
            dimensionResource(id = R.dimen.detail_screen_subtitle_vertical_padding) * 2).toPx(density)
//    val timeAlpha = when {
//        scrollState.value < spaceAboveTitle -> 1f
//        else -> ((spaceAboveTitle + subtitleVerticalSpace - scrollState.value)/subtitleVerticalSpace)
//            .coerceAtLeast(0f)
//    }

    val sectionSeparation = (dimensionResource(id = R.dimen.detail_screen_section_separation))
        .toPx(density)
    val hourlyHeaderInitialY = spaceAboveTitle + timeBottomSpacerDistanceFromHeader + sectionSeparation
    val hourlyHeaderTop = when {
        scrollState.value < hourlyHeaderInitialY - headerHeight -> hourlyHeaderInitialY - scrollState.value
        else -> headerHeight
    }
    val chooserRailHeight = 48.dp.toPx(density)
    val hourlyHeaderAlpha =
        when {
            scrollState.value < hourlyHeaderInitialY - headerHeight -> 1f
            else -> ((hourlyHeaderInitialY - headerHeight + chooserRailHeight - scrollState.value)/chooserRailHeight)
                .coerceAtLeast(0f)
        }

    val dailyHeaderInitialY = spaceAboveTitle + hourlyBottomSpacerDistanceFromHeader + sectionSeparation
    val dailyHeaderTop = when {
        scrollState.value < dailyHeaderInitialY - headerHeight -> dailyHeaderInitialY - scrollState.value
        else -> headerHeight
    }
    val dailyHeaderAlpha = when {
        scrollState.value < dailyHeaderInitialY - headerHeight -> 1f
        else -> ((dailyHeaderInitialY - headerHeight + chooserRailHeight - scrollState.value) / chooserRailHeight)
            .coerceAtLeast(0f)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(spaceAboveTitle.toDp(density)))
//            val columnShape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
            val surfaceColor = MaterialTheme.customColorScheme.detailScreenSurface.toArgb()
            val borderColor = MaterialTheme.colorScheme.primaryContainer
//            val currentConditionScroll = spaceAboveTitle - scrollState.value
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val width = size.width
                        val height = size.height
                        val cornerRad = CornerRadius(cornerRadius.toPx())
                        val backgroundPath = Path().apply {
                            this.addRoundRect(
                                roundRect = RoundRect(
                                    left = 0f, top = 0f, right = width, bottom = height,
                                    topLeftCornerRadius = cornerRad, topRightCornerRadius = cornerRad
                                )
                            )
                        }
                        drawPath(
                            path = backgroundPath,
                            color = barColor(
                                scroll = scrollState.value,
                                start = titleShrinkStartPoint,
                                end = spaceAboveTitle,
                                color1 = Transparent10.toArgb(),
                                color2 = surfaceColor
                            )
                        )
                        drawPath(
                            path = backgroundPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(borderColor, Color.Transparent),
                                startY = 0f,
                                endY = 300f
                            ),
                            style = Stroke(width = 1.dp.toPx()),
                        )
                    }
//                    .background(
//                        color = barColor(
//                            scroll = scrollState.value,
//                            start = titleShrinkStartPoint,
//                            end = spaceAboveTitle,
//                            color1 = Transparent10.toArgb(),
//                            color2 = MaterialTheme.customColorScheme.detailScreenSurface.toArgb()
//                        ),
//                        shape = columnShape
//                    )
//                    .border(
//                        border = BorderStroke(
//                            width = 1.dp,
//                            brush = Brush.verticalGradient(
//                                colors = listOf(
//                                    MaterialTheme.colorScheme.primaryContainer,
//                                    Color.Transparent
//                                ),
//                                startY = 0f,
//                                endY = 300f
//                            )
//                        ),
//                        shape = columnShape
//                    )
//                    .clip(columnShape)
                    .padding(bottom = bottomPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val width = size.width
                            val height = size.height
                            val cornerRad = CornerRadius(cornerRadius.toPx())
                            val backgroundPath = Path().apply {
                                this.addRoundRect(
                                    roundRect = RoundRect(
                                        left = 0f,
                                        top = 0f,
                                        right = width,
                                        bottom = height,
                                        topLeftCornerRadius = cornerRad,
                                        topRightCornerRadius = cornerRad
                                    )
                                )
                            }
                            drawPath(
                                path = backgroundPath,
                                brush = Brush.verticalGradient(
                                    0f to borderColor,
                                    1f to barColor(
                                        scroll = scrollState.value,
                                        start = titleShrinkStartPoint,
                                        end = spaceAboveTitle,
                                        color1 = 0x00FFFFFF,
                                        color2 = borderColor.toArgb()
                                    ),
//                            1f to Color.Transparent,
//                            startY = 0.1f * headerHeight,
                                    endY = movementFraction(
                                        scroll = scrollState.value,
                                        start = titleShrinkStartPoint,
                                        end = spaceAboveTitle
                                    ) * headerHeight
                                )
                            )
                        }
                        /*.background(
                            brush = Brush.verticalGradient(
                                0f to MaterialTheme.colorScheme.primaryContainer,
                                1f to barColor(
                                    scroll = scrollState.value,
                                    start = titleShrinkStartPoint,
                                    end = spaceAboveTitle,
                                    color1 = 0x00FFFFFF,
                                    color2 = MaterialTheme.colorScheme.primaryContainer.toArgb()
                                ),
//                            1f to Color.Transparent,
//                            startY = 0.1f * headerHeight,
                                endY = movementFraction(
                                    scroll = scrollState.value,
                                    start = titleShrinkStartPoint,
                                    end = spaceAboveTitle
                                ) * headerHeight
                            )
                        )*/
                        .padding(horizontal = dimensionResource(R.dimen.detail_screen_padding))
                ) {
                    Title(
                        locationName = hourlyChartData.firstOrNull()?.locationName ?: "",
                        modifier = Modifier
                            .height(headerHeight.toDp(density))
                            .graphicsLayer {
                                translationX = titleXTranslation(
                                    scroll = scrollState.value,
                                    start = titleShrinkStartPoint,
                                    end = spaceAboveTitle,
                                    maxMovement = titleMaxXMove
                                )
                            }
                            .padding(end = endPadding)
                            .onGloballyPositioned {
                                titleWidth = it.size.width
                            },
                        scrollValue = scrollState.value,
                        titleShrinkStartPoint = titleShrinkStartPoint,
                        spaceAboveTitle = spaceAboveTitle
                    )
                }
                Spacer(modifier = Modifier.height(subtitleVerticalSpace.toDp(density)))
//                with(sharedTransitionScope) {
                    WeatherCurrentCondition(
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp),
                        weatherCode = detailState.currentDayHourlyWeather
                                .getOrNull(detailState.targetX.toInt())?.weatherCode,
                        sunRise = detailState.sunRise,
                        sunSet = detailState.sunSet,
                        utcOffset = detailState.utcOffset ?: 0,
                        targetX = detailState.targetX
                    )
                    WindCurrentCondition(
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                        currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
                        targetX = detailState.targetX,
                        appSettings = appSettings
                    )
                    CurrentCondition(
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                        weatherQuantity = listOf(WeatherQuantity.HUMIDITY).toImmutableList(),
                        currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
                        currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
                        targetX = detailState.targetX,
                        appSettings = appSettings,
                        topBannerHeight = headerHeight
                    )
                    CurrentCondition(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        weatherQuantity = listOf(
                            WeatherQuantity.PRECIPITATION,
                            WeatherQuantity.PRECIPITATIONPROBABILITY
                        ).toImmutableList(),
                        currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
//                        currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
                        targetX = detailState.targetX,
                        appSettings = appSettings,
                        topBannerHeight = headerHeight,
//                        scroll = currentConditionScroll
                    )
                    CurrentCondition(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        weatherQuantity = listOf(WeatherQuantity.DEWPOINT).toImmutableList(),
                        currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
                        currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
                        targetX = detailState.targetX,
                        appSettings = appSettings,
                        topBannerHeight = headerHeight
                    )
                    CurrentCondition(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        weatherQuantity = listOf(WeatherQuantity.FREEZINGLEVELHEIGHT).toImmutableList(),
                        currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
                        currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
                        targetX = detailState.targetX,
                        appSettings = appSettings,
                        topBannerHeight = headerHeight
                    )
                    CurrentCondition(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        weatherQuantity = listOf(WeatherQuantity.SURFACEPRESSURE).toImmutableList(),
                        currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
                        currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
                        targetX = detailState.targetX,
                        appSettings = appSettings,
                        topBannerHeight = headerHeight
                    )
                    CurrentCondition(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        weatherQuantity = listOf(WeatherQuantity.VISIBILITY).toImmutableList(),
                        currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
                        currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
                        targetX = detailState.targetX,
                        appSettings = appSettings,
                        topBannerHeight = headerHeight
                    )
                    UVCurrentCondition(
                        modifier = Modifier.padding(8.dp),
                        currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
                        targetX = detailState.targetX,
                        appSettings = appSettings
                    )
                    CurrentCondition(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        weatherQuantity = listOf(
                            WeatherQuantity.DIRECTRADIATION,
                            WeatherQuantity.DIRECTNORMALIRRADIANCE
                        ).toImmutableList(),
                        currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
                        currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
                        targetX = detailState.targetX,
                        appSettings = appSettings,
                        topBannerHeight = headerHeight,
//                        scroll = currentConditionScroll
                    )
                    Box(
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        AirQualityCurrentCondition(
                            modifier = Modifier.Companion.fillMaxWidth()
                                /*.sharedBounds(
                                    sharedTransitionScope.rememberSharedContentState(
                                        key = "AQI_${dailyChartData.firstOrNull()?.locationId}"
                                    ),
                                    animatedVisibilityScope = animatedContentScope,
                                )*/,
                            currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
                            targetX = detailState.targetX,
                            topBannerHeight = headerHeight,
                            appSettings = appSettings,
                            quantity = WeatherQuantity.AQI,
//                            scroll = currentConditionScroll
                        )
                        TextButton(
                            modifier = Modifier.align(Alignment.TopEnd).padding(end = endPadding),
                            onClick = { onNavigateToAQDetailScreen() }
                        ) {
                            Text(
                                text = "Details",
                                fontFamily = FontFamily.Default
                            )
                        }
                    }
//                }


                Spacer(
                    modifier = Modifier
                        .height(
                            dimensionResource(id = R.dimen.detail_screen_section_separation) +
                                    subtitleVerticalSpace.toDp(density)
                        )
                        .onGloballyPositioned {
                            timeBottomSpacerDistanceFromHeader = it.positionInParent().y
                        }
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (detailState.chartsVisibility) {
                    val undoIconTint = when (hourlyChartState.chartTheme) {
                        ChartTheme.LIGHT -> OnDiagramLightTheme
                        ChartTheme.DARK -> OnDiagramDarkTheme
                        ChartTheme.APPTHEME -> LocalContentColor.current
                        ChartTheme.DAYNIGHT -> DiagramGrid
                    }
                    QuantityChooserRail(
                        modifier = Modifier.padding(end = endPadding),
                        quantities = hourlyChartState.chartQuantities.toImmutableList(),
                        isAirQualityQuantities = false,
                        onRemoveChartQuantity = onRemoveChartQuantity,
                        onAddChartQuantity = onAddChartQuantity
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    QuantityChooserRail(
                        modifier = Modifier.padding(end = endPadding),
                        quantities = hourlyChartState.chartQuantities.toImmutableList(),
                        isAirQualityQuantities = true,
                        onRemoveChartQuantity = onRemoveChartQuantity,
                        onAddChartQuantity = onAddChartQuantity
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HourlyDiagramLegend(
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp + endPadding),
                        chartQuantities = hourlyChartState.chartQuantities.toImmutableList(),
                        dotsOnCurveVisible = hourlyChartState.dotsOnCurveVisible,
                        curveValueAtIndicator = hourlyChartState.curveValueAtIndicator.toImmutableList(),
                        appSettings = appSettings,
                        onRemoveChartQuantity = onRemoveChartQuantity
                    )

                    val diagramHorPadding =
                        dimensionResource(id = R.dimen.diagram_frame_hor_padding).toPx(density)
                    val diagramVertPadding =
                        dimensionResource(id = R.dimen.diagram_frame_ver_padding).toPx(density)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.diagram_height))
                            .padding(horizontal = dimensionResource(id = R.dimen.whole_diagram_hor_padding))
                    ) {
                        HourlyChart(
                            modifier = Modifier.fillMaxSize(),
                            diagramHorPadding = diagramHorPadding,
                            diagramVertPadding = diagramVertPadding,
                            hourlyChartData = hourlyChartData.toImmutableList(),
                            hourlyChartState = hourlyChartState,
                            appSettings = appSettings,
                            onAddYAxis = onAddYAxis,
                            onMoveAxis = onMoveHourlyDiagramAxis,
                            onScaleAxis = onScaleHourlyDiagramAxis,
                            onScaleBack = onScaleBack,
                            onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                            onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation,
                            onUpdateCurveValueAtIndicator = onUpdateHourlyCurveValueAtIndicator
                        )
                        IconButton(
                            modifier = Modifier
                                .padding(
                                    top = dimensionResource(id = R.dimen.diagram_frame_ver_padding) - 8.dp,
                                    end = dimensionResource(id = R.dimen.diagram_frame_hor_padding) - 8.dp + endPadding
                                )
                                .align(Alignment.TopEnd),
                            onClick = undoHourlyDiagramScaleMove
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.undo),
                                contentDescription = "Undo",
                                tint = undoIconTint
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    IndicatorSlider(
                        modifier = Modifier
                            .padding(
                                horizontal = dimensionResource(id = R.dimen.whole_diagram_hor_padding) +
                                        dimensionResource(id = R.dimen.diagram_frame_hor_padding) -
                                        dimensionResource(id = R.dimen.diagram_slider_thumb_size) / 2,
                                vertical = 8.dp
                            )
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.diagram_slider_thumb_size)),
                        thumbPosition = hourlyChartState.sliderThumbPosition,
                        onUpdateThumbPosition = onUpdateHourlyThumbPosition
                    )
                    Spacer(
                        modifier = Modifier
                            .height(
                                dimensionResource(id = R.dimen.detail_screen_section_separation) +
                                        subtitleVerticalSpace.toDp(density)
                            )
                            .onGloballyPositioned {
                                hourlyBottomSpacerDistanceFromHeader = it.positionInParent().y
                            }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    DailyQuantityChooserRail(
                        modifier = Modifier.padding(end = endPadding),
                        quantity = dailyChartState.chartQuantity,
                        onUpdateChartQuantity = onUpdateDailyChartQuantity
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.diagram_height))
                            .padding(horizontal = dimensionResource(id = R.dimen.whole_diagram_hor_padding))
                    ) {
                        DailyChart(
                            diagramHorPadding = diagramHorPadding,
                            diagramVertPadding = diagramVertPadding,
                            dailyChartData = dailyChartData.toImmutableList(),
                            appSettings = appSettings,
                            dailyChartState = dailyChartState,
                            chartGrid = hourlyChartState.chartGrid,
                            chartTheme = hourlyChartState.chartTheme,
                            curveShadowVisible = hourlyChartState.curveShadowVisible,
                            onUpdateDailyYAxis = onUpdateDailyYAxis,
                            onMoveAxis = onMoveDailyDiagramAxis,
                            onScaleAxis = onScaleDailyDiagramAxis,
                            onScaleBack = onScaleBackDaily,
                            onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                            onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation,
                            onUpdateCurveValueAtIndicator = onUpdateDailyCurveValueAtIndicator
                        )
                        IconButton(
                            modifier = Modifier
                                .padding(
                                    top = dimensionResource(id = R.dimen.diagram_frame_ver_padding) - 8.dp,
                                    end = dimensionResource(id = R.dimen.diagram_frame_hor_padding) - 8.dp + endPadding
                                )
                                .align(Alignment.TopEnd),
                            onClick = undoDailyDiagramScaleMove
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.undo),
                                contentDescription = "Undo",
                                tint = undoIconTint
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    IndicatorSlider(
                        modifier = Modifier
                            .padding(
                                horizontal = dimensionResource(id = R.dimen.whole_diagram_hor_padding) +
                                        dimensionResource(id = R.dimen.diagram_frame_hor_padding) -
                                        dimensionResource(id = R.dimen.diagram_slider_thumb_size) / 2,
                                vertical = 8.dp
                            )
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.diagram_slider_thumb_size)),
                        thumbPosition = dailyChartState.sliderThumbPosition,
                        onUpdateThumbPosition = onUpdateDailyThumbPosition
                    )
                }
                Spacer(modifier = Modifier.height(204.dp))
            }
        }
        if (timeAlpha > 0) {
            Time(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        translationY = (spaceAboveTitle + headerHeight - scrollState.value)
                            .coerceAtLeast(headerHeight)
                        alpha = timeAlpha
                    },
                dateTime = detailState.localDateTime
            )
        }
        if (detailState.chartsVisibility) {
            if (hourlyHeaderAlpha > 0) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .graphicsLayer {
                            translationY = hourlyHeaderTop
                            alpha = hourlyHeaderAlpha
                        }
                ) {
                    HourlyDiagramTopBar(
                        endPadding = endPadding,
                        hourlyDiagramSettingOpen = detailState.hourlyDiagramSettingOpen,
                        dotsOnCurveVisible = hourlyChartState.dotsOnCurveVisible,
                        sunRiseSetIconsVisible = hourlyChartState.sunRiseSetIconsVisible,
                        weatherConditionIconsVisible = hourlyChartState.weatherConditionIconsVisible,
                        curveShadowVisible = hourlyChartState.curveShadowVisible,
                        chartGrid = hourlyChartState.chartGrid,
                        onUpdateHourlyDiagramSettingOpen = onUpdateHourlyDiagramSettingOpen,
                        onUpdateChartWeatherConditionVisibility = onUpdateChartWeatherConditionVisibility,
                        onUpdateChartDotsOnCurveVisibility = onUpdateChartDotsOnCurveVisibility,
                        onUpdateChartCurveShadowVisibility = onUpdateChartCurveShadowVisibility,
                        onUpdateChartSunRiseSetIconsVisibility = onUpdateChartSunRiseSetIconsVisibility,
                        onUpdateChartGrids = onUpdateChartGrids,
                        onUpdateChooseDiagramThemeDialogVisibility = onUpdateChooseDiagramThemeDialogVisibility,
                        onUpdateHourlyDiagramSettingRectangle = onUpdateHourlyDiagramSettingRectangle
                    )
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth()
                    .graphicsLayer {
                        translationY = dailyHeaderTop
                        alpha = dailyHeaderAlpha
                    }
            ) {
                DailyDiagramTopBar(
                    endPadding = endPadding,
                    dailyDiagramSettingOpen = detailState.dailyDiagramSettingOpen,
                    curveShadowVisible = hourlyChartState.curveShadowVisible,
                    chartGrid = hourlyChartState.chartGrid,
                    weatherConditionIconsVisible = dailyChartState.weatherConditionIconsVisible,
                    onUpdateDailyDiagramSettingOpen = onUpdateDailyDiagramSettingOpen,
                    onUpdateChartWeatherConditionVisibility = onUpdateDailyChartWeatherConditionVisibility,
                    onUpdateChartCurveShadowVisibility = onUpdateChartCurveShadowVisibility,
                    onUpdateChartGrids = onUpdateChartGrids,
                    onUpdateChooseDiagramThemeDialogVisibility = onUpdateChooseDiagramThemeDialogVisibility,
                    onUpdateDailyDiagramSettingRectangle = onUpdateDailyDiagramSettingRectangle
                )
            }
        }
    }
}

@Composable
fun Title(
    locationName: String,
    modifier: Modifier = Modifier,
    scrollValue: Int,
    titleShrinkStartPoint: Float,
    spaceAboveTitle: Float
) {
    Box(
        modifier = modifier
    ) {
        val fontSize = (30 - 10 *
                (scrollValue - titleShrinkStartPoint)/(spaceAboveTitle - titleShrinkStartPoint))
            .coerceIn(20f, 30f)
        BorderedText(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(64.dp)
                .wrapContentHeight(),
            text = buildAnnotatedString { append(locationName) },
            fontFamily = Font(R.font.cmu_serif_bold).toFontFamily(),
            strokeColor = MaterialTheme.colorScheme.background,
            fillColor = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = fontSize.sp,
            strokeWidth = 1.5f * (fontSize - 20)
        )
    }
}

@Composable
fun Time(
    modifier: Modifier = Modifier,
    dateTime: LocalDateTime
) {
    val density = LocalDensity.current
    val timeText = dateTime.format(
        DateTimeFormatter.ofPattern("EEEE, d MMM uuuu, HH\u200C:\u200Cmm")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.detail_screen_subtitle_vertical_padding))
    ) {
        Row(
            modifier = Modifier
                .height(dimensionResource(id = R.dimen.detail_screen_subtitle_height))
                .graphicsLayer {
                    shape = RoundedCornerShape(percent = 50)
                    shadowElevation = 4.dp.toPx()
                    clip = true
                }
                .background(color = MaterialTheme.colorScheme.tertiaryContainer)
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeText,
                fontFamily = Font(R.font.cmu_typewriter_bold).toFontFamily(),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontSize = dimensionResource(id = R.dimen.detail_screen_subtitle_font_size).toSp(density)
            )
        }
    }
}


private fun titleXTranslation(
    scroll: Int,
    start: Float,
    end: Float,
    maxMovement: Float
): Float {
    return ((scroll-start)/(end - start)).coerceIn(0f, 1f) * maxMovement
}
private fun movementFraction(scroll: Int, start: Float, end: Float): Float {
    return ((scroll - start) / (end - start)).coerceIn(0.1f, 1f)
}
private fun barColor(scroll: Int, start: Float, end: Float, color1: Int, color2: Int):Color {
    return Color(
        ColorUtils.blendARGB(
        color1,
        color2,
        ((scroll - start) / (end - start)).pow(5).coerceIn(0f, 1f)
    ))
}


@Preview(showBackground = true)
@Composable
fun HourlyDiagramTopLegendPreview(modifier: Modifier = Modifier) {
    WeathererTheme {
        HourlyDiagramLegendRow(
            text = buildAnnotatedString { append(" Hello! Hello! Hello! Hello! Hello!") },
            curveIndex = 1,
            showDot = true,
            onRemoveQuantity = {}
        )
    }
}