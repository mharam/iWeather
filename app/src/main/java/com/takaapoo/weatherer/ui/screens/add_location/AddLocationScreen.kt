package com.takaapoo.weatherer.ui.screens.add_location

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.ScaleBar
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.MapState
import com.takaapoo.weatherer.domain.model.MaxFreeLocationsCount
import com.takaapoo.weatherer.domain.model.MaxLocationsCount
import com.takaapoo.weatherer.ui.screens.add_location.dialogs.AddLimitDialog
import com.takaapoo.weatherer.ui.screens.add_location.dialogs.AddLocationDialog
import com.takaapoo.weatherer.ui.screens.add_location.dialogs.DonateDialog
import com.takaapoo.weatherer.ui.screens.add_location.dialogs.HelpDialog
import com.takaapoo.weatherer.utility.calculateLocationSurroundingPoints
import com.takaapoo.weatherer.utility.toPx

val indicatingCircleDiameter = 200.dp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddLocationScreen(
    modifier: Modifier = Modifier,
    mapState: MapState,
    locationsCount: Int,
    appSettings: AppSettings,
    onUpdateLocationPermissionGranted: (Boolean) -> Unit,
    onUpdateDeviceLocationEnabled: (Boolean) -> Unit,
    onUpdateTriggerMapRelocation: (Int) -> Unit,
    onShowIndicatingCircle: () -> Unit,
    onResetAddResult: () -> Unit,
    onUpdateShowLocationState: (Boolean) -> Unit,
    onGoToMyLocation: (Context) -> Unit,
    onUpdateSearchQuery: (String, Boolean) -> Unit,
    onGoToLocation: (LatLng, DoubleArray?) -> Unit,
    onUpdateSelectedLocationLatLng: (LatLng) -> Unit,
    onUpdateAddLocationDialogVisibility: (Boolean) -> Unit,
    onAddLocationToDB: (Location) -> Unit,
    onUpdateSelectedLocationName: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onUpdateDonateDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateLocationCountDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateAddLocationHelpVisibility: (visible: Boolean) -> Unit,
    onNavigateToDonateScreen: () -> Unit,
    onShowPrecipitation: (location: LatLng) -> Unit,
    onReInitialize: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapState.destinationLocationLatLng, 1f)
    }
    val initialCircleCenter = Point(-1000, -1000)
    val circleCenter by remember(
        key1 = cameraPositionState.position,
        key2 = mapState.destinationLocationLatLng
    ){
        mutableStateOf(cameraPositionState.projection?.toScreenLocation(mapState.destinationLocationLatLng)
            ?: initialCircleCenter)
    }
    val radius by animateFloatAsState(
        targetValue = if (mapState.indicatingCircleVisible) 1f else -0.3f,
        animationSpec = repeatable(
            iterations = if (mapState.indicatingCircleVisible) 5 else 1,
            animation = TweenSpec(
                durationMillis = if (mapState.indicatingCircleVisible) 1000 else 50,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )

    val context = LocalContext.current
    val density : Density = LocalDensity.current
    val defaultOffset = with(density){ indicatingCircleDiameter.toPx() / 2}.toInt()

    LaunchedEffect(Unit) {
        onUpdateLocationPermissionGranted(isLocationPermissionGranted(context))
        onUpdateDeviceLocationEnabled(isLocationEnabled(context))
        onUpdateTriggerMapRelocation(0)
    }
    LaunchedEffect(key1 = mapState.triggerMapRelocation){
        val useBoundingBox = mapState.boundingBox != null && mapState.boundingBox.size == 4
        runCatching {
            MapsInitializer.initialize(context)
        }.onSuccess {
            cameraPositionState.animate(
                update = if (useBoundingBox)
                    CameraUpdateFactory.newLatLngBounds(
                        LatLngBounds(
                            LatLng(mapState.boundingBox[1], mapState.boundingBox[0]),
                            LatLng(mapState.boundingBox[3], mapState.boundingBox[2])
                        ),
                        0
                    )
                else
                    CameraUpdateFactory.newLatLngZoom(
                        mapState.destinationLocationLatLng,
                        mapState.currentZoom
                    ),
                durationMs = 2000
            )
            if (mapState.triggerMapRelocation > 0)
                onShowIndicatingCircle()
        }
    }
    LaunchedEffect(mapState.addResult){
        when(mapState.addResult){
            is MyResult.Success -> {
                snackbarHostState.showSnackbar(
                    mapState.addResult.data as String
                )
                onResetAddResult()
            }
            is MyResult.Error -> {
                val exception = mapState.addResult.exception
                val message = mapState.addResult.message
                snackbarHostState.showSnackbar(
                    when {
                        exception?.message != null -> exception.message!!
                        message != null  -> message
                        else -> "Some error occurred"
                    }
                )
                onResetAddResult()
            }
            is MyResult.Loading -> {}
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        onResetAddResult()
    }

    if (mapState.handleShowingLocation){
        HandleLocationRequest(
            onDismiss = { onUpdateShowLocationState(false) },
            goToMyLocation = { onGoToMyLocation(context) },
            updateLocationPermissionGrant = { granted:Boolean ->
                onUpdateLocationPermissionGranted(granted)
            }
        )
    }
    var surroundingPoints by remember{ mutableStateOf(emptyList<LatLng>()) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier,
        floatingActionButton = {
            MapFAB(
                onMyLocationPressed = {
                    onUpdateShowLocationState(it)
                },
                mapState = mapState,
                screenCenter = cameraPositionState.position.target
            )
        },
        topBar = {
            MapTopBar(
                mapState = mapState,
                onSearchQueryChange = { query: String, getLocation: Boolean ->
                    onUpdateSearchQuery(query, getLocation)
                },
                navigateUp = onNavigateUp,
                goToLocation = { latLng: LatLng, boundingBox: DoubleArray? ->
                    onGoToLocation(latLng, boundingBox)
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()){
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapState.properties,
                uiSettings = mapState.uiSettings,
                onMapLongClick = {
                    surroundingPoints = calculateLocationSurroundingPoints(
                        location = it,
                        pointsCount = 12
                    )
                    if (locationsCount < MaxLocationsCount) {
                        if (appSettings.hasDonated || locationsCount < MaxFreeLocationsCount) {
                            onUpdateSelectedLocationLatLng(it)
                            onUpdateAddLocationDialogVisibility(true)
                        }
                        else
                            onUpdateDonateDialogVisibility(true)
                    } else {
                        onUpdateLocationCountDialogVisibility(true)
                    }
                    onShowPrecipitation(it)
//                    val locationType = determineLocationType(context, it)
//                    Log.i("loc1", "locationType = $locationType")
                }
            )
            ScaleBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(bottom = 8.dp, start = 8.dp)
                    .align(Alignment.BottomStart),
                cameraPositionState = cameraPositionState,
//                visibilityDurationMillis = 2000
            )
            if (mapState.indicatingCircleVisible) {
                val colorStops = arrayOf(
                    radius - 0.05f to Color.Transparent,
                    radius to MaterialTheme.colorScheme.primary.copy(alpha = (1 - radius)),
                    radius + 0.3f to Color.Transparent,
                )
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                circleCenter.x - defaultOffset,
                                circleCenter.y - defaultOffset
                            )
                        }
                        .align(Alignment.TopStart)
                        .requiredSize(indicatingCircleDiameter)
                        .background(
                            brush = Brush.radialGradient(colorStops = colorStops),
                            shape = CircleShape
                        )
                )
            }

            val pointSize = 4.dp
            val pointRadiusPx = (pointSize.toPx(density) / 2).toInt()
            surroundingPoints.forEach { latLng ->
                cameraPositionState.projection?.toScreenLocation(latLng)?.let { center ->
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(center.x - pointRadiusPx, center.y - pointRadiusPx)
                            }
                            .align(Alignment.TopStart)
                            .requiredSize(pointSize)
                            .background(
                                color = Color.Red,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
        AnimatedVisibility (visible = mapState.addLocationDialogVisible){
            AddLocationDialog(
                onDismissRequest = {
                    onUpdateAddLocationDialogVisibility(false)
                },
                onConfirmation = {
                    onAddLocationToDB(
                        Location(
                            name = mapState.selectedLocationName,
                            latitude = mapState.selectedLocationLatLng.latitude.toFloat(),
                            longitude = mapState.selectedLocationLatLng.longitude.toFloat()
                        )
                    )
                    onUpdateAddLocationDialogVisibility(false)
                },
                latLng = mapState.selectedLocationLatLng,
                locationName = mapState.selectedLocationName,
                onUpdateLocationName = {
                    onUpdateSelectedLocationName(it)
                },
                nameAlreadyExists = mapState.nameAlreadyExists
            )
        }
        AnimatedVisibility (visible = mapState.donateDialogVisible){
            DonateDialog(
                onDismiss = {
                    onUpdateDonateDialogVisibility(false)
                },
                onConfirmation = {
                    onUpdateDonateDialogVisibility(false)
                    onNavigateToDonateScreen()
                },
            )
        }
        AnimatedVisibility (visible = mapState.locationCountDialogVisible){
            AddLimitDialog(
                onDismiss = {
                    onUpdateLocationCountDialogVisibility(false)
                },
            )
        }
        AnimatedVisibility (visible = appSettings.addLocationHelpVisible){
            HelpDialog(
                onDismiss = {
                    onUpdateAddLocationHelpVisibility(false)
                },
            )
        }
    }
    LifecycleEventEffect(event = Lifecycle.Event.ON_PAUSE) {
        onReInitialize()
    }
}


