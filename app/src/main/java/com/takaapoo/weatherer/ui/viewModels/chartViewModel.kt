package com.takaapoo.weatherer.ui.viewModels

import androidx.lifecycle.ViewModel
import com.takaapoo.weatherer.domain.model.ChartState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChartViewModel: ViewModel() {

    private val _chartState = MutableStateFlow(ChartState())
    val chartState = _chartState.asStateFlow()




}