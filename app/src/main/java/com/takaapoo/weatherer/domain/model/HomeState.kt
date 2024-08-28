package com.takaapoo.weatherer.domain.model

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.lazy.LazyListState
import com.takaapoo.weatherer.domain.MyResult
import java.time.LocalDateTime


data class HomeState(
    val zoom: Boolean = false,
    val zoomCenterX: Float = 0f,
    val zoomCenterY: Float = 0f,
    val selectedItemIndex: Int = 0,
    val selectedItemOffset: Int = 0,
    val selectedLocationId: Int = 0,
    val deleteDialogVisible: Boolean = false,
    val deleteDialogLocationName: String = "",
    val deleteDialogLocationId: Int = 0,
    val toBeDeletedLocationId: Int = 0,
    val editDialogVisible: Boolean = false,
    val editDialogNewLocationName: String = "",
    val editDialogOldLocationName: String = "",
    val editDialogLocationId: Int = 0,
    val nameAlreadyExists: Boolean = false,
    val editNameResult: MyResult<String> = MyResult.Loading(),
    val isRefreshing: Boolean = false,
    val clockGaugeRotation: Animatable<Float, AnimationVector1D> = Animatable(initialValue = 0f),
    val clockGaugeNaturalRotation: Float = 0f,
    val dayGaugeIndex: Int = 0,
    val dayGaugeNaturalIndex: Int = Int.MAX_VALUE / 2 + LocalDateTime.now().dayOfWeek.value - 1,
    val dayListState: LazyListState = LazyListState(firstVisibleItemIndex = dayGaugeNaturalIndex),
    val visibleDayIndex: Int = dayGaugeNaturalIndex,
    val navigatedToDetailScreen: Boolean = false
)