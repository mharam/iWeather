package com.takaapoo.weatherer.ui.screens.detail.historical

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R

@Composable
fun DataLoadingError(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onReloadHistoricalData: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter =  fadeIn(animationSpec = tween(delayMillis = 300)) +
                expandVertically(
                    animationSpec = tween(delayMillis = 300),
                    expandFrom = Alignment.Top
                ),
        exit = fadeOut(animationSpec = tween(delayMillis = 1000)) +
                shrinkVertically(
                    animationSpec = tween(delayMillis = 1000),
                    shrinkTowards = Alignment.Top
                )
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.error_24px),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error)
                )
                Spacer(Modifier.width(8.dp))
//                                    val errorResult = (historicalChartState.dataLoadingStatus as? MyResult.Error)
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Retrieving data from server failed! Check your internet connection and try again later.",
                    fontFamily = FontFamily.Default,
                    maxLines = 3,
                    fontSize = 14.sp,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onReloadHistoricalData
                ) {
                    Icon(
                        painter = painterResource(R.drawable.refresh_24px),
                        contentDescription = "Retry"
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}