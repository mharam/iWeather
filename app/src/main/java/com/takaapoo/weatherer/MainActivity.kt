package com.takaapoo.weatherer

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.datastore.dataStore
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationSettingsStates
import com.takaapoo.weatherer.data.remote.generateControlPoints2
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.AppSettingsSerializer
import com.takaapoo.weatherer.ui.screens.AppScreen
import com.takaapoo.weatherer.ui.screens.add_location.REQUEST_CHECK_SETTINGS
import com.takaapoo.weatherer.ui.screens.home.HomeScreen
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.utility.readJSONFromAssets
import com.takaapoo.weatherer.ui.viewModels.AddLocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

val Context.dataStore by dataStore(
    fileName = "app_settings.json",
    serializer = AppSettingsSerializer
)
lateinit var moonPhaseMap: Map<String, JsonElement>

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val displayMetrics = DisplayMetrics()
    var windowHeight = 0
    var windowWidth = 0

    private val addLocationViewModel: AddLocationViewModel by viewModels()
    lateinit var appName: String
    private lateinit var locationChangeReceiver: BroadcastReceiver

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                        permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Precise or coarse location access granted.
                    addLocationViewModel.updateLocationPermissionGranted(true)
                    addLocationViewModel.goToMyLocation(this)
                }
                else -> {
                    // No location access granted.
                    addLocationViewModel.updateLocationPermissionGranted(false)
                    Toast.makeText(this, "Location Permission NOT Granted!", Toast.LENGTH_LONG).show()
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT > 29) {
            windowHeight = windowManager.currentWindowMetrics.bounds.height()
            windowWidth = windowManager.currentWindowMetrics.bounds.width()
        } else {
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            windowHeight = displayMetrics.heightPixels
            windowWidth = displayMetrics.widthPixels
        }

        appName = applicationInfo.loadLabel(packageManager).toString()
        locationChangeReceiver = LocationProviderChangeReceiver(
            onUpdateDeviceLocationEnabled = {
                addLocationViewModel.updateDeviceLocationEnabled(it)
            }
        )
        moonPhaseMap = Json.parseToJsonElement(
            readJSONFromAssets(this, "moon_phase.json")
        ).jsonObject.toMap()

        setContent {
            WeathererTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScreen(
                        modifier = Modifier.fillMaxSize(),
                        addLocationViewModel = addLocationViewModel
                    )
                }
            }
        }
        ContextCompat.registerReceiver(
            this,
            locationChangeReceiver,
            IntentFilter(LocationManager.MODE_CHANGED_ACTION),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationChangeReceiver)
    }

    @Deprecated("For Resolving Location Settings")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val locationSettingsStates = data?.let { LocationSettingsStates.fromIntent(it) }
        when (requestCode){
            REQUEST_CHECK_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK){
                    lifecycleScope.launch {
                        delay(150)
                        addLocationViewModel.goToMyLocation(this@MainActivity)
                    }
                }
            }
        }
    }

    fun launchRequestPermission(){
        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeathererTheme {
        HomeScreen(
            appSettings = AppSettings(),
            onMenuButtonClick = {},
            initialFirstItemIndex = 0
        )
    }
}