package com.takaapoo.weatherer.ui.viewModels

import androidx.lifecycle.ViewModel
import com.takaapoo.weatherer.domain.model.HourlyChartState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChartViewModel: ViewModel() {

    private val _chartState = MutableStateFlow(HourlyChartState())
    val chartState = _chartState.asStateFlow()




}