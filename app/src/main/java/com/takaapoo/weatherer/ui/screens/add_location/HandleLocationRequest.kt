package com.takaapoo.weatherer.ui.screens.add_location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.takaapoo.weatherer.MainActivity

const val REQUEST_CHECK_SETTINGS = 1

@Composable
fun HandleLocationRequest(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    goToMyLocation: () -> Unit,
    updateLocationPermissionGrant: (Boolean) -> Unit
) {
    val context = LocalContext.current
    when {
        isLocationPermissionGranted(context) -> {
            updateLocationPermissionGrant(true)
            goToMyLocation()
            onDismiss()
        }
        ActivityCompat.shouldShowRequestPermissionRationale(
            context as Activity, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                AlertDialog(
                    onDismissRequest = onDismiss,
                    icon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                    confirmButton = {
                        TextButton(onClick = {
                            onDismiss()
                            (context as MainActivity).launchRequestPermission()
                        }){
                            Text("Ok")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onDismiss){
                            Text("Dismiss")
                        }
                    },
                    title = {
                        Text(
                            text = "Location Access"
                        )
                    },
                    text = {
                        Text("To show your location, ${(context as MainActivity).appName} needs location access permission")
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    )
                )
            }
        else -> {
            (context as MainActivity).launchRequestPermission()
            onDismiss()
        }
    }
}

fun isLocationPermissionGranted(context: Context) =
    (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED)

fun isLocationEnabled(context: Context?): Boolean{
    val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    var enabled = false
    locationManager?.allProviders?.forEach { provider ->
        enabled = enabled or locationManager.isProviderEnabled(provider)
    }
    return enabled
}