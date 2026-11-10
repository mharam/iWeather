package com.takaapoo.weatherer.ui.screens.radar

import android.Manifest
import android.app.Activity
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.takaapoo.weatherer.ui.screens.add_location.isLocationPermissionGranted

@Composable
fun MyLocationDropdownMenuItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onPermissionGranted: () -> Unit,
    onRequestPermission: () -> Unit,
    onRequestRationale: () -> Unit,
) {
    val context = LocalContext.current

    DropdownMenuItem(
        modifier = modifier,
        text = {
            Text(
                text = "My Location",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        },
        shape = MenuDefaults.leadingItemShape,
        onClick = {
            onClick()
            when {
                isLocationPermissionGranted(context) -> onPermissionGranted()
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity, Manifest.permission.ACCESS_FINE_LOCATION
                ) -> onRequestRationale()
                else -> onRequestPermission()
            }
        }
    )
}
