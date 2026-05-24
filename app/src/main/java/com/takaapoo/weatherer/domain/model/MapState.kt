package com.takaapoo.weatherer.domain.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.takaapoo.weatherer.data.remote.Location
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.utility.MapStyle

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
    val addResult: MyResult<*> = MyResult.Loading(),
    val addLocationDialogVisible: Boolean = false,
    val donateDialogVisible: Boolean = false,
    val locationCountDialogVisible: Boolean = false,
    val nameAlreadyExists: Boolean = false,
    val selectedLocationLatLng: LatLng = LatLng(0.0, 0.0),
    val triggerMapRelocation: Int = 0,
    val weatherDataLoadStatus: Map<Int, MyResult<*>> = emptyMap()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapState

        if (currentZoom != other.currentZoom) return false
        if (handleShowingLocation != other.handleShowingLocation) return false
        if (locationPermissionGranted != other.locationPermissionGranted) return false
        if (deviceLocationEnabled != other.deviceLocationEnabled) return false
        if (indicatingCircleVisible != other.indicatingCircleVisible) return false
        if (addLocationDialogVisible != other.addLocationDialogVisible) return false
        if (donateDialogVisible != other.donateDialogVisible) return false
        if (locationCountDialogVisible != other.locationCountDialogVisible) return false
        if (nameAlreadyExists != other.nameAlreadyExists) return false
        if (triggerMapRelocation != other.triggerMapRelocation) return false
        if (properties != other.properties) return false
        if (uiSettings != other.uiSettings) return false
        if (searchQuery != other.searchQuery) return false
        if (locations != other.locations) return false
        if (destinationLocationLatLng != other.destinationLocationLatLng) return false
        if (userLocationLatLng != other.userLocationLatLng) return false
        if (!boundingBox.contentEquals(other.boundingBox)) return false
        if (selectedLocationName != other.selectedLocationName) return false
        if (addResult != other.addResult) return false
        if (selectedLocationLatLng != other.selectedLocationLatLng) return false
        if (weatherDataLoadStatus != other.weatherDataLoadStatus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentZoom.hashCode()
        result = 31 * result + handleShowingLocation.hashCode()
        result = 31 * result + locationPermissionGranted.hashCode()
        result = 31 * result + deviceLocationEnabled.hashCode()
        result = 31 * result + indicatingCircleVisible.hashCode()
        result = 31 * result + addLocationDialogVisible.hashCode()
        result = 31 * result + donateDialogVisible.hashCode()
        result = 31 * result + locationCountDialogVisible.hashCode()
        result = 31 * result + nameAlreadyExists.hashCode()
        result = 31 * result + triggerMapRelocation
        result = 31 * result + properties.hashCode()
        result = 31 * result + uiSettings.hashCode()
        result = 31 * result + searchQuery.hashCode()
        result = 31 * result + locations.hashCode()
        result = 31 * result + destinationLocationLatLng.hashCode()
        result = 31 * result + userLocationLatLng.hashCode()
        result = 31 * result + (boundingBox?.contentHashCode() ?: 0)
        result = 31 * result + selectedLocationName.hashCode()
        result = 31 * result + addResult.hashCode()
        result = 31 * result + selectedLocationLatLng.hashCode()
        result = 31 * result + weatherDataLoadStatus.hashCode()
        return result
    }
}