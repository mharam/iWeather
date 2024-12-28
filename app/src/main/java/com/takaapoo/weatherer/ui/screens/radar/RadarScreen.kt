package com.takaapoo.weatherer.ui.screens.radar

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun RadarScreen(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier){
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Radar"
        )
    }
}