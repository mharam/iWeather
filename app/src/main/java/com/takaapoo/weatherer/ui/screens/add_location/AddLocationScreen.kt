package com.takaapoo.weatherer.ui.screens.add_location

import android.annotation.SuppressLint
import android.graphics.Point
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.DisappearingScaleBar
import com.google.maps.android.compose.widgets.ScaleBar
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.viewModels.AddLocationViewModel

val indicatingCircleDiameter = 200.dp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddLocationScreen(
    modifier: Modifier = Modifier,
    addViewModel: AddLocationViewModel = hiltViewModel(),
    navController: NavController
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val mapState by addViewModel.mapState.collectAsStateWithLifecycle()

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
        targetValue = if (mapState.indicatingCircleVisible) 1f else 0f,
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
        addViewModel.updateLocationPermissionGranted(
            isLocationPermissionGranted(context)
        )
        addViewModel.updateDeviceLocationEnabled(
            isLocationEnabled(context)
        )
        addViewModel.triggerMapRelocation = 0
    }
    LaunchedEffect(key1 = addViewModel.triggerMapRelocation){
        val useBoundingBox = mapState.boundingBox != null && mapState.boundingBox!!.size == 4
        cameraPositionState.animate(
            update = if (useBoundingBox)
                CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds(
                        LatLng(mapState.boundingBox!![1], mapState.boundingBox!![0]),
                        LatLng(mapState.boundingBox!![3], mapState.boundingBox!![2])
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
        if (addViewModel.triggerMapRelocation > 0)
            addViewModel.showIndicatingCircle()
    }
    LaunchedEffect(mapState.addResult){
        when(mapState.addResult){
            is MyResult.Success -> {
                snackbarHostState.showSnackbar(
                    (mapState.addResult as MyResult.Success<*>).data as String
                )
                addViewModel.resetAddResult()
            }
            is MyResult.Error -> {
                val exception = (mapState.addResult as MyResult.Error).exception
                val message = (mapState.addResult as MyResult.Error).message
                snackbarHostState.showSnackbar(
                    when {
                        exception?.message != null -> exception.message!!
                        message != null  -> message
                        else -> "Some error occurred"
                    }
                )
                addViewModel.resetAddResult()
            }
            is MyResult.Loading -> {}
        }
    }

    if (mapState.handleShowingLocation){
        HandleLocationRequest(
            onDismiss = { addViewModel.onMyLocationPressed(false) },
            goToMyLocation = { addViewModel.goToMyLocation(context) },
            updateLocationPermissionGrant = { granted:Boolean ->
                addViewModel.updateLocationPermissionGranted(granted)
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier,
        floatingActionButton = {
            MapFAB(
                onMyLocationPressed = {
                    addViewModel.onMyLocationPressed(it)
                },
                mapState = mapState,
                screenCenter = cameraPositionState.position.target
            )
        },
        topBar = {
            MapTopBar(
                mapState = mapState,
                onSearchQueryChange = { query: String, getLocation: Boolean ->
                    addViewModel.onSearchQueryChange(query, getLocation)
                },
                navigateUp = navController::navigateUp,
                goToLocation = { latLng: LatLng, boundingBox: DoubleArray? ->
                    addViewModel.goToLocation(latLng, boundingBox)
                }
            )
        }
    ) {
        Box(modifier = modifier.fillMaxSize()){
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapState.properties,
                uiSettings = mapState.uiSettings,
                onMapLongClick = {
                    addViewModel.updateSelectedLocationLatLng(it)
                    addViewModel.updateShowAddLocationDialog(true)
                }
            )
            ScaleBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(bottom = 8.dp, start = 8.dp)
                    .align(Alignment.BottomStart),
//                height = 60.dp,
                cameraPositionState = cameraPositionState,
//                visibilityDurationMillis = 2000
            )
            if (mapState.indicatingCircleVisible) {
                val colorStops = arrayOf(
                    radius - 0.2f to Color.Transparent,
                    radius to Color(red = 1f, green = 0.3f, blue = 0f, alpha = 1-radius),
                    radius + 0.2f to Color.Transparent,
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
        }
        if (mapState.showAddLocationDialog){
            AddLocationDialog(
                onDismissRequest = {
                    addViewModel.updateShowAddLocationDialog(false)
                },
                onConfirmation = {
                    addViewModel.addLocationToDB(
                        Location(
                            name = mapState.selectedLocationName,
                            latitude = mapState.selectedLocationLatLng.latitude.toFloat(),
                            longitude = mapState.selectedLocationLatLng.longitude.toFloat()
                        )
                    )
                    addViewModel.updateShowAddLocationDialog(false)
                },
                latLng = mapState.selectedLocationLatLng,
                locationName = mapState.selectedLocationName,
                onLocationNameChange = {
                    addViewModel.updateSelectedLocationName(it)
                },
                nameAlreadyExists = mapState.nameAlreadyExists
            )
        }
    }

}



@Preview(showBackground = true)
@Composable
fun AddLocationScreenPreview() {
    WeathererTheme {
        AddLocationScreen(navController = rememberNavController())
    }
}