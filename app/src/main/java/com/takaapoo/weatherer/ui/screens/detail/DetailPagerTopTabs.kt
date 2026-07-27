package com.takaapoo.weatherer.ui.screens.detail

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.timeFontFamily
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class DetailTabs {
    Now,
    Hourly,
    Daily,
    History
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun detailPagerTopTabs(
    modifier: Modifier = Modifier,
    localDateTime: LocalDateTime,
    selectedTabIndex: Int,
    onUpdateSelectedTabIndex: (tab: DetailTabs) -> Unit
): Int {
    val infiniteTransition = rememberInfiniteTransition(label = "clock")
    val clockColonVisibility by infiniteTransition.animateValue(
        initialValue = 0f,
        targetValue = 1f,
        typeConverter = Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "clock colon"
    )
    var tabHeight by remember { mutableIntStateOf(0) }

    PrimaryScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = Transparent
    ) {
        DetailTabs.entries.forEachIndexed { index, tab ->
            Tab(
                modifier = Modifier.onSizeChanged {
                    tabHeight = it.height
                },
                selected = index == selectedTabIndex,
                onClick = {
                    onUpdateSelectedTabIndex(DetailTabs.entries.find { it.ordinal == index } ?: DetailTabs.Now)
                },
                text = {
                    val commonTextStyle = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp,
                        fontWeight = FontWeight(500),
                        color = if (index == selectedTabIndex) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (index == 0)
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val localTime = localDateTime.toLocalTime()
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            fontFamily = FontFamily.Default
                                        )
                                    ){ append(tab.name) }
                                    withStyle(
                                        style = SpanStyle(
                                            fontFamily = timeFontFamily
                                        )
                                    ){
                                        append(" (${localTime.format(DateTimeFormatter.ofPattern("HH"))}")
                                    }
                                },
                                style = commonTextStyle
                            )
                            Text(
                                modifier = Modifier.graphicsLayer {
                                    alpha = if (clockColonVisibility <= 0.5f) 1f else 0f
                                },
                                text = buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            fontFamily = timeFontFamily
                                        )
                                    ){
                                        append("\u200C:\u200C")
                                    }
                                },
                                style = commonTextStyle
                            )
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            fontFamily = timeFontFamily
                                        )
                                    ){
                                        append("${localTime.format(DateTimeFormatter.ofPattern("mm"))})")
                                    }
                                },
                                style = commonTextStyle
                            )
                        }
                    else
                        Text(
                            text = tab.name,
                            style = commonTextStyle
                        )
                }
            )
        }
    }
    return tabHeight
}

