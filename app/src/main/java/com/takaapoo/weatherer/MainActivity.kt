package com.takaapoo.weatherer

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.datastore.dataStore
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationSettingsStates
import com.takaapoo.weatherer.domain.model.AppSettingsSerializer
import com.takaapoo.weatherer.ui.screens.AppScreen
import com.takaapoo.weatherer.ui.screens.add_location.REQUEST_CHECK_SETTINGS
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.utility.readJSONFromAssets
import com.takaapoo.weatherer.ui.viewModels.AddLocationViewModel
import com.takaapoo.weatherer.ui.viewModels.HomeViewModel
import com.takaapoo.weatherer.ui.viewModels.PreferenceViewModel
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

//    private val displayMetrics = DisplayMetrics()
    var windowHeight = 0
    var windowWidth = 0

    private val homeViewModel: HomeViewModel by viewModels()
    private val addLocationViewModel: AddLocationViewModel by viewModels()
    private val preferenceViewModel: PreferenceViewModel by viewModels()
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
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Transparent.toArgb(),
                darkScrim = Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Transparent.toArgb(),
                darkScrim = Transparent.toArgb()
            )
        )
        // Set up an OnPreDrawListener to the root view.
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Check whether the initial data is ready.
                    return if (homeViewModel.locationsState.value.firstOrNull()?.locationId == -2) {
                        // The content isn't ready. Suspend.
                        false
                    } else {
                        // The content is ready. Start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    }
                }
            }
        )


        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT > 29) {
            windowHeight = windowManager.currentWindowMetrics.bounds.height()
            windowWidth = windowManager.currentWindowMetrics.bounds.width()
        } else {
            val outSize = Point()
            windowManager.defaultDisplay.getSize(outSize)
//            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            windowHeight = outSize.y
            windowWidth = outSize.x
        }
//Log.i("size1", "windowWidth = ${windowWidth} , windowHeight = ${windowHeight}")
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
            WeathererTheme(
                preferenceViewModel = preferenceViewModel
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScreen(
                        modifier = Modifier.fillMaxSize(),
                        homeViewModel = homeViewModel,
                        addLocationViewModel = addLocationViewModel,
                        preferenceViewModel = preferenceViewModel
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


//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    WeathererTheme {
//        HomeScreen(
//            appSettings = AppSettings(),
//            onMenuButtonClick = {},
//            initialFirstItemIndex = 0,
//            onNavigateToDetailScreen = {_, _ ->},
//            onNavigateToAddLocationScreen = {}
//        )
//    }
//}