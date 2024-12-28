package com.takaapoo.weatherer.ui.screens.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.DetailState
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
    chartStateList: List<HourlyChartState>,
    dailyChartStateList: List<DailyChartState>,
    hourlyChartsData: List<List<HourlyChartDto>>,
    dailyChartsData: List<List<LocalDailyWeather>>,
    detailViewModel: DetailViewModel,
//    sharedTransitionScope: SharedTransitionScope,
//    animatedContentScope: AnimatedContentScope,
    onUpdateHomeSelectedItemIndex: (index: Int) -> Unit,
    onNavigateUp: (pageNumber: Int) -> Unit,
    onNavigateToAQDetail: (pageNumber: Int, scrollValue: Int) -> Unit
) {
//    val detailState by detailViewModel.detailState.collectAsStateWithLifecycle()
//    val chartStateList by detailViewModel.hourlyChartState.collectAsStateWithLifecycle()
//    val dailyChartStateList by detailViewModel.dailyChartState.collectAsStateWithLifecycle()
//
//    val hourlyChartsData by detailViewModel.locationsHourlyChartData
//        .collectAsStateWithLifecycle()
//    val dailyChartsData by detailViewModel.locationsDailyChartData
//        .collectAsStateWithLifecycle()

    var offsetY by remember { mutableFloatStateOf(0f) }
//    val scrollStates = List(size = pagerState.pageCount){ index ->
//        rememberScrollState(initial = detailState[index].scrollValue)
//    }

    LaunchedEffect(key1 = pagerState.currentPage) {
        onUpdateHomeSelectedItemIndex(pagerState.currentPage)
    }
    HorizontalPager(
        modifier = modifier
            .pointerInteropFilter {
                offsetY = it.y
                val touchPosition = Offset(it.x, it.y)
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
                }
                return@pointerInteropFilter false
                                  },
        state = pagerState,
        beyondViewportPageCount = 0,
        pageSpacing = 8.dp,
    ) { page ->
        val width = pagerState.layoutInfo.viewportSize.width
        val endOffset = pagerState.endOffsetForPage(page)
        val pageShape = CustomCircleShape(
            progress = 1f - endOffset.absoluteValue.coerceAtMost(1f),
            origin = Offset(
                width.toFloat(),
                offsetY,
            )
        )
        DetailPagerScreen(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val pageOffset = pagerState.offsetForPage(page)
                    translationX = size.width * pageOffset
                    shadowElevation = 8.dp.toPx()
                    shape = pageShape
                    clip = true

                    val scale = 1f + (pageOffset.absoluteValue * 0.3f)
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
                .clip(pageShape)
            ,
            detailState = detailState.getOrElse(index = page, defaultValue = { DetailState() }),
            detailViewModel = detailViewModel,
            hourlyChartState = chartStateList.getOrElse(index = page, defaultValue = { HourlyChartState() }),
            dailyChartState = dailyChartStateList.getOrElse(index = page, defaultValue = { DailyChartState() }),
            hourlyChartData = hourlyChartsData.getOrElse(
                index = page,
                defaultValue = { emptyList() }
            ),
            dailyChartData = dailyChartsData.getOrElse(
                index = page,
                defaultValue = { emptyList() }
            ),
            pageNumber = page,
//            scrollState = scrollStates[page],
            appSettings = appSettings,
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
            onNavigateToAQDetail = remember { onNavigateToAQDetail }
//            sharedTransitionScope = sharedTransitionScope,
//            animatedContentScope = animatedContentScope
        )
    }

//    BackHandler {
//        detailViewModel.updateNavigateBack(
//            state = true,
//            pageIndex = pagerState.currentPage
//        )
//    }
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