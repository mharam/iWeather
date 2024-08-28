package com.takaapoo.weatherer.ui.viewModels

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.LatLng
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.MapState
import com.takaapoo.weatherer.domain.use_case.AddLocationUseCase
import com.takaapoo.weatherer.domain.use_case.UpdateWeatherUseCase
import com.takaapoo.weatherer.ui.screens.add_location.REQUEST_CHECK_SETTINGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow


@HiltViewModel
class AddLocationViewModel @Inject constructor(
    private val updateWeatherUseCase: UpdateWeatherUseCase,
    private val addLocationUseCase: AddLocationUseCase
): ViewModel() {

    private val origin = LatLng(0.0, 0.0)
    private val _mapState = MutableStateFlow(MapState(destinationLocationLatLng = origin))
    val mapState = _mapState.asStateFlow()
    var triggerMapRelocation: Int by mutableIntStateOf(0)

    fun resetAddResult(){
        _mapState.update {
            it.copy(addResult = MyResult.Loading())
        }
    }

    fun addLocationToDB(location: Location) {
        viewModelScope.launch {
            when (val result = addLocationUseCase(location)){
                is MyResult.Success -> {
                    _mapState.update {
                        it.copy(
                            addResult = MyResult.Success("${location.name} added successfully")
                        )
                    }
                    launch {
                        updateWeatherUseCase(location.copy(id = result.data))
                    }
//                    launch {
//                        addLocationUseCase.setLocationTime(location)
//                    }
                }
                is MyResult.Error -> {
                    _mapState.update {
                        it.copy(
                            addResult = MyResult.Error(exception = result.exception, message = result.message)
                        )
                    }
                }
                is MyResult.Loading -> {}
            }
        }
    }

    fun updateLocationPermissionGranted(granted: Boolean){
        _mapState.update {
            it.copy(
                locationPermissionGranted = granted
            )
        }
    }

    fun updateDeviceLocationEnabled(enabled: Boolean){
        _mapState.update {
            it.copy(
                deviceLocationEnabled = enabled
            )
        }
    }

    private fun updateUserLocation(latLng: LatLng){
        _mapState.update {
            it.copy(
                userLocationLatLng = latLng
            )
        }
    }

    fun updateShowAddLocationDialog(state: Boolean){
        _mapState.update {
            it.copy(
                showAddLocationDialog = state,
                selectedLocationName = ""
            )
        }
    }

    fun updateSelectedLocationLatLng(latLng: LatLng){
        _mapState.update {
            it.copy(
                selectedLocationLatLng = latLng
            )
        }
    }

    fun updateSelectedLocationName(name: String){
        viewModelScope.launch {
            _mapState.update {
                it.copy(
                    selectedLocationName = name,
                    nameAlreadyExists = addLocationUseCase.countLocationWithName(name) > 0
                )
            }
        }
    }

    fun showIndicatingCircle(){
        viewModelScope.launch {
            delay(900)
            _mapState.emit(
                _mapState.value.copy(
                    indicatingCircleVisible = true
                )
            )
            delay(7000)
            _mapState.emit(
                _mapState.value.copy(
                    indicatingCircleVisible = false
                )
            )
        }
    }

    fun onSearchQueryChange(newQuery: String, getLocation: Boolean = true){
        _mapState.update {
            it.copy(searchQuery = newQuery)
        }
        if (newQuery.length > 1 && getLocation){
            getLocation(newQuery)
        } else {
            _mapState.update {
                it.copy(locations = emptyList())
            }
        }
    }

    private fun getLocation(text: String){
        viewModelScope.launch {
            val locationResult = addLocationUseCase.getLocation(text)
            if (locationResult is MyResult.Success) {
                _mapState.update {
                    it.copy(locations = locationResult.data.features)
                }
            }
        }
    }

    fun goToLocation(latLng: LatLng, boundingBox: DoubleArray? = null, zoom: Float = 16f){
        val minLatitudeDifference = 180 / 2.0.pow(zoom.toDouble())
        val okBoundingBox = boundingBox?.run {
            if (this[3] - this[1] > minLatitudeDifference) this
            else null
        }
        _mapState.update {
            it.copy(
                destinationLocationLatLng = latLng,
                currentZoom = zoom,
                boundingBox = okBoundingBox
            )
        }
        triggerMapRelocation++
    }

    fun onMyLocationPressed(state: Boolean){
        _mapState.update {
            it.copy(handleShowingLocation = state)
        }
    }

    fun goToMyLocation(context: Context){
        val locationRequest = LocationRequest.Builder(10000)
            .setMinUpdateIntervalMillis(10000)
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)

        settingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener { locationSettingsResponse ->
                // All location settings are satisfied. The client can initialize location requests here.
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { location : android.location.Location? ->
                        location?.let {
                            val locationLatLng = LatLng(it.latitude, it.longitude)
                            goToLocation(locationLatLng)
                            updateUserLocation(locationLatLng)
                        }
                    }
            }.addOnFailureListener { exception ->
                if (exception is ResolvableApiException){
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(
                            context as Activity,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
    }
}

