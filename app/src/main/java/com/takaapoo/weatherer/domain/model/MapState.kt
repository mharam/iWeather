package com.takaapoo.weatherer.domain.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.takaapoo.weatherer.data.remote.Location
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.ui.utility.MapStyle

data class MapState(
    val properties: MapProperties = MapProperties(
        mapStyleOptions = MapStyleOptions(MapStyle.gowallaStyle),
        mapType = MapType.NORMAL
    ),
    val uiSettings: MapUiSettings = MapUiSettings(
        compassEnabled = false,
        zoomControlsEnabled = false,
        myLocationButtonEnabled = false,
        rotationGesturesEnabled = false
    ),
    val searchQuery: String = "",
    val locations: List<Location> = emptyList(),
    val destinationLocationLatLng: LatLng = LatLng(0.0, 0.0),
    val userLocationLatLng: LatLng = LatLng(-90.0, 0.0),
    val currentZoom: Float = 1f,
    val boundingBox: DoubleArray? = null,
    val handleShowingLocation: Boolean = false,
    val locationPermissionGranted: Boolean = false,
    val deviceLocationEnabled: Boolean = false,
    val indicatingCircleVisible: Boolean = false,
    val selectedLocationName: String = "",
    val addResult: MyResult<String> = MyResult.Loading(),
    val addLocationDialogVisibility: Boolean = false,
    val nameAlreadyExists: Boolean = false,
    val selectedLocationLatLng: LatLng = LatLng(0.0, 0.0),
    val triggerMapRelocation: Int = 0,
    val weatherDataLoadStatus: Map<Int, MyResult<Unit>> = emptyMap<Int, MyResult<Unit>>()
)