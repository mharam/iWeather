package com.takaapoo.weatherer.ui.screens.detail.historical

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.theme.timeFontFamily
import com.takaapoo.weatherer.utility.BorderedText
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DatePicker(
    modifier: Modifier = Modifier,
    yearListState: LazyListState,
    monthListState: LazyListState,
    dayListState: LazyListState,
    theme: AppTheme,
    onAnimateDiagramAxis: (date: LocalDate) -> Unit
) {
    val startYear = 1940
    val gaugeBorderRadius = 4.dp
    val gaugeHeight = 32.dp
    val dayGaugeResource = when (theme) {
        AppTheme.LIGHT -> R.drawable.day_gauge_light
        AppTheme.DARK -> R.drawable.day_gauge_dark
        AppTheme.SYSTEM -> {
            if (isSystemInDarkTheme()) R.drawable.day_gauge_dark
            else R.drawable.day_gauge_light
        }
    }

    Row(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(width = 64.dp, height = gaugeHeight)
                ){
                    Image(
                        painter = painterResource(dayGaugeResource),
                        modifier = Modifier.align(Alignment.Center).fillMaxSize(),
                        contentDescription = "Day display",
                        contentScale = ContentScale.FillBounds
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                            .align(Alignment.Center),
                        state = yearListState,
                        flingBehavior = rememberSnapFlingBehavior(lazyListState = yearListState)
                    ) {
                        items(
                            items = (startYear .. LocalDate.now().year).toList(),
                            key = { it }
                        ) {
                            BorderedText(
                                modifier = Modifier.fillMaxWidth()
                                    .height(gaugeHeight)
                                    .wrapContentSize(
                                        align = Alignment.Center,
                                        unbounded = true
                                    ),
                                fillColor = MaterialTheme.colorScheme.onBackground,
                                strokeWidth = 1f,
                                text = AnnotatedString(it.toString()),
                                fontFamily = timeFontFamily,
                                fontSize = 16.sp,
                                alignment = Alignment.Center
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(width = 96.dp, height = gaugeHeight)
                ){
                    Image(
                        painter = painterResource(dayGaugeResource),
                        modifier = Modifier.align(Alignment.Center).fillMaxSize(),
                        contentDescription = "Day display",
                        contentScale = ContentScale.FillBounds
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().align(Alignment.Center),
                        state = monthListState,
                        flingBehavior = rememberSnapFlingBehavior(lazyListState = monthListState)
                    ) {
                        val months = Month.entries.map {
                            it.getDisplayName(TextStyle.FULL, Locale.US)
                        }
                        items(
                            items = months,
                            key = { months.indexOf(it) }
                        ) {
                            BorderedText(
                                modifier = Modifier.fillMaxWidth()
                                    .height(gaugeHeight)
                                    .wrapContentSize(
                                        align = Alignment.Center,
                                        unbounded = true
                                    ),
                                fillColor = MaterialTheme.colorScheme.onBackground,
                                strokeWidth = 1f,
                                text = AnnotatedString(it.toString()),
                                fontFamily = timeFontFamily,
                                fontSize = 16.sp,
                                alignment = Alignment.Center
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(width = 48.dp, height = gaugeHeight)
                ){
                    Image(
                        painter = painterResource(dayGaugeResource),
                        modifier = Modifier.align(Alignment.Center).fillMaxSize(),
                        contentDescription = "Day display",
                        contentScale = ContentScale.FillBounds
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().align(Alignment.Center),
                        state = dayListState,
                        flingBehavior = rememberSnapFlingBehavior(lazyListState = dayListState)
                    ) {
                        val daysCount = Month.of(monthListState.firstVisibleItemIndex + 1)
                            .length(Year.isLeap(startYear + yearListState.firstVisibleItemIndex.toLong()))
                        items(
                            items = (1 .. daysCount).toList(),
                            key = { it }
                        ) {
                            BorderedText(
                                modifier = Modifier.fillMaxWidth()
                                    .height(gaugeHeight)
                                    .wrapContentSize(
                                        align = Alignment.Center,
                                        unbounded = true
                                    ),
                                fillColor = MaterialTheme.colorScheme.onBackground,
                                strokeWidth = 1f,
                                text = AnnotatedString(it.toString()),
                                fontFamily = timeFontFamily,
                                fontSize = 16.sp,
                                alignment = Alignment.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Button (
                    onClick = {
                        onAnimateDiagramAxis(LocalDate.of(
                            startYear + yearListState.firstVisibleItemIndex,
                            monthListState.firstVisibleItemIndex + 1,
                            dayListState.firstVisibleItemIndex + 1
                        ))
                    }
                ) {
                    Text(
                        text = "Go!",
                        fontFamily = FontFamily.Default
                    )
                }
            }

        }

    }

}


@Preview(showBackground = true)
@Composable
fun DatePickerPreview() {
    WeathererTheme {
        DatePicker(
            modifier = Modifier.height(300.dp),
            yearListState = rememberLazyListState(),
            monthListState = rememberLazyListState(),
            dayListState = rememberLazyListState(),
            theme = AppTheme.LIGHT,
            onAnimateDiagramAxis = {

            }
        )
    }
}