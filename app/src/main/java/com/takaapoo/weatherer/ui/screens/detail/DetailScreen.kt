package com.takaapoo.weatherer.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.ui.viewModels.DetailViewModel
import kotlin.math.absoluteValue


@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    appSettings: AppSettings = AppSettings(),
    roomWidth: Float? = 0f,
    locationsCount: Int,
    initialPageNumber: Int = 0,
    navController: NavController = rememberNavController(),
    detailViewModel: DetailViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val detailState by detailViewModel.detailState.collectAsStateWithLifecycle()
    val chartStateList by detailViewModel.chartState.collectAsStateWithLifecycle()
    val dailyChartStateList by detailViewModel.dailyChartState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = initialPageNumber,
        pageCount = { locationsCount }
    )
    var offsetY by remember { mutableFloatStateOf(0f) }

    HorizontalPager(
        modifier = modifier
            .pointerInteropFilter {
                offsetY = it.y
                val touchPosition = Offset(it.x, it.y)
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
                    if (!detailState[pagerState.currentPage].dailyDiagramSettingRectangle.contains(touchPosition)) {
                        detailViewModel.updateDailyDiagramSettingOpen(
                            open = false,
                            pageIndex = pagerState.currentPage
                        )
                    }
                    return@pointerInteropFilter !detailState[pagerState.currentPage]
                        .dailyDiagramSettingRectangle.contains(touchPosition)
                }
                return@pointerInteropFilter false
                                  },
        state = pagerState,
        beyondBoundsPageCount = 2,
        pageSpacing = 8.dp,
    ) { page ->
        val width = pagerState.layoutInfo.viewportSize.width
        val endOffset = pagerState.endOffsetForPage(page)
        val pageShape = CirclePath(
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
                .clip(pageShape),
            roomWidth = roomWidth,
            navController = navController,
            detailViewModel = detailViewModel,
            chartState = chartStateList[page],
            dailyChartState = dailyChartStateList[page],
            pageNumber = page,
            appSettings = appSettings,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope
        )
    }

    BackHandler {
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