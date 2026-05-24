package com.takaapoo.weatherer.domain.model

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Immutable
import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.ui.screens.detail.DetailTabs
import java.time.LocalDateTime
import java.time.ZoneId

@Immutable
data class DetailState(
    val locationName: String = "",
    val selectedTab: DetailTabs = DetailTabs.Now,
    val detailPagerIndex: Int? = null,
    val maxTemperature: Float? = null,
    val minTemperature: Float? = null,
    val currentDayHourlyWeather: List<LocalHourlyWeather> = emptyList(),
    val currentDayHourlyAirQuality: List<LocalAirQuality> = emptyList(),
    val sunRise: String? = null,
    val sunSet: String? = null,
    val utcOffset: Long? = 0,
    val localDateTime: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")),
    val currentPictureUrl: String = "",
    val lastPicId: Int? = null,
    val picName: String = "1_1_1",

    val targetX: Float = 0f,
    val navigateBack: Boolean = false,
    val hourlyDiagramSettingOpen: Boolean = false,
    val dailyDiagramSettingOpen: Boolean = false,
    val chooseDiagramThemeDialogVisible: Boolean = false,
    val scrollState: ScrollState = ScrollState(initial = 0),
    val topBarState: TopAppBarState =
        TopAppBarState(-Float.MAX_VALUE, 0f, 0f),

    val historyHourlyYearListState: LazyListState = LazyListState(firstVisibleItemIndex = 60),
    val historyHourlyMonthListState: LazyListState = LazyListState(),
    val historyHourlyDayListState: LazyListState = LazyListState(),

    val historyDailyYearListState: LazyListState = LazyListState(firstVisibleItemIndex = 60),
    val historyDailyMonthListState: LazyListState = LazyListState(),
    val historyDailyDayListState: LazyListState = LazyListState(),
) {
    fun scrollEnabled() = (!historyHourlyYearListState.isScrollInProgress ||
            !historyHourlyYearListState.canScrollForward || !historyHourlyYearListState.canScrollBackward) &&
            (!historyHourlyMonthListState.isScrollInProgress ||
            !historyHourlyMonthListState.canScrollForward || !historyHourlyMonthListState.canScrollBackward) &&
            (!historyHourlyDayListState.isScrollInProgress ||
            !historyHourlyDayListState.canScrollForward || !historyHourlyDayListState.canScrollBackward) &&

            (!historyDailyYearListState.isScrollInProgress ||
                    !historyDailyYearListState.canScrollForward || !historyDailyYearListState.canScrollBackward) &&
            (!historyDailyMonthListState.isScrollInProgress ||
                    !historyDailyMonthListState.canScrollForward || !historyDailyMonthListState.canScrollBackward) &&
            (!historyDailyDayListState.isScrollInProgress ||
                    !historyDailyDayListState.canScrollForward || !historyDailyDayListState.canScrollBackward)

}
