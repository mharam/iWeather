package com.takaapoo.weatherer.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.LocationsState
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.home.WeatherItemFake

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCardWeatherQuantityScreen(
    modifier: Modifier = Modifier,
    itemState: LocationsState,
    appSettings: AppSettings,
    cardWeatherQuantities: List<WeatherQuantity>,

    onUpdateHomeCardQuantityUnitsVisibility: (visible: Boolean) -> Unit,
    onUpdateCardWeatherQuantities: (quantity: WeatherQuantity, add: Boolean) -> Unit,
    onNavigateUp: () -> Unit
) {
    val scrollState = rememberScrollState()

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
        ) {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = if (scrollState.canScrollBackward) 4.dp else 0.dp,
                        shape = topBoxShape
                    )
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.background, shape = topBoxShape)
                    .padding(
                        start = dimensionResource(R.dimen.home_card_spacing),
                        end = dimensionResource(R.dimen.home_card_spacing),
                        top = dimensionResource(R.dimen.home_card_spacing),
                        bottom = dimensionResource(R.dimen.home_card_spacing)
                    )
            ) {
                WeatherItemFake(
                    itemState = itemState,
                    pageNumber = -1,
                    appSettings = appSettings,
                    windowRotation = 0f,
                    modifier = Modifier.height(dimensionResource(R.dimen.home_card_max_height)),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
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

                WeatherQuantity.entries.filter { it.homeCardNameId != null }.forEach { quantity ->
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
                Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding() + 16.dp))
            }
        }
    }
}