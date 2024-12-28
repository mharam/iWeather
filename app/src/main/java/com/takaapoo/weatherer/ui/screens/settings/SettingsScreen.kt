package com.takaapoo.weatherer.ui.screens.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.domain.model.SettingsState
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.theme.customColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    appSettings: AppSettings,
    settingsState: SettingsState,
    onUpdateSilence: (silent: Boolean) -> Unit,
    onUpdateScreenOn: (screenOn: Boolean) -> Unit,
    onUpdateThemeDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateTheme: (theme: AppTheme) -> Unit,
    onUpdateTemperatureUnit: (unit: Temperature) -> Unit,
    onUpdateLengthUnit: (unit: Length) -> Unit,
    onUpdatePressureUnit: (unit: Pressure) -> Unit,
    onUpdateSpeedUnit: (unit: Speed) -> Unit,
    onUpdateClockGaugeVisibility: (visible: Boolean) -> Unit,

    onNavigateUp: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (settingsState.themeDialogVisibility){
            ChooseThemeDialog(
                appTheme = appSettings.theme,
                onDismiss = {
                    onUpdateThemeDialogVisibility(false)
                },
                onConfirmation = {
                    onUpdateTheme(it)
                    onUpdateThemeDialogVisibility(false)
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsGroupTitle(
                modifier = Modifier.fillMaxWidth(),
                title = "General"
            )
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.volume_off_24px,
                title = "Silent",
                subTitle = "Silence the app",
                onClick = { onUpdateSilence(!appSettings.silent) },
                content = {
                    Switch(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        checked = appSettings.silent,
                        onCheckedChange = onUpdateSilence
                    )
                }
            )
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.smartphone_24px,
                title = "Screen On",
                subTitle = "Keep screen On",
                onClick = { onUpdateScreenOn(!appSettings.screenOn) },
                content = {
                    Switch(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        checked = appSettings.screenOn,
                        onCheckedChange = onUpdateScreenOn
                    )
                }
            )
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.theme,
                title = "Theme",
                subTitle = stringResource(appSettings.theme.description),
                onClick = { onUpdateThemeDialogVisibility(true) },
                content = {}
            )
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.clock_icon,
                title = "Time manipulator",
                subTitle = if (appSettings.clockGaugeVisibility) "Visible" else "Invisible",
                onClick = { onUpdateClockGaugeVisibility(!appSettings.clockGaugeVisibility) },
                content = {
                    Switch(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        checked = appSettings.clockGaugeVisibility,
                        onCheckedChange = onUpdateClockGaugeVisibility
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            SettingsGroupTitle(
                modifier = Modifier.fillMaxWidth(),
                title = "Unit"
            )
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

@Composable
fun SettingsGroupTitle(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
        modifier = modifier
            .height(56.dp)
            .padding(start = 72.dp, top = 16.dp, bottom = 8.dp),
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun SettingsRow1(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String,
    subTitle: String? = null,
    onClick: () -> Unit,
    content: @Composable (() -> Unit)
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ){
        Icon(
            modifier = Modifier
                .size(48.dp)
                .padding(12.dp),
            painter = painterResource(icon),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            subTitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.customColorScheme.lowEmphasisText
                )
            }
        }
        Spacer(modifier = Modifier
            .width(16.dp)
            .weight(1f))
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsRow2(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String,
    subTitle: String? = null,
    content: @Composable (() -> Unit)
) {
    FlowRow(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        Row(
//            modifier = Modifier.fillMaxWidth()
        ){
            Icon(
                modifier = Modifier
                    .size(48.dp)
                    .padding(12.dp),
                painter = painterResource(icon),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                subTitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.customColorScheme.lowEmphasisText
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            content()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    WeathererTheme {
        SettingsScreen(
            appSettings = AppSettings(),
            settingsState = SettingsState(),
            onUpdateSilence = {},
            onNavigateUp = {},
            onUpdateThemeDialogVisibility = {},
            onUpdateTheme = { _ -> },
            onUpdateTemperatureUnit = {},
            onUpdateLengthUnit = {},
            onUpdatePressureUnit = {},
            onUpdateSpeedUnit = {},
            onUpdateScreenOn = {},
            onUpdateClockGaugeVisibility = {}
        )
    }
}

