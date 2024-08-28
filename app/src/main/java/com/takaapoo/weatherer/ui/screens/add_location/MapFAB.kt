package com.takaapoo.weatherer.ui.screens.add_location

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.utils.sphericalDistance
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.MapState

@Composable
fun MapFAB(
    onMyLocationPressed: (Boolean) -> Unit,
    mapState: MapState,
    screenCenter: LatLng
) {
    SmallFloatingActionButton(
        onClick = {
            onMyLocationPressed(true)
        },
        containerColor = MaterialTheme.colorScheme.surface
    ){
        when {
            !mapState.locationPermissionGranted || !mapState.deviceLocationEnabled -> {
                Icon(
                    painter = painterResource(R.drawable.location_disabled_24px),
                    contentDescription = "My location",
                    tint = colorResource(
                        if (isSystemInDarkTheme()) android.R.color.holo_red_light
                        else android.R.color.holo_red_dark
                    )
                )
            }
            mapState.userLocationLatLng.sphericalDistance(screenCenter) > 20 -> {
                Icon(
                    painter = painterResource(R.drawable.location_searching_24px),
                    contentDescription = "My location"
                )
            }
            else -> {
                Icon(
                    painter = painterResource(R.drawable.my_location_24px),
                    contentDescription = "My location",
                    tint = colorResource(
                        if (isSystemInDarkTheme()) android.R.color.holo_blue_bright
                        else android.R.color.holo_blue_dark
                    )
                )
            }
        }
    }
}