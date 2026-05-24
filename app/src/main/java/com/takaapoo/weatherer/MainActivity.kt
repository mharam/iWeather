package com.takaapoo.weatherer

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import android.provider.Settings.EXTRA_APP_PACKAGE
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.datastore.dataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationSettingsStates
import com.takaapoo.weatherer.domain.model.AppSettingsSerializer
import com.takaapoo.weatherer.main.DataRefreshReceiver
import com.takaapoo.weatherer.main.LocationProviderChangeReceiver
import com.takaapoo.weatherer.ui.screens.AppScreen
import com.takaapoo.weatherer.ui.screens.add_location.REQUEST_CHECK_SETTINGS
import com.takaapoo.weatherer.ui.screens.updateAllWidgets
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.viewModels.AddLocationViewModel
import com.takaapoo.weatherer.ui.viewModels.HomeViewModel
import com.takaapoo.weatherer.ui.viewModels.PreferenceViewModel
import com.takaapoo.weatherer.utility.getWindowSize
import com.takaapoo.weatherer.utility.readMoonPhaseBinFromAssets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import java.util.Calendar


val Context.dataStore by dataStore(
    fileName = "app_settings.json",
    serializer = AppSettingsSerializer
)
lateinit var moonPhaseMap: Map<String, JsonElement>
lateinit var moonPhase: List<Float>
const val WEATHER_CHANNEL_ID = "WEATHER_CHANNEL"
const val WEATHER_NOTIFICATION_ID = 1001


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
    lateinit var notificationManager: NotificationManager
    lateinit var alarmManager: AlarmManager

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationPermission = permissions.getOrDefault(
                Manifest.permission.ACCESS_FINE_LOCATION,
                null
            )
            val coarseLocationPermission = permissions.getOrDefault(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                null
            )
            val notificationPermission = permissions.getOrDefault(
                Manifest.permission.POST_NOTIFICATIONS,
                null
            )

            if (fineLocationPermission == true || coarseLocationPermission == true) {
                addLocationViewModel.updateLocationPermissionGranted(true)
                addLocationViewModel.goToMyLocation(this)
            }
            if (fineLocationPermission == false && coarseLocationPermission == false) {
                addLocationViewModel.updateLocationPermissionGranted(false)
                Toast.makeText(this, "Location Permission NOT Granted!", Toast.LENGTH_LONG).show()
            }
            if (notificationPermission == false) {
                preferenceViewModel.updateNotificationVisibility(false)
            } else if (notificationPermission == true) {
                preferenceViewModel.updateDisplayNotificationTrigger()
            }
        }

    var goneToNotificationPermissionSettings = false
    var goneToAlarmPermissionSettings = false
    var enterAppRecently = true

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
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager


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
                        Log.i("delete1", "locationListState 3 = ${homeViewModel.locationsState.value.map { it.locationId }}")
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    }
                }
            }
        )
        createNotificationChannel()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        getWindowSize(windowManager).let {
            windowHeight = it.height
            windowWidth = it.width
        }

        /*if (Build.VERSION.SDK_INT > 29) {
            windowHeight = windowManager.currentWindowMetrics.bounds.height()
            windowWidth = windowManager.currentWindowMetrics.bounds.width()
        } else {
            val outSize = Point()
            windowManager.defaultDisplay.getSize(outSize)
//            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            windowHeight = outSize.y
            windowWidth = outSize.x
        }*/

        appName = applicationInfo.loadLabel(packageManager).toString()
        locationChangeReceiver = LocationProviderChangeReceiver(
            onUpdateDeviceLocationEnabled = {
                addLocationViewModel.updateDeviceLocationEnabled(it)
            }
        )
//        moonPhaseMap = Json.parseToJsonElement(readJSONFromAssets(this, "moon_phase.json"))
//            .jsonObject.toMap()
        moonPhase = readMoonPhaseBinFromAssets(this, "moon_phase.bin")

        setContent {
            val appSettings by preferenceViewModel.appSettings.collectAsStateWithLifecycle()
            val locationListState by homeViewModel.locationsState.collectAsStateWithLifecycle()
            if (locationListState.firstOrNull()?.locationId != -2){
                WeathererTheme(
                    appSettings = appSettings
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
                            preferenceViewModel = preferenceViewModel,
                            appSettings = appSettings,
                            locationListState = locationListState.toImmutableList(),
                        )
                    }
                }
            }
        }
        ContextCompat.registerReceiver(
            this,
            locationChangeReceiver,
            IntentFilter(LocationManager.MODE_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
//        Intent(this, MainActivity::class.java)
//        Intent("miui.intent.action.OP_AUTO_START")
//        val intent = Intent().apply {
//            component = ComponentName(
//                "com.miui.securitycenter",
//                "com.miui.permcenter.autostart.AutoStartManagementActivity"
//            )
//        }
//        startActivity(intent)
//        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
//        if (Build.VERSION.SDK_INT >= 28) {
//            if (activityManager.isBackgroundRestricted) {
//                Log.i("restrict1", "it is Background restricted")
//            }
//        }
        setDataRefreshAlarm(context = this)
        lifecycleScope.launch {
            updateAllWidgets(this@MainActivity)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!notificationManager.areNotificationsEnabled() ||
            (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms() &&
                    (!goneToNotificationPermissionSettings || goneToAlarmPermissionSettings) &&
                    !enterAppRecently)
            ) {
            preferenceViewModel.updateNotificationVisibility(false)
        }
        goneToNotificationPermissionSettings = false
        goneToAlarmPermissionSettings = false
        enterAppRecently = false

        preferenceViewModel.updateDisplayNotificationTrigger()
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
                if (resultCode == RESULT_OK){
                    lifecycleScope.launch {
                        delay(150)
                        addLocationViewModel.goToMyLocation(this@MainActivity)
                    }
                }
            }
        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String?>,
//        grantResults: IntArray,
//        deviceId: Int
//    ) {
//        Log.i("notif1", "onRequestPermissionsResult}")
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
//        Log.i("notif1", "requestCode = $requestCode}")
//        if (requestCode == REQUEST_NOTIFICATION_PERMISSION_CODE) {
//            Log.i("notif1", "grantResults = ${grantResults.toList()}")
//            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED)
//                startNotificationService()
//            else
//                preferenceViewModel.updateNotificationVisibility(false)
//        }
//    }



    fun launchRequestLocationPermission(){
        permissionRequestLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(WEATHER_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            goneToNotificationPermissionSettings = true
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS))
                permissionRequestLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            else {
                goToNotificationSettings()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun goToNotificationSettings(){
        preferenceViewModel.updateNotificationVisibility(false)
        val intent = Intent(ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(EXTRA_APP_PACKAGE, packageName)
            flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    fun goToAlarmSettings(){
        goneToAlarmPermissionSettings = true
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${packageName}")
            flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}

fun setDataRefreshAlarm(context: Context){
    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
    val dataRefreshIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, DataRefreshReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(dataRefreshIntent)
    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        add(Calendar.HOUR_OF_DAY, 1)
    }
    if (Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms())
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            dataRefreshIntent
        )
    else
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1 * AlarmManager.INTERVAL_HOUR,
            dataRefreshIntent
        )
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