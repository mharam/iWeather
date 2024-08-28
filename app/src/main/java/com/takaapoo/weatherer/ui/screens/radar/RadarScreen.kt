package com.takaapoo.weatherer.ui.screens.radar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.takaapoo.weatherer.ui.screens.detail.LAST_PAGE_NUMBER
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RadarScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController()
) {
    Box(modifier = modifier){
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Radar"
        )
    }
}