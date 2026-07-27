package com.takaapoo.weatherer.ui.screens.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.LocationsState
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.home.WeatherItemFake
import com.takaapoo.weatherer.utility.toDp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCardWeatherQuantityScreen(
    modifier: Modifier = Modifier,
    itemState: LocationsState,
    appSettings: AppSettings,
    cardWeatherQuantities: List<HourlyWeatherQuantity>,

    onUpdateHomeCardQuantityUnitsVisibility: (visible: Boolean) -> Unit,
    onUpdateCardWeatherQuantities: (quantity: HourlyWeatherQuantity, add: Boolean) -> Unit,
    onNavigateUp: () -> Unit
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val settingScrollState = rememberScrollState()
    val scrollState = rememberScrollState()
    var maxHeight by remember { mutableStateOf(0.dp) }
    val cardPartHeight = dimensionResource(R.dimen.home_card_max_height) + dimensionResource(R.dimen.home_card_spacing) * 2

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Cards",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val topBoxShape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = 32.dp,
            bottomEnd = 32.dp
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                    top = paddingValues.calculateTopPadding()
                )
                .onSizeChanged {
                    maxHeight = it.height.toDp(density)
                }
                .verticalScroll(scrollState)
                .nestedScroll(SettingsScrollConnection(scrollState, coroutineScope))
        ) {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = if (settingScrollState.canScrollBackward) 4.dp else 0.dp,
                        shape = topBoxShape
                    )
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.background, shape = topBoxShape)
                    .padding(
                        start = dimensionResource(R.dimen.home_card_spacing),
                        end = dimensionResource(R.dimen.home_card_spacing),
                        top = dimensionResource(R.dimen.home_card_spacing),
                        bottom = dimensionResource(R.dimen.home_card_spacing)
                    ),
                contentAlignment = Alignment.Center
            ) {
                WeatherItemFake(
                    itemState = itemState,
                    appSettings = appSettings,
                    windowRotation = remember { mutableFloatStateOf(0f) },
                    modifier = Modifier.height(dimensionResource(R.dimen.home_card_max_height)),
                    landscapeId = R.drawable.landscape1
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth().height(maxHeight)
                    .verticalScroll(settingScrollState)
            ) {
                SettingsRow1(
                    modifier = Modifier.fillMaxWidth(),
                    icon = R.drawable.length_24px,
                    title = "Units",
                    subTitle = if (appSettings.homeCardQuantityUnitsVisibility) "Visible" else "Invisible",
                    onClick = {
                        onUpdateHomeCardQuantityUnitsVisibility(!appSettings.homeCardQuantityUnitsVisibility)
                    },
                    content = {
                        Switch(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            checked = appSettings.homeCardQuantityUnitsVisibility,
                            onCheckedChange = onUpdateHomeCardQuantityUnitsVisibility
                        )
                    }
                )

                SettingsGroupTitle(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Weather quantities (${cardWeatherQuantities.size} out of 4)"
                )

                HourlyWeatherQuantity.entries.filter { it.homeCardNameId != null }.forEach { quantity ->
                    SettingsRow1(
                        modifier = Modifier.fillMaxWidth(),
                        icon = quantity.iconId,
                        title = stringResource(quantity.homeCardNameId!!),
                        onClick = {
                            onUpdateCardWeatherQuantities(quantity, !cardWeatherQuantities.contains(quantity))
                        },
                        content = {
                            Checkbox(
                                checked = cardWeatherQuantities.contains(quantity),
                                onCheckedChange = { onUpdateCardWeatherQuantities(quantity, it) }
                            )
                        }
                    )
                }
                Spacer(
                    modifier = Modifier.height(
                        paddingValues.calculateBottomPadding() + cardPartHeight + 16.dp
                                - scrollState.value.toDp(density)
                    )
                )
            }
        }
    }
}

class SettingsScrollConnection(
    val scrollState: ScrollState,
    val coroutineScope: CoroutineScope
) : NestedScrollConnection {
    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (scrollState.value < 0) {
            val consumeY = max(available.y, scrollState.value.toFloat())
            coroutineScope.launch {
                scrollState.scrollBy(-consumeY)
            }
            return Offset(0f, consumeY) // Consume for outer scroll
        }
        return Offset.Zero // Delegate to inner
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        return if (available.y > 0) super.onPostScroll(consumed, available, source)
        else available
    }
}