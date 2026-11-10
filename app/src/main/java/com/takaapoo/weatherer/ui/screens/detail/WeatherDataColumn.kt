package com.takaapoo.weatherer.ui.screens.detail

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.domain.model.HistoricDailyChartDto
import com.takaapoo.weatherer.domain.model.HistoricHourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.DailyChart
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.DailyQuantityChooserRail
import com.takaapoo.weatherer.ui.screens.detail.historical.DataLoadingError
import com.takaapoo.weatherer.ui.screens.detail.historical.DatePicker
import com.takaapoo.weatherer.ui.screens.detail.historical.DonatePromotion
import com.takaapoo.weatherer.ui.screens.detail.historical.HistoricTopTabs
import com.takaapoo.weatherer.ui.screens.detail.historical.HistoricalTabs
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.HourlyChart
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.HourlyDiagramLegend
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.QuantityChooserRail
import com.takaapoo.weatherer.ui.screens.donate.Error
import com.takaapoo.weatherer.ui.theme.DiagramGrid
import com.takaapoo.weatherer.ui.theme.OnDiagramDarkTheme
import com.takaapoo.weatherer.ui.theme.OnDiagramLightTheme
import com.takaapoo.weatherer.ui.theme.Transparent10
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.utility.drawThickText
import com.takaapoo.weatherer.utility.toDp
import com.takaapoo.weatherer.utility.toPx
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDataColumn(
    endPadding: Dp,
    hourlyChartData: List<HourlyChartDto>,
    dailyChartData: List<LocalDailyWeather>,
    historicalHourlyData: List<HistoricHourlyChartDto>,
    historicalDailyData: List<HistoricDailyChartDto>,
    appSettings: AppSettings,
    detailState: DetailState,
    hourlyChartState: HourlyChartState,
    dailyChartState: DailyChartState,
    historicalChartState: HourlyChartState,
    historicalDailyChartState: DailyChartState,
    modifier: Modifier = Modifier,
    spaceAboveTitle: Float,
    bottomPadding: Dp,
    headerHeight: Float,
    cornerRadiusProvider: () -> Dp,
    tab: DetailTabs,
    tabHeight: Float,
    initialXAxisRange: ClosedFloatingPointRange<Float>,
    historicInitialXAxisRange: ClosedFloatingPointRange<Float>,
    backdrop: LayerBackdrop,
    trialPeriodExpired: Boolean?,
    hasDonated: Boolean?,

    onAddYAxis: (start: Float, end: Float, diagramHeight: Float, textMeasurer: TextMeasurer, axisIndex: Int?, curveAnimatorInitialValue: Float, isHistoric: Boolean) -> Unit,
    onUpdateDailyYAxis: (quantity: DailyWeatherQuantity, curveAnimatorInitialValue: Float, isHistoric: Boolean) -> Unit,
    onMoveHourlyDiagramAxis: (Offset, isHistoric: Boolean) -> Unit,
    onScaleHourlyDiagramAxis: (Offset, Float, Float, isHistoric: Boolean) -> Unit,
    onScaleBack: (Offset, isHistoric: Boolean) -> Unit,
    undoHourlyDiagramScaleMove: () -> Unit,
    onMoveDailyDiagramAxis: (Offset, isHistoric: Boolean) -> Unit,
    onScaleDailyDiagramAxis: (Offset, Float, Float, isHistoric: Boolean) -> Unit,
    onScaleBackDaily: (Offset, isHistoric: Boolean) -> Unit,
    undoDailyDiagramScaleMove: () -> Unit,
    onCalculateHorizontalBarSeparation:
        (start: Float, end: Float, diagramHeight: Float, textMeasurer: TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation:
        (start: Float, end: Float, diagramWidth: Float, textMeasurer: TextMeasurer) -> Pair<Int, Float>,
    onAddHourlyChartQuantity: (quantity: HourlyWeatherQuantity, isHistoric: Boolean) -> Unit,
    onRemoveHourlyChartQuantity: (quantity: HourlyWeatherQuantity, isHistoric: Boolean) -> Unit,
    onUpdateDailyChartQuantity: (quantity: DailyWeatherQuantity, isHistoric: Boolean) -> Unit,
    onUpdateHourlyThumbPosition: (movement: Float, isHistoric: Boolean) -> Unit,
    onUpdateDailyThumbPosition: (movement: Float, isHistoric: Boolean) -> Unit,
    onUpdateHourlyCurveValueAtIndicator: (curveIndex: Int, value: Float?, isHistoric: Boolean) -> Unit,
    onNavigateToAQDetailScreen: () -> Unit,
    onUpdateSelectedTab: (tab: DetailTabs) -> Unit,
    onAnimateHourlyDiagramAxis: (time: LocalDate) -> Unit,
    onReloadHistoricalData: () -> Unit,
    onReloadHistoricalDailyData: () -> Unit,
    onAnimateDailyDiagramAxis: (time: LocalDate) -> Unit,
    onNavigateToDonateScreen: () -> Unit,
    onNavigateToLoginScreen: () -> Unit,
    onConnectRetry: () -> Unit,
) {
    val activity = LocalActivity.current
    val density = LocalDensity.current
//    val scrollState = detailState.scrollState
    val titleShrinkStartPoint = spaceAboveTitle - 0.3f * (activity as MainActivity).windowHeight
//    var titleWidth by rememberSaveable{ mutableIntStateOf(0) }
//    val screenPadding = dimensionResource(R.dimen.detail_screen_padding).toPx(density)
//    val titleMaxXMove = (detailScreenWidth - titleWidth)/2f - screenPadding
    var previousTab by rememberSaveable { mutableStateOf(tab) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(
                state = detailState.scrollState,
                enabled = detailState.scrollEnabled()
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(spaceAboveTitle.toDp(density)))
        val surfaceColor = MaterialTheme.customColorScheme.detailScreenSurface.toArgb()
        val borderColor = MaterialTheme.colorScheme.primaryContainer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = {
                        val cornerSize = CornerSize(cornerRadiusProvider())
                        RoundedCornerShape(
                            topStart = cornerSize,
                            topEnd = cornerSize,
                            bottomStart = CornerSize(0f),
                            bottomEnd = CornerSize(0f)
                        )
                    },
                    effects = {
                        vibrancy()
                        lens(64.dp.toPx(), 32.dp.toPx())
                        blur(4.dp.toPx())
                    }
                )
                .drawBehind {
                    val width = size.width
                    val height = size.height
                    val cornerRad = CornerRadius(cornerRadiusProvider().toPx())
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
                        color = barColor(
                            scroll = detailState.scrollState.value,
                            start = titleShrinkStartPoint,
                            end = spaceAboveTitle,
                            color1 = Transparent10.toArgb(),
                            color2 = surfaceColor
                        )
                    )
                    if (Build.VERSION.SDK_INT < 33) {
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
                }
                .padding(bottom = bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = if (detailState.scrollState.value < activity.windowHeight) 1f else 0f
                    }
                    .drawBehind {
                        val width = size.width
                        val height = size.height
                        val cornerRad = CornerRadius(cornerRadiusProvider().toPx())
                        val backgroundPath = Path().apply {
                            addRoundRect(
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
                                    scroll = detailState.scrollState.value,
                                    start = titleShrinkStartPoint,
                                    end = spaceAboveTitle,
                                    color1 = 0x00FFFFFF,
                                    color2 = borderColor.toArgb()
                                ),
                                endY = movementFraction(
                                    scroll = detailState.scrollState.value,
                                    start = titleShrinkStartPoint,
                                    end = spaceAboveTitle
                                ) * headerHeight
                            )
                        )
                    }
            ) {
                Title2(
                    locationName = hourlyChartData.firstOrNull()?.locationName ?: "",
                    modifier = Modifier
                        .height(headerHeight.toDp(density))
                        .padding(
                            start = dimensionResource(R.dimen.detail_screen_padding),
                            end = endPadding + dimensionResource(R.dimen.detail_screen_padding)
                        ),
                    scrollState = detailState.scrollState,
                    titleShrinkStartPoint = titleShrinkStartPoint,
                    spaceAboveTitle = spaceAboveTitle
                )
                detailPagerTopTabs(
                    localDateTime = detailState.localDateTime,
                    selectedTabIndex = detailState.selectedTab.ordinal,
                    onUpdateSelectedTabIndex = onUpdateSelectedTab
                )
            }

            AnimatedContent(
                modifier = Modifier,
                targetState = tab,
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = if (previousTab < tab) {
                            slideInHorizontally(initialOffsetX = { it })
                        } else {
                            slideInHorizontally(initialOffsetX = { -it })
                        },
                        initialContentExit = if (previousTab < tab) {
                            slideOutHorizontally(targetOffsetX = { -it })
                        } else {
                            slideOutHorizontally(targetOffsetX = { it })
                        },
                        targetContentZIndex = 0f,
                        sizeTransform = null
                    )
                }
            ) {  detailTab ->
                val columnHeight = activity.windowHeight.toFloat() - tabHeight -
                        WindowInsets.statusBars.getTop(density) - bottomPadding.toPx(density)
                val collapseBarHeight = (64.dp).toPx(density)
                LaunchedEffect(detailTab) {
                    val maxScroll = activity.windowHeight.toFloat() + collapseBarHeight
                    if (detailState.scrollState.value > maxScroll){
                        detailState.scrollState.scrollTo(maxScroll.toInt())
                    }
                }
                when(detailTab) {
                    DetailTabs.Now -> {
                        CurrentWeather(
                            modifier = Modifier,
                            detailState = detailState,
                            appSettings = appSettings,
                            headerHeight = headerHeight,
                            endPadding = endPadding,
                            onNavigateToAQDetailScreen = onNavigateToAQDetailScreen
                        )
                    }
                    DetailTabs.Hourly -> {
                        HourlyWeather(
                            modifier = Modifier,
                            hourlyChartState = hourlyChartState,
                            endPadding = endPadding,
                            appSettings = appSettings,
                            hourlyChartData = hourlyChartData,
                            columnHeight = columnHeight,
//                            maxScroll = context.windowHeight.toFloat() + collapseBarHeight,
//                            scrollState = detailState.scrollState,
                            initialXAxisRange = initialXAxisRange,

                            onRemoveChartQuantity = {
                                onRemoveHourlyChartQuantity(it, false)
                            },
                            onAddChartQuantity = {
                                onAddHourlyChartQuantity(it, false)
                            },
                            onAddYAxis = { start, end, diagramHeight, textMeasurer, axisIndex,
                                           curveAnimatorInitialValue ->
                                onAddYAxis( start, end, diagramHeight,
                                    textMeasurer, axisIndex, curveAnimatorInitialValue, false )
                            },
                            onMoveHourlyDiagramAxis = {
                                onMoveHourlyDiagramAxis(it, false)
                            },
                            onScaleHourlyDiagramAxis = { center, scaleX, scaleY ->
                                onScaleHourlyDiagramAxis(center, scaleX, scaleY, false)
                            },
                            onScaleBack = { offset ->
                                onScaleBack(offset, false)
                            },
                            onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                            onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation,
                            onUpdateHourlyCurveValueAtIndicator = { index, value ->
                                onUpdateHourlyCurveValueAtIndicator(index, value, false)
                            },
                            undoHourlyDiagramScaleMove = undoHourlyDiagramScaleMove,
                            onUpdateHourlyThumbPosition = {
                                onUpdateHourlyThumbPosition(it, false)
                            }
                        )
                    }
                    DetailTabs.Daily -> {
                        DailyWeather(
                            modifier = Modifier,
                            hourlyChartState = hourlyChartState,
                            endPadding = endPadding,
                            dailyChartState = dailyChartState,
                            appSettings = appSettings,
                            dailyChartData = dailyChartData,
                            columnHeight = columnHeight,
//                            maxScroll = context.windowHeight.toFloat() + headerHeight,
//                            scrollState = detailState.scrollState,

                            onUpdateDailyChartQuantity = { quantity ->
                                onUpdateDailyChartQuantity(quantity, false)
                            },
                            onUpdateDailyYAxis = { quantity, curveAnimatorInitialValue ->
                                onUpdateDailyYAxis(quantity, curveAnimatorInitialValue, false)
                            },
                            onMoveDailyDiagramAxis = { offset ->
                                onMoveDailyDiagramAxis(offset, false)
                            },
                            onScaleDailyDiagramAxis = { center, scaleX, scaleY ->
                                onScaleDailyDiagramAxis(center, scaleX, scaleY, false)
                            },
                            onScaleBackDaily = { offset ->
                                onScaleBackDaily(offset, false)
                            },
                            onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                            onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation,
//                                onUpdateDailyCurveValueAtIndicator = { minValue, maxValue ->
//                                    onUpdateDailyCurveValueAtIndicator(minValue, maxValue, false)
//                                },
                            undoDailyDiagramScaleMove = undoDailyDiagramScaleMove,
                            onUpdateDailyThumbPosition = {
                                onUpdateDailyThumbPosition(it, false)
                            }
                        )
                    }
                    DetailTabs.History -> {
                        if(hasDonated == true || trialPeriodExpired == false) {
                            HistoricalWeather(
                                modifier = Modifier,
                                historicalChartState = historicalChartState.copy(
                                    dotsOnCurveVisible = hourlyChartState.dotsOnCurveVisible,
                                    curveShadowVisible = hourlyChartState.curveShadowVisible,
                                    chartGrid = hourlyChartState.chartGrid,
                                    chartTheme = hourlyChartState.chartTheme,
                                    weatherConditionIconsVisible = hourlyChartState.weatherConditionIconsVisible
                                ),
                                historicalDailyChartState = historicalDailyChartState.copy(
                                    weatherConditionIconsVisible = hourlyChartState.weatherConditionIconsVisible
                                ),
                                endPadding = endPadding,
                                appSettings = appSettings,
                                historicalHourlyData = historicalHourlyData,
                                historicalDailyData = historicalDailyData,
                                columnHeight = columnHeight,
//                                maxScroll = context.windowHeight.toFloat() + headerHeight,
                                detailState = detailState,
                                initialXAxisRange = historicInitialXAxisRange,

                                onRemoveChartQuantity = {
                                    onRemoveHourlyChartQuantity(it, true)
                                },
                                onAddChartQuantity = {
                                    onAddHourlyChartQuantity(it, true)
                                },
                                onAddYAxis = {
                                        start, end, diagramHeight, textMeasurer, axisIndex,
                                        curveAnimatorInitialValue
                                    ->
                                    onAddYAxis(
                                        start, end, diagramHeight,
                                        textMeasurer, axisIndex, curveAnimatorInitialValue, true
                                    )
                                },
                                onMoveHourlyDiagramAxis = {
                                    onMoveHourlyDiagramAxis(it, true)
                                },
                                onScaleHourlyDiagramAxis = { center, scaleX, scaleY ->
                                    onScaleHourlyDiagramAxis(center, scaleX, scaleY, true)
                                },
                                onScaleBack = { offset ->
                                    onScaleBack(offset, true)
                                },
                                onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                                onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation,
                                onUpdateHourlyCurveValueAtIndicator = { index, value ->
                                    onUpdateHourlyCurveValueAtIndicator(index, value, true)
                                },
                                onUpdateHourlyThumbPosition = {
                                    onUpdateHourlyThumbPosition(it, true)
                                },
                                onAnimateHourlyDiagramAxis = onAnimateHourlyDiagramAxis,
                                onReloadHistoricalData = onReloadHistoricalData,

                                onUpdateDailyChartQuantity = { quantity ->
                                    onUpdateDailyChartQuantity(quantity, true)
                                },
                                onUpdateDailyYAxis = { quantity, curveAnimatorInitialValue ->
                                    onUpdateDailyYAxis(
                                        quantity,
                                        curveAnimatorInitialValue,
                                        true
                                    )
                                },
                                onMoveDailyDiagramAxis = { offset ->
                                    onMoveDailyDiagramAxis(offset, true)
                                },
                                onScaleDailyDiagramAxis = { center, scaleX, scaleY ->
                                    onScaleDailyDiagramAxis(center, scaleX, scaleY, true)
                                },
//                                    onUpdateDailyCurveValueAtIndicator = { minValue, maxValue ->
//                                        onUpdateDailyCurveValueAtIndicator(
//                                            minValue,
//                                            maxValue,
//                                            false
//                                        )
//                                    },
                                onScaleBackDaily = { offset ->
                                    onScaleBackDaily(offset, true)
                                },
                                onUpdateDailyThumbPosition = {
                                    onUpdateDailyThumbPosition(it, true)
                                },
                                onAnimateDailyDiagramAxis = onAnimateDailyDiagramAxis,
                                onReloadHistoricalDailyData = onReloadHistoricalDailyData,
                            )
                        }
                        else if(hasDonated == null) {
                            Error(
                                modifier = modifier,
                                text = "Connecting to Play store failed!",
                                onConnectRetry = onConnectRetry
                            )
                        }
                        else {
                            DonatePromotion(
                                modifier = Modifier,
                                columnHeight = columnHeight,
                                trialPeriodExpired = trialPeriodExpired,
                                onNavigateToDonateScreen = onNavigateToDonateScreen,
                                onNavigateToLoginScreen = onNavigateToLoginScreen
                            )
                        }
                    }
                }
                previousTab = detailTab
            }
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
        )
    )
}

