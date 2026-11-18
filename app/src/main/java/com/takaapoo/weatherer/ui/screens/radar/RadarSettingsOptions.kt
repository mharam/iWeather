package com.takaapoo.weatherer.ui.screens.radar

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.LocationsBriefState
import com.takaapoo.weatherer.domain.model.RadarState
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.MySelectableDropdownMenuItem
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.utility.getMyLocationResolvable
import com.takaapoo.weatherer.utility.toDp
import kotlinx.coroutines.launch
import java.time.Clock

enum class RadarClocks(@param:StringRes val nameId: Int){
    UTC(nameId = R.string.hourly_diagram_utc_title),
    DEVICE(nameId = R.string.hourly_diagram_device_title);

    fun toClock(): Clock {
        return when (this) {
            RadarClocks.UTC -> Clock.systemUTC()
            RadarClocks.DEVICE -> Clock.systemDefaultZone()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarSettingsOptions(
    modifier: Modifier = Modifier,
    radarState: RadarState = RadarState(),
    locationState: List<LocationsBriefState>,
    titleHeight: Dp,

    onUpdateSettingsOpen: (open: Boolean) -> Unit,
    onUpdateCrossHairVisibility: (visible: Boolean) -> Unit,
    onUpdateElevationVisibility: (visible: Boolean) -> Unit,
    onUpdateClockGaugeVisibility: (visible: Boolean) -> Unit,
    onUpdateRadarClock: (RadarClocks) -> Unit,
    onUpdateLocation: (location: Pair<Float, Float>) -> Unit,
) {
    val locationMenuHorizontalPadding = 8.dp
    val context = LocalContext.current
    val mainActivity = LocalActivity.current as MainActivity
    val density = LocalDensity.current
    var locationMenuExpanded by remember { mutableStateOf(false) }
    var clockMenuExpanded by remember { mutableStateOf(false) }

    var menu1Position by remember { mutableStateOf(Offset.Zero) }
    var locationItemPosition by remember { mutableStateOf(Offset.Zero) }
    var clockItemPosition by remember { mutableStateOf(Offset.Zero) }

    var menu1Size by remember { mutableStateOf(IntSize.Zero) }
    var clockMenuSize by remember { mutableStateOf(IntSize.Zero) }
    var locationMenuSize by remember { mutableStateOf(IntSize.Zero) }

    val mainMenuYOffset = 8.dp
    val coroutineScope = rememberCoroutineScope()

    var rationalDialogVisible by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            coroutineScope.launch {
                getMyLocationResolvable(
                    mainActivity = mainActivity,
                    pendingLocationActionCode = 2,
                    onLocationResult = { latitude, longitude ->
                        onUpdateLocation(latitude.toFloat() to longitude.toFloat())
                    }
                )
            }
        } else {
            Toast.makeText(
                context,
                "Location Permission NOT Granted!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    if (rationalDialogVisible) {
        AlertDialog(
            onDismissRequest = { rationalDialogVisible = false },
            icon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
            confirmButton = {
                TextButton(
                    onClick = {
                        rationalDialogVisible = false
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text(
                        text = "Ok",
                        fontFamily = FontFamily.Default
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { rationalDialogVisible = false }
                ) {
                    Text(
                        text = "Dismiss",
                        fontFamily = FontFamily.Default
                    )
                }
            },
            title = {
                Text(text = "Location Access")
            },
            text = {
                Text(
                    text = "To show your location, ${mainActivity.appName} needs location access permission",
                    fontFamily = FontFamily.Default
                )
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .size(titleHeight)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                )
        ) {
            IconButton(
                onClick = {
                    onUpdateSettingsOpen(!radarState.settingsOpen)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.settings_24px),
                    contentDescription = stringResource(id = R.string.settings),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Column(
            modifier = Modifier,
        ) {
            DropdownMenu(
                modifier = Modifier
                    .onGloballyPositioned {
                        menu1Position = it.positionInParent()
                        menu1Size = it.size
                    }
                    .widthIn(min = 220.dp)
                    .padding(horizontal = 4.dp),
                expanded = radarState.settingsOpen,
                offset = DpOffset(x = 0.dp, y = mainMenuYOffset),
                onDismissRequest = {
                    onUpdateSettingsOpen(false)
                },
                shape = MenuDefaults.standaloneGroupShape,
                properties = PopupProperties()
//                containerColor = Transparent,
//                tonalElevation = 0.dp,
//                shadowElevation = 0.dp
            ) {
                DropdownMenuItem(
                    modifier = Modifier.clip(
                        MenuDefaults.leadingItemShape
                    ),
                    text = {
                        Text(
                            text = stringResource(
                                id = if (radarState.crossHairVisible) R.string.hide_cross_hair
                                else R.string.show_cross_hair
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.cross_hair_24px),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onUpdateCrossHairVisibility(!radarState.crossHairVisible)
                    }
                )
                DropdownMenuItem(
                    modifier = Modifier.clip(
                        MenuDefaults.middleItemShape
                    ),
                    text = {
                        Text(
                            text = stringResource(
                                id = if (radarState.elevationVisible) R.string.hide_elevation
                                else R.string.show_elevation
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.freeze_height),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onUpdateElevationVisibility(!radarState.elevationVisible)
                    }
                )
                DropdownMenuItem(
                    modifier = Modifier.clip(
                        MenuDefaults.middleItemShape
                    ),
                    text = {
                        Text(
                            text = stringResource(
                                id = if (radarState.clockGaugeVisible) R.string.hide_clock_gauge
                                else R.string.show_clock_gauge
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.clock_icon),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onUpdateClockGaugeVisibility(!radarState.clockGaugeVisible)
                    }
                )
                DropdownMenuItem(
                    modifier = Modifier
                        .onGloballyPositioned {
                            clockItemPosition = it.positionInParent()
                        }
                        .clip(MenuDefaults.middleItemShape),
                    text = {
                        Text(
                            text = stringResource(id = R.string.time_zone),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.globe_clock_24dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.rotate(90f),
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        clockMenuExpanded = true
                    }
                )



                HorizontalDivider(modifier = Modifier.padding(8.dp))
                DropdownMenuItem(
                    modifier = Modifier
                        .onGloballyPositioned {
                            locationItemPosition = it.positionInParent()
                        }
                        .clip(
                            MenuDefaults.trailingItemShape
                        ),
                    text = {
                        Text(
                            text = stringResource(id = R.string.go_to),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.location_on_24px),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.rotate(90f),
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        locationMenuExpanded = true
                    }
                )
            }
            DropdownMenu(
                modifier = Modifier
                    .onGloballyPositioned {
                        clockMenuSize = it.size
                    },
                expanded = clockMenuExpanded,
                onDismissRequest = {
                    clockMenuExpanded = false
                    locationMenuExpanded = false
                },
                offset = DpOffset(
                    x = (menu1Position.x - menu1Size.width - clockMenuSize.width).toDp(density),
                    y = (menu1Position.y + clockItemPosition.y).toDp(density) + mainMenuYOffset
                ),
                shape = MenuDefaults.standaloneGroupShape,
            ) {
                RadarClocks.entries.forEachIndexed { itemIndex, clock ->
                    MySelectableDropdownMenuItem(
                        modifier = Modifier,
                        selected = radarState.clock == clock.toClock(),
                        text = stringResource(id = clock.nameId),
                        itemIndex = itemIndex,
                        totalItemCount = RadarClocks.entries.size,
                        onClick = {
                            onUpdateRadarClock(clock)
                        }
                    )
                }
            }



            DropdownMenu(
                modifier = Modifier
                    .onGloballyPositioned {
                        locationMenuSize = it.size
                    },
                expanded = locationMenuExpanded,
                onDismissRequest = {
                    clockMenuExpanded = false
                    locationMenuExpanded = false
                },
                offset = DpOffset(
                    x = (menu1Position.x - menu1Size.width - locationMenuSize.width).toDp(density) +
                            locationMenuHorizontalPadding,
                    y = (menu1Position.y + locationItemPosition.y).toDp(density) + mainMenuYOffset
                ),
//                shape = MenuDefaults.standaloneGroupShape,
                containerColor = Transparent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = locationMenuHorizontalPadding,
                        end = locationMenuHorizontalPadding,
                        bottom = 16.dp
                    )
                ) {
                    DropdownMenuGroup(
                        shapes = MenuDefaults.groupShape(
                            index = 0,
                            count = 2
                        ),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        MyLocationDropdownMenuItem(
                            modifier = Modifier,
                            onClick = {
                                locationMenuExpanded = false
                                onUpdateSettingsOpen(false)
                            },
                            onPermissionGranted = {
                                coroutineScope.launch {
                                    getMyLocationResolvable(
                                        mainActivity = mainActivity,
                                        pendingLocationActionCode = 2,
                                        onLocationResult = { latitude, longitude ->
                                            onUpdateLocation(latitude.toFloat() to longitude.toFloat())
                                        }
                                    )
                                }
                            },
                            onRequestRationale = {
                                rationalDialogVisible = true
                            },
                            onRequestPermission = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        )
                    }
                    Spacer(
                        modifier = Modifier.height(
                            MenuDefaults.GroupSpacing
                        )
                    )
                    DropdownMenuGroup(
                        modifier = Modifier.heightIn(max = 140.dp)
                            .verticalScroll(rememberScrollState()),
                        shapes = MenuDefaults.groupShape(
                            index = 1,
                            count = 2
                        ),
                    ) {
                        locationState.forEachIndexed { itemIndex, location ->
                            val itemShape =
                                if (locationState.size == 1) MenuDefaults.standaloneItemShape
                                else {
                                    when (itemIndex) {
                                        0 -> MenuDefaults.leadingItemShape
                                        locationState.size - 1 -> MenuDefaults.trailingItemShape
                                        else -> MenuDefaults.middleItemShape
                                    }
                                }
                            DropdownMenuItem(
                                modifier = Modifier,
                                text = {
                                    Text(
                                        text = location.locationName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                shape = itemShape,
                                onClick = {
                                    locationMenuExpanded = false
                                    onUpdateSettingsOpen(false)
                                    val latitude = locationState[itemIndex].latitude
                                    val longitude = locationState[itemIndex].longitude

                                    onUpdateLocation(latitude to longitude)
                                }
                            )
                        }
                    }
                }
            }
//            DropdownMenu(
//                modifier = Modifier
//                    .onGloballyPositioned {
//                        themeMenuSize = it.size
//                    },
//                expanded = themeMenuExpanded,
//                onDismissRequest = {
//                    themeMenuExpanded = false
//                    gridMenuExpanded = false
//                },
//                offset = DpOffset(
//                    x = (menu1Position.x - menu1Size.width - themeMenuSize.width).toDp(density),
//                    y = (menu1Position.y + themeItemPosition.y).toDp(density)
//                )
//            ) {
//                ChartTheme.entries.forEach { theme ->
//                    DropdownMenuItem(
//                        text = {
//                            Text(
//                                text = stringResource(id = theme.nameId),
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.onSurface
//                            )
//                        },
//                        leadingIcon = {
//                            if (radarState.chartTheme == theme) {
//                                Icon(
//                                    imageVector = Icons.Rounded.Check,
//                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
//                                    contentDescription = null
//                                )
//                            }
//                        },
//                        onClick = {
//                            onUpdateChartTheme(theme)
//                        }
//                    )
//                }

        }
    }
}