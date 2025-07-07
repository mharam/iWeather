package com.takaapoo.weatherer.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSettingsScreen(
    modifier: Modifier = Modifier,
    appSettings: AppSettings,
    singlePane: Boolean,
    onUpdateLengthUnit: (unit: Length) -> Unit,
    onUpdateTemperatureUnit: (unit: Temperature) -> Unit,
    onUpdatePressureUnit: (unit: Pressure) -> Unit,
    onUpdateSpeedUnit: (unit: Speed) -> Unit,

    onNavigateUp: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Unit",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    if (singlePane) {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
        ) {
            SettingsRow2(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.length_24px,
                title = "Length",
                subTitle = stringResource(appSettings.lengthUnit.description),
                content = {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        val items = Length.entries
                        items.forEachIndexed { index, unit ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size),
                                onClick = { onUpdateLengthUnit(unit) },
                                selected = unit == appSettings.lengthUnit
                            ) {
                                Text(
                                    text = unit.title,
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            )
            SettingsRow2(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.temperature,
                title = "Temperature",
                subTitle = stringResource(appSettings.temperatureUnit.description),
                content = {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ){
                        val items = Temperature.entries
                        items.forEachIndexed { index, unit ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size),
                                onClick = { onUpdateTemperatureUnit(unit) },
                                selected = unit == appSettings.temperatureUnit
                            ) {
                                Text(
                                    text = unit.title,
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            )
            SettingsRow2(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.pressure,
                title = "Pressure",
                subTitle = stringResource(appSettings.pressureUnit.description),
                content = {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ){
                        val items = Pressure.entries
                        items.forEachIndexed { index, unit ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size),
                                onClick = { onUpdatePressureUnit(unit) },
                                selected = unit == appSettings.pressureUnit
                            ) {
                                Text(
                                    text = unit.title,
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            )
            SettingsRow2(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.speed_24px,
                title = "Speed",
                subTitle = stringResource(appSettings.speedUnit.description),
                content = {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ){
                        val items = Speed.entries
                        items.forEachIndexed { index, unit ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size),
                                onClick = { onUpdateSpeedUnit(unit) },
                                selected = unit == appSettings.speedUnit
                            ) {
                                Text(
                                    text = unit.title,
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}