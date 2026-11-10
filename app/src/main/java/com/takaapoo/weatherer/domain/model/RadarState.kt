package com.takaapoo.weatherer.domain.model

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import com.takaapoo.weatherer.data.remote.RadarSpecs
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.ui.screens.radar.RadarWeatherQuantity
import com.takaapoo.weatherer.ui.screens.radar.WeatherModels
import java.time.Clock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class RadarState(
    val radarInitializationStatus: MyResult<out String> = MyResult.Loading(),
    val radarSpecs: Map<WeatherModels, RadarSpecs> = emptyMap(),
    val quantity: RadarWeatherQuantity = RadarWeatherQuantity.TEMPERATURE,
    val quantityWeatherModels: Map<RadarWeatherQuantity, WeatherModels> =
        RadarWeatherQuantity.entries.associateWith { it.defaultModel },
    val quantityDialogVisible: Boolean = false,
    val loadingIndicatorVisible: Boolean = false,
    val quantityValue: Float? = null,
    val elevation: Float? = null,
    val settingsOpen: Boolean = false,
    val crossHairVisible: Boolean = true,
    val elevationVisible: Boolean = true,
    val clockGaugeVisible: Boolean = true,

    val clock: Clock = Clock.systemUTC(),
    val dayGaugeIndex: Int = 0,
    val dayGaugeNaturalIndex: Int = Int.MAX_VALUE / 2 + LocalDateTime.now(clock).dayOfWeek.value - 1,
    val clockGaugeRotation: Animatable<Float, AnimationVector1D> = Animatable(initialValue = 0f),
    val visibleDayIndex: Int = dayGaugeNaturalIndex,
    val clockGaugeNaturalRotation: Float = 0f,
    val clockGaugeTime: LocalDateTime = LocalDateTime.now(Clock.systemUTC())
        .truncatedTo(ChronoUnit.HOURS).plusHours(1),
    val clockGaugeDragStopped: Boolean = true,

    val noDataWarningVisible: Boolean = false,
    val snackbarMessage: String = "",
    val snackbarTrigger: Int = 0,

)