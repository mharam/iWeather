package com.takaapoo.weatherer.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.domain.model.SettingsPaneContent
import com.takaapoo.weatherer.domain.model.SettingsState
import com.takaapoo.weatherer.ui.navigation.SettingsCard
import com.takaapoo.weatherer.ui.screens.settings.dialogs.ChooseThemeDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(
    modifier: Modifier = Modifier,
    appSettings: AppSettings,
    singlePane: Boolean,
    settingsState: SettingsState,

    onUpdateSilence: (silent: Boolean) -> Unit,
    onUpdateVibration: (vibrate: Boolean) -> Unit,
    onUpdateScreenOn: (screenOn: Boolean) -> Unit,
    onUpdateThemeDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateDynamicTheme: (dynamic: Boolean) -> Unit,
    onUpdateClockGaugeVisibility: (visible: Boolean) -> Unit,
//    onUpdateHomeCardDialogVisibility: (visible: Boolean) -> Unit,
    onUpdatePaneContent: (content: SettingsPaneContent, previousContent: SettingsPaneContent?) -> Unit,
    onNavigateToScreen: (route: NavKey) -> Unit,
    onUpdateTheme: (theme: AppTheme) -> Unit,

    onNavigateUp: () -> Unit
) {
    val scrollState = rememberScrollState()
    when {
        settingsState.themeDialogVisibility -> {
            ChooseThemeDialog(
                appTheme = appSettings.theme,
                onDismiss = {
                    onUpdateThemeDialogVisibility(false)
                },
                onConfirmation = {
                    onUpdateTheme(it)
//                    onUpdateThemeDialogVisibility(false)
                }
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "General",
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
        val layoutDirection = LocalLayoutDirection.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                    top = paddingValues.calculateTopPadding()
                )
                .verticalScroll(scrollState),
        ) {
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

            SettingsGroupTitle(
                modifier = Modifier.fillMaxWidth(),
                title = "Sound and vibration"
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
                icon = R.drawable.vibration_24px,
                title = "Vibration",
                onClick = { onUpdateVibration(!appSettings.vibration) },
                content = {
                    Switch(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        checked = appSettings.vibration,
                        onCheckedChange = onUpdateVibration
                    )
                }
            )

            SettingsGroupTitle(
                modifier = Modifier.fillMaxWidth(),
                title = "Appearance"
            )
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.theme,
                title = "App Theme",
                subTitle = stringResource(appSettings.theme.description),
                onClick = { onUpdateThemeDialogVisibility(true) },
                content = {}
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SettingsRow1(
                    modifier = Modifier.fillMaxWidth(),
                    icon = R.drawable.palette_24px,
                    title = "Dynamic colors",
                    subTitle = "Use device wallpaper colors",
                    onClick = { onUpdateDynamicTheme(!appSettings.dynamicColor) },
                    content = {
                        Switch(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            checked = appSettings.dynamicColor,
                            onCheckedChange = onUpdateDynamicTheme
                        )
                    }
                )
            }

            SettingsGroupTitle(
                modifier = Modifier.fillMaxWidth(),
                title = "Home screen"
            )
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.clock_icon,
                title = "Time machine",
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
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.card,
                title = "Cards",
                onClick = {
                    onNavigateToScreen(SettingsCard)
//                    onUpdatePaneContent(SettingsPaneContent.CARD_WEATHER_QUANTITY, SettingsPaneContent.GENERAL)
                          },
                content = {}
            )

            Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding() + 16.dp))

        }
    }
}