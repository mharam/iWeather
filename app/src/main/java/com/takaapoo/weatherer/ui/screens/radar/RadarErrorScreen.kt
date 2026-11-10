package com.takaapoo.weatherer.ui.screens.radar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R

@Composable
fun RadarErrorScreen(
    modifier: Modifier = Modifier,
    onReloadRadarData: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Icon(
            modifier = Modifier.size(48.dp),
            painter = painterResource(R.drawable.cloud_off),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "Error"
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading radar data failed!",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight(500),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onReloadRadarData) {
            Text(
                text = "Retry",
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Default
            )
        }
    }
}