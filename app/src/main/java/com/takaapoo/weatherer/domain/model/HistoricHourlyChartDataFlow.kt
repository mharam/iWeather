package com.takaapoo.weatherer.domain.model

import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity

data class HistoricHourlyChartDataFlow( // Used in historicHourlyChartDataFlow function in DetailViewModel
    val chartQuantities: List<HourlyWeatherQuantity> = emptyList(),
    val xAxisStart: Float = 0f,
    val xAxisEnd: Float = 0f,
    val appSettings: AppSettings = AppSettings(),
    val detailPagerIndex: Int? = null
)

data class HistoricHourlyChartStatePart(
    val chartQuantities: List<HourlyWeatherQuantity> = emptyList(),
    val xAxisStart: Float = 0f,
    val xAxisEnd: Float = 0f,
    val awaker: Int = 0
)

data class HistoricDailyChartDataFlow(
    val chartQuantity: DailyWeatherQuantity = DailyWeatherQuantity.TEMPERATUREMINMAX,
    val xAxisStart: Float = 0f,
    val xAxisEnd: Float = 0f,
    val appSettings: AppSettings = AppSettings(),
    val detailPagerIndex: Int? = null
)

data class HistoricDailyChartStatePart(
    val chartQuantity: DailyWeatherQuantity = DailyWeatherQuantity.TEMPERATUREMINMAX,
    val xAxisStart: Float = 0f,
    val xAxisEnd: Float = 0f,
    val awaker: Int = 0
)
