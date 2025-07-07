package com.takaapoo.weatherer.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.ui.screens.settings.ChooseLocationDialog
import com.takaapoo.weatherer.ui.screens.settings.ChooseTimeAxisDialog
import com.takaapoo.weatherer.ui.screens.settings.SettingsRow1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Widget2ConfigurationScreen(
    modifier: Modifier = Modifier,
    allLocations: List<Location>,
    appSettings: AppSettings,

    onUpdateWidgetLocationId: (id: Int) -> Unit,
    onUpdateWidgetDateTime: (time: HourlyDiagramTime) -> Unit,
    onSaveSettings: () -> Unit,
) {
    var locationDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var timeClockDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }

    if (locationDialogVisible) {
        ChooseLocationDialog(
            locationList = allLocations,
            locationId = appSettings.widgetLocationId,
            onDismiss = {
                locationDialogVisible = false
            },
            onConfirmation = {
                onUpdateWidgetLocationId(it)
            }
        )
    } else if (timeClockDialogVisible) {
        ChooseTimeAxisDialog(
            time = appSettings.widgetDateTime,
            title = "Date & time",
            onDismiss = {
                timeClockDialogVisible = false
            },
            onConfirmation = {
                onUpdateWidgetDateTime(it)
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Widget settings",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = onSaveSettings
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.location_on_24px,
                title = "Location",
                subTitle = allLocations.find { it.id == appSettings.widgetLocationId }?.name,
                onClick = { locationDialogVisible = true },
                content = {}
            )
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.clock_icon,
                title = "Date & Time",
                subTitle = stringResource(appSettings.widgetDateTime.title),
                onClick = { timeClockDialogVisible = true },
                content = {}
            )


            Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
        }
    }

}