@Composable
fun Title2(
    locationName: String,
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    titleShrinkStartPoint: Float,
    spaceAboveTitle: Float,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val textColor = MaterialTheme.colorScheme.onPrimaryContainer
    val borderColor = MaterialTheme.colorScheme.primaryContainer
    val fontFamily = Font(R.font.cmu_serif_bold).toFontFamily()
    val borderWidth = 15f
    val fontSize = 30.sp
    val titleSize = textMeasurer.measure(
        text = locationName,
        style = TextStyle(
            fontFamily = fontFamily,
            fontSize = fontSize,
            drawStyle = Stroke(width = borderWidth)
        )
    ).size
    val titleMaxXMove = (detailScreenWidth - titleSize.width)/2f - dimensionResource(R.dimen.detail_screen_padding).toPx(density)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                val fontSize = (30 - 10 *
                        (scrollState.value - titleShrinkStartPoint) / (spaceAboveTitle - titleShrinkStartPoint))
                    .coerceIn(20f, 30f)
                val scale = fontSize / 30f
                scaleX = scale
                scaleY = scale
                translationX = titleXTranslation(
                    scroll = scrollState.value,
                    start = titleShrinkStartPoint,
                    end = spaceAboveTitle,
                    maxMovement = titleMaxXMove
                ) * scale
            }
            .drawWithContent {
                drawThickText(
                    textMeasurer = textMeasurer,
                    text = locationName,
                    fontFamily = fontFamily,
                    fontSize = fontSize,
                    textLeft = 0f,
                    textBottom = size.height,
                    textColor = textColor,
                    borderColor = borderColor,
                    borderWidth = borderWidth,
                )
            }
    )
}



