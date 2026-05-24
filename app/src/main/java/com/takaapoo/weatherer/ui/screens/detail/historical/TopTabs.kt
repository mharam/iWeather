package com.takaapoo.weatherer.ui.screens.detail.historical

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.ui.theme.Transparent

enum class HistoricalTabs {
    Hourly,
    Daily
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricTopTabs(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    onUpdateSelectedTab: (tab: HistoricalTabs) -> Unit
) {

    var tabHeight by remember { mutableIntStateOf(0) }

    SecondaryTabRow (
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = Transparent
    ) {
        HistoricalTabs.entries.forEachIndexed { index, tab ->
            Tab(
                modifier = Modifier.onSizeChanged {
                    tabHeight = it.height
                },
                selected = index == selectedTabIndex,
                onClick = {
                    onUpdateSelectedTab(HistoricalTabs.entries.find { it.ordinal == index } ?: HistoricalTabs.Hourly)
                },
                text = {
                    Text(
                        text =
                            buildAnnotatedString {
                                append(tab.name)
                            },
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp,
                        fontWeight = FontWeight(500),
                        color = if (index == selectedTabIndex) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            )
        }
    }
}
