package com.takaapoo.weatherer.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.SettingsPaneContent
import com.takaapoo.weatherer.ui.theme.WeathererTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsScreen(
    modifier: Modifier = Modifier,
    appSettings: AppSettings,

    onUpdateNotificationVisibility: (Boolean) -> Unit,
    onUpdatePaneContent: (content: SettingsPaneContent, previousContent: SettingsPaneContent?) -> Unit,
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
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.settings_24px,
                title = "General",
                subTitle = "Theme, appearance, sound",
                onClick = { onUpdatePaneContent(SettingsPaneContent.GENERAL, SettingsPaneContent.MAIN) },
                content = {}
            )
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.length_24px,
                title = "Unit",
                subTitle = "Length, temperature, pressure, speed",
                onClick = { onUpdatePaneContent(SettingsPaneContent.UNIT, SettingsPaneContent.MAIN) },
                content = {}
            )
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.hourly_diagram,
                title = "Hourly diagram",
                subTitle = "Curve, frame",
                onClick = {
                    onUpdatePaneContent(SettingsPaneContent.HOURLY_DIAGRAM, SettingsPaneContent.MAIN)
                          },
                content = {}
            )
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.notifications,
                title = "Persistent notification",
                subTitle = "Notification content",
                dividerVisible = true,
                onClick = { onUpdatePaneContent(SettingsPaneContent.NOTIFICATION, SettingsPaneContent.MAIN) },
                content = {
                    Switch(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        checked = appSettings.notificationVisibility,
                        onCheckedChange = onUpdateNotificationVisibility
                    )
                }
            )

            Spacer(Modifier.height(16.dp))
            /*Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


            }*/
        }

    }
}


@Preview(showBackground = true)
@Composable
fun MainSettingsScreenPreview() {
    WeathererTheme {
        MainSettingsScreen(
            appSettings = AppSettings(),
            onUpdateNotificationVisibility = { _ -> },
            onUpdatePaneContent = { _, _ -> },
            onNavigateUp = {}
        )
    }
}