@Composable
fun CurrentWeather(
    modifier: Modifier = Modifier,
    detailState: DetailState,
    appSettings: AppSettings,
    headerHeight: Float,
    endPadding: Dp,
    onNavigateToAQDetailScreen: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
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
            hourlyWeatherQuantity = listOf(HourlyWeatherQuantity.HUMIDITY).toImmutableList(),
            currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
//                        currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
            targetX = detailState.targetX,
            appSettings = appSettings,
            topBannerHeight = headerHeight
        )
        CurrentCondition(
            modifier = Modifier.padding(horizontal = 8.dp),
            hourlyWeatherQuantity = listOf(
                HourlyWeatherQuantity.PRECIPITATION,
                HourlyWeatherQuantity.PRECIPITATIONPROBABILITY
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
            hourlyWeatherQuantity = listOf(HourlyWeatherQuantity.CLOUDCOVER).toImmutableList(),
            currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
//                    currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
            targetX = detailState.targetX,
            appSettings = appSettings,
            topBannerHeight = headerHeight
        )
        CurrentCondition(
            modifier = Modifier.padding(horizontal = 8.dp),
            hourlyWeatherQuantity = listOf(HourlyWeatherQuantity.DEWPOINT).toImmutableList(),
            currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
//                        currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
            targetX = detailState.targetX,
            appSettings = appSettings,
            topBannerHeight = headerHeight
        )
        CurrentCondition(
            modifier = Modifier.padding(horizontal = 8.dp),
            hourlyWeatherQuantity = listOf(HourlyWeatherQuantity.FREEZINGLEVELHEIGHT).toImmutableList(),
            currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
//                        currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
            targetX = detailState.targetX,
            appSettings = appSettings,
            topBannerHeight = headerHeight
        )
        CurrentCondition(
            modifier = Modifier.padding(horizontal = 8.dp),
            hourlyWeatherQuantity = listOf(HourlyWeatherQuantity.SURFACEPRESSURE).toImmutableList(),
            currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
//                        currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
            targetX = detailState.targetX,
            appSettings = appSettings,
            topBannerHeight = headerHeight
        )
        CurrentCondition(
            modifier = Modifier.padding(horizontal = 8.dp),
            hourlyWeatherQuantity = listOf(HourlyWeatherQuantity.VISIBILITY).toImmutableList(),
            currentDayHourlyWeather = detailState.currentDayHourlyWeather.toImmutableList(),
//                        currentDayHourlyAirQuality = detailState.currentDayHourlyAirQuality.toImmutableList(),
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
            hourlyWeatherQuantity = listOf(
                HourlyWeatherQuantity.DIRECTRADIATION,
                HourlyWeatherQuantity.DIRECTNORMALIRRADIANCE
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
                modifier = Modifier.fillMaxWidth()
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
                quantity = HourlyWeatherQuantity.AQI,
//                            scroll = currentConditionScroll
            )
            TextButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = endPadding),
                onClick =  onNavigateToAQDetailScreen
            ) {
                Text(
                    text = "Details",
                    fontFamily = FontFamily.Default
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun HourlyWeather(
    modifier: Modifier = Modifier,
    hourlyChartState: HourlyChartState,
    endPadding: Dp,
    appSettings: AppSettings,
    hourlyChartData: List<HourlyChartDto>,
    columnHeight: Float,
//    maxScroll: Float,
//    scrollState: ScrollState,
    initialXAxisRange: ClosedFloatingPointRange<Float>,

    onRemoveChartQuantity: (HourlyWeatherQuantity) -> Unit,
    onAddChartQuantity: (HourlyWeatherQuantity) -> Unit,
    onAddYAxis: (Float, Float, Float, TextMeasurer, Int?, Float) -> Unit,
    onMoveHourlyDiagramAxis: (Offset) -> Unit,
    onScaleHourlyDiagramAxis: (Offset, Float, Float) -> Unit,
    onScaleBack: (Offset) -> Unit,
    onCalculateHorizontalBarSeparation: (Float, Float, Float, TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation: (Float, Float, Float, TextMeasurer) -> Pair<Int, Float>,
    onUpdateHourlyCurveValueAtIndicator: (Int, Float?) -> Unit,
    undoHourlyDiagramScaleMove: () -> Unit,
    onUpdateHourlyThumbPosition: (Float) -> Unit
) {
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = columnHeight.toDp(density))
            .padding(top = 16.dp)
    ) {
        val currentColor = LocalContentColor.current
        val undoIconTint = remember(hourlyChartState.chartTheme) {
            when (hourlyChartState.chartTheme) {
                ChartTheme.LIGHT -> OnDiagramLightTheme
                ChartTheme.DARK -> OnDiagramDarkTheme
                ChartTheme.APPTHEME -> currentColor
                ChartTheme.DAYNIGHT -> DiagramGrid
            }
        }
        QuantityChooserRail(
            modifier = Modifier.padding(end = endPadding),
            appSettings = appSettings,
            selectedQuantities = hourlyChartState.chartQuantities.toImmutableList(),
            isAirQualityQuantities = false,
            onRemoveChartQuantity = onRemoveChartQuantity,
            onAddChartQuantity = onAddChartQuantity
        )
        Spacer(modifier = Modifier.height(8.dp))
        QuantityChooserRail(
            modifier = Modifier.padding(end = endPadding),
            appSettings = appSettings,
            selectedQuantities = hourlyChartState.chartQuantities.toImmutableList(),
            isAirQualityQuantities = true,
            onRemoveChartQuantity = onRemoveChartQuantity,
            onAddChartQuantity = onAddChartQuantity
        )
        Spacer(modifier = Modifier.height(16.dp))
        HourlyDiagramLegend(
            modifier = Modifier.padding(start = 16.dp, /*end = 8.dp + endPadding*/),
            chartQuantities = hourlyChartState.chartQuantities,
            dotsOnCurveVisible = hourlyChartState.dotsOnCurveVisible,
            curveValueAtIndicator = hourlyChartState.curveValueAtIndicator,
            appSettings = appSettings,
            onRemoveChartQuantity = onRemoveChartQuantity
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.diagram_height))
                .padding(horizontal = dimensionResource(id = R.dimen.whole_diagram_hor_padding))
        ) {
            HourlyChart(
                modifier = Modifier.fillMaxSize(),
                diagramHorPadding = dimensionResource(id = R.dimen.diagram_frame_hor_padding).toPx(density),
                diagramVertPadding = dimensionResource(id = R.dimen.diagram_frame_ver_padding).toPx(density),
                hourlyChartData = hourlyChartData.toImmutableList(),
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
                onScaleBack = onScaleBack,
                onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation,
                onUpdateCurveValueAtIndicator = onUpdateHourlyCurveValueAtIndicator,
            )
            ChartUndoBotton(
                modifier = Modifier
                    .padding(
                        top = dimensionResource(id = R.dimen.diagram_frame_ver_padding) - 8.dp,
                        end = dimensionResource(id = R.dimen.diagram_frame_hor_padding) - 8.dp + endPadding
                    )
                    .align(Alignment.TopEnd),
                undoIconTint = undoIconTint,
                undoDiagramScaleMove = undoHourlyDiagramScaleMove,
            )
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
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ChartUndoBotton(
    modifier: Modifier = Modifier,
    undoIconTint: Color,
    undoDiagramScaleMove: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = undoDiagramScaleMove
    ) {
        Icon(
            painter = painterResource(id = R.drawable.undo),
            contentDescription = "Undo",
            tint = undoIconTint
        )
    }
}

@Composable
fun DailyWeather(
    modifier: Modifier = Modifier,
    hourlyChartState: HourlyChartState,
    endPadding: Dp,
    dailyChartState: DailyChartState,
    appSettings: AppSettings,
    dailyChartData: List<LocalDailyWeather>,
    columnHeight: Float,
//    maxScroll: Float,
//    scrollState: ScrollState,

    onUpdateDailyChartQuantity: (DailyWeatherQuantity) -> Unit,
    onUpdateDailyYAxis: (DailyWeatherQuantity, curveAnimatorInitialValue: Float) -> Unit,
    onMoveDailyDiagramAxis: (Offset) -> Unit,
    onScaleDailyDiagramAxis: (Offset, Float, Float) -> Unit,
    onScaleBackDaily: (Offset) -> Unit,
    onCalculateHorizontalBarSeparation: (Float, Float, Float, TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation: (Float, Float, Float, TextMeasurer) -> Pair<Int, Float>,
//    onUpdateDailyCurveValueAtIndicator: (Float, Float) -> Unit,
    undoDailyDiagramScaleMove: () -> Unit,
    onUpdateDailyThumbPosition: (Float) -> Unit
) {
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = columnHeight.toDp(density))
            .padding(top = 16.dp)
    ) {
        val undoIconTint = when (hourlyChartState.chartTheme) {
            ChartTheme.LIGHT -> OnDiagramLightTheme
            ChartTheme.DARK -> OnDiagramDarkTheme
            ChartTheme.APPTHEME, ChartTheme.DAYNIGHT -> LocalContentColor.current
        }
        val diagramHorPadding =
            dimensionResource(id = R.dimen.diagram_frame_hor_padding).toPx(density)
        val diagramVertPadding =
            dimensionResource(id = R.dimen.diagram_frame_ver_padding).toPx(density)

        DailyQuantityChooserRail(
            modifier = Modifier.padding(end = endPadding),
            allQuantities = DailyWeatherQuantity.entries
                .filter { it.historicalState != HistoricalState.HISTORIC_ONLY }.toImmutableList(),
            selectedQuantity = dailyChartState.chartQuantity,
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
//                onUpdateCurveValueAtIndicator = onUpdateDailyCurveValueAtIndicator
            )
            ChartUndoBotton(
                modifier = Modifier
                    .padding(
                        top = dimensionResource(id = R.dimen.diagram_frame_ver_padding) - 8.dp,
                        end = dimensionResource(id = R.dimen.diagram_frame_hor_padding) - 8.dp + endPadding
                    )
                    .align(Alignment.TopEnd),
                undoIconTint = undoIconTint,
                undoDiagramScaleMove = undoDailyDiagramScaleMove,
            )
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
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun HistoricalWeather(
    modifier: Modifier = Modifier,
    historicalChartState: HourlyChartState,
    historicalDailyChartState: DailyChartState,
    endPadding: Dp,
    appSettings: AppSettings,
    historicalHourlyData: List<HistoricHourlyChartDto>,
    historicalDailyData: List<HistoricDailyChartDto>,
    columnHeight: Float,
//    maxScroll: Float,
    detailState: DetailState,
    initialXAxisRange: ClosedFloatingPointRange<Float>,

    onRemoveChartQuantity: (HourlyWeatherQuantity) -> Unit,
    onAddChartQuantity: (HourlyWeatherQuantity) -> Unit,
    onAddYAxis: (Float, Float, Float, TextMeasurer, Int?, Float) -> Unit,
    onMoveHourlyDiagramAxis: (Offset) -> Unit,
    onScaleHourlyDiagramAxis: (Offset, Float, Float) -> Unit,
    onScaleBack: (Offset) -> Unit,
    onCalculateHorizontalBarSeparation: (Float, Float, Float, TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation: (Float, Float, Float, TextMeasurer) -> Pair<Int, Float>,
    onUpdateHourlyCurveValueAtIndicator: (Int, Float?) -> Unit,
    onUpdateHourlyThumbPosition: (Float) -> Unit,
    onAnimateHourlyDiagramAxis: (date: LocalDate) -> Unit,
    onReloadHistoricalData: () -> Unit,

    onUpdateDailyChartQuantity: (DailyWeatherQuantity) -> Unit,
    onUpdateDailyYAxis: (quantity: DailyWeatherQuantity, curveAnimatorInitialValue: Float) -> Unit,
    onMoveDailyDiagramAxis: (Offset) -> Unit,
    onScaleDailyDiagramAxis: (center: Offset, scaleX:Float, scaleY:Float) -> Unit,
//    onUpdateDailyCurveValueAtIndicator: (valueMin: Float, valueMax: Float) -> Unit,
    onScaleBackDaily: (Offset) -> Unit,
    onUpdateDailyThumbPosition: (Float) -> Unit,
    onAnimateDailyDiagramAxis: (date: LocalDate) -> Unit,
    onReloadHistoricalDailyData: () -> Unit,
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableStateOf(HistoricalTabs.Hourly) }
    val pagerState = rememberPagerState(
        initialPage = selectedTab.ordinal,
        pageCount = { 2 }
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = columnHeight.toDp(density))
//            .padding(top = 16.dp)
    ) {
        HistoricTopTabs(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = selectedTab.ordinal,
            onUpdateSelectedTab = {
                coroutineScope.launch {
                    selectedTab = it
                    pagerState.animateScrollToPage(it.ordinal)
                }
            }
        )
//        Spacer(modifier = Modifier.height(16.dp))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp),
            verticalAlignment = Alignment.Top,
            userScrollEnabled = false,
            beyondViewportPageCount = 1,
            pageSpacing = 8.dp,
        ) { page ->
            when (page) {
                0 -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        QuantityChooserRail(
                            modifier = Modifier.padding(end = endPadding),
                            appSettings = appSettings,
                            isHistorical = true,
                            selectedQuantities = historicalChartState.chartQuantities.toImmutableList(),
                            isAirQualityQuantities = false,
                            onRemoveChartQuantity = onRemoveChartQuantity,
                            onAddChartQuantity = onAddChartQuantity
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HourlyDiagramLegend(
                            modifier = Modifier.padding(start = 16.dp, /*end = 8.dp + endPadding*/),
                            chartQuantities = historicalChartState.chartQuantities.toImmutableList(),
                            dotsOnCurveVisible = historicalChartState.dotsOnCurveVisible,
                            curveValueAtIndicator = historicalChartState.curveValueAtIndicator.toImmutableList(),
                            appSettings = appSettings,
                            onRemoveChartQuantity = onRemoveChartQuantity
                        )
                        DataLoadingError(
                            modifier = Modifier,
                            visible = historicalChartState.dataLoadingStatus is MyResult.Error,
                            onReloadHistoricalData = onReloadHistoricalData
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensionResource(R.dimen.diagram_height))
                                .padding(horizontal = dimensionResource(id = R.dimen.whole_diagram_hor_padding))
                        ) {
                            HourlyChart(
                                modifier = Modifier.fillMaxSize(),
                                diagramHorPadding = dimensionResource(id = R.dimen.diagram_frame_hor_padding).toPx(density),
                                diagramVertPadding = dimensionResource(id = R.dimen.diagram_frame_ver_padding).toPx(density),
                                historicalHourlyData = historicalHourlyData.toImmutableList(),
                                curveAnimator = historicalChartState.curveAnimator,
                                chartQuantities = historicalChartState.chartQuantities,
                                settingsUpdated = historicalChartState.settingsUpdated,
                                xAxisAnimationTrigger = historicalChartState.xAxisAnimationTrigger,
                                xAxisStart = historicalChartState.xAxisStart,
                                xAxisEnd = historicalChartState.xAxisEnd,
                                yAxesStarts = historicalChartState.yAxesStarts,
                                yAxesEnds = historicalChartState.yAxesEnds,
                                chartGrid = historicalChartState.chartGrid,
                                chartTheme = historicalChartState.chartTheme,
                                dataLoadingStatus = historicalChartState.dataLoadingStatus,
                                curvesSmoothness = historicalChartState.curvesSmoothness,
                                curvesColor = historicalChartState.curvesColor,
                                dotsOnCurveVisible = historicalChartState.dotsOnCurveVisible,
                                curveShadowVisible = historicalChartState.curveShadowVisible,
                                sliderThumbPosition = historicalChartState.sliderThumbPosition,
                                sunRiseSetIconsVisible = historicalChartState.sunRiseSetIconsVisible,
                                weatherConditionIconsVisible = historicalChartState.weatherConditionIconsVisible,

                                appSettings = appSettings,
                                initialXAxisRange = initialXAxisRange,
                                onAddYAxis = onAddYAxis,
                                onMoveAxis = onMoveHourlyDiagramAxis,
                                onScaleAxis = onScaleHourlyDiagramAxis,
                                onScaleBack = onScaleBack,
                                onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                                onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation,
                                onUpdateCurveValueAtIndicator = onUpdateHourlyCurveValueAtIndicator
                            )
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
                            thumbPosition = historicalChartState.sliderThumbPosition,
                            onUpdateThumbPosition = onUpdateHourlyThumbPosition
                        )
                        DatePicker(
                            modifier = Modifier.height(104.dp),
                            yearListState = detailState.historyHourlyYearListState,
                            monthListState = detailState.historyHourlyMonthListState,
                            dayListState = detailState.historyHourlyDayListState,
                            theme = appSettings.theme,
                            onAnimateDiagramAxis = onAnimateHourlyDiagramAxis
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                1 -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DailyQuantityChooserRail(
                            modifier = Modifier.padding(end = endPadding),
                            allQuantities = DailyWeatherQuantity.entries
                                .filter { it.historicalState != HistoricalState.FORECAST_ONLY }.toImmutableList(),
                            selectedQuantity = historicalDailyChartState.chartQuantity,
                            onUpdateChartQuantity = onUpdateDailyChartQuantity
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DataLoadingError(
                            modifier = Modifier,
                            visible = historicalDailyChartState.dataLoadingStatus is MyResult.Error,
                            onReloadHistoricalData = onReloadHistoricalDailyData
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensionResource(R.dimen.diagram_height))
                                .padding(horizontal = dimensionResource(id = R.dimen.whole_diagram_hor_padding))
                        ) {
                            DailyChart(
                                diagramHorPadding = dimensionResource(id = R.dimen.diagram_frame_hor_padding).toPx(density),
                                diagramVertPadding = dimensionResource(id = R.dimen.diagram_frame_ver_padding).toPx(density),
//                                dailyChartData = dailyChartData.toImmutableList(),
                                historicDailyChartData = historicalDailyData.toImmutableList(),
                                appSettings = appSettings,
                                dailyChartState = historicalDailyChartState,
                                chartGrid = historicalChartState.chartGrid,
                                chartTheme = historicalChartState.chartTheme,
                                curveShadowVisible = historicalChartState.curveShadowVisible,
                                onUpdateDailyYAxis = onUpdateDailyYAxis,
                                onMoveAxis = onMoveDailyDiagramAxis,
                                onScaleAxis = onScaleDailyDiagramAxis,
                                onScaleBack = onScaleBackDaily,
                                onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                                onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation,
//                                onUpdateCurveValueAtIndicator = onUpdateDailyCurveValueAtIndicator
                            )
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
                            thumbPosition = historicalDailyChartState.sliderThumbPosition,
                            onUpdateThumbPosition = onUpdateDailyThumbPosition
                        )
                        DatePicker(
                            modifier = Modifier.height(104.dp),
                            yearListState = detailState.historyDailyYearListState,
                            monthListState = detailState.historyDailyMonthListState,
                            dayListState = detailState.historyDailyDayListState,
                            theme = appSettings.theme,
                            onAnimateDiagramAxis = onAnimateDailyDiagramAxis
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

