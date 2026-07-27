package com.takaapoo.weatherer.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.domain.model.HistoricDailyChartDto
import com.takaapoo.weatherer.domain.model.HistoricHourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.ui.viewModels.DetailViewModel
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    appSettings: AppSettings = AppSettings(),
    pagerState: PagerState,
    detailState: List<DetailState>,
    chartStateList: List<State<HourlyChartState>>,
    dailyChartStateList: List<DailyChartState>,
    hourlyChartsData: List<List<HourlyChartDto>>,
    dailyChartsData: List<List<LocalDailyWeather>>,
    historicalHourlyChartData: List<List<HistoricHourlyChartDto>>,
    historicalDailyChartData: List<List<HistoricDailyChartDto>>,
    historicalChartStateList: List<HourlyChartState>,
    historicalDailyChartStateList: List<DailyChartState>,
    detailViewModel: DetailViewModel,
    singlePane: Boolean = true,
    trialPeriodExpired: Boolean?,
//    dataReady: Boolean = true,
//    sharedTransitionScope: SharedTransitionScope,
//    animatedContentScope: AnimatedContentScope,
    onUpdateHomeSelectedItemIndex: (index: Int) -> Unit,
    onNavigateUp: (pageNumber: Int) -> Unit,
    onNavigateToAQDetail: (pageNumber: Int, scrollValue: Int) -> Unit,
    onNavigateToDonateScreen: () -> Unit,
    onNavigateToLoginScreen: () -> Unit
) {

    var offsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(key1 = pagerState.currentPage) {
        onUpdateHomeSelectedItemIndex(pagerState.currentPage)
        detailViewModel.updateDetailPagerIndex(pagerState.currentPage)
    }
    HorizontalPager(
        modifier = modifier
            .pointerInteropFilter {
                offsetY = it.y
                /*val touchPosition = Offset(it.x, it.y)
                if (detailState.isNotEmpty()) {
                    if (detailState[pagerState.currentPage].hourlyDiagramSettingOpen) {
                        if (!detailState[pagerState.currentPage].hourlyDiagramSettingRectangle.contains(touchPosition)) {
                            detailViewModel.updateHourlyDiagramSettingOpen(
                                open = false,
                                pageIndex = pagerState.currentPage
                            )
                        }
                        return@pointerInteropFilter !detailState[pagerState.currentPage]
                            .hourlyDiagramSettingRectangle.contains(touchPosition)
                    }
                    if (detailState[pagerState.currentPage].dailyDiagramSettingOpen) {
                        if (!detailState[pagerState.currentPage].dailyDiagramSettingRectangle.contains(
                                touchPosition
                            )
                        ) {
                            detailViewModel.updateDailyDiagramSettingOpen(
                                open = false,
                                pageIndex = pagerState.currentPage
                            )
                        }
                        return@pointerInteropFilter !detailState[pagerState.currentPage]
                            .dailyDiagramSettingRectangle.contains(touchPosition)
                    }
                }*/
                return@pointerInteropFilter false
                                  }
        ,
        state = pagerState,
        beyondViewportPageCount = 0,
        pageSpacing = 8.dp,
        userScrollEnabled = singlePane
    ) { page ->
        DetailPagerScreen(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val width = pagerState.layoutInfo.viewportSize.width
                    val endOffset = pagerState.endOffsetForPage(page)
                    val pageShape = CustomCircleShape(
                        progress = 1f - endOffset.absoluteValue.coerceAtMost(1f),
                        origin = Offset(
                            width.toFloat(),
                            offsetY,
                        )
                    )
                    val pageOffset = pagerState.offsetForPage(page)
                    translationX = size.width * pageOffset
                    shadowElevation = 8.dp.toPx()
                    shape = pageShape
                    clip = true

                    val scale = 1f + (pageOffset.absoluteValue * 0.2f)
                    scaleX = scale
                    scaleY = scale

                    val startOffset = pagerState.startOffsetForPage(page)
                    alpha = (1.75f * (1f - startOffset)).coerceIn(0f, 1f)
                }
            /*.drawWithContent {
                    val progress = 1f - endOffset.absoluteValue.coerceAtMost(1f)
                    val origin = Offset(size.width, offsetY)
                    val center = Offset(
                        x = size.center.x - ((size.center.x - origin.x) * (1f - progress)),
                        y = size.center.y - ((size.center.y - origin.y) * (1f - progress)),
                    )
                    val radius = (sqrt(size.height * size.height + size.width * size.width) * .5f) * progress
                    val pageOffset = pagerState.offsetForPage(page)
                    val scale = 1f + (pageOffset.absoluteValue * 0.3f)

                    clipPath(
                        path = Path().apply {
                            addOval(Rect(center = center, radius = radius))
                        }
                    ){
                        scale(scale = scale) {
                            this@drawWithContent.drawContent()
                        }
                    }
                }*/
//                .clip(pageShape)
            ,
            detailState = detailState.getOrElse(index = page, defaultValue = { DetailState() }),
            detailViewModel = detailViewModel,
            hourlyChartState = chartStateList.getOrElse(
                index = page,
                defaultValue = { remember { mutableStateOf(HourlyChartState()) } }
            ),
            dailyChartState = dailyChartStateList.getOrElse(
                index = page,
                defaultValue = { DailyChartState() }),
            historicalChartState = historicalChartStateList.getOrElse(
                index = page,
                defaultValue = { HourlyChartState(chartQuantities = emptyList()) }
            ),
            historicalDailyChartState = historicalDailyChartStateList.getOrElse(
                index = page,
                defaultValue = { DailyChartState() }
            ),
            hourlyChartData = hourlyChartsData.getOrElse(
                index = page,
                defaultValue = { emptyList() }
            ),
            dailyChartData = dailyChartsData.getOrElse(
                index = page,
                defaultValue = { emptyList() }
            ),
            historicalHourlyData = historicalHourlyChartData.getOrElse(
                index = page,
                defaultValue = { emptyList() }
            ),
            historicalDailyData = historicalDailyChartData.getOrElse(
                index = page,
                defaultValue = { emptyList() }
            ),
            pageNumber = page,
            singlePane = singlePane,
            appSettings = appSettings,
            initialLandscape = detailViewModel.initialLandScape,
            trialPeriodExpired = trialPeriodExpired,
//            dataReady = dataReady,

            onResetScrollStates = {
                detailState.filterIndexed { index, _ ->
                    index != page
                }.map { it.scrollState }.forEach {
                    launch {
                        it.scrollTo(0)
                    }
                }
            },
            onNavigateUp = remember { onNavigateUp },
            onNavigateToAQDetail = remember { onNavigateToAQDetail },
            onNavigateToDonateScreen = remember { onNavigateToDonateScreen },
            onNavigateToLoginScreen = remember { onNavigateToLoginScreen }
//            sharedTransitionScope = sharedTransitionScope,
//            animatedContentScope = animatedContentScope
        )
    }

    BackHandler(enabled = singlePane) {
        detailViewModel.updateNavigateBack(
            state = true,
            pageIndex = pagerState.currentPage
        )
    }
}

// ACTUAL OFFSET
@OptIn(ExperimentalFoundationApi::class)
fun PagerState.offsetForPage(page: Int) = (currentPage - page) + currentPageOffsetFraction

// OFFSET ONLY FROM THE LEFT
@OptIn(ExperimentalFoundationApi::class)
fun PagerState.startOffsetForPage(page: Int): Float {
    return offsetForPage(page).coerceAtLeast(0f)
}

// OFFSET ONLY FROM THE RIGHT
@OptIn(ExperimentalFoundationApi::class)
fun PagerState.endOffsetForPage(page: Int): Float {
    return offsetForPage(page).coerceAtMost(0f)
}