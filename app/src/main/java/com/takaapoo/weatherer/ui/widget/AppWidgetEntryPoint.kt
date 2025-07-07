package com.takaapoo.weatherer.ui.widget

import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.use_case.GetHourlyChartDataUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppWidgetEntryPoint {
    fun getPreferenceRepository(): PreferenceRepository
    fun getLocalWeatherRepository(): LocalWeatherRepository
    fun getHourlyChartDataUseCase(): GetHourlyChartDataUseCase
}
