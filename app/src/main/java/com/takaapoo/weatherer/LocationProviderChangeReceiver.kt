package com.takaapoo.weatherer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.takaapoo.weatherer.ui.screens.add_location.isLocationEnabled

class LocationProviderChangeReceiver(
    val onUpdateDeviceLocationEnabled: (Boolean) -> Unit
): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == LocationManager.MODE_CHANGED_ACTION){
            onUpdateDeviceLocationEnabled(isLocationEnabled(context))
        }
    }